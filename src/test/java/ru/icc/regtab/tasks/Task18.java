package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.semantics.item.ItemType;
import ru.icc.regtab.itm.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.pattern.TablePattern;

/**
 * Task 18: vertical list of {@code KEY=value} lines per student; pivot to one wide row per student.
 * <p>
 * Subtable: {@code oneOrMore()}; row count per block inferred as {@code 1 + 15} from {@code rows().one()} + {@code rows().exactly(15)}.
 * Each cell is compound {@code attr=value}; {@code val().actions().rec(...)} marks the first row's value as {@code rec}
 * anchor (Task 15 style); {@code actions().avp(...)} on the cell group pairs each value with the key in the same cell.
 */
public final class Task18 extends TaskBase {

    private static final ItemFilterCondition AVP_SAME_CELL = (a, c) -> c.sameCell(a);

    private static final ItemFilterCondition REC_VALUES_BELOW = (a, c) ->
            c.below(a).sameSubtable() && c.type() == ItemType.VALUE;

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().one()
                .cells().one().compound()
                .attr()
                .sep("=")
                .val().actions().rec(REC_VALUES_BELOW)
                .actions().avp(AVP_SAME_CELL).rows()
                .exactly(15)
                .cells().one().compound()
                .attr()
                .sep("=")
                .val()
                .end()
                .actions().avp(AVP_SAME_CELL)
                .apply(syntax);
    }
}
