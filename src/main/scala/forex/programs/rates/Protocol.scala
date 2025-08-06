package forex.programs.rates

import forex.domain.Rate

object Protocol {

  final case class GetRatesRequest(
    pairs: List[Rate.Pair]
  )

}
