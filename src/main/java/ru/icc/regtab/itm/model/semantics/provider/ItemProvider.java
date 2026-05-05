package ru.icc.regtab.itm.model.semantics.provider;

import ru.icc.regtab.itm.model.semantics.item.Item;

import java.util.List;

/**
 * Sealed interface unifying cell-derived and context-derived item providers.
 */
public sealed interface ItemProvider permits CellDerivedItemProvider, ContextDerivedItemProvider {

    /**
     * Returns items relevant to the given anchor item.
     */
    List<? extends Item> provide(Item anchor);
}
