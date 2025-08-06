package forex.services.logger

import cats.effect.Sync
import org.slf4j.{Logger, LoggerFactory}


object LoggerService {
  def logger(name: String): Logger = LoggerFactory.getLogger(name)

  def loggerFor[T](implicit classTag: scala.reflect.ClassTag[T]): Logger = {
    LoggerFactory.getLogger(classTag.runtimeClass)
  }

  def info[F[_] : Sync](logger: Logger, message: String): F[Unit] =
    Sync[F].delay(logger.info(message))

  def warn[F[_] : Sync](logger: Logger, message: String): F[Unit] =
    Sync[F].delay(logger.warn(message))

  def error[F[_] : Sync](logger: Logger, message: String): F[Unit] = {
    println(s"Test: $message")
    Sync[F].delay(logger.error(message))
  }

  def error[F[_] : Sync](logger: Logger, message: String, cause: Throwable): F[Unit] =
    Sync[F].delay(logger.error(message, cause))
}
