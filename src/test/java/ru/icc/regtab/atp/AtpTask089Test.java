package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellMatchCondition;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CellPredicate;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;


/**
 * Task 89: two ATTR header rows with VAL data rows; first section uses an explicit
 * repeating subtable (VAL row + BLANK row), second section has consecutive VAL rows.
 * COL-&gt;AVP assigns column names; RT*-&gt;REC collects the full row into one record.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_089/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask089Test}
 */
class AtpTask089Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK_COND =
            new CellMatchCondition(CellPredicate.Blank.INSTANCE);
    private static final ItemFilterConditionSpec RIGHT_OF = ItemFilterConditionSpec.rightOf();
    private static final ItemFilterConditionSpec SAME_COL = ItemFilterConditionSpec.sameCol();

    @Override
    protected String taskId() { return "089"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec colAvp = ActionSpec.avp(ProviderSpec.attr(SAME_COL));
        ActionSpec rec    = ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, RIGHT_OF));

        CellPattern attrPlus   = CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.attr());
        CellPattern valAnchor  = CellPattern.of(AtomicContentSpec.val(rec, colAvp));
        CellPattern valContPlus = CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(colAvp));
        CellPattern blankPlus  = new CellPattern(BLANK_COND, Quantifier.oneOrMore(), null);

        return TablePattern.of(
                // implicit subtable 1: ATTR header row
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(attrPlus)
                ),
                // explicit subtable: {VAL row + BLANK row}+
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(valAnchor, valContPlus),
                        RowPattern.of(blankPlus)
                ),
                // implicit subtable 2: ATTR header row + VAL rows
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(attrPlus),
                        RowPattern.of(Quantifier.oneOrMore(), valAnchor, valContPlus)
                )
        );
    }
}
