package future.play.json

object Json {

  inline def toJson[A: Writes](value: A): JsValue = Writes.of[A].write(value)

  inline def fromJson[A: Reads](json: JsValue): A = Reads.of[A].read(json)

  inline def serialize[A: Writes](value: A): String = Printer.noSpaces.print(toJson(value))

  inline def prettyPrint[A: Writes](value: A)(implicit printer: Printer): String = printer.print(toJson(value))
}
