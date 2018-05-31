package future.play.models

import future.play.services.RoleService

case class Role(name: String) extends AnyVal

case class Permission(name: String) extends AnyVal

case class Authorization(roles: Set[Role], permissions: Set[Permission]) {

  private[this] var cachedPermissions: Set[Permission] = null

  def allPermissions(implicit roleSvc: RoleService): AppCtx.To[Set[Permission]] = {
    if (cachedPermissions eq null) {
      cachedPermissions = roleSvc.expandRoles(roles) ++ permissions
    }
    cachedPermissions
  }

  def hasPermission(permission: Permission): Boolean = {
    permissions contains permission
  }
}
