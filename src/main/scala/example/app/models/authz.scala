package example.app.models

import future.play.json._

case class Role(name: String) extends AnyVal
object Role {
  implicit val writesRole: Writes[Role] = role => JsValue.JsString(role.name)
}

case class Permission(name: String) extends AnyVal

case class Authorization(roles: Set[Role], permissions: Set[Permission]) {

  def hasPermission(permission: Permission): Boolean = {
    permissions contains permission
  }
}
