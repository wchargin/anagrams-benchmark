package anagrams;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * An anagrams finder that uses idiomatic Java 8 stream code to group strings into equivalence
 * classes by their canonical forms.
 */
public class StreamsAnagramFinder implements AnagramsFinder {
    @Override
    public Set<Set<String>> findAnagrams(
            Alphabetizer<?> alphabetizer,
            List<String> words,
            int minGroupSize) {
        return words.stream()
                .collect(Collectors.groupingBy(
                        alphabetizer::alphabetize, Collectors.toCollection(TreeSet::new)))
                .values().stream()
                .filter(group -> group.size() >= minGroupSize)
                .collect(Collectors.toSet());
    }
}
