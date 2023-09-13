## Overview

I've read a lot of professional articles and books during my life. Here, I wish to keep some references to books that I've read (maybe, with some implementations). 

I'll also leave here some examples of problems that were challenging and important to answer at the time I was asked to look into it.

And another type of problems, some tricky challenges that I found interesting, perhaps complex, or that taught me anything 


**<span style="color:darkgreen">Work in progress...</span>**


### Interesting problems

- Stack numeric and non-numeric

Once upon a time I saw a [question](https://ru.stackoverflow.com/q/1278314/417043) on stackoverflow, which (I supposed) was asked by a student about his homework, but problem itself seemed interesting to me. Original question was in Russian.

> Implement an immutable Stack structure using scala List, it should have an `average` method in case the parameter type is numeric, and it should return average over constant time 

Story and link to solution can be found [here](Stack.md)

---

- [ReactiveMongo](https://github.com/ReactiveMongo/ReactiveMongo) mongo driver issue with concurrent usage of channels

Long story in short - we used ReactiveMongo driver in our application. We had a problem from time to time that requests which usually work fast could take much more time than usual (seconds/tens of seconds instead of milliseconds). This "from time to time" was random, so I spent some time debugging and tracing, and eventually found, that driver uses channels open to mongodb as if they are fully asynchronous (multiple requests are replied by multiple responses in any order, as soon as they are ready). But instead channels worked as FIFO.. Which meant that if fast request was sent to the same channel, which was used just before for slow request, then this fast request execution time will be extended to execution time of slow request.

Issue was reported to maintainer and was solved by him [here](https://github.com/ReactiveMongo/ReactiveMongo/pull/762) (I commented PR from my work github account [eugeneatnezasa](https://github.com/eugeneatnezasa))

---

- I spent much time working with akkastreams. Some utilities and their purposes can be found [here](AkkaStreams.md)

---

- 

### Videos

- [ZIO from Scratch](https://www.youtube.com/playlist?list=PLvdARMfvom9B21CNSdn88ldDKCWKZ8Kfx)

(hmm, there are much more videos... when I watched it, there were only 5 parts in the playlist)

- [Category Theory](https://www.youtube.com/playlist?list=PLbgaMIhjbmEnaH_LTkxLI7FMa2HsnawM_) by Bartosz Milewski

Super long and requires to watch very carefully and sometimes rewind and re-watch.. There are still 5 videos to complete.

But it made me buy the book [Category Theory For Programmers](https://github.com/hmemcpy/milewski-ctfp-pdf) which is waiting on the shelf

---

### Books

- A wonderful book by Thomas H. Cormen (and others) ["Introduction to Algorithms"](https://www.amazon.de/-/en/Thomas-H-Cormen/dp/026204630X/)

For the first time I read this book in Uni, and then re-read some parts later

Some examples:

1. [Sorting](https://github.com/ximera239/cv/blob/main/modules/cormen/src/main/scala/com/zhoga/cv/cormen/Sorting.scala)


---

- [Functional Programming for Mortals](https://leanpub.com/fpmortals) - I bought version for Cats. It is available in my library at leanpub, but not available for sale. Sources can be found [here](https://github.com/sadhu89/fpmortals-cats)

Nice book. Some chapters were unclear at first, and then I met situations where I can use this. **todo example**  