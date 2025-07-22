package io.channing.tree

import java.util.UUID
import scala.collection.mutable

trait NodeEntry {
  def reset: NodeEntry
}

final case class Node(
    id: UUID = UUID.randomUUID(),
    data: NodeEntry,
    children: List[Node] = List.empty[Node],
    references: Set[Reference] = Set.empty[Reference]
) {

  def addChild(): Node =
    children.headOption match {
      case None             => this
      case Some(firstChild) =>
        val copiedChild = copySubtreeWithNewIds(firstChild)
        this.copy(children = children :+ copiedChild)
    }

  def addChildAt(id: UUID): Node =
    if (this.id == id) {
      this.addChild()
    } else {
      val updatedChildren = children.map(child => child.addChildAt(id))
      if (updatedChildren != children) {
        this.copy(children = updatedChildren)
      } else {
        this
      }
    }

  private def copySubtreeWithNewIds(node: Node): Node = {
    val idMapping = mutable.Map[UUID, UUID]()
    copyNodeRecursively(node, idMapping)
  }

  private def copyNodeRecursively(node: Node, idMapping: mutable.Map[UUID, UUID]): Node = {
    val newId = UUID.randomUUID()
    idMapping(node.id) = newId

    val copiedChildren = node.children.map(child => copyNodeRecursively(child, idMapping))

    val updatedReferences = node.references.map { ref =>
      idMapping.get(ref.nodeId) match {
        case Some(newNodeId) => ref.copy(nodeId = newNodeId) // Internal reference
        case None            => ref // External reference - keep unchanged
      }
    }

    Node(
      id = newId,
      data = node.data.reset,
      children = copiedChildren,
      references = updatedReferences
    )
  }
}
