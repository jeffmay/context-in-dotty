package example

import scala.concurrent._
import scala.concurrent.duration._

// object FreeArguments {
//   // Fixing the arguments, freeing the execution context
//   type Fn = (Int, Int) => Future[Int]

//   def explicitParam(ec: ExecutionContext, x: Int, y: Int): Future[Int] = Future(x + y)(ec)

//   def curriedParam(ec: ExecutionContext)(x: Int, y: Int): Future[Int] = Future(x + y)(ec)

//   def functionParam(ec: ExecutionContext): Fn = (x, y) => Future(x + y)(ec)

//   def moduleParam(ec: ExecutionContext) = new ModuleParam(ec)
//   class ModuleParam(ec: ExecutionContext) {
//     def subtract(x: Int, y: Int): Future[Int] = Future(x - y)(ec)
//     def add(x: Int, y: Int): Future[Int] = Future(x + y)(ec)
//   }

//   def main(args: Array[String]): Unit = {
//     val ec = ExecutionContext.global
//     // explicit parameter
//     explicitParam(ec, 1, 1)
//     val fn1: Fn = explicitParam(ec, _, _)
//     // parameterized function (currying)
//     val add = curriedParam(ec)
//     add(2, 2)
//     val fn2: Fn = add
//     // parameterized module
//     val mod = moduleParam(ec)
//     val fn3: Fn = mod.add
//   }
// }

// // trait Monad[F[_]] {
// //   def pure[A](a: A): F[A]
// //   def map[A, B](m: F[A], f: A => B): F[B]
// //   def flatMap[A, B](m: F[A], f: A => F[B]): F[B]
// // }
// // object Monad {
// //   inline def apply[F[_]](implicit m: Monad[F]): Monad[F] = m

// //   implicit def FutureMonad(implicit ec: ExecutionContext): Monad[Future] = new Monad[Future] {
// //     final override def pure[A](a: A): Future[A] = Future.successful(a)
// //     final override def map[A, B](m: Future[A], f: A => B): Future[B] = m.map(f)
// //     final override def flatMap[A, B](m: Future[A], f: A => Future[B]): Future[B] = m.flatMap(f)
// //   }
// // }


object FreeContext {

  // The control (almost all languages support this parameterization)
  def explicitParam(x: Int, y: Int, ec: ExecutionContext): Future[Int] = Future(x + y)(ec)

  def curriedParam(x: Int, y: Int)(ec: ExecutionContext): Future[Int] = Future(x + y)(ec)

  def module(x: Int, y: Int) = new ModuleParam(x, y)
  class ModuleParam(x: Int, y: Int) {
    def add(ec: ExecutionContext): Future[Int] = Future(x + y)(ec)
    def subtract(ec: ExecutionContext): Future[Int] = Future(x - y)(ec)
  }

  // Fixing the arguments, freeing the execution context //
  type Fn = ExecutionContext => Future[Int]
  def functionParam(x: Int, y: Int): Fn = {
    ec => Future(x + y)(ec) // declares the argument as implicit
  }

  def main(args: Array[String]): Unit = {
    import ExecutionContext.global
    // explicit parameter
    explicitParam(1, 1, global)
    val explicitFn: Fn = explicitParam(1, 1, _)
    // parameterized function (currying)
    val add = curriedParam(2, 2)
    add(global)
    val curriedFn: Fn = add
    // parameterized module
    val mod = module(3, 3)
    mod.add(global)
    val moduleFn: Fn = mod.add
    val result = module(3, 3).subtract(global)
  }
}

object ImplicitFreeContext {

  // Scala 2
  def curriedParam(x: Int, y: Int)(implicit ec: ExecutionContext): Future[Int] = 
    Future(x + y) // context is passed implicitly

  def module(x: Int, y: Int) = new ModuleParam(x, y)
  class ModuleParam(x: Int, y: Int) {
    def add(implicit ec: ExecutionContext): Future[Int] = Future(x + y)
    def subtract(implicit ec: ExecutionContext): Future[Int] = Future(x - y)
  }

  // New in Scala 3
  type Fn = implicit ExecutionContext => Future[Int]
  def functionParam(x: Int, y: Int): Fn =
    Future(x + y) // no need to declare the argument as implicit... or at all

  def main(args: Array[String]): Unit = {
    import ExecutionContext.Implicits.global
    // implicit parameter (curried)
    curriedParam(1, 1)
    val curriedExplicitFn: FreeContext.Fn = curriedParam(1, 1)
    val curriedImplicitFn: Fn = curriedParam(1, 1)
    // parameterized function
    functionParam(2, 2)
    // note: removing underscore below crashes the compiler
    val explicitAdd: FreeContext.Fn = functionParam(2, 2)(_)
    val implicitAdd: Fn = functionParam(2, 2)
    explicitAdd(global)
    implicitAdd
    // parameterized module
    module(3, 3).add
    // note: removing underscore below crashes the compiler
    val modExplicitAdd: FreeContext.Fn = module(3, 3).add(_)
    val modImplicitAdd: Fn = module(3, 4).add
  }
}

object ImplicitParamContext {

  def add(x: Int, y: Int)(implicit ec: ExecutionContext): Future[Int] = Future(x + y)
  def reportResult(userId: Int, result: Int)(implicit ec: ExecutionContext, iot: IoT): Future[Unit] = {
    Future(iot.report(userId, s"Successfully calculated result=$result"))
  }

