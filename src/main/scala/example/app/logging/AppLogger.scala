package example.app.logging

import future.logging.{ContextLogger, LoggerName, MDCLogFormat}
import example.app.context.RequestCtx
import scala.reflect.ClassTag

class AppLogger(name: String) extends ContextLogger[RequestCtx](name) 
  with MDCLogFormat[RequestCtx] {
  def this(cls: Class[_]) = this(LoggerName.forClass(cls))

  override protected def extractMDC(ctx: RequestCtx): Map[String, String] = Map(
    "path" -> ctx.request.path,
    "userId" -> ctx.maybeUserId.map(_.toString).getOrElse("unauthenticated"),
    "correlationId" -> ctx.correlationId.value
  )
}
