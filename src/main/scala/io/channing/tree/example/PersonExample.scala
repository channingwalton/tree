package io.channing.tree.example

import io.channing.tree.{ Mermaid, Node, Reference }
import java.util.UUID

object PersonExample {

  def createPersonWithAddress(): Node[String] = {
    // Create address component nodes
    val line1Node = Node(
      id = UUID.randomUUID(),
      data = Some("123 Main Street")
    )

    val postcodeNode = Node(
      id = UUID.randomUUID(),
      data = Some("SW1A 1AA")
    )

    val countryNode = Node(
      id = UUID.randomUUID(),
      data = Some("United Kingdom")
    )

    // Create address node with children
    val addressNode = Node(
      id = UUID.randomUUID(),
      data = Some("Home Address"),
      children = List(line1Node, postcodeNode, countryNode),
      references = Set(
        Reference("line1", line1Node.id),
        Reference("postcode", postcodeNode.id),
        Reference("country", countryNode.id)
      )
    )

    // Create person node with address as child
    val personNode = Node(
      id = UUID.randomUUID(),
      data = Some("John Doe"),
      children = List(addressNode),
      references = Set(
        Reference("address", addressNode.id)
      )
    )

    personNode
  }

  def createPersonWithMultipleAddresses(): Node[String] = {
    // Create home address
    val homeLine1    = Node(id = UUID.randomUUID(), data = Some("123 Main Street"))
    val homePostcode = Node(id = UUID.randomUUID(), data = Some("SW1A 1AA"))
    val homeCountry  = Node(id = UUID.randomUUID(), data = Some("United Kingdom"))

    val homeAddress = Node(
      id = UUID.randomUUID(),
      data = Some("Home Address"),
      children = List(homeLine1, homePostcode, homeCountry),
      references = Set(
        Reference("line1", homeLine1.id),
        Reference("postcode", homePostcode.id),
        Reference("country", homeCountry.id)
      )
    )

    // Create work address
    val workLine1    = Node(id = UUID.randomUUID(), data = Some("456 Business Ave"))
    val workPostcode = Node(id = UUID.randomUUID(), data = Some("EC1A 1BB"))
    val workCountry  = Node(id = UUID.randomUUID(), data = Some("United Kingdom"))

    val workAddress = Node(
      id = UUID.randomUUID(),
      data = Some("Work Address"),
      children = List(workLine1, workPostcode, workCountry),
      references = Set(
        Reference("line1", workLine1.id),
        Reference("postcode", workPostcode.id),
        Reference("country", workCountry.id)
      )
    )

    // Create person with multiple addresses
    val personNode = Node(
      id = UUID.randomUUID(),
      data = Some("Jane Smith"),
      children = List(homeAddress, workAddress),
      references = Set(
        Reference("homeAddress", homeAddress.id),
        Reference("workAddress", workAddress.id)
      )
    )

    personNode
  }

  def main(args: Array[String]): Unit = {
    println("=== Simple Person with Address ===")
    val simplePerson = createPersonWithAddress()
    println(Mermaid.toFlowchart(simplePerson))

    println("\n=== Person with Multiple Addresses ===")
    val complexPerson = createPersonWithMultipleAddresses()
    println(Mermaid.toFlowchart(complexPerson))
  }
}
