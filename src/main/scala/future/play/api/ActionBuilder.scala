package future.play.api

import scala.concurrent.{ExecutionContext, Future}

import future.context.ContextCompanion
import future.concurrent.ImplicitExecutionContext

object ActionBuilder {

  inline def here(implicit ec: ExecutionContext): ActionBuilder[Request] = {
    new ActionBuilder[Request]
  }

  val global: ActionBuilder[Request] = apply(ExecutionContext.global)

  def apply(executionContext: ExecutionContext): ActionBuilder[Request] = {
    implicit def ec: ExecutionContext = executionContext
    new ActionBuilder[Request]
  }
}

class ActionBuilder[BaseCtx](implicit 
  refiner: ContextRefiner[Request, BaseCtx],
  handlerContext: ExecutionContext
) extends ImplicitExecutionContext(handlerContext) {

  def handle[R: Responder](block: implicit BaseCtx => R): Action = Action {
    refiner.refineOrRespond().flatMap {
      case Right(ctx) =>
        implicit def c: BaseCtx = ctx
        Responder.responseFor(block)
      case Left(rsp) =>
        Future.successful(rsp)
    }
  }

  def refined[C](implicit nextRefiner: ContextRefiner[BaseCtx, C]): ActionBuilder[C] = {
    new ActionBuilder[C]()({ implicit playCtx =>
      refiner(playCtx).flatMap {
        case Right(base) =>
          nextRefiner(base)
        case Left(response) =>
          Future.successful(Left(response))
      }
    }, handlerContext)
  }
}
