package anagrams;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;

class JMHOutputAnalysis {

    private static final Pattern MANY_SPACE_PLUS_MINUS = Pattern.compile("[\\s±]+");
    private static final DecimalFormat SIGNED_NUMBER = new DecimalFormat("+0.00;-0");

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
     * A structured representation of (the important parts of) a file containing output from the
     * JMH benchmark.
     */
    private static class JMHOutput {
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

        /**
         * Analyzes the output and prints a description.
         */
        public void describe() {
            trials.forEach((k, v) -> System.out.printf("%s: %s %s%n", k, v, units));
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
