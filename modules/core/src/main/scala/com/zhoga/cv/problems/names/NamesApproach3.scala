package com.zhoga.cv.problems.names

import com.zhoga.cv.problems.names.Utils.Id

import java.io.{File, InputStream}
import java.util.concurrent.Executors
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source

object NamesApproach3 extends NamesApproach[Future] {
  override def compute(file: File): Future[Long] = {
    ProcessRunner.run(NamesApproach3Impl.process(file))
  }

  def compute(is: InputStream): Future[Long] = {
    ProcessRunner.run(
      NamesApproach3Impl.process(
        Source
          .fromInputStream(is)
      )
    )
  }

  def compute(iterator: Iterator[Array[Byte]]): Future[Long] = {
    ProcessRunner.run(NamesApproach3Impl.process(iterator))
  }
}

object NamesApproach3Sync extends NamesApproach[Id] {
  override def compute(file: File): Long = {
    ProcessRunner.runSync(NamesApproach3Impl.process(file))
  }

  def compute(is: InputStream): Long = {
    ProcessRunner.runSync(
      NamesApproach3Impl.process(
        Source
          .fromInputStream(is)
      )
    )
  }

  def compute(iterator: Iterator[Array[Byte]]): Long = {
    ProcessRunner.runSync(NamesApproach3Impl.process(iterator))
  }
}

object NamesApproach3Impl {
  private[problems] def process(file: File): Process = {
    process(Source.fromFile(file))
  }

  private[problems] def process(s: Source): Process = process(Utils.sourceIterator(s))
  private[problems] def process(s: Iterator[Array[Byte]]): Process = {
    def process(offset: Int, prefixSum: Long, s: Iterator[Array[Byte]]): Process = {
      val buckets = s.foldLeft(Utils.bucketsByLetter()) { case (a, word) =>
        val firstLetter = word(0)
        a(firstLetter).incAndAddIfNonEmpty(word.drop(1))
        a
      }

      ProcessComposite(
        buckets
          .zipWithIndex
          .foldLeft((offset, List.empty[Process])) { case ((offset, processes), (tc, index)) =>
            val sumWithoutTail =
              (prefixSum + index + 1) * (offset + 1 + offset + tc.getCountWithoutTail) * tc.getCountWithoutTail / 2

            val nextPrefixSum = prefixSum + index + 1
            val nextOffset = offset + tc.getCountWithoutTail
            (
              offset + tc.getCount,
              tc.iterator().fold[Process](ProcessValue(sumWithoutTail)) { iterator =>
                ProcessContinuation(sumWithoutTail, () => process(nextOffset, nextPrefixSum, iterator))
              } :: processes
            )
          }
          ._2
      )
    }

    process(offset = 0, prefixSum = 0L, s = s)
  }
}

sealed trait Process

case class ProcessValue(value: Long) extends Process

case class ProcessContinuation(value: Long, fork: () => Process) extends Process

case class ProcessComposite(forks: List[Process]) extends Process

object ProcessRunner {
  def run(process: Process, par: Int = 8): Future[Long] = {
    implicit val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(par))

    def step(process: Process): Future[Long] =
      process match {
        case ProcessValue(v) => Future.successful(v)
        case ProcessContinuation(value, fork) => Future.successful(value).flatMap(v => step(fork()).map(_ + v))
        case ProcessComposite(forks) =>
          Future.sequence(forks.map(p => step(p))).map(_.sum)
      }

    val result = step(process)
    result.onComplete { _ =>
      ec.shutdownNow()
      ()
    }
    result
  }

  def runSync(process: Process): Long = {
    case class Queue(processes: List[Process])
    @tailrec
    def step(sum: Long, processesToDo: List[Process]): Long =
      processesToDo match {
        case Nil => sum
        case ProcessValue(v) :: tail => step(sum + v, tail)
        case ProcessContinuation(value, fork) :: tail => step(sum + value, fork() :: tail)
        case ProcessComposite(forks) :: tail => step(sum, forks ++ tail)
      }

    step(0, List(process))
  }
}
