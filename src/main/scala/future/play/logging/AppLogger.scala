package future.play.logging

import future.logging.{ContextLogger, DefaultLoggingFormat}
import future.play.models.AppCtx

class AppLogger extends ContextLogger[AppCtx] with DefaultLoggingFormat[AppCtx] {
  override protected def formatContext(ctx: AppCtx): String = {
    s"path=${ctx.request.path}, correlationId=${ctx.correlationId.value}"
  }
}
