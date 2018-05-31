package future.play.controllers

import scala.annotation.implicitNotFound
import scala.concurrent.{ExecutionContext, Future}

import future.concurrent.ImplicitExecutionContext
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

  def findUser(userId: Int): Action = Action.withContext[AuthCtx].async {
    service.findUser(userId).map { maybeUser: Option[User] =>
      logger.info(s"Found user called with $userId")
      maybeUser.map(user => Response(200, user.toString)).getOrElse(Response(404))
    }
  } (ActionContext.maybeFromRequestOrElse(Response(500)))
}
