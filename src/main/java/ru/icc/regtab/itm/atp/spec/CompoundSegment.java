package ru.icc.regtab.itm.atp.spec;

import java.util.Objects;

/**
 * A segment within a compound content specification: a leading delimiter
 * followed by a content specification (atomic or delimited).
 * <p>
 * The first segment's leading delimiter corresponds to δ₀ in Def. 25;
 * subsequent segments' leading delimiters correspond to δ₁, δ₂, etc.
 *
 * @param leadingDelimiter delimiter preceding this segment (empty string if absent)
 * @param spec            atomic or delimited content specification for this segment
 */
public record CompoundSegment(
        String leadingDelimiter,
        ContentSpec spec
) {
    public CompoundSegment {
        Objects.requireNonNull(leadingDelimiter, "leadingDelimiter");
        Objects.requireNonNull(spec, "spec");
        if (!(spec instanceof AtomicContentSpec) && !(spec instanceof DelimitedContentSpec)) {
            throw new IllegalArgumentException(
                    "Compound segment spec must be AtomicContentSpec or DelimitedContentSpec, got: "
                            + spec.getClass().getSimpleName());
        }
    }
}
