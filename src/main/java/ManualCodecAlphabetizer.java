import java.util.Collections;
import java.util.List;

public class ManualCodecAlphabetizer implements Alphabetizer<String> {
    @Override
    public String alphabetize(String word) {
        List<Integer> codePoints = UTF16.decode(word);
        Collections.sort(codePoints);
        return UTF16.encode(codePoints);
    }
}
