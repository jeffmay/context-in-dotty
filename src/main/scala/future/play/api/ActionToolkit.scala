package future.play.api

// import future.implicits.ImplicitTupleItems
import future.play.api._
import future.play.api.ActionContext
import future.play.api.ActionContext.FromRequest

/**
  * Inject this into your Controller and import from it to get implicits and a Play-like DSL.
  *
  * @tparam Ctx the type of context to extract for all actions
  */
abstract class ActionToolkit[A <: AB[_]](val Action: A) {

  // implicit def extractCtxOrDefaultResponse(implicit
  //   extractCtx: ActionContext.MaybeFromRequest[Ctx],
  //   failedHandler: ActionContext.MissingFromRequest[Ctx],
  // ): ActionContext.FromRequest[Ctx] = {
  //   request => extractCtx.extractOpt(request).toRight(failedHandler.respondToMissingContext(request))
  // }
}

final object ActionToolkit extends ActionToolkit[AB.type](AB) {
  type Untyped = ActionToolkit[AB.type]
}
