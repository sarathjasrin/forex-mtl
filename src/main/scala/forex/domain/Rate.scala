package forex.domain

import io.circe.Decoder

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

  implicit val rateDecoder: Decoder[Rate] = Decoder.forProduct6(
    "from",
    "to",
    "bid",
    "ask",
    "price",
    "time_stamp"
  )(Rate.apply)
}
