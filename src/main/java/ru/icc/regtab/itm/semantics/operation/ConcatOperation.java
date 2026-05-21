package ru.icc.regtab.itm.semantics.operation;

/**
 * O_concat: concatenates the record item sequences of the provided items
 * (excluding their anchors) into the anchor's record item sequence,
 * then removes the concatenated sequences from dom(rec).
 */
public record ConcatOperation() implements WorkingStateOperation {
}
