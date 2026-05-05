package ru.icc.regtab.itm.pattern;

import java.util.Objects;

/**
 * Splits a single cell into several {@link ru.icc.regtab.itm.model.semantics.item.ItemType#VALUE} items
 * by a delimiter (e.g. comma-separated list).
 */
public record DelimitedSplitSpec(String delimiter) {

    public DelimitedSplitSpec {
        Objects.requireNonNull(delimiter, "delimiter");
        if (delimiter.isEmpty()) {
            throw new IllegalArgumentException("delimiter must be non-empty");
        }
    }
}
