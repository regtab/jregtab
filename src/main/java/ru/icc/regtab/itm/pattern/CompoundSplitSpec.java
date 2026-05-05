package ru.icc.regtab.itm.pattern;

import java.util.List;

/**
 * Splits a single physical cell into several logical items using literal separator strings between
 * consecutive tokens. {@code separators.size()} must equal {@code tokens.size() - 1}: gap {@code i}
 * uses {@code separators.get(i)} between token {@code i} and token {@code i + 1}.
 * <p>
 * Splitting is sequential: find each separator in the remainder of the text (first occurrence),
 * so extra occurrences of the same substring later in the tail do not create extra segments.
 */
public record CompoundSplitSpec(List<String> separators, List<CompoundTokenSpec> tokens) {

    public CompoundSplitSpec {
        tokens = List.copyOf(tokens);
        separators = List.copyOf(separators);
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("tokens must not be empty");
        }
        if (separators.size() != tokens.size() - 1) {
            throw new IllegalArgumentException("separators count must be tokens.size() - 1");
        }
        for (String s : separators) {
            if (s == null || s.isEmpty()) {
                throw new IllegalArgumentException("each separator must be non-empty");
            }
        }
    }
}
