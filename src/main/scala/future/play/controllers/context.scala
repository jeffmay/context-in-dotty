package future.play.controllers

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

import future.concurrent.ImplicitExecutionContext
import future.play.api.{ActionContext, ActionToolkit, Request, Response}
import future.play.models._
import future.play.services.{RoleService, UserService}

case class CtrlCtx(
  actionToolkit: AppActionToolkit,
  executionContext: ExecutionContext,
)

class AppActionToolkit(
  roleService: RoleService,
  userService: UserService,
) extends ActionToolkit[AppCtx] {

  /**
    * Extract the [[CorrelationId]] from the headers.
    *
    * NOTE: This is just for demo purposes, we wouldn't need this since we can always use [[UnAuthCtx]]
    */
  implicit lazy val correlationIdFromRequest: ActionContext.FromRequest[CorrelationId] = ActionContext.fromRequest {
    Right(CorrelationId.extractFromHeaderMap(Request.here.headers))
  }

  private def extractUnAuthContext(request: Request): UnAuthCtx = {
    UnAuthCtx(request, CorrelationId.extractFromHeaderMap(request.headers))
  }

  /**
    * Extract the [[UnAuthCtx]] from the headers.
    */
  implicit lazy val unAuthCtxFromRequest: ActionContext.FromRequest[UnAuthCtx] = ActionContext.fromRequest {
    Right(extractUnAuthContext(Request.here))
  }

  /**
    * Extract the [[AuthCtx]] from the headers or let the controller / action handle the failure.
    */
  implicit def authCtxFromRequest(implicit ec: ExecutionContext): ActionContext.MaybeFromRequestAsync[AuthCtx] = {
    ActionContext.maybeFromRequestAsync { implicit request =>
      val maybeUserId = for {
        authToken <- request.headers.get("Authorization")
        userId <- Try(authToken.toInt).toOption
      } yield userId
      maybeUserId match {
        case Some(userId) =>
          implicit val ctx = extractUnAuthContext(request)
          userService.findUser(userId) flatMap {
            case Some(user) =>
              roleService.expandRoles(user.roles) map { perms =>
                Some(AuthCtx(request, ctx.correlationId, Authorization(user.roles, perms)))
              }
            case None =>
              Future.successful(None)
          }
        case None =>
          Future.successful(None)
      }
    }
  }

  /**
    * The default failure handler when the [[AppCtx]] cannot be extracted from the request.
    */
  lazy val authCtxMissingFromRequest: ActionContext.MissingFromRequest[AuthCtx] = ActionContext.missingFromRequest {
    Response(401, s"Unauthorized request to ${Request.here}")
  }

  /**
    * Extract the [[AppCtx]] from the headers.
    */
  implicit def appCtxFromRequest(implicit ec: ExecutionContext): ActionContext.FromRequestAsync[AppCtx] = {
    ActionContext.fromRequestAsync { implicit request =>
      authCtxFromRequest.extractOptAsync(request)
        .map(maybeCtx => Right(maybeCtx.getOrElse(extractUnAuthContext(request))))
    }
  }
}

abstract class BaseController(cctx: CtrlCtx)
  extends ImplicitExecutionContext(cctx.executionContext)
