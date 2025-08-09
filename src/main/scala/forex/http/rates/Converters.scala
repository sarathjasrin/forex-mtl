package forex.http.rates

import forex.domain._

object Converters {

  import Protocol._

  private[rates] implicit class GetApiResponseOps(val rate: List[Rate]) extends AnyVal {
    def asGetApiResponse: List[GetApiResponse] =
      rate.map(r =>
        GetApiResponse(
          from = r.from,
          to = r.to,
          bid = r.bid,
          ask = r.ask,
          price = r.price,
          timestamp = r.timestamp
        )
      )
  }

  private[rates] implicit class GetApiResponseSingle(val rate: Rate) extends AnyVal {
    def asGetApiResponse: GetApiResponse =
      GetApiResponse(
        from = rate.from,
        to = rate.to,
        bid = rate.bid,
        ask = rate.ask,
        price = rate.price,
        timestamp = rate.timestamp
      )
  }

}
