package future.play.api

/**
  * A dumb response model
  */
case class Response(status: Int, body: String = "", headers: Map[String, String] = Map.empty)
