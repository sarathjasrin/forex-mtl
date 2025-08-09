package forex.programs.rates

import cats.data.EitherT
import cats.effect.Sync
import cats.syntax.all._
import forex.domain._
import forex.programs.rates.errors._
import forex.services.{RatesServiceDummy, RatesServiceLive}
import forex.services.cache.CacheAlgebra
import forex.services.logger.LoggerService


class Program[F[_] : Sync](
  ratesServiceDummy: RatesServiceDummy[F],
  ratesServiceLive: RatesServiceLive[F],
  cache: CacheAlgebra[F, String, Rate]
) extends Algebra[F] {
  private val logger = LoggerService.loggerFor[Program[F]]

  override def getSingle(request: Protocol.GetSingleRateRequest): F[Either[Error, Rate]] =
    EitherT(ratesServiceDummy.get(Rate.Pair(from = request.from, to = request.to))).leftMap(toProgramError(_)).value

  override def get(request: Protocol.GetRatesRequest): F[Either[Error, List[Rate]]] = {
    val pairStrings = request.pairs.map(p => s"${p.from.show}${p.to.show}")

    EitherT.liftF {
      cache.getMany(pairStrings).flatMap { cachedRates =>
        val cached = cachedRates.flatten
        val missingPairs = pairStrings.zip(cachedRates).collect {
          case (pair, None) => pair
        }

        if (missingPairs.isEmpty) {
          for {
            _ <- LoggerService.info[F](logger, s"Cache hit for all pairs: $pairStrings")
          } yield Right(cached): Either[Error, List[Rate]]
        } else {
          LoggerService.info[F](logger, s"Cache miss for pairs: $missingPairs") >>
            EitherT(
              ratesServiceLive.get(
                request.pairs.filter(p => missingPairs.contains(s"${p.from.show}${p.to.show}"))
              )
            ).leftMap(errors.toProgramError)
              .value
              .flatTap {
                case Right(rates) =>
                  LoggerService.info[F](logger, s"Received rates from service: $rates") >>
                    rates.traverse_ { rate =>
                      cache.put(s"${rate.from}${rate.to}", rate)
                    }
                case Left(_) => Sync[F].unit
              }.map(_.map(rates => cached ++ rates.filterNot(r => cached.exists(_.from == r.from && r.to == r.to))))
        }
      }
    }.merge
  }
}

object Program {
  def apply[F[_] : Sync](
    ratesServiceDummy: RatesServiceDummy[F],
    ratesServiceLive: RatesServiceLive[F],
    cache: CacheAlgebra[F, String, Rate]
  ): Algebra[F] = new Program[F](ratesServiceDummy, ratesServiceLive, cache)
}

