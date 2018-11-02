package future.play.api

import future.play.context.PlayRequestCtx

/**
  * A dumb request model
  */
case class Request(method: String, path: String, body: String = "", headers: Map[String, String] = Map.empty) {
  override def toString: String = s"$method $path"
}

object Request {

  inline def here(implicit request: Request): Request = request

  // inline def here(implicit ctx: PlayRequestCtx): Request = ctx.request

  type To[T] = implicit Request => T
}
