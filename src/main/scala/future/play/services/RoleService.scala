package future.play.services

import scala.concurrent.Future

import future.play.models.{AppCtx, Permission, Role}

class RoleService(sctx: SvcCtx) extends BaseService(sctx) {

  private val memberPerms: Set[Permission] = Set(
    Permission("CreateArticle"),
  )
  private val editorPerms: Set[Permission] = Set(
    Permission("PublishArtice"),
    Permission("ViewPlugins"),
  ) ++ memberPerms
  private val adminPerms: Set[Permission] = Set(
    Permission("AddUser"),
    Permission("EditPlugins"),
    Permission("EditRoles"),
    Permission("EditPermissions"),
  ) ++ editorPerms
  
  private val permissionMap: Map[Role, Set[Permission]] = Map(
    Role("member") -> memberPerms,
    Role("editor") -> editorPerms,
    Role("admin") -> adminPerms,
  )

  def expandRoles(roles: Set[Role]): AppCtx.To[Future[Set[Permission]]] = {
    Future.successful(roles.flatMap(role => permissionMap.getOrElse(role, Set.empty)))
  }
}
