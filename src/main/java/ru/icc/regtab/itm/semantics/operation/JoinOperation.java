package ru.icc.regtab.itm.semantics.operation;

import java.util.Objects;

/**
 * O_join^K: the record product. Every record of the anchor is combined with every record of the
 * provided anchors — a cross product for K = ∅, an equi-join on the key K otherwise;
 * the key items of the joined record are dropped, and a named attribute shared by the two
 * records acts as a natural-join condition (the pair is kept only if the values agree, and the
 * attribute occurs once in the result). The provided anchors are marked as joined-away and are
 * excluded from recordset extraction; their records stay available, so that several anchors may
 * join the same records irrespective of the order in which the actions are applied.
 * If no record pair satisfies the conditions, the anchor keeps its records (left outer join).
 * <p>
 * The width of the records stays fixed while their number grows — the counterpart of
 * {@code CROSS JOIN} / {@code LATERAL}. The folding operation that was called {@code JOIN(K)}
 * up to jRegTab 0.5.x is now {@link ConcatOperation} ({@code CONCAT(K)}).
 *
 * @param key K — key positions and/or key attribute names ({@link RecordKey}) at which a record
 *            pair must agree, dropped from the joined record. {@link RecordKey#EMPTY} means a
 *            cross product.
 */
public record JoinOperation(RecordKey key) implements WorkingStateOperation {
    public JoinOperation {
        Objects.requireNonNull(key, "key");
    }
}
