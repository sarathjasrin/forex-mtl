package forex.domain

case class Rate(
  from: Currency,
  to: Currency,
  bid: Price,
  ask: Price,
  price: Price,
  timestamp: String
)

object Rate {
  final case class Pair(
    from: Currency,
    to: Currency
  )
}
