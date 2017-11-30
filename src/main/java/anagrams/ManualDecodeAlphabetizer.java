package anagrams;

import java.util.Collections;
import java.util.List;

/**
 * An alphabetizer that sorts the Unicode code points of a string and returns a list of
 * the code points as boxed integers, where decoding is performed by the handwritten function
 * {@link UTF16#decode(String)}.
 */
public class ManualDecodeAlphabetizer implements Alphabetizer<List<Integer>> {
    @Override
    public List<Integer> alphabetize(String word) {
        List<Integer> codePoints = UTF16.decode(word);
        Collections.sort(codePoints);
        return codePoints;
    }
}
