package ru.icc.regtab.itm.semantics.operation;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The key K of {@link ConcatOperation} / {@link JoinOperation}: a set of 0-based
 * <em>positions</em> in the item-based record (0 is the anchor, the following positions are the
 * items in the order the {@code REC} providers supplied them) and/or a set of attribute
 * <em>names</em>. A name is resolved to a position per record at apply time — the position of
 * the item whose attribute-value pair carries that name — so the key does not depend on the
 * order of the fields.
 * <p>
 * RTL: {@code CONCAT(0, 1, 'A', 'B')}, {@code JOIN('Year')}. The manuscript defines K over
 * positions only; names are an extension of the implementation with the same semantics.
 *
 * @param positions key positions, K ∩ ℕ₀ (empty = none)
 * @param names     key attribute names, K ∩ A (empty = none); each name is non-blank
 */
public record RecordKey(Set<Integer> positions, Set<String> names) {

    /** K = ∅. */
    public static final RecordKey EMPTY = new RecordKey(Set.of(), Set.of());

    public RecordKey {
        positions = Set.copyOf(positions != null ? positions : Set.of());
        names = Set.copyOf(names != null ? names : Set.of());
        for (int k : positions) {
            if (k < 0) throw new IllegalArgumentException("key position must be non-negative: " + k);
        }
        for (String a : names) {
            if (a.isBlank()) throw new IllegalArgumentException("key attribute name must not be blank");
        }
    }

    public static RecordKey positions(int... positions) {
        return new RecordKey(Arrays.stream(positions).boxed().collect(Collectors.toSet()), Set.of());
    }

    public static RecordKey positions(Set<Integer> positions) {
        return new RecordKey(positions, Set.of());
    }

    public static RecordKey names(String... names) {
        return new RecordKey(Set.of(), Set.of(names));
    }

    public static RecordKey of(Set<Integer> positions, Set<String> names) {
        return new RecordKey(positions, names);
    }

    public boolean isEmpty() {
        return positions.isEmpty() && names.isEmpty();
    }
}
