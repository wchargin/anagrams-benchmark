import java.util.List;
import java.util.Set;

/**
 * An object that can identify groups of anagrams in a corpus via a canonical-forms algorithm.
 */
public interface AnagramsFinder {
    /**
     * Find all anagram groups of size at least {@code minGroupSize} within the given list of
     * {@code words}. This method need not do anything with the result.
     *
     * @param alphabetizer
     *         a strategy to be used for alphabetizing words
     * @param words
     *         a corpus of words from which to extract anagrams
     * @param minGroupSize
     *         the minimum size of anagram groups to consider
     */
    public Set<Set<String>> findAnagrams(
            Alphabetizer<?> alphabetizer,
            List<String> words,
            int minGroupSize);
}
