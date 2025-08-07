package forex.services.cache

trait CacheAlgebra[F[_], K, V] {
  def get(key: K): F[Option[V]]

  def getMany(keys: List[K]): F[List[Option[V]]]

  def put(key: K, value: V): F[Unit]

  def cleanup: F[Unit]
}
