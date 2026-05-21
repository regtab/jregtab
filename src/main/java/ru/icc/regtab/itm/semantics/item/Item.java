package ru.icc.regtab.itm.semantics.item;

/**
 * An atomic piece of information represented in a table or its context.
 * Sealed interface with two permitted implementations:
 * {@link CellDerivedItem} and {@link ContextDerivedItem}.
 */
public sealed interface Item permits CellDerivedItem, ContextDerivedItem {

    /**
     * The string content of this item.
     */
    String str();

    /**
     * The type of this item (VALUE, ATTRIBUTE, or AUXILIARY).
     */
    ItemType type();
}
