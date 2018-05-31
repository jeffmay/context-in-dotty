package future.play.controllers

import scala.annotation.implicitNotFound
import scala.concurrent.{ExecutionContext, Future}

case class CorrelationId(value: String)

@implicitNotFound("Missing implicit AppCtx here. You'll need to pass it into implicit scope.")
case class AppCtx(request: Request, correlationId: CorrelationId)
object AppCtx {

  // inline
  def here(implicit ctx: AppCtx): AppCtx = ctx

  /**
    * Extract the [[AppCtx]] from the headers or let the controller / action handle the failure.
    */
  implicit lazy val maybeFromRequest: ActionContext.MaybeFromRequest[AppCtx] = ActionContext.maybeFromRequest {
    Request.here.headers.get("Correlation-Id").map { id =>
      AppCtx(Request.here, CorrelationId(id))
    }
  }

  /**
    * Extract the [[AppCtx]] from the headers or use this default failure.
    */
  lazy val fromRequest: ActionContext.FromRequest[AppCtx] = ActionContext.fromRequest { implicit request =>
    maybeFromRequest.extractOpt(request).toRight(missingFromRequest.respondToMissingContext(request))
  }

  /**
    * The default failure handler when the [[AppCtx]] cannot be extracted from the request.
    */
  lazy val missingFromRequest: ActionContext.MissingFromRequest[AppCtx] = ActionContext.missingFromRequest {
    Response(400, s"Could not extract AppCtx from request to ${Request.here.path} " +
      s"with headers ${Request.here.headers.map((k, v) => s"$k=$v").mkString(", ")}")
  }

  /**
    * A simple type alias for all service methods to use when they require the standard
    * application context.
    */
  type To[T] = ImplicitFunction1[AppCtx, T]
}

class Application(
  actionToolkit: ActionToolkit[AppCtx],
  service: Service,
  logger: Logger = new Logger
)(implicit ec: ExecutionContext) {
  import actionToolkit._

  def example: Action = Action.async {
    logger.info("Received request to Application.example")
    Future.successful(Response(200, "Simple Little Example"))
  }

  def findUser(userId: Int): Action = Action.async {
    service.findUser(userId).map { maybeUser: Option[User] =>
      logger.info(s"Found user called with $userId")
      maybeUser.map(user => Response(200, user.toString)).getOrElse(Response(404))
    }
  } (ActionContext.maybeFromRequestOrElse(Response(500)))
}

case class User(id: Int, name: String)

class Service(implicit ec: ExecutionContext) {

  private val users = Seq(
    User(1, "Jeff"),
    User(2, "Marco")
  ).map(u => u.id -> u).toMap

  def findUser(id: Int): AppCtx.To[Future[Option[User]]] = {
    Future(users.get(id))
  }
}

class Logger {

  def info(msg: String)(implicit ctx: AppCtx): Unit = {
    println(s"INFO: $msg [path=${ctx.request.path}, correlationId=${ctx.correlationId.value}]")
  }
}
