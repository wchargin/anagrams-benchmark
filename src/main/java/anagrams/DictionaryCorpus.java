package anagrams;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A corpus whose data is read from a file or other input stream, usually corresponding to a word
 * list in some natural language.
 */
public class DictionaryCorpus implements Corpus {
    private final String name;
    private final List<String> words;
    private final boolean bmpOnly;

    private DictionaryCorpus(String name, List<String> words, boolean bmpOnly) {
        this.name = name;
        this.words = words;
        this.bmpOnly = bmpOnly;
    }

    public static DictionaryCorpus fromFile(File file) throws IOException {
        return fromInputStream(file.getName(), new FileInputStream(file));
    }

    public static DictionaryCorpus fromInputStream(String name, InputStream stream)
            throws IOException {
        final List<String> words = new BufferedReader(new InputStreamReader(stream))
                .lines().collect(Collectors.toList());
        final boolean bmpOnly = words.stream().allMatch(word ->
                word.codePoints().allMatch(Character::isBmpCodePoint));
        return new DictionaryCorpus(name, words, bmpOnly);
    }

    @Override
    public boolean isBmpOnly() {
        return bmpOnly;
    }

    @Override
    public List<String> words() {
        return words;
    }

    @Override
    public String toString() {
        return String.format("DictionaryCorpus[%s]", name);
    }
}
