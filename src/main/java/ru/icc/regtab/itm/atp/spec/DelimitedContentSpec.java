package ru.icc.regtab.itm.atp.spec;

import java.util.Objects;

/**
 * Delimited content specification (Def. 24):
 * CS_delim = (δ, CS_atom).
 * <p>
 * The raw cell text is split on the non-empty delimiter δ, and the atomic
 * content specification is applied independently to each resulting substring,
 * deriving one item per substring.
 *
 * @param delimiter   non-empty delimiter string δ
 * @param atomicSpec  atomic content specification applied to each substring
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
