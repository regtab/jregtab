package ru.icc.regtab.itm.atp.spec;

import java.util.Objects;

/**
 * Delimited content specification S_delim (def:delimited-content-spec):
 * S_delim = (δ, S_atom).
 * <p>
 * The raw cell text is split on the non-empty delimiter δ into substrings
 * s₁, …, sₙ, and S_atom is applied independently to each sₖ as its input text,
 * deriving one item per substring.
 *
 * @param delimiter  non-empty delimiter string δ
 * @param atomicSpec atomic content specification S_atom applied to each substring
 */
public record DelimitedContentSpec(
        String delimiter,
        AtomicContentSpec atomicSpec
) implements ContentSpec {

    public DelimitedContentSpec {
        Objects.requireNonNull(delimiter, "delimiter");
        if (delimiter.isEmpty()) {
            throw new IllegalArgumentException("delimiter must be non-empty");
        }
        Objects.requireNonNull(atomicSpec, "atomicSpec");
    }
}
