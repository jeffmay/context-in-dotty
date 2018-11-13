package future.play.api

import future.context.ContextCompanion

/**
  * Captured during request filtering / initialization.
  */
trait PlayRequestContext(val request: Request)
object PlayRequestContext extends ContextCompanion[PlayRequestContext]
