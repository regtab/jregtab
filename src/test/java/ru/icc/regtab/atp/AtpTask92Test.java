package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellMatchCondition;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CellPredicate;
import ru.icc.regtab.atp.spec.FilterTerm;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubrowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

/**
 * Task 92: rows with one label VAL followed by grouped value VALs separated by blank cells;
 * REC anchor collects all same-row VALs to the right (ROW &amp; C+1..) and the label VAL to
 * the left (LT).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_92/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask92Test}
 * <pre>
 * [ [VAL] [VAL: ((ROW &amp; C+1..)*, LT)-&gt;REC ] { [!BLANK? VAL]+ [BLANK?]? }+ ]+
 * </pre>
 */
class AtpTask92Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
    private static final CellMatchCondition BLANK     = new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    @Override
    protected String taskId() { return "92"; }

    @Override
    protected TablePattern buildPattern() {
        // (ROW & C+1..)* — all VAL items in same row strictly to the right of anchor
        ItemFilterConditionSpec rowAndColRight = ItemFilterConditionSpec.and(
                FilterTerm.SameRow.INSTANCE,
                new FilterTerm.ColRange(1, Integer.MAX_VALUE)
        );
        ProviderSpec rowRight = ProviderSpec.val(ProviderSpec.UNBOUNDED, rowAndColRight);

        // LT — VAL item to the left of anchor (same subrow, col < anchor.col)
        ProviderSpec leftOf = ProviderSpec.val(ItemFilterConditionSpec.leftOf());

        ActionSpec rec = ActionSpec.rec(rowRight, leftOf);

        CellPattern plainVal    = new CellPattern(null,      Quantifier.one(),       AtomicContentSpec.val());
        CellPattern recVal      = new CellPattern(null,      Quantifier.one(),       AtomicContentSpec.val(rec));
        CellPattern notBlankVal = new CellPattern(NOT_BLANK, Quantifier.oneOrMore(), AtomicContentSpec.val());
        CellPattern blankCell   = new CellPattern(BLANK,     Quantifier.zeroOrOne(), null);

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                SubrowPattern.of(plainVal, recVal),
                                SubrowPattern.of(Quantifier.oneOrMore(), notBlankVal, blankCell)
                        )
                )
        );
    }
}
