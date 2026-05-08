package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.pattern.TablePattern;
import ru.icc.regtab.itm.model.syntax.TableSyntax;

/**
 * Task 01: subtables with RecAction (sameSubtable predicate).
 * Pattern from USAGE_EXAMPLE_1.
 */
public final class Task01 extends TaskBase {

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().one()
                .cells().one().val()
                .actions().rec((a, c) -> c.sameSubtable(a))
                .cells().exactly(2).val()
                .cells().oneOrMore().skip()
                .rows().one()
                .cells().one().skip()
                .cells().exactly(4).val()
                .cells().oneOrMore().skip()
                .apply(syntax);
    }
}
