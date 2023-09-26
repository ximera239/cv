package com.zhoga.cv.problems.names

import java.io.{File, InputStream}
import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.Source

object Names {
  def main(args: Array[String]): Unit = {
    val config = readConfig(args.toList, Config())

    config
      .fileName
      .fold(
        Utils.log(s"Please provide file name with data")
      ) { fileName =>
        val f = new File(fileName)

        val resultMsg = List(
          config.run(_.run1)(("r1 (simple)", timed("NamesApproach1Simple", NamesApproach1Simple.compute(f)))),
          config.run(_.run2)(("r2 (recursion)", timed("NamesApproach2", NamesApproach2.compute(f)))),
          config.run(_.run3Sync)(("r3 (trampoline)", timed("NamesApproach3Sync", NamesApproach3Sync.compute(f)))),
          config.run(_.run3SyncFS)(
            (
              "r4 (trampoline, fast source)",
              Utils.bytearraysIterator(f) { iterator =>
                timed("NamesApproach3Sync with fast source", NamesApproach3Sync.compute(iterator))
              }
            )
          ),
          config.run(_.run3Async)(("r5 (parallel)", timed("NamesApproach3", Await.result(NamesApproach3.compute(f), Duration.Inf)))),
          config.run(_.run3AsyncFS)(
            (
              "r6 (parallel, fast source)",
              Utils.bytearraysIterator(f) { iterator =>
                timed("NamesApproach3 with fast source", Await.result(NamesApproach3.compute(iterator), Duration.Inf))
              }
            )
          ),
        ).flatten.map { case (msg, result) => s"  $msg --> $result" }.mkString("Results:\n", "\n", "\n==================\n")

        Utils.log(resultMsg)

        if (config.runReadersBenchmark) {
          testSources(f)
        }
      }
  }

  case class Config(
    fileName: Option[String] = None,
    run1: Boolean = false,
    run2: Boolean = false,
    run3Sync: Boolean = false,
    run3SyncFS: Boolean = false,
    run3Async: Boolean = false,
    run3AsyncFS: Boolean = false,
    runReadersBenchmark: Boolean = false,
  ) {
    def run(check: Config => Boolean)(fn: => (String, Long)): Option[(String, Long)] = {
      if (check(this)) {
        Some(fn).map { case (msg, result) => (f"$msg%30s", result) }
      } else None
    }
  }

  private def timed[T](prefix: String, f: => T): T = {
    val start = System.currentTimeMillis()
    try {
      f
    } finally {
      Utils.log(s"$prefix / takes ${System.currentTimeMillis() - start}ms")
    }
  }

  def testSources(f: File): Unit = {
    timed("Simple stream read from file to bytearray", Utils.simpleBytesReader(f).reduce((a1, _) => a1))

    timed("Source text from file", Source.fromFile(f).getLines().mkString.length)
    timed("Source text from file, and split", NamesApproach1Simple.readFile(Source.fromFile(f)))
    timed("Source text from file, and split/sort", NamesApproach1Simple.readFileAndSort(Source.fromFile(f)))
    timed("Source text from file, do all at once", Utils.sourceIterator(Source.fromFile(f)).reduce((a1, _) => a1))
    timed("Source text from file/binary, do all at once", Utils.bytearraysIterator(f)(_.reduce((a1, _) => a1)))

  }

  @tailrec
  def readConfig(args: List[String], config: Config): Config = {
    args match {
      case Nil =>
        config
      case "--file" :: name :: tail =>
        readConfig(tail, config.copy(fileName = Some(name)))
      case "--run" :: "simple" :: tail =>
        readConfig(tail, config.copy(run1 = true))
      case "--run" :: "recursive" :: tail =>
        readConfig(tail, config.copy(run2 = true))
      case "--run" :: "trampoline" :: tail =>
        readConfig(tail, config.copy(run3Sync = true))
      case "--run" :: "trampoline-fs" :: tail =>
        readConfig(tail, config.copy(run3SyncFS = true))
      case "--run" :: "async" :: tail =>
        readConfig(tail, config.copy(run3Async = true))
      case "--run" :: "async-fs" :: tail =>
        readConfig(tail, config.copy(run3AsyncFS = true))
      case "--run" :: "all" :: tail =>
        readConfig(tail, config.copy(run1 = true, run2 = true, run3Sync = true, run3SyncFS = true, run3Async = true, run3AsyncFS = true))
      case "--test" :: "sources" :: tail =>
        readConfig(tail, config.copy(runReadersBenchmark = true))
      case other :: tail =>
        val msg = s"Unsupported parameter '$other'!"
        Utils.log(msg)
        throw new IllegalArgumentException(msg)
    }
  }
}

trait NamesApproach[F[_]] {
  def compute(file: File): F[Long]
  def compute(file: InputStream): F[Long]
}
