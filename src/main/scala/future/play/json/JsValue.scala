package future.play.json

enum JsValue {
  case JsBoolean(value: Boolean)
  case JsNull
  case JsNumber(value: BigDecimal)
  case JsString(value: String)
  case JsArray(value: Seq[JsValue])
  case JsObject(value: Map[String, JsValue])
}
