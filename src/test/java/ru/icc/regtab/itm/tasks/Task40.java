package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.model.syntax.Cell;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.pattern.TablePattern;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Task 40: stacked blocks (title "Reported crime in &lt;State&gt;", blank row, five year/value rows,
 * optional inter-block blank separator); pivot to wide table with columns {@code ""}, {@code 2004}…{@code 2008}.
 * <p>
 * Subtable size: {@code 1 + 1 + 5 + (0|1)} rows (title, spacer, data, optional separator). Blank spacer
 * and separator rows are two skipped cells per row; subtable boundaries after a separator are inferred only
 * when the next row matches the first row type (title), so the internal spacer after the title is not
 * mistaken for an inter-block separator.
 */
public final class Task40 extends TaskBase {

    private static final Predicate<Cell> REPORTED_CRIME_TITLE = c ->
            c.text().contains("Reported crime in");

    private static final Function<Cell, String> STATE_NAME = c ->
            c.text().replace("Reported crime in", "").trim();

    private static final ItemFilterCondition COL1_IN_SUBTABLE = (a, c) ->
            c.is.in.sameSubtable(a) && c.is.in.col(1);

    private static final ItemFilterCondition ATTR_IN_SAME_ROW = (a, c) -> c.is.in.sameRow(a);

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().one()
                .cells().one().check(REPORTED_CRIME_TITLE).val(STATE_NAME)
                .actions().avp("").rec(COL1_IN_SUBTABLE)
                .cells().one().skip()
                .rows().one()
                .cells().exactly(2).skip()
                .rows().exactly(5)
                .cells().one().attr()
                .cells().one().val()
                .actions().avp(ATTR_IN_SAME_ROW)
                .rows()
                .zeroOrOne()
                .cells().exactly(2).skip()
                .apply(syntax);
    }
}
