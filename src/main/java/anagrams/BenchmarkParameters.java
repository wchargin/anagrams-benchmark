package anagrams;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.function.Supplier;

/**
 * A class containing various options that apply to a particular trial of a benchmark.
 */
public final class BenchmarkParameters {
    private BenchmarkParameters() {
        throw new AssertionError("Do not instantiate.");
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
        RANDOM_BMP(
                () -> RandomCorpus.bmpOnly(new Random(0)),
                1_000_000),
        RANDOM_FULL(
                () -> RandomCorpus.fullSpectrum(new Random(0)),
                1_000_000),
        SUPERCOLLIDER(
                SupercolliderCorpus::new, 531441),
        NATURAL_ENGLISH(
                () -> dictionaryCorpus("english.txt"), 64001),
        NATURAL_GREEK(
                () -> dictionaryCorpus("greek.txt"), 530760),
        NATURAL_PHOENICIAN(
                () -> dictionaryCorpus("phoenician.txt"), 530760);

        private final Supplier<Corpus> supplier;
        private final int corpusSize;

        private CorpusSpecification(Supplier<Corpus> supplier, int corpusSize) {
            this.supplier = supplier;
            this.corpusSize = corpusSize;
        }

        private static Corpus dictionaryCorpus(String filename) {
            final File resource;
            try {
                InputStream stream = CorpusSpecification.class.getResourceAsStream(filename);
                return DictionaryCorpus.fromInputStream(filename, stream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public Corpus newCorpus() {
            final Corpus corpus = supplier.get();
            final int actualSize = corpus.words().size();
            if (corpus.words().size() != corpusSize) {
                throw new AssertionError(
                        String.format(
                                "Corpus size mismatch: expected %s, got %s",
                                corpusSize, actualSize));
            }
            return corpus;
        }

        public int size() {
            return corpusSize;
        }
    }
}
