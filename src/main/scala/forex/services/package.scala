package forex

package object services {
  type RatesServiceDummy[F[_]] = rates.DummyAlgebra[F]
  type RatesServiceLive[F[_]] = rates.Algebra[F]
  final val RatesServices = rates.Interpreters
  final val CacheServices = cache.Cache
}
