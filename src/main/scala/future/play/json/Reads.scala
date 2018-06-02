package future.play.json

trait Reads[A] {
  def read(json: JsValue): A // TODO: Capture / validate error type
}

object Reads {

  inline def of[A](implicit reader: Reads[A]): Reads[A] = reader

  implicit val readsBoolean: Reads[Boolean] = {
    case JsValue.JsBoolean(value) => value
    case _ => ???
  }

  implicit val readsDouble: Reads[Double] = {
    case JsValue.JsNumber(value) => value.toDouble
    case _ => ???
  }

  implicit val readsInt: Reads[Int] = {
    case JsValue.JsNumber(value) => value.toIntExact
    case _ => ???
  }

  implicit val readsLong: Reads[Long] = {
    case JsValue.JsNumber(value) => value.toLongExact
    case _ => ???
  }

  implicit val readsString: Reads[String] = {
    case JsValue.JsString(value) => value
    case _ => ???
  }

  implicit def readsIterable[A: Reads]: Reads[Iterable[A]] = {
    case JsValue.JsArray(values) => values.map(Json.fromJson[A](_))
    case _ => ???
  }

  implicit def readsMap[K: ReadsKey, V: Reads]: Reads[Map[K, V]] = {
    case JsValue.JsObject(values) => values.map {
      case (k, v) => (ReadsKey.of[K].readKey(k), Reads.of[V].read(v))
    }
    case _ => ???
  }
}
