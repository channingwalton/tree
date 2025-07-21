package io.channing.tree

import org.scalatest.wordspec.AnyWordSpec

import java.util.UUID

class MermaidSpec extends AnyWordSpec {

  "Mermaid.toFlowchart" should {
    "generate flowchart for simple node" in {
      val node = Node(
        id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
        data = TestEntry(Some("simple node"))
      )

      val flowchart = Mermaid.toFlowchart(node)

      assert(flowchart.contains("flowchart TD"))
      assert(flowchart.contains("A[\"TestEntry(value = Some(simple node)) <br/> ID: 550e8400-e29b-41d4-a716-446655440000\"]"))
    }

    "generate flowchart for node with children" in {
      val child1 = Node(
        id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
        data = TestEntry(Some("child 1"))
      )

      val child2 = Node(
        id = UUID.fromString("22222222-2222-2222-2222-222222222222"),
        data = TestEntry(Some("child 2"))
      )

      val parent = Node(
        id = UUID.fromString("33333333-3333-3333-3333-333333333333"),
        data = TestEntry(Some("parent")),
        children = List(child1, child2)
      )

      val flowchart = Mermaid.toFlowchart(parent)

      assert(flowchart.contains("flowchart TD"))
      assert(flowchart.contains("C[\"TestEntry(value = Some(parent)) <br/> ID: 33333333-3333-3333-3333-333333333333\"]"))
      assert(flowchart.contains("A[\"TestEntry(value = Some(child 1)) <br/> ID: 11111111-1111-1111-1111-111111111111\"]"))
      assert(flowchart.contains("B[\"TestEntry(value = Some(child 2)) <br/> ID: 22222222-2222-2222-2222-222222222222\"]"))
      assert(flowchart.contains("C --> A"))
      assert(flowchart.contains("C --> B"))
    }

    "generate flowchart for node with references" in {
      val externalId = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")

      val target = Node(
        id = UUID.fromString("44444444-4444-4444-4444-444444444444"),
        data = TestEntry(Some("target"))
      )

      val source = Node(
        id = UUID.fromString("55555555-5555-5555-5555-555555555555"),
        data = TestEntry(Some("source")),
        children = List(target),
        references = Set(
          Reference("internal", target.id),
          Reference("external", externalId)
        )
      )

      val flowchart = Mermaid.toFlowchart(source)

      assert(flowchart.contains("flowchart TD"))
      assert(flowchart.contains("B --> A"))
      assert(flowchart.contains("B -.->|\"internal\"| A"))
      assert(flowchart.contains("B -.->|\"external\"| C"))
      assert(flowchart.contains("C[\"External <br/> ID: eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee\"]"))
    }
  }
}
