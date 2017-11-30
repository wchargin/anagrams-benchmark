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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A benchmark of anagram finder implementations, some more robust than others, on various corpora.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class JMHBenchmark {
    @Param
    private CorpusSpecification corpusSpec;

    @Param
    private AnagramsFinderSpecification finderSpec;

    @Param
    private AlphabetizerSpecification alphabetizerSpec;

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

    /**
     * A choice of {@link Alphabetizer} implementation to be used in the benchmark.
     */
    public enum AlphabetizerSpecification {
        NAIVE_BMP(BMPAlphabetizer::new),
        MANUAL_DECODE(ManualDecodeAlphabetizer::new),
        MANUAL_CODEC(ManualCodecAlphabetizer::new),
        BUILTIN_DECODE(BuiltinDecodeAlphabetizer::new),
        BUILTIN_CODEC(BuiltinCodecAlphabetizer::new);

        private final Supplier<? extends Alphabetizer<?>> supplier;

        private AlphabetizerSpecification(Supplier<? extends Alphabetizer<?>> supplier) {
            this.supplier = supplier;
        }

        public Alphabetizer<?> createAlphabetizer() {
            return supplier.get();
        }
    }

    /**
     * A choice of {@link AnagramsFinder} implementation to be used in the benchmark.
     */
    public enum AnagramsFinderSpecification {
        STREAMY(StreamsAnagramFinder::new),
        ITERATIVE(IterativeAnagramsFinder::new);

        private final Supplier<? extends AnagramsFinder> supplier;

        private AnagramsFinderSpecification(Supplier<? extends AnagramsFinder> supplier) {
            this.supplier = supplier;
        }

        public AnagramsFinder createFinder() {
            return supplier.get();
        }
    }

    /**
     * A choice of {@link Corpus} to be used in the benchmark.
     */
    public enum CorpusSpecification {
        RANDOM_BMP(() -> RandomCorpus.bmpOnly(new Random(0))),
        RANDOM_FULL(() -> RandomCorpus.fullSpectrum(new Random(0))),
        SUPERCOLLIDER(SupercolliderCorpus::new),
        NATURAL_ENGLISH(() -> dictionaryCorpus("english.txt")),
        NATURAL_GREEK(() -> dictionaryCorpus("greek.txt")),
        NATURAL_PHOENICIAN(() -> dictionaryCorpus("phoenician.txt"));

        private final Supplier<Corpus> supplier;

        private CorpusSpecification(Supplier<Corpus> supplier) {
            this.supplier = supplier;
        }

        private static Corpus dictionaryCorpus(String filename) {
            final File resource;
            try {
                InputStream stream = JMHBenchmark.class.getResourceAsStream(filename);
                return DictionaryCorpus.fromInputStream(filename, stream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public Corpus newCorpus() {
            return supplier.get();
        }
    }
}
