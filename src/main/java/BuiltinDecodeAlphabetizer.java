import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class BuiltinDecodeAlphabetizer implements Alphabetizer<List<Integer>> {
    @Override
    public List<Integer> alphabetize(String word) {
        return word.codePoints().sorted().boxed().collect(Collectors.toList());
    }
}
