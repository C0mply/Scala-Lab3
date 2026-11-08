object BrokenFlatten {
  def safeDiv(n: Int, d: Int) = if (d == 0) None else n / d
  val result = List(0, 1, 2).map(safeDiv(5, _)).flatten
}
