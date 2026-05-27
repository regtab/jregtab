package ru.icc.regtab.itm.semantics.operation;

/**
 * O_rec: creates an item-based record with the anchor as the first element,
 * followed by the items returned by the providers.
 */
public record RecOperation() implements WorkingStateOperation {
}
