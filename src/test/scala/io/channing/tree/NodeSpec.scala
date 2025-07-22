package io.channing.tree

import java.util.UUID
import org.scalatest.wordspec.AnyWordSpec

class NodeSpec extends AnyWordSpec {

  "addChild" should {
    "copy the first child with new IDs" in {
      val grandchildData = TestEntry(Some("grandchild"))
      val grandchild     = Node(id = UUID.randomUUID(), data = grandchildData)

      val firstChildData = TestEntry(Some("first child"))
      val firstChild     = Node(
        id = UUID.randomUUID(),
        data = firstChildData,
        children = List(grandchild)
      )

      val parent = Node(
        id = UUID.randomUUID(),
        data = TestEntry(Some("parent")),
        children = List(firstChild)
      )

      val updatedParent = parent.addChild()

      assert(updatedParent.children.length == 2)

      assert(updatedParent.children.head == firstChild)

      val copiedChild = updatedParent.children(1)
      assert(copiedChild.id != firstChild.id)
      assert(copiedChild.data == TestEntry(None))
      assert(copiedChild.children.length == firstChild.children.length)

      val copiedGrandchild = copiedChild.children.head
      assert(copiedGrandchild.id != grandchild.id)
      assert(copiedGrandchild.data == TestEntry(None))
    }

    "return same node when no children exist" in {
      val parent = Node(id = UUID.randomUUID(), data = TestEntry(Some("parent")))
      val result = parent.addChild()

      assert(result == parent)
    }

    "correctly resolve internal and external references recursively" in {
      val externalNodeId = UUID.randomUUID()

      val greatGrandchild = Node(
        id = UUID.randomUUID(),
        data = TestEntry(Some("great-grandchild"))
      )

      val grandchild1 = Node(
        id = UUID.randomUUID(),
        data = TestEntry(Some("grandchild1")),
        children = List(greatGrandchild),
        references = Set(
          Reference("child", greatGrandchild.id), // Internal ref to great-grandchild
          Reference("external", externalNodeId) // External reference
        )
      )

      val grandchild2 = Node(
        id = UUID.randomUUID(),
        data = TestEntry(Some("grandchild2")),
        references = Set(
          Reference("sibling", grandchild1.id), // Internal ref to sibling
          Reference("nephew", greatGrandchild.id) // Internal ref to nephew
        )
      )

      val firstChild = Node(
        id = UUID.randomUUID(),
        data = TestEntry(Some("first child")),
        children = List(grandchild1, grandchild2),
        references = Set(
          Reference("child1", grandchild1.id), // Internal ref to child
          Reference("child2", grandchild2.id), // Internal ref to child
          Reference("external", externalNodeId) // External reference
        )
      )

      val parent = Node(
        id = UUID.randomUUID(),
        data = TestEntry(Some("parent")),
        children = List(firstChild)
      )

      val updatedParent = parent.addChild()

      val copiedChild           = updatedParent.children(1)
      val copiedGrandchild1     = copiedChild.children(0)
      val copiedGrandchild2     = copiedChild.children(1)
      val copiedGreatGrandchild = copiedGrandchild1.children.head

      val childRef1 = copiedChild.references.find(_.name == "child1")
      assert(childRef1.isDefined)
      assert(childRef1.get.nodeId == copiedGrandchild1.id)
      assert(childRef1.get.nodeId != grandchild1.id)

      val childRef2 = copiedChild.references.find(_.name == "child2")
      assert(childRef2.isDefined)
      assert(childRef2.get.nodeId == copiedGrandchild2.id)
      assert(childRef2.get.nodeId != grandchild2.id)

      val childExternalRef = copiedChild.references.find(_.name == "external")
      assert(childExternalRef.isDefined)
      assert(childExternalRef.get.nodeId == externalNodeId)

      val gc1ChildRef = copiedGrandchild1.references.find(_.name == "child")
      assert(gc1ChildRef.isDefined)
      assert(gc1ChildRef.get.nodeId == copiedGreatGrandchild.id)
      assert(gc1ChildRef.get.nodeId != greatGrandchild.id)

      val gc1ExternalRef = copiedGrandchild1.references.find(_.name == "external")
      assert(gc1ExternalRef.isDefined)
      assert(gc1ExternalRef.get.nodeId == externalNodeId)

      val gc2SiblingRef = copiedGrandchild2.references.find(_.name == "sibling")
      assert(gc2SiblingRef.isDefined)
      assert(gc2SiblingRef.get.nodeId == copiedGrandchild1.id)
      assert(gc2SiblingRef.get.nodeId != grandchild1.id)

      val gc2NephewRef = copiedGrandchild2.references.find(_.name == "nephew")
      assert(gc2NephewRef.isDefined)
      assert(gc2NephewRef.get.nodeId == copiedGreatGrandchild.id)
      assert(gc2NephewRef.get.nodeId != greatGrandchild.id)
    }
  }

