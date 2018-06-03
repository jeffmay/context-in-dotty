package future.play.api

import java.util.concurrent.atomic.AtomicInteger

import scala.annotation.implicitNotFound
import scala.concurrent.{ExecutionContext, Future}
import scala.collection
import scala.util.{Success, Try}

object ActionContext {

  def extractOpt[Ctx](implicit request: Request, extractor: MaybeFromRequest[Ctx]): Option[Ctx] = {
    extractor.extractOpt(request)
  }

  def extractOptAsync[Ctx](implicit request: Request, extractor: MaybeFromRequestAsync[Ctx]): Future[Option[Ctx]] = {
    extractor.extractOptAsync(request)
  }

  def extractOrRespond[Ctx](implicit request: Request, extractor: FromRequest[Ctx]): Either[Response, Ctx] = {
    extractor.extractOrRespond(request)
  }

  def extractOrRespondAsync[Ctx](implicit request: Request, extractor: FromRequestAsync[Ctx]): Future[Either[Response, Ctx]] = {
    extractor.extractOrRespondAsync(request)
  }

  def fromRequest[Ctx](parse: implicit Request => Either[Response, Ctx]): FromRequest[Ctx] = {
    implicit request => parse
  }

  def maybeFromRequestOrElse[Ctx](orElse: implicit Request => Response)
    (implicit extractor: MaybeFromRequest[Ctx]): FromRequest[Ctx] = {
    implicit request => extractor.extractOpt(request) match {
      case Some(ctx) => Right(ctx)
      case _ => Left(orElse)
    }
  }

  def fromRequestAsync[Ctx](parse: implicit Request => Future[Either[Response, Ctx]]): FromRequestAsync[Ctx] = {
    implicit request => parse
  }

  def maybeFromRequestAsyncOrElse[Ctx](orElse: implicit Request => Response)
    (implicit extractor: MaybeFromRequestAsync[Ctx], ec: ExecutionContext): FromRequestAsync[Ctx] = {
    implicit request => extractor.extractOptAsync(request).map {
      case Some(ctx) => Right(ctx)
      case _ => Left(orElse)
    }
  }

  def maybeFromRequest[Ctx](parse: implicit Request => Option[Ctx]): MaybeFromRequest[Ctx] = {
    implicit request => parse
  }

  def maybeFromRequestAsync[Ctx](parse: implicit Request => Future[Option[Ctx]]): MaybeFromRequestAsync[Ctx] = {
    implicit request => parse
  }

