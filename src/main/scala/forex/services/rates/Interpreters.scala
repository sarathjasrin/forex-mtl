package forex.services.rates

import cats.effect.Sync
import forex.repository.OneFrameRepository
import forex.services.rates.interpreters._

object Interpreters {
  def live[F[_] : Sync](repo: OneFrameRepository[F]): Algebra[F] = new OneFrameLive[F](repo)
}
