package io.channing.tree

case class TestEntry(value: Option[String]) extends NodeEntry {
  override def reset: NodeEntry = copy(value = None)
}
