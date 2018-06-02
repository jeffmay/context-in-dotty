package future.play.controllers

import scala.annotation.implicitNotFound
import scala.concurrent.{ExecutionContext, Future}

import future.concurrent.{ExecuteOnCallingThread, ImplicitExecutionContext}
import future.play.api._
import future.play.models.User
import future.play.services.UserService
import future.play.logging.AppLogger
import future.play.models.{AppCtx, AuthCtx, CorrelationId}

class Application(
  cctx: CtrlCtx,
  service: UserService,
  logger: AppLogger = new AppLogger
) extends BaseController(cctx) {
  import cctx.actionToolkit._

  def echoCorrelationId: Action = Action.withContext[CorrelationId].async {
    println(CorrelationId.here)
    Future.successful(Response(200))
  }

  def example: Action = Action.async {
    logger.info("Received request to Application.example")
    Future.successful(Response(200, "Simple Little Example"))
  }

  def findUser(userId: Int): Action = Action.withContext[AuthCtx].asyncOr(Response(500)) {
    service.findUser(userId).map { maybeUser: Option[User] =>
      logger.info(s"Found user called with $userId")
      maybeUser.map(user => Response(200, user.toString)).getOrElse(Response(404))
    }
  }
}

object Example {
  import future.play.models.CorrelationId
  implicit def ec: ExecutionContext = ExecuteOnCallingThread

  import ActionToolkit._

  /**
    * Extract the [[CorrelationId]] from the headers.
    *
    * NOTE: This is just for demo purposes, we wouldn't need this since we can always use [[UnAuthCtx]]
    */
  implicit lazy val correlationIdFromRequest: ActionContext.FromRequest[CorrelationId] = ActionContext.fromRequest {
    Right(CorrelationId.extractFromHeaderMap(Request.here.headers))
  }

  // Action.withContext[CorrelationId].async {
  //   println(CorrelationId.here)
  //   Response(200)
  // }
}
