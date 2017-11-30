package anagrams;

/**
 * An alphabetizer that sorts the Unicode code points of a string and returns a string with the
 * resulting characters, where decoding and encoding are performed by the built-in functions
 * {@link String#codePoints()} and {@link String#String(int[], int, int)}.
 */
public class BuiltinCodecAlphabetizer implements Alphabetizer<String> {
    @Override
    public String alphabetize(String word) {
        final int[] newCodePoints = word.codePoints().sorted().toArray();
        return new String(newCodePoints, 0, newCodePoints.length);
    }
}
