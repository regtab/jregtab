package ru.icc.regtab.itm.semantics.action;

import ru.icc.regtab.itm.semantics.item.Item;
import ru.icc.regtab.itm.semantics.operation.WorkingStateOperation;
import ru.icc.regtab.itm.semantics.provider.ItemProvider;

import java.util.List;
import java.util.Objects;

/**
 * Interpretation action (def:interpretation-action): a triple (anchor, providers, operation)
 * where anchor is the item being interpreted, providers yield related items,
 * and operation updates the working state.
 *
 * @param inherited {@code true} if the action was inherited from the {@code actSpecs} of an
 *                  enclosing scope (table/subtable/row/subrow/cell level) rather than written on
 *                  the anchor's own content spec. Does not affect the semantics of the operation;
 *                  it only decides whether a {@code CONCAT}/{@code JOIN} on an anchor without a
 *                  record is reported as a diagnostic (explicit actions) or skipped silently
 *                  (inherited actions, for which such anchors are routine).
 */
public record InterpretationAction(
        Item anchor,
        List<ItemProvider> providers,
        WorkingStateOperation operation,
        boolean inherited
) {
    public InterpretationAction {
        Objects.requireNonNull(anchor, "anchor");
        providers = List.copyOf(Objects.requireNonNull(providers, "providers"));
        Objects.requireNonNull(operation, "operation");
    }

    /** An explicit action ({@code inherited = false}). */
    public InterpretationAction(Item anchor, List<ItemProvider> providers, WorkingStateOperation operation) {
        this(anchor, providers, operation, false);
    }
}
