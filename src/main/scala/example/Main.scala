package example

import scala.concurrent.{ExecutionContext, Future}

import future.concurrent.SynchronousExecution._
import future.concurrent.ExecuteOnCallingThread
import future.play.api._
import example.app.modules._

object Main {
  def main(args: Array[String]): Unit = {
    val app = new ServerModule(ExecuteOnCallingThread)
  }
}
