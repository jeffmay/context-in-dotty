package future.logging

trait ContextMapFormat[Ctx: LoggableAsMap] extends ContextLogger[Ctx] {

  override protected def formatContext(ctx: Ctx): String = {
    val fields = implicitly[LoggableAsMap[Ctx]].asMap(ctx)
    fields.map { case (k, v) => s"$k=$v" }.mkString(", ")
  }
}