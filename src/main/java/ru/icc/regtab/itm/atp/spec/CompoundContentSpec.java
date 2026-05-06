package ru.icc.regtab.itm.atp.spec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Compound content specification (Def. 25):
 * CS_comp = (δ₀, CS_x¹, δ₁, CS_x², δ₂, …, CS_xⁿ, δₙ).
 * <p>
 * Describes a cell whose raw text matches a pattern of alternating delimiters
 * and content specifications. Each segment contains a leading delimiter and
 * its content spec. The trailing delimiter δₙ is stored separately.
 *
 * @param segments         ordered sequence of compound segments (≥ 1)
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
