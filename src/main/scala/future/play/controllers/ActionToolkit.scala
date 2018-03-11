package future.play.controllers

import future.play.controllers.ActionContext.FromRequest

import scala.annotation.implicitNotFound
import scala.concurrent.Future

/**
  * Inject this into your Controller and import from it to get implicits and a Play-like DSL.
  *
  * @tparam Ctx the type of context to extract for all actions
  */
class ActionToolkit[Ctx] {

  implicit def extractCtxOrDefaultResponse(implicit
    extractCtx: ActionContext.MaybeFromRequest[Ctx],
    failedHandler: ActionContext.MissingFromRequest[Ctx]
  ): ActionContext.FromRequest[Ctx] = {
    request => extractCtx.extractOpt(request).toRight(failedHandler.respondToMissingContext(request))
  }

  object Action extends ActionBuilder[Ctx]
}

object ActionToolkit extends ActionToolkit[Unit] {
  type Untyped = ActionToolkit[Unit]
}

/**
  * A dumb request model
  */
case class Request(path: String, body: String = "", headers: Map[String, String] = Map.empty)
object Request {
  // inline
  def here(implicit request: Request): Request = request
}

/**
  * A dumb response model
  */
case class Response(status: Int, body: String = "", headers: Map[String, String] = Map.empty)

object ActionContext {

  def extractOpt[Ctx](implicit request: Request, extractor: MaybeFromRequest[Ctx]): Option[Ctx] = {
    extractor.extractOpt(request)
  }

  def extractOrRespond[Ctx](implicit request: Request, extractor: FromRequest[Ctx]): Either[Response, Ctx] = {
    extractor.extractOrRespond(request)
  }

  def fromRequest[Ctx](parse: ImplicitFunction1[Request, Either[Response, Ctx]]): FromRequest[Ctx] = {
    implicit request => parse
  }

  def maybeFromRequest[Ctx](parse: ImplicitFunction1[Request, Option[Ctx]]): MaybeFromRequest[Ctx] = {
    implicit request => parse
  }

  def missingFromRequest[Ctx](respond: ImplicitFunction1[Request, Response]): MissingFromRequest[Ctx] = {
    implicit request => respond
  }

  def maybeFromRequestOrElse[Ctx](respond: ImplicitFunction1[Request, Response])
    (implicit extractor: MaybeFromRequest[Ctx]): FromRequest[Ctx] = {
    request => extractor.extractOpt(request).toRight(respond(request))
  }

  /**
    * A simple function for optionally extracting the context.
    *
    * Typically, this is combined with a
    *
    * @tparam Ctx the type of context to extract from the request.
    */
  trait MaybeFromRequest[Ctx] {
    def extractOpt(request: Request): Option[Ctx]
  }

  /**
    * A simple function that defines how to recover from a failure to extract a context from a
    * [[MaybeFromRequest]] instance.
    *
    * This combines with [[MaybeFromRequest]] to form a full [[FromRequest]].
    */
  trait MissingFromRequest[Ctx] {
    def respondToMissingContext(request: Request): Response
  }

  object MissingFromRequest {
    implicit def respondEmpty400[Ctx]: MissingFromRequest[Ctx] = {
      _ => Response(400)
    }
  }

  /**
    * A simple function for extracting the context from the request or short-circuiting with
    * and early response.
    *
    * @tparam Ctx the type of context to extract from the request.
    */
  @implicitNotFound("Cannot find implicit FromRequest[${Ctx}] in scope. " +
    "This can be automatically built if there is both an implicit " +
    "MaybeFromRequest[${Ctx}] and FromRequest.FailedHandler[${Ctx}] in scope.")
  trait FromRequest[Ctx] {

    def extractOrRespond(request: Request): Either[Response, Ctx]
  }

  object FromRequest {

    /**
      * A default for use by [[ActionBuilder.Untyped]]
      */
    implicit val unitFromRequest: FromRequest[Unit] = {
      _ => Right(())
    }

    implicit def optionFromRequest[Ctx](implicit extractor: MaybeFromRequest[Ctx]): FromRequest[Option[Ctx]] = {
      request => Right(extractor.extractOpt(request))
    }
  }
}

/**
  * A dumb definition of a request handler.
  */
trait Action {
  def handle(request: Request): Future[Response]
}

object Action {

  /**
    * A type definition for the kind of block that must be implemented for async actions.
    */
  type AsyncCtxBlock[Ctx] = ImplicitFunction2[Ctx, Request, Future[Response]]

  def apply(handle: ImplicitFunction1[Request, Future[Response]]): Action = {
    implicit request => handle
  }
}

/**
  * A sample of how Play's ActionBuilder could look if it was context-aware.
  */
class ActionBuilder[Ctx] {

  def withContext[NewCtx]: ActionBuilder[NewCtx] = new ActionBuilder[NewCtx]

  def async(block: Action.AsyncCtxBlock[Ctx])(implicit extractor: FromRequest[Ctx]): Action = {
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
