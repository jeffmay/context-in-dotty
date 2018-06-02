package example.app.services

import example.app.context.{AuthCtx}
import example.app.models.{Authorization, Role, User}
import example.database.context.DBCtx
import example.database.collections.UserCollection

import scala.concurrent.{ExecutionContext, Future}

import future.concurrent.ImplicitExecutionContext

class UserService(
  users: UserCollection,
  ec: ExecutionContext
) extends ImplicitExecutionContext(ec) {

  def findUser(id: Int): AuthCtx.To[Future[Option[User]]] = {
    users.findById(id)
  }
}
