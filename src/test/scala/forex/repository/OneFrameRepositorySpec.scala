package forex.repository

import cats.effect.IO
import forex.config.OneFrameConfig
import forex.domain.Rate.Pair
import forex.domain.{Currency, Price, Rate}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._

class OneFrameRepositorySpec extends AnyWordSpec with Matchers {

  "OneFrameRepository" should {
    "return a list of rates from the OneFrame API" in {
      val testRates = List(
        Rate(
          from = Currency.USD,
          to = Currency.JPY,
          bid = Price(BigDecimal(1.25)),
          ask = Price(BigDecimal(1.27)),
          price = Price(BigDecimal(1.26)),
          timestamp = "2025-08-07T21:00:00Z"
        )
      )

      val client = Client.fromHttpApp[IO](
        HttpApp[IO] { _ =>
          IO.pure(
            Response[IO](status = Status.Ok).withEntity(testRates.asJson)
          )
        }
      )

      val config = OneFrameConfig(
        host = Uri.unsafeFromString("http://localhost:8080"),
        token = "test-token",
        cacheTtl = 200.seconds,
        cleanupInterval = 50.seconds
      )

      val repo = OneFrameRepository.make[IO](client, config)

      val result = repo.get(List(Pair(Currency.USD, Currency.JPY))).unsafeRunSync()

      result shouldEqual testRates
    }
  }
}
