package future.os

import java.io.{Closeable, FileInputStream, InputStream}
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

import scala.io.{Codec, Source}
import scala.util.control.NonFatal

object OS {

  class OpensFile private[OS] extends CloseableRegistry

  trait CloseableRegistry extends Closeable {
    private val lastIdx: AtomicInteger = new AtomicInteger(0)
    private var openHandles: Map[Int, Closeable] = Map.empty

    def register(handle: Closeable): Int = {
      val idx = lastIdx.incrementAndGet()
      openHandles += idx -> handle
      println(s"Registered #$idx")
      idx
    }

    def unregister(idx: Int): Unit = {
      openHandles = openHandles - idx
      println(s"Unregistered #$idx")
    }

    override def close(): Unit = {
      println("Closing everything...")
      openHandles.foreach { (idx, handle) =>
        println(s"Closing #$idx...")
        try handle.close()
        catch {
          case NonFatal(ex) =>
            println(s"Encountered on close: $ex")
        }
      }
    }
  }

  def openResource(path: Path)(implicit effect: OpensFile, codec: Codec): Source = {
    val classLoader = Thread.currentThread().getContextClassLoader
    val inputStream = classLoader.getResourceAsStream(path.toString)
    createBufferedSource(inputStream, reset = () => openResource(path))
      .withDescription(s"resource:$path")
  }

  def openFile(path: Path)(implicit effect: OpensFile, codec: Codec): Source = {
    val inputStream = new FileInputStream(path.toFile)
    createBufferedSource(inputStream, reset = () => openFile(path))
      .withDescription(s"file:${path.toAbsolutePath}")
  }

  private def createBufferedSource(inputStream: InputStream, reset: () => Source)
    (implicit effect: OpensFile, codec: Codec): Source = {
    val source = Source.createBufferedSource(inputStream)
    val idx = effect.register(source)
    source
      .withReset(reset)
      .withClose(() => {
        inputStream.close()
        effect.unregister(idx)
      })
  }

  def closeAllOnFinally[T](block: ImplicitFunction1[OpensFile, T]): T = {
    implicit val effect: OpensFile = new OpensFile
    try block
    finally effect.close()
  }
}
