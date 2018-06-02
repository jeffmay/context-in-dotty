package future.play.api

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

import future.concurrent.ExecuteOnCallingThread
import future.play.api.ActionContext.{FromRequestAsync, MaybeFromRequestAsync, MissingFromRequest}

sealed abstract class AB[Ctx](implicit ctx: AB.Context[Ctx]) {

  implicit protected def executionContext: ExecutionContext = ctx.executionContext
  implicit protected def fromRequestAsync: FromRequestAsync[Ctx] = ctx.fromRequestAsync

  def withContext[NewCtx: FromRequestAsync]: AB1[NewCtx] = new AB1[NewCtx]

  def async[R: Responder](block: implicit Ctx => R): Action = {
    Action { implicit request =>
      ActionContext.extractOrRespondAsync[Ctx].flatMap {
        case Right(ctx) =>
          Responder.responseFor(block(ctx))
        case Left(rsp) =>
          Future.successful(rsp)
      }
    }
  }

  def asyncOr[E: Responder, B: Responder](
    earlyResponse: E
  )(
    block: implicit (Ctx, Request) => B
  ): Action = {
    Action { implicit request =>
      fromRequestAsync.extractOptAsync(request) flatMap {
        case Some(ctx) =>
          Responder.responseFor(block(ctx, request))
        case None =>
          Responder.responseFor(earlyResponse)
      }
    }
  }

}

object AB {

  def apply[A: FromRequestAsync](implicit ec: ExecutionContext): AB1[A] = new AB1[A](Context.here)

  case class Context[Ctx](executionContext: ExecutionContext, fromRequestAsync: FromRequestAsync[Ctx])
  object Context {
    implicit def here[Ctx](implicit ec: ExecutionContext, fromReq: FromRequestAsync[Ctx]): Context[Ctx] = {
      Context(ec, fromReq)
    }
  }
}

class AB1[A](implicit ctx: AB.Context[(Request, A)]) extends AB[(Request, A)] {

  def and[B: MaybeFromRequestAsync]: AB2[A, B] = new AB2[A, B]
}

class AB2[A, B](implicit ctx: AB.Context[(Request, A, B)]) extends AB[(Request, A, B)]

// /**
//   * A sample of how Play's ActionBuilder could look if it was context-aware.
//   */
// class ActionBuilder[Ctx] {

//   def withoutContext: ActionBuilder[Unit] = new ActionBuilder[Unit]

//   def withContext[NewCtx]: ActionBuilder[NewCtx] = new ActionBuilder[NewCtx]

//   def async[B: Responder](block: implicit (Ctx, Request) => B)(implicit extractor: FromRequestAsync[Ctx], ec: ExecutionContext): Action = {
//     Action { implicit request =>
//       extractor.extractOrRespondAsync(request) flatMap {
//         case Right(ctx) =>
//           Responder.responseFor(block(ctx, request))
//         case Left(rsp) =>
//           Future.successful(rsp)
//       }
//     }
//   }

//   def asyncOr[E: Responder, B: Responder](
//     earlyResponse: E
//   )(
//     block: implicit (Ctx, Request) => B
//   )(implicit 
//     extractor: MaybeFromRequestAsync[Ctx], 
//     ec: ExecutionContext
//   ): Action = {
//     Action { implicit request =>
//       extractor.extractOptAsync(request) flatMap {
//         case Some(ctx) =>
//           Responder.responseFor(block(ctx, request))
//         case None =>
//           Responder.responseFor(earlyResponse)
//       }
//     }
//   }
// }
