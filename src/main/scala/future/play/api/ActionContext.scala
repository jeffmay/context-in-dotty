package future.play.api

import scala.annotation.implicitNotFound
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object ActionContext {

  def extractOpt[Ctx](implicit request: Request, extractor: MaybeFromRequest[Ctx]): Option[Ctx] = {
    extractor.extractOpt(request)
  }

  def extractOrRespond[Ctx](implicit request: Request, extractor: FromRequest[Ctx]): Either[Response, Ctx] = {
    extractor.extractOrRespond(request)
  }

  def fromRequest[Ctx](parse: implicit Request => Either[Response, Ctx]): FromRequest[Ctx] = {
    implicit request => parse
  }

  def fromRequestAsync[Ctx](parse: implicit Request => Future[Either[Response, Ctx]]): FromRequestAsync[Ctx] = {
    implicit request => parse
  }

  def maybeFromRequest[Ctx](parse: ImplicitFunction1[Request, Option[Ctx]]): MaybeFromRequest[Ctx] = {
    implicit request => parse
  }

  def maybeFromRequestAsync[Ctx](parse: implicit Request => Future[Option[Ctx]]): MaybeFromRequestAsync[Ctx] = {
    implicit request => parse
  }

  def missingFromRequest[Ctx](respond: implicit Request => Response): MissingFromRequest[Ctx] = {
    implicit request => respond
  }

  def maybeFromRequestOrElse[Ctx](respond: implicit Request => Response)
    (implicit extractor: MaybeFromRequest[Ctx]): FromRequest[Ctx] = {
    request => extractor.extractOpt(request).toRight(respond(request))
  }

  def maybeFromRequestAsyncOrElse[Ctx](respond: implicit Request => Response)
    (implicit extractor: MaybeFromRequestAsync[Ctx], ec: ExecutionContext): FromRequestAsync[Ctx] = {
    request => extractor.extractOptAsync(request).map(_.toRight(respond(request)))
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

  trait MaybeFromRequestAsync[Ctx] {
    def extractOptAsync(request: Request): Future[Option[Ctx]]
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
    "MaybeFromRequest[${Ctx}] and MissingFromRequest[${Ctx}] in scope.")
  trait FromRequest[Ctx] extends FromRequestAsync[Ctx] {

    def extractOrRespond(request: Request): Either[Response, Ctx]

    override def extractOrRespondAsync(request: Request): Future[Either[Response, Ctx]] = {
      Future.fromTry(Try(extractOrRespond(request)))
    }
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

  /**
    * TODO: Better message
    * A simple function for extracting the context from the request or short-circuiting with
    * and early response.
    *
    * @tparam Ctx the type of context to extract from the request.
    */
  @implicitNotFound("Cannot find implicit FromRequestAsync[${Ctx}] in scope. " +
    "This can be automatically built if there is both an implicit " +
    "MaybeFromRequestAsync[${Ctx}] and MissingFromRequest[${Ctx}] in scope.")
  trait FromRequestAsync[Ctx] {

    def extractOrRespondAsync(request: Request): Future[Either[Response, Ctx]]
  }
}
