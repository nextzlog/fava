package fava

import gram._
import java.lang.{String=>S}
import scala.language.implicitConversions
import scala.{Any=>A, Int=>I, Double=>D, Boolean=>B}

class FaVM(val codes: Seq[Code], var pc: Int = 0) {
	val call = new Stack[Env]
	val data = new Stack[Any]
	while(pc < codes.size) codes(pc)(this)
}

class Stack[E] extends collection.mutable.Stack[E] {
	def popN(n: Int) = Seq.fill(n)(pop).reverse
	def popAs[Type]: Type = pop.asInstanceOf[Type]
	def topAs[Type]: Type = top.asInstanceOf[Type]
	def env = (this :+ null).top.asInstanceOf[Env]
}

class Code(op: FaVM => Unit) {
	def apply(vm: FaVM) = (op(vm), vm.pc += 1)
}

class ALU(n: Int, s: String, f: PartialFunction[Seq[Any], Any]) extends Code(vm => {
	def err(v: Seq[_]) = v.map(s => s"$s: ${s.getClass}").mkString("(", ", ", ")")
	vm.data.push(f.applyOrElse(vm.data.popN(n), (v: Seq[_]) => sys.error(err(v))))
})

class Jump(op: FaVM => Option[Int]) extends Code(vm => op(vm).foreach(to => vm.pc = to - 1))

case class Closure(from: Int, narg: Int, out: Env)
case class Promise(thunk: Closure, var cache: Any = null, var empty: Boolean = true)

case class Env(args: Seq[Any], out: Env = null) {
	def apply(nest: Int, index: Int): Any = if(nest > 0) out(nest - 1, index) else args(index)
}

case class Push(v: Any) extends Code(vm => vm.data.push(v))

case object Pos extends ALU(1, "+", {
	case Seq(v: I) => + v
	case Seq(v: D) => + v
})

case object Neg extends ALU(1, "-", {
	case Seq(v: I) => - v
	case Seq(v: D) => - v
})

case object Not extends ALU(1, "!", {
	case Seq(v: B) => ! v
})

case object Add extends ALU(2, "+", {
	case Seq(lhs: I, rhs: I) => lhs + rhs
	case Seq(lhs: I, rhs: D) => lhs + rhs
	case Seq(lhs: D, rhs: I) => lhs + rhs
	case Seq(lhs: D, rhs: D) => lhs + rhs
	case Seq(lhs: S, rhs: A) => s"$lhs$rhs"
	case Seq(lhs: A, rhs: S) => s"$lhs$rhs"
})

case object Sub extends ALU(2, "-", {
	case Seq(lhs: I, rhs: I) => lhs - rhs
	case Seq(lhs: I, rhs: D) => lhs - rhs
	case Seq(lhs: D, rhs: I) => lhs - rhs
	case Seq(lhs: D, rhs: D) => lhs - rhs
})

case object Mul extends ALU(2, "*", {
	case Seq(lhs: I, rhs: I) => lhs * rhs
	case Seq(lhs: I, rhs: D) => lhs * rhs
	case Seq(lhs: D, rhs: I) => lhs * rhs
	case Seq(lhs: D, rhs: D) => lhs * rhs
})

case object Div extends ALU(2, "/", {
	case Seq(lhs: I, rhs: I) => lhs / rhs
	case Seq(lhs: I, rhs: D) => lhs / rhs
	case Seq(lhs: D, rhs: I) => lhs / rhs
	case Seq(lhs: D, rhs: D) => lhs / rhs
})

case object Mod extends ALU(2, "%", {
	case Seq(lhs: I, rhs: I) => lhs % rhs
	case Seq(lhs: I, rhs: D) => lhs % rhs
	case Seq(lhs: D, rhs: I) => lhs % rhs
	case Seq(lhs: D, rhs: D) => lhs % rhs
})

case object Gt extends ALU(2, ">", {
	case Seq(lhs: I, rhs: I) => lhs > rhs
	case Seq(lhs: I, rhs: D) => lhs > rhs
	case Seq(lhs: D, rhs: I) => lhs > rhs
	case Seq(lhs: D, rhs: D) => lhs > rhs
	case Seq(lhs: S, rhs: S) => lhs > rhs
})

case object Lt extends ALU(2, "<", {
	case Seq(lhs: I, rhs: I) => lhs < rhs
	case Seq(lhs: I, rhs: D) => lhs < rhs
	case Seq(lhs: D, rhs: I) => lhs < rhs
	case Seq(lhs: D, rhs: D) => lhs < rhs
	case Seq(lhs: S, rhs: S) => lhs < rhs
})

