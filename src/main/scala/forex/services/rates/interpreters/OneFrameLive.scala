package forex.services.rates.interpreters

import cats.effect.Sync
import cats.syntax.all._
import forex.domain.Rate
import forex.repository.OneFrameRepository
import forex.services.logger.LoggerService
import forex.services.rates.Algebra
import forex.services.rates.errors.Error._
import forex.services.rates.errors._

class OneFrameLive[F[_] : Sync](repo: OneFrameRepository[F]) extends Algebra[F] {
  private val logger = LoggerService.loggerFor[OneFrameLive[F]]

  override def get(pairs: List[Rate.Pair]): F[Either[Error, List[Rate]]] = {
    LoggerService.info[F](logger, s"Fetching rates for pairs: ${pairs.toString}") >>
      repo.get(pairs).attempt.map {
        case Right(rates) if rates.nonEmpty => Right(rates)
        case Left(error) =>
          LoggerService.error[F](logger, error.getMessage)
          Left(OneFrameLookupFailed(error.getMessage))
        case _ => Left(OneFrameLookupFailed("No rates found"))
      }
  }
}
