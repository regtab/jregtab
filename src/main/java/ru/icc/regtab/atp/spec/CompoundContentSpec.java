package ru.icc.regtab.atp.spec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Compound content specification S_comp (def:compound-content-spec):
 * S_comp = (δ₀, S_x¹, δ₁, S_x², δ₂, …, S_xⁿ, δₙ).
 * <p>
 * The raw cell text is parsed according to the delimiter structure, producing
 * one substring per component S_xⁱ; each component is then resolved as either
 * an atomic or a delimited specification, yielding one or more items per component.
 *
 * @param segments          ordered sequence of (δᵢ, S_xⁱ) pairs (≥ 1)
 * @param trailingDelimiter trailing delimiter δₙ (empty string if absent)
 */
public record CompoundContentSpec(
        List<CompoundSegment> segments,
        String trailingDelimiter
) implements ContentSpec {

    public CompoundContentSpec {
        segments = List.copyOf(Objects.requireNonNull(segments, "segments"));
        if (segments.isEmpty()) {
            throw new IllegalArgumentException("At least one segment is required");
        }
        Objects.requireNonNull(trailingDelimiter, "trailingDelimiter");
    }

    /** Convenience: compound with no trailing delimiter. */
    public CompoundContentSpec(List<CompoundSegment> segments) {
        this(segments, "");
    }

    /** A delimiter–spec pair used with the {@link #of} factory. */
    public record Segment(String delimiter, ContentSpec spec) {
        public Segment {
            Objects.requireNonNull(delimiter, "delimiter");
            Objects.requireNonNull(spec, "spec");
        }

        public static Segment of(String delimiter, ContentSpec spec) {
            return new Segment(delimiter, spec);
        }
    }

    /** Convenience: build from a leading spec followed by delimited segments. */
    public static CompoundContentSpec of(ContentSpec first, Segment... rest) {
        var segs = new ArrayList<CompoundSegment>(1 + rest.length);
        segs.add(new CompoundSegment("", first));
        for (var s : rest) {
            segs.add(new CompoundSegment(s.delimiter(), s.spec()));
        }
        return new CompoundContentSpec(List.copyOf(segs));
    }
}
