package io.channing.tree

import java.util.UUID
import scala.collection.mutable

final case class Node[T](
    id: UUID = UUID.randomUUID(),
    data: Option[T] = None,
    children: List[Node[T]] = List.empty[Node[T]],
    references: Set[Reference] = Set.empty[Reference]
) {

  def addChild(): Node[T] =
    children.headOption match {
      case None             => this
      case Some(firstChild) =>
        val copiedChild = copySubtreeWithNewIds(firstChild)
        this.copy(children = children :+ copiedChild)
    }

  private def copySubtreeWithNewIds(node: Node[T]): Node[T] = {
    val idMapping = mutable.Map[UUID, UUID]()
    copyNodeRecursively(node, idMapping)
  }

  private def copyNodeRecursively(node: Node[T], idMapping: mutable.Map[UUID, UUID]): Node[T] = {
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
      data = None,
      children = copiedChildren,
      references = updatedReferences
    )
  }
}
