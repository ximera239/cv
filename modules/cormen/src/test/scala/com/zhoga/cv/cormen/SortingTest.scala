package com.zhoga.cv.cormen

import org.specs2.matcher.MustMatchers
import org.specs2.mutable.Specification

class SortingTest extends Specification {
  "Insertion sort" should  {
    "sort everything correctly" >> {
      Sorting.insertionSort(Seq.empty[Int]) must beEqualTo(Seq.empty[Int])
      Sorting.insertionSort(Seq(3, 2, 1)) must beEqualTo(Seq(1, 2, 3))
      Sorting.insertionSort(Seq(1, 2, 3)) must beEqualTo(Seq(1, 2, 3))
      Sorting.insertionSort(Seq(1, 2, 3, 1)) must beEqualTo(Seq(1, 1, 2, 3))
      Sorting.insertionSort(Seq(3)) must beEqualTo(Seq(3))
      Sorting.insertionSort(Seq(4, 1, 3, 2, 16, 9, 10, 14, 8, 7)) must beEqualTo(Seq(1, 2, 3, 4, 7, 8, 9, 10, 14, 16))
    }
  }

  "Merge sort" should {
    "sort everything correctly" >> {
      Sorting.mergeSort(Seq.empty[Int]) must beEqualTo(Seq.empty[Int])
      Sorting.mergeSort(Seq(3, 2, 1)) must beEqualTo(Seq(1, 2, 3))
      Sorting.mergeSort(Seq(1, 2, 3)) must beEqualTo(Seq(1, 2, 3))
      Sorting.mergeSort(Seq(1, 2, 3, 1)) must beEqualTo(Seq(1, 1, 2, 3))
      Sorting.mergeSort(Seq(3)) must beEqualTo(Seq(3))
      Sorting.mergeSort(Seq(4, 1, 3, 2, 16, 9, 10, 14, 8, 7)) must beEqualTo(Seq(1, 2, 3, 4, 7, 8, 9, 10, 14, 16))
    }
  }

  "Heap sort" should {
    "sort everything correctly" >> {
      Sorting.heapSort(Seq.empty[Int]) must beEqualTo(Seq.empty[Int])
      Sorting.heapSort(Seq(3, 2, 1)) must beEqualTo(Seq(1, 2, 3))
      Sorting.heapSort(Seq(1, 2, 3)) must beEqualTo(Seq(1, 2, 3))
      Sorting.heapSort(Seq(1, 2, 3, 1)) must beEqualTo(Seq(1, 1, 2, 3))
      Sorting.heapSort(Seq(3)) must beEqualTo(Seq(3))
      Sorting.heapSort(Seq(4, 1, 3, 2, 16, 9, 10, 14, 8, 7)) must beEqualTo(Seq(1, 2, 3, 4, 7, 8, 9, 10, 14, 16))
    }
  }

  "Quick sort" should {
    "sort everything correctly" >> {
      Sorting.quickSort(Seq.empty[Int]) must beEqualTo(Seq.empty[Int])
      Sorting.quickSort(Seq(3, 2, 1)) must beEqualTo(Seq(1, 2, 3))
      Sorting.quickSort(Seq(1, 2, 3)) must beEqualTo(Seq(1, 2, 3))
      Sorting.quickSort(Seq(1, 2, 3, 1)) must beEqualTo(Seq(1, 1, 2, 3))
      Sorting.quickSort(Seq(3)) must beEqualTo(Seq(3))
      Sorting.quickSort(Seq(4, 1, 3, 2, 16, 9, 10, 14, 8, 7)) must beEqualTo(Seq(1, 2, 3, 4, 7, 8, 9, 10, 14, 16))
    }
  }
}
