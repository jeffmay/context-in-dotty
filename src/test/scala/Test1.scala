import org.junit.Test
import org.junit.Assert._

class Test1 {
  @Test def t1(): Unit = {
    assert("I was compiled by dotty :)".nonEmpty)
  }
}