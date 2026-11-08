object TypeProbe {
  def safeDiv(n: Int, d: Int) = if (d == 0) None else n / d
}
