package future.concurrent

import scala.concurrent.ExecutionContext

trait ImplicitExecutionContext(ec: ExecutionContext) {
  implicit protected def executionContext: ExecutionContext = ec
}

object ImplicitExecutionContext {
  type To[T] = implicit ExecutionContext => T
}
