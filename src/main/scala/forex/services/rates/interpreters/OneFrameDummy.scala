package forex.services.rates.interpreters

import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import forex.domain.Currency.{JPY, USD}
import forex.domain.{Price, Rate, Timestamp}
import forex.services.rates.DummyAlgebra
import forex.services.rates.errors._

class OneFrameDummy[F[_] : Applicative] extends DummyAlgebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    Rate(
      from = JPY, to = USD, bid = Price(BigDecimal(100)), ask = Price(BigDecimal(89)), price = Price(BigDecimal(55)), timestamp = Timestamp.toString
    ).asRight[Error].pure[F]
  }

}