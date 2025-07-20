package io.channing.tree

import java.util.UUID
import scala.collection.mutable

object Mermaid {

  def toFlowchart[T](rootNode: Node[T]): String = {
    val allNodes    = collectAllNodes(rootNode)
    val externalIds = collectExternalNodeIds(allNodes)

    // Create alphabetic mapping for all nodes
    val allNodeIds   = allNodes.map(_.id).toList ++ externalIds.toList
    val nodeAlphaMap = allNodeIds.zipWithIndex.map { case (id, index) =>
      id -> ('A' + index).toChar.toString
    }.toMap

    val nodeDefinitions = (allNodes.map(node => generateNodeDefinitionWithAlpha(node, nodeAlphaMap)) ++
      externalIds.map(id => generateExternalNodeDefinitionWithAlpha(id, nodeAlphaMap))).mkString("\n    ")
    val treeEdges      = generateTreeEdgesWithAlpha(allNodes, nodeAlphaMap)
    val referenceEdges = generateReferenceEdgesWithAlpha(allNodes, nodeAlphaMap)

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

  private def generateNodeDefinitionWithAlpha[T](node: Node[T], alphaMap: Map[UUID, String]): String = {
    val nodeAlpha = alphaMap(node.id)
    val dataStr   = node.data.map(_.toString).getOrElse("None")
    val label     = s"$dataStr <br/> ID: ${node.id}"
    s"""$nodeAlpha["$label"]"""
  }

  private def generateExternalNodeDefinitionWithAlpha(id: UUID, alphaMap: Map[UUID, String]): String = {
    val nodeAlpha = alphaMap(id)
    val label     = s"External <br/> ID: $id"
    s"""$nodeAlpha["$label"]"""
  }

  private def generateTreeEdgesWithAlpha[T](allNodes: Set[Node[T]], alphaMap: Map[UUID, String]): String = {
    val edges = for {
      node <- allNodes
      child <- node.children
    } yield {
      val parentAlpha = alphaMap(node.id)
      val childAlpha  = alphaMap(child.id)
      s"    $parentAlpha --> $childAlpha"
    }
    edges.mkString("\n")
  }

  private def generateReferenceEdgesWithAlpha[T](allNodes: Set[Node[T]], alphaMap: Map[UUID, String]): String = {
    val edges = for {
      node <- allNodes
      reference <- node.references
    } yield {
      val sourceAlpha = alphaMap(node.id)
      val targetAlpha = alphaMap(reference.nodeId)
      s"""    $sourceAlpha -.->|"${reference.name}"| $targetAlpha"""
    }
    edges.mkString("\n")
  }
}
