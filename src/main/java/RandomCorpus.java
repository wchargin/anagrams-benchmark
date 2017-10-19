import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomCorpus implements Corpus {

    private static final int MINIMUM_WORD_LENGTH = 3;
    private static final int MAXIMUM_WORD_LENGTH = 7;
    private static final int CHARSET_SIZE = 16;
    private static final int SQRT_CHARSET_SIZE = (int) Math.sqrt(CHARSET_SIZE);
    private static final int CORPUS_SIZE = 1000000;

    private final boolean bmpOnly;
    private final List<String> words;

    public static RandomCorpus bmpOnly(Random rng) {
        return new RandomCorpus(rng, true);
    }

    public static RandomCorpus nonBmpOnly(Random rng) {
        return new RandomCorpus(rng, false);
    }

    private RandomCorpus(Random rng, boolean bmpOnly) {
        this.bmpOnly = bmpOnly;
        if (bmpOnly) {
            words = generateBmpWords(rng);
        } else {
            words = generateArbitraryWords(rng);
        }
    }

    private static List<String> generateBmpWords(Random rng) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < CORPUS_SIZE; i++) {
            final StringBuilder sb = new StringBuilder();
            final int wordLength = MINIMUM_WORD_LENGTH +
                    rng.nextInt(MAXIMUM_WORD_LENGTH - MINIMUM_WORD_LENGTH);
            for (int j = 0; j < wordLength; j++) {
                final char c = (char) (rng.nextInt(CHARSET_SIZE) + 'a');
                sb.append(c);
            }
            result.add(sb.toString());
        }
        return result;
    }

    private List<String> generateArbitraryWords(Random rng) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < CORPUS_SIZE; i++) {
            final StringBuilder sb = new StringBuilder();
            final int wordLength = MINIMUM_WORD_LENGTH +
                    rng.nextInt(MAXIMUM_WORD_LENGTH - MINIMUM_WORD_LENGTH);
            for (int j = 0; j < wordLength; j++) {
                sb.append((char) (rng.nextInt(SQRT_CHARSET_SIZE) +
                        Character.MIN_HIGH_SURROGATE));
                sb.append((char) (rng.nextInt(SQRT_CHARSET_SIZE) +
                        Character.MIN_LOW_SURROGATE));
            }
            result.add(sb.toString());
        }
        return result;
    }

    @Override
    public boolean isBmpOnly() {
        return bmpOnly;
    }

    @Override
    public List<String> words() {
        return Collections.unmodifiableList(words);
    }

    @Override
    public String toString() {
        return "RandomCorpus[]";
    }
}
