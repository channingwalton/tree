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
      "return Some(node) when reference exists and target node is found in root" in {
        val targetNode = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("target"))
        )

        val childNode = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("child"))
        )

        val root = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("root")),
          children = List(targetNode, childNode)
        )

        val nodeWithReference = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("node with ref")),
          references = Set(Reference("my_target", targetNode.id))
        )

        val result = nodeWithReference.referencedNode("my_target", root)

        assert(result.isDefined)
        assert(result.get == targetNode)
      }

      "return None when reference name does not exist" in {
        val targetNode = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("target"))
        )

        val root = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("root")),
          children = List(targetNode)
        )

        val nodeWithReference = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("node with ref")),
          references = Set(Reference("existing_ref", targetNode.id))
        )

        val result = nodeWithReference.referencedNode("nonexistent_ref", root)

        assert(result.isEmpty)
      }

      "return None when reference exists but target node is not found in root" in {
        val targetNodeId  = UUID.randomUUID()
        val differentNode = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("different"))
        )

        val root = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("root")),
          children = List(differentNode)
        )

        val nodeWithReference = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("node with ref")),
          references = Set(Reference("missing_target", targetNodeId))
        )

        val result = nodeWithReference.referencedNode("missing_target", root)

        assert(result.isEmpty)
      }

      "find target node in deeply nested structure" in {
        val deeplyNestedTarget = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("deeply nested target"))
        )

        val level3 = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("level 3")),
          children = List(deeplyNestedTarget)
        )

        val level2 = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("level 2")),
          children = List(level3)
        )

        val level1 = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("level 1")),
          children = List(level2)
        )

        val root = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("root")),
          children = List(level1)
        )

        val nodeWithReference = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("node with ref")),
          references = Set(Reference("deep_ref", deeplyNestedTarget.id))
        )

        val result = nodeWithReference.referencedNode("deep_ref", root)

        assert(result.isDefined)
        assert(result.get == deeplyNestedTarget)
      }

      "find target node when it is the root node itself" in {
        val root = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("root"))
        )

        val nodeWithReference = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("node with ref")),
          references = Set(Reference("root_ref", root.id))
        )

        val result = nodeWithReference.referencedNode("root_ref", root)

        assert(result.isDefined)
        assert(result.get == root)
      }

      "handle multiple references and return correct one by name" in {
        val target1 = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("target 1"))
        )

        val target2 = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("target 2"))
        )

        val target3 = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("target 3"))
        )

        val root = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("root")),
          children = List(target1, target2, target3)
        )

        val nodeWithMultipleReferences = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("node with multiple refs")),
          references = Set(
            Reference("first", target1.id),
            Reference("second", target2.id),
            Reference("third", target3.id)
          )
        )

        val result1 = nodeWithMultipleReferences.referencedNode("first", root)
        val result2 = nodeWithMultipleReferences.referencedNode("second", root)
        val result3 = nodeWithMultipleReferences.referencedNode("third", root)

        assert(result1.isDefined && result1.get == target1)
        assert(result2.isDefined && result2.get == target2)
        assert(result3.isDefined && result3.get == target3)
      }

      "return None when node has no references" in {
        val targetNode = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("target"))
        )

        val root = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("root")),
          children = List(targetNode)
        )

        val nodeWithoutReferences = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("node without refs"))
        )

        val result = nodeWithoutReferences.referencedNode("any_name", root)

        assert(result.isEmpty)
      }

      "find target node among siblings" in {
        val sibling1 = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("sibling 1"))
        )

        val targetSibling = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("target sibling"))
        )

        val sibling3 = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("sibling 3"))
        )

        val parent = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("parent")),
          children = List(sibling1, targetSibling, sibling3)
        )

        val root = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("root")),
          children = List(parent)
        )

        val nodeWithReference = Node(
          id = UUID.randomUUID(),
          data = TestEntry(Some("node with ref")),
          references = Set(Reference("sibling_ref", targetSibling.id))
        )

        val result = nodeWithReference.referencedNode("sibling_ref", root)

        assert(result.isDefined)
        assert(result.get == targetSibling)
      }
    }
  }
}
