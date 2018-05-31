package future.play.services

import scala.concurrent.ExecutionContext

import future.concurrent.ImplicitExecutionContext

case class SvcCtx(executionContext: ExecutionContext)

abstract class BaseService(sctx: SvcCtx)
  extends ImplicitExecutionContext(sctx.executionContext) {


}
