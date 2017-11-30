package anagrams;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A benchmark of anagram finder implementations, some more robust than others, on various corpora.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class JMHBenchmark {
    @Param
    private BenchmarkParameters.CorpusSpecification corpusSpec;

    @Param
    private BenchmarkParameters.AnagramsFinderSpecification finderSpec;

    @Param
    private BenchmarkParameters.AlphabetizerSpecification alphabetizerSpec;

    @Param({ "5" })
    private int minGroupSize;

    private List<String> words;
    private AnagramsFinder finder;
    private Alphabetizer<?> alphabetizer;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + JMHBenchmark.class.getSimpleName() + ".*")
                .build();
        new Runner(opt).run();
    }

    @Setup
    public void initialize() {
        final Corpus corpus = corpusSpec.newCorpus();
        words = corpus.words();
        finder = finderSpec.createFinder();
        alphabetizer = alphabetizerSpec.createAlphabetizer();
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Fork(1)
    public void baseline() {
        // This method intentionally left blank.
    }

    @Benchmark
    @Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 10, time = 5, timeUnit = TimeUnit.SECONDS)
    @Fork(2)
    public Set<Set<String>> alphabetizeAllWords() {
        return finder.findAnagrams(alphabetizer, words, minGroupSize);
    }
}
