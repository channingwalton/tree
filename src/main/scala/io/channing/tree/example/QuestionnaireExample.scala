package io.channing.tree.example

import io.channing.tree.{ Mermaid, Node, NodeEntry }

import java.util.UUID
import io.channing.tree.example.QuestionnaireExample.ValidationResult.Success
import io.channing.tree.example.QuestionnaireExample.ValidationResult.Failure

object QuestionnaireExample:

  // A predicate context provided to predicates to validate answers
  final case class PredicateContext(subject: Question, root: Node)

  sealed trait ValidationResult
  object ValidationResult:
    case object Success extends ValidationResult
    case class Failure(question: Question, message: String) extends ValidationResult

  type Validation = PredicateContext => ValidationResult

  val isPositive: Validation = {
    case PredicateContext(q @ QuestionnaireExample.IntQuestion(_, _, Some(ans)), _) =>
      if ans > 0 then ValidationResult.Success
      else ValidationResult.Failure(q, "Answer must be positive")
    case PredicateContext(q, _) => ValidationResult.Success
  }

  sealed trait Question extends NodeEntry:
    def question: String
    def validate: Validation
    def isValid(root: Node): ValidationResult = validate(PredicateContext(this, root))

  final case class Label(question: String, validate: Validation = _ => ValidationResult.Success) extends Question:
    override def asString: String = question
    override def reset: NodeEntry = this

  final case class StringQuestion(
      question: String,
      validate: Validation = _ => ValidationResult.Success,
      ans: Option[String] = None
  ) extends Question:
    override def asString: String = s"$question${ans.map(a => s" ($a)").getOrElse("")}"
    override def reset: NodeEntry = copy(ans = None)

  final case class IntQuestion(
      question: String,
      validate: Validation = _ => ValidationResult.Success,
      ans: Option[Int] = None
  ) extends Question:
    override def reset: NodeEntry = copy(ans = None)
    override def asString: String = s"$question${ans.map(a => s" ($a)").getOrElse("")}"

  final case class BooleanQuestion(
      question: String,
      validate: Validation = _ => ValidationResult.Success,
      ans: Option[Boolean] = None
  ) extends Question:
    override def reset: NodeEntry = copy(ans = None)
    override def asString: String = s"$question${ans.map(a => s" ($a)").getOrElse("")}"

  def validate(node: Node): List[ValidationResult.Failure] = {
    val question = node.data.asInstanceOf[Question] // yuck
    question.validate(PredicateContext(question, node)) match
      case Success                     => node.children.flatMap(validate)
      case f: ValidationResult.Failure => f :: node.children.flatMap(validate)
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
      data = IntQuestion("How old are you?", validate = isPositive, ans = Some(-25)),
      children = List(),
      references = Set()
    )

    Node(
      id = UUID.randomUUID(),
      data = Label("Person"),
      children = List(age, addressesSection)
    )

  def main(args: Array[String]): Unit =
    val personQuestionnaire = createPersonAddressQuestionnaire()
    val html = s"""<!doctype html>
                     |<html lang="en">
                     |  <body>
                     |   <h1>Person Address Questionnaire</h1>
                     |    Person Questionnaire validation:
                     |    ${validate(personQuestionnaire).mkString("<li>", "\n", "</li>")}
                     |    <pre class="mermaid">
                     |    ${Mermaid.toFlowchart(personQuestionnaire)}
                     |    </pre>
                     |    <script type="module">
                     |      import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.esm.min.mjs';
                     |    </script>
                     |  </body>
                     |</html>""".stripMargin
    import java.nio.file.{Files, Paths}
    import scala.sys.process.*
    val path = Paths.get("target/questionnaire.html")
    val _ = Files.write(path, html.getBytes("UTF-8"))
    val _ = s"open ${path.toString}".!


end QuestionnaireExample
