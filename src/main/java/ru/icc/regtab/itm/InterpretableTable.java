package ru.icc.regtab.itm;

import ru.icc.regtab.itm.model.semantics.TableSemantics;
import ru.icc.regtab.itm.model.syntax.TableSyntax;

import java.util.Objects;

/**
 * An interpretable table: the union of the syntactic and semantic layers.
 * A table represented as an InterpretableTable at both layers
 * can be automatically interpreted to extract a recordset.
 */
public final class InterpretableTable {

    private final TableSyntax syntax;
    private final TableSemantics semantics;

    public InterpretableTable(TableSyntax syntax, TableSemantics semantics) {
        this.syntax = Objects.requireNonNull(syntax, "syntax");
        this.semantics = Objects.requireNonNull(semantics, "semantics");
    }

    public TableSyntax syntax() { return syntax; }
    public TableSemantics semantics() { return semantics; }
}
