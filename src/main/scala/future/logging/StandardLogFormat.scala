package future.logging

trait StandardLogFormat {
  self: ContextLogger[_] =>

  override protected def formatLogEntry(level: String, msg: String)(implicit ctx: Ctx): String = {
    s"[${level}] ${name}: $msg [${formatContext(ctx)}]"
  }
}
