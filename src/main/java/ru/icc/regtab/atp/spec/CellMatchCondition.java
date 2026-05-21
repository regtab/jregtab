package ru.icc.regtab.atp.spec;

import ru.icc.regtab.itm.syntax.Cell;

import java.util.Objects;

/**
 * Cell match condition λ (def:atp:cell-match-condition): a predicate on cells expressed as a
 * finite Boolean combination of atomic constraints on cell properties.
 * <p>
 * A cell c satisfies λ iff {@code λ(c) = true}.
 */
public record CellMatchCondition(CellPredicate cellPredicate) {

    public CellMatchCondition {
        Objects.requireNonNull(cellPredicate, "cellPredicate");
    }

    /** Tests whether the given cell satisfies this condition. */
    public boolean test(Cell cell) {
        return cellPredicate.toPredicate().test(cell);
    }
}
