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
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;

/**
 * Task 91: repeating inline ATTR/VAL rows; -LT-&gt;AVP (inherited) maps each VAL to its
 * nearest left ATTR; ROW*-&gt;REC on the anchor VAL collects all same-row VAL items.
 * Blank separator rows are consumed by an optional trailing row inside a repeating
 * explicit subtable.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_091/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask091Test}
 */
class AtpTask091Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
    private static final CellMatchCondition BLANK     = new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    private static final ItemFilterConditionSpec LEFT_OF  = ItemFilterConditionSpec.leftOf();
    private static final ItemFilterConditionSpec SAME_ROW = ItemFilterConditionSpec.sameRow();

    @Override
    protected String taskId() { return "091"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec ltAvp  = ActionSpec.avp(ProviderSpec.attr(TraversalOrder.REVERSE_ROW_MAJOR, LEFT_OF));
        ActionSpec rowRec = ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_ROW));

        CellPattern notBlankAttr      = new CellPattern(NOT_BLANK, Quantifier.one(), AtomicContentSpec.attr());
        CellPattern notBlankValAnchor = new CellPattern(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(rowRec, ltAvp));
        CellPattern notBlankVal       = new CellPattern(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(ltAvp));
        CellPattern blankCell         = new CellPattern(BLANK, Quantifier.oneOrMore(), null);

        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                SubrowPattern.of(notBlankAttr, notBlankValAnchor),
                                SubrowPattern.of(Quantifier.oneOrMore(), notBlankAttr, notBlankVal)
                        ),
                        RowPattern.of(Quantifier.zeroOrOne(), blankCell)
                )
        );
    }
}
