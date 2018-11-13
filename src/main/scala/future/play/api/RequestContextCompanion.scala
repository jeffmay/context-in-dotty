package future.play.api

import future.context.ContextCompanion

trait RequestContextCompanion[Ctx] extends ContextCompanion[Ctx] {
  type RefineFrom[-X] = ContextRefiner[X, Ctx]
}
