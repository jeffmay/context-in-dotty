package example.database.collections

import example.database.context.DBCtx
import example.app.models.{User, Role}

import future.concurrent.ImplicitExecutionContext

import scala.concurrent.{ExecutionContext, Future}

trait UserCollection {

  def findById(id: Int): DBCtx.Async[Option[User]]
}

case class InMemoryUserCollection(users: Map[Int, User]) extends UserCollection {

  override def findById(id: Int): DBCtx.Async[Option[User]] = {
    Future.successful(users.get(id))
  }

}

object UserCollection {
  val example: InMemoryUserCollection = new InMemoryUserCollection(Seq(
    User(1, "Jeff", Set(Role("admin"))),
    User(2, "Marco", Set(Role("member")))
  ).map(u => u.id -> u).toMap)
}
