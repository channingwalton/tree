package io.channing.tree

import java.util.UUID
import scala.collection.mutable

object Mermaid {

  def toFlowchart[T](rootNode: Node[T]): String = {
    val allNodes    = collectAllNodes(rootNode)
    val externalIds = collectExternalNodeIds(allNodes)

    val nodeDefinitions = (allNodes.map(generateNodeDefinition) ++
      externalIds.map(generateExternalNodeDefinition)).mkString("\n    ")
    val treeEdges      = generateTreeEdges(allNodes)
    val referenceEdges = generateReferenceEdges(allNodes)

    s"""flowchart TD
    |    $nodeDefinitions
    |    
    |    $treeEdges
    |    $referenceEdges""".stripMargin
  }

  private def collectAllNodes[T](rootNode: Node[T]): Set[Node[T]] = {
    val visited = mutable.Set[UUID]()
    val nodes   = mutable.Set[Node[T]]()

    def traverse(node: Node[T]): Unit =
      if (!visited.contains(node.id)) {
        visited += node.id
        nodes += node
        node.children.foreach(traverse)
      }

    traverse(rootNode)
    nodes.toSet
  }

  private def collectExternalNodeIds[T](allNodes: Set[Node[T]]): Set[UUID] = {
    val nodeIds = allNodes.map(_.id)
    allNodes.flatMap(_.references.map(_.nodeId)).filterNot(nodeIds.contains)
  }

  private def generateNodeDefinition[T](node: Node[T]): String = {
    val nodeId  = sanitizeId(node.id)
    val dataStr = node.data.map(_.toString).getOrElse("None")
    val label   = s"$dataStr<br/>${node.id}"
    s"""$nodeId["$label"]"""
  }

  private def generateExternalNodeDefinition(id: UUID): String = {
    val nodeId = sanitizeId(id)
    val label  = s"External<br/>$id"
    s"""$nodeId["$label"]"""
  }

  private def generateTreeEdges[T](allNodes: Set[Node[T]]): String = {
    val edges = for {
      node <- allNodes
      child <- node.children
    } yield {
      val parentId = sanitizeId(node.id)
      val childId  = sanitizeId(child.id)
      s"    $parentId --> $childId"
    }
    edges.mkString("\n")
  }

  private def generateReferenceEdges[T](allNodes: Set[Node[T]]): String = {
    val edges = for {
      node <- allNodes
      reference <- node.references
    } yield {
      val sourceId = sanitizeId(node.id)
      val targetId = sanitizeId(reference.nodeId)
      s"""    $sourceId -.->|"${reference.name}"| $targetId"""
    }
    edges.mkString("\n")
  }

  private def sanitizeId(uuid: UUID): String = uuid.toString.take(8).replace("-", "u")
}
