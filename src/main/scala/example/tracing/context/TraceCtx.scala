package example.tracing.context

import example.tracing.CorrelationId

import future.context.ContextCompanion

import scala.annotation.implicitNotFound

@implicitNotFound(
  "Missing implicit TraceCtx here. This model is extracted from the request by the controller " +
  "and passed along implicitly. Please include or define an implicit context in scope."
)
trait TraceCtx {
  def correlationId: CorrelationId
  def maybeUserId: Option[Int]
}
object TraceCtx extends ContextCompanion[TraceCtx]
