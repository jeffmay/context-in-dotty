package future.play.controllers

import scala.concurrent.ExecutionContext

import future.concurrent.ImplicitExecutionContext
import future.play.api.{ActionContext, ActionToolkit, Request, Response}
import future.play.models._
import future.play.services.RoleService

case class CtrlCtx(
  actionToolkit: AppActionToolkit,
  executionContext: ExecutionContext
)

class AppActionToolkit(roleService: RoleService) extends ActionToolkit[AppCtx] {

  /**
    * Extract the [[AppCtx]] from the headers.
    */
  implicit lazy val correlationIdFromRequest: ActionContext.FromRequest[CorrelationId] = ActionContext.fromRequest {
    Right(CorrelationId.extractFromHeaderMap(Request.here.headers))
  }

  private def extractUnAuthContext(request: Request): UnAuthCtx = {
    UnAuthCtx(request, CorrelationId.extractFromHeaderMap(request.headers))
  }

  /**
    * Extract the [[AppCtx]] from the headers.
    */
  implicit lazy val unAuthCtxFromRequest: ActionContext.FromRequest[UnAuthCtx] = ActionContext.fromRequest {
    Right(extractUnAuthContext(Request.here))
  }

  /**
    * Extract the [[AppCtx]] from the headers or let the controller / action handle the failure.
    */
  implicit lazy val authCtxFromRequest: ActionContext.MaybeFromRequest[AuthCtx] = ActionContext.maybeFromRequest { implicit request =>
    request.headers.get("Authorization") map { authToken =>
      implicit val ctx = extractUnAuthContext(request)
      val rolesFromToken = Set(Role("member"))
      val perms = roleService.expandRoles(rolesFromToken)
      AuthCtx(request, ctx.correlationId, Authorization(rolesFromToken, perms))
    }
  }

  /**
    * The default failure handler when the [[AppCtx]] cannot be extracted from the request.
    */
  lazy val authCtxMissingFromRequest: ActionContext.MissingFromRequest[AuthCtx] = ActionContext.missingFromRequest {
    Response(401, s"Unauthorized request to ${Request.here}")
  }

  private def extractAppCtx(request: Request): AppCtx = {
    authCtxFromRequest.extractOpt(request) getOrElse extractUnAuthContext(request)
  }

  /**
    * Extract the [[AppCtx]] from the headers.
    */
  implicit lazy val appCtxFromRequest: ActionContext.FromRequest[AppCtx] = ActionContext.fromRequest { implicit request =>
    Right(extractAppCtx(request))
  }
}

abstract class BaseController(cctx: CtrlCtx)
  extends ImplicitExecutionContext(cctx.executionContext)
