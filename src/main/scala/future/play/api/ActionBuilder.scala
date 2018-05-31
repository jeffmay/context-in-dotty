package future.play.api

import scala.concurrent.Future

import future.play.api.ActionContext.FromRequest

/**
  * A sample of how Play's ActionBuilder could look if it was context-aware.
  */
class ActionBuilder[Ctx] {

  def withContext[NewCtx]: ActionBuilder[NewCtx] = new ActionBuilder[NewCtx]

  // TODO: Add Responder typeclass for better error message

  def async(block: implicit (Ctx, Request) => Future[Response])(implicit extractor: FromRequest[Ctx]): Action = {
    Action { implicit request =>
      extractor.extractOrRespond(request) match {
        case Right(ctx) =>
          block(ctx, request)
        case Left(rsp) =>
          Future.successful(rsp)
      }
    }
  }
}
