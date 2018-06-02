package future.play.json

trait Printer {

  def print(json: JsValue): String // You could use a better interface for streaming
}

object Printer {

  private final class Impl() extends Printer {
    override def print(json: JsValue): String = {
      val b = new StringBuffer

      def print0(json: JsValue): Unit = json match {
        case JsValue.JsNull =>
          b.append("null")
        case JsValue.JsBoolean(v) =>
          b.append(v)
        case JsValue.JsNumber(v) =>
          b.append(v)
        case JsValue.JsString(v) =>
          b.append('"')
          b.append(v)
          b.append('"')
        case JsValue.JsArray(vs) =>
          printArray(vs)
        case JsValue.JsObject(vs) =>
          printObject(vs.toIterable)
      }

      def printArray(values: Seq[JsValue]): Unit = {
        b.append('[')
        if (values.nonEmpty) {
          for (json <- values) {
            print0(json)
            b.append(',')
          }
          b.setLength(b.length - 1)
        }
        b.append(']')
      }

      def printObject(values: Iterable[(String, JsValue)]): Unit = {
        b.append('{')
        if (values.nonEmpty) {
          for ((k, json) <- values) {
            b.append('"')
            b.append(k)
            b.append('"')
            b.append(':')
            print0(json)
            b.append(',')
          }
          b.setLength(b.length - 1)
        }
        b.append('}')
      }

      print0(json)
      b.toString
    }
  }

  val noSpaces: Printer = new Impl()
}