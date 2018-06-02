package example.tracing

import java.util.UUID

import scala.annotation.implicitNotFound

import future.context.ContextCompanion

/**
  * An identifier for relating all logs to a single request -> response cycle.
  */
case class CorrelationId(value: String) extends AnyVal
final object CorrelationId extends ContextCompanion[CorrelationId] {

  def random(): CorrelationId = CorrelationId(UUID.randomUUID().toString)

  final val HeaderKey = "Correlation-Id"

  def extractFromHeaderMap(headers: Map[String, String]): CorrelationId = {
    headers.get(HeaderKey).map(CorrelationId(_)).getOrElse(random())
  }
}
