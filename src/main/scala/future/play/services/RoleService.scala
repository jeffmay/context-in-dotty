package future.play.services

import future.play.models.{AppCtx, Permission, Role}

class RoleService(sctx: SvcCtx) extends BaseService(sctx) {

  private val memberPerms = Set(Permission("ViewGroup"))
  private val editorPerms = Set(Permission("EditGroup"))
  private val adminPerms = editorPerms ++ memberPerms ++ Set(Permission("EditRoles"), Permission("EditPermissions"))
  
  private val permissionMap = Map(
    Role("member") -> memberPerms,
    Role("editor") -> editorPerms,
    Role("admin") -> adminPerms
  )

  def expandRoles(roles: Set[Role]): AppCtx.To[Set[Permission]] = {
    roles.flatMap(role => permissionMap.getOrElse(role, Set.empty))
  }
}
