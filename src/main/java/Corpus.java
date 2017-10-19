import java.util.List;

public interface Corpus {
    /**
     * Determine whether this corpus contains only Unicode code points found within the Basic
     * Multilingual Plane.
     *
     * @return {@code true} if all code points in this corpus are in the BMP, or {@code false}
     * otherwise
     */
    public boolean isBmpOnly();

    /**
     * Get a list of words in this corpus.
     *
     * @return a list of words in this corpus
     */
    public List<String> words();
}
