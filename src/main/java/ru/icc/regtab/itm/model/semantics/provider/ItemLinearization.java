package ru.icc.regtab.itm.model.semantics.provider;

import ru.icc.regtab.itm.model.semantics.item.CellDerivedItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Item linearization Ω_τ (Def. 8): produces a sorted sequence of items
 * according to traversal order τ and the precedence relation prec_τ.
 * <p>
 * For two items a, b with cells c_a, c_b at positions (i,j) and (i',j'):
 * <ul>
 *   <li>ROW_MAJOR (→):            i &lt; i' or (i == i' and j &lt; j')</li>
 *   <li>REVERSE_ROW_MAJOR (←):    i &gt; i' or (i == i' and j &lt; j')</li>
 *   <li>COLUMN_MAJOR (↓):         j &lt; j' or (j == j' and i &lt; i')</li>
 *   <li>REVERSE_COLUMN_MAJOR (↑): j &gt; j' or (j == j' and i &lt; i')</li>
 * </ul>
 * Within the same cell, items are ordered by their index.
 */
public final class ItemLinearization {

    private final TraversalOrder traversalOrder;

    public ItemLinearization(TraversalOrder traversalOrder) {
        this.traversalOrder = Objects.requireNonNull(traversalOrder, "traversalOrder");
    }

    public TraversalOrder traversalOrder() {
        return traversalOrder;
    }

    public List<CellDerivedItem> sort(Collection<CellDerivedItem> items) {
        List<CellDerivedItem> result = new ArrayList<>(items);
        result.sort(comparator());
        return result;
    }

    private Comparator<CellDerivedItem> comparator() {
        return (a, b) -> {
            if (a.cell() == b.cell()) {
                return Integer.compare(a.index(), b.index());
            }
            int ar = a.cell().row(), ac = a.cell().col();
            int br = b.cell().row(), bc = b.cell().col();
            return switch (traversalOrder) {
                case ROW_MAJOR -> ar != br ? Integer.compare(ar, br) : Integer.compare(ac, bc);
                case REVERSE_ROW_MAJOR -> ar != br ? Integer.compare(br, ar) : Integer.compare(ac, bc);
                case COLUMN_MAJOR -> ac != bc ? Integer.compare(ac, bc) : Integer.compare(ar, br);
                case REVERSE_COLUMN_MAJOR -> ac != bc ? Integer.compare(bc, ac) : Integer.compare(ar, br);
            };
        };
    }
}
