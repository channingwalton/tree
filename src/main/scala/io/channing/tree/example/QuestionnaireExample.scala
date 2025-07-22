package io.channing.tree.example

import io.channing.tree.{ Mermaid, Node, NodeEntry }

import java.util.UUID

object QuestionnaireExample:

  // A predicate context provided to predicates to validate answers
  final case class PredicateContext(subject: Question, root: Node)

  sealed trait ValidationResult:
    import ValidationResult.*
    def &&(other: ValidationResult): ValidationResult =
      (this, other) match {
        case (Success, Success) => Success
        case _                  => this
      }

  object ValidationResult:
    case object Success extends ValidationResult
    case class Failure(message: String) extends ValidationResult

  type Validation = PredicateContext => ValidationResult

  val isPositive: Validation = {
    case PredicateContext(QuestionnaireExample.IntQuestion(_, _, Some(ans)), _) =>
      if ans > 0 then ValidationResult.Success else ValidationResult.Failure("Answer must be positive")
    case _ => ValidationResult.Success
  }

  sealed trait Question extends NodeEntry:
    def question: String
    def validate: Validation
    def isValid(root: Node): ValidationResult = validate(PredicateContext(this, root))

  final case class Label(question: String, validate: Validation = _ => ValidationResult.Success) extends Question:
    override def reset: NodeEntry = this

  final case class StringQuestion(
      question: String,
      validate: Validation = _ => ValidationResult.Success,
      ans: Option[String] = None
  ) extends Question:
    override def reset: NodeEntry = copy(ans = None)

  final case class IntQuestion(
      question: String,
      validate: Validation = _ => ValidationResult.Success,
      ans: Option[Int] = None
  ) extends Question:
    override def reset: NodeEntry = copy(ans = None)

  final case class BooleanQuestion(
      question: String,
      validate: Validation = _ => ValidationResult.Success,
      ans: Option[Boolean] = None
  ) extends Question:
    override def reset: NodeEntry = copy(ans = None)

  def isValid(node: Node): Boolean = {
    val question = node.data.asInstanceOf[Question] // yuck
    question.validate(PredicateContext(question, node)) == ValidationResult.Success && node.children.forall(isValid)
  }

  def createPersonAddressQuestionnaire(): Node =
    val firstLineQuestion = Node(
      id = UUID.randomUUID(),
      data = StringQuestion("First line of address", ans = Some("1 Road"))
    )

    val secondLineQuestion = Node(
      id = UUID.randomUUID(),
      data = StringQuestion("Second line of address", ans = Some("2 Road"))
    )

    val postcodeQuestion = Node(
      id = UUID.randomUUID(),
      data = StringQuestion("Postcode", ans = Some("12345"))
    )

    val countryQuestion = Node(
      id = UUID.randomUUID(),
      data = StringQuestion("Country", ans = Some("UK"))
    )

    val yearsQuestion = Node(
      id = UUID.randomUUID(),
      data = IntQuestion("How many years have you lived at this address?", validate = isPositive, ans = Some(5))
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
      data = IntQuestion("How old are you?", ans = Some(25)),
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
    println(s"Person Questionnaire is valid: ${isValid(personQuestionnaire)}")
    println(Mermaid.toFlowchart(personQuestionnaire))
end QuestionnaireExample
