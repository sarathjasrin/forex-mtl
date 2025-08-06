package forex.services.rates

import forex.domain.Rate
import errors._

trait RateAlgebra[F[_]] {
  def getRates(pairs: List[Rate.Pair]): F[Either[Error, List[Rate]]]
}

