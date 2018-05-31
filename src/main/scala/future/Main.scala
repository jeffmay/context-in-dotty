package future

import java.nio.file.Paths

import scala.concurrent.{ExecutionContext, Future}

import future.concurrent.SynchronousExecution._
import future.concurrent.ExecuteOnCallingThread
import future.play.api._
import future.play.models.User
import future.play.services.{SvcCtx, RoleService, UserService}
import future.play.controllers.{Application, AppActionToolkit, CtrlCtx}

object Main {
  def main(args: Array[String]): Unit = {
    println("Hello world!")
    TestPlay.run()
  }
}

object TestPlay {
  def run(): Unit = {
    val sctx = SvcCtx(ExecuteOnCallingThread)
    val userSvc = new UserService(sctx)
    val roleSvc = new RoleService(sctx)
    val cctx = CtrlCtx(new AppActionToolkit(roleSvc, userSvc), ExecuteOnCallingThread)
    val app = new Application(cctx, userSvc)
    val goodRequest = Request("GET", "/fake", "Good Request", Map("Authorization" -> "1"))
    val badRequest = Request("GET", "/fake")
    val example = app.example.handle(badRequest).!
    println(s"Expected 200 (example): ${example.status}")

    val findUserGood = app.findUser(1).handle(goodRequest).!
    println(s"Expected 200 (findUser): ${findUserGood.status}")
    val userNotFound = app.findUser(0).handle(goodRequest).!
    println(s"Expected 404 (findUser): ${userNotFound.status}")
    val findUserBad = app.findUser(1).handle(badRequest).!
    println(s"Expected 500 (findUser): ${findUserBad.status}")
  }
}
