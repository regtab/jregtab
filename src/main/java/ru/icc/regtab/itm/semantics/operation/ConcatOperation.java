package ru.icc.regtab.itm.semantics.operation;

import java.util.Objects;

/**
 * O_concat^K: concatenates the item-based records of the provided items to the anchor's record
 * (one wide record), dropping the key items K from each concatenated record,
 * and removes the concatenated anchors from dom(rec). The number of records strictly decreases.
 * <p>
 * Applicable only if the key agrees across all records and no named attribute
 * (apart from the key) occurs in more than one of the concatenated records; otherwise the
 * operation has no effect and a diagnostic is recorded.
 * <p>
 * RTL: {@code CONCAT}, {@code CONCAT(k1, k2, …)}, {@code CONCAT(0, 'A')}. Up to jRegTab 0.5.x
 * this operation was called {@code JOIN(K)}; {@code JOIN} now denotes the record product
 * ({@link JoinOperation}).
 *
 * @param key K — key positions and/or key attribute names ({@link RecordKey}); the key items are
 *            dropped from the concatenated records. {@link RecordKey#EMPTY} means all items included.
 */
public record ConcatOperation(RecordKey key) implements WorkingStateOperation {
    public ConcatOperation {
        Objects.requireNonNull(key, "key");
    }
}
