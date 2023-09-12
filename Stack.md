# Stack numeric and non-numeric

[Original problem](https://ru.stackoverflow.com/q/1278314/417043)

At first glance it looks obvious. If we need `average` for numeric, then we just need to write extension method like:

```scala
implicit class  NumericStackWrapper[T: Numeric](stack: Stack[T]) {
  def average: T = ???
}
```

But we do not have division for Numeric, instead we have `div` for `Fractional` and `quot` for `Integral`. I.e. we need two methods

```scala
implicit class NumericFraStack[T: Fractional](base: Stack[T]) {
  def average: T = {
    base match {
      case NumericStack(_, sum, count) =>
        val c = implicitly[Fractional[T]].fromInt(count)
        implicitly[Fractional[T]].div(sum, c)
      case _ =>
        throw new Exception("Wrong type")
    }
  }

implicit class NumericIntStack[T: Integral](base: Stack[T]) {
  def average: T = {
    base match {
      case NumericStack(_, sum, count) =>
        val c = implicitly[Integral[T]].fromInt(count)
        implicitly[Integral[T]].quot(sum, c)
      case _ =>
        throw new Exception("Wrong type")
    }
  }
}
```

As you can see, we're going to keep data for providing `average` in Stack implementation class (we will have two fields, sum and length). Also, we will have two implementations, one is for numeric types, and another for non-numeric:

```scala
sealed trait Stack[T] {
  def push(element: T): Stack[T]
  def pop(): (Stack[T], T)
}
object Stack {
  private[Stack] case class GeneralStack[T](elements: List[T]) extends Stack[T] {
    def push(element: T): GeneralStack[T] = {
      new GeneralStack[T](element :: elements)
    }

    def pop(): (GeneralStack[T], T) = elements match {
      case head :: tail =>
        new GeneralStack[T](tail) -> head
      case _ => throw new Exception("Stack is empty")
    }
  }

  private[Stack] case class NumericStack[T: Numeric] (elements: List[T], sum: T, count: Int) extends Stack[T] {
    def push(element: T): NumericStack[T] = {
      val newSum = implicitly[Numeric[T]].plus(sum, element)
      new NumericStack[T](element :: elements, newSum, count + 1)
    }

    def pop(): (NumericStack[T], T) = elements match {
      case head :: tail =>
        val newSum = implicitly[Numeric[T]].minus(sum, head)
        new NumericStack[T](tail, newSum, count - 1) -> head
      case _ => throw new Exception("Stack is empty")
    }
  }
}
```

But now we want to have nice stack construction. Like:

```scala
Stack.empty[Int].push(1).push(2).push(5).average
Stack.empty[Double].push(1).push(2).push(6).average
Stack.empty[String].push("1").push("2")
Stack.empty[Any].push("1").push(2)
```

so, that we cannot call `.average` on lines 3 and 4 because of compile-time error. This also solve problem with incorrect Stack init (when we create `GeneralStack[Int]`).
So, `Stack.empty[T]` should be able to use correct implementation depending on parameter type. It is easy for `Numeric`. but what to do with non-numeric? Well, for that purpose we will use ambiguous implicits trick:

1. We define type for "not equals".

```scala
trait =!=[A, B]
implicit def neq[A, B] : A =!= B = null
implicit def neqAmbig1[A: Numeric]: A =!= Numeric[A] = null
implicit def neqAmbig2[A: Numeric]: A =!= Numeric[A] = null
```

Here, when we have some not numeric type, method `neq` will handle them. But in case of `Numeric`, we will have ambiguous implicits (`neqAmbig1` amd `neqAmbig2`)

Then we need a `trait` from which we can get `Numeric[T]` instance, when it is available:

```scala
trait NumericEvidence[A] {
  def getEvidence(): Option[Numeric[A]]
}
object NumericEvidence {
  implicit def getNumericEvidence[T](implicit ev: Numeric[T]): NumericEvidence[T] = () => Some(ev)
  implicit def noneEvidence[T: ClassTag](implicit ev: T =!= Numeric[T]): NumericEvidence[T] = () => None
}
```

and then we can define our `empty` method:

```scala
def empty[T: NumericEvidence]: Stack[T] = implicitly[NumericEvidence[T]].getEvidence()
  .fold[Stack[T]](new GeneralStack[T](Nil)){implicit ev =>
    new NumericStack[T](Nil, ev.zero, 0)
  }
```


Full version can be found [here](https://github.com/ximera239/cv/blob/main/modules/core/src/main/scala/com/zhoga/cv/problems/Stack.scala)

-----------------

When I shared this problem and approach to colleagues, I was immediately challenged to re-write it in scala3. Well, after some time i wrote this (it was written short after release of scala 3.0):

```scala 3
import scala.util.NotGiven
import scala.language.implicitConversions

sealed trait Stack[T]:
  def push(element: T): Stack[T]
  def pop(): (Stack[T], T)

object Stack:
  private[Stack] case class GeneralStack[T](elements: List[T]) extends Stack[T]:
    def push(element: T): GeneralStack[T] = GeneralStack[T](element :: elements)

    def pop(): (GeneralStack[T], T) = elements match
      case head :: tail => GeneralStack[T](tail) -> head
      case _ => throw Exception("Stack is empty")

  private[Stack] case class NumericStack[T: Numeric](elements: List[T], sum: T, count: Int) extends Stack[T]:
    def push(element: T): NumericStack[T] =
      val newSum = summon[Numeric[T]].plus(sum, element)
      NumericStack[T](element :: elements, newSum, count + 1)

    def pop(): (NumericStack[T], T) = elements match
      case head :: tail =>
        val newSum = implicitly[Numeric[T]].minus(sum, head)
        new NumericStack[T](tail, newSum, count - 1) -> head
      case _ => throw Exception("Stack is empty")

  trait AvgExtension[T]:
    def average: T
  class FraStackAvgExtension[T: Fractional](stack: NumericStack[T]) extends AvgExtension[T]:
    def average: T = summon[Fractional[T]].div(stack.sum, summon[Fractional[T]].fromInt(stack.count))
  class IntStackAvgExtension[T: Integral](stack: NumericStack[T]) extends AvgExtension[T]:
    def average: T = summon[Integral[T]].quot(stack.sum, summon[Integral[T]].fromInt(stack.count))

  given [T](using Integral[T]): Conversion[Stack[T], IntStackAvgExtension[T]] = new Conversion[Stack[T], IntStackAvgExtension[T]] {
    def apply(base: Stack[T]): IntStackAvgExtension[T] = base match
      case ns: NumericStack[T] => new IntStackAvgExtension(ns)
      case _ => throw new Exception("Wrong type")
  }

  given [T](using Fractional[T]): Conversion[Stack[T], FraStackAvgExtension[T]] = new Conversion[Stack[T], FraStackAvgExtension[T]] {
    def apply(base: Stack[T]): FraStackAvgExtension[T] = base match
      case ns: NumericStack[T] => new FraStackAvgExtension(ns)
      case _ => throw new Exception("Wrong type")
  }

  trait NumericEvidence[A]:
    def getEvidence(): Option[Numeric[A]]

  object NumericEvidence:
    given numericEvidence[T](using ev: Numeric[T]): NumericEvidence[T] = () => Some(ev)
    given noneEvidence[T](using NotGiven[Numeric[T]]): NumericEvidence[T] = () => None

  def empty[T](using NumericEvidence[T]): Stack[T] = summon[NumericEvidence[T]]
    .getEvidence()
    .fold[Stack[T]](GeneralStack[T](Nil)) { ev => new NumericStack[T](Nil, ev.zero, 0)(using ev) }
```