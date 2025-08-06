package forex.repository

import cats.effect.Sync
import cats.syntax.all._
import forex.config.OneFrameConfig
import forex.domain.Rate
import forex.domain.Rate.Pair
import forex.http.RequestBuilder
import forex.services.logger.LoggerService
import io.circe.Decoder
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf
import org.http4s.client.Client

trait OneFrameRepository[F[_]] {
  def get(pairs: List[Pair]): F[List[Rate]]
}


object OneFrameRepository {
  def make[F[_] : Sync](client: Client[F], config: OneFrameConfig): OneFrameRepository[F] = new OneFrameRepository[F] {

    private val logger = LoggerService.loggerFor[OneFrameRepository[F]]
    private val requestBuilder: RequestBuilder[F] = RequestBuilder.oneFrame[F](config)
    implicit val rateDecoder: Decoder[Rate] = Decoder.forProduct6(
      "from",
      "to",
      "bid",
      "ask",
      "price",
      "time_stamp"
    )(Rate.apply)

    implicit val rateListDecoder: EntityDecoder[F, List[Rate]] =
      jsonOf[F, List[Rate]]

    override def get(pairs: List[Pair]): F[List[Rate]] = {
      val queryParams = pairs.map {
        case Pair(from, to) => "pair" -> s"${from.show}${to.show}"
      }

      for {
        request <- requestBuilder.buildGet("/rates", queryParams)
        _ <- LoggerService.info[F](logger, s"Sending request to OneFrame API: $request")
        response <- client.expect[List[Rate]](request)
      } yield response
    }
  }

}