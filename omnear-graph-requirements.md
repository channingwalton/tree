# Omnear Graph Requirements Document

## 1. Executive Summary

The Omnear Graph is a specialized tree-based data structure that supports unique node identification through UUIDs and enables node duplication with intelligent reference management. This document outlines the complete requirements for implementing the Omnear Graph data structure.

## 2. Overview

### 2.1 Purpose
The Omnear Graph provides a tree structure where:
- Nodes can have multiple children
- Each node has a unique UUID identifier
- Nodes can have named references to other nodes in the graph
- Child nodes can be duplicated with automatic reference resolution

### 2.2 Key Characteristics
- **Tree Structure**: Fundamentally a tree with parent-child relationships
- **Multiple Children**: Each node can have 1 or more children
- **Named Reference System**: Nodes can reference other nodes with named relationships
- **UUID Identifiers**: Simple, unique identification using UUIDs
- **Smart Duplication**: Copy operations intelligently update internal references

## 3. Data Structure Requirements

### 3.1 Node Structure

Each node in the Omnear Graph must contain:

#### 3.1.1 Identifier
- **Type**: UUID (Universally Unique Identifier)
- **Format**: Standard UUID format (e.g., "550e8400-e29b-41d4-a716-446655440000")
- **Generation**: Use standard UUID generation

#### 3.1.2 Node Properties
- **Children**: Ordered list of child nodes
- **References**: Set of named references to other nodes
- **Data**: Optional payload/data associated with the node

#### 3.1.3 Reference Structure
```typescript
interface Reference {
  name: string;      // The name/type of the reference
  nodeId: UUID;      // The ID of the referenced node
}
```

### 3.2 Graph Structure
- **Root Node**: The graph must have a single root node
- **Parent Pointers**: Optional but recommended for traversal efficiency
- **Node Registry**: A map/dictionary for O(1) node lookup by UUID

## 4. Functional Requirements

### 4.1 Core Operations

#### 4.1.1 Node Creation
- **Create Root**: Initialize a graph with a root node
- **Add Child**: Add a new child node to an existing parent
- **Input**: Parent node ID, new node data
- **Output**: New node with generated UUID

#### 4.1.2 Node Identification
- **Generate ID**: Create new UUID for each node
- **Find Node**: Locate a node by its UUID
- **ID Validation**: Verify UUID format and existence

#### 4.1.3 Reference Management
- **Add Reference**: Create a named reference from one node to another
- **Remove Reference**: Delete an existing reference by name
- **Update Reference**: Change the target of a named reference
- **Get References**: Retrieve all references from a node
- **Get Reference By Name**: Find a specific named reference
- **Validate Reference**: Check if a referenced node exists

#### 4.1.4 Tree Traversal
- **Depth-First Search**: Pre-order, in-order, and post-order traversal
- **Breadth-First Search**: Level-order traversal
- **Find Node**: Locate a node by its UUID
- **Path Finding**: Find path between two nodes

### 4.2 Advanced Operations

#### 4.2.1 Node Duplication with Reference Resolution
- **Copy Subtree**: Duplicate a node and all its descendants
- **Reference Resolution Process**:
  1. Create a mapping table: `Map<OriginalUUID, NewUUID>`
  2. Recursively copy all nodes in the subtree, generating new UUIDs
  3. Store each original→new UUID mapping
  4. For each reference in the copied nodes:
     - If the referenced node ID exists in the mapping (internal reference), update to new UUID
     - If the referenced node ID is not in the mapping (external reference), keep unchanged
  5. Return the root of the copied subtree

#### 4.2.2 Graph Operations
- **Merge Subtrees**: Combine two subtrees while maintaining UUID uniqueness
- **Prune Subtree**: Remove a node and all its descendants
- **Export/Import**: Serialize and deserialise the graph
- **Reference Integrity Check**: Validate all references point to existing nodes

### 4.3 Query Operations
- **Get Children**: Retrieve all children of a node
- **Get Parent**: Find the parent of a node
- **Get Ancestors**: List all ancestors up to root
- **Get Descendants**: List all descendants of a node
- **Get Siblings**: Find all siblings of a node
- **Find Referencing Nodes**: Find all nodes that reference a given node

## 5. Non-Functional Requirements

### 5.1 Performance
- **Node Lookup**: O(1) average case using UUID
- **Child Addition**: O(1) amortized
- **Tree Traversal**: O(n) where n is number of nodes
- **Duplication**: O(m) where m is size of subtree being copied
- **Reference Resolution**: O(r) where r is number of references in subtree

### 5.2 Memory
- **Efficient Storage**: UUID storage optimization
- **Reference Integrity**: Ensure references don't create memory leaks
- **Scalability**: Support graphs with 10^6+ nodes

### 5.3 Reliability
- **UUID Uniqueness**: Use proper UUID generation to guarantee uniqueness
- **Reference Consistency**: Maintain valid references during operations
- **Transaction Safety**: Operations should be atomic
- **Referential Integrity**: No dangling references after operations

## 6. API Specification

### 6.1 Core Classes/Interfaces

