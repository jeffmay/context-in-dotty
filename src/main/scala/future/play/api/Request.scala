package future.play.api

/**
  * A dumb request model
  */
case class Request(method: String, path: String, body: String = "", headers: Map[String, String] = Map.empty) {
  override def toString: String = s"$method $path"
}

object Request {
  inline def here(implicit request: Request): Request = request
}
