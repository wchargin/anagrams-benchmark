# anagrams-benchmark

Benchmarks for different implementations of a program designed to find
groups of anagrams in a corpus. Some of these implementations unsoundly
assume that the code points are all within the basic multilingual plane,
while others work on any valid Unicode strings. The purpose of this
repository is to determine the price that we pay for correctness: how
much slower are the versions that work in all cases?

## Building and running

Invoke `gradle build` or `gradle run` in the root directory.

## Output

```
Constructing corpora...
--- Random, BMP only
--- Random, full spectrum
--- Supercollider
--- Natural language, english.txt
--- Natural language, greek.txt
--- Natural language, phoenician.txt
Warming up...
Beginning timing.

Corpus: RandomCorpus[] (size: 1000000; BMP-only: true)
StreamsAnagramFinder          BMPAlphabetizer               (   640.9 +/-  11.4) ms  errors=0
StreamsAnagramFinder          ManualDecodeAlphabetizer      (   801.3 +/-   6.2) ms  errors=0
StreamsAnagramFinder          ManualCodecAlphabetizer       (   849.9 +/-  10.2) ms  errors=0
StreamsAnagramFinder          BuiltinDecodeAlphabetizer     (  1047.3 +/-  11.4) ms  errors=0
StreamsAnagramFinder          BuiltinCodecAlphabetizer      (  1002.8 +/-  18.1) ms  errors=0
IterativeAnagramsFinder       BMPAlphabetizer               (   700.1 +/-   1.9) ms  errors=0
IterativeAnagramsFinder       ManualDecodeAlphabetizer      (   846.4 +/-   8.7) ms  errors=0
IterativeAnagramsFinder       ManualCodecAlphabetizer       (   836.7 +/-   8.9) ms  errors=0
IterativeAnagramsFinder       BuiltinDecodeAlphabetizer     (  1058.8 +/-  20.5) ms  errors=0
IterativeAnagramsFinder       BuiltinCodecAlphabetizer      (  1009.5 +/-  12.5) ms  errors=0

Corpus: RandomCorpus[] (size: 1000000; BMP-only: false)
StreamsAnagramFinder          BMPAlphabetizer               (   964.2 +/-   9.3) ms  errors=49313
StreamsAnagramFinder          ManualDecodeAlphabetizer      (   964.6 +/-  10.2) ms  errors=0
StreamsAnagramFinder          ManualCodecAlphabetizer       (   898.7 +/-   8.0) ms  errors=0
StreamsAnagramFinder          BuiltinDecodeAlphabetizer     (  1163.9 +/-  10.3) ms  errors=0
StreamsAnagramFinder          BuiltinCodecAlphabetizer      (  1016.5 +/-  12.4) ms  errors=0
IterativeAnagramsFinder       BMPAlphabetizer               (   869.7 +/-   4.4) ms  errors=49313
IterativeAnagramsFinder       ManualDecodeAlphabetizer      (   942.5 +/-  14.7) ms  errors=0
IterativeAnagramsFinder       ManualCodecAlphabetizer       (   906.3 +/-   5.9) ms  errors=0
IterativeAnagramsFinder       BuiltinDecodeAlphabetizer     (  1150.7 +/-   9.9) ms  errors=0
IterativeAnagramsFinder       BuiltinCodecAlphabetizer      (  1019.9 +/-   9.6) ms  errors=0

Corpus: SupercolliderCorpus[] (size: 531441; BMP-only: false)
StreamsAnagramFinder          BMPAlphabetizer               (   262.1 +/-   0.4) ms  errors=3469
StreamsAnagramFinder          ManualDecodeAlphabetizer      (   349.2 +/-   4.6) ms  errors=0
StreamsAnagramFinder          ManualCodecAlphabetizer       (   357.2 +/-   5.7) ms  errors=0
StreamsAnagramFinder          BuiltinDecodeAlphabetizer     (   492.9 +/-   9.6) ms  errors=0
StreamsAnagramFinder          BuiltinCodecAlphabetizer      (   441.3 +/-   7.2) ms  errors=0
IterativeAnagramsFinder       BMPAlphabetizer               (   260.8 +/-   4.2) ms  errors=3469
IterativeAnagramsFinder       ManualDecodeAlphabetizer      (   341.9 +/-   4.6) ms  errors=0
IterativeAnagramsFinder       ManualCodecAlphabetizer       (   359.3 +/-   5.6) ms  errors=0
IterativeAnagramsFinder       BuiltinDecodeAlphabetizer     (   506.4 +/-   6.5) ms  errors=0
IterativeAnagramsFinder       BuiltinCodecAlphabetizer      (   434.2 +/-   2.2) ms  errors=0

Corpus: DictionaryCorpus[english.txt] (size: 64001; BMP-only: true)
StreamsAnagramFinder          BMPAlphabetizer               (    20.3 +/-   0.1) ms  errors=0
StreamsAnagramFinder          ManualDecodeAlphabetizer      (    32.6 +/-   0.2) ms  errors=0
StreamsAnagramFinder          ManualCodecAlphabetizer       (    36.4 +/-   0.3) ms  errors=0
StreamsAnagramFinder          BuiltinDecodeAlphabetizer     (    45.6 +/-   0.4) ms  errors=0
StreamsAnagramFinder          BuiltinCodecAlphabetizer      (    42.4 +/-   0.2) ms  errors=0
IterativeAnagramsFinder       BMPAlphabetizer               (    19.3 +/-   0.1) ms  errors=0
IterativeAnagramsFinder       ManualDecodeAlphabetizer      (    32.2 +/-   0.2) ms  errors=0
IterativeAnagramsFinder       ManualCodecAlphabetizer       (    35.6 +/-   0.1) ms  errors=0
IterativeAnagramsFinder       BuiltinDecodeAlphabetizer     (    45.2 +/-   0.5) ms  errors=0
IterativeAnagramsFinder       BuiltinCodecAlphabetizer      (    41.1 +/-   0.2) ms  errors=0

Corpus: DictionaryCorpus[greek.txt] (size: 530760; BMP-only: true)
StreamsAnagramFinder          BMPAlphabetizer               (   359.8 +/- 111.1) ms  errors=0
StreamsAnagramFinder          ManualDecodeAlphabetizer      (   461.2 +/-  47.3) ms  errors=0
StreamsAnagramFinder          ManualCodecAlphabetizer       (   478.3 +/-  17.9) ms  errors=0
StreamsAnagramFinder          BuiltinDecodeAlphabetizer     (   907.9 +/- 304.2) ms  errors=0
StreamsAnagramFinder          BuiltinCodecAlphabetizer      (   475.7 +/-  15.2) ms  errors=0
IterativeAnagramsFinder       BMPAlphabetizer               (   260.7 +/-  12.5) ms  errors=0
IterativeAnagramsFinder       ManualDecodeAlphabetizer      (   500.5 +/-  37.2) ms  errors=0
IterativeAnagramsFinder       ManualCodecAlphabetizer       (   464.0 +/-  14.2) ms  errors=0
IterativeAnagramsFinder       BuiltinDecodeAlphabetizer     (   501.7 +/-   1.3) ms  errors=0
IterativeAnagramsFinder       BuiltinCodecAlphabetizer      (   465.0 +/-  10.6) ms  errors=0

Corpus: DictionaryCorpus[phoenician.txt] (size: 530760; BMP-only: false)
StreamsAnagramFinder          BMPAlphabetizer               (   335.9 +/-   7.4) ms  errors=0
StreamsAnagramFinder          ManualDecodeAlphabetizer      (   489.2 +/-  24.1) ms  errors=0
StreamsAnagramFinder          ManualCodecAlphabetizer       (   501.8 +/-   9.1) ms  errors=0
StreamsAnagramFinder          BuiltinDecodeAlphabetizer     (   570.8 +/-  18.6) ms  errors=0
StreamsAnagramFinder          BuiltinCodecAlphabetizer      (   498.9 +/-  11.9) ms  errors=0
IterativeAnagramsFinder       BMPAlphabetizer               (   323.3 +/-   2.3) ms  errors=0
IterativeAnagramsFinder       ManualDecodeAlphabetizer      (   478.0 +/-  19.5) ms  errors=0
IterativeAnagramsFinder       ManualCodecAlphabetizer       (   499.1 +/-  11.4) ms  errors=0
IterativeAnagramsFinder       BuiltinDecodeAlphabetizer     (   582.2 +/-  27.5) ms  errors=0
IterativeAnagramsFinder       BuiltinCodecAlphabetizer      (   497.6 +/-  12.6) ms  errors=0

Done.
Total runtime: 230.1 s.
```
