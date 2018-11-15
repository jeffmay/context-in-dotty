package example.app.controllers

import example.app.context.{AuthCtx, RequestCtx}
import example.app.logging.AppLogger
import example.app.services.UserService

import future.concurrent.{ImplicitExecutionContext}
import future.play.api._
import future.play.json._

import scala.concurrent.{ExecutionContext, Future}

class ExampleController(
  action: ActionBuilder[Request],
  ec: ExecutionContext
) extends ImplicitExecutionContext(ec) {

  def returnOk = action.handle {
    println(s"${Request.here.path} called")
    Response(200)
  }
}
