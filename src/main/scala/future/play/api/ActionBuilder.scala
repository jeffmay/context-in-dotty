package future.play.api

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

import future.concurrent.ExecuteOnCallingThread
import future.play.api.ActionContext.{FromRequest, FromRequestAsync, MaybeFromRequestAsync, MissingFromRequest}

// TODO: Simplify to MaybeFromRequestAsync?

/**
 * MaybeFromRequestAsync - 
 * 
 */

sealed abstract class AB[Ctx] {

  type CtxFn[R]

  protected def executeWithContext[R](block: CtxFn[R], ctx: Ctx): R

  // implicit protected def executionContext: ExecutionContext = ctx.executionContext
  // implicit protected def fromRequestAsync: FromRequestAsync[Ctx] = ctx.fromRequestAsync

  def withContext[A]: AB1[A] = new AB1[A]
  def withContext[A, B]: AB2[A, B] = new AB2[A, B]
  def withContext[A, B, C]: AB3[A, B, C] = new AB3[A, B, C]
  def withContext[A, B, C, D]: AB4[A, B, C, D] = new AB4[A, B, C, D]
  def withContext[A, B, C, D, E]: AB5[A, B, C, D, E] = new AB5[A, B, C, D, E]

  def handle[R: Responder](block: CtxFn[R])(implicit
    fromReq: FromRequestAsync[Ctx],
    ec: ExecutionContext
  ): Action = {
    Action { implicit request =>
      Result(fromReq.extractOrRespondAsync(request).flatMap {
        case Right(ctx) =>
          Responder.responseFor(executeWithContext(block, ctx))
        case Left(rsp) =>
          Future.successful(rsp)
      })
    }
  }

  def handleOr[E: Responder, R: Responder](
    fallback: E
  )(
    block: CtxFn[R]
  )(implicit 
    maybeFromReq: MaybeFromRequestAsync[Ctx],
    ec: ExecutionContext
  ): Action = {
    Action { implicit request =>
      Result(maybeFromReq.extractOptAsync(request) flatMap {
        case Some(ctx) =>
          Responder.responseFor(executeWithContext(block, ctx))
        case _ =>
          Responder.responseFor(fallback)
      })
    }
  }

}

object AB extends AB[Request] {
  type CtxFn[R] = implicit Request => R
  override protected def executeWithContext[R](block: CtxFn[R], ctx: Request): R = block(ctx)

  // def json: AB1[JsonRequest]

  // case class ContextWrapper[Ctx](request: Request, context: Ctx)
  // object ContextWrapper {
  //   implicit def requestFromContext
  // }
}

class AB1[A] extends AB[(Request, A)] {
  override type CtxFn[R] = implicit (Request, A) => R

  override protected def executeWithContext[R](block: CtxFn[R], ctx: (Request, A)): R = {
    block(ctx._1, ctx._2)
  }
}

class AB2[A, B] extends AB[(Request, A, B)] {
  override type CtxFn[R] = implicit (Request, A, B) => R

  override protected def executeWithContext[R](block: CtxFn[R], ctx: (Request, A, B)): R = {
    block(ctx._1, ctx._2, ctx._3)
  }
}

class AB3[A, B, C] extends AB[(Request, A, B, C)] {
  override type CtxFn[R] = implicit (Request, A, B, C) => R

  override protected def executeWithContext[R](block: CtxFn[R], ctx: (Request, A, B, C)): R = {
    block(ctx._1, ctx._2, ctx._3, ctx._4)
  }
}

class AB4[A, B, C, D] extends AB[(Request, A, B, C, D)] {
  override type CtxFn[R] = implicit (Request, A, B, C, D) => R

  override protected def executeWithContext[R](block: CtxFn[R], ctx: (Request, A, B, C, D)): R = {
    block(ctx._1, ctx._2, ctx._3, ctx._4, ctx._5)
  }
}

class AB5[A, B, C, D, E] extends AB[(Request, A, B, C, D, E)] {
  override type CtxFn[R] = implicit (Request, A, B, C, D, E) => R

  override protected def executeWithContext[R](block: CtxFn[R], ctx: (Request, A, B, C, D, E)): R = {
    block(ctx._1, ctx._2, ctx._3, ctx._4, ctx._5, ctx._6)
  }
}

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
