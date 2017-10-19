# anagrams-benchmark

Benchmarks for different implementations of a program designed to find
groups of anagrams in a corpus. Some of these implementations unsoundly
assume that the code points are all within the basic multilingual plane,
while others work on any valid Unicode strings. The purpose of this
repository is to determine the price that we pay for correctness: how
much slower are the versions that work in all cases?
