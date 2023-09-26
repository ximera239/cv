package com.zhoga.cv.problems.names

import java.io.{File, InputStream}
import scala.io.Source
import Utils.Id

object NamesApproach1Simple extends NamesApproach[Id] {
  def compute(file: File): Long = {
    run(
      Source
        .fromFile(file)
    )
  }

  def compute(is: InputStream): Long = {
    run(
      Source
        .fromInputStream(is)
    )
  }

  private[problems] def readFile(source: Source): Array[String] = {
    source
      .getLines()
      .mkString("")
      .split(",")
  }
  private[problems] def readFileAndSort(source: Source): Array[String] = {
    readFile(source).sorted
  }
  private[problems] def run(source: Source): Long = {
    readFileAndSort(source)
      .map(_.drop(1).dropRight(1).map(_ - 'A' + 1).sum)
      .zipWithIndex
      .map { case (sum, index) => sum.toLong * (index + 1) }
      .sum
  }
}
