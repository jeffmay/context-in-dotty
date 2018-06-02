package future.play.api

import future.implicits.ImplicitTupleItems
import future.play.api.ActionContext
import future.play.api.ActionContext.FromRequest

/**
  * Inject this into your Controller and import from it to get implicits and a Play-like DSL.
  *
  * @tparam Ctx the type of context to extract for all actions
  */
class ActionToolkit[Ctx](root: AB[Ctx]) extends ImplicitTupleItems {

  // implicit def extractCtxOrDefaultResponse(implicit
  //   extractCtx: ActionContext.MaybeFromRequest[Ctx],
  //   failedHandler: ActionContext.MissingFromRequest[Ctx],
  // ): ActionContext.FromRequest[Ctx] = {
  //   request => extractCtx.extractOpt(request).toRight(failedHandler.respondToMissingContext(request))
  // }

  def Action: AB[Ctx] = root
}

final object ActionToolkit extends ActionToolkit[Request](AB) {
  type Untyped = ActionToolkit[Request]
}
