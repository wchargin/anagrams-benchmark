package anagrams;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
