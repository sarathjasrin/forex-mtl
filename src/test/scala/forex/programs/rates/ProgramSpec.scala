
package forex.programs.rates

import cats.effect.IO
import forex.domain._
import forex.programs.rates.Protocol.GetRatesRequest
import forex.services.RatesService
import forex.services.cache.CacheAlgebra
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import forex.services.rates.errors._

class ProgramSpec extends AsyncWordSpec with Matchers {

  val dummyRate: Rate = Rate(Currency.USD, Currency.JPY, Price(BigDecimal(1.25)), Price(BigDecimal(1.25)), Price(BigDecimal(1.25)), "2025-08-07T21:00:00Z")
  val rateKey: String = s"${dummyRate.from}${dummyRate.to}"

  "Program.get" should {
    "return cached rate if all requested pairs are found in cache" in {
      val request = GetRatesRequest(List(Rate.Pair(dummyRate.from, dummyRate.to)))

      val ratesService: RatesService[IO] = new RatesService[IO] {
        override def get(pairs: List[Rate.Pair]): IO[Either[Error, List[Rate]]] =
          fail("Should not call service when cache has data")
      }

      val cache = new CacheAlgebra[IO, String, Rate] {
        override def getMany(keys: List[String]): IO[List[Option[Rate]]] =
          IO.pure(List(Some(dummyRate)))

        override def put(key: String, value: Rate): IO[Unit] =
          IO.unit

        override def get(key: String): IO[Option[Rate]] = IO.pure(None)

        override def cleanup: IO[Unit] = IO.unit
      }

      val program = Program[IO](ratesService, cache)
      program.get(request).unsafeToFuture().map { result =>
        result shouldBe Right(List(dummyRate))
      }
    }

    "fetch missing pairs from service and update cache" in {
      val request = GetRatesRequest(List(Rate.Pair(dummyRate.from, dummyRate.to)))

      var putCalled = false

      val ratesService: RatesService[IO] = new RatesService[IO] {
        override def get(pairs: List[Rate.Pair]): IO[Either[Error, List[Rate]]] =
          IO.pure(Right(List(dummyRate)))
      }

      val cache = new CacheAlgebra[IO, String, Rate] {
        override def getMany(keys: List[String]): IO[List[Option[Rate]]] =
          IO.pure(List(None))

        override def put(key: String, value: Rate): IO[Unit] =
          IO {
            putCalled = true
          }

        override def get(key: String): IO[Option[Rate]] = IO.pure(None)

        override def cleanup: IO[Unit] = IO.unit
      }

      val program = Program[IO](ratesService, cache)
      program.get(request).unsafeToFuture().map { result =>
        result shouldBe Right(List(dummyRate))
        putCalled shouldBe true
      }
    }

    "return error if service fails to fetch rates" in {
      val request = GetRatesRequest(List(Rate.Pair(dummyRate.from, dummyRate.to)))
      val error = new RuntimeException("Service failure")

      val ratesService: RatesService[IO] = new RatesService[IO] {
        override def get(pairs: List[Rate.Pair]): IO[Either[Error, List[Rate]]] =
          IO.pure(Left(Error.OneFrameLookupFailed(error.getMessage)))
      }

      val cache = new CacheAlgebra[IO, String, Rate] {
        override def getMany(keys: List[String]): IO[List[Option[Rate]]] =
          IO.pure(List(None))

        override def put(key: String, value: Rate): IO[Unit] =
          IO.unit

        override def get(key: String): IO[Option[Rate]] = IO.pure(None)

        override def cleanup: IO[Unit] = IO.unit
      }

      val program = Program[IO](ratesService, cache)
      program.get(request).unsafeToFuture().map { result =>
        result shouldBe a[Left[_, _]]
      }
    }
  }
}
