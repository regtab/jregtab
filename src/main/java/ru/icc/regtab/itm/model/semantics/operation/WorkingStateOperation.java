package ru.icc.regtab.itm.model.semantics.operation;

/**
 * Working-state update operation (Def. 13).
 * Sealed interface with six permitted implementations.
 */
public sealed interface WorkingStateOperation
        permits FillOperation, PrefixOperation, SuffixOperation,
                AvpOperation, RecOperation, ConcatOperation {
}
