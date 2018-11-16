package example.app.modules

import example.app.controllers.ExampleController
import example.app.context.RequestCtx

import example.app.logging.AppLogger

import future.play.api.{ActionBuilder, Request}

class ControllerModule(
  playActionBuilder: ActionBuilder[Request],
  executionModule: ExecutionModule,
  serviceModule: ServiceModule
) {

  lazy val rootActionBuilder: ActionBuilder[RequestCtx] = playActionBuilder.refined[RequestCtx]

  lazy val exampleCtrl: ExampleController = new ExampleController(
    rootActionBuilder,
    new AppLogger(classOf[ExampleController]),
    serviceModule.userService,
    executionModule.cpuBound
  )
}