case object Ge extends ALU(2, ">=", {
	case Seq(lhs: I, rhs: I) => lhs >= rhs
	case Seq(lhs: I, rhs: D) => lhs >= rhs
	case Seq(lhs: D, rhs: I) => lhs >= rhs
	case Seq(lhs: D, rhs: D) => lhs >= rhs
	case Seq(lhs: S, rhs: S) => lhs >= rhs
})

case object Le extends ALU(2, "<=", {
	case Seq(lhs: I, rhs: I) => lhs <= rhs
	case Seq(lhs: I, rhs: D) => lhs <= rhs
	case Seq(lhs: D, rhs: I) => lhs <= rhs
	case Seq(lhs: D, rhs: D) => lhs <= rhs
	case Seq(lhs: S, rhs: S) => lhs <= rhs
})

case object Eq extends ALU(2, "==", {
	case Seq(lhs: A, rhs: A) => lhs == rhs
})

case object Ne extends ALU(2, "!=", {
	case Seq(lhs: A, rhs: A) => lhs != rhs
})

case object And extends ALU(2, "&", {
	case Seq(lhs: B, rhs: B) => lhs & rhs
	case Seq(lhs: I, rhs: I) => lhs & rhs
})

case object Or extends ALU(2, "|", {
	case Seq(lhs: B, rhs: B) => lhs | rhs
	case Seq(lhs: I, rhs: I) => lhs | rhs
})

case class Skip(plus: Int) extends Jump(vm => Some(vm.pc + plus))
case class Skin(plus: Int) extends Jump(vm => Option.when(!vm.data.popAs[B])(vm.pc + plus))

case class Def(size: Int, narg: Int) extends Jump(vm => Some {
	vm.data.push(Closure(vm.pc + 1, narg, vm.call.env))
	vm.pc + size
})

case object Ret extends Jump(vm => Some {
	vm.call.remove(0).asInstanceOf[Env]
	vm.data.remove(1).asInstanceOf[Int]
})

case class Call(argc: Int) extends Jump(vm => Some {
	val args = vm.data.popN(argc)
	val func = vm.data.popAs[Closure]
	vm.call.push(Env(args, func.out))
	vm.data.push(vm.pc + 1)
	if(args.size == func.narg) func.from
	else sys.error(s"${func.narg} arguments required")
})

case class Load(nest: Int, id: Int) extends Code(vm => vm.data.push(vm.call.env(nest, id)))

case object Arg extends Code(vm => vm.data.push(Promise(vm.data.popAs[Closure])))
case object Get extends Code(vm => vm.data.push(vm.data.popAs[Promise].cache))
case object Nil extends Code(vm => vm.data.push(vm.data.topAs[Promise].empty))
case object Ref extends Code(vm => vm.data.push(vm.data.topAs[Promise].thunk))
case object Set extends Code(vm => vm.data.popAs[Promise].cache = vm.data.pop)
case object Fix extends Code(vm => vm.data.topAs[Promise].empty = false)

trait AST {
	def code(implicit env: DefST): Seq[Code]
}

case class LitST(value: Any) extends AST {
	def code(implicit env: DefST) = Seq(Push(value))
}

case class StrST(string: String) extends AST {
	def code(implicit env: DefST) = LitST(StringContext.processEscapes(string)).code
}

case class UnST(op: String, expr: AST) extends AST {
	def code(implicit env: DefST) = op match {
		case "+" => expr.code :+ Pos
		case "-" => expr.code :+ Neg
		case "!" => expr.code :+ Not
	}
}

case class BinST(op: String, e1: AST, e2: AST) extends AST {
	def code(implicit env: DefST) = op match {
		case "+"  => e1.code ++ e2.code :+ Add
		case "-"  => e1.code ++ e2.code :+ Sub
		case "*"  => e1.code ++ e2.code :+ Mul
		case "/"  => e1.code ++ e2.code :+ Div
		case "%"  => e1.code ++ e2.code :+ Mod
		case "&"  => e1.code ++ e2.code :+ And
		case "|"  => e1.code ++ e2.code :+ Or
		case ">=" => e1.code ++ e2.code :+ Ge
		case "<=" => e1.code ++ e2.code :+ Le
		case ">"  => e1.code ++ e2.code :+ Gt
		case "<"  => e1.code ++ e2.code :+ Lt
		case "==" => e1.code ++ e2.code :+ Eq
		case "!=" => e1.code ++ e2.code :+ Ne
	}
}

