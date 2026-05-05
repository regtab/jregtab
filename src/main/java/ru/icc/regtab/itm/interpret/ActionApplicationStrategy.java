package ru.icc.regtab.itm.interpret;

import ru.icc.regtab.itm.model.semantics.action.InterpretationAction;
import ru.icc.regtab.itm.model.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.model.syntax.GridPosition;

import java.util.Comparator;

/**
 * Strategy for ordering anchor items during working state completion (Def. 15).
 * <p>
 * {@code ROW_FIRST} (Γ_row) processes anchors row-by-row (primary: row, secondary: column).
 * {@code COLUMN_FIRST} (Γ_col) processes anchors column-by-column (primary: column, secondary: row).
 */
public enum ActionApplicationStrategy {

    ROW_FIRST(Comparator.comparingInt((GridPosition p) -> p.row())
            .thenComparingInt(GridPosition::col)),

    COLUMN_FIRST(Comparator.comparingInt((GridPosition p) -> p.col())
            .thenComparingInt(GridPosition::row));

    private final Comparator<GridPosition> positionComparator;

    ActionApplicationStrategy(Comparator<GridPosition> positionComparator) {
        this.positionComparator = positionComparator;
    }

    Comparator<InterpretationAction> actionComparator() {
        return (a1, a2) -> {
            GridPosition p1 = anchorPosition(a1);
            GridPosition p2 = anchorPosition(a2);
            if (p1 != null && p2 != null) return positionComparator.compare(p1, p2);
            if (p1 != null) return -1;
            if (p2 != null) return 1;
            return 0;
        };
    }

    private static GridPosition anchorPosition(InterpretationAction action) {
        if (action.anchor() instanceof CellDerivedItem cdi) {
            return cdi.cell().pos();
        }
        return null;
    }
}
