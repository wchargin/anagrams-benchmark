package anagrams;

import java.util.Arrays;

public class BMPAlphabetizer implements Alphabetizer<String> {
    @Override
    public String alphabetize(String word) {
        char[] a = word.toCharArray();
        Arrays.sort(a);
        return new String(a);
    }
}