case class IfST(cond: AST, v1: AST, v2: AST) extends AST {
	def code(implicit env: DefST) = {
		val (code1, code2) = v1.code -> v2.code
		val jmp1 = Skin(2 + code1.size) +: code1
		val jmp2 = Skip(1 + code2.size) +: code2
		cond.code ++ jmp1 ++ jmp2
	}
}

case class DefST(pars: Seq[String], body: AST, var out: DefST = null) extends AST {
	def code(implicit env: DefST) = {
		val codes = body.code((this.out = env, this)._2)
		(Def(codes.size + 2, pars.size) +: codes :+ Ret)
	}
}

object Root extends DefST(Seq(), null)

case class StIdST(val name: String) extends AST {
	def search(env: DefST, nest: Int = 0): Load = {
		val idx = env.pars.indexOf(name)
		if(idx >= 0) return Load(nest, idx)
		if(env.out != null) search(env.out, nest + 1)
		else sys.error(s"variable $name not defined")
	}
	def code(implicit env: DefST) = Seq(search(env))
}

case class LzIdST(val name: StIdST) extends AST {
	val (head, tail) = Seq(Nil, Skin(6), Ref, Call(0)) -> Seq(Fix, Set, Get)
	def code(implicit env: DefST) = (name.code ++ head ++ name.code ++ tail)
}

case class LzArgST(body: AST) extends AST {
	def code(implicit env: DefST) = DefST(Seq(), body).code :+ Arg
}

case class CallST(f: AST, args: Seq[AST]) extends AST {
	def code(implicit env: DefST) = f.code ++ args.map(_.code).flatten :+ Call(args.size)
}

object FavaPEGs extends PEGs {
	def expr: PEG[AST] = (cond / or) <~ ("//" ~ ".*$".r).?
	def cond = (or <~ "?") ~ expr ~ (":" ~> expr) ^ {case c->y->n => IfST(c, y, n)}
	def or   = new Fold(and, "|" ^ (op => BinST(op, _, _)))
	def and  = new Fold(eql, "&" ^ (op => BinST(op, _, _)))
	def eql  = new Fold(rel, """(!|=)=""".r ^ (op => BinST(op, _, _)))
	def rel  = new Fold(add, """[<>]=?""".r ^ (op => BinST(op, _, _)))
	def add  = new Fold(mul, """[\+\-]""".r ^ (op => BinST(op, _, _)))
	def mul  = new Fold(unr, """[\*/%]""".r ^ (op => BinST(op, _, _)))
	def unr  = ("+" / "-" / "!").* ~ call ^ {case o->e => o.foldRight(e)(UnST)}
	def call = fact ~ args.* ^ {case f->a => a.foldLeft(f)(CallST)}
	def args = "(" ~> new Sep(expr ^ LzArgST, ",") <~")"
	def fact = func / bool / text / real / int / name / ("(" ~> expr <~ ")")
	def func = pars ~ ("=>" ~> expr) ^ {case p->e => DefST(p, e)}
	def pars = "(" ~> new Sep(name, ",") <~ ")" ^ (_.map(_.name.name))
	def bool = ("true" / "false") ^ (_.toBoolean) ^ LitST
	def text = ("\"" ~> """([^"\\]|\\[\\'"bfnrt])*""".r <~ "\"") ^ StrST
	def int  = """\d+""".r ^ (_.toInt) ^ LitST
	def real = """(\d+\.\d*|\d*\.\d+)""".r ^ (_.toDouble) ^ LitST
	def name = """[@A-Z_a-z][@0-9A-Z_a-z]*""".r ^ StIdST ^ LzIdST
}

object FavaReplPEGs extends gram.PEGs {
	def full: PEG[AST] = pile / expr
	def expr: PEG[AST] = FavaPEGs.expr <~ "$".r
	def pile: PEG[AST] = "compile" ~ "(" ~> expr <~ ")" ^ (_.code(Root).mkString(" ")) ^ LitST
}

object Repl {
	val jline = new scala.tools.jline.console.ConsoleReader
	jline.setExpandEvents(false)
	jline.setPrompt(s"${Console.BLUE}fava$$ ${Console.RESET}")
	def main(args: Array[String]) = while(true) try {
		val ast = FavaReplPEGs.expr(jline.readLine)
		if(ast.isDefined) println(new FaVM(ast.get.m.code(Root)).data.pop)
		else println(s"${Console.RED}compilation failure: illegal syntax")
	} catch {
		case ex: Exception => println(s"${Console.RED} $ex")
	}
}
