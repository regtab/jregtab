package ru.icc.regtab.itm.semantics.provider;

import ru.icc.regtab.itm.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.semantics.item.Item;
import ru.icc.regtab.itm.semantics.item.ItemType;
import ru.icc.regtab.itm.syntax.Cell;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Cell-derived item provider (def:cell-derived-item-provider):
 * Υ^{J,k}_{τ,κ}(anchor) = Ω_τ(Φ_κ(anchor, J \ {anchor}))[:k].
 * <p>
 * Parameterized by traversal order τ, item filter Φ_κ, item linearization Ω_τ,
 * target set J, and cardinality k.
 * <p>
 * Implementation: J is accessed through a {@link CellDerivedItemIndex} (shared by all providers
 * of a table) and a {@link CandidateScope} — a static over-approximation of the support of κ.
 * The index yields the candidates of the scope already in the order of Ω_τ, so {@link #provide}
 * neither copies J nor sorts: it scans the slice in traversal order, applies κ to every candidate,
 * and stops after k matches. The result is identical to filtering all of J and sorting.
 */
public final class CellDerivedItemProvider implements ItemProvider {

    public static final int UNBOUNDED = Integer.MAX_VALUE;
    private static final TraversalOrder DEFAULT_TRAVERSAL_ORDER = TraversalOrder.ROW_MAJOR;

    private final ItemFilter filter;
    private final ItemLinearization linearization;
    private final CellDerivedItemIndex index;
    private final CandidateScope scope;
    private final int cardinality;
    private final CellDerivedProviderKind cellKind;
    /**
     * When {@code true} (default), J is {@code J \\ {anchor}} before κ. When {@code false}, anchor stays in J
     * — used for {@code O_fill} so κ can select the anchor item (e.g. {@code sameCell}).
     */
    private final boolean excludeAnchorFromCandidates;
    /**
     * When {@code true}, an anchor whose type is incompatible with {@link #cellKind} causes
     * {@link #provide} to return an empty list (silent skip) instead of throwing.
     * Set for providers that were inherited from a parent scope (row/subrow/subtable level).
     */
    private final boolean lenient;

    /**
     * Full constructor over a shared item index.
     *
     * @param filter        item filter Φ_κ
     * @param linearization item linearization Ω_τ
     * @param index         index over the target set J (shared between providers; J may grow after construction)
     * @param scope         candidate scope — must contain the support of κ; {@link CandidateScope#ALL} is always correct
     * @param cardinality   maximum number of items to return (Integer.MAX_VALUE for unbounded)
     * @param cellKind      restriction on J and on anchor type for named ITM provider instances
     * @param excludeAnchorFromCandidates if {@code false}, anchor is not removed from J before κ (for {@code O_fill})
     * @param lenient       if {@code true}, incompatible anchor type causes silent empty result instead of throw
     */
    public CellDerivedItemProvider(ItemFilter filter, ItemLinearization linearization,
                                   CellDerivedItemIndex index, CandidateScope scope, int cardinality,
                                   CellDerivedProviderKind cellKind, boolean excludeAnchorFromCandidates,
                                   boolean lenient) {
        this.filter = Objects.requireNonNull(filter, "filter");
        this.linearization = Objects.requireNonNull(linearization, "linearization");
        this.index = Objects.requireNonNull(index, "index");
        this.scope = Objects.requireNonNull(scope, "scope");
        if (cardinality < 0) throw new IllegalArgumentException("cardinality must be non-negative: " + cardinality);
        this.cardinality = cardinality;
        this.cellKind = Objects.requireNonNull(cellKind, "cellKind");
        this.excludeAnchorFromCandidates = excludeAnchorFromCandidates;
        this.lenient = lenient;
    }

    /**
     * @param filter        item filter Φ_κ
     * @param linearization item linearization Ω_τ
     * @param targetSet     target set J of cell-derived items (read when {@link #provide} runs; may grow after construction)
     * @param cardinality   maximum number of items to return (Integer.MAX_VALUE for unbounded)
     */
    public CellDerivedItemProvider(ItemFilter filter, ItemLinearization linearization,
                                   Set<CellDerivedItem> targetSet, int cardinality) {
        this(filter, linearization, targetSet, cardinality, CellDerivedProviderKind.UNRESTRICTED);
    }

    /**
     * @param cellKind restriction on J and on anchor type for named ITM provider instances; use
     *                 {@link CellDerivedProviderKind#UNRESTRICTED} for legacy behaviour.
     */
    public CellDerivedItemProvider(ItemFilter filter, ItemLinearization linearization,
                                   Set<CellDerivedItem> targetSet, int cardinality,
                                   CellDerivedProviderKind cellKind) {
        this(filter, linearization, targetSet, cardinality, cellKind, true);
    }

    /**
     * @param excludeAnchorFromCandidates if {@code false}, anchor is not removed from J before κ (for {@code O_fill}).
     * @param lenient                     if {@code true}, incompatible anchor type causes silent empty result instead of throw.
     */
    public CellDerivedItemProvider(ItemFilter filter, ItemLinearization linearization,
                                   Set<CellDerivedItem> targetSet, int cardinality,
                                   CellDerivedProviderKind cellKind, boolean excludeAnchorFromCandidates,
                                   boolean lenient) {
        this(filter, linearization, new CellDerivedItemIndex(Objects.requireNonNull(targetSet, "targetSet")),
                CandidateScope.ALL, cardinality, cellKind, excludeAnchorFromCandidates, lenient);
    }

    /** @see #CellDerivedItemProvider(ItemFilter, ItemLinearization, Set, int, CellDerivedProviderKind, boolean, boolean) */
    public CellDerivedItemProvider(ItemFilter filter, ItemLinearization linearization,
                                   Set<CellDerivedItem> targetSet, int cardinality,
                                   CellDerivedProviderKind cellKind, boolean excludeAnchorFromCandidates) {
        this(filter, linearization, targetSet, cardinality, cellKind, excludeAnchorFromCandidates, false);
    }

    /**
     * Convenience: wraps predicate κ into ItemFilter, creates linearization from traversal order.
     */
    public CellDerivedItemProvider(ItemFilterCondition predicate, TraversalOrder traversalOrder,
                                   Set<CellDerivedItem> targetSet, int cardinality) {
        this(new ItemFilter(predicate), new ItemLinearization(traversalOrder), targetSet, cardinality,
                CellDerivedProviderKind.UNRESTRICTED);
    }

    /**
     * Named ITM cell-derived provider instance (Υ<sub>tbl</sub><sup>val|attr|aux</sup>).
     */
    public CellDerivedItemProvider(ItemFilterCondition predicate, TraversalOrder traversalOrder,
                                   Set<CellDerivedItem> targetSet, int cardinality,
                                   CellDerivedProviderKind cellKind) {
        this(new ItemFilter(predicate), new ItemLinearization(traversalOrder), targetSet, cardinality, cellKind, true);
    }

    /**
     * Same as {@link #CellDerivedItemProvider(ItemFilterCondition, TraversalOrder, Set, int, CellDerivedProviderKind)}
     * with explicit anchor exclusion (for {@code O_fill} vs {@code O_rec} / {@code O_prefix} / {@code O_suffix}).
     */
    public CellDerivedItemProvider(ItemFilterCondition predicate, TraversalOrder traversalOrder,
                                   Set<CellDerivedItem> targetSet, int cardinality,
                                   CellDerivedProviderKind cellKind, boolean excludeAnchorFromCandidates) {
        this(new ItemFilter(predicate), new ItemLinearization(traversalOrder), targetSet, cardinality, cellKind,
                excludeAnchorFromCandidates, false);
    }

    /**
     * Named ITM cell-derived provider instance with lenient anchor-kind check.
     * Used by {@code SemanticConstructor} when the action was inherited from a parent scope.
     */
    public CellDerivedItemProvider(ItemFilterCondition predicate, TraversalOrder traversalOrder,
                                   Set<CellDerivedItem> targetSet, int cardinality,
                                   CellDerivedProviderKind cellKind, boolean excludeAnchorFromCandidates,
                                   boolean lenient) {
        this(new ItemFilter(predicate), new ItemLinearization(traversalOrder), targetSet, cardinality, cellKind,
                excludeAnchorFromCandidates, lenient);
    }

    /**
     * Convenience: wraps predicate κ, creates linearization, unbounded cardinality (k = ∞).
     */
    public CellDerivedItemProvider(ItemFilterCondition predicate, TraversalOrder traversalOrder,
                                   Set<CellDerivedItem> targetSet) {
        this(new ItemFilter(predicate), new ItemLinearization(traversalOrder), targetSet, UNBOUNDED,
                CellDerivedProviderKind.UNRESTRICTED);
    }

    /**
     * Convenience: default traversal order (→ ROW_MAJOR), specified cardinality.
     */
    public CellDerivedItemProvider(ItemFilterCondition predicate,
                                   Set<CellDerivedItem> targetSet, int cardinality) {
        this(predicate, DEFAULT_TRAVERSAL_ORDER, targetSet, cardinality);
    }

    /**
     * Convenience: default traversal order (→ ROW_MAJOR), unbounded cardinality (k = ∞).
     */
    public CellDerivedItemProvider(ItemFilterCondition predicate,
                                   Set<CellDerivedItem> targetSet) {
        this(predicate, DEFAULT_TRAVERSAL_ORDER, targetSet, UNBOUNDED);
    }

    public ItemFilter filter() { return filter; }
    public ItemLinearization linearization() { return linearization; }
    public Set<CellDerivedItem> targetSet() { return index.items(); }
    public CellDerivedItemIndex index() { return index; }
    public CandidateScope scope() { return scope; }
    public int cardinality() { return cardinality; }
    public CellDerivedProviderKind cellKind() { return cellKind; }

    @Override
    public List<CellDerivedItem> provide(Item anchor) {
        if (!(anchor instanceof CellDerivedItem anch)) {
            throw new IllegalArgumentException("CellDerivedItemProvider requires a cell-derived anchor");
        }
        if (!isAnchorCompatible(anch)) {
            if (lenient) return List.of();
            throw new IllegalArgumentException(
                    "Υ_tbl^val and Υ_tbl^attr require a value-associated anchor, got: " + anch.type());
        }
        List<CellDerivedItem> result = new ArrayList<>();
        if (cardinality == 0) return result;

        CellDerivedItemIndex.Range range = index.lookup(scope, anch, linearization.traversalOrder());
        if (range.isEmpty()) return result;

        CellDerivedItem[] a = range.array();
        if (!range.backward()) {
            for (int i = range.from(); i < range.to(); i++) {
                if (accept(anch, a[i], result)) return result;
            }
            return result;
        }
        // Reverse traversal: cells in reverse order, items inside a cell still by ascending index.
        int i = range.to() - 1;
        while (i >= range.from()) {
            Cell cell = a[i].cell();
            int j = i;
            while (j > range.from() && a[j - 1].cell() == cell) j--;
            for (int k = j; k <= i; k++) {
                if (accept(anch, a[k], result)) return result;
            }
            i = j - 1;
        }
        return result;
    }

    /** Applies J \ {anchor}, the kind restriction on J and κ; returns {@code true} once k items are collected. */
    private boolean accept(CellDerivedItem anchor, CellDerivedItem candidate, List<CellDerivedItem> result) {
        if (excludeAnchorFromCandidates && candidate == anchor) return false;
        if (!isCandidateCompatible(candidate)) return false;
        if (!filter.predicate().test(anchor, candidate)) return false;
        result.add(candidate);
        return result.size() >= cardinality;
    }

    private boolean isAnchorCompatible(CellDerivedItem anchor) {
        if (cellKind == CellDerivedProviderKind.UNRESTRICTED) return true;
        return switch (cellKind) {
            case VAL, ATTR -> anchor.type() == ItemType.VALUE;
            case AUX       -> anchor.type() == ItemType.VALUE || anchor.type() == ItemType.ATTRIBUTE;
            default        -> true;
        };
    }

    private boolean isCandidateCompatible(CellDerivedItem candidate) {
        return switch (cellKind) {
            case UNRESTRICTED, AUX -> true;
            case VAL -> candidate.type() == ItemType.VALUE;
            case ATTR -> candidate.type() == ItemType.ATTRIBUTE;
        };
    }
}
