package ru.icc.regtab.atp.spec;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Item string extractor ξ (def:atomic-content-spec): a structured, serializable
 * transformation applied to input text to produce the derived item's string value.
 * <p>
 * If absent (null), the input text is used directly as the item string.
 */
public sealed interface StringExtractor permits
        StringExtractor.Verbatim,
        StringExtractor.Replaced,
        StringExtractor.WhitespaceNormalized,
        StringExtractor.Trimmed,
        StringExtractor.Substring,
        StringExtractor.UpperCase,
        StringExtractor.LowerCase,
        StringExtractor.Chain,
        StringExtractor.Custom {

    /** RTL representation of this extractor step or chain. */
    String toRtl();

    /** Applies the transformation to the input text. */
    String apply(String input);

    // ---- Sealed variants ----

    /** Returns input as-is. RTL: not emitted (absence of {@code = strExtr}). */
    record Verbatim() implements StringExtractor {
        public static final Verbatim INSTANCE = new Verbatim();
        public String toRtl() { return ""; }
        public String apply(String input) { return input; }
    }

    /** Replaces all occurrences of {@code regex} with {@code replacement}. RTL: {@code REPL("rx","rep")}. */
    record Replaced(String regex, String replacement) implements StringExtractor {
        public String toRtl() { return "REPL(\"" + regex.replace("\"", "\"\"") + "\",\"" + replacement.replace("\"", "\"\"") + "\")"; }
        public String apply(String input) { return input.replaceAll(regex, replacement); }
    }

    /** Trims leading/trailing whitespace and collapses internal runs to a single space. RTL: {@code NORM}. */
    record WhitespaceNormalized() implements StringExtractor {
        public static final WhitespaceNormalized INSTANCE = new WhitespaceNormalized();
        public String toRtl() { return "NORM"; }
        public String apply(String input) { return input.strip().replaceAll("\\s+", " "); }
    }

    /** Removes leading and trailing whitespace. RTL: {@code TRIM}. */
    record Trimmed() implements StringExtractor {
        public static final Trimmed INSTANCE = new Trimmed();
        public String toRtl() { return "TRIM"; }
        public String apply(String input) { return input.trim(); }
    }

    /** Extracts a substring [{@code begin}, {@code end}). RTL: {@code SUBSTR(b,e)}. */
    record Substring(int begin, int end) implements StringExtractor {
        public String toRtl() { return "SUBSTR(" + begin + "," + end + ")"; }
        public String apply(String input) { return input.substring(begin, Math.min(end, input.length())); }
    }

    /** Converts to upper-case. RTL: {@code UC}. */
    record UpperCase() implements StringExtractor {
        public static final UpperCase INSTANCE = new UpperCase();
        public String toRtl() { return "UC"; }
        public String apply(String input) { return input.toUpperCase(); }
    }

    /** Converts to lower-case. RTL: {@code LC}. */
    record LowerCase() implements StringExtractor {
        public static final LowerCase INSTANCE = new LowerCase();
        public String toRtl() { return "LC"; }
        public String apply(String input) { return input.toLowerCase(); }
    }

    /**
     * Applies a sequence of extractors left-to-right.
     * RTL: steps joined with {@code .} — e.g. {@code REPLACE("x","y").TRIM}.
     */
    record Chain(List<StringExtractor> steps) implements StringExtractor {
        public String toRtl() {
            return steps.stream().map(StringExtractor::toRtl).collect(Collectors.joining("."));
        }
        public String apply(String input) {
            String result = input;
            for (StringExtractor step : steps) result = step.apply(result);
            return result;
        }
    }

    /** Escape hatch — {@link #toRtl()} throws {@link UnsupportedOperationException}. */
    record Custom(String description, Function<String, String> fn) implements StringExtractor {
        public String toRtl() {
            throw new UnsupportedOperationException("Custom StringExtractor has no RTL analog: " + description);
        }
        public String apply(String input) { return fn.apply(input); }
    }
}
