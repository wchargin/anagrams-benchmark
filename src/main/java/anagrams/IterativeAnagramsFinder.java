package anagrams;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * An anagrams finder that uses idiomatic Java 7 iterative code to group strings into equivalence
 * classes by their canonical forms.
 */
public class IterativeAnagramsFinder implements AnagramsFinder {
    @Override
    public Set<Set<String>> findAnagrams(Alphabetizer<?> alphabetizer, List<String> words, int
            minGroupSize) {
        return findAnagramsExplicit(alphabetizer, words, minGroupSize);
    }

    private <T> Set<Set<String>> findAnagramsExplicit(
            Alphabetizer<T> alphabetizer, List<String> words, int minGroupSize) {
        Map<T, Set<String>> groups = new HashMap<>();
        for (String word : words) {
            groups.computeIfAbsent(alphabetizer.alphabetize(word),
                    (unused) -> new TreeSet<>()).add(word);
        }
        final Set<Set<String>> results = new HashSet<>();
        for (Set<String> group : groups.values()) {
            if (group.size() >= minGroupSize) {
                results.add(group);
            }
        }
        return results;
    }
}
