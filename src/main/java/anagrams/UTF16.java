package anagrams;

import java.util.ArrayList;
import java.util.List;

public class UTF16 {

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
