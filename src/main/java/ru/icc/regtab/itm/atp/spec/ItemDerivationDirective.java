package ru.icc.regtab.itm.atp.spec;

import ru.icc.regtab.itm.model.semantics.item.ItemType;

/**
 * Item derivation directive (Def. 20): specifies how matched cell content
 * is processed.
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