```scala
import java.util.UUID
import scala.collection.mutable

case class Reference(name: String, nodeId: UUID)

case class Node[T](
  id: UUID,
  data: Option[T] = None,
  children: mutable.ListBuffer[Node[T]] = mutable.ListBuffer.empty,
  references: mutable.Set[Reference] = mutable.Set.empty
)

sealed trait TraversalOrder
case object PreOrder extends TraversalOrder
case object InOrder extends TraversalOrder
case object PostOrder extends TraversalOrder
case object BreadthFirst extends TraversalOrder

trait OmnearGraph[T] {
  val root: Node[T]
  
  // Node operations
  def createNode(data: Option[T] = None): Node[T]
  def addChild(parentId: UUID, data: Option[T] = None): Node[T]
  def findNode(id: UUID): Option[Node[T]]
  def removeNode(id: UUID): Boolean
  
  // Reference operations
  def addReference(fromId: UUID, name: String, toId: UUID): Unit
  def removeReference(fromId: UUID, name: String): Unit
  def updateReference(fromId: UUID, name: String, newToId: UUID): Unit
  def getReference(fromId: UUID, name: String): Option[Reference]
  
  // Duplication
  def copySubtree(nodeId: UUID): Node[T]
  def copyChild(parentId: UUID, childId: UUID): Node[T]
  
  // Traversal
  def traverse(callback: Node[T] => Unit, order: TraversalOrder = PreOrder): Unit
  
  // Query operations
  def findReferencingNodes(targetId: UUID): Seq[Node[T]]
}

case class CopyResult[T](
  newRoot: Node[T],
  idMapping: Map[UUID, UUID]
)
```

### 6.2 Usage Examples

```scala
// Create a new graph
val graph = new OmnearGraphImpl[Map[String, String]]()

// Add children
val child1 = graph.addChild(graph.root.id, Some(Map("name" -> "Child 1")))
val child2 = graph.addChild(graph.root.id, Some(Map("name" -> "Child 2")))
val grandchild = graph.addChild(child1.id, Some(Map("name" -> "Grandchild")))

// Add references
graph.addReference(child1.id, "sibling", child2.id)
graph.addReference(grandchild.id, "parent", child1.id)
graph.addReference(grandchild.id, "external", someExternalNodeId)

// Copy a subtree (child1 and its descendants)
val copiedChild = graph.copyChild(graph.root.id, child1.id)
// copiedChild has a new UUID
// grandchild copy's "parent" reference now points to copiedChild (internal)
// grandchild copy's "external" reference still points to someExternalNodeId (external)
```

## 7. Error Handling

### 7.1 Error Cases
- **Invalid UUID**: Attempting to operate on non-existent node
- **Duplicate Reference Names**: Adding a reference with an existing name
- **Invalid Reference Target**: Reference points to non-existent node
- **Circular References**: Detecting and handling circular dependencies
- **Invalid Operations**: Operations that would violate tree structure

### 7.2 Error Responses
- Clear error messages indicating the nature of the problem
- Error codes for programmatic handling
- Recovery suggestions where applicable

## 8. Testing Requirements

### 8.1 Unit Tests
- UUID generation and validation
- Node creation and manipulation
- Reference management (add, remove, update)
- Tree traversal algorithms
- Reference resolution during copy

### 8.2 Integration Tests
- Complex duplication scenarios with mixed internal/external references
- Large graph operations
- Reference integrity across operations
- Performance benchmarks
- Edge cases in reference resolution

### 8.3 Test Scenarios for Duplication
1. **Simple Copy**: Node with no references
2. **Internal References**: Nodes referencing each other within copied subtree
3. **External References**: Nodes referencing nodes outside copied subtree
4. **Mixed References**: Combination of internal and external references
5. **Deep Nesting**: Multiple levels with complex reference patterns
6. **Circular References**: Nodes with circular reference patterns

## 9. Documentation Requirements

### 9.1 API Documentation
- Complete method signatures
- Parameter descriptions
- Return value specifications
- Usage examples for each operation
- Reference resolution algorithm explanation

### 9.2 Implementation Guide
- Architecture overview
- Design patterns used
- Performance considerations
- Best practices for reference management

### 9.3 User Guide
- Getting started tutorial
- Common use cases
- Reference patterns and best practices
- Troubleshooting guide
- FAQ section

## 10. Future Considerations

### 10.1 Potential Extensions
- **Reference Types**: Typed references with validation
- **Bidirectional References**: Automatic reverse reference management
- **Reference Constraints**: Cardinality and type constraints
- **Versioning**: Track changes to nodes and references over time
- **Distributed Support**: Enable graph distribution across systems
- **Query Language**: DSL for complex graph queries with reference traversal

### 10.2 Optimization Opportunities
- **UUID Pool**: Pre-generate UUIDs for performance
- **Reference Index**: Reverse index for finding referencing nodes
- **Lazy Loading**: Load subtrees on demand
- **Caching**: Cache frequently accessed paths and references
- **Batch Operations**: Optimize bulk reference updates

## 11. Acceptance Criteria

The implementation will be considered complete when:
1. All core operations are implemented and tested
2. Reference resolution during copy works correctly for all scenarios
3. Performance requirements are met
4. Documentation is complete
5. Error handling is comprehensive
6. Test coverage exceeds 90%
7. API is stable and intuitive
8. Reference integrity is maintained across all operations

## 12. Glossary

- **Node**: Basic unit of the graph containing data and relationships
- **UUID**: Universally Unique Identifier for each node
- **Reference**: Named pointer from one node to another
- **Internal Reference**: Reference to a node within the same subtree being copied
- **External Reference**: Reference to a node outside the subtree being copied
- **Subtree**: A node and all its descendants
- **Reference Resolution**: Process of updating references during copy operations
- **Graph**: The complete data structure including all nodes
