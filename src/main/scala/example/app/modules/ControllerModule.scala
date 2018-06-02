package example.app.modules

import example.app.controllers.ExampleController
import example.app.context.RootCtx

import example.app.logging.AppLogger

import future.play.api.{ActionBuilder, PlayRequestContext}

class ControllerModule(
  playActionBuilder: ActionBuilder[PlayRequestContext],
  executionModule: ExecutionModule,
  serviceModule: ServiceModule
) {

  lazy val rootActionBuilder: ActionBuilder[RootCtx] = playActionBuilder.refined[RootCtx]

  lazy val exampleCtrl: ExampleController = new ExampleController(
    rootActionBuilder,
    new AppLogger(classOf[ExampleController]),
    serviceModule.userService,
    executionModule.cpuBound
  )
}
