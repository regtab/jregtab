package ru.icc.regtab.itm.semantics.provider;

import ru.icc.regtab.itm.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.semantics.item.Item;
import ru.icc.regtab.itm.semantics.item.ItemType;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Cell-derived item provider (def:cell-derived-item-provider):
 * Υ^{J,k}_{τ,κ}(anchor) = Ω_τ(Φ_κ(anchor, J \ {anchor}))[:k].
 * <p>
 * Parameterized by traversal order τ, item filter Φ_κ, item linearization Ω_τ,
 * target set J, and cardinality k.
 */
public final class CellDerivedItemProvider implements ItemProvider {

    public static final int UNBOUNDED = Integer.MAX_VALUE;
    private static final TraversalOrder DEFAULT_TRAVERSAL_ORDER = TraversalOrder.ROW_MAJOR;

    private final ItemFilter filter;
    private final ItemLinearization linearization;
    private final Set<CellDerivedItem> targetSet;
    private final int cardinality;
    private final CellDerivedProviderKind cellKind;
    /**
     * When {@code true} (default), J is {@code J \\ {anchor}} before κ. When {@code false}, anchor stays in J
     * — used for {@code O_fill} so κ can select the anchor item (e.g. {@code sameCell}).
     */
    private final boolean excludeAnchorFromCandidates;

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
     */
    public CellDerivedItemProvider(ItemFilter filter, ItemLinearization linearization,
                                   Set<CellDerivedItem> targetSet, int cardinality,
                                   CellDerivedProviderKind cellKind, boolean excludeAnchorFromCandidates) {
        this.filter = Objects.requireNonNull(filter, "filter");
        this.linearization = Objects.requireNonNull(linearization, "linearization");
        this.targetSet = Objects.requireNonNull(targetSet, "targetSet");
        if (cardinality < 0) throw new IllegalArgumentException("cardinality must be non-negative: " + cardinality);
        this.cardinality = cardinality;
        this.cellKind = Objects.requireNonNull(cellKind, "cellKind");
        this.excludeAnchorFromCandidates = excludeAnchorFromCandidates;
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
                excludeAnchorFromCandidates);
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
    public Set<CellDerivedItem> targetSet() { return targetSet; }
    public int cardinality() { return cardinality; }
    public CellDerivedProviderKind cellKind() { return cellKind; }

    @Override
    public List<CellDerivedItem> provide(Item anchor) {
        if (!(anchor instanceof CellDerivedItem anch)) {
            throw new IllegalArgumentException("CellDerivedItemProvider requires a cell-derived anchor");
        }
        validateAnchorKind(anch);
        Set<CellDerivedItem> candidates = new HashSet<>(targetSet);
        if (excludeAnchorFromCandidates) {
            candidates.remove(anch);
        }
        candidates = restrictCandidateSet(candidates);
        Set<CellDerivedItem> filtered = filter.apply(anch, candidates);
        List<CellDerivedItem> sorted = linearization.sort(filtered);
        if (sorted.size() <= cardinality) {
            return sorted;
        }
        return sorted.subList(0, cardinality);
    }

    private void validateAnchorKind(CellDerivedItem anchor) {
        if (cellKind == CellDerivedProviderKind.UNRESTRICTED) {
            return;
        }
        switch (cellKind) {
            case VAL, ATTR -> {
                if (anchor.type() != ItemType.VALUE) {
                    throw new IllegalArgumentException(
                            "Υ_tbl^val and Υ_tbl^attr require a value-associated anchor, got: " + anchor.type());
                }
            }
            case AUX -> {
                if (anchor.type() != ItemType.VALUE && anchor.type() != ItemType.ATTRIBUTE) {
                    throw new IllegalArgumentException(
                            "Υ_tbl^aux requires a value- or attribute-associated anchor, got: " + anchor.type());
                }
            }
            default -> { }
        }
    }

    private Set<CellDerivedItem> restrictCandidateSet(Set<CellDerivedItem> candidates) {
        return switch (cellKind) {
            case UNRESTRICTED, AUX -> candidates;
            case VAL -> candidates.stream()
                    .filter(c -> c.type() == ItemType.VALUE)
                    .collect(Collectors.toCollection(HashSet::new));
            case ATTR -> candidates.stream()
                    .filter(c -> c.type() == ItemType.ATTRIBUTE)
                    .collect(Collectors.toCollection(HashSet::new));
        };
    }
}
