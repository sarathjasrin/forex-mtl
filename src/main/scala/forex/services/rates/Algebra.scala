package forex.services.rates

import forex.domain.Rate
import errors._

trait Algebra[F[_]] {
  def get(pairs: List[Rate.Pair]): F[Either[Error, List[Rate]]]
}
