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
 * Task 93: repeating subtables, each with a grouped ATTR header row (groups separated by
 * required blank) and one-or-more grouped data rows; SC-&gt;AVP links each VAL to its header
 * ATTR in the same subcol, RT*-&gt;REC on the anchor collects sibling VALs to the right.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_93/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask93Test}
 * <pre>
 * { [ { [!BLANK? ATTR]+ [BLANK?] }+ ]
 *   [ { SC-&gt;AVP [!BLANK? VAL: RT*-&gt;REC] [!BLANK? VAL]+ [BLANK?] }+ ]+
 *   [ [BLANK?]+ ]? }+
 * </pre>
 */
class AtpTask93Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
    private static final CellMatchCondition BLANK     = new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    private static final ItemFilterConditionSpec RIGHT_OF = ItemFilterConditionSpec.rightOf();
    private static final ItemFilterConditionSpec SAME_SC  = ItemFilterConditionSpec.sameSubcol();

    @Override
    protected String taskId() { return "93"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec scAvp = ActionSpec.avp(ProviderSpec.attr(SAME_SC));
        ActionSpec rtRec = ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, RIGHT_OF));

        CellPattern notBlankAttr  = CellPattern.of(NOT_BLANK, Quantifier.oneOrMore(), AtomicContentSpec.attr());
        CellPattern optionalBlank = CellPattern.of(BLANK, Quantifier.zeroOrOne(), null);
        CellPattern anchorVal     = CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(scAvp, rtRec));
        CellPattern otherVals     = CellPattern.of(NOT_BLANK, Quantifier.oneOrMore(), AtomicContentSpec.val(scAvp));
        CellPattern manyBlanks    = CellPattern.of(BLANK, Quantifier.oneOrMore(), null);

        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(Quantifier.one(),
                                SubrowPattern.of(Quantifier.oneOrMore(), notBlankAttr, optionalBlank)),
                        RowPattern.of(Quantifier.oneOrMore(),
                                SubrowPattern.of(Quantifier.oneOrMore(), anchorVal, otherVals, optionalBlank)),
                        RowPattern.of(Quantifier.zeroOrOne(), manyBlanks)
                )
        );
    }
}
