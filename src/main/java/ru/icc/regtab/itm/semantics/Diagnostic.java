package ru.icc.regtab.itm.semantics;

import ru.icc.regtab.itm.semantics.item.CellDerivedItem;

import java.util.Objects;

/**
 * A diagnostic recorded during working state completion when an operation is not applicable
 * to its anchor (a violated precondition, e.g. a named attribute shared by two records that
 * are being concatenated). By the formal model the operation then has no effect; the
 * diagnostic makes the silent no-op visible to the pattern author.
 *
 * @param anchor    the anchor item of the action that was skipped
 * @param operation the operation name ({@code CONCAT}, {@code JOIN}, …)
 * @param message   what precondition was violated and why
 */
public record Diagnostic(CellDerivedItem anchor, String operation, String message) {
    public Diagnostic {
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(message, "message");
    }

    @Override
    public String toString() {
        return operation + " skipped at " + anchor + ": " + message;
    }
}
