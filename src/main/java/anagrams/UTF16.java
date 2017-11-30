package anagrams;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for working with UTF-16.
 */
public class UTF16 {

    /**
     * Convert a UTF-16&ndash;encoded string to the list of code points represented by that
     * string, as integer.
     *
     * @param s
     *         a UTF-16&ndash;encoded string
     * @return a list whose length is equal to the number of code points in {@code s}, and whose
     * {@code i}th element is the {@code i}th code point in {@code s}
     * @throws IllegalArgumentException
     *         if {@code s} is not a valid string of UTF-16 code units, which can occur if there
     *         is an invalid surrogate pair or the final code unit is a high-surrogate character
     */
    public static List<Integer> decode(String s) {
        final List<Integer> result = new ArrayList<>();
        final char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            final char here = chars[i];
            if (here < 0xD800 || here > 0xDFFF) {
                result.add((int) here);
            } else {
                if (i + 1 >= chars.length) {
                    throw new IllegalArgumentException(
                            "Incomplete surrogate at index " + i);
                }
                final char next = chars[++i];
                if (0xD800 <= here && here <= 0xDBFF
                        && 0xDC00 <= next && next <= 0xDFFF) {
                    result.add(
                            (((int) here - 0xD800) << 10)
                                    + ((int) next - 0xDC00)
                                    + 0x010000);
                } else {
                    throw new IllegalArgumentException(
                            "Invalid surrogate pair at index " + (i - 1));
                }
            }
        }
        return result;
    }

    /**
     * Encode a list of Unicode code points as a UTF-16 string.
     *
     * @param codePoints
     *         a list of Unicode code points
     * @return a string with the code points encoded as UTF-16
     */
    public static String encode(List<Integer> codePoints) {
        StringBuilder sb = new StringBuilder();
        for (int codePoint : codePoints) {
            if (Character.isBmpCodePoint(codePoint)) {
                sb.append((char) codePoint);
            } else {
                sb.append(Character.highSurrogate(codePoint));
                sb.append(Character.lowSurrogate(codePoint));
            }
        }
        return sb.toString();
    }

}
