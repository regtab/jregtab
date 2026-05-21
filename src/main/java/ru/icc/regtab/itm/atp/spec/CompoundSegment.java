package ru.icc.regtab.itm.atp.spec;

import java.util.Objects;

/**
 * One (δᵢ, S_xⁱ) pair within a compound content specification S_comp
 * (def:compound-content-spec): a leading delimiter δᵢ followed by a component
 * specification S_xⁱ (atomic or delimited).
 *
 * @param leadingDelimiter leading delimiter δᵢ (empty string if absent, i.e. δ₀)
 * @param spec             component specification S_xⁱ (atomic or delimited)
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
