package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.pattern.ProviderSpec;
import ru.icc.regtab.pattern.TablePattern;

/**
 * Task 36 (Foofah exp0_49): pivot student blocks (12 rows × 3 cols: name / subject / grade) into wide rows;
 * column names come from subject {@code attr} cells via {@code O_avp}, not anonymous {@code $a_i} names.
 * <p>
 * First row of each block: name (val) + {@code rec} of all grades in column 2 in the subtable, {@code avp("")} for the
 * name column header; then subject {@code attr}, grade {@code val} with {@code avp} to the attribute left of the grade.
 * Next {@code exactly(11)} rows: skip empty name column, same subject/grade + {@code avp} pattern.
 */
public final class Task36 extends TaskBase {

    /** All grade cells (column 2) in the same subtable as the name anchor. */
    private static final ProviderSpec REC_GRADES_COL2 =
            ProviderSpec.val((a, c) -> c.col(2) && c.sameSubtable(a));

    /** Subject {@code attr} cell immediately to the left of the grade (see {@code O_avp} predicate κ). */
    private static final ItemFilterCondition SUBJECT_LEFT_OF_GRADE =
            (a, c) -> c.leftOf(a).sameRow();

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().one()
                .cells().one().val()
                .actions().avp("").rec(REC_GRADES_COL2)
                .cells().one().attr()
                .cells().one().val()
                .actions().avp(SUBJECT_LEFT_OF_GRADE)
                .rows().exactly(11)
                .cells().one().skip()
                .cells().one().attr()
                .cells().one().val()
                .actions().avp(SUBJECT_LEFT_OF_GRADE)
                .apply(syntax);
    }
}
