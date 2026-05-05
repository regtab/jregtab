package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.model.semantics.item.ItemType;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.pattern.TablePattern;
import ru.icc.regtab.itm.model.syntax.TableSyntax;

/**
 * Task 26: blocks of 6 rows (label + index + value); pivot to wide table with columns "", "0".."5".
 * <p>
 * Subtable boundaries: every 6 rows, inferred as {@code 1 + 5} from {@code rows().one()} + {@code rows().exactly(5)} with {@code oneOrMore()}.
 * <p>
 * Context {@code O_avp} on the label and {@code O_rec}: either {@code actions().avp("").rec(…)} or
 * {@code actions().rec(…).avp("")} (same semantics). Indices come from {@code .attr()} + {@code O_avp} on value cells.
 */
public final class Task26 extends TaskBase {

    private static final ItemFilterCondition COL2_IN_SUBTABLE = (a, c) ->
            c.is.in.sameSubtable(a) && c.is.in.col(2);

    private static final ItemFilterCondition ATTR_IN_SAME_ROW = (a, c) ->
            c.is.in.sameRow(a) && c.type() == ItemType.ATTRIBUTE;

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().one()
                .cells().one().val()
                .actions().avp("").rec(COL2_IN_SUBTABLE)
                .cells().one().attr()
                .cells().one().val()
                .actions().avp(ATTR_IN_SAME_ROW).rows()
                .exactly(5)
                .cells().one().skip()
                .cells().one().attr()
                .cells().one().val()
                .actions().avp(ATTR_IN_SAME_ROW)
                .apply(syntax);
    }
}
