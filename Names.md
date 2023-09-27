# Names task

## Problem
Using names.txt, a 46K text file containing over five-thousand first names, begin by sorting it into alphabetical order. Then working out the alphabetical value for each name, multiply this value by its alphabetical position in the list to obtain a name score.

For example, when the list is sorted into alphabetical order, `COLIN`, which is worth `3 + 15 + 12 + 9 + 14 = 53`, is the 938th name in the list. So, `COLIN` would obtain a score of `938 × 53 = 49714`.

What is the total of all the name scores in the file?

## Solutions

### Approach 1, simple

Obviously, straightforward solution is very simple:

```scala
def compute(file: File): Long = {
  Source
    .fromFile(file)
    .getLines()
    .mkString("")
    .split(",")
    .sorted
    .map(_.drop(1).dropRight(1).map(_ - 'A' + 1).sum)
    .zipWithIndex
    .map { case (sum, index) => sum.toLong * (index + 1) }
    .sum
}
```

This solution can be found [here](https://github.com/ximera239/cv/blob/main/modules/core/src/main/scala/com/zhoga/cv/problems/names/NamesApproach1Simple.scala)

it takes 1 minute to write and perfectly solves the problem. But what if we need to process such files regularly, and, more important, with non-predictable file-size?

Our simple solution has a couple of problems:

1. It loads everything into memory.
2. Complexity is O(n*log(n)) and O(n^2) in worst case (sort uses quick-sort)

### Approach 2, recursive

Problem 1 (everything is in the memory): we can read tokens

[Here](https://github.com/ximera239/cv/blob/main/modules/core/src/main/scala/com/zhoga/cv/problems/names/Utils.scala#L28) you can find implementation example

Problem 2 (sort complexity): Our task is to compute a value. Sorting and sorted collection is only intermediate step and not a part of result. So, we can try to improve this part. 

Idea is to process data recursively. We need to find the index for each word. And each word can contain only limited set of symbols (26, uppercase letters). This means that we can go through all the collection, and split it into 26 computational sub-streams, and when finished, we can run the same process for each sub-stream (at this moment we alreay know all the offsets, e.g. we have 1001 words starting from `A`, so first word, which starts with `B` will have index number 1002). For simplicity I built substreams still in memory, but we can work with files or anything else.

Implementation example is [here](https://github.com/ximera239/cv/blob/main/modules/core/src/main/scala/com/zhoga/cv/problems/names/NamesApproach2.scala#L22)

Advantages:

- if we assume that avg word length is a constant, then complexity is linear
- we can limit our heap usage

Disadvantage:

- in case if we have too long words, we will fail with stack overflow exception

### Approach 3, trampoline

To solve this problem we can use trampoline. Instead of direct recursive call we return the object. One of:

```scala
sealed trait Process

case class ProcessValue(value: Long) extends Process

case class ProcessContinuation(value: Long, fork: () => Process) extends Process

case class ProcessComposite(forks: List[Process]) extends Process
```

Now we can implement independent tail-recursive processor. Implementation is [here](https://github.com/ximera239/cv/blob/main/modules/core/src/main/scala/com/zhoga/cv/problems/names/NamesApproach3.scala#L115)

But, actually, you can notice, that all our sub-streams are independent, so, we can write another processor with futures and do some work in parallel. [Here](https://github.com/ximera239/cv/blob/main/modules/core/src/main/scala/com/zhoga/cv/problems/names/NamesApproach3.scala#L96) you can find implementation.

Another improvement is how we read the data. `Source` does convertions to chars (which we then convert back to bytes). We can avoid this and read the file directly, [which](https://github.com/ximera239/cv/blob/main/modules/core/src/main/scala/com/zhoga/cv/problems/names/Utils.scala#L72C1) also improves performance

## Run it

Each approach implementation can be run:

```sbt
// run simple solution (approach 1)
> run --file "<your file here>" --run simple
// recursive (approach 2)
> run --file "<your file here>" --run recursive
// solution with trampoline (sync)
> run --file "<your file here>" --run trampoline
// solution with trampoline (sync) with improved source (read bytes, not chars)
> run --file "<your file here>" --run trampoline-fs
// async solution
> run --file "<your file here>" --run async
// async solution with improved source (read bytes, not chars)
> run --file "<your file here>" --run async-fs
// run all above one by another
> run --file "<your file here>" --run all
```

TODO: Improved implementations work faster on huge files (e.g. ~500mb), but they spend too much time in GC. I need to implement good iterator builder, which will not create new structures every iteration. I think this will improve performance even more. 