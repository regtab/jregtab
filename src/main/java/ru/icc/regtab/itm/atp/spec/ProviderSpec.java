package ru.icc.regtab.itm.atp.spec;

import ru.icc.regtab.itm.model.semantics.item.ItemType;
import ru.icc.regtab.itm.model.semantics.provider.CellDerivedProviderKind;
import ru.icc.regtab.itm.model.semantics.provider.ContextDerivedProviderKind;
import ru.icc.regtab.itm.model.semantics.provider.TraversalOrder;

import java.util.Objects;

/**
 * Item provider specification PS_i = (k, τ, κ) (def:item-provider-spec).
 * <p>
 * Together these parameters define either a typed cell-derived item provider
 * instantiated at match time or a context-derived literal provider.
 *
 * @param cardinality maximum number of items to retrieve (Integer.MAX_VALUE for ∞)
 * @param traversalOrder traversal order τ
 * @param filterCondition item filter condition κ (null for context literals)
 * @param targetItemKind target cell-derived item set kind
 * @param contextLiteral optional fixed context item specification
 */
public record ProviderSpec(
        int cardinality,
        TraversalOrder traversalOrder,
        ItemFilterConditionSpec filterCondition,
        CellDerivedProviderKind targetItemKind,
        ContextLiteralSpec contextLiteral
) {
    /** Fixed context string and item type. */
    public record ContextLiteralSpec(String text, ItemType type) {
        public ContextLiteralSpec {
            Objects.requireNonNull(text, "text");
            Objects.requireNonNull(type, "type");
        }

        public ContextDerivedProviderKind kind() {
            return switch (type) {
                case VALUE -> ContextDerivedProviderKind.VAL;
                case ATTRIBUTE -> ContextDerivedProviderKind.ATTR;
                case AUXILIARY -> ContextDerivedProviderKind.AUX;
            };
        }
    }

    /** Unbounded cardinality constant. */
    public static final int UNBOUNDED = Integer.MAX_VALUE;

    public ProviderSpec {
        if (cardinality < 0) {
            throw new IllegalArgumentException("cardinality must be non-negative: " + cardinality);
        }
        Objects.requireNonNull(traversalOrder, "traversalOrder");
        if (contextLiteral == null) {
            Objects.requireNonNull(filterCondition, "filterCondition");
            Objects.requireNonNull(targetItemKind, "targetItemKind");
            if (targetItemKind == CellDerivedProviderKind.ATTR && cardinality != 1) {
                throw new IllegalArgumentException("ATTR provider requires cardinality = 1, got: " + cardinality);
            }
        }
    }

    /** Convenience: cardinality-1 unrestricted provider, default traversal (ROW_MAJOR). */
    public static ProviderSpec any(ItemFilterConditionSpec condition) {
        return new ProviderSpec(1, TraversalOrder.ROW_MAJOR, condition, CellDerivedProviderKind.UNRESTRICTED, null);
    }

    /** Convenience: specified cardinality, unrestricted provider, default traversal (ROW_MAJOR). */
    public static ProviderSpec any(int cardinality, ItemFilterConditionSpec condition) {
        return new ProviderSpec(cardinality, TraversalOrder.ROW_MAJOR, condition, CellDerivedProviderKind.UNRESTRICTED, null);
    }

    /** Convenience: full constructor for unrestricted cell-derived provider. */
    public static ProviderSpec any(int cardinality, TraversalOrder traversalOrder, ItemFilterConditionSpec condition) {
        return new ProviderSpec(cardinality, traversalOrder, condition, CellDerivedProviderKind.UNRESTRICTED, null);
    }

    /** Typed value provider Υ_tbl^val. */
    public static ProviderSpec val(ItemFilterConditionSpec condition) {
        return new ProviderSpec(1, TraversalOrder.ROW_MAJOR, condition, CellDerivedProviderKind.VAL, null);
    }

    public static ProviderSpec val(int cardinality, ItemFilterConditionSpec condition) {
        return new ProviderSpec(cardinality, TraversalOrder.ROW_MAJOR, condition, CellDerivedProviderKind.VAL, null);
    }

    public static ProviderSpec val(int cardinality, TraversalOrder traversalOrder, ItemFilterConditionSpec condition) {
        return new ProviderSpec(cardinality, traversalOrder, condition, CellDerivedProviderKind.VAL, null);
    }

    /** Typed attribute provider Υ_tbl^attr. Always cardinality 1. */
    public static ProviderSpec attr(ItemFilterConditionSpec condition) {
        return new ProviderSpec(1, TraversalOrder.ROW_MAJOR, condition, CellDerivedProviderKind.ATTR, null);
    }

    public static ProviderSpec attr(TraversalOrder traversalOrder, ItemFilterConditionSpec condition) {
        return new ProviderSpec(1, traversalOrder, condition, CellDerivedProviderKind.ATTR, null);
    }

    /** Typed auxiliary provider Υ_tbl^aux. */
    public static ProviderSpec aux(ItemFilterConditionSpec condition) {
        return new ProviderSpec(1, TraversalOrder.ROW_MAJOR, condition, CellDerivedProviderKind.AUX, null);
    }

    public static ProviderSpec aux(int cardinality, TraversalOrder traversalOrder, ItemFilterConditionSpec condition) {
        return new ProviderSpec(cardinality, traversalOrder, condition, CellDerivedProviderKind.AUX, null);
    }

    /** Context-derived constant provider. */
    public static ProviderSpec ctx(String text, ItemType type) {
        return new ProviderSpec(1, TraversalOrder.ROW_MAJOR, null, null, new ContextLiteralSpec(text, type));
    }

    /** Context-derived attribute constant provider. */
    public static ProviderSpec ctxAttr(String text) {
        return ctx(text, ItemType.ATTRIBUTE);
    }

    /** Context-derived value constant provider. */
    public static ProviderSpec ctxVal(String text) {
        return ctx(text, ItemType.VALUE);
    }

    /** Context-derived auxiliary constant provider. */
    public static ProviderSpec ctxAux(String text) {
        return ctx(text, ItemType.AUXILIARY);
    }

    public boolean isContextLiteral() {
        return contextLiteral != null;
    }
}
