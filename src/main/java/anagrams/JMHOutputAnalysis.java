package anagrams;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class JMHOutputAnalysis {

    private static final Pattern MANY_SPACE_PLUS_MINUS = Pattern.compile("[\\s±]+");
    private static final DecimalFormat Z_SCORE = new DecimalFormat("+0.00;-0");

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Please pass a single argument, with value the path to a file");
            System.err.println("containing the output of the JMH benchmark.");
            System.err.println("");
            System.err.println("If invoking from gradle, you can run:");
            System.err.println("    ./gradlew analyze -PbenchmarkOutput='path/to/file'");
            System.exit(1);
        }
        final File benchmarkOutputFilename = new File(args[0]);
        final JMHOutput output = JMHOutput.fromFile(benchmarkOutputFilename,
                "JMHBenchmark.alphabetizeAllWords");
        output.describe();
    }

    /**
     * Creates a string of the given length, all of whose characters are the given character.
     *
     * @param c
     *         the character to appear at every position of the string
     * @param length
     *         the length of the string to create
     * @return a string {@code s} of length {@code length} such that {@code s.charAt(i) == c} for
     * every {@code i}
     */
    private static String constantString(char c, int length) {
        char[] cs = new char[length];
        Arrays.fill(cs, c);
        return new String(cs);
    }

    /**
     * Extends {@code s} to at least {@code minimumLength} characters by left-padding if necessary.
     *
     * @param s
     *         the base string
     * @param minimumLength
     *         the minimum number of characters in the result string
     * @return {@code s} if {@code s} has at least {@code minimumLength} characters, or
     * {@code spaces + s} otherwise, where {@code spaces} is a string all of whose characters are
     * spaces and {@code spaces + s} has length {@code minimumLength}
     */
    private static String padded(String s, int minimumLength) {
        final int paddingLength = Math.max(0, minimumLength - s.length());
        return constantString(' ', paddingLength) + s;
    }

    /**
     * Converts {@code element} to a string that is left-padded with spaces until it is as long
     * as the longest name of any enum constant of the same type.
     *
     * @param element
     *         any non-{@code null} enum constant
     * @param <E>
     *         any enum type
     * @return a string {@code spaces + element.toString()}, where every character in
     * {@code spaces} is a space and the combined string has length the maximum value of
     * {@code x.toString()} for any enum constant {@code x} of type {@code E}
     */
    private static <E extends Enum<E>> String paddedEnum(E element) {
        Objects.requireNonNull(element);
        @SuppressWarnings("unchecked") final Class<E> clazz = (Class<E>) element.getClass();
        // The following call to `getAsInt` must succeed, because the type `E` has at least one
        // enum constant: namely, `element`.
        final int maxLength =
                Arrays.stream(clazz.getEnumConstants())
                        .map(E::toString).mapToInt(String::length).max()
                        .getAsInt();
        return padded(element.toString(), maxLength);
    }

    /**
     * A structured representation of (the important parts of) a file containing output from the
     * JMH benchmark.
     */
    private static class JMHOutput {
        /**
         * The number of standard deviations away from the mean that a quantity must be for it to
         * be considered statistically significant.
         */
        private static final double SIGNIFICANCE_LEVEL = 3.0;

        /**
         * The average time per op for each trial.
         */
        final Map<TrialKey, UncertainQuantity> trials;

        /**
         * The units used for the average time per op, and therefore the quantities in
         * {@code trials}.
         */
        final String units;

        private JMHOutput(Map<TrialKey, UncertainQuantity> trials, String units) {
            this.trials = Collections.unmodifiableMap(trials);
            this.units = units;
        }

        /**
         * Print a nicely formatted table ranking the keys in order of ascending times, showing
         * their consecutive differences and associated <i>z</i>-scores.
         *
         * @param keys
         *         the objects to rank
         * @param times
         *         a list of times such that {@code times.get(i)} is the time for key
         *         {@code keys.get(i)}
         * @param units
         *         the units of measurement for the {@code times}
         * @param fractionDigits
         *         the number of fractional digits to display for the times
         * @param <E>
         *         the key type
         */
        private static <E> void printRankingTable(
                List<E> keys, List<UncertainQuantity> times, String units, int fractionDigits) {
            if (keys.size() != times.size()) {
                throw new IllegalArgumentException(
                        String.format("Size mismatch: %d vs. %d", keys.size(), times.size()));
            }
            if (keys.isEmpty()) {
                return;
            }
            final List<E> sortedByTime = new ArrayList<>(keys);
            final Map<E, Integer> originalIndex = new HashMap<>();
            for (int i = 0; i < keys.size(); i++) {
                originalIndex.put(keys.get(i), i);
            }
            sortedByTime.sort(Comparator.comparing(x -> times.get(originalIndex.get(x))));

            final int minimumLength = keys.stream()
                    .mapToInt(x -> x.toString().length()).max()
                    .orElseThrow(() -> new AssertionError("Empty keys?"));
            System.out.printf("%s  fastest%n", padded(
                    sortedByTime.get(0).toString(), minimumLength));

            final List<UncertainQuantity> deltas = new ArrayList<>();
            for (int i = 1; i < sortedByTime.size(); i++) {
                final E here = sortedByTime.get(i);
                final E previous = sortedByTime.get(i - 1);
                final UncertainQuantity delta = UncertainQuantity.difference(
                        times.get(originalIndex.get(here)),
                        times.get(originalIndex.get(previous)));
                deltas.add(delta);
            }
            final List<String> deltaStrings = UncertainQuantity.toStrings(
                    fractionDigits, deltas);

            for (int i = 1; i < sortedByTime.size(); i++) {
                final int deltaIndex = i - 1;
                final UncertainQuantity delta = deltas.get(deltaIndex);
                final String deltaString = deltaStrings.get(deltaIndex);
                final double z = delta.uncertaintyRatio();
                System.out.printf("%s  slower by %s %s (z-score: %s)%n",
                        padded(sortedByTime.get(i).toString(), minimumLength),
                        deltaString,
                        units,
                        Z_SCORE.format(z));
            }
        }

        /**
         * Parses a file containing JMH benchmark output.
         *
         * @param file
         *         a file containing JMH benchmark output
         * @param benchmarkName
         *         the name of the benchmark method for which to extract trials
         * @return a new {@code JMHOutput} object
         * @throws IOException
         *         if there is a problem reading from the file
         */
        private static JMHOutput fromFile(File file, String benchmarkName) throws IOException {
            final List<String> lines = Files.readAllLines(file.toPath());
            final Iterator<String> it = lines.iterator();
            if (!it.hasNext()) {
                throw new IllegalArgumentException("Empty input file");
            }
            final String headerLine = it.next();
            final String[] headers = MANY_SPACE_PLUS_MINUS.split(headerLine);

            final ToIntFunction<String> matchingColumnIndex = (keyword) -> {
                int match = -1;
                for (int i = 0; i < headers.length; i++) {
                    if (headers[i].contains(keyword)) {
                        if (match != -1) {
                            throw new IllegalArgumentException("Duplicate column matching: " + keyword);
                        }
                        match = i;
                    }
                }
                if (match == -1) {
                    throw new IllegalArgumentException("No column matching: " + keyword);
                }
                return match;
            };

            final int benchmarkColumn = matchingColumnIndex.applyAsInt("Benchmark");
            final int corpusColumn = matchingColumnIndex.applyAsInt("corpus");
            final int finderColumn = matchingColumnIndex.applyAsInt("finder");
            final int alphabetizerColumn = matchingColumnIndex.applyAsInt("alphabetizer");
            final int scoreColumn = matchingColumnIndex.applyAsInt("Score");
            final int errorColumn = matchingColumnIndex.applyAsInt("Error");
            final int unitsColumn = matchingColumnIndex.applyAsInt("Units");
            final AtomicReference<String> units = new AtomicReference<>(null);

            final Map<TrialKey, UncertainQuantity> result = new HashMap<>();
            it.forEachRemaining(line -> {
                final String[] parts = MANY_SPACE_PLUS_MINUS.split(line);
                if (!parts[benchmarkColumn].equals(benchmarkName)) {
                    return;
                }

                final String theseUnits = parts[unitsColumn];
                final String thoseUnits = units.get();
                if (thoseUnits == null) {
                    units.set(theseUnits);
                } else if (!theseUnits.equals(thoseUnits)) {
                    throw new IllegalArgumentException(
                            "Unit mismatch: " + thoseUnits + ", " + theseUnits);
                }

                final TrialKey key = new TrialKey(
                        BenchmarkParameters.CorpusSpecification.valueOf(
                                parts[corpusColumn]),
                        BenchmarkParameters.AnagramsFinderSpecification.valueOf(
                                parts[finderColumn]),
                        BenchmarkParameters.AlphabetizerSpecification.valueOf(
                                parts[alphabetizerColumn]));
                if (result.containsKey(key)) {
                    throw new IllegalArgumentException("Duplicate trial: " + key);
                }
                final UncertainQuantity score = new UncertainQuantity(
                        Double.parseDouble(parts[scoreColumn]),
                        Double.parseDouble(parts[errorColumn]));
                result.put(key, score);
            });
            return new JMHOutput(result, units.get());
        }

        public void describe() {
            final PrintStream oldOut = System.out;
            final IndentingStream indentation = new IndentingStream(oldOut);
            System.setOut(new PrintStream(indentation));

            try {
                final List<BenchmarkParameters.CorpusSpecification> corpora =
                        trials.keySet().stream().map(x -> x.corpus).distinct().sorted()
                                .collect(Collectors.toList());
                final String corpusTitle = "Analysis by corpus";
                System.out.println(corpusTitle);
                System.out.println(constantString('=', corpusTitle.length()));

                corpora.forEach(c -> {
                    System.out.println();
                    final String title = "Analyzing corpus: " + c;
                    System.out.println(title);
                    System.out.println(constantString('-', title.length()));
                    indentation.indent();
                    describeCorpusBehavior(c);
                    indentation.dedent();
                });
                System.out.print("\n\n");

                final List<BenchmarkParameters.AlphabetizerSpecification> alphabetizers =
                        trials.keySet().stream().map(x -> x.alphabetizer).distinct().sorted()
                                .collect(Collectors.toList());
                final String algorithmTitle = "Analysis by alphabetization algorithm";
                System.out.println(algorithmTitle);
                System.out.println(constantString('=', algorithmTitle.length()));
                alphabetizers.forEach(a -> {
                    System.out.println();
                    final String title = "Analyzing algorithm: " + a;
                    System.out.println(title);
                    System.out.println(constantString('-', title.length()));
                    indentation.indent();
                    describeAlphabetizerBehavior(a);
                    indentation.dedent();
                });

                {
                    System.out.println();
                    final String title = "Overall analysis";
                    System.out.println(title);
                    System.out.println(constantString('=', title.length()));
                    indentation.indent();
                    describeOverall();
                    indentation.dedent();
                }
            } finally {
                System.setOut(oldOut);
            }
        }

        private void describeOverall() {
            System.out.println("Average performance by algorithm, fastest to slowest:");
            final Map<BenchmarkParameters.AlphabetizerSpecification, UncertainQuantity> meanTimes =
                    trials.keySet().stream().collect(Collectors.groupingBy(x -> x.alphabetizer,
                            Collectors.collectingAndThen(
                                    Collectors.mapping(trials::get, Collectors.toList()),
                                    UncertainQuantity::mean)));
            final List<BenchmarkParameters.AlphabetizerSpecification> sortedByTime =
                    meanTimes.keySet().stream()
                            .sorted(Comparator.comparing(meanTimes::get))
                            .collect(Collectors.toList());
            final List<String> meanTimeStrings = UncertainQuantity.toStrings(
                    sortedByTime.stream().map(meanTimes::get).collect(Collectors.toList()));
            for (int i = 0; i < sortedByTime.size(); i++) {
                System.out.printf("%s  %s %s%n",
                        paddedEnum(sortedByTime.get(i)), meanTimeStrings.get(i), units);
            }
            System.out.println("(Weighted uniformly across corpus and iterative/streamy.)");

            if (sortedByTime.size() > 1) {
                System.out.println();
                System.out.println("Consecutive differences:");
                printRankingTable(sortedByTime,
                        sortedByTime.stream().map(meanTimes::get).collect(Collectors.toList()),
                        units,
                        2);
            }
        }

        /**
         * Describes the behavior of the provided algorithm on different corpora. This
         * computes the average improvement due to using iteration instead of streams, and also
         * displays the average times on each corpus and the differences between
         * consecutively-ranked times.
         *
         * @param alphabetizer
         *         an algorithm to analyze
         */
        private void describeAlphabetizerBehavior(
                BenchmarkParameters.AlphabetizerSpecification alphabetizer) {
            final List<UncertainQuantity> improvementsFromIteration = new ArrayList<>();
            final List<BenchmarkParameters.CorpusSpecification> corpora = new ArrayList<>();
            final List<UncertainQuantity> averageScores = new ArrayList<>();
            final List<UncertainQuantity> averageNormalizedScores = new ArrayList<>();
            // We'll compute "average time per <this many> words."
            final int normalizationUnitWords = 1000;

            for (BenchmarkParameters.CorpusSpecification corpus :
                    BenchmarkParameters.CorpusSpecification.values()) {
                final UncertainQuantity iterative = iterativeTime(alphabetizer, corpus);
                final UncertainQuantity streamy = streamyTime(alphabetizer, corpus);
                if (iterative == null || streamy == null) {
                    continue;
                }
                // The benefit to the iterative version is the _extra_ time taken by the streamy
                // version, so we subtract "streamy minus iterative".
                corpora.add(corpus);
                improvementsFromIteration.add(UncertainQuantity.difference(streamy, iterative));
                averageScores.add(UncertainQuantity.mean(iterative, streamy));
                averageNormalizedScores.add(
                        UncertainQuantity.mean(iterative, streamy).scale(
                                (double) normalizationUnitWords / corpus.size()));
            }
            if (improvementsFromIteration.isEmpty()) {
                System.out.println("Insufficient data for meaningful answer.");
                return;
            }

            final UncertainQuantity averageImprovement = UncertainQuantity.mean(
                    improvementsFromIteration.toArray(new UncertainQuantity[0]));
            System.out.println("Average improvement of iterative version over streamy version:");
            System.out.printf("    %s %s (z-score: %s)%n",
                    averageImprovement,
                    units,
                    Z_SCORE.format(averageImprovement.uncertaintyRatio()));
            System.out.println("(Average taken with uniform weight across all corpora.)");

            System.out.println();
            System.out.println("Average performance for each corpus:");
            final List<String> averageScoreStrings = UncertainQuantity.toStrings(averageScores);
            for (int i = 0; i < corpora.size(); i++) {
                final BenchmarkParameters.CorpusSpecification corpus = corpora.get(i);
                System.out.printf("%s  %s %s  (corpus size: %d %s)%n",
                        paddedEnum(corpus), averageScoreStrings.get(i), units,
                        corpus.size(), corpus.size() == 1 ? "word" : "words");
            }
            System.out.println("(Remember that corpora are not equally sized.)");

            System.out.println();
            System.out.println("Average performance for each corpus, normalized by word count:");
            final List<String> averageNormalizedScoreStrings =
                    UncertainQuantity.toStrings(4, averageNormalizedScores);
            for (int i = 0; i < corpora.size(); i++) {
                final BenchmarkParameters.CorpusSpecification corpus = corpora.get(i);
                System.out.printf("%s  %s %s per %d words%n",
                        paddedEnum(corpus),
                        averageNormalizedScoreStrings.get(i),
                        units,
                        normalizationUnitWords);
            }
            System.out.println("" +
                    "(Remember that even after this normalization, corpora may have different " +
                    "word-length distributions.)");

            if (corpora.size() > 1) {
                System.out.println();
                System.out.println("Ranking, with consecutive-pair differences:");
                printRankingTable(corpora, averageNormalizedScores,
                        String.format("%s per %d words", units, normalizationUnitWords),
                        4);
            }
        }

        private UncertainQuantity streamyTime(
                BenchmarkParameters.AlphabetizerSpecification alphabetizer,
                BenchmarkParameters.CorpusSpecification corpus) {
            final TrialKey key = new TrialKey(
                    corpus,
                    BenchmarkParameters.AnagramsFinderSpecification.STREAMY,
                    alphabetizer);
            return trials.get(key);
        }

        private UncertainQuantity iterativeTime(
                BenchmarkParameters.AlphabetizerSpecification alphabetizer,
                BenchmarkParameters.CorpusSpecification corpus) {
            final TrialKey key = new TrialKey(
                    corpus,
                    BenchmarkParameters.AnagramsFinderSpecification.ITERATIVE,
                    alphabetizer);
            return trials.get(key);
        }


        /**
         * Describes the behavior of different algorithms on a specific corpus. For each
         * algorithm, this computes and presents the timing difference between the iterative and
         * streamy versions. Furthermore, this prints the average times for each algorithm, ranks
         * the algorithms, and computes the amounts to which each algorithm is faster than the
         * previous.
         *
         * @param corpus
         *         a corpus to analyze
         */
        private void describeCorpusBehavior(BenchmarkParameters.CorpusSpecification corpus) {
            final List<BenchmarkParameters.AlphabetizerSpecification> keys = new ArrayList<>();
            final List<UncertainQuantity> improvementFromIteration = new ArrayList<>();
            final List<UncertainQuantity> averageScores = new ArrayList<>();

            for (BenchmarkParameters.AlphabetizerSpecification alphabetizer :
                    BenchmarkParameters.AlphabetizerSpecification.values()) {
                final UncertainQuantity iterative = iterativeTime(alphabetizer, corpus);
                final UncertainQuantity streamy = streamyTime(alphabetizer, corpus);
                if (iterative == null || streamy == null) {
                    continue;
                }
                keys.add(alphabetizer);
                // The benefit to the iterative version is the _extra_ time taken by the streamy
                // version, so we subtract "streamy minus iterative".
                improvementFromIteration.add(UncertainQuantity.difference(streamy, iterative));
                averageScores.add(UncertainQuantity.mean(iterative, streamy));
            }

            // Display iteration improvement for each algorithm.
            System.out.println("Improvement of iterative version over streamy version:");
            final List<String> benefitStrings =
                    UncertainQuantity.toStrings(improvementFromIteration);
            for (int i = 0; i < keys.size(); i++) {
                System.out.format("%s  %s %s  (z-score: %s)%n",
                        paddedEnum(keys.get(i)),
                        benefitStrings.get(i),
                        units,
                        Z_SCORE.format(improvementFromIteration.get(i).uncertaintyRatio()));
            }

            // Display average time per op for each algorithm.
            System.out.println();
            System.out.println("Average time per op:");
            final List<String> averageScoreStrings = UncertainQuantity.toStrings(averageScores);
            for (int i = 0; i < keys.size(); i++) {
                System.out.format("%s  %s %s%n",
                        paddedEnum(keys.get(i)), averageScoreStrings.get(i), units);
            }
            System.out.println(
                    "(Averages taken by weighting iterative/streamy versions uniformly.)");

            // Rank different algorithms by average time.
            if (keys.size() > 1) {
                System.out.println();
                System.out.println("Ranking, with consecutive-pair differences:");
                printRankingTable(keys, averageScores, units, 2);
            }
        }

        /**
         * A stream that adds indentation whenever a newline is printed. This is a bit of a hack,
         * and certainly performs terribly, but we don't to generate tons of output and it is
         * pretty convenient to just call {@link System#setOut(PrintStream)}.
         */
        private static class IndentingStream extends OutputStream {
            /**
             * The number of spaces per tab stop.
             */
            private static final int SHIFT_WIDTH = 4;

            /**
             * The backing stream, to which {@link #write(int)} defers.
             */
            private final OutputStream target;

            /**
             * The number of logical indentations (tab stops) currently active.
             */
            private int indentationLevel = 0;

            /**
             * True when the stream is initialized or when the last byte written was a newline. When
             * a non-newline is written while this flag is set, the flag is cleared and indentation
             * is written.
             */
            private boolean atBeginningOfLine = true;

            public IndentingStream(OutputStream target) {
                this.target = target;
            }

            /**
             * Increases the indentation level by one.
             */
            public void indent() {
                indentationLevel++;
            }

            /**
             * Decreases the indentation level by one, if it is positive, or else does nothing.
             */
            public void dedent() {
                indentationLevel = Math.max(indentationLevel - 1, 0);
            }

            private void writeIndent() throws IOException {
                byte[] spaces = new byte[indentationLevel * SHIFT_WIDTH];
                Arrays.fill(spaces, (byte) ' ');
                target.write(spaces);
            }

            @Override
            public void write(int i) throws IOException {
                final byte b = (byte) (i & 0xFF);
                if (b == '\n' || b == '\r') {
                    atBeginningOfLine = true;
                } else if (atBeginningOfLine) {
                    writeIndent();
                    atBeginningOfLine = false;
                }
                target.write(i);
            }
        }
    }

    /**
     * <p>
     * A quantity with an estimate and an uncertainty, like
     * &ldquo;4.2&nbsp;&plusmn;&nbsp;0.1&rdquo;.
     * </p>
     * <p>
     * The natural ordering of this class is the natural ordering of its {@link #estimate()}s.
     * </p>
     */
    private static final class UncertainQuantity implements Comparable<UncertainQuantity> {
        private final double estimate;
        private final double uncertainty;

        public UncertainQuantity(double estimate, double uncertainty) {
            this.estimate = estimate;
            this.uncertainty = uncertainty;
        }

        /**
         * Computes the sum of all the provided uncertain quantities, which are assumed to be
         * uncorrelated.
         *
         * @param quantities
         *         zero or more quantities whose sum to compute
         * @return a quantity whose estimate is the sum of the estimates of the provided
         * quantities, and whose uncertainty is the square-root-of-sum-of-squares of the
         * estimates of the provided quantities
         */
        public static UncertainQuantity sum(UncertainQuantity... quantities) {
            return new UncertainQuantity(
                    Arrays.stream(quantities).mapToDouble(UncertainQuantity::estimate).sum(),
                    Math.sqrt(
                            Arrays.stream(quantities).mapToDouble(UncertainQuantity::uncertainty)
                                    .map(x -> x * x).sum()));
        }

        /**
         * Computes the arithmetic mean of all the provided uncertain quantities, which are
         * assumed to be uncorrelated.
         *
         * @param quantities
         *         one or more quantities whose mean to compute
         * @return a quantity whose estimate is the mean of the estimates of the provided
         * quantities, and whose uncertainty is the square-root-of-sum-of-squares of the
         * estimates of the provided quantities, all divided by {@code n}, the length of the input
         * @throws IllegalArgumentException
         *         if {@code quantities.length == 0}
         */
        public static UncertainQuantity mean(UncertainQuantity... quantities) {
            if (quantities.length == 0) {
                throw new IllegalArgumentException("Empty input");
            }
            return sum(quantities).scale(1.0 / quantities.length);
        }

        /**
         * Equivalent to {@link #mean(UncertainQuantity...)}. The argument must be non-{@code null}.
         */
        public static UncertainQuantity mean(List<UncertainQuantity> quantities) {
            return mean(quantities.toArray(new UncertainQuantity[0]));
        }

        /**
         * Computes the signed difference {@code a - b}, assuming that {@code a} and {@code b} are
         * uncorrelated. Absolute difference can be computed by invoking {@link #abs()} on the
         * result.
         *
         * @param a
         *         the quantity from which {@code b} will be subtracted
         * @param b
         *         the quantity to subtract from {@code a}
         * @return the difference {@code a - b}
         */
        public static UncertainQuantity difference(UncertainQuantity a, UncertainQuantity b) {
            return sum(a, b.scale(-1));
        }

        /**
         * Equivalent to {@link #toStrings(UncertainQuantity...)}. The argument must be
         * non-{@code null}.
         */
        public static List<String> toStrings(List<UncertainQuantity> quantities) {
            return Arrays.asList(toStrings(quantities.toArray(new UncertainQuantity[0])));
        }

        /**
         * Equivalent to {@link #toStrings(int, UncertainQuantity...)}. The list argument must be
         * non-{@code null}.
         */
        public static List<String> toStrings(
                int fractionDigits, List<UncertainQuantity> quantities) {
            return Arrays.asList(toStrings(
                    fractionDigits, quantities.toArray(new UncertainQuantity[0])));
        }

        /**
         * Converts each uncertain quantity to a string such that the decimal points and
         * plus-or-minus signs line up across all strings: that is, the strings are all mutually
         * aligned.
         *
         * @param quantities
         *         zero or more quantities to convert to strings
         * @return an array {@code strings} such that {@code strings[i]} is a string representing
         * the quantity {@code quantities[i]}
         */
        public static String[] toStrings(UncertainQuantity... quantities) {
            return toStrings(2, quantities);
        }

        /**
         * Converts each uncertain quantity to a string such that the decimal points and
         * plus-or-minus signs line up across all strings: that is, the strings are all mutually
         * aligned. Each string will have {@code fractionDigits} digits after the decimal point.
         *
         * @param fractionDigits
         *         the number of digits to retain after the decimal point
         * @param quantities
         *         zero or more quantities to convert to strings
         * @return an array {@code strings} such that {@code strings[i]} is a string representing
         * the quantity {@code quantities[i]}
         */
        public static String[] toStrings(int fractionDigits, UncertainQuantity... quantities) {
            if (quantities.length == 0) {
                return new String[0];
            }
            final DecimalFormat format = new DecimalFormat();
            format.setMinimumFractionDigits(fractionDigits);
            format.setMaximumFractionDigits(fractionDigits);

            final String[] estimates = new String[quantities.length];
            final String[] uncertainties = new String[quantities.length];
            int maxEstimateLength = -1;
            int maxUncertaintyLength = -1;
            for (int i = 0; i < quantities.length; i++) {
                estimates[i] = format.format(quantities[i].estimate);
                uncertainties[i] = format.format(quantities[i].uncertainty);
                maxEstimateLength = Math.max(maxEstimateLength, estimates[i].length());
                maxUncertaintyLength = Math.max(maxUncertaintyLength, uncertainties[i].length());
            }

            final String[] results = new String[uncertainties.length];
            for (int i = 0; i < results.length; i++) {
                results[i] = String.format("%s ± %s",
                        padded(estimates[i], maxEstimateLength),
                        padded(uncertainties[i], maxUncertaintyLength));
            }
            return results;
        }

        @Override
        public String toString() {
            return toStrings(this)[0];
        }

        /**
         * Scales this quantity by a constant.
         *
         * @param k
         *         the constant by which to scale this quantity
         * @return a quantity whose estimate is {@code k} times this quantity's estimate, and
         * whose uncertainty is {@code |k|} times this quantity's uncertainty
         */
        public UncertainQuantity scale(double k) {
            return new UncertainQuantity(estimate * k, uncertainty * Math.abs(k));
        }

        /**
         * Computes this quantity's absolute value.
         *
         * @return a quantity whose estimate is equal to the absolute value of this quantity's
         * estimate, and with the same uncertainty as this quantity
         */
        public UncertainQuantity abs() {
            return estimate < 0 ? this.scale(-1) : this;
        }

        /**
         * Computes the uncertainty ratio of a quantity centered around zero: for a quantity
         * <i>z</i>&nbsp;&plusmn;&nbsp;<i>&sigma;</i>, this is <i>z</i>&nbsp;/&nbsp;<i>&sigma;</i>.
         *
         * @return the estimate of this quantity divided by its uncertainty
         */
        public double uncertaintyRatio() {
            return estimate / uncertainty;
        }

        /**
         * Gets the estimate for this uncertain quantity.
         *
         * @return the estimate for this quantity
         */
        public double estimate() {
            return estimate;
        }

        /**
         * Gets the uncertainty associated with this uncertain quantity.
         *
         * @return the estimate for this uncertainty
         */
        public double uncertainty() {
            return uncertainty;
        }

        @Override
        public int compareTo(UncertainQuantity that) {
            return Double.compare(this.estimate, that.estimate);
        }
    }

    /**
     * A key containing the parameters to a benchmark: namely, specifications for a corpus, an
     * anagrams finder, and an alphabetizer.
     */
    private static final class TrialKey {
        public final BenchmarkParameters.CorpusSpecification corpus;
        public final BenchmarkParameters.AnagramsFinderSpecification finder;
        public final BenchmarkParameters.AlphabetizerSpecification alphabetizer;

        private TrialKey(
                BenchmarkParameters.CorpusSpecification corpus,
                BenchmarkParameters.AnagramsFinderSpecification finder,
                BenchmarkParameters.AlphabetizerSpecification alphabetizer) {
            this.corpus = Objects.requireNonNull(corpus, "corpus");
            this.finder = Objects.requireNonNull(finder, "finder");
            this.alphabetizer = Objects.requireNonNull(alphabetizer, "alphabetizer");
        }

        @Override
        public String toString() {
            return "TrialKey{" +
                    "corpus=" + corpus +
                    ", finder=" + finder +
                    ", alphabetizer=" + alphabetizer +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TrialKey)) {
                return false;
            }
            final TrialKey k = (TrialKey) o;
            return corpus == k.corpus && finder == k.finder && alphabetizer == k.alphabetizer;
        }

        @Override
        public int hashCode() {
            int result = 0;
            result = 31 * result + corpus.hashCode();
            result = 31 * result + finder.hashCode();
            result = 31 * result + alphabetizer.hashCode();
            return result;
        }
    }
}
