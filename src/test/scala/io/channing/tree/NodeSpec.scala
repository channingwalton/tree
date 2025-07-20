package io.channing.tree

import java.util.UUID
import org.scalatest.wordspec.AnyWordSpec

class NodeSpec extends AnyWordSpec {

  "addChild" should {
    "copy the first child with new IDs" in {
      val grandchildData = Some("grandchild")
      val grandchild     = Node(id = UUID.randomUUID(), data = grandchildData)

      val firstChildData = Some("first child")
      val firstChild     = Node(
        id = UUID.randomUUID(),
        data = firstChildData,
        children = List(grandchild)
      )

      val parent = Node(
        id = UUID.randomUUID(),
        data = Some("parent"),
        children = List(firstChild)
      )

      val updatedParent = parent.addChild()

      assert(updatedParent.children.length == 2)

      assert(updatedParent.children.head == firstChild)

      val copiedChild = updatedParent.children(1)
      assert(copiedChild.id != firstChild.id)
      assert(copiedChild.data == None)
      assert(copiedChild.children.length == firstChild.children.length)

      val copiedGrandchild = copiedChild.children.head
      assert(copiedGrandchild.id != grandchild.id)
      assert(copiedGrandchild.data == None)
    }

    "return same node when no children exist" in {
      val parent = Node(id = UUID.randomUUID(), data = Some("parent"))
      val result = parent.addChild()

      assert(result == parent)
    }

    "correctly resolve internal and external references recursively" in {
      val externalNodeId = UUID.randomUUID()

      val greatGrandchild = Node(
        id = UUID.randomUUID(),
        data = Some("great-grandchild")
      )

      val grandchild1 = Node(
        id = UUID.randomUUID(),
        data = Some("grandchild1"),
        children = List(greatGrandchild),
        references = Set(
          Reference("child", greatGrandchild.id), // Internal ref to great-grandchild
          Reference("external", externalNodeId) // External reference
        )
      )

      val grandchild2 = Node(
        id = UUID.randomUUID(),
        data = Some("grandchild2"),
        references = Set(
          Reference("sibling", grandchild1.id), // Internal ref to sibling
          Reference("nephew", greatGrandchild.id) // Internal ref to nephew
        )
      )

      val firstChild = Node(
        id = UUID.randomUUID(),
        data = Some("first child"),
        children = List(grandchild1, grandchild2),
        references = Set(
          Reference("child1", grandchild1.id), // Internal ref to child
          Reference("child2", grandchild2.id), // Internal ref to child
          Reference("external", externalNodeId) // External reference
        )
      )

      val parent = Node(
        id = UUID.randomUUID(),
        data = Some("parent"),
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
}
