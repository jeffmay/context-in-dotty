package future.play.controllers

import scala.concurrent.ExecutionContext

import future.play.services.{RoleService, UserService}

case class CtrlCtx(
  actionToolkit: AppActionToolkit,
)
