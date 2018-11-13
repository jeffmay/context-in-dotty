package example.database.context

import example.database.Session
import example.tracing.context.TraceCtx

import future.context.ContextCompanion

import scala.annotation.implicitNotFound
import scala.concurrent.Future

/**
  * A database context that could live in another package or library
  */
@implicitNotFound(
  "Missing implicit DBCtx here. This model is extracted from the request by the controller " +
  "and passed along implicitly. Please include or define an implicit context in scope."
)
trait DBCtx extends TraceCtx {  
  lazy val session: Session = new Session
}
object DBCtx extends ContextCompanion[DBCtx] {
  type Async[V] = To[Future[V]]
}
