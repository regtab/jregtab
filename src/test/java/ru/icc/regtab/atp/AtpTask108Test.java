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
import ru.icc.regtab.atp.spec.SubrowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

/**
 * Task 108: repeating "A B C" column groups separated by blank columns and blank rows.
 * Header row creates ATTR items for COL->AVP; data subrows group each A-B-C triple,
 * with the middle cell as REC anchor gathering the right cell via RT->REC.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_108/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask108Test}
 * <pre>
 *   [ [ATTR]+ ]
 * { COL-&gt;AVP
 *   [ { [!BLANK] [!BLANK? VAL: RT-&gt;REC] [!BLANK? VAL] [BLANK]* }+ ]
 *   [ [BLANK]+ ]*
 * }+
 * </pre>
 */
class AtpTask108Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
    private static final CellMatchCondition BLANK     = new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    private static final ItemFilterConditionSpec SAME_COL = ItemFilterConditionSpec.sameCol();
    private static final ItemFilterConditionSpec RIGHT_OF = ItemFilterConditionSpec.rightOf();

    @Override
    protected String taskId() { return "108"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec colAvp = ActionSpec.avp(ProviderSpec.attr(SAME_COL));
        ActionSpec rtRec  = ActionSpec.rec(ProviderSpec.val(RIGHT_OF));

        return TablePattern.of(
                // implicit subtable: [ [ATTR]+ ]
                SubtablePattern.of(
                        RowPattern.of(CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.attr()))
                ),
                // explicit subtable+: { COL->AVP ... }+
                SubtablePattern.of(Quantifier.oneOrMore(),
                        // [ { [!BLANK] [!BLANK? VAL: RT->REC] [!BLANK? VAL] [BLANK]* }+ ]
                        RowPattern.of(Quantifier.one(),
                                SubrowPattern.of(Quantifier.oneOrMore(),
                                        CellPattern.of(NOT_BLANK, Quantifier.one(), null),
                                        CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(colAvp, rtRec)),
                                        CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(colAvp)),
                                        CellPattern.of(BLANK, Quantifier.zeroOrMore(), null)
                                )
                        ),
                        // [ [BLANK]+ ]*
                        RowPattern.of(Quantifier.zeroOrMore(),
                                CellPattern.of(BLANK, Quantifier.oneOrMore(), null)
                        )
                )
        );
    }
}
