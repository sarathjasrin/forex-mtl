package forex.services.rates.interpreters

import cats.effect.IO
import forex.domain.{Currency, Price, Rate}
import forex.repository.OneFrameRepository
import forex.services.rates.errors.Error.OneFrameLookupFailed
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext

class OneFrameLiveSpec extends AsyncFunSuite with Matchers {

  implicit override def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.global

  def createFakeRate(pair: Rate.Pair): Rate =
    Rate(pair.from,pair.to, Price(BigDecimal(1.25)),Price(BigDecimal(1.25)), Price(BigDecimal(1.25)), "2025-08-07T21:00:00Z")

  test("should return rates when repo returns valid results") {
    val testPair = Rate.Pair(Currency.USD, Currency.EUR)
    val testRates = List(createFakeRate(testPair))

    val fakeRepo = new OneFrameRepository[IO] {
      override def get(pairs: List[Rate.Pair]): IO[List[Rate]] = IO.pure(testRates)
    }

    val service = new OneFrameLive[IO](fakeRepo)
    val result = service.get(List(testPair))

    result.unsafeToFuture().map {
      case Right(rates) => rates shouldEqual testRates
      case _ => fail("Expected Right(List[Rate])")
    }
  }

  test("should return OneFrameLookupFailed when repo returns empty list") {
    val testPair = Rate.Pair(Currency.USD, Currency.EUR)

    val fakeRepo = new OneFrameRepository[IO] {
      override def get(pairs: List[Rate.Pair]): IO[List[Rate]] = IO.pure(List.empty)
    }

    val service = new OneFrameLive[IO](fakeRepo)
    val result = service.get(List(testPair))

    result.unsafeToFuture().map {
      case Left(OneFrameLookupFailed(msg)) =>
        msg should include("No rates found")
      case _ => fail("Expected Left(OneFrameLookupFailed)")
    }
  }

  test("should return OneFrameLookupFailed when repo throws exception") {
    val testPair = Rate.Pair(Currency.USD, Currency.EUR)

    val fakeRepo = new OneFrameRepository[IO] {
      override def get(pairs: List[Rate.Pair]): IO[List[Rate]] =
        IO.raiseError(new RuntimeException("repo error"))
    }

    val service = new OneFrameLive[IO](fakeRepo)
    val result = service.get(List(testPair))

    result.unsafeToFuture().map {
      case Left(OneFrameLookupFailed(msg)) =>
        msg should include("repo error")
      case _ => fail("Expected Left(OneFrameLookupFailed)")
    }
  }
}
