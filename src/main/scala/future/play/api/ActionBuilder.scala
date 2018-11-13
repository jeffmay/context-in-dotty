package future.play.api

import scala.concurrent.{ExecutionContext, Future}

import future.context.ContextCompanion
import future.concurrent.ImplicitExecutionContext

object ActionBuilder {

  def apply(executionContext: ExecutionContext): ActionBuilder[PlayRequestContext] = {
    implicit def ec: ExecutionContext = executionContext
    new ActionBuilder[PlayRequestContext]
  }
}

class ActionBuilder[BaseCtx](implicit 
  refiner: ContextRefiner[PlayRequestContext, BaseCtx],
  handlerContext: ExecutionContext
) extends ImplicitExecutionContext(handlerContext) {
  outer =>

  def handle[R: Responder](block: implicit BaseCtx => R): Action = { request =>
    implicit val playCtx: PlayRequestContext = new PlayRequestContext(request) {}
    refiner.refineOrRespond().flatMap {
      case Right(ctx) =>
        implicit def c: BaseCtx = ctx
        Responder.responseFor(block)
      case Left(rsp) =>
        Future.successful(rsp)
    }
  }

  def refined[C](implicit refiner: ContextRefiner[BaseCtx, C]): ActionBuilder[C] = {
    new ActionBuilder[C]()({ implicit playCtx =>
      outer.refiner(playCtx).flatMap {
        case Right(base) =>
          refiner(base)
        case Left(response) =>
          Future.successful(Left(response))
      }
    }, handlerContext)
  }
}
