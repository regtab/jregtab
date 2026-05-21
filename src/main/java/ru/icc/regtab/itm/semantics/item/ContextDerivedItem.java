package ru.icc.regtab.itm.semantics.item;

import java.util.Objects;

/**
 * A context-derived item: a string constant supplied from the external context (def:context-derived-item).
 */
public final class ContextDerivedItem implements Item {

    private final String str;
    private final ItemType type;

    public ContextDerivedItem(String str, ItemType type) {
        this.str = Objects.requireNonNull(str, "str");
        this.type = Objects.requireNonNull(type, "type");
    }

    @Override
    public String str() { return str; }

    @Override
    public ItemType type() { return type; }

    @Override
    public String toString() {
        return "ContextDerivedItem[str=\"" + str + "\", type=" + type + "]";
    }
}
