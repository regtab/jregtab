package ru.icc.regtab.dsl;

import ru.icc.regtab.atp.spec.FilterTerm;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.semantics.provider.CellDerivedProviderKind;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * Cell-derived provider builder — the DSL mirror of an RTL {@code tblProvSpec}.
 * <p>
 * Immutable; starts from a spatial/content constant ({@code ST}, {@code ROW}, …)
 * or factory ({@code C(n)}, {@code tag("H")}, …) in {@link Rtl}, then chains:
 * {@code ST.and(C(2, 5)).unbounded().colMajor()} ≙ RTL {@code ^ST&C2..5*}.
 * <p>
 * The provider kind (VAL/ATTR/UNRESTRICTED) is not set here — it is inferred from
 * the action the provider is passed to, exactly as in the RTL compiler.
 */
public final class Prov implements ProvArg {

    private final List<FilterTerm> terms;
    private final int cardinality;
    private final TraversalOrder order;

    Prov(FilterTerm term) {
        this(List.of(term), 1, TraversalOrder.ROW_MAJOR);
    }

    private Prov(List<FilterTerm> terms, int cardinality, TraversalOrder order) {
        this.terms = List.copyOf(terms);
        this.cardinality = cardinality;
        this.order = order;
    }

    /** Conjunction with another constraint (RTL {@code &}). */
    public Prov and(Prov other) {
        var merged = new ArrayList<>(terms);
        merged.addAll(other.terms);
        return new Prov(merged, cardinality, order);
    }

    /** Escape hatch: conjunction with an arbitrary Java item filter (no RTL analog). */
    public Prov where(String description, BiPredicate<CellDerivedItem, CellDerivedItem> predicate) {
        var merged = new ArrayList<>(terms);
        merged.add(new FilterTerm.Custom(description, predicate));
        return new Prov(merged, cardinality, order);
    }

    /** Cardinality {@code {n}} — at most n items. */
    public Prov card(int n) {
        return new Prov(terms, n, order);
    }

    /** Cardinality {@code *} — unbounded. */
    public Prov unbounded() {
        return new Prov(terms, ProviderSpec.UNBOUNDED, order);
    }

    /** Traversal order {@code ^} — column-major. */
    public Prov colMajor() {
        return new Prov(terms, cardinality, TraversalOrder.COLUMN_MAJOR);
    }

    /** Traversal order {@code -} — reverse row-major. */
    public Prov reversed() {
        return new Prov(terms, cardinality, TraversalOrder.REVERSE_ROW_MAJOR);
    }

    /** Traversal order {@code -^} — reverse column-major. */
    public Prov reversedColMajor() {
        return new Prov(terms, cardinality, TraversalOrder.REVERSE_COLUMN_MAJOR);
    }

    /** Resolves to a {@link ProviderSpec} with the kind inferred from the enclosing action. */
    ProviderSpec spec(CellDerivedProviderKind kind) {
        ItemFilterConditionSpec condition = terms.size() == 1
                ? new ItemFilterConditionSpec.Bare(terms.get(0))
                : new ItemFilterConditionSpec.And(terms);
        int actualCardinality = (kind == CellDerivedProviderKind.ATTR) ? 1 : cardinality;
        return new ProviderSpec(actualCardinality, order, condition, kind, null);
    }
}
