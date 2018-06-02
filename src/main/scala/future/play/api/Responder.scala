package future.play.api

import future.concurrent.ExecuteOnCallingThread

import scala.annotation.implicitNotFound
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@implicitNotFound(
  "Cannot find an implicit Responder[${V}] to convert the return value into a valid Response.\n\n" +
  "Response and Future[Response] have standard implementations by default, but all other types require you " +
  "to write a custom Responder.\n\n" +
  "Either explicitly convert the ${V} into one of the types with standard implimentations " +
  "or define a custom responder for this type."
)
trait Responder[V] {
  def responseFor(value: V): Future[Response]
}

object Responder extends LowPrioResponder {

  /**
    * Implicitly converts to a [[Responder]] if the given [[RCtx]] is implicit in scope.
    */
  trait WithCtx[V, RCtx] {
    def responseFor(value: V)(implicit ctx: RCtx): Future[Response]
  }

  def async[V](block: V => Future[Response]): Responder[V] = {
    value => block(value)
  }

  def sync[V](block: V => Response): Responder[V] = {
    value => Future.successful(block(value))
  }

  inline def withContext[RCtx]: WithCtxBuilder[RCtx] = new WithCtxBuilder[RCtx]

  final class WithCtxBuilder[RCtx] {
    inline def async[V](block: implicit RCtx => V => Future[Response]): Responder.WithCtx[V, RCtx] = {
      new Responder.WithCtx[V, RCtx] {
        override final def responseFor(value: V)(implicit ctx: RCtx): Future[Response] = block(ctx)(value)
      }
    }

    inline def sync[V](block: implicit RCtx => V => Response): Responder.WithCtx[V, RCtx] = {
      new Responder.WithCtx[V, RCtx] {
        override final def responseFor(value: V)(implicit ctx: RCtx): Future[Response] = {
          Future.fromTry(Try(block(ctx)(value)))
        }
      }
    }
  }

  def responseFor[V](value: V)(implicit responder: Responder[V]): Future[Response] = {
    responder.responseFor(value)
  }
}

sealed trait LowPrioResponder {

  implicit val responseResponder: Responder[Response] = Responder.sync(identity)

  implicit def respondWithContext[V, RCtx](implicit responder: Responder.WithCtx[V, RCtx], ctx: RCtx): Responder[V] = {
    Responder.async(responder.responseFor(_))
  }

  implicit def futureResponder[V](implicit 
    responder: Responder[V],
    ec: ExecutionContext = ExecuteOnCallingThread
  ): Responder[Future[V]] = {
    Responder.async(_.flatMap(responder.responseFor(_)))
  }
}
