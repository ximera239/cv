package com.zhoga.cv.akkasupport

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.TestKit
import org.specs2.mutable.SpecificationLike
import org.specs2.specification.core.{Env, OwnEnv}

class GroupByTest(val env: Env) extends TestKit(ActorSystem("GroupByTest")) with SpecificationLike with OwnEnv {
  "GroupBy" should {
    "group stream by key (only repeated keys)" >> {
      // simple key extractor - identity
      val intGroupBy = new GroupBy[Int, Int](identity)

      // empty source
      Source(Seq.empty[Int]).via(intGroupBy).runWith(Sink.collection) must beEqualTo(Seq.empty[Seq[Int]]).await
      // sequential groups
      Source(Seq(1, 1, 2, 2, 3, 3, 4, 4)).via(intGroupBy).runWith(Sink.collection) must beEqualTo(Seq(Seq(1, 1), Seq(2, 2), Seq(3, 3), Seq(4, 4))).await
      // group only sequential same key
      Source(Seq(1, 1, 2, 2, 1, 1)).via(intGroupBy).runWith(Sink.collection) must beEqualTo(Seq(Seq(1, 1), Seq(2, 2), Seq(1, 1))).await
    }

    "help to aggregate" >> {
      case class Image(cardId: Int, url: String)
      case class Card(id: Int, name: String)
      case class CompositeBuilder(id: Int, card: Option[Card] = None, images: Seq[Image] = Seq.empty) {
        def correct: Boolean = card.nonEmpty
      }
      object CompositeBuilder {
        // ordering will be used for ordered merge of sources
        implicit val ordering: Ordering[CompositeBuilder] = Ordering.fromLessThan[CompositeBuilder](_.id < _.id)
        def compose(cb1: CompositeBuilder, cb2: CompositeBuilder): CompositeBuilder =
          CompositeBuilder(
            id = cb1.id,
            card = cb1.card.orElse(cb2.card),
            images = cb1.images ++ cb2.images,
          )
      }

      val cbGroupBy = new GroupBy[CompositeBuilder, Int](_.id)

      // source of cards we convert to composite objects
      val cardsSource = Source(
        Seq(
          Card(1, "card 1"),
          Card(3, "card 3"),
        )
      ).map(card => CompositeBuilder(id = card.id, card = Some(card)))

      // source of images we convert to composite objects
      val imagesSource = Source(
        Seq(
          Image(1, "https://example.com/image1_1.jpg"),
          Image(1, "https://example.com/image1_2.jpg"),
          Image(2, "https://example.com/image2_1.jpg"),
          Image(2, "https://example.com/image2_2.jpg"),
          Image(3, "https://example.com/image3_1.jpg"),
          Image(3, "https://example.com/image3_2.jpg"),
        )
      ).map(image => CompositeBuilder(id = image.cardId, images = Seq(image)))

      val result = cardsSource
        .mergeSorted(imagesSource)
        // group with our stage
        .via(cbGroupBy)
        // and collapse data
        .map(_.reduce(CompositeBuilder.compose))
        // at this stage we though out images for non-existent card with ID 2
        .filter(_.correct)
        .runWith(Sink.collection)
        .map(_.toList)

      // we should have only two cards
      result.map(_.size) must be_===(2).await
      // card with id 1 and 2 images
      result.map(_.head.id) must be_===(1).await
      result.map(_.head.images.size) must be_===(2).await
      // and card with id 3 and 2 images
      result.map(_.tail.head.id) must be_===(3).await
      result.map(_.tail.head.images.size) must be_===(2).await
    }
  }
}
