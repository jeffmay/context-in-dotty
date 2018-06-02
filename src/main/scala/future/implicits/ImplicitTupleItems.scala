package future.implicits

object ImplicitTupleItems extends ImplicitTupleItems
trait ImplicitTupleItems {

  // implicit def tuple2To1[A](implicit t: Tuple2[A, _]): A = t._1
  // implicit def tuple2To2[B](implicit t: Tuple2[_, B]): B = t._2

  // implicit def tuple3To1[A](implicit t: Tuple3[A, _, _]): A = t._1
  // implicit def tuple3To2[B](implicit t: Tuple3[_, B, _]): B = t._2
  // implicit def tuple3To3[C](implicit t: Tuple3[_, _, C]): C = t._3

  // implicit def tuple4To1[A](implicit t: Tuple4[A, _, _, _]): A = t._1
  // implicit def tuple4To2[B](implicit t: Tuple4[_, B, _, _]): B = t._2
  // implicit def tuple4To3[C](implicit t: Tuple4[_, _, C, _]): C = t._3
  // implicit def tuple4To4[D](implicit t: Tuple4[_, _, _, D]): D = t._4

  // implicit def tuple5To1[A](implicit t: Tuple5[A, _, _, _, _]): A = t._1
  // implicit def tuple5To2[B](implicit t: Tuple5[_, B, _, _, _]): B = t._2
  // implicit def tuple5To3[C](implicit t: Tuple5[_, _, C, _, _]): C = t._3
  // implicit def tuple5To4[D](implicit t: Tuple5[_, _, _, D, _]): D = t._4
  // implicit def tuple5To5[E](implicit t: Tuple5[_, _, _, _, E]): E = t._5
}
