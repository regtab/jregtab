package ru.icc.regtab.itm.semantics.operation;

import java.util.Objects;
import java.util.Set;

/**
 * O_concat^K: concatenates the item-based records of the provided items to the anchor's record
 * (one wide record), dropping the items at the key positions K from each concatenated record,
 * and removes the concatenated anchors from dom(rec). The number of records strictly decreases.
 * <p>
 * Applicable only if the key positions agree across all records and no named attribute
 * (apart from the key) occurs in more than one of the concatenated records; otherwise the
 * operation has no effect and a diagnostic is recorded.
 * <p>
 * RTL: {@code CONCAT}, {@code CONCAT(k1, k2, …)}. Up to jRegTab 0.5.x this operation was
 * called {@code JOIN(K)}; {@code JOIN} now denotes the record product ({@link JoinOperation}).
 *
 * @param keyPositions K ⊆ ℕ₀; items at these positions are dropped from the concatenated records.
 *                     Empty set means no positions are dropped (all items included).
 */
public record ConcatOperation(Set<Integer> keyPositions) implements WorkingStateOperation {
    public ConcatOperation {
        keyPositions = Set.copyOf(Objects.requireNonNull(keyPositions, "keyPositions"));
    }
}
