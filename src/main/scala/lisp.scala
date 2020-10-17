package lisp

import gram._

object LispPEGs extends PEGs {
	def sexp: PEG[Sexp] = list/quasi/uquot/uqspl/quote/atom
	def atom = """[^'`,@\(\)\s]+""".r^(Name(_).ref)
	def list  = "(" ~> (sexp.* ^ List) <~ ")"
	def quasi = "`" ~> (sexp ^ (Seq(Name("quasi"), _)) ^ List)
	def uquot = "," ~> (sexp ^ (Seq(Name("uquot"), _)) ^ List)
	def uqspl = "@" ~> (sexp ^ (Seq(Name("uqspl"), _)) ^ List)
	def quote = "'" ~> (sexp ^ (Seq(Name("quote"), _)) ^ List)
}

class Scope(out: Option[Scope], pars: List, args: Seq[Sexp]) {
	val table = pars.seq.zip(args).to(collection.mutable.Map)
	def get(name: Sexp): Sexp = {
		if(table.isDefinedAt(name)) table(name)
		else if(out.nonEmpty) out.get.get(name)
		else sys.error(s"$name is not defined")
	}
	def put(name: Name, value: Sexp) = (table(name) = value, value)._2
}

class Forms(out: Option[Scope]) extends Scope(out, List(Seq()), Seq()) {
	def update(name: String, body: Body) = put(Name(name), Native(name, body))
}

trait Sexp {
	def as[V] = asInstanceOf[V]
	def eval(scope: Scope): Sexp
}

import util.Try

case class Name(str: String) extends Sexp {
	override def toString = str
	def ref: Sexp = {
		if(str.matches("^\".*\"$")) Text(str.tail.init)
		else Try(Real(BigDecimal(str))).getOrElse(this)
	}
	override def eval(scope: Scope) = scope.get(this)
}

trait Atom extends Sexp {
	override def eval(scope: Scope) = this
}

case class Bool(bool: Boolean) extends Atom {
	override def toString = bool.toString
}

case class Text(text: String) extends Atom {
	override def toString = "\"%s\"".format(text)
}

case class Real(real: BigDecimal) extends Atom {
	override def toString = real.toString
}

case class List(seq: Seq[Sexp]) extends Sexp {
	override def toString = s"(${seq.mkString(" ")})"
	override def eval(scope: Scope) = seq.head.eval(scope).as[Form](seq.tail, scope)
}

trait Body extends ((Seq[Sexp], Scope) => Sexp)

trait Form extends Atom with Body

case class Native(name: String, body: Body) extends Form {
	override def apply(args: Seq[Sexp], scope: Scope) = body(args, scope)
	override def toString = s"#<native form ${name}>"
}

case class Lambda(pars: List, body: Sexp, out: Scope) extends Form {
	override def toString = s"(lambda $pars $body)"
	override def apply(args: Seq[Sexp], scope: Scope) = {
		body.eval(new Scope(Some(out), pars, args.map(_.eval(scope))))
	}
}

case class Syntax(pars: List, body: Sexp, out: Scope) extends Form {
	override def toString = s"(lambda $pars $body)"
	override def apply(args: Seq[Sexp], scope: Scope) = {
		body.eval(new Scope(Some(out), pars, args)).eval(scope)
	}
}

class Prelude {
	val prelude = getClass.getResourceAsStream("/lisp.lisp")
	for(line <- io.Source.fromInputStream(prelude).getLines()) {
		LispPEGs.sexp(line).map(_.m.eval(Root))
	}
	prelude.close
}

object Repl extends Prelude {
	val jline = new scala.tools.jline.console.ConsoleReader
	jline.setExpandEvents(false)
	jline.setPrompt(s"${Console.BLUE}lisp$$ ${Console.RESET}")
	def main(args: Array[String]) = while(true) try {
		val sexp = LispPEGs.sexp(jline.readLine)
		if(sexp.isDefined) println(sexp.get.m.eval(Root))
		else println(s"${Console.RED}compilation failure")
	} catch {
		case ex: Exception => println(s"${Console.RED} ${ex}")
	}
}

object Root extends Forms(None) {
	this("quote")  = (args, scope) => args.head
	this("list")   = (args, scope) => List(args.map(_.eval(scope)))
	this("car")    = (args, scope) => args.head.eval(scope).as[List].seq.head
	this("cdr")    = (args, scope) => List(args.head.eval(scope).as[List].seq.tail)
	this("lambda") = (args, scope) => new Lambda(args.head.as[List], args(1), scope)
	this("syntax") = (args, scope) => new Syntax(args.head.as[List], args(1), scope)
	this("set")    = (args, scope) => scope.put(args.head.eval(scope).as[Name], args(1).eval(scope))
	this("+")      = (args, scope) => Real(args.map(_.eval(scope).as[Real].real).reduce(_ + _))
	this("-")      = (args, scope) => Real(args.map(_.eval(scope).as[Real].real).reduce(_ - _))
	this("*")      = (args, scope) => Real(args.map(_.eval(scope).as[Real].real).reduce(_ * _))
	this("/")      = (args, scope) => Real(args.map(_.eval(scope).as[Real].real).reduce(_ / _))
	this("eq")     = (args, scope) => Bool(args(0).eval(scope) == args(1).eval(scope))
	this("if")     = (args, scope) => args(if(args(0).eval(scope).as[Bool].bool) 1 else 2).eval(scope)
}
