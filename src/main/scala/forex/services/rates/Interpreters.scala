package forex.services.rates

import cats.Applicative
import cats.effect.Sync
import forex.repository.OneFrameRepository
import forex.services.rates.interpreters._

object Interpreters {
  def dummy[F[_] : Applicative]: DummyAlgebra[F] = new OneFrameDummy[F]()

  def live[F[_] : Sync](repo: OneFrameRepository[F]): Algebra[F] = new OneFrameLive[F](repo)
}
