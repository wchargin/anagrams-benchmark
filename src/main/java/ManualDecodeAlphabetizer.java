import java.util.Collections;
import java.util.List;

public class ManualDecodeAlphabetizer implements Alphabetizer<List<Integer>> {
    @Override
    public List<Integer> alphabetize(String word) {
        List<Integer> codePoints = UTF16.decode(word);
        Collections.sort(codePoints);
        return codePoints;
    }
}
