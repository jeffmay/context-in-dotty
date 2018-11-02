package future.play.context

import scala.annotation.implicitNotFound

import future.play.api._
import future.play.models._
import scala.concurrent.{ExecutionContext, Future}

trait PlayRequestCtx(implicit val request: Request)
object PlayRequestCtx extends ContextCompanion[PlayRequestCtx]

trait PlayServerCtx(implicit val executionContext: ExecutionContext)
object PlayServerCtx extends ContextCompanion[PlayServerCtx]

trait PlayErrorCtx(val error: Throwable) // TODO: Better exception here?

abstract class ContextCompanion[Ctx] {
  
  /**
    * A simple type alias for all service methods to use when they require the standard
    * application context.
    */
  type To[X] = implicit Ctx => X

  inline def here(implicit ctx: Ctx): Ctx = ctx
}

trait ServerCtx extends PlayServerCtx

@implicitNotFound(
  "Missing implicit RequestCtx here. This model is extracted from the request by the controller " +
  "and passed along implicitly. Please include or define an implicit context in scope."
)
sealed trait RequestCtx extends PlayRequestCtx with CorrelationIdCtx
object RequestCtx extends ContextCompanion[RequestCtx] {

  implicit val extractor: RequestContextValidator.Of[ServerCtx, RequestCtx] = RequestContextValidator.always {
    new RequestCtx with PlayRequestCtx with CorrelationIdCtx(CorrelationId.extractFromHeaderMap(Request.here.headers))
  }
}

// case class UnAuthCtx(request: Request, correlationId: CorrelationId) extends AppCtx

// case class AuthCtx(
//   request: Request, 
//   correlationId: CorrelationId,
//   authorization: Authorization
// ) extends AppCtx

object RequestContextValidator {

  type Of[ServerCtx <: PlayServerCtx, RequestCtx <: PlayRequestCtx] = implicit (Request, ServerCtx) => Future[Either[Response, RequestCtx]]

  inline def always[ServerCtx <: PlayServerCtx, RequestCtx <: PlayRequestCtx](
    block: implicit (Request, ServerCtx) => RequestCtx
  ): RequestContextValidator.Of[ServerCtx, RequestCtx] = {
    Future.successful(Right(block))
  }

  inline def either[RequestCtx <: PlayRequestCtx](
    block: implicit (Request, ServerCtx) => Either[Response, RequestCtx]
  ): RequestContextValidator.Of[ServerCtx, RequestCtx] = Future.successful(block)

  inline def async[ServerCtx <: PlayServerCtx, RequestCtx <: PlayRequestCtx](
    block: implicit (Request, ServerCtx) => Future[RequestCtx]
  ): RequestContextValidator.Of[ServerCtx, RequestCtx] = {
    val ctx = PlayServerCtx.here
    import ctx._
    block.map(Right(_))
  }

  // final class Builder[ServerCtx <: PlayServerCtx] {
  inline def asyncEither[ServerCtx <: PlayServerCtx, RequestCtx <: PlayRequestCtx](
    block: implicit (Request, ServerCtx) => Future[Either[Response, RequestCtx]]
  ): RequestContextValidator.Of[ServerCtx, RequestCtx] = {
    block
  }

  inline def asyncOr[ServerCtx <: PlayServerCtx, RequestCtx <: PlayRequestCtx](
    failed: implicit PlayRequestCtx & PlayErrorCtx => Response
  )(
    block: implicit (Request, ServerCtx) => Future[RequestCtx]
  ): RequestContextValidator.Of[ServerCtx, RequestCtx] = {
    val ctx = PlayServerCtx.here
    import ctx._
    block.map(Right(_)).recover {
      case ex =>
        implicit val ctx: PlayRequestCtx & PlayErrorCtx = new PlayRequestCtx with PlayErrorCtx(ex)
        Left(failed)
    }
  }

  
  // }

  // def from[ServerCtx <: PlayServerCtx]: Builder[ServerCtx] = new Builder[ServerCtx]
}
