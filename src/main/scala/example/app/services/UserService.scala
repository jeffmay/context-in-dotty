package example.app.services

import example.app.context.{AuthCtx}
import example.app.models.{Authorization, Role, User}
import example.database.context.DBCtx
import example.database.collections.UserCollection
import example.app.logging.AppLogger

import scala.util.Success
import scala.concurrent.{ExecutionContext, Future}

import future.concurrent.ImplicitExecutionContext

class UserService(
  users: UserCollection,
  logger: AppLogger,
  ec: ExecutionContext
) extends ImplicitExecutionContext(ec) {

  def findUser(id: Int): AuthCtx.To[Future[Option[User]]] = {
    users.findById(id).andThen {
      case Success(Some(user)) =>
        logger.info(s"Found user ${user}")
      case _ =>
        logger.info(s"Could not find user with id=${id}")
    }
  }
}
