package anagrams;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An alphabetizer that sorts the Unicode code points of a string and returns a list of
 * the code points as boxed integers, where decoding is performed by the built-in function
 * {@link String#codePoints()}.
 */
public class BuiltinDecodeAlphabetizer implements Alphabetizer<List<Integer>> {
    @Override
    public List<Integer> alphabetize(String word) {
        return word.codePoints().sorted().boxed().collect(Collectors.toList());
    }
}
