package anagrams;

import java.util.Collections;
import java.util.List;

/**
 * An alphabetizer that sorts the Unicode code points of a string and returns a string with the
 * resulting characters, where decoding and encoding are performed by the handwritten functions
 * {@link UTF16#decode(String)} and {@link UTF16#encode(List)}.
 */
public class ManualCodecAlphabetizer implements Alphabetizer<String> {
    @Override
    public String alphabetize(String word) {
        List<Integer> codePoints = UTF16.decode(word);
        Collections.sort(codePoints);
        return UTF16.encode(codePoints);
    }
}
