package com.zhoga.cv.problems.names

import com.zhoga.cv.problems.names.Utils.Id

import java.io.{File, InputStream}
import scala.collection.mutable
import scala.io.Source

object NamesApproach2 extends NamesApproach[Id] {
  override def compute(file: File): Long =
    run(Utils.sourceIterator(Source.fromFile(file)))

  def compute(is: InputStream): Long = {
    run(
      Utils.sourceIterator(
        Source
          .fromInputStream(is)
      )
    )
  }

  private[problems] def run(s: Iterator[Array[Byte]]): Long = {
    def process(offset: Int, prefixSum: Long, s: Iterator[Array[Byte]]): Long = {
      val buckets = s.foldLeft(Utils.bucketsByLetter()) { case (a, word) =>
        val firstLetter = word(0)
        a(firstLetter).incAndAddIfNonEmpty(word.drop(1))
        a
      }

      buckets
        .zipWithIndex
        .foldLeft((offset, 0L)) { case ((offset, sum), (tc, index)) =>
          val sumWithoutTail =
            (prefixSum + index + 1) * (offset + 1 + offset + tc.getCountWithoutTail) * tc.getCountWithoutTail / 2

          val nextPrefixSum = prefixSum + index + 1
          val nextOffset = offset + tc.getCountWithoutTail
          (
            offset + tc.getCount,
            sum + tc.iterator().fold(sumWithoutTail) { iterator =>
              sumWithoutTail + process(nextOffset, nextPrefixSum, iterator)
            }
          )
        }
        ._2
    }

    process(offset = 0, prefixSum = 0L, s)
  }
}

trait IteratorBuilder {
  def addIfNonEmpty(element: Array[Byte]): Boolean
  def build(): Option[Iterator[Array[Byte]]]
}

object IteratorBuilder {
  def create(): IteratorBuilder = new InMemIteratorBuilder
}

class InMemIteratorBuilder extends IteratorBuilder {
  private val elements = mutable.ListBuffer.empty[Array[Byte]]
  private var isBuilt = false
  override def addIfNonEmpty(element: Array[Byte]): Boolean = {
    if (isBuilt) throw new IllegalStateException("Iterator was already built")
    if (element.nonEmpty) {
      elements.append(element)
      true
    } else false
  }

  override def build(): Option[Iterator[Array[Byte]]] = {
    isBuilt = true
    if (elements.nonEmpty) Some(elements.iterator)
    else None
  }
}

class TailsCounter(private var count: Int = 0, private var countWithoutTail: Int = 0, iteratorBuilder: IteratorBuilder = IteratorBuilder.create()) {
  def incAndAddIfNonEmpty(element: Array[Byte]): Unit = {
    count += 1
    if (!iteratorBuilder.addIfNonEmpty(element)) countWithoutTail += 1
  }
  def getCount: Int = count
  def getCountWithoutTail: Int = countWithoutTail
  def iterator(): Option[Iterator[Array[Byte]]] = iteratorBuilder.build()
}

object TailsCounter {
  def create() = new TailsCounter()
}
