package ru.icc.regtab.itm.semantics.operation;

import java.util.Objects;
import java.util.Set;

/**
 * O_join^K: joins item-based records into the anchor's record,
 * dropping items at key positions K from each joined record, then deduplicating
 * by named attribute, and removing the joined records from dom(rec).
 *
 * @param keyPositions K ⊆ ℕ₀; items at these positions are dropped from joined records before merge.
 *                     Empty set means no positions are dropped (all items included).
 */
public record JoinOperation(Set<Integer> keyPositions) implements WorkingStateOperation {
    public JoinOperation {
        keyPositions = Set.copyOf(Objects.requireNonNull(keyPositions, "keyPositions"));
    }
}
