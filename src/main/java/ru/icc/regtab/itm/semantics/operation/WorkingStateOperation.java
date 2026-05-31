package ru.icc.regtab.itm.semantics.operation;

/**
 * Working-state update operation (def:ws-update-operation).
 * Sealed interface with six permitted implementations.
 */
public sealed interface WorkingStateOperation
        permits FillOperation, PrefixOperation, SuffixOperation,
                AvpOperation, RecOperation, JoinOperation {
}
