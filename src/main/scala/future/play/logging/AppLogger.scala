package future.play.logging

import future.logging.{ContextLogger, MDCLogFormat}
import future.play.models.AppCtx

class AppLogger extends ContextLogger[AppCtx] with MDCLogFormat[AppCtx] {
  override protected def extractMDC(ctx: AppCtx): Map[String, String] = Map(
    "path" -> ctx.request.path,
    "correlationId" -> ctx.correlationId.value
  )
}
