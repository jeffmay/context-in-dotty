package example.app.modules

import scala.concurrent.ExecutionContext

trait ExecutionModule {

  def cpuBound: ExecutionContext
}

class SingleExecutionModule(default: ExecutionContext) extends ExecutionModule {

  final def cpuBound: ExecutionContext = default
}
