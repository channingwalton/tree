# Tree augmented by references between nodes

## 1. Executive Summary

The tree is a specialized tree-based data structure that supports references to any node, and node duplication with intelligent reference management.

## 2. Overview

### 2.1 Purpose
The library provides a tree structure where:
- Nodes have a NodeEntry
- Nodes can have multiple children
- Each node has a unique UUID identifier
- Nodes can have named references to other nodes in the graph
- When a node is duplicated, all references internal to the subtree are updated to point to the correspongin nodes in the duplicate node. References external to the copied node remain in place.
- When a node is duplicated, the Node data is reset

### 2.2 Key Characteristics
- **Tree Structure**: Fundamentally a tree with parent-child relationships
- **Multiple Children**: Each node can have 1 or more children
- **References**
  - References are named
  - Nodes can reference other nodes with named relationships
  - Node references can be relative using a path
- **UUID Identifiers**: Simple, unique identification using UUIDs
- **Smart Duplication**: Copy operations intelligently update internal references
