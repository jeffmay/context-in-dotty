package future.logging

trait LoggableAsMap[Ctx] {
  def asMap(ctx: Ctx): Map[String, String]
}

object LoggableAsMap {
  inline def write[Ctx](ctx: Ctx)(implicit c: LoggableAsMap[Ctx]): Map[String, String] = {
    c.asMap(ctx)
  }
}