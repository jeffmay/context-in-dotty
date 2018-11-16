package example.app.logging

import future.logging._
import example.app.context.RequestCtx
import scala.reflect.{classTag, ClassTag}

class AppLogger[Ctx: LoggableAsMap](name: String)
  extends ContextLogger[Ctx](name)
  with StandardLogFormat
  with ContextMapFormat[Ctx] {

  def this(cls: Class[_]) = this(LoggerName.forClass(cls))
}

object AppLogger {
  inline def apply[T: LoggableAsMap: ClassTag]: AppLogger[T] = {
    new AppLogger(classTag[T].runtimeClass)
  }
}
