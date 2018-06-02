package future.play.json

trait ReadsKey[K] {
  def readKey(key: String): K
}

object ReadsKey {

  inline def of[A](implicit reader: ReadsKey[A]): ReadsKey[A] = reader

  implicit val readsKeyString: ReadsKey[String] = identity
  implicit val readsKeyInt: ReadsKey[Int] = _.toInt
  implicit val readsKeyLong: ReadsKey[Int] = _.toInt
}
