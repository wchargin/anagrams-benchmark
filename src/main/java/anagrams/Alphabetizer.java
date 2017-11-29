package anagrams;

@FunctionalInterface
public interface Alphabetizer<T> {
    /**
     * Return a canonical form for the given word. Two canonical forms are guaranteed to be equal
     * ({@code equals} to each other) if and only if the two words are anagrams.
     * @param word the word to alphabetize
     * @return a canonical form for the word
     */
    public T alphabetize(String word);
}
