package ru.icc.regtab.itm.atp.spec;

import ru.icc.regtab.itm.model.syntax.Cell;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Cell match condition λ (Def. 22): a predicate on cells expressed as a
 * finite Boolean combination of atomic constraints on cell properties.
 * <p>
 * A cell c satisfies λ iff {@code λ(c) = true}.
 */
public record CellMatchCondition(Predicate<Cell> predicate) {

    public CellMatchCondition {
        Objects.requireNonNull(predicate, "predicate");
    }

    /** Tests whether the given cell satisfies this condition. */
    public boolean test(Cell cell) {
        return predicate.test(cell);
    }

    /** Conjunction: this AND other. */
    public CellMatchCondition and(CellMatchCondition other) {
        return new CellMatchCondition(predicate.and(other.predicate));
    }

    /** Disjunction: this OR other. */
    public CellMatchCondition or(CellMatchCondition other) {
        return new CellMatchCondition(predicate.or(other.predicate));
    }

    /** Negation: NOT this. */
    public CellMatchCondition negate() {
        return new CellMatchCondition(predicate.negate());
    }
}
