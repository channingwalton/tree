package io.channing.tree.example

import io.channing.tree.{ Mermaid, Node, NodeEntry }

import java.util.UUID

object QuestionnaireExample:

  sealed trait Question extends NodeEntry:
    def question: String

  final case class Label(question: String) extends Question:
    override def reset: NodeEntry = this

  final case class StringQuestion(question: String, ans: Option[String] = None) extends Question:
    override def reset: NodeEntry = copy(ans = None)

  final case class IntQuestion(question: String, ans: Option[Int] = None) extends Question:
    override def reset: NodeEntry = copy(ans = None)

  final case class BooleanQuestion(question: String, ans: Option[Boolean] = None) extends Question:
    override def reset: NodeEntry = copy(ans = None)

  def createPersonAddressQuestionnaire(): Node =
    val firstLineQuestion = Node(
      id = UUID.randomUUID(),
      data = StringQuestion("First line of address", Some("1 Road"))
    )

    val secondLineQuestion = Node(
      id = UUID.randomUUID(),
      data = StringQuestion("Second line of address", Some("2 Road"))
    )

    val postcodeQuestion = Node(
      id = UUID.randomUUID(),
      data = StringQuestion("Postcode", Some("12345"))
    )

    val countryQuestion = Node(
      id = UUID.randomUUID(),
      data = StringQuestion("Country", Some("UK"))
    )

    val yearsQuestion = Node(
      id = UUID.randomUUID(),
      data = IntQuestion("How many years have you lived at this address?", Some(5))
    )

    val address = Node(
      id = UUID.randomUUID(),
      data = Label("Address"),
      children = List(firstLineQuestion, secondLineQuestion, postcodeQuestion, countryQuestion, yearsQuestion)
    )

    // address section with a child added
    // the first address is filled but the second address is empty
    val addressesSection = Node(
      id = UUID.randomUUID(),
      data = Label("Addresses"),
      children = List(address)
    ).addChild()

    val age = Node(
      id = UUID.randomUUID(),
      data = IntQuestion("How old are you?", Some(25)),
      children = List(),
      references = Set()
    )

    Node(
      id = UUID.randomUUID(),
      data = Label("Person"),
      children = List(age, addressesSection)
    )
  def main(args: Array[String]): Unit =
    println("=== Person Address Questionnaire ===")
    val personQuestionnaire = createPersonAddressQuestionnaire()
    println(Mermaid.toFlowchart(personQuestionnaire))
end QuestionnaireExample
