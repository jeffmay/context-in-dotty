import java.nio.file.Paths

import future.os.OS
import future.os.OS.OpensFile
import future.play.controllers._


object Main {

  def main(args: Array[String]): Unit = {
    println("Hello world!")
//    TestOS.run()
    TestPlay.run()
  }

}

object TestOS {

  def run(): Unit = {
    OS.closeAllOnFinally {
      // safe to open files without closing them inside this block
      val source = OS.openResource(Paths.get("example1.txt"))
      println(source.mkString)
      functionThatOpensResources()
    }

    // not safe to open files outside of the effect safe-zone
//     OS.openResource(Paths.get("example.txt")) // compile error
  }

  def functionThatOpensResources()(implicit effect: OpensFile): Unit = {
    // also safe to open files inside nested blocks, as long as the effect is carried along
    val example2 = OS.openResource(Paths.get("example2.txt"))
    println(example2.mkString)
  }
}

object TestPlay {
  import future.concurrent.SynchronousExecution._

  def run(): Unit = executeSingleThreaded {
    val app = new Application(new ActionToolkit, new Service)
    val goodRequest = Request("/fake", "Good Request", Map("Correlation-Id" -> "fakeCorrelationId"))
    val badRequest = Request("/fake")
    val exampleGood = app.example.handle(goodRequest).getOrThrow
    println(exampleGood)
    val exampleBad = app.example.handle(badRequest).getOrThrow
    println(exampleBad)


    val findUserGood = app.findUser(1).handle(goodRequest).getOrThrow
    println(findUserGood)
    val userNotFound = app.findUser(0).handle(goodRequest).getOrThrow
    println(userNotFound)
    val findUserBad = app.findUser(1).handle(badRequest).getOrThrow
    println(findUserBad)
  }
}
