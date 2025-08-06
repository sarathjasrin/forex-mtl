package forex.domain

import io.circe.Decoder


case class Price(value: BigDecimal) extends AnyVal

object Price {
  def apply(value: Integer): Price =
    Price(BigDecimal(value))

  implicit val decodePrice: Decoder[Price] = Decoder.instance { cursor =>
    cursor.as[BigDecimal].map(Price(_))
  }
}
