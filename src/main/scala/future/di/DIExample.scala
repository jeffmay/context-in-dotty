package future.di

import future.di.DIExample.ImplicitFunction.DelayContext2

/**
  * Dependency injection in Scala can take many forms:
  *
  * - Java dependency injection with constructor injection + Guice
  * - Thin-cake pattern with traits, possibly helped using macwire
  * - Composeable module pattern with module classes that are injected into each other
  * - Parfeit pattern with a module that you pass around as an implicit argument
  * - Reader Monad to combine dependencies of each monadic operation
  * - Implicit parameters at the call-site (ie. [[scala.concurrent.ExecutionContext]])
  *
  * The problem with Guice is that it fails at runtime and is slower than compile-time injection and has
  * all of the problems of the thin-cake pattern and composeable modules pattern (it's really the same
  * as this one, but it is converted into a runtime map).
  *
  * The problem with thin-cake pattern is that you run into namespace collisions and implicit collisions
  * and awkwardly large module objects without much of an easy way to compose them. They make binary
  * compatibility of the DI code difficult. The other problem with this kind of DI is that once you wire
  * up an object, you can't change the dependencies with which it was wired.
  *
  * The problem with the module pattern is similar to the thin-cake pattern but without the namespace
  * pollution issues. They are maybe a little bit more of a composeable pattern in that you can inject modules
  * into other modules. Binary compatibility is less of a concern because you can hide information inside the
  * module classes. However, you still have to have a dependency on the library that publishes the module.
  *
  * The parfeit pattern has the benefits and drawbacks of the above module patterns, except that you can
  * customize the dependencies of functions that use this pattern at the call-site.
  *
  * The problem with the Reader monad is that it declares that dependencies must be provided in a specific
  * order. In order to combine dependencies from other projects you have to compose product types and
  * decompose co-product types in the correct order to avoid compiler errors. It is very rigid and requires
  * a lot of implicit glue.
  *
  * Implicit parameters at the call-site flip the problem on its head. Any change to the call-site's dependencies requires
  * a change to the method signature of the call-site. This makes binary compatibility difficult. However, since
  * you are only relying on built-in Scala language features, you don't have to incur a library dependency cost.
  * Another major downside is that you have to put these implicit parameter lists everywhere and there is no
  * way in Scala 2 to abstract over them. You can solve many of these issues using the parfeit pattern.
  *
  * Dotty introduces another way to handle this:
  *
  *    Implicit Functions
  *
  * This still has the same downsides of binary compatibility at the call-site as implicit parameters, however
  * you no longer incur the cost of repeating these implicit argument lists everywhere.
  *
  * Since `implicit` is now included in the type, you can use *normal type-level abstraction techniques to
  * wrap the type into an shorter alias to use throughout the code.
  *
  * If you combine this with the parfeit pattern, you can have complete control over the binary compatibility
  * and extensibility of your functions. In other words, functions can depend on a module class that is used
  * to hide changes in dependencies. This makes them customizable at the call-site without the cost of binary
  * compatibility. Additionally, you can abstract these modules they depend on using implicit function aliases.
  * This makes the process of implementing and maintaining this code a lot easier than maintaining implicit
  * parameter lists.
  *
  * Lastly, once some of the syntactic issues regarding implicit parameter lists are cleaned up, you will no
  * longer run into the pain points of this pattern when you get to more complex situations.
  */
object DIExample {

  type IF1[-A, +R] = ImplicitFunction1[A, R]
  type IF2[-A, -B, +R] = ImplicitFunction2[A, B, R]

  // Doesn't work, but interesting example
  implicit class ImplicitFunction1Ops[-A, +R](f1: => IF1[A, R]) {
    def andThenWithContext[B, BR](f2: R => IF1[B, BR]): IF2[A, B, BR] = f2(f1)
  }

  object ImplicitFunction {

    // Playing around with different ways to compose implicit functions...
    // In the end, you typically don't need this stuff because creating implicit functions
    // is incredibly easy, you don't even have to reference the arguments by name. All you
    // have to do is declare the correct type on the left-hand side.

    def zip[I1, A, I2, B](f1: IF1[I1, A], f2: IF1[I2, B]): IF2[I1, I2, (A, B)] = (f1, f2)

    def chain[I1, A, I2, B](f1: IF1[I1, A], f2: A => IF1[I2, B]): IF2[I1, I2, B] = f2(f1)

    def delayContext[I1, I2] = new DelayContext2[I1, I2]

    class DelayContext2[I1, I2] {

      def capture[A](fa: IF2[I1, I2, A])(implicit i1: => I1, i2: => I2): () => A = {
        () => fa
      }
    }

  }

  class Session
  class Connection
  object Connection {
    def fromSession(implicit session: Session): Connection = new Connection
  }

  case class User(id: String)
  case class Data(value: String)

  def callDatabase(userId: String): IF1[Session, User] = User(userId)
  def callWebServer(user: User): IF1[Connection, Data] = Data(s"About user ${user.id}")

  // TODO: How to combine things with same implicit argument types in different orders?
  // Turns out isn't much of a need for this... if you have all the implicit arguments
  // available in scope, then you can call any implicit function with a subset of the
  // implicit arguments required.

  // This works, but is slightly unnecessary
  val f: IF2[Session, Connection, Data] = ImplicitFunction.chain(callDatabase("Jeff"), user => callWebServer(user))
  // This doesn't work, and with good reason... it's much easier to call the function f2
  // val f: IF2[Session, Connection, Data] = callDatabase("1").andThenWithContext(user => callWebServer(user))
  val f2: IF2[Session, Connection, Data] = callWebServer(callDatabase("Jeff"))
  val h: IF2[Session, Connection, (User, Data)] = ImplicitFunction.zip(callDatabase("1"), callWebServer(User("Bob")))

  // A method that captures its current implicit scope
  def j(implicit c: Connection, s: Session): () => User = {
    ImplicitFunction.delayContext[Connection, Session].capture(callDatabase("2"))
  }

  // An implicit function whose implicit argument is derived from another implicit function
  def callDatabaseWithSession(userId: String): IF1[Session, User] = {
    implicit val conn: Connection = Connection.fromSession
    callDatabase(userId)
  }

  def main(args: Array[String]): Unit = {
    var hash1: Int = 0
    var hash2: Int = 0
    var capturedHash: Int = 0

    val delayedContext: DelayContext2[Connection, Session] = locally {
      implicit val c1: Connection = new Connection
      implicit val s1: Session = new Session

      hash1 = c1.##
      println(s"hash1 = $hash1")

      val fResult = f
      println(fResult)
      val hResult = h
      println(hResult)

      ImplicitFunction.delayContext[Connection, Session]
    }

    val closure = locally {
      implicit val c2: Connection = new Connection
      implicit val s2: Session = new Session

      hash2 = c2.##
      println(s"hash2 = $hash2")

      delayedContext.capture {
        capturedHash = implicitly[Connection].##
        println(s"capturedHash = $capturedHash")
        callDatabase("2")
      }
    }

    val result = closure()

    assert(hash1 != 0)
    assert(hash2 != 0)
    assert(hash1 != hash2)
    assert(capturedHash == hash2)
  }
}
