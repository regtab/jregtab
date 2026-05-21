package ru.icc.regtab.itm.model.semantics.operation;

import java.util.Objects;

/**
 * O_prefix^δ: prepends the delimiter-joined strings of the provided items
 * to the current value/attribute of the anchor.
 */
public record PrefixOperation(String delimiter) implements WorkingStateOperation {

    public PrefixOperation {
        Objects.requireNonNull(delimiter, "delimiter");
    }
}
