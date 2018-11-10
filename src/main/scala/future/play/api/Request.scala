package future.play.api

import future.context.ContextCompanion

/**
  * A simple request model
  */
case class Request(method: String, path: String, body: String = "", headers: Map[String, String] = Map.empty) {
  override def toString: String = s"$method $path"
}

object Request extends ContextCompanion[Request] {
  inline implicit def fromContext(implicit playContext: PlayRequestContext): Request = playContext.request
}
