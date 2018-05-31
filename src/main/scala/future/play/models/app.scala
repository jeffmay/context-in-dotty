package future.play.models

import java.util.UUID

import scala.annotation.implicitNotFound

import future.play.api._
import future.play.models.{Authorization, AuthCtx, Role, Permission, UnAuthCtx}

/**
  * An identifier for relating all logs to a single request -> response cycle.
  */
case class CorrelationId(value: String) extends AnyVal
final object CorrelationId {

  inline def here(implicit id: CorrelationId): CorrelationId = id

  def random(): CorrelationId = CorrelationId(UUID.randomUUID().toString)

  final val HeaderKey = "Correlation-Id"

  def extractFromHeaderMap(headers: Map[String, String]): CorrelationId = {
    headers.get(HeaderKey).map(CorrelationId(_)).getOrElse(random())
  }
}

@implicitNotFound(
  "Missing implicit AppCtx here. This model is extracted from the request by the controller " +
  "and passed along implicitly. Please include or define an implicit context in scope."
)
sealed trait AppCtx {
  def request: Request
  def correlationId: CorrelationId
}
object AppCtx {

  inline def here(implicit ctx: AppCtx): AppCtx = ctx

  /**
    * A simple type alias for all service methods to use when they require the standard
    * application context.
    */
  type To[T] = implicit AppCtx => T
}

case class UnAuthCtx(request: Request, correlationId: CorrelationId) extends AppCtx

case class AuthCtx(
  request: Request, 
  correlationId: CorrelationId,
  authorization: Authorization
) extends AppCtx
