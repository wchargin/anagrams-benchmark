package anagrams;

import java.util.Arrays;

/**
 * An alphabetizer that assumes that all code points of its input string are in the Basic
 * Multilingual Plane, and simply the code points numerically to create the alphabetized string.
 * When passed a string not all of whose code points are in the BMP, incorrect behavior may
 * result: strings that are not anagrams of each other may map to the same alphabetized form.
 */
public class BMPAlphabetizer implements Alphabetizer<String> {
    @Override
    public String alphabetize(String word) {
        char[] a = word.toCharArray();
        Arrays.sort(a);
        return new String(a);
    }
}
