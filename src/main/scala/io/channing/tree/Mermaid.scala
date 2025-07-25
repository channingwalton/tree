package io.channing.tree

import java.util.UUID
import scala.collection.mutable

object Mermaid:

  def toFlowchart(rootNode: Node): String =
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

  private def collectAllNodes(rootNode: Node): Set[Node] =
    val visited = mutable.Set[UUID]()
    val nodes   = mutable.Set[Node]()

    def traverse(node: Node): Unit =
      if !visited.contains(node.id) then
        visited += node.id
        nodes += node
        node.children.foreach(traverse)

    traverse(rootNode)
    nodes.toSet

  private def collectExternalNodeIds(allNodes: Set[Node]): Set[UUID] =
    val nodeIds = allNodes.map(_.id)
    allNodes.flatMap(_.references.map(_.nodeId)).diff(nodeIds)

  private def generateNodeDefinitionWithAlpha(node: Node, alphaMap: Map[UUID, String]): String =
    val nodeAlpha = alphaMap(node.id)
    val dataStr   = node.data.asString
    val label     = s"$dataStr <br/> ID: ${node.id}"
    s"""$nodeAlpha["$label"]"""

  private def generateExternalNodeDefinitionWithAlpha(id: UUID, alphaMap: Map[UUID, String]): String =
    val nodeAlpha = alphaMap(id)
    val label     = s"External <br/> ID: $id"
    s"""$nodeAlpha["$label"]"""

  private def generateTreeEdgesWithAlpha(allNodes: Set[Node], alphaMap: Map[UUID, String]): String =
    val edges = for {
      node <- allNodes
      child <- node.children
    } yield {
      val parentAlpha = alphaMap(node.id)
      val childAlpha  = alphaMap(child.id)
      s"    $parentAlpha --> $childAlpha"
    }
    edges.mkString("\n")

  private def generateReferenceEdgesWithAlpha(allNodes: Set[Node], alphaMap: Map[UUID, String]): String =
    val edges = for {
      node <- allNodes
      reference <- node.references
    } yield {
      val sourceAlpha = alphaMap(node.id)
      val targetAlpha = alphaMap(reference.nodeId)
      s"""    $sourceAlpha -.->|"${reference.name}"| $targetAlpha"""
    }
    edges.mkString("\n")

end Mermaid