  "addChildAt" should {
    "add child to node with matching ID" in {
      val grandchild = Node(
        id = UUID.randomUUID(),
        data = TestEntry(Some("grandchild"))
      )

      val child = Node(
        id = UUID.randomUUID(),
        data = TestEntry(Some("child")),
        children = List(grandchild)
      )

      val parent = Node(
        id = UUID.randomUUID(),
        data = TestEntry(Some("parent")),
        children = List(child)
      )

      val updatedParent = parent.addChildAt(child.id)

      // Check that the child node now has 2 children (original + copied)
      val updatedChild = updatedParent.children.head
      assert(updatedChild.children.length == 2)
      assert(updatedChild.children.head == grandchild) // Original grandchild

      // The copied grandchild should have a new ID and reset data
      val copiedGrandchild = updatedChild.children(1)
      assert(copiedGrandchild.id != grandchild.id)
      assert(copiedGrandchild.data == TestEntry(None))
    }

    "add child to deeply nested node" in {
      val leafChild = Node(
        id = UUID.randomUUID(),
        data = TestEntry(Some("leaf child"))
      )

      val deepChild = Node(
        id = UUID.randomUUID(),
        data = TestEntry(Some("deep child")),
        children = List(leafChild)
      )

      val middleChild = Node(
        id = UUID.randomUUID(),
        data = TestEntry(Some("middle child")),
        children = List(deepChild)
      )

      val topChild = Node(
        id = UUID.randomUUID(),
        data = TestEntry(Some("top child")),
        children = List(middleChild)
      )

      val root = Node(
        id = UUID.randomUUID(),
        data = TestEntry(Some("root")),
        children = List(topChild)
      )

      val updatedRoot = root.addChildAt(deepChild.id)

      // Navigate to the deep child and verify it was updated
      val updatedDeepChild = updatedRoot.children.head.children.head.children.head
      assert(updatedDeepChild.children.length == 2) // Should have added a child (original + copy)
      assert(updatedDeepChild.children.head == leafChild) // Original child unchanged

      // The copied child should have a new ID and reset data
      val copiedLeafChild = updatedDeepChild.children(1)
      assert(copiedLeafChild.id != leafChild.id) // New ID
      assert(copiedLeafChild.data == TestEntry(None)) // Reset data
    }

    "return unchanged node when ID not found" in {
      val child = Node(
        id = UUID.randomUUID(),
        data = TestEntry(Some("child"))
      )

      val parent = Node(
        id = UUID.randomUUID(),
        data = TestEntry(Some("parent")),
        children = List(child)
      )

      val nonExistentId = UUID.randomUUID()
      val result        = parent.addChildAt(nonExistentId)

      // Should return the same tree unchanged
      assert(result == parent)
    }

    "add child to root node itself" in {
      val child = Node(
        id = UUID.randomUUID(),
        data = TestEntry(Some("child"))
      )

      val root = Node(
        id = UUID.randomUUID(),
        data = TestEntry(Some("root")),
        children = List(child)
      )

      val updatedRoot = root.addChildAt(root.id)

      // Root should now have 2 children (original + copied)
      assert(updatedRoot.children.length == 2)
      assert(updatedRoot.children.head == child) // Original child

      // The copied child should have a new ID and reset data
      val copiedChild = updatedRoot.children(1)
      assert(copiedChild.id != child.id)
      assert(copiedChild.data == TestEntry(None))
    }

    "referencedNode" should {
      "return None when node has no references" in {
        val root = Node(data = TestEntry(Some("root")))
        val node = Node(data = TestEntry(None))

        assert(node.referencedNode("any", root).isEmpty)
      }

      "return None when target node is not in root tree" in {
        val missingId = UUID.randomUUID()
        val root      = Node(data = TestEntry(Some("root")))
        val node      = Node(data = TestEntry(None), references = Set(Reference("ref", missingId)))

        assert(node.referencedNode("ref", root).isEmpty)
      }

      "find deeply nested target nodes" in {
        val deep = Node(data = TestEntry(Some("deep")))
        val root = Node(
          data = TestEntry(Some("root")),
          children = List(
            Node(
              data = TestEntry(None),
              children = List(
                Node(data = TestEntry(None), children = List(deep))
              )
            )
          )
        )
        val node = Node(data = TestEntry(None), references = Set(Reference("deep", deep.id)))

        assert(node.referencedNode("deep", root).contains(deep))
      }

      "find root node itself as target" in {
        val root = Node(data = TestEntry(Some("root")))
        val node = Node(data = TestEntry(None), references = Set(Reference("root", root.id)))

        assert(node.referencedNode("root", root).contains(root))
      }

      "handle multiple references correctly" in {
        val (t1, t2) = (Node(data = TestEntry(Some("1"))), Node(data = TestEntry(Some("2"))))
        val root     = Node(data = TestEntry(None), children = List(t1, t2))
        val node     = Node(
          data = TestEntry(None),
          references = Set(
            Reference("first", t1.id),
            Reference("second", t2.id)
          )
        )

        assert(node.referencedNode("first", root).contains(t1))
        assert(node.referencedNode("second", root).contains(t2))
      }

    }
  }
}
