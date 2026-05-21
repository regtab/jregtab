package ru.icc.regtab.pattern;

import ru.icc.regtab.itm.semantics.item.ItemType;
import ru.icc.regtab.itm.semantics.provider.CellDerivedItemProvider;
import ru.icc.regtab.itm.semantics.provider.CellDerivedProviderKind;
import ru.icc.regtab.itm.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;

import java.util.Objects;

/**
 * Item provider specification for pattern actions: either a <em>cell-derived</em> provider
 * (predicate κ, traversal τ, cardinality k, Υ<sub>tbl</sub><sup>·</sup>) or a <em>context-derived</em>
 * string constant (Υ<sub>ctx</sub>) for {@code fill} / {@code prefix} / {@code suffix} / {@code rec}.
 * <p>
 * Use {@link #val}, {@link #attr}, {@link #aux} for cell-derived instances; {@link #of} keeps legacy unrestricted J;
 * use {@link #ctx} or {@link #aux(String)} for context literals.
 */
public record ProviderSpec(
        ItemFilterCondition predicate,
        TraversalOrder traversal,
        int cardinality,
        CellDerivedProviderKind cellKind,
        ContextLiteralSpec contextLiteral
) {
    /**
     * Fixed context string and {@link ItemType} for {@link #ctx(String, ItemType)}.
     */
    public record ContextLiteralSpec(String text, ItemType type) {
        public ContextLiteralSpec {
            Objects.requireNonNull(text, "text");
            Objects.requireNonNull(type, "type");
        }
    }

    private static final ItemFilterCondition CTX_DUMMY_PREDICATE = (a, c) -> false;

    public ProviderSpec {
        if (contextLiteral == null) {
            Objects.requireNonNull(predicate, "predicate");
            Objects.requireNonNull(traversal, "traversal");
            Objects.requireNonNull(cellKind, "cellKind");
            if (cardinality < 0) {
                throw new IllegalArgumentException("cardinality must be non-negative: " + cardinality);
            }
            if (cellKind == CellDerivedProviderKind.ATTR && cardinality != 1) {
                throw new IllegalArgumentException("Υ_tbl^attr requires k = 1, got: " + cardinality);
            }
        } else {
            Objects.requireNonNull(contextLiteral, "contextLiteral");
        }
    }

    /**
     * Context-derived constant: fixed string participates in join ({@code fill}/{@code prefix}/{@code suffix})
     * or {@code rec} sequences (Υ<sub>ctx</sub><sup>val|attr|aux</sup>).
     */
    public static ProviderSpec ctx(String text, ItemType type) {
        return new ProviderSpec(
                CTX_DUMMY_PREDICATE,
                TraversalOrder.ROW_MAJOR,
                0,
                CellDerivedProviderKind.UNRESTRICTED,
                new ContextLiteralSpec(text, type));
    }

    /**
     * Context-derived auxiliary string (common for Foofah-style delimiter-only joins with an empty aux slot).
     */
    public static ProviderSpec aux(String text) {
        return ctx(text, ItemType.AUXILIARY);
    }

    /**
     * Legacy: full J (no type restriction before κ); same defaults as single-predicate {@code rec}.
     */
    public static ProviderSpec any(ItemFilterCondition predicate) {
        return new ProviderSpec(predicate, TraversalOrder.ROW_MAJOR, CellDerivedItemProvider.UNBOUNDED,
                CellDerivedProviderKind.UNRESTRICTED, null);
    }

    public static ProviderSpec any(ItemFilterCondition predicate, int cardinality) {
        return new ProviderSpec(predicate, TraversalOrder.ROW_MAJOR, cardinality, CellDerivedProviderKind.UNRESTRICTED,
                null);
    }

    public static ProviderSpec any(ItemFilterCondition predicate, TraversalOrder traversal) {
        return new ProviderSpec(predicate, traversal, CellDerivedItemProvider.UNBOUNDED,
                CellDerivedProviderKind.UNRESTRICTED, null);
    }

    public static ProviderSpec any(ItemFilterCondition predicate, TraversalOrder traversal, int cardinality) {
        return new ProviderSpec(predicate, traversal, cardinality, CellDerivedProviderKind.UNRESTRICTED, null);
    }

    /** Υ<sub>tbl</sub><sup>val</sup>: J = I<sub>tbl</sub><sup>val</sup>. */
    public static ProviderSpec val(ItemFilterCondition predicate) {
        return new ProviderSpec(predicate, TraversalOrder.ROW_MAJOR, CellDerivedItemProvider.UNBOUNDED,
                CellDerivedProviderKind.VAL, null);
    }

    public static ProviderSpec val(ItemFilterCondition predicate, int cardinality) {
        return new ProviderSpec(predicate, TraversalOrder.ROW_MAJOR, cardinality, CellDerivedProviderKind.VAL, null);
    }

    public static ProviderSpec val(ItemFilterCondition predicate, TraversalOrder traversal, int cardinality) {
        return new ProviderSpec(predicate, traversal, cardinality, CellDerivedProviderKind.VAL, null);
    }

    /** Υ<sub>tbl</sub><sup>attr</sup>: J = I<sub>tbl</sub><sup>attr</sup>, k = 1. */
    public static ProviderSpec attr(ItemFilterCondition predicate) {
        return new ProviderSpec(predicate, TraversalOrder.ROW_MAJOR, 1, CellDerivedProviderKind.ATTR, null);
    }

    public static ProviderSpec attr(ItemFilterCondition predicate, TraversalOrder traversal) {
        return new ProviderSpec(predicate, traversal, 1, CellDerivedProviderKind.ATTR, null);
    }

    /** Υ<sub>tbl</sub><sup>aux</sup>: J = I<sub>tbl</sub>. */
    public static ProviderSpec aux(ItemFilterCondition predicate) {
        return new ProviderSpec(predicate, TraversalOrder.ROW_MAJOR, CellDerivedItemProvider.UNBOUNDED,
                CellDerivedProviderKind.AUX, null);
    }

    public static ProviderSpec aux(ItemFilterCondition predicate, TraversalOrder traversal, int cardinality) {
        return new ProviderSpec(predicate, traversal, cardinality, CellDerivedProviderKind.AUX, null);
    }
}
