package ru.icc.regtab.itm.model.semantics.operation;

/**
 * O_rec: creates a record item sequence with the anchor as the first element,
 * followed by the items returned by the providers.
 */
public record RecOperation() implements WorkingStateOperation {
}
