package future.play.api

import scala.concurrent.{ExecutionContext, Future}

import future.play.api.ActionContext.{FromRequestAsync, MaybeFromRequestAsync}

/**
  * A sample of how Play's ActionBuilder could look if it was context-aware.
  */
class ActionBuilder[Ctx] {

  def withoutContext: ActionBuilder[Unit] = new ActionBuilder[Unit]

  def withContext[NewCtx]: ActionBuilder[NewCtx] = new ActionBuilder[NewCtx]

  def async[B: Responder](block: implicit (Ctx, Request) => B)(implicit extractor: FromRequestAsync[Ctx], ec: ExecutionContext): Action = {
    Action { implicit request =>
      extractor.extractOrRespondAsync(request) flatMap {
        case Right(ctx) =>
          Responder.responseFor(block(ctx, request))
        case Left(rsp) =>
          Future.successful(rsp)
      }
    }
  }

  def asyncOr[E: Responder, B: Responder](
    earlyResponse: E
  )(
    block: implicit (Ctx, Request) => B
  )(implicit 
    extractor: MaybeFromRequestAsync[Ctx], 
    ec: ExecutionContext
  ): Action = {
    Action { implicit request =>
      extractor.extractOptAsync(request) flatMap {
        case Some(ctx) =>
          Responder.responseFor(block(ctx, request))
        case None =>
          Responder.responseFor(earlyResponse)
      }
    }
  }
}
