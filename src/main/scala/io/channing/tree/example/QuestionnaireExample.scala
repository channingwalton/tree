package io.channing.tree.example

import io.channing.tree.{ Mermaid, Node, Reference }
import java.util.UUID

object QuestionnaireExample {

  sealed trait Question { def question: String }
  final case class Label(question: String) extends Question
  final case class StringQuestion(question: String, ans: Option[String] = None) extends Question
  final case class IntQuestion(question: String, ans: Option[Int] = None) extends Question
  final case class BooleanQuestion(question: String, ans: Option[Boolean] = None) extends Question

  def createPersonAddressQuestionnaire(): Node[Question] = {
    // Address component questions
    val firstLineQuestion = Node(
      id = UUID.randomUUID(),
      data = Some(StringQuestion("First line of address"))
    )

    val secondLineQuestion = Node(
      id = UUID.randomUUID(),
      data = Some(StringQuestion("Second line of address"))
    )

    val postcodeQuestion = Node(
      id = UUID.randomUUID(),
      data = Some(StringQuestion("Postcode"))
    )

    val countryQuestion = Node(
      id = UUID.randomUUID(),
      data = Some(StringQuestion("Country"))
    )

    // Address section
    val addressSection = Node(
      id = UUID.randomUUID(),
      data = Some(Label("Address")),
      children = List(firstLineQuestion, secondLineQuestion, postcodeQuestion, countryQuestion),
      references = Set(
        Reference("first_line", firstLineQuestion.id),
        Reference("second_line", secondLineQuestion.id),
        Reference("postcode", postcodeQuestion.id),
        Reference("country", countryQuestion.id)
      )
    )

    val age = Node(
      id = UUID.randomUUID(),
      data = Some(IntQuestion("How old are you?")),
      children = List(),
      references = Set.empty
    )

    // Person top level
    Node(
      id = UUID.randomUUID(),
      data = Some(Label("Person")),
      children = List(age, addressSection),
      references = Set(
        Reference("address", addressSection.id)
      )
    )
  }

  def generateDiagram(questionnaire: Node[Question]): String = Mermaid.toFlowchart(questionnaire)

  def main(args: Array[String]): Unit = {
    println("=== Person Address Questionnaire ===")
    val personQuestionnaire = createPersonAddressQuestionnaire()
    println(generateDiagram(personQuestionnaire))
  }
}
