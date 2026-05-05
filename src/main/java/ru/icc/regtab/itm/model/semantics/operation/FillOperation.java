package ru.icc.regtab.itm.model.semantics.operation;

import java.util.Objects;

/**
 * O_fill^delta: replaces the value/attribute of the anchor with the
 * delimiter-joined strings of the provided items.
 */
public record FillOperation(String delimiter) implements WorkingStateOperation {

    public FillOperation {
        Objects.requireNonNull(delimiter, "delimiter");
    }
}
