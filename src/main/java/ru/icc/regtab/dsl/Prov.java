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
 * Disjunction mirrors RTL {@code |} with the same distribution semantics as the
 * RTL compiler: {@code A.and(B.or(C))} ≙ {@code A&(B|C)} ≙ {@code (A&B)|(A&C)}.
 * <p>
 * The provider kind (VAL/ATTR/UNRESTRICTED) is not set here — it is inferred from
 * the action the provider is passed to, exactly as in the RTL compiler.
 */
public final class Prov implements ProvArg {

    /** Disjunction of conjunctions: outer list = OR-groups, inner list = AND-ed terms. */
    private final List<List<FilterTerm>> orGroups;
    private final int cardinality;
    private final TraversalOrder order;

    Prov(FilterTerm term) {
        this(List.of(List.of(term)), 1, TraversalOrder.ROW_MAJOR);
    }

    private Prov(List<List<FilterTerm>> orGroups, int cardinality, TraversalOrder order) {
        this.orGroups = orGroups;
        this.cardinality = cardinality;
        this.order = order;
    }

    /** Conjunction (RTL {@code &}); nested ORs are distributed: {@code A&(B|C) → (A&B)|(A&C)}. */
    public Prov and(Prov other) {
        var distributed = new ArrayList<List<FilterTerm>>();
        for (List<FilterTerm> left : orGroups) {
            for (List<FilterTerm> right : other.orGroups) {
                var combined = new ArrayList<>(left);
                combined.addAll(right);
                distributed.add(List.copyOf(combined));
            }
        }
        return new Prov(List.copyOf(distributed), cardinality, order);
    }

    /** Disjunction (RTL {@code |}). */
    public Prov or(Prov other) {
        var groups = new ArrayList<>(orGroups);
        groups.addAll(other.orGroups);
        return new Prov(List.copyOf(groups), cardinality, order);
    }

    /** Escape hatch: conjunction with an arbitrary Java item filter (no RTL analog). */
    public Prov where(String description, BiPredicate<CellDerivedItem, CellDerivedItem> predicate) {
        return and(new Prov(new FilterTerm.Custom(description, predicate)));
    }

    /** Cardinality {@code {n}} — at most n items. */
    public Prov card(int n) {
        return new Prov(orGroups, n, order);
    }

    /** Cardinality {@code *} — unbounded. */
    public Prov unbounded() {
        return new Prov(orGroups, ProviderSpec.UNBOUNDED, order);
    }

    /** Traversal order {@code ^} — column-major. */
    public Prov colMajor() {
        return new Prov(orGroups, cardinality, TraversalOrder.COLUMN_MAJOR);
    }

    /** Traversal order {@code -} — reverse row-major. */
    public Prov reversed() {
        return new Prov(orGroups, cardinality, TraversalOrder.REVERSE_ROW_MAJOR);
    }

    /** Traversal order {@code -^} — reverse column-major. */
    public Prov reversedColMajor() {
        return new Prov(orGroups, cardinality, TraversalOrder.REVERSE_COLUMN_MAJOR);
    }

    /** Resolves to a {@link ProviderSpec} with the kind inferred from the enclosing action. */
    ProviderSpec spec(CellDerivedProviderKind kind) {
        ItemFilterConditionSpec condition;
        if (orGroups.size() == 1) {
            List<FilterTerm> terms = orGroups.get(0);
            condition = terms.size() == 1
                    ? new ItemFilterConditionSpec.Bare(terms.get(0))
                    : new ItemFilterConditionSpec.And(terms);
        } else {
            condition = new ItemFilterConditionSpec.Or(
                    orGroups.stream().map(ItemFilterConditionSpec.And::new).toList());
        }
        int actualCardinality = (kind == CellDerivedProviderKind.ATTR) ? 1 : cardinality;
        return new ProviderSpec(actualCardinality, order, condition, kind, null);
    }
}
