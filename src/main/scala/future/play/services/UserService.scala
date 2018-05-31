package future.play.services

import scala.concurrent.{ExecutionContext, Future}

import future.concurrent.ImplicitExecutionContext
import future.play.models.{AppCtx, User}

class UserService(sctx: SvcCtx) extends BaseService(sctx) {

  private val users = Seq(
    User(1, "Jeff"),
    User(2, "Marco")
  ).map(u => u.id -> u).toMap

  def findUser(id: Int): AppCtx.To[Future[Option[User]]] = {
    Future(users.get(id))
  }
}
