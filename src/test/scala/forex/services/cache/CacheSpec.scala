package forex.services.cache

import cats.effect.{ContextShift, IO, Timer}
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class CacheSpec extends AsyncFlatSpec with Matchers {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  "Cache" should "store and retrieve values" in {
    val cleanupInterval = 1.minute
    val cacheTtl = 285.seconds

    val cacheIO = Cache.make[IO, String, Int](cacheTtl, cleanupInterval)
    val result = cacheIO.flatMap { cache =>
      for {
        _ <- cache.put("one", 1)
        value <- cache.get("one")
      } yield value
    }

    result.unsafeToFuture().map { maybeValue =>
      maybeValue shouldBe Some(1)
    }
  }

  it should "return None for missing keys" in {
    val test = for {
      cache <- Cache.make[IO, String, String](ttl = 2.seconds, cleanupInterval = 10.seconds)
      value <- cache.get("missing")
    } yield value

    test.unsafeRunSync() shouldBe None
  }

  it should "return None after expiration and cleanup" in {
    val test = for {
      cache <- Cache.make[IO, String, String](ttl = 1.second, cleanupInterval = 500.millis)
      _     <- cache.put("key1", "value1")
      _     <- IO.sleep(1500.millis)
      _     <- cache.cleanup
      value <- cache.get("key1")
    } yield value

    test.unsafeRunSync() shouldBe None
  }

  it should "get many keys with some present and others missing" in {
    val test = for {
      cache <- Cache.make[IO, String, String](ttl = 5.seconds, cleanupInterval = 10.seconds)
      _     <- cache.put("a", "1")
      _     <- cache.put("b", "2")
      results <- cache.getMany(List("a", "b", "c"))
    } yield results

    test.unsafeRunSync() shouldBe List(Some("1"), Some("2"), None)
  }

  it should "not remove unexpired keys during cleanup" in {
    val test = for {
      cache <- Cache.make[IO, String, String](ttl = 5.seconds, cleanupInterval = 10.seconds)
      _     <- cache.put("x", "val")
      _     <- IO.sleep(1.second)
      _     <- cache.cleanup
      value <- cache.get("x")
    } yield value

    test.unsafeRunSync() shouldBe Some("val")
  }
}
