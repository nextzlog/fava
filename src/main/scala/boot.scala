object Main {
	def main(args: Array[String]) = {
		val jline = new scala.tools.jline.console.ConsoleReader
		jline.setExpandEvents(false)
		println(s"${Console.YELLOW}which language do you use?")
		println("[0]: fava")
		println("[1]: math")
		println("[2]: univ")
		println("[3]: regl")
		println("[4]: lisp")
		jline.readLine(s"select: ${Console.RESET}") match {
			case "0" => fava.Repl.main(args)
			case "1" => math.Repl.main(args)
			case "2" => univ.Repl.main(args)
			case "3" => regl.Repl.main(args)
			case "4" => lisp.Repl.main(args)
		}
	}
}
