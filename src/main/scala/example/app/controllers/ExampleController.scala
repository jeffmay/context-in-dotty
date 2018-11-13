package example.app.controllers

import example.app.context.{AuthCtx, RootCtx}
import example.app.logging.AppLogger
import example.app.services.UserService

import future.concurrent.{ImplicitExecutionContext}
import future.play.api._
import future.play.json._

import scala.concurrent.{ExecutionContext, Future}

class ExampleController(
  action: ActionBuilder[RootCtx],
  logger: AppLogger,
  userService: UserService,
  ec: ExecutionContext
) extends ImplicitExecutionContext(ec) {

  implicit val stringResponder: Responder[String] = Responder.sync(value => Response(200, value))

  implicit def optionResponder[A: Writes]: Responder[Option[A]] = {
    Responder.sync {
      case Some(value) => Response(200, Json.serialize(value))
      case None => Response(404)
    }
  }

  def returnOk = action.handle {
    println(s"${Request.here.path} called")
    Response(200)
  }

  def authenticated = action.refined[AuthCtx].handle {
    Response(200)
  }

  def responder = action.handle {
    "hello"
  }

  def findUser = action.refined[AuthCtx].handle {
    userService.findUser(AuthCtx.here.authenticatedUserId)
  }

  def logging = action.refined[AuthCtx].handle {
    logger.info("Testing")
    Response(200)
  }
}
