package example.app.services

import example.app.context.AuthCtx
import example.app.models.{Permission, Role}

import future.concurrent.ImplicitExecutionContext

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

class RoleService(executionContext: ExecutionContext) extends ImplicitExecutionContext(executionContext) {

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

  def expandRoles(roles: Set[Role]): AuthCtx.To[Future[Set[Permission]]] = {
    Future.successful(roles.flatMap(role => permissionMap.getOrElse(role, Set.empty)))
  }
}
