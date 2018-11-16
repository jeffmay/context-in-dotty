package example

import scala.concurrent.{ExecutionContext, Future}

import future.concurrent.SynchronousExecution._
import future.concurrent.ExecuteOnCallingThread
import future.play.api._
import example.app.modules._

object Main {
  def main(args: Array[String]): Unit = {
    val app = new ServerModule(ExecuteOnCallingThread)
    import app.controllers._

    val goodRequest = Request("GET", "/fake", "Good Request")
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
