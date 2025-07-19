import java.util.UUID

final case class Node[T](
    id: UUID = UUID.randomUUID(),
    data: Option[T] = None,
    children: List[Node[T]] = List.empty[Node[T]],
    references: Set[Reference] = Set.empty[Reference]
)

