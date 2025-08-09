package forex.programs.rates

import forex.domain.{Currency, Rate}

object Protocol {

  final case class GetRatesRequest(
    pairs: List[Rate.Pair]
  )

  final case class GetSingleRateRequest(
    from: Currency,
    to: Currency
  )

}
