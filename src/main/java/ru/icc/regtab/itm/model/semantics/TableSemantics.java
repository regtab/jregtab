package ru.icc.regtab.itm.model.semantics;

import ru.icc.regtab.itm.model.semantics.action.InterpretationAction;
import ru.icc.regtab.itm.model.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.model.semantics.item.ContextDerivedItem;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The semantic layer of an ITM instance (Def. 16):
 * L_sem = (I_tbl, I_ctx, A).
 */
public final class TableSemantics {

    private final Set<CellDerivedItem> cellDerivedItems;
    private final Set<ContextDerivedItem> contextDerivedItems;
    private final List<InterpretationAction> actions;

    public TableSemantics(
            Set<CellDerivedItem> cellDerivedItems,
            Set<ContextDerivedItem> contextDerivedItems,
            List<InterpretationAction> actions
    ) {
        this.cellDerivedItems = Set.copyOf(Objects.requireNonNull(cellDerivedItems));
        this.contextDerivedItems = Set.copyOf(Objects.requireNonNull(contextDerivedItems));
        this.actions = List.copyOf(Objects.requireNonNull(actions));
    }

    public Set<CellDerivedItem> cellDerivedItems() { return cellDerivedItems; }
    public Set<ContextDerivedItem> contextDerivedItems() { return contextDerivedItems; }
    public List<InterpretationAction> actions() { return actions; }
}
