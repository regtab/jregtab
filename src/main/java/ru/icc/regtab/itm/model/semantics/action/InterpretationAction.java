package ru.icc.regtab.itm.model.semantics.action;

import ru.icc.regtab.itm.model.semantics.item.Item;
import ru.icc.regtab.itm.model.semantics.operation.WorkingStateOperation;
import ru.icc.regtab.itm.model.semantics.provider.ItemProvider;

import java.util.List;
import java.util.Objects;

/**
 * Interpretation action (Def. 14): a triple (anchor, providers, operation)
 * where anchor is the item being interpreted, providers yield related items,
 * and operation updates the working state.
 */
public record InterpretationAction(
        Item anchor,
        List<ItemProvider> providers,
        WorkingStateOperation operation
) {
    public InterpretationAction {
        Objects.requireNonNull(anchor, "anchor");
        providers = List.copyOf(Objects.requireNonNull(providers, "providers"));
        Objects.requireNonNull(operation, "operation");
    }
}
