package example.app.models

import future.play.json._

case class User(id: Int, name: String, roles: Set[Role])

object User {
  implicit val writesUser: Writes[User] = { user =>
    JsValue.JsObject(Map(
      "id" -> Json.toJson(user.id),
      "name" -> Json.toJson(user.name),
      "roles" -> Json.toJson(user.roles)
    ))
  }
}
