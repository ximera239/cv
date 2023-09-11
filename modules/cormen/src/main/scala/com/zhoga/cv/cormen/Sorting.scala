package com.zhoga.cv.cormen

import scala.reflect.ClassTag

object Sorting {
  def insertionSort[T: ClassTag](values: Seq[T])(implicit ordering: Ordering[T]): Seq[T] = {
    val result = scala.collection.mutable.ArraySeq.from(values)
    for {
      i <- result.indices.drop(1) // start from second element
    } yield {
      val next = result(i) // remember next element (we will move sorted elements forward when required)
      ((i - 1) to (0, -1)).takeWhile { j =>
        if (ordering.compare(result(j), next) > 0) {
          result(j + 1) = result(j)
          true
        } else false
      }.lastOption.foreach { j =>
        result(j) = next
      }
    }
    result.toSeq
  }

  def mergeSort[T: ClassTag](values: Seq[T])(implicit ordering: Ordering[T]): Seq[T] = {
    def merge(
      left: scala.collection.mutable.ArraySeq[T],
      right: scala.collection.mutable.ArraySeq[T],
    ): scala.collection.mutable.ArraySeq[T] = {
      val result = scala.collection.mutable.ArraySeq.make(Array.ofDim[T](left.length + right.length))
      result.indices.foldLeft((0, 0)) {
        case ((leftIndex, rightIndex), i) if leftIndex == left.length =>
          result(i) = right(rightIndex)
          (leftIndex, rightIndex + 1)
        case ((leftIndex, rightIndex), i) if rightIndex == right.length =>
          result(i) = left(leftIndex)
          (leftIndex + 1, rightIndex)
        case ((leftIndex, rightIndex), i) =>
          if (ordering.compare(left(leftIndex), right(rightIndex)) > 0) {
            result(i) = right(rightIndex)
            (leftIndex, rightIndex + 1)
          } else {
            result(i) = left(leftIndex)
            (leftIndex + 1, rightIndex)
          }
      }
      result
    }

    def sort(seq: scala.collection.mutable.ArraySeq[T]): scala.collection.mutable.ArraySeq[T] = {
      if (seq.length < 2) seq
      else {
        val (left, right) = seq.splitAt(seq.length / 2)
        merge(sort(left), sort(right))
      }
    }

    sort(scala.collection.mutable.ArraySeq.from(values)).toSeq
  }

  def heapSort[T: ClassTag](values: Seq[T])(implicit ordering: Ordering[T]): Seq[T] = {
    // internally we use 1-based array indexes.

    // swap two elements in array
    def swap(seq: scala.collection.mutable.ArraySeq[T],  i: Int, j: Int): scala.collection.mutable.ArraySeq[T] = {
      val t = seq(i - 1)
      seq(i - 1) = seq(j - 1)
      seq(j - 1) = t
      seq
    }
    // get left child index if exist
    def left(seq: scala.collection.mutable.ArraySeq[T], i: Int, heapSize: Option[Int]): Option[Int] = {
      val length = heapSize.getOrElse(seq.length)
      Some(i * 2).filter(_ <= length)
    }

    // get right child index if exist
    def right(seq: scala.collection.mutable.ArraySeq[T], i: Int, heapSize: Option[Int]): Option[Int] = {
      val length = heapSize.getOrElse(seq.length)
      Some(i * 2 + 1).filter(_ <= length)
    }

    def maxHeapify(seq: scala.collection.mutable.ArraySeq[T], i: Int, heapSize: Option[Int]): scala.collection.mutable.ArraySeq[T] = {
      val maybeLeft = left(seq, i, heapSize)
      val maybeRight = right(seq, i, heapSize)
      val maybeLargestIndexToMove = {
        // left or i
        maybeLeft.filter(l => ordering.compare(seq(l - 1), seq(i - 1)) > 0).orElse(Some(i))
          // result compare with right
          .flatMap(l => maybeRight.filter(r => ordering.compare(seq(r - 1), seq(l - 1)) > 0).orElse(Some(l)))
          // filter
          .filter(_ != i)
      }

      maybeLargestIndexToMove.map {largest =>
        swap(seq, largest, i)
        maxHeapify(seq, largest, heapSize)
      }.getOrElse(seq)
    }

    def buildMaxHeap(seq: scala.collection.mutable.ArraySeq[T], heapSize: Option[Int]): scala.collection.mutable.ArraySeq[T] = {
      val length = heapSize.getOrElse(seq.length)
      ((length / 2) to (1, -1)).foldLeft(seq) {
        case (result, i) =>
          maxHeapify(result, i, heapSize = heapSize)
      }
      seq
    }

    val result = buildMaxHeap(scala.collection.mutable.ArraySeq.from(values), heapSize = None)
    (result.length to (2, -1)).foldLeft(result) {
      case (r, i) =>
        swap(r, 1, i)
        val result = maxHeapify(r, 1, Some(i - 1))
        result
    }.toSeq
  }

  def quickSort[T: ClassTag](values: Seq[T])(implicit ordering: Ordering[T]): Seq[T] = {
    // swap two elements in array
    def swap(seq: scala.collection.mutable.ArraySeq[T], i: Int, j: Int): scala.collection.mutable.ArraySeq[T] = {
      val t = seq(i)
      seq(i) = seq(j)
      seq(j) = t
      seq
    }

    def partition(seq: scala.collection.mutable.ArraySeq[T], p: Int, r: Int): Int = {
      val x = seq(r)

      val i = (p to (r - 1)).foldLeft(p - 1) {
        case (i, j) if ordering.compare(seq(j), x) <= 0 =>
          swap(seq, i + 1, j)
          i + 1
        case (i, _) => i
      }

      swap(seq, i + 1, r)
      i + 1
    }
    def sort(seq: scala.collection.mutable.ArraySeq[T], p: Int, r: Int): scala.collection.mutable.ArraySeq[T] = {
      if (p < r) {
        val q = partition(seq, p, r)
        sort(seq, p, q - 1)
        sort(seq, q + 1, r)
      }
      seq
    }

    sort(scala.collection.mutable.ArraySeq.from(values), 0, values.length - 1).toSeq
  }
}
