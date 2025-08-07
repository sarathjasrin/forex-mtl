package forex.services.cache

import cats.effect.concurrent.Ref
import cats.effect.{Clock, Concurrent, Timer}
import cats.syntax.all._
import forex.services.logger.LoggerService

import scala.concurrent.duration._

case class CacheEntry[A](value: A, expiresAt: Long)

object Cache {
  def make[F[_] : Concurrent : Clock : Timer, K, V](ttl: FiniteDuration, cleanupInterval: FiniteDuration): F[CacheAlgebra[F, K, V]] = {
    for {
      ref <- Ref.of[F, Map[K, CacheEntry[V]]](Map.empty)
      logger = LoggerService.loggerFor[Cache.type]
      cache = new CacheAlgebra[F, K, V] {
        override def get(key: K): F[Option[V]] = for {
          now <- Clock[F].realTime(MILLISECONDS)
          map <- ref.get
          entry = map.get(key).filter(_.expiresAt > now)
        } yield entry.map(_.value)

        override def getMany(keys: List[K]): F[List[Option[V]]] = for {
          now <- Clock[F].realTime(MILLISECONDS)
          map <- ref.get
          entries = keys.map(k => map.get(k).filter(_.expiresAt > now).map(_.value))
        } yield entries

        override def put(key: K, value: V): F[Unit] = for {
          now <- Clock[F].realTime(MILLISECONDS)
          _ <- ref.update(_.updated(key, CacheEntry(value, now + ttl.toMillis)))
        } yield ()

        override def cleanup: F[Unit] = for {
          _ <- LoggerService.info[F](logger, s"Running cache invalidation")
          now <- Clock[F].realTime(MILLISECONDS)
          oldSize <- ref.get.map(_.size)
          _ <- ref.update(_.filter(_._2.expiresAt > now))
          newSize <- ref.get.map(_.size)
          _ <- LoggerService.info[F](logger, s"Cache cleanup: removed ${oldSize - newSize} expired entries")
        } yield ()
      }
      background = (cache.cleanup >> Timer[F].sleep(cleanupInterval)).foreverM.void
      _ <- Concurrent[F].start(background)
    } yield cache
  }
}
