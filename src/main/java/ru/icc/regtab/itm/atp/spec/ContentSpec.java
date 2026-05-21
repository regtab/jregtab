package ru.icc.regtab.itm.atp.spec;

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
}
