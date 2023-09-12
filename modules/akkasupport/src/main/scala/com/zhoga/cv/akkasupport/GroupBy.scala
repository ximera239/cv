package com.zhoga.cv.akkasupport

import akka.stream._
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}

class GroupBy[T, R](groupFunc: T => R) extends GraphStage[FlowShape[T, Seq[T]]] {
  val in: Inlet[T] = Inlet[T]("group.in")

  val out: Outlet[Seq[T]] = Outlet("grouped.out")

  override val shape: FlowShape[T, Seq[T]] = FlowShape(in, out)
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    var group: List[T] = List.empty[T]

    setHandler(
      in,
      new InHandler {
        override def onPush(): Unit = {
          val nextElement = grab(in)
          if (group.headOption.exists(e => groupFunc(e) != groupFunc(nextElement))) {
            // we need to reverse the group to get elements in same order as we got them from the source
            val grouped = group.reverse
            group = List(nextElement)
            push(out, grouped)
          } else {
            // we build the group in reversed order
            group = nextElement :: group
            pull(in)
          }
        }

        override def onUpstreamFinish(): Unit = {
          if (group.nonEmpty) {
            val grouped = group.reverse
            group = List()
            emit(out, grouped)
          }
          super.onUpstreamFinish()
        }

      }
    )

    setHandler(
      out,
      new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      }
    )
  }
}