  def main(args: Array[String]): Unit = {
    val readRes: (ExecutionContext, IoT) => Future[Int] = { (ec, iot) =>
      implicit val implicitEC: ExecutionContext = ec
      implicit val implicitIoT: IoT = iot
      for {
        res1 <- add(1, 1)
        res2 <- add(2, 2)
        result = res1 + res2
        _ <- reportResult(1, result)
      } yield result
    }
    val result: Future[Int] = {
      import ExecutionContext.Implicits.global
      val iot = new IoT
      readRes(global, iot)
    }
  }
}

case class Reader[I, O](f: I => O) {
  def map[A](g: O => A): Reader[I, A] = Reader(in => g(f(in)))
  def flatMap[A](g: O => Reader[I, A]): Reader[I, A] = Reader(in => g(this.f(in)).f(in))
}

class IoT {
  def report(userId: Int, message: String): Unit = {
    println(message)
  }
}

object SimpleMonadContext {
  type AsyncOp[T] = Reader[ExecutionContext, Future[T]]
  def async[T](block: ExecutionContext => Future[T]): AsyncOp[T] = Reader(block)

  def add(x: Int, y: Int): AsyncOp[Int] = Reader { implicit ec => Future(x + y) }

  def main(args: Array[String]): Unit = {
    val readRes: AsyncOp[Int] = for {
      op1 <- add(1, 1)
      op2 <- add(2, 2)
      result <- async { implicit ec =>
        for {
          res1 <- op1
          res2 <- op2
        } yield res1 + res2
      }
    } yield result
    val result: Future[Int] = {
      import ExecutionContext.Implicits.global
      readRes.f(global)
    }
  }
}

object ComplexMonadContext {
  type AsyncOp[T] = Reader[(ExecutionContext, IoT), Future[T]]
  def async[T](block: (ExecutionContext, IoT) => Future[T]): AsyncOp[T] = Reader(block.tupled)

  def add(x: Int, y: Int): AsyncOp[Int] = Reader { (ec, iot) => Future(x + y)(ec) }
  def reportResult(userId: Int, result: Int): AsyncOp[Unit] = Reader { (ec, iot) =>
    Future {
      iot.report(userId, s"Successfully calculated result=$result")
    } (ec)
  }

  def main(args: Array[String]): Unit = {
    val readRes: AsyncOp[Int] = for {
      op1 <- add(1, 1)
      op2 <- add(2, 2)
      result <- async { (ec: ExecutionContext, iot: IoT) =>
        implicit val ctx: ExecutionContext = ec
        for {
          res1 <- op1
          res2 <- op2
        } yield res1 + res2
      }
      _ <- async { (ec: ExecutionContext, iot: IoT) => 
        result.flatMap(res => reportResult(1, res).f((ec, iot)))(ec)
      }
    } yield result
    val result: Future[Int] = {
      import ExecutionContext.Implicits.global
      val iot = new IoT
      readRes.f((global, iot))
    }
  }
}

object FunctionContext {

  def add(x: Int, y: Int): ExecutionContext => Future[Int] = { implicit ec => Future(x + y) }
  def reportResult(userId: Int, result: Int): (ExecutionContext, IoT) => Future[Unit] = { (ec, iot) =>
    Future(iot.report(userId, s"Successfully calculated result=$result"))(ec)
  }

  def main(args: Array[String]): Unit = {
    val readRes: (ExecutionContext, IoT) => Future[Int] = { (ec, iot) =>
      implicit val ctx: ExecutionContext = ec
      for {
        res1 <- add(1, 1)(ec)
        res2 <- add(2, 2)(ec)
        result = res1 + res2
        _ <- reportResult(1, result)(ec, iot)
      } yield result
    }
    val result: Future[Int] = {
      import ExecutionContext.Implicits.global
      val iot = new IoT
      readRes(global, iot)
    }
  }
}

object ImplicitFunctionContext {
  type AsyncCtx[T] = implicit ExecutionContext => Future[T]
  type IotCtx[T] = implicit IoT => T

  def add(x: Int, y: Int): AsyncCtx[Int] = Future(x + y)
  def reportResult(userId: Int, result: Int): AsyncCtx[IotCtx[Unit]] = {
    Future {
      implicitly[IoT].report(userId, s"Successfully calculated result=$result")
    }
  }

  def main(args: Array[String]): Unit = {
    val readRes: IotCtx[AsyncCtx[Int]] = {
      val op1 = add(1, 1)
      val op2 = add(2, 2)
      val result = for {
        res1 <- op1
        res2 <- op2
      } yield res1 + res2
      result.map(reportResult(1, _)) // implicit arguments swapped
      result
    }
    val result: Future[Int] = {
      import ExecutionContext.Implicits.global
      implicit val iot: IoT = new IoT
      readRes
    }
  }
}

object ImplicitCurriedFunction {
  class Transaction(val name: String)
  type Txn[T] = implicit Transaction => T
  type Async[T] = implicit ExecutionContext => T

  def main(args: Array[String]): Unit = {
    val op1: Async[Txn[Int]] = {
      println(implicitly[Transaction].name)
      println(implicitly[ExecutionContext].getClass)
      1
    }
    val op2: implicit Transaction => implicit ExecutionContext => Int = 2
    val op3: implicit ExecutionContext => implicit Transaction => Int = 3

    implicit val txn: Transaction = new Transaction("main transaction")
    implicit val ec: ExecutionContext = ExecutionContext.global
    op1
    op2
    op3
  }
}

