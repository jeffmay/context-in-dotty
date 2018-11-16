package future.logging

import scala.reflect.ClassTag

abstract class ContextLogger[C](val name: String) {
  type Ctx = C

  protected def formatContext(ctx: Ctx): String

  protected def formatLogEntry(level: String, msg: String)(implicit ctx: Ctx): String

  def info(msg: String)(implicit ctx: Ctx): Unit = {
    println(formatLogEntry("INFO", msg))
  }
}
