package ru.icc.regtab.itm.model.semantics.provider;

import ru.icc.regtab.itm.model.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.model.semantics.item.ContextDerivedItem;
import ru.icc.regtab.itm.model.semantics.item.Item;
import ru.icc.regtab.itm.model.semantics.item.ItemType;

import java.util.List;
import java.util.Objects;

/**
 * Cell-derived item provider (Def. 11):
 * named instances Υ<sub>ctx</sub><sup>val</sup>, Υ<sub>ctx</sub><sup>attr</sup>, Υ<sub>ctx</sub><sup>aux</sup>
 * with anchor constraints; {@link ContextDerivedProviderKind#UNRESTRICTED} matches legacy behaviour.
 */
public final class ContextDerivedItemProvider implements ItemProvider {

    private final List<ContextDerivedItem> items;
    private final ContextDerivedProviderKind kind;

    public ContextDerivedItemProvider(List<ContextDerivedItem> items) {
        this(items, inferKind(Objects.requireNonNull(items, "items")));
    }

    public ContextDerivedItemProvider(List<ContextDerivedItem> items, ContextDerivedProviderKind kind) {
        this.items = List.copyOf(Objects.requireNonNull(items, "items"));
        this.kind = Objects.requireNonNull(kind, "kind");
    }

    public List<ContextDerivedItem> items() { return items; }

    public ContextDerivedProviderKind kind() { return kind; }

    private static ContextDerivedProviderKind inferKind(List<ContextDerivedItem> items) {
        if (items.isEmpty()) {
            return ContextDerivedProviderKind.UNRESTRICTED;
        }
        if (items.size() == 1 && items.get(0).type() == ItemType.ATTRIBUTE) {
            return ContextDerivedProviderKind.ATTR;
        }
        if (items.stream().allMatch(i -> i.type() == ItemType.VALUE)) {
            return ContextDerivedProviderKind.VAL;
        }
        if (items.stream().allMatch(i -> i.type() == ItemType.AUXILIARY)) {
            return ContextDerivedProviderKind.AUX;
        }
        return ContextDerivedProviderKind.UNRESTRICTED;
    }

    @Override
    public List<ContextDerivedItem> provide(Item anchor) {
        if (kind == ContextDerivedProviderKind.UNRESTRICTED) {
            return items;
        }
        switch (kind) {
            case VAL -> {
                if (!items.stream().allMatch(i -> i.type() == ItemType.VALUE)) {
                    throw new IllegalStateException("Υ_ctx^val requires context value items only");
                }
                if (!(anchor instanceof CellDerivedItem a) || a.type() != ItemType.VALUE) {
                    throw new IllegalArgumentException("Υ_ctx^val requires a table value anchor");
                }
            }
            case ATTR -> {
                if (items.size() != 1 || items.get(0).type() != ItemType.ATTRIBUTE) {
                    throw new IllegalStateException("Υ_ctx^attr requires exactly one attribute context item");
                }
                if (!isAttrAnchorAllowed(anchor)) {
                    throw new IllegalArgumentException(
                            "Υ_ctx^attr requires anchor ∈ I_tbl^val ∪ I_ctx^val, got: " + anchor);
                }
            }
            case AUX -> {
                if (!items.stream().allMatch(i -> i.type() == ItemType.AUXILIARY)) {
                    throw new IllegalStateException("Υ_ctx^aux requires context auxiliary items only");
                }
                if (!(anchor instanceof CellDerivedItem a)
                        || (a.type() != ItemType.VALUE && a.type() != ItemType.ATTRIBUTE)) {
                    throw new IllegalArgumentException(
                            "Υ_ctx^aux requires a table value or table attribute anchor");
                }
            }
            default -> { }
        }
        return items;
    }

    private static boolean isAttrAnchorAllowed(Item anchor) {
        if (anchor instanceof CellDerivedItem c && c.type() == ItemType.VALUE) {
            return true;
        }
        return anchor instanceof ContextDerivedItem c && c.type() == ItemType.VALUE;
    }
}
