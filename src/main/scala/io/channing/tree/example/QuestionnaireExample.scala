package io.channing.tree.example

import io.channing.tree.{ Mermaid, Node, NodeEntry }

import java.util.UUID

object QuestionnaireExample {

  sealed trait Question extends NodeEntry {
    def question: String
  }
  final case class Label(question: String) extends Question {
    override def reset: NodeEntry = this
  }
  final case class StringQuestion(question: String, ans: Option[String] = None) extends Question {
    override def reset: NodeEntry = copy(ans = None)
  }
  final case class IntQuestion(question: String, ans: Option[Int] = None) extends Question {
    override def reset: NodeEntry = copy(ans = None)
  }
  final case class BooleanQuestion(question: String, ans: Option[Boolean] = None) extends Question {
    override def reset: NodeEntry = copy(ans = None)
  }

  def createPersonAddressQuestionnaire(): Node = {
    val firstLineQuestion = Node(
      id = UUID.randomUUID(),
      data = StringQuestion("First line of address")
    )

    val secondLineQuestion = Node(
      id = UUID.randomUUID(),
      data = StringQuestion("Second line of address")
    )

    val postcodeQuestion = Node(
      id = UUID.randomUUID(),
      data = StringQuestion("Postcode")
    )

    val countryQuestion = Node(
      id = UUID.randomUUID(),
      data = StringQuestion("Country")
    )

    val address = Node(
      id = UUID.randomUUID(),
      data = Label("Address"),
      children = List(firstLineQuestion, secondLineQuestion, postcodeQuestion, countryQuestion)
    )

    val addressesSection = Node(
      id = UUID.randomUUID(),
      data = Label("Addresses"),
      children = List(address)
    ).addChild()

    val age = Node(
      id = UUID.randomUUID(),
      data = IntQuestion("How old are you?"),
      children = List(),
      references = Set.empty
    )

    Node(
      id = UUID.randomUUID(),
      data = Label("Person"),
      children = List(age, addressesSection)
    )
  }

  def main(args: Array[String]): Unit = {
    println("=== Person Address Questionnaire ===")
    val personQuestionnaire = createPersonAddressQuestionnaire()
    println(Mermaid.toFlowchart(personQuestionnaire))
  }
}
