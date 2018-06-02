package example.app.context

import example.tracing.context.TraceCtx
import example.tracing.CorrelationId
import example.database.context.DBCtx

import future.context.ContextCompanion
import future.play.api._

import scala.annotation.implicitNotFound
import scala.concurrent.Future
import scala.util.Try

@implicitNotFound(
  "Missing implicit RequestCtx here. This model is extracted from the request by the controller " +
  "and passed along implicitly. Please include or define an implicit context in scope."
)
class RootCtxFromRequest(req: Request) 
  extends PlayRequestContext(req)
  with RootCtx {
  override lazy val correlationId: CorrelationId = CorrelationId.extractFromHeaderMap(request.headers)
  override lazy val maybeUserId: Option[Int] = request.headers.get("UserId").flatMap(v => Try(v.toInt).toOption)
}

sealed trait RootCtx extends TraceCtx {
  def request: Request
}

object RootCtx extends RequestContextCompanion[RootCtx] {
  implicit val refine: RefineFrom[PlayRequestContext] = {
    ContextRefiner.sync { implicit ctx =>
      new RootCtxFromRequest(ctx.request)
    }
  }
}

@implicitNotFound(
  "Missing implicit AuthCtx here. This model is extracted from the request by the controller " +
  "and passed along implicitly. Please include or define an implicit context in scope."
)
sealed trait AuthCtx extends RootCtx with DBCtx with TraceCtx {
  def authenticatedUserId: Int
}

object AuthCtx extends RequestContextCompanion[AuthCtx] {
  implicit val refine: RefineFrom[RootCtx] = ContextRefiner.syncOrRespond {
    RootCtx.here.maybeUserId.map { userId =>
      new AuthCtxFromRequest(RootCtx.here.request, userId)
    }.toRight(Response(401))
  }
}

class AuthCtxFromRequest(
  request: Request,
  override val authenticatedUserId: Int
) extends RootCtxFromRequest(request) 
  with AuthCtx
