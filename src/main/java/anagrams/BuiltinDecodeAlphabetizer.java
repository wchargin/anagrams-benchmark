package anagrams;

import java.util.List;
import java.util.stream.Collectors;

public class BuiltinDecodeAlphabetizer implements Alphabetizer<List<Integer>> {
    @Override
    public List<Integer> alphabetize(String word) {
        return word.codePoints().sorted().boxed().collect(Collectors.toList());
    }
}
