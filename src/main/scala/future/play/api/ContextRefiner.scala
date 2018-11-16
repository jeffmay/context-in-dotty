package future.play.api

import future.concurrent.ExecuteOnCallingThread

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait ContextRefiner[-BaseCtx, +RefinedCtx]
  extends (BaseCtx => Future[Either[Response, RefinedCtx]]) {
  inline def refineOrRespond()(implicit base: BaseCtx): Future[Either[Response, RefinedCtx]] = {
    apply(base)
  }
}

object ContextRefiner {

  def async[A, B](convert: implicit A => Future[B])(implicit
    ec: ExecutionContext = ExecuteOnCallingThread
  ): ContextRefiner[A, B] = { 
    implicit value => convert.map(Right(_))
  }

  def asyncOrRespond[A, B](convert: implicit A => Future[Either[Response, B]]): ContextRefiner[A, B] = {
    implicit value => convert
  }

  def sync[A, B](convert: implicit A => B): ContextRefiner[A, B] = {
    implicit value => Future.fromTry(Try(Right(convert)))
  }

  def syncOrRespond[A, B](convert: implicit A => Either[Response, B]): ContextRefiner[A, B] = {
    implicit value => Future.fromTry(Try(convert))
  }

  type FromPlayAction[RefinedCtx] = ContextRefiner[PlayRequestContext, RefinedCtx] 

  implicit def refineIdentity[Ctx]: ContextRefiner[Ctx, Ctx] = { implicit ctx =>
    Future.successful(Right(ctx))
  }
}
