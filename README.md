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
--- Natural language, english.txt
--- Natural language, greek.txt
--- Natural language, phoenician.txt
Warming up...
Beginning timing.

Corpus: RandomCorpus[] (size: 1000000; BMP-only: true)
StreamsAnagramFinder          BMPAlphabetizer               (   666.0 +/-  11.9) ms  errors=0
StreamsAnagramFinder          ManualDecodeAlphabetizer      (   858.7 +/-   6.6) ms  errors=0
StreamsAnagramFinder          ManualCodecAlphabetizer       (   844.7 +/-   3.7) ms  errors=0
StreamsAnagramFinder          BuiltinDecodeAlphabetizer     (  1080.2 +/-   4.7) ms  errors=0
StreamsAnagramFinder          BuiltinCodecAlphabetizer      (   958.1 +/-   7.9) ms  errors=0
IterativeAnagramsFinder       BMPAlphabetizer               (   647.7 +/-  10.7) ms  errors=0
IterativeAnagramsFinder       ManualDecodeAlphabetizer      (   786.5 +/-   9.4) ms  errors=0
IterativeAnagramsFinder       ManualCodecAlphabetizer       (   789.9 +/-   6.8) ms  errors=0
IterativeAnagramsFinder       BuiltinDecodeAlphabetizer     (  1039.2 +/-   7.2) ms  errors=0
IterativeAnagramsFinder       BuiltinCodecAlphabetizer      (   959.1 +/-  10.6) ms  errors=0

Corpus: RandomCorpus[] (size: 1000000; BMP-only: false)
StreamsAnagramFinder          BMPAlphabetizer               (   959.1 +/-   5.1) ms  errors=49313
StreamsAnagramFinder          ManualDecodeAlphabetizer      (   917.5 +/-  12.1) ms  errors=0
StreamsAnagramFinder          ManualCodecAlphabetizer       (   911.4 +/-  12.3) ms  errors=0
StreamsAnagramFinder          BuiltinDecodeAlphabetizer     (  1167.1 +/-  11.6) ms  errors=0
StreamsAnagramFinder          BuiltinCodecAlphabetizer      (   995.7 +/-   5.2) ms  errors=0
IterativeAnagramsFinder       BMPAlphabetizer               (   953.4 +/-   3.1) ms  errors=49313
IterativeAnagramsFinder       ManualDecodeAlphabetizer      (   943.9 +/-   6.4) ms  errors=0
IterativeAnagramsFinder       ManualCodecAlphabetizer       (   878.2 +/-   9.0) ms  errors=0
IterativeAnagramsFinder       BuiltinDecodeAlphabetizer     (  1114.1 +/-   5.3) ms  errors=0
IterativeAnagramsFinder       BuiltinCodecAlphabetizer      (   955.2 +/-   6.6) ms  errors=0

Corpus: DictionaryCorpus[english.txt] (size: 64001; BMP-only: true)
StreamsAnagramFinder          BMPAlphabetizer               (    20.6 +/-   0.1) ms  errors=0
StreamsAnagramFinder          ManualDecodeAlphabetizer      (    34.1 +/-   0.1) ms  errors=0
StreamsAnagramFinder          ManualCodecAlphabetizer       (    38.1 +/-   0.2) ms  errors=0
StreamsAnagramFinder          BuiltinDecodeAlphabetizer     (    45.3 +/-   0.3) ms  errors=0
StreamsAnagramFinder          BuiltinCodecAlphabetizer      (    41.0 +/-   0.3) ms  errors=0
IterativeAnagramsFinder       BMPAlphabetizer               (    19.3 +/-   0.1) ms  errors=0
IterativeAnagramsFinder       ManualDecodeAlphabetizer      (    32.0 +/-   0.2) ms  errors=0
IterativeAnagramsFinder       ManualCodecAlphabetizer       (    35.4 +/-   0.2) ms  errors=0
IterativeAnagramsFinder       BuiltinDecodeAlphabetizer     (    43.2 +/-   0.4) ms  errors=0
IterativeAnagramsFinder       BuiltinCodecAlphabetizer      (    39.2 +/-   0.3) ms  errors=0

Corpus: DictionaryCorpus[greek.txt] (size: 530760; BMP-only: true)
StreamsAnagramFinder          BMPAlphabetizer               (   271.3 +/-  12.9) ms  errors=0
StreamsAnagramFinder          ManualDecodeAlphabetizer      (   762.1 +/- 271.0) ms  errors=0
StreamsAnagramFinder          ManualCodecAlphabetizer       (   482.6 +/-  12.3) ms  errors=0
StreamsAnagramFinder          BuiltinDecodeAlphabetizer     (   598.0 +/-  26.5) ms  errors=0
StreamsAnagramFinder          BuiltinCodecAlphabetizer      (   484.8 +/-  15.9) ms  errors=0
IterativeAnagramsFinder       BMPAlphabetizer               (   256.7 +/-  12.6) ms  errors=0
IterativeAnagramsFinder       ManualDecodeAlphabetizer      (   441.1 +/-  20.3) ms  errors=0
IterativeAnagramsFinder       ManualCodecAlphabetizer       (   450.8 +/-   6.0) ms  errors=0
IterativeAnagramsFinder       BuiltinDecodeAlphabetizer     (   541.6 +/-  20.0) ms  errors=0
IterativeAnagramsFinder       BuiltinCodecAlphabetizer      (   428.4 +/-   9.2) ms  errors=0

Corpus: DictionaryCorpus[phoenician.txt] (size: 530760; BMP-only: false)
StreamsAnagramFinder          BMPAlphabetizer               (   343.1 +/-   9.6) ms  errors=0
StreamsAnagramFinder          ManualDecodeAlphabetizer      (   496.6 +/-  26.9) ms  errors=0
StreamsAnagramFinder          ManualCodecAlphabetizer       (   498.2 +/-   3.7) ms  errors=0
StreamsAnagramFinder          BuiltinDecodeAlphabetizer     (   570.0 +/-  22.5) ms  errors=0
StreamsAnagramFinder          BuiltinCodecAlphabetizer      (   499.4 +/-  10.6) ms  errors=0
IterativeAnagramsFinder       BMPAlphabetizer               (   340.2 +/-   3.2) ms  errors=0
IterativeAnagramsFinder       ManualDecodeAlphabetizer      (   459.3 +/-  10.1) ms  errors=0
IterativeAnagramsFinder       ManualCodecAlphabetizer       (   489.2 +/-   6.0) ms  errors=0
IterativeAnagramsFinder       BuiltinDecodeAlphabetizer     (   563.3 +/-  26.9) ms  errors=0
IterativeAnagramsFinder       BuiltinCodecAlphabetizer      (   473.7 +/-  10.4) ms  errors=0

Done.
Total runtime: 199.5 s.
```
