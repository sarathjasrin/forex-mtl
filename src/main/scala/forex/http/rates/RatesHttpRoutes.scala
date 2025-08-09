package forex.http
package rates

import cats.effect.Sync
import cats.syntax.flatMap._
import forex.programs.RatesProgram
import forex.programs.rates.{Protocol => RatesProgramProtocol}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{EntityEncoder, HttpRoutes}

class RatesHttpRoutes[F[_] : Sync](rates: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._
  import Protocol._
  import QueryParams._

  private[http] val prefixPath = "/rates"

  implicit def getApiResponseListEncoder: EntityEncoder[F, List[GetApiResponse]] =
    jsonEncoderOf[F, List[GetApiResponse]]

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      rates.getSingle(RatesProgramProtocol.GetSingleRateRequest(from, to)).flatMap(Sync[F].fromEither).flatMap { rate =>
        Ok(rate.asGetApiResponse)
      }
    case GET -> Root :? QueryParams.PairQueryParam(pairsValidated) =>
      pairsValidated.fold(
        failures => BadRequest(failures.map(_.details).toList.mkString(", ")),
        pairs => rates.get(RatesProgramProtocol.GetRatesRequest(pairs)).flatMap {
          case Right(rate) => Ok(rate.asGetApiResponse)
          case Left(error) => BadRequest(error.toString)
        }
      )
    case _ => NotFound("Invalid Route")
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
