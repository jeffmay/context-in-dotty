package future.play.api

import future.play.api.ActionContext
import future.play.api.ActionContext.FromRequest

/**
  * Inject this into your Controller and import from it to get implicits and a Play-like DSL.
  *
  * @tparam Ctx the type of context to extract for all actions
  */
class ActionToolkit[Ctx] {

  implicit def extractCtxOrDefaultResponse(implicit
    extractCtx: ActionContext.MaybeFromRequest[Ctx],
    failedHandler: ActionContext.MissingFromRequest[Ctx]
  ): ActionContext.FromRequest[Ctx] = {
    request => extractCtx.extractOpt(request).toRight(failedHandler.respondToMissingContext(request))
  }

  final object Action extends ActionBuilder[Ctx]
}

final object ActionToolkit extends ActionToolkit[Unit] {
  type Untyped = ActionToolkit[Unit]
}
