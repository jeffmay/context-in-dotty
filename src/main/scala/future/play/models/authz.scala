package future.play.models

import future.play.services.RoleService

case class Role(name: String) extends AnyVal

case class Permission(name: String) extends AnyVal

case class Authorization(roles: Set[Role], permissions: Set[Permission]) {

  def hasPermission(permission: Permission): Boolean = {
    permissions contains permission
  }
}
