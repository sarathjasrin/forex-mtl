package forex

import scala.concurrent.ExecutionContext
import cats.effect._
import forex.config._
import fs2.Stream
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    new Application[IO].stream(executionContext).compile.drain.as(ExitCode.Success)

}

class Application[F[_]: ConcurrentEffect: Timer] {

  def stream(ec: ExecutionContext): Stream[F, Unit] = {
    for {
      config <- Config.stream("app")
      clientResource = BlazeClientBuilder[F](ec).resource
      _ <- Stream.resource(clientResource).flatMap { client =>
        Stream.eval(Module.make[F](config, client)).flatMap { module =>
          BlazeServerBuilder[F](ec)
            .bindHttp(config.http.port, config.http.host)
            .withHttpApp(module.httpApp)
            .serve
        }
      }
    } yield ()
  }

}
