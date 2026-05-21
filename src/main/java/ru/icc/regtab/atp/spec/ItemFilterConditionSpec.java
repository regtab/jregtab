package ru.icc.regtab.atp.spec;

import ru.icc.regtab.itm.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.semantics.provider.ItemFilterCondition;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * Specification of an item filter condition — structured, serializable alternative to
 * opaque {@link ItemFilterCondition} lambdas.
 *
 * <p>Mirrors the RTL grammar's {@code tblProvSpec} constraint structure:
 * <ul>
 *   <li>{@link Bare} — a single bare {@code spatConstr} (no parentheses)</li>
 *   <li>{@link And}  — {@code (c1 & c2 & …)} — one orGroup</li>
 *   <li>{@link Or}   — {@code (g1 | g2 | …)} — multiple orGroups</li>
 *   <li>{@link Custom} — escape hatch; {@link #toRtl()} throws UOE</li>
 * </ul>
 */
public sealed interface ItemFilterConditionSpec permits
        ItemFilterConditionSpec.Bare,
        ItemFilterConditionSpec.And,
        ItemFilterConditionSpec.Or,
        ItemFilterConditionSpec.Custom {

    /** RTL representation of this condition. */
    String toRtl();

    /** Produces the runtime behavioral predicate from this specification. */
    ItemFilterCondition toCondition();

    // ---- Sealed variants ----

    /** Bare spatConstr — no parentheses. */
    record Bare(FilterTerm constraint) implements ItemFilterConditionSpec {
        public String toRtl() { return constraint.toRtl(); }
        public ItemFilterCondition toCondition() { return constraint.toCondition(); }
    }

    /** Parenthesized AND of constraints: {@code (c1 & c2 & …)}. */
    record And(List<FilterTerm> terms) implements ItemFilterConditionSpec {
        public String toRtl() {
            return "(" + terms.stream().map(FilterTerm::toRtl).collect(Collectors.joining(" & ")) + ")";
        }
        public ItemFilterCondition toCondition() {
            return (a, c) -> terms.stream().allMatch(t -> t.toCondition().test(a, c));
        }
    }

    /**
     * Parenthesized OR of AND-groups: {@code (g1 | g2 | …)}.
     * Not used in tasks 01–50 but supported by the grammar.
     */
    record Or(List<And> groups) implements ItemFilterConditionSpec {
        public String toRtl() {
            return "(" + groups.stream().map(And::toRtl).collect(Collectors.joining(" | ")) + ")";
        }
        public ItemFilterCondition toCondition() {
            return (a, c) -> groups.stream().anyMatch(g -> g.toCondition().test(a, c));
        }
    }

    /** Escape hatch for conditions that have no RTL analog. */
    record Custom(String description,
                  BiPredicate<CellDerivedItem, CellDerivedItem> predicate)
            implements ItemFilterConditionSpec {
        public String toRtl() {
            throw new UnsupportedOperationException("Custom ItemFilterConditionSpec has no RTL analog: " + description);
        }
        public ItemFilterCondition toCondition() { return predicate::test; }
    }

    // ---- General factories ----

    static Bare bare(FilterTerm c) { return new Bare(c); }

    static And and(FilterTerm... terms) { return new And(List.of(terms)); }

    static Or or(And... groups) { return new Or(List.of(groups)); }

    // ---- Bare shorthands ----

    static Bare sameSubtable() { return new Bare(FilterTerm.SameSubtable.INSTANCE); }
    static Bare sameSubrow()   { return new Bare(FilterTerm.SameSubrow.INSTANCE); }
    static Bare sameSubcol()   { return new Bare(FilterTerm.SameSubcol.INSTANCE); }
    static Bare sameCell()     { return new Bare(FilterTerm.SameCell.INSTANCE); }
    static Bare sameRow()      { return new Bare(FilterTerm.SameRow.INSTANCE); }
    static Bare sameCol()      { return new Bare(FilterTerm.SameCol.INSTANCE); }
    static Bare below()        { return new Bare(FilterTerm.Below.INSTANCE); }
    static Bare above()        { return new Bare(FilterTerm.Above.INSTANCE); }
    static Bare rightOf()      { return new Bare(FilterTerm.RightOf.INSTANCE); }
    static Bare leftOf()       { return new Bare(FilterTerm.LeftOf.INSTANCE); }
}
