package ru.icc.regtab.itm.model.semantics.provider;

import ru.icc.regtab.itm.model.semantics.item.CellDerivedItem;

/**
 * Item predicate κ: (anchor, candidate) → {false, true}.
 * Used by {@link ItemFilter} to determine which candidate items
 * are relevant to an anchor item.
 */
@FunctionalInterface
public interface ItemFilterCondition {

    boolean test(CellDerivedItem anchor, CellDerivedItem candidate);
}
