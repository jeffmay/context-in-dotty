package future.play.models

import java.util.UUID

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
