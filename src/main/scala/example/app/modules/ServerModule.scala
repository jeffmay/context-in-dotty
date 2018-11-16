package example.app.modules

import scala.concurrent.ExecutionContext

import future.play.api.ActionBuilder

class ServerModule(executionContext: ExecutionContext) {

  final lazy val execution = new SingleExecutionModule(executionContext)

  final lazy val database = new DatabaseModule

  final lazy val services = new ServiceModule(database, execution)

  final lazy val controllers = new ControllerModule(
    ActionBuilder(executionContext),
    execution,
    services
  )
}