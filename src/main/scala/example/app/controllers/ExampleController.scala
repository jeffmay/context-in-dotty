package example.app.controllers

import example.app.context.{AuthCtx, RequestCtx}
import example.app.logging.AppLogger
import example.app.services.UserService

import future.concurrent.{ImplicitExecutionContext}
import future.play.api._
import future.play.json._

import scala.concurrent.{ExecutionContext, Future}

class ExampleController(
  ec: ExecutionContext
) extends ImplicitExecutionContext(ec) {

  def returnOk = ActionBuilder(ec).handle {
    Response(200)
  }
}
