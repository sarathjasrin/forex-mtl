package forex

import cats.effect.{Concurrent, Timer}
import cats.syntax.all._
import forex.config.ApplicationConfig
import forex.domain.Rate
import forex.http.rates.RatesHttpRoutes
import forex.services._
import forex.programs._
import forex.repository.OneFrameRepository
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, Timeout}
import org.http4s.client.Client

class Module[F[_]](
  val httpApp: HttpApp[F]
)

object Module {
  def make[F[_]: Concurrent: Timer](config: ApplicationConfig, client: Client[F]): F[Module[F]] = {
    for {
      cacheServices <- CacheServices.make[F, String, Rate](config.oneFrame.cacheTtl, config.oneFrame.cleanupInterval)
      oneFrameRepository = OneFrameRepository.make(client, config.oneFrame)
      ratesService = RatesServices.live[F](oneFrameRepository)
      ratesProgram = RatesProgram[F](ratesService, cacheServices)
      ratesHttpRoutes = new RatesHttpRoutes[F](ratesProgram).routes
      routesMiddleware = AutoSlash(_: HttpRoutes[F])
      appMiddleware = Timeout(config.http.timeout)(_: HttpApp[F])
      http = routesMiddleware(ratesHttpRoutes).orNotFound
      httpApp = appMiddleware(http)
    } yield new Module[F](httpApp)
  }
}

