package ru.icc.regtab.itm.model.semantics.operation;

import java.util.Objects;

/**
 * O_suffix^delta: appends the delimiter-joined strings of the provided items
 * to the current value/attribute of the anchor.
 */
public record SuffixOperation(String delimiter) implements WorkingStateOperation {

    public SuffixOperation {
        Objects.requireNonNull(delimiter, "delimiter");
    }
}
