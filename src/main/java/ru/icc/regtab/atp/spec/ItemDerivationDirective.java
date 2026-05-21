package ru.icc.regtab.atp.spec;

import ru.icc.regtab.itm.semantics.item.ItemType;

/**
 * Item derivation directive idd (def:atomic-content-spec): specifies how the
 * input text of a matched cell is processed to derive an item.
 */
public enum ItemDerivationDirective {
    /** Derive a value-associated item. */
    VAL,
    /** Derive an attribute-associated item. */
    ATTR,
    /** Derive an auxiliary item. */
    AUX,
    /** Ignore the cell content (no item derived, no actions instantiated). */
    SKIP;

    /**
     * Converts this directive to the corresponding {@link ItemType}.
     *
     * @throws IllegalStateException if this is {@link #SKIP}
     */
    public ItemType toItemType() {
        return switch (this) {
            case VAL -> ItemType.VALUE;
            case ATTR -> ItemType.ATTRIBUTE;
            case AUX -> ItemType.AUXILIARY;
            case SKIP -> throw new IllegalStateException("SKIP has no corresponding ItemType");
        };
    }
}
