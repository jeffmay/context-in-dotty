package example.app.context

import example.tracing.context.TraceCtx
import example.tracing.CorrelationId
import example.database.context.DBCtx

import future.logging.LoggableAsMap
import future.context.ContextCompanion
import future.play.api._

import scala.annotation.implicitNotFound
import scala.concurrent.Future
import scala.util.Try

@implicitNotFound(
  "Missing implicit RequestCtx here. This model is extracted from the request by the controller " +
  "and passed along implicitly. Please include or define an implicit context in scope."
)
class BaseRequestContext(req: Request) 
  extends PlayRequestContext(req)
  with RequestCtx {
  override lazy val correlationId: CorrelationId = CorrelationId.extractFromHeaderMap(request.headers)
  override lazy val maybeUserId: Option[Int] = request.headers.get("UserId").flatMap(v => Try(v.toInt).toOption)
}

sealed trait RequestCtx
  extends PlayRequestContext
  with TraceCtx

object RequestCtx extends RequestContextCompanion[RequestCtx] {
  implicit val refine: RefineFrom[Request] = {
    ContextRefiner.sync { implicit request =>
      new BaseRequestContext(request)
    }
  }

  implicit val loggable: LoggableAsMap[RequestCtx] = { ctx =>
    Map(
      "path" -> ctx.request.path,
      "userId" -> ctx.maybeUserId.map(_.toString).getOrElse("unauthenticated"),
      "correlationId" -> ctx.correlationId.value
    )
  }
}

@implicitNotFound(
  "Missing implicit AuthCtx here. This model is extracted from the request by the controller " +
  "and passed along implicitly. Please include or define an implicit context in scope."
)
sealed trait AuthCtx extends RequestCtx with DBCtx {
  def authenticatedUserId: Int
}

object AuthCtx extends RequestContextCompanion[AuthCtx] {
  implicit val refine: RefineFrom[RequestCtx] = ContextRefiner.syncOrRespond {
    RequestCtx.here.maybeUserId.map { userId =>
      new AuthCtxFromRequest(RequestCtx.here.request, userId)
    }.toRight(Response(401))
  }

  implicit val loggableAsMap: LoggableAsMap[AuthCtx] = { ctx =>
    LoggableAsMap.write(ctx)
  }
}

class AuthCtxFromRequest(
  request: Request,
  override val authenticatedUserId: Int
) extends BaseRequestContext(request) 
  with AuthCtx
