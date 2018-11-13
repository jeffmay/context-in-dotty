package future.play.json

trait Writes[-A] {
  def write(value: A): JsValue
}

object Writes {

  inline def of[A](implicit writer: Writes[A]): Writes[A] = writer

  implicit val writesBoolean: Writes[Boolean] = JsValue.JsBoolean(_)
  implicit val writesDouble: Writes[Double] = value => JsValue.JsNumber(BigDecimal(value))
  implicit val writesInt: Writes[Int] = value => JsValue.JsNumber(BigDecimal(value))
  implicit val writesLong: Writes[Long] = value => JsValue.JsNumber(BigDecimal(value))
  implicit val writesString: Writes[String] = JsValue.JsString(_)
  implicit def writesIterable[A: Writes]: Writes[Iterable[A]] = { values =>
    JsValue.JsArray(values.map(Json.toJson[A](_)).toSeq)
  }
  implicit def writesMap[K: WritesKey, V: Writes]: Writes[Map[K, V]] = { values =>
    JsValue.JsObject(values.map {
      (k, v) => (WritesKey.of[K].write(k), Writes.of[V].write(v))
    })
  }
}
