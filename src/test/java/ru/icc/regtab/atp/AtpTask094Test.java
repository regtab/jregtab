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
 * Task 94: single header row (groups separated by optional blank) above one-or-more
 * blank-separated data blocks; COL*-&gt;REC collects all same-column VALs regardless of
 * subtable boundaries, (ROW &amp; C+1.. &amp; STR)*-&gt;JOIN(0) merges sibling header columns
 * into one record.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_094/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask094Test}
 * <pre>
 * [ { [!BLANK? VAL: COL*-&gt;REC, (ROW &amp; C+1.. &amp; STR)*-&gt;JOIN(0)]+ [BLANK?]? }+ ]
 * { [ { [!BLANK? VAL]+ [BLANK?]? }+ ]+
 *   [ [BLANK?]+ ]? }+
 * </pre>
 */
class AtpTask094Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
    private static final CellMatchCondition BLANK     = new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    @Override
    protected String taskId() { return "094"; }

    @Override
    protected TablePattern buildPattern() {
        ItemFilterConditionSpec sameCol = ItemFilterConditionSpec.sameCol();
        ItemFilterConditionSpec rowColRightStr = ItemFilterConditionSpec.and(
                FilterTerm.SameRow.INSTANCE,
                new FilterTerm.ColRange(1, Integer.MAX_VALUE),
                FilterTerm.SameStr.INSTANCE
        );

        ActionSpec colRec  = ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, sameCol));
        ActionSpec rowJoin = ActionSpec.join(0, ProviderSpec.val(ProviderSpec.UNBOUNDED, rowColRightStr));

        CellPattern headerCell = CellPattern.of(NOT_BLANK, Quantifier.oneOrMore(),
                AtomicContentSpec.val(colRec, rowJoin));
        CellPattern optBlank   = CellPattern.of(BLANK, Quantifier.zeroOrOne(), null);
        CellPattern dataCell   = CellPattern.of(NOT_BLANK, Quantifier.oneOrMore(), AtomicContentSpec.val());
        CellPattern manyBlanks = CellPattern.of(BLANK, Quantifier.oneOrMore(), null);

        SubrowPattern headerSubrow = SubrowPattern.of(Quantifier.oneOrMore(), headerCell, optBlank);
        SubrowPattern dataSubrow   = SubrowPattern.of(Quantifier.oneOrMore(), dataCell, optBlank);

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.one(), headerSubrow)
                ),
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(Quantifier.oneOrMore(), dataSubrow),
                        RowPattern.of(Quantifier.zeroOrOne(), manyBlanks)
                )
        );
    }
}
