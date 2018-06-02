package future.context

trait ContextCompanion[Ctx] {

  type To[+X] = implicit Ctx => X

  inline def here(implicit ctx: Ctx): Ctx = ctx
}
