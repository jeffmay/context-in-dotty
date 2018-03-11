package future.concurrent

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

sealed trait SynchronousExecution extends ExecutionContext
object SynchronousExecution {

  def executeSingleThreaded[T](block: implicit SynchronousExecution => T): T = {
    block(ExecuteSingleThreaded)
  }

  import scala.concurrent.duration._
  implicit class FutureResult[T](val future: Future[T]) extends AnyVal {
    def getOrThrow(implicit sync: SynchronousExecution): T = Await.result(future, Duration.Inf)
    def toTry(implicit sync: SynchronousExecution): Try[T] = Try(getOrThrow)
  }
}

object ExecuteSingleThreaded extends SynchronousExecution {
  override def execute(runnable: Runnable): Unit = runnable.run()
  override def reportFailure(cause: Throwable): Unit = ()
}
