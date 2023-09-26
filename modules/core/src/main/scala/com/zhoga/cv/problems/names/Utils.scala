package com.zhoga.cv.problems.names

import java.io._
import java.nio.channels.FileChannel
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source

object Utils {
  type Id[A] = A

  object Id {
    def apply[A](a: A): Id[A] = a
  }

  trait ResultExtractor[F[_]] {
    def getResult[X](id: F[X]): X
  }
  object Implicits {
    implicit val idResultExtractor: ResultExtractor[Id] = new ResultExtractor[Id] {
      def getResult[X](id: Id[X]): X = id
    }
    implicit val futureResultExtractor: ResultExtractor[Future] = new ResultExtractor[Future] {
      def getResult[X](f: Future[X]): X = Await.result(f, Duration.Inf)
    }
  }

  private[problems] def sourceIterator(s: Source): Iterator[Array[Byte]] = {
    def nextWord(): Option[Array[Byte]] = {
      val emptyOk: Option[Unit] = Some(())

      def takeQuote: Option[Unit] = {
        Some(()).filter(_ => s.take(1).toList.nonEmpty)
      }

      def takeWord: Option[Array[Byte]] = {
        Some(s.takeWhile(_ != '"').toArray.map(c => (c - 'A').toByte))
      }

      def takeQuoteWithMaybeComma: Option[Unit] = {
        s.take(1).toList
        emptyOk
      }

      for {
        _ <- takeQuote
        word <- takeWord
        _ <- takeQuoteWithMaybeComma
      } yield word
    }

    var buffer: Option[Array[Byte]] = nextWord()

    new Iterator[Array[Byte]] {
      override def hasNext: Boolean = buffer.nonEmpty

      override def next(): Array[Byte] = {
        val result = buffer
        buffer = nextWord()
        result.get
      }
    }.filter(_.nonEmpty)
  }

  def useClosable[T <: { def close() }, R](closable: T)(f: T => R): R = {
    try {
      f(closable)
    } finally {
      closable.close()
    }
  }
  private[problems] def bytearraysIterator[T](f: File)(fn: Iterator[Array[Byte]] => T): T = useClosable(new BufferedReader(new FileReader(f))) { stream =>
    val s = new Iterator[Byte] {
      var nextByte: Byte = stream.read().toByte
      override def hasNext: Boolean = nextByte != -1

      override def next(): Byte = {
        val result = nextByte
        nextByte = stream.read().toByte
        result
      }
    }
    val quoteByte = '"'.toByte
    val abyte = 'A'.toByte

    def nextWord(): Option[Array[Byte]] = {
      val emptyOk: Option[Unit] = Some(())

      def takeQuote: Option[Unit] = {
        Some(()).filter(_ => s.take(1).toList.nonEmpty)
      }

      def takeWord: Option[Array[Byte]] = {
        Some(s.takeWhile(_ != quoteByte).toArray.map(c => (c - abyte).toByte))
      }

      def takeQuoteWithMaybeComma: Option[Unit] = {
        s.take(1).toList
        emptyOk
      }

      for {
        _ <- takeQuote
        word <- takeWord
        _ <- takeQuoteWithMaybeComma
      } yield word
    }

    var buffer: Option[Array[Byte]] = nextWord()

    val iterator = new Iterator[Array[Byte]] {
      override def hasNext: Boolean = buffer.nonEmpty

      override def next(): Array[Byte] = {
        val result = buffer
        buffer = nextWord()
        result.get
      }
    }.filter(_.nonEmpty)

    fn(iterator)
  }

  def simpleBytesReader(f: File): Iterator[Array[Byte]] = {
    useClosable(new BufferedInputStream(new FileInputStream(f))) { source =>
      val array = Array.ofDim[Byte](f.length().toInt)
      source.read(array, 0, f.length().toInt)
      array.grouped(5)
    }
  }

  private[problems] def mmSourceIterator(f: File): Iterator[Array[Byte]] = {
    val file = new RandomAccessFile(f, "r")
    val channel = file.getChannel()
    // val chBuffer = ByteBuffer.allocate(channel.size().toInt)
    val chBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
    // channel.read(chBuffer)
    // chBuffer.flip() // 271 sec

    var channelIndex = 0
    val s = Source.fromIterable(new Iterable[Char] {
      override def iterator: Iterator[Char] = new Iterator[Char] {
        override def hasNext: Boolean = channelIndex < channel.size()

        override def next(): Char = {
          channelIndex += 1
          chBuffer.get().toChar
        }
      }
    })

    def nextWord(): Option[Array[Byte]] = {
      val emptyOk: Option[Unit] = Some(())

      def takeQuote: Option[Unit] = {
        Some(()).filter(_ => s.take(1).toList.nonEmpty)
      }

      def takeWord: Option[Array[Byte]] = {
        Some(s.takeWhile(_ != '"').toArray.map(c => (c - 'A').toByte))
      }

      def takeQuoteWithMaybeComma: Option[Unit] = {
        s.take(1).toList
        emptyOk
      }

      for {
        _ <- takeQuote
        word <- takeWord
        _ <- takeQuoteWithMaybeComma
      } yield word
    }

    var buffer: Option[Array[Byte]] = nextWord()

    new Iterator[Array[Byte]] {
      override def hasNext: Boolean = buffer.nonEmpty

      override def next(): Array[Byte] = {
        val result = buffer
        buffer = nextWord()
        buffer.orElse {
          channel.close()
          file.close()
          None
        }
        result.get
      }
    }
  }
  private[problems] def bucketsByLetter() = {
    val a = Array.ofDim[TailsCounter](26)
    (0 until a.size).foreach(i => a(i) = TailsCounter.create())
    a
  }

  private[problems] def log(msg: => String): Unit = {
    println(msg)
  }
}
