# anagrams-benchmark

Benchmarks for different implementations of a program designed to find
groups of anagrams in a corpus. Some of these implementations unsoundly
assume that the code points are all within the basic multilingual plane,
while others work on any valid Unicode strings. The purpose of this
repository is to determine the price that we pay for correctness: how
much slower are the versions that work in all cases?

## What's in the box

This repository includes:

  - a benchmark written with the popular benchmarking framework JMH;
  - a script to analyze the result of that benchmark and compute a few
    derived quantities (like relative average performance and
    statistical significance of differences); and
  - an older version of the benchmark that I wrote by hand (i.e., not
    using JMH), which has the extra feature that it reports the number
    of errors made by each algorithm.

The legacy benchmark runs quickly (a few minutes), but the JMH benchmark
takes a few hours to run. The `results` directory therefore contains
output from each of the two benchmarks, as well as the result of running
the analysis script on the JMH data. Each of these was computed after a
fresh boot of my laptop from tty1, with minimal other load. My laptop
is a Lenovo T440s with an i5-4300U (4-core) and 12 GB of RAM.

## The algorithms

Ten algorithms are tested. Each algorithm comprises two independent
parts: a strategy for alphabetizing words, and a strategy for iterating
over the dataset and collecting results. The alphabetizers are:

  - `NAIVE_BMP` (BMPAlphabetizer.java): Sorts the `char`s of a string.
    This can give incorrect results when the string contains code points
    not in the basic multilingual plane.

  - `MANUAL_DECODE` (ManualDecodeAlphabetizer.java): Runs a hand-written
    function to convert a string to Unicode code points, sorts those,
    and treats the list of integers as the representative of the
    equivalence class of strings (without converting back to a string).

  - `MANUAL_CODEC` (ManualCodecAlphabetizer.java): Runs a hand-written
    function to convert a string to Unicode code points, sorts those,
    then runs a hand-written function to encode these back into a UTF-16
    string, which is used as the key.

  - `BUILTIN_DECODE` (BuiltinDecodeAlphabetizer.java): Runs the Java 8
    method `String.codePoints` to decode a string, sorts them, and uses
    the list of integers as the key (without converting back to a
    string).

  - `BUILTIN_CODEC` (BuiltinCodec.java): Runs the Java 8 method
    `String.codePoints` to decode a string, sorts them, and invokes the
    string constructor `String(int[], int, int)` to convert the sorted
    array back into a string, which is used as the key.

The iteration methods are:

  - `ITERATIVE` (IterativeAnagramsFinder.java): Uses idiomatic Java 7
    techniques to iterate over the strings, collect them into sets, and
    process the result as needed.

  - `STREAMY` (StreamsAnagramFinder.java): Uses the Java 8 streams API
    to perform the computations.

These five alphabetizers can be combined independently with the two
iterators, yielding ten methods in total.

In principle, some of the alphabetizers could be factored apart a bit
more. For instance, it would be interesting to see the results of an
algorithm using the hand-written UTF-16 decoding function, but the
built-in `String` constructor to re-encode. (This constructor is
available in Java 5, so such a solution might be considered the
“canonical solution” in pre–Java 8 code.)

It could also be interesting to try to write a branchless UTF-16 decoder
and compare the performance of the resulting algorithms. Not only would
this potentially make the algorithms more efficient, it could enable
them to run on GPUs. (Making it branchless might not be sufficient, but
it would probably be necessary.)

## Building and running

To run the JMH benchmark, run

    ./gradlew jmh

in the root directory. It’ll take a while (about three hours on my
machine).

To analyze the JMH output, run

    ./gradlew analyze -PbenchmarkOutput=build/reports/jmh/results.txt

