package forex.programs.rates

import forex.domain.Rate
import errors._

trait Algebra[F[_]] {
  def getSingle(request: Protocol.GetSingleRateRequest): F[Error Either Rate]

  def get(request: Protocol.GetRatesRequest): F[Either[Error, List[Rate]]]
}
