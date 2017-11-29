public class BuiltinCodecAlphabetizer implements Alphabetizer<String> {
    @Override
    public String alphabetize(String word) {
        final int[] newCodePoints = word.codePoints().sorted().toArray();
        return new String(newCodePoints, 0, newCodePoints.length);
    }
}
