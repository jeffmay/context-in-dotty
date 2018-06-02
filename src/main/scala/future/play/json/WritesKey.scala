package future.play.json

trait WritesKey[K] {
  def write(key: K): String
}

object WritesKey {

  inline def of[K](implicit writer: WritesKey[K]): WritesKey[K] = writer

  implicit val writesKeyString: WritesKey[String] = identity
  implicit val writesKeyInt: WritesKey[Int] = _.toString
  implicit val writesKeyLong: WritesKey[Long] = _.toString
}
