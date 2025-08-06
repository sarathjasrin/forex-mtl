package forex.config

import cats.effect.Sync
import fs2.Stream
import org.http4s.Uri
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.generic.auto._

object Config {

  /**
   * @param path the property path inside the default configuration
   */
  def stream[F[_] : Sync](path: String): Stream[F, ApplicationConfig] = {
    implicit val uriReader: ConfigReader[Uri] = ConfigReader[String].map(Uri.unsafeFromString)
    Stream.eval(Sync[F].delay(
      ConfigSource.default.at(path).loadOrThrow[ApplicationConfig]))
  }

}
