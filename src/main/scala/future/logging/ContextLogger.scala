package future.logging

abstract class ContextLogger[Ctx] {

  protected def formatLog(level: String, msg: String)(implicit ctx: Ctx): String

  def info(msg: String)(implicit ctx: Ctx): Unit = {
    println(formatLog("INFO", msg))
  }

}

trait DefaultLoggingFormat[Ctx] {
  self: ContextLogger[Ctx] =>

  protected def formatContext(ctx: Ctx): String

  override protected def formatLog(level: String, msg: String)(implicit ctx: Ctx): String = {
    s"${level}: $msg [${formatContext(ctx)}]"
  }
}
