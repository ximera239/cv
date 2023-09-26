package com.zhoga.cv.problems.names

import com.zhoga.cv.problems.names.Utils.ResultExtractor
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification

class NamesTest extends Specification {

  "Names simple approach" should {
    "compute correctly" >> {
      val r1 = NamesApproach1Simple.compute(getClass.getResourceAsStream("/names_test1.txt"))
      r1 must_=== (18L)

      val r2 = NamesApproach1Simple.compute(getClass.getResourceAsStream("/names_test2.txt"))
      r2 must_=== (0L)

      val r3 = NamesApproach1Simple.compute(getClass.getResourceAsStream("/names_test3.txt"))
      r3 must_=== (0L)

      val r4 = NamesApproach1Simple.compute(getClass.getResourceAsStream("/names_test4.txt"))
      r4 must_=== (36L)
    }

    "other approaches should give same results as simple approach" >> {
      val r1 = NamesApproach1Simple.compute(getClass.getResourceAsStream("/names_test1.txt"))
      val r2 = NamesApproach1Simple.compute(getClass.getResourceAsStream("/names_test2.txt"))
      val r3 = NamesApproach1Simple.compute(getClass.getResourceAsStream("/names_test3.txt"))
      val r4 = NamesApproach1Simple.compute(getClass.getResourceAsStream("/names_test4.txt"))

      def testAproach[F[_]](approach: NamesApproach[F])(implicit extractor: ResultExtractor[F]): MatchResult[Long] = {
        extractor.getResult(approach.compute(getClass.getResourceAsStream("/names_test1.txt"))) must_===(r1)
        extractor.getResult(approach.compute(getClass.getResourceAsStream("/names_test2.txt"))) must_===(r2)
        extractor.getResult(approach.compute(getClass.getResourceAsStream("/names_test3.txt"))) must_===(r3)
        extractor.getResult(approach.compute(getClass.getResourceAsStream("/names_test4.txt"))) must_===(r4)
      }

      import Utils.Implicits._

      testAproach(NamesApproach2)
      testAproach(NamesApproach3Sync)
      testAproach(NamesApproach3)
    }
  }
}
