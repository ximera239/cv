# AkkaStreams

I was given the task of developing a system that would receive data from various outside sources, store it, combine it, analyze it, create derivatives, index it, and then make it in some way accessible to real-time applications.

Since the organization was already using them, I utilized Akkastreams for backoffice and found them to be quite helpful in the following situations:

- decrease load on the MongoDB

Imagine you have a lot of items. Each item is made up of various components from different collections. Without destroying any MongoDB nodes, you must process all the objects in an acceptable amount of time. I utilized an application-side approach where I joined data from various datasets.

I created [GroupBy](https://github.com/ximera239/cv/blob/main/modules/akkasupport/src/main/scala/com/zhoga/cv/akkasupport/GroupBy.scala) graph stage, where we have a `Source` of items `R`s and can combine multiple records from the source into a `Seq` of records `R` by some computed key.

Example of application-side join can be found in test [here](https://github.com/ximera239/cv/blob/5781cf12ebd23504c5eb2c1d4b29854eaa970a2b/modules/akkasupport/src/test/scala/com/zhoga/cv/akkasupport/GroupByTest.scala#L23C2-L23C2)