  def missingFromRequest[Ctx](respond: implicit Request => Response): MissingFromRequest[Ctx] = {
    implicit request => respond
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

  object MaybeFromRequest {

    implicit def requestMaybeFromRequest: MaybeFromRequest[Request] = {
      request => Some(request)
    }
  }

  trait MaybeFromRequestAsync[Ctx] {
    def extractOptAsync(request: Request): Future[Option[Ctx]]
  }

  object MaybeFromRequestAsync {

    implicit def maybeFromRequestSyncAsAsync[Ctx: MaybeFromRequest]: MaybeFromRequestAsync[Ctx] = {
      new MaybeFromRequestAsync[Ctx] {
        override def extractOptAsync(request: Request): Future[Option[Ctx]] = {
          implicit def req: Request = request
          Future.fromTry(Try(extractOpt[Ctx]))
        }
      }
    }

    // Folds the thunks into the first early response or an array of the accumulated results
    private def squash[T](
      thunks: Seq[() => Future[Option[Any]]],
    )(
      gather: PartialFunction[Array[Any], T],
    )(implicit ec: ExecutionContext): Future[Option[T]] = {
      val acc = new Array[Any](thunks.size)
      var squashed: Future[Option[Array[Any]]] = Future.successful(Some(acc))
      for ((result, i) <- thunks.zipWithIndex) {
        squashed = result().map(_.map { x =>
          acc(i) = x
          acc
        })
      }
      squashed.map(_.map(gather))
    }

    implicit def maybeFromRequestAsync2[
      A: MaybeFromRequestAsync,
      B: MaybeFromRequestAsync,
    ](implicit ec: ExecutionContext): MaybeFromRequestAsync[(A, B)] = { implicit request =>
      squash(Seq(
        () => extractOptAsync[A],
        () => extractOptAsync[B],
      )) {
        case Array(a, b) => (a, b).asInstanceOf[(A, B)]
      }
    }

    implicit def fromRequest3[
      A: MaybeFromRequestAsync,
      B: MaybeFromRequestAsync,
      C: MaybeFromRequestAsync,
    ](implicit ec: ExecutionContext): MaybeFromRequestAsync[(A, B, C)] = { implicit request =>
      squash(Seq(
        () => extractOptAsync[A],
        () => extractOptAsync[B],
        () => extractOptAsync[C],
      )) {
        case Array(a, b, c) => (a, b, c).asInstanceOf[(A, B, C)]
      }
    }
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

  /**
    * A simple function for extracting the context from the request or short-circuiting with
    * and early response.
    *
    * @tparam Ctx the type of context to extract from the request.
    */
  @implicitNotFound("Cannot find implicit FromRequest[${Ctx}] in scope. " +
    "This can be automatically built if there is both an implicit " +
    "MaybeFromRequest[${Ctx}] and MissingFromRequest[${Ctx}] in scope.")
  trait FromRequest[Ctx] { // extends FromRequestAsync[Ctx] {

    def extractOrRespond(request: Request): Either[Response, Ctx]
  }

  object FromRequest {

    /**
      * A default for use by [[ActionBuilder.Untyped]]
      */
    implicit lazy val requestFromRequest: FromRequest[Request] = {
      request => Right(request)
    }

    implicit def extractCtxOrDefaultResponse[Ctx](implicit
      extractCtx: MaybeFromRequest[Ctx],
      failedHandler: MissingFromRequest[Ctx],
    ): FromRequest[Ctx] = new FromRequest[Ctx] {
      override def extractOrRespond(request: Request): Either[Response, Ctx] = {
        extractCtx.extractOpt(request).toRight(failedHandler.respondToMissingContext(request))
      }
    }

    implicit def optionFromRequest[Ctx](implicit extractor: MaybeFromRequest[Ctx]): FromRequest[Option[Ctx]] = {
      new FromRequest[Option[Ctx]] {
        override def extractOrRespond(request: Request): Either[Response, Option[Ctx]] = {
          Right(extractor.extractOpt(request))
        }
      }
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

  object FromRequestAsync {

    implicit def fromRequestSyncAsAsync[Ctx: FromRequest]: FromRequestAsync[Ctx] = new FromRequestAsync[Ctx] {
      override def extractOrRespondAsync(request: Request): Future[Either[Response, Ctx]] = {
        implicit def req: Request = request
        Future.fromTry(Try(extractOrRespond[Ctx]))
      }
    }

    implicit def extractCtxOrDefaultResponseAsync[Ctx](implicit 
      find: MaybeFromRequestAsync[Ctx],
      orElse: MissingFromRequest[Ctx],
      ec: ExecutionContext,
    ): FromRequestAsync[Ctx] = { implicit request =>
      find.extractOptAsync(request).map(_.toRight(orElse.respondToMissingContext(request)))
    }

    // Folds the futures into the first early response or an array of the accumlated results
    private def squash[T](
      thunks: Seq[() => Future[Either[Response, Any]]]
    )(
      gather: PartialFunction[Array[Any], T]
    )(implicit ec: ExecutionContext): Future[Either[Response, T]] = {
      val acc = new Array[Any](thunks.size)
      var squashed: Future[Either[Response, Array[Any]]] = Future.successful(Right(acc))
      for ((result, i) <- thunks.zipWithIndex) {
        squashed = squashed.flatMap { 
          case Left(resp) => Future.successful(Left(resp))
          case Right(acc) =>
            result().map(_.right.map { x =>
              acc(i) = x
              acc
            })
        }
      }
      squashed.map(_.right.map(gather))
    }

    implicit def fromRequestAsync2[
      A: FromRequestAsync,
      B: FromRequestAsync,
    ](implicit ec: ExecutionContext): FromRequestAsync[(A, B)] = { implicit request =>
      squash(Seq(
        () => extractOrRespondAsync[A],
        () => extractOrRespondAsync[B],
      )) {
        case Array(a, b) => (a, b).asInstanceOf[(A, B)]
      }
    }

    implicit def fromRequestAsync3[
      A: FromRequestAsync,
      B: FromRequestAsync,
      C: FromRequestAsync,
    ](implicit ec: ExecutionContext): FromRequestAsync[(A, B, C)] = { implicit request =>
      squash(Seq(
        () => extractOrRespondAsync[A],
        () => extractOrRespondAsync[B],
        () => extractOrRespondAsync[C],
      )) {
        case Array(a, b, c) => (a, b, c).asInstanceOf[(A, B, C)]
      }
    }
  }
}
