package ru.icc.regtab.itm.atp.spec;

/**
 * Content specification: describes the items to be derived from a matched cell
 * and the interpretive actions to be instantiated upon them.
 * <p>
 * Four kinds (Defs. 20, 24–27): atomic, delimited, compound, conditional.
 */
public sealed interface ContentSpec
        permits AtomicContentSpec, DelimitedContentSpec, CompoundContentSpec, ConditionalContentSpec {
}
