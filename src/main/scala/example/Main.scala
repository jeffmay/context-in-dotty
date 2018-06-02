package example.app

import java.nio.file.Paths

import scala.concurrent.{ExecutionContext, Future}

import future.concurrent.SynchronousExecution._
import future.concurrent.ExecuteOnCallingThread
import future.play.api._
import example.app.context.RootCtx
import example.app.models.User
import example.app.modules._
import example.app.services.{RoleService, UserService}
import example.app.controllers._
import example.database.collections.UserCollection

object Main {
  def main(args: Array[String]): Unit = {
    println("Hello world!")
    TestPlay.run()
  }
}

object TestPlay {
  def run(): Unit = {
    val goodRequest = Request("GET", "/fake", "Good Request",
      Map(
        "Authorization" -> "1",
        "UserId" -> "3"
      )
    )
    val badRequest = Request("GET", "/fake")
    val execution = new SingleExecutionModule(ExecuteOnCallingThread)
    val database = new DatabaseModule
    val services = new ServiceModule(database, execution)
    val controllers = new ControllerModule(
      ActionBuilder(ExecuteOnCallingThread),
      execution,
      services
    )
    import controllers._

    val result1 = exampleCtrl.returnOk.handle(goodRequest).!
    println(s"ok result: ${result1.status}")

    val result2 = exampleCtrl.authenticated.handle(goodRequest).!
    println(s"authenticated result: ${result2.status}")

    val result3 = exampleCtrl.responder.handle(goodRequest).!
    println(s"responder result: ${result3.body}")

    val result4 = exampleCtrl.findUser.handle(goodRequest).!
    println(s"findUser response: ${result4}")

    val result5 = exampleCtrl.logging.handle(goodRequest).!
    println(s"logging response: ${result5}")
  }
}
