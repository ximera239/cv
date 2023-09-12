## Overview

During my life I read quite some professional books and papers. I will not be able to recall even the names for the most of them.
During my carrier I solved some number of challenging problems, and when you are asked about that it can be hard to recall.
Here I want to keep some links to books that I read (with some implementations), I will leave here some examples of problems which were difficult and important to solve at the time I got them, or problems which I found interesting, maybe tricky or taught me something. 


**<span style="color:darkblue">Work in progress...</span>**


### Interesting problems

- Stack numeric and non-numeric

Once upon a time I saw a [question](https://ru.stackoverflow.com/q/1278314/417043) on stackoverflow, which (I supposed) was asked by a student about his homework, but problem itself seemed interesting to me. Original question was in Russian.

> Implement an immutable Stack structure using scala List, it should have an `average` method in case the parameter type is numeric, and it should return average over constant time 

Story and link to solution can be found [here](Stack.md)

- [ReactiveMongo](https://github.com/ReactiveMongo/ReactiveMongo) mongo driver issue with concurrent usage of channels

Long story in short - we used ReactiveMongo driver in our application. We had a problem from time to time that requests which usually work fast count take much more time than usual (seconds/tens of seconds). This "from time to time" was random, so I spent some time debugging and tracing, and eventually found, that driver uses chnnals open to mongodb as if they are fully asynchronous (multiple requests are replied by multiple responses in any order, as soon as it ready). But instead it worked as FIFO.. Which meant that if fast request was sent to the same channel, which was used just before for slow request, then this fast request execution time will be extended with execution time of slow request.

Issue was solved by maintainer [here](https://github.com/ReactiveMongo/ReactiveMongo/pull/762) (I commented PR from my work github account [eugeneatnezasa](https://github.com/eugeneatnezasa))

- I spent much time working with akkastreams. Some utilities and purposes can be found [here](AkkaStreams.md)

- 

### Books

- A wonderful book by Thomas H. Cormen (and others) ["Introduction to Algorithms"](https://www.amazon.de/-/en/Thomas-H-Cormen/dp/026204630X/)

For the first time I read this book in Uni, and then re-read some parts later

Some examples:

1. [Sorting](https://github.com/ximera239/cv/blob/main/modules/cormen/src/main/scala/com/zhoga/cv/cormen/Sorting.scala)

2. 

- 