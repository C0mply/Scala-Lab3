object BrokenOptionMix {
  val people: Option[Seq[Option[String]]] = Some(Seq(Some("12345"), None))
  val result = people.flatMap(entries => entries.flatMap(person => person.map(identity)))
}
