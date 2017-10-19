import java.util.ArrayList;
import java.util.List;

/**
 * A corpus that tries to have as many surrogate-pair collisions as possible, by exhaustively
 * listing surrogate pairs in a small range.
 */
public class SupercolliderCorpus implements Corpus {

    private static final int HIGH_SURROGATE_RANGE = 3;
    private static final int LOW_SURROGATE_RANGE = 3;
    private static final int WORD_LENGTH = 6;

    /**
     * Keep this value in mind when adjusting the previous parameters!
     */
    private static final int CORPUS_SIZE = (int) Math.pow(
            HIGH_SURROGATE_RANGE * LOW_SURROGATE_RANGE, WORD_LENGTH);

    private final List<String> words;

    public SupercolliderCorpus() {
        words = generateWords();
    }

    private List<String> generateWords() {
        final int[] codePoints = new int[WORD_LENGTH];
        final List<String> result = new ArrayList<>();
        populateCorpus(codePoints, result, 0);
        if (result.size() != CORPUS_SIZE) {
            throw new AssertionError("Wrong corpus size: " + result.size() + " vs. " + CORPUS_SIZE);
        }
        return result;
    }

    /**
     * For each {@code word} in the desired corpus such that the first {@code offset} values of
     * {@code codePoints} form a prefix of the code points in {@code word}, add {@code word} to
     * the {@code result}. Only indices of {@code offset} or greater may be mutated in {@code
     * codePoints}.
     */
    private void populateCorpus(int[] codePoints, List<String> result, int offset) {
        if (offset >= WORD_LENGTH) {
            result.add(new String(codePoints, 0, codePoints.length));
        } else {
            for (int hi = 0; hi < HIGH_SURROGATE_RANGE; hi++) {
                for (int lo = 0; lo < LOW_SURROGATE_RANGE; lo++) {
                    codePoints[offset] = Character.toCodePoint(
                            (char) (Character.MIN_HIGH_SURROGATE + hi),
                            (char) (Character.MIN_LOW_SURROGATE + lo));
                    populateCorpus(codePoints, result, offset + 1);
                }
            }
        }
    }

    @Override
    public boolean isBmpOnly() {
        return false;
    }

    @Override
    public List<String> words() {
        return words;
    }

    @Override
    public String toString() {
        return "SupercolliderCorpus[]";
    }
}
