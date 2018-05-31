package future.play.controllers

import future.concurrent.ImplicitExecutionContext

abstract class BaseController(cctx: CtrlCtx) {

  protected val actionToolkit = cctx.actionToolkit
}
