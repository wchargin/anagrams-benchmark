import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ManualCodecAlphabetizer implements Alphabetizer<String> {
    @Override
    public String alphabetize(String word) {
        List<Integer> codePoints = UTF16.decode(word);
        Collections.sort(codePoints);
        return UTF16.encode(codePoints);
    }
}
