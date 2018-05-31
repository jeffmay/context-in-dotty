package future.concurrent

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try

final object SynchronousExecution {

  def executeSync[T](block: implicit ExecuteOnCallingThread.type => T): T = {
    block(ExecuteOnCallingThread)
  }

  implicit class FutureResult[T](val future: Future[T]) extends AnyVal {
    inline def ! : T = getOrThrow
    inline def getOrThrow: T = Await.result(future, Duration.Inf)
    inline def toTry: Try[T] = Try(getOrThrow)
  }
}

final object ExecuteOnCallingThread extends ExecutionContext {
  override def execute(runnable: Runnable): Unit = runnable.run()
  override def reportFailure(cause: Throwable): Unit = throw cause
}
