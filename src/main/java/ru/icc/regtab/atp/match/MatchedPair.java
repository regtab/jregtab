package ru.icc.regtab.atp.match;

import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.itm.syntax.Cell;

import java.util.Objects;

/**
 * A matched cell pattern–cell pair (P, c) ∈ M accumulated during
 * syntactic layer matching.
 *
 * @param pattern the cell pattern from the ATP
 * @param cell    the matched ITM cell
 */
public record MatchedPair(CellPattern pattern, Cell cell) {

    public MatchedPair {
        Objects.requireNonNull(pattern, "pattern");
        Objects.requireNonNull(cell, "cell");
    }
}
