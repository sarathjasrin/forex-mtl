package forex.http
package rates

import forex.domain.Currency.show
import forex.domain.Rate.Pair
import forex.domain._
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredEncoder

object Protocol {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  final case class GetApiRequest(
    pairs: List[String]
  )

  final case class GetApiResponse(
    from: Currency,
    to: Currency,
    bid: Price,
    ask: Price,
    price: Price,
    timestamp: String
  )

  implicit val currencyEncoder: Encoder[Currency] =
    Encoder.instance[Currency] {
      show.show _ andThen Json.fromString
    }

  implicit val pairEncoder: Encoder[Pair] =
    deriveConfiguredEncoder[Pair]

  implicit val rateEncoder: Encoder[Rate] =
    deriveConfiguredEncoder[Rate]

  implicit val getApiResponseEncoder: Encoder[GetApiResponse] =
    deriveConfiguredEncoder[GetApiResponse]

}
