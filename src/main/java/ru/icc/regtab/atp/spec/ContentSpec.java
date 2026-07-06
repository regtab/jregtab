package ru.icc.regtab.atp.spec;

/**
 * Content specification S_cont: describes the items to be derived from a matched cell
 * and the interpretive actions to be instantiated upon them.
 * <p>
 * Four kinds (def:atomic-content-spec, def:delimited-content-spec,
 * def:compound-content-spec, def:conditional-content-spec):
 * atomic (S_atom), delimited (S_delim), compound (S_comp), conditional (S_cond).
 */
public sealed interface ContentSpec
        permits AtomicContentSpec, DelimitedContentSpec, CompoundContentSpec, ConditionalContentSpec {

    /**
     * Chains this spec with the next segment into a compound spec
     * (RTL {@code S_x "delim" S_x}). Appends when this is already compound;
     * not applicable to conditional specs.
     */
    default CompoundContentSpec then(String delimiter, ContentSpec next) {
        if (this instanceof ConditionalContentSpec)
            throw new UnsupportedOperationException("Conditional spec cannot be a compound segment");
        if (this instanceof CompoundContentSpec c) {
            var segs = new java.util.ArrayList<>(c.segments());
            segs.add(new CompoundSegment(delimiter, next));
            return new CompoundContentSpec(segs, c.trailingDelimiter());
        }
        return new CompoundContentSpec(java.util.List.of(
                new CompoundSegment("", this), new CompoundSegment(delimiter, next)));
    }
}
