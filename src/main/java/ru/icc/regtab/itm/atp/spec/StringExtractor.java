package ru.icc.regtab.itm.atp.spec;

/**
 * Item string extractor ξ (Def. 20): an implementation-defined function
 * applied to the input text to obtain the string of the derived item.
 * <p>
 * If absent (null), the item string equals the input text as-is.
 */
@FunctionalInterface
public interface StringExtractor {

    /**
     * Transforms the input text into the item string.
     *
     * @param input the raw cell text or a substring thereof
     * @return the extracted/transformed string
     */
    String apply(String input);

    /** Identity extractor: returns input as-is. */
    static StringExtractor identity() {
        return input -> input;
    }

    /** Substring extractor. */
    static StringExtractor substring(int beginIndex, int endIndex) {
        return input -> input.substring(beginIndex, Math.min(endIndex, input.length()));
    }

    /** Regex replace extractor. */
    static StringExtractor replace(String regex, String replacement) {
        return input -> input.replaceAll(regex, replacement);
    }

    /** Trim extractor. */
    static StringExtractor trim() {
        return String::trim;
    }

    /** Whitespace-normalising extractor: trims and collapses internal whitespace to a single space. */
    static StringExtractor norm() {
        return s -> s.strip().replaceAll("\\s+", " ");
    }
}