in the root directory. (You can of course replace the path to the
results file with another path in case you've moved the file.) This
should run very quickly.

To run the legacy benchmark, which also reports each algorithm’s
correctness on the tested dataset, run

    ./gradlew legacyBenchmark

from the root directory. It takes about 2–3 minutes to complete on my
machine.

Invoke `./gradlew legacyBenchmark` in the root directory.

## Discussion

First of all, where the results of my handwritten benchmark and the JMH
benchmark differ, the JMH benchmark is probably correct.

To start, we can look at the bottom section of the analysis file:

    Overall analysis
    ================
        Average performance by algorithm, fastest to slowest:
             NAIVE_BMP  318.86 ± 0.80 ms/op
         MANUAL_DECODE  442.94 ± 2.04 ms/op
          MANUAL_CODEC  465.21 ± 1.84 ms/op
         BUILTIN_CODEC  496.43 ± 2.95 ms/op
        BUILTIN_DECODE  576.82 ± 5.17 ms/op
        (Weighted uniformly across corpus and iterative/streamy.)

        Consecutive differences:
             NAIVE_BMP  fastest
         MANUAL_DECODE  slower by 124.08 ± 2.19 ms/op (z-score: +56.65)
          MANUAL_CODEC  slower by  22.27 ± 2.74 ms/op (z-score: +8.12)
         BUILTIN_CODEC  slower by  31.21 ± 3.48 ms/op (z-score: +8.98)
        BUILTIN_DECODE  slower by  80.39 ± 5.95 ms/op (z-score: +13.51)

As we should expect, `NAIVE_BMP` is the fastest. This only makes sense:
it’s doing the least work. With the exception of `MANUAL_DECODE`, every
algorithm is doing a strict superset of the work that `NAIVE_BMP` does,
so it should be fastest.

The fastest correct algorithm is about 40% slower, which is certainly
not negligible but is also not too bad of a price to pay for
correctness. The slowest algorithm is about 80% slower than the naive
algorithm, so all these algorithms are in the same factor-of-2 ballpark.

If we look at the per-algorithm analysis, we see that there is no
significant difference between the streamy and iterative versions. All
*z*-scores are less than 2 in absolute value, and all but one are less
than 1.

I’m not sure what to make of the dramatically varying performance of the
algorithms on the different corpora. When the runtimes are normalized by
the corpus word count, all algorithms but one perform best on the Greek
dictionary, which is surprising. The characters in the Greek corpus are
all in the Basic Multilingual Plane, but they’re not in Latin-1. And
Greek words are long compared to English words: 10.9 characters on
average compared to English’s 8.3 characters. The alphabet is
approximately the same size, too.

It could be the case that normalizing the full runtime against the
corpus size gives a biased estimate of the time per word, as described
in the [JMH sample code (#11, “Loops”)][jmh-sample]. But, as
I understand it, this would bias the results to favor larger corpora,
and this is not the distribution that we see represented: the randomly
generated corpora have the largest size (and also a short word length
and a small alphabet), and they tend to perform among the worst of all
corpora.

[jmh-sample]: http://hg.openjdk.java.net/code-tools/jmh/file/1ddf31f810a3/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_11_Loops.java

Within each corpus, the ranking of the algorithms tends to be
approximately the same as the overall ranking. That is, there’s not too
much relative per-corpus variation. (Sometimes the `MANUAL_CODEC` and
`BUILTIN_CODEC` algorithms trade places.) The `RANDOM_FULL` corpus is an
exception, where the three top algorithms are all within a few standard
deviations of each other, and not in the usual order. One post-hoc
justification for this could be: the `RANDOM_FULL` corpus contains
exclusively code points from the Supplementary Multilingual Plane, so
the naive algorithm is doing twice the work whereas the UTF-16–aware
algorithms’ penalty is minimized. However, the Phoenician corpus is also
entirely in the Supplementary Multilingual Plane, and algorithms perform
more typically on it. In fact, `MANUAL_DECODE` is slower than
`NAIVE_BMP` by more than 80-sigma on this corpus!

I would be interested to see additional results or additional analysis
of these existing results. Let me know if you find anything intruiging.
