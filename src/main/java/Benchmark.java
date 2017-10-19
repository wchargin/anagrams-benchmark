import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.DoubleStream;

public class Benchmark {
    private static final int MIN_GROUP_SIZE = 5;
    private static final long MINIMUM_RUNTIME = (long) 2e9;

    private final List<Corpus> corpora;
    private final List<AnagramsFinder> finders;
    private final List<Alphabetizer<?>> alphabetizers;

    public Benchmark() {
        this.corpora = makeCorpora();
        this.finders = makeFinders();
        this.alphabetizers = makeAlphabetizers();
    }

    public static void main(String[] args) {
        final long startMillis = System.currentTimeMillis();
        new Benchmark().run();
        final long endMillis = System.currentTimeMillis();
        final double runtimeSeconds = (endMillis - startMillis) / 1e3;
        System.out.format("Total runtime: %.1f s.%n", runtimeSeconds);
    }

    private static List<Corpus> makeCorpora() {
        System.out.println("Constructing corpora...");
        final Random rng = new Random(0);
        final List<Corpus> corpora = new ArrayList<>();
        System.out.println("--- Random, BMP only");
        corpora.add(RandomCorpus.bmpOnly(rng));
        System.out.println("--- Random, full spectrum");
        corpora.add(RandomCorpus.fullSpectrum(rng));
        try {
            final String[] filenames = { "english.txt", "greek.txt", "phoenician.txt" };
            for (final String filename : filenames) {
                System.out.println("--- Natural language, " + filename);
                corpora.add(DictionaryCorpus.fromFile(
                        new File(Benchmark.class.getResource(filename).toURI())));
            }
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
        return Collections.unmodifiableList(corpora);
    }

    private static List<Alphabetizer<?>> makeAlphabetizers() {
        return Arrays.asList(
                new BMPAlphabetizer(),
                new ManualDecodeAlphabetizer(),
                new ManualCodecAlphabetizer(),
                new BuiltinDecodeAlphabetizer(),
                new BuiltinCodecAlphabetizer());
    }

    private static List<AnagramsFinder> makeFinders() {
        return Arrays.asList(
                new StreamsAnagramFinder(),
                new IterativeAnagramsFinder());
    }

    private static Set<Set<String>> referenceSolution(Corpus corpus) {
        return new StreamsAnagramFinder().findAnagrams(
                new BuiltinCodecAlphabetizer(), corpus.words(), MIN_GROUP_SIZE);
    }

    private void run() {
        warmUp();
        System.out.println("Beginning timing.");
        System.out.println();

        for (final Corpus corpus : corpora) {
            final Set<Set<String>> expected = referenceSolution(corpus);
            final List<String> words = corpus.words();
            System.out.format("Corpus: %s (size: %d; BMP-only: %s)%n",
                    corpus, words.size(), corpus.isBmpOnly());
            for (final AnagramsFinder finder : finders) {
                for (final Alphabetizer<?> alphabetizer : alphabetizers) {
                    timeNanoseconds(words, alphabetizer, finder, expected);
                }
            }
            System.out.println();
        }

        System.out.println("Done.");
    }

    private void warmUp() {
        System.out.println("Warming up...");
        for (final Corpus corpus : corpora) {
            final List<String> words = corpus.words();
            for (final AnagramsFinder finder : finders) {
                long bestTime = -1;
                for (final Alphabetizer<?> alphabetizer : alphabetizers) {
                    finder.findAnagrams(alphabetizer, words, MIN_GROUP_SIZE);
                }
            }
        }
    }

    private void timeNanoseconds(
            List<String> words, Alphabetizer<?> alphabetizer, AnagramsFinder finder,
            Set<Set<String>> expected) {
        final long start = System.nanoTime();
        long end;
        int trials = 0;
        Set<Set<String>> result = null;
        final List<Double> timesMillisList = new ArrayList<>();
        for (trials = 0;
             ((end = System.nanoTime()) - start) < MINIMUM_RUNTIME || trials < 5;
             trials++) {
            result = finder.findAnagrams(alphabetizer, words, MIN_GROUP_SIZE);
            timesMillisList.add((System.nanoTime() - end) / 1e6);
        }
        final double[] timesMillis = timesMillisList.stream().mapToDouble(x -> x).toArray();
        final double mean = DoubleStream.of(timesMillis).average().getAsDouble();
        final double stddev =
                Math.sqrt(DoubleStream.of(timesMillis)
                        .map(x -> Math.pow(x - mean, 2)).sum() / (trials - 1))
                / Math.sqrt(trials);
        final Set<Set<String>> finalResult = result;
        final long errors = finalResult.stream().filter(g -> !expected.contains(g)).count() +
                expected.stream().filter(g -> !finalResult.contains(g)).count();
        System.out.format("%-28s  %-28s  (%8.1f +/- %5.1f) ms  errors=%s%n",
                finder.getClass().getName(),
                alphabetizer.getClass().getName(),
                mean, stddev, errors);
    }
}
