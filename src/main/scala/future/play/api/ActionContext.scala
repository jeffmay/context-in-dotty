package future.play.api

import scala.annotation.implicitNotFound
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

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

  def fromRequestAsync[Ctx](parse: implicit Request => Future[Either[Response, Ctx]]): FromRequestAsync[Ctx] = {
    implicit request => parse
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
  trait MaybeFromRequest[Ctx] extends MaybeFromRequestAsync[Ctx] {
    
    def extractOpt(request: Request): Option[Ctx]

    override def extractOptAsync(request: Request): Future[Option[Ctx]] = {
      Future.fromTry(Try(extractOpt(request)))
    }
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
    implicit lazy val requestFromRequest: FromRequest[Request] = new FromRequest[Request] {
      override def extractOrRespond(request: Request): Either[Response, Request] = Right(request)
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
  trait FromRequestAsync[Ctx] extends MaybeFromRequestAsync[Ctx] {

    def extractOptAsync(request: Request): Future[Option[Ctx]] = {
      extractOrRespondAsync(request).transform(_.map(_.right.toOption))
    }

    def extractOrRespondAsync(request: Request): Future[Either[Response, Ctx]]
  }

  object FromRequestAsync {

    def extract[Ctx](request: Request)(implicit fromRequest: FromRequestAsync[Ctx]): Future[Either[Response, Ctx]] = {
      fromRequest.extractOrRespondAsync(request)
    }

    implicit def missing[Ctx](implicit 
      find: MaybeFromRequestAsync[Ctx],
      orElse: MissingFromRequest[Ctx],
      ec: ExecutionContext,
    ): FromRequestAsync[Ctx] = { implicit request =>
      find.extractOptAsync(Request.here).map(_.toRight(orElse.respondToMissingContext(request)))
    }

    private def squash(computeStream: Stream[Future[Either[Response, Any]]]): implicit ExecutionContext => Future[Either[Response, List[Any]]] = {
      var squashed: Future[Either[Response, List[Any]]] = Future.successful(Right(Nil))
      for (result <- computeStream) {
        squashed = squashed.flatMap { 
          case Left(resp) => Future.successful(Left(resp))
          case Right(acc) =>
            result.transform(_.map {
              case Left(resp) => Left(resp)
              case Right(x) => Right(x :: acc)
            })
        }
      }
      squashed
    }

    implicit def fromRequestAsync2[
      A: FromRequestAsync,
      B: FromRequestAsync,
    ](implicit ec: ExecutionContext): FromRequestAsync[(A, B)] = { request =>
      val squashed = squash(Stream(
        FromRequestAsync.extract[A](request),
        FromRequestAsync.extract[B](request),
      ))
      squashed.transform(_.map(_.right.map {
        case List(b, a) => (a, b).asInstanceOf[(A, B)]
        case _ => ???
      }))
    }

    implicit def fromRequestAsync3[
      A: FromRequestAsync,
      B: FromRequestAsync,
      C: FromRequestAsync,
    ](implicit ec: ExecutionContext): FromRequestAsync[(A, B, C)] = { request =>
      val squashed = squash(Stream(
        FromRequestAsync.extract[A](request),
        FromRequestAsync.extract[B](request),
        FromRequestAsync.extract[C](request),
      ))
      squashed.transform(_.map(_.right.map {
        case List(c, b, a) => (a, b, c).asInstanceOf[(A, B, C)]
        case _ => ???
      }))
    }
  }
}
