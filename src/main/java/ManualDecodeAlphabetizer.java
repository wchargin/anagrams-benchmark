import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ManualDecodeAlphabetizer implements Alphabetizer<List<Integer>> {
    @Override
    public List<Integer> alphabetize(String word) {
        List<Integer> codePoints = UTF16.decode(word);
        Collections.sort(codePoints);
        return codePoints;
    }
}
