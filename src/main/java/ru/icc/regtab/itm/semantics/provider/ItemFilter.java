package ru.icc.regtab.itm.semantics.provider;

import ru.icc.regtab.itm.semantics.item.CellDerivedItem;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Item filter Φ_κ (def:item-filter): for a given anchor item and a subset of items,
 * returns the subset of items relevant to the anchor.
 * <p>
 * Φ_κ(ι_anch, J) = {ι ∈ J | κ(ι_anch, ι) = true}
 */
public final class ItemFilter {

    private final ItemFilterCondition predicate;

    public ItemFilter(ItemFilterCondition predicate) {
        this.predicate = Objects.requireNonNull(predicate, "predicate");
    }

    public ItemFilterCondition predicate() {
        return predicate;
    }

    /**
     * Applies the filter: returns the subset of {@code items} for which
     * the predicate κ(anchor, candidate) holds.
     */
    public Set<CellDerivedItem> apply(CellDerivedItem anchor, Set<CellDerivedItem> items) {
        return items.stream()
                .filter(candidate -> predicate.test(anchor, candidate))
                .collect(Collectors.toSet());
    }
}
