# AkkaStreams

I got a task to build a system, which takes data from multiple external resources, store it, unify, process and build derivatives, index and at the end should be available in some form to real-time application.
For backoffice I used akkastreams actively (as they were already in use by company) and I found them quite useful in some specific questions:

- decrease load on MongoDB

Let's say you have a lot of objects. Each object consists of various parts from different collections. You need to process all the objects, in reasonable time without killing any MongoDB node. The approach which I used was to join the data from different collections on application side.
For that purpose I wrote [GroupBy](https://github.com/ximera239/cv/blob/main/modules/akkasupport/src/main/scala/com/zhoga/cv/akkasupport/GroupBy.scala) graph stage, which can group multiple records from source into `Seq` of records `R` and we have `Source` of `Seq[R]`s.

Example of application-side join can be found in test [here](https://github.com/ximera239/cv/blob/main/modules/akkasupport/src/test/scala/com/zhoga/cv/akkasupport/GroupByTest.scala)