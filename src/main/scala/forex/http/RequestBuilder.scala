package forex.http

import cats.effect.Sync
import forex.config.OneFrameConfig
import org.http4s._
import org.typelevel.ci.CIString

class RequestBuilder[F[_] : Sync](
  baseUri: Uri,
  defaultHeader: Headers
) {
  def buildGet(endpoint: String, queryParams: List[(String, String)], method: Method = Method.GET): F[Request[F]] = Sync[F].delay {
    val multiParams: Map[String, Seq[String]] =
      queryParams.groupMap(_._1)(_._2).view.mapValues(_.toSeq).toMap

    val uri = baseUri
      .withPath(Uri.Path.unsafeFromString(endpoint))
      .withMultiValueQueryParams(multiParams)
    Request[F](
      method = method,
      uri = uri,
      headers = defaultHeader
    )
  }
}

object RequestBuilder {
  def oneFrame[F[_] : Sync](config: OneFrameConfig): RequestBuilder[F] = new RequestBuilder[F](
    config.host,
    Headers(Header.Raw(CIString("token"), config.token))
  )
}
