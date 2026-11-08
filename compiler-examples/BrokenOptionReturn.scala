object BrokenOptionReturn {
  def safeDiv(n: Int, d: Int): Option[Int] = if (d == 0) None else n / d
}
