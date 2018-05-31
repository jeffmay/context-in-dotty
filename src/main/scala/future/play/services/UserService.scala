package future.play.services

import scala.concurrent.{ExecutionContext, Future}

import future.concurrent.ImplicitExecutionContext
import future.play.models.{AppCtx, Authorization, Role, User}

class UserService(sctx: SvcCtx) extends BaseService(sctx) {

  private val users: Map[Int, User] = Seq(
    User(1, "Jeff", Set(Role("admin"))),
    User(2, "Marco", Set(Role("member")))
  ).map(u => u.id -> u).toMap

  def findUser(id: Int): AppCtx.To[Future[Option[User]]] = {
    Future(users.get(id))
  }
}
