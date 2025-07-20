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

    "correctly resolve internal and external references" in {
      val externalNodeId = UUID.randomUUID()

      val grandchild = Node(
        id = UUID.randomUUID(),
        data = Some("grandchild")
      )

      val firstChild = Node(
        id = UUID.randomUUID(),
        data = Some("first child"),
        children = List(grandchild),
        references = Set(
          Reference("sibling", grandchild.id), // Internal reference within subtree
          Reference("external", externalNodeId) // External reference outside subtree
        )
      )

      val parent = Node(
        id = UUID.randomUUID(),
        data = Some("parent"),
        children = List(firstChild)
      )

      val updatedParent = parent.addChild()

      val copiedChild      = updatedParent.children(1)
      val copiedGrandchild = copiedChild.children.head

      // Check that internal reference was updated to point to copied grandchild
      val siblingRef = copiedChild.references.find(_.name == "sibling")
      assert(siblingRef.isDefined)
      assert(siblingRef.get.nodeId == copiedGrandchild.id)
      assert(siblingRef.get.nodeId != grandchild.id)

      // Check that external reference remained unchanged
      val externalRef = copiedChild.references.find(_.name == "external")
      assert(externalRef.isDefined)
      assert(externalRef.get.nodeId == externalNodeId)
    }
  }
}
