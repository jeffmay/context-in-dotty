package future.play.api

import scala.concurrent.Future

/**
  * A dumb definition of a request handler.
  */
trait Action {
  def handle(request: Request): Future[Response]
}

object Action {

  def apply(handle: implicit Request => Future[Response]): Action = {
    implicit request => handle
  }
}
