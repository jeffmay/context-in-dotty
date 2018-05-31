package future.logging

abstract class ContextLogger[Ctx] {

  protected def formatLogEntry(level: String, msg: String)(implicit ctx: Ctx): String

  def info(msg: String)(implicit ctx: Ctx): Unit = {
    println(formatLogEntry("INFO", msg))
  }

}

trait MDCLogFormat[Ctx] {
  self: ContextLogger[Ctx] =>

  protected def extractMDC(ctx: Ctx): Map[String, String]

  protected def formatMDC(fields: Map[String, String]): String = fields.map { case (k, v) => s"$k=$v" }.mkString(", ")

  override protected def formatLogEntry(level: String, msg: String)(implicit ctx: Ctx): String = {
    s"${level}: $msg [${formatMDC(extractMDC(ctx))}]"
  }
}
