package future.play.api

import scala.annotation.implicitNotFound
import scala.concurrent.Future

/**
  * A dumb definition of a request handler.
  */
trait Action {
  // Note: This could be more fancy than returning a Future and Responder could return this as well
  def handle(request: Request): Future[Response]
}

object Action {

  def apply[T: Responder](block: Request.To[T]): Action = {
    implicit request => Responder.responseFor(block)
  }
}

@implicitNotFound(
  "Cannot find an implicit Responder[${T}] to convert the return value into a valid Response.\n\n" +
  "Response and Future[Response] have standard implementations by default, but all other types require you " +
  "to write a custom Responder.\n\n" +
  "Either explicitly convert the ${T} into one of the types with standard implimentations " +
  "or define a custom responder for this type."
)
trait Responder[T] {
  def responseFor(value: T): Request.To[Future[Response]]
}

object Responder {

  def responseFor[T](value: T)(implicit responder: Responder[T]): Request.To[Future[Response]] = {
    responder.responseFor(value)
  }

  implicit val responseResponder: Responder[Response] = {
    response => Future.successful(response)
  }

  implicit val futureResponseResponder: Responder[Future[Response]] = {
    future => future
  }
}
