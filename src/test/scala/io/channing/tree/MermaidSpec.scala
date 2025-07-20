package io.channing.tree

import java.util.UUID
import org.scalatest.wordspec.AnyWordSpec

class MermaidSpec extends AnyWordSpec {

  "Mermaid.toFlowchart" should {
    "generate flowchart for simple node" in {
      val node = Node(
        id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
        data = Some("simple node")
      )

      val flowchart = Mermaid.toFlowchart(node)

      assert(flowchart.contains("flowchart TD"))
      assert(flowchart.contains("550e8400[\"simple node<br/>550e8400-e29b-41d4-a716-446655440000\"]"))
    }

    "generate flowchart for node with children" in {
      val child1 = Node(
        id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
        data = Some("child 1")
      )

      val child2 = Node(
        id = UUID.fromString("22222222-2222-2222-2222-222222222222"),
        data = Some("child 2")
      )

      val parent = Node(
        id = UUID.fromString("33333333-3333-3333-3333-333333333333"),
        data = Some("parent"),
        children = List(child1, child2)
      )

      val flowchart = Mermaid.toFlowchart(parent)

      assert(flowchart.contains("flowchart TD"))
      assert(flowchart.contains("33333333[\"parent<br/>33333333-3333-3333-3333-333333333333\"]"))
      assert(flowchart.contains("11111111[\"child 1<br/>11111111-1111-1111-1111-111111111111\"]"))
      assert(flowchart.contains("22222222[\"child 2<br/>22222222-2222-2222-2222-222222222222\"]"))
      assert(flowchart.contains("33333333 --> 11111111"))
      assert(flowchart.contains("33333333 --> 22222222"))
    }

    "generate flowchart for node with references" in {
      val externalId = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")

      val target = Node(
        id = UUID.fromString("44444444-4444-4444-4444-444444444444"),
        data = Some("target")
      )

      val source = Node(
        id = UUID.fromString("55555555-5555-5555-5555-555555555555"),
        data = Some("source"),
        children = List(target),
        references = Set(
          Reference("internal", target.id),
          Reference("external", externalId)
        )
      )

      val flowchart = Mermaid.toFlowchart(source)

      assert(flowchart.contains("flowchart TD"))
      assert(flowchart.contains("55555555 --> 44444444"))
      assert(flowchart.contains("55555555 -.->|\"internal\"| 44444444"))
      assert(flowchart.contains("55555555 -.->|\"external\"| eeeeeeee"))
      assert(flowchart.contains("eeeeeeee[\"External<br/>eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee\"]"))
    }
  }
}
