package com.zhoga.cv.problems

import scala.reflect.ClassTag

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

  private[Stack] case class NumericStack[T: Numeric](elements: List[T], sum: T, count: Int) extends Stack[T] {
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

  trait =!=[A, B]
  implicit def neq[A, B]: A =!= B = null
  implicit def neqAmbig1[A: Numeric]: A =!= Numeric[A] = null
  implicit def neqAmbig2[A: Numeric]: A =!= Numeric[A] = null

  trait NumericEvidence[A] {
    def getEvidence(): Option[Numeric[A]]
  }
  object NumericEvidence {
    implicit def getNumericEvidence[T](implicit ev: Numeric[T]): NumericEvidence[T] = () => Some(ev)
    implicit def noneEvidence[T: ClassTag](implicit ev: T =!= Numeric[T]): NumericEvidence[T] = () => None
  }

  def empty[T: NumericEvidence]: Stack[T] = implicitly[NumericEvidence[T]]
    .getEvidence()
    .fold[Stack[T]](new GeneralStack[T](Nil)) { implicit ev => new NumericStack[T](Nil, ev.zero, 0) }

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
}
