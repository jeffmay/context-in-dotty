package future.play.api

import scala.annotation.implicitNotFound
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * A simple definition of a request handler.
  */
trait Action {
  // Note: This could be more fancy than returning a Future and Responder could return this as well
  def handle(request: Request): Future[Response]
}

object Action {

  // TODO: Define some kind of converter from Request => PlayRequestContext
  def apply(block: implicit Request => Future[Response]): Action = {
    implicit request => block
  }
}
