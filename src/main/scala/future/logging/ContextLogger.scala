package future.logging

import scala.reflect.ClassTag

abstract class ContextLogger[Ctx](val name: String) {

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
    s"[${level}] ${name}: $msg [${formatMDC(extractMDC(ctx))}]"
  }
}

object LoggerName {
  def forClass(cls: Class[_]): String = {
    val b = new StringBuilder
    val parts = cls.getPackage.getName.split('.').map(p => p.charAt(0))
    for (c <- parts) {
      b.append(c)
      b.append('.')
    }
    b.append(cls.getSimpleName)
    b.toString
  }
}
