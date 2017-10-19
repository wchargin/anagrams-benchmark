import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class BuiltinCodecAlphabetizer implements Alphabetizer<String> {
    @Override
    public String alphabetize(String word) {
        final int[] newCodePoints = word.codePoints().sorted().toArray();
        return new String(newCodePoints, 0, newCodePoints.length);
    }
}
