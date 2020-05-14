package univ

class UTM[V](data1: Seq[V], data2: Seq[V], bk1: V, bk2: V, mvL: V, mvR: V, var st1: V) {
	val tape1 = data1.zipWithIndex.map(_.swap).to(collection.mutable.SortedMap)
	val tape2 = data2.zipWithIndex.map(_.swap).to(collection.mutable.SortedMap)
	var (hd1, hd2) -> st2 = (0, 0) -> "F0"
	def rd1 = tape1.getOrElse(hd1, bk1)
	def rd2 = tape2.getOrElse(hd2, bk2)
	def apply(stop: V) = Iterator.continually(st2 match {
		case "F0" if rd2 == st1 => (st1 = st1, st2 = "F1", tape1(hd1) = rd1, hd1 += 0, hd2 += 1)
		case "F0" if rd2 != st1 => (st1 = st1, st2 = "F0", tape1(hd1) = rd1, hd1 += 0, hd2 += 5)
		case "F1" if rd2 == rd1 => (st1 = st1, st2 = "F2", tape1(hd1) = rd1, hd1 += 0, hd2 += 1)
		case "F1" if rd2 != rd1 => (st1 = st1, st2 = "F0", tape1(hd1) = rd1, hd1 += 0, hd2 += 4)
		case "F2" if rd2 != bk2 => (st1 = rd2, st2 = "F3", tape1(hd1) = rd1, hd1 += 0, hd2 += 1)
		case "F3" if rd2 != bk2 => (st1 = st1, st2 = "F4", tape1(hd1) = rd2, hd1 += 0, hd2 += 1)
		case "F4" if rd2 == bk1 => (st1 = st1, st2 = "F5", tape1(hd1) = rd1, hd1 += 0, hd2 += 1)
		case "F4" if rd2 == mvL => (st1 = st1, st2 = "F5", tape1(hd1) = rd1, hd1 -= 1, hd2 += 1)
		case "F4" if rd2 == mvR => (st1 = st1, st2 = "F5", tape1(hd1) = rd1, hd1 += 1, hd2 += 1)
		case "F5" if rd2 == bk2 => (st1 = st1, st2 = "F0", tape1(hd1) = rd1, hd1 += 0, hd2 += 1)
		case "F5" if rd2 != bk2 => (st1 = st1, st2 = "F5", tape1(hd1) = rd1, hd1 += 0, hd2 -= 1)
	}).takeWhile(t => st1 != stop || st2 != "F0").map(t => tape1.values.mkString)
}
case class CUTM(data1: String, data2: String) extends UTM(data1, data2, ' ', '*', 'L', 'R', 'I')

object Repl {
	val jline = new scala.tools.jline.console.ConsoleReader
	jline.setExpandEvents(false)
	jline.setPrompt(s"${Console.BLUE}univ$$ ${Console.RESET}")
	val data2 = "I0a0RI1a1Ra0a0Ra1a1Ra b Lb0c1Lb1b0Lb F1 c0c0Lc1c1Lc F R"
	def main(args: Array[String]) = while(true) CUTM(jline.readLine, data2)('F').foreach(println)
}
