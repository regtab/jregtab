package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;

/**
 * Task 135: KSR capacity table with hierarchical location columns and fill.
 * Row-level COL-&gt;AVP inherited by all data cells. First 3 columns may be blank
 * (hierarchical fill from nearest non-blank above). REC on last cell: -LT{2} (2 cols
 * to the left) and ROW{4} (4 row-header cells).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_135/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask135Test}
 * <pre>
 * [ [ATTR=UC]+ ]
 * [ COL-&gt;AVP [BLANK ? VAL : -AV&amp;!BLANK-&gt;FILL | VAL]{3} [VAL]{3}
 *   [VAL : (-LT{2},ROW{4})-&gt;REC] ]+
 * </pre>
 */
class AtpTask135Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK =
            new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    @Override
    protected String taskId() { return "135"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec colAvp = ActionSpec.avp(ProviderSpec.attr(ItemFilterConditionSpec.sameCol()))
                .asInherited();

        ItemFilterConditionSpec avNotBlank = ItemFilterConditionSpec.and(
                FilterTerm.Above.INSTANCE, FilterTerm.NotBlank.INSTANCE);
        ActionSpec fill = ActionSpec.fill("",
                ProviderSpec.any(1, TraversalOrder.REVERSE_ROW_MAJOR, avNotBlank));

        ActionSpec rec = ActionSpec.rec(
                ProviderSpec.val(2, TraversalOrder.REVERSE_ROW_MAJOR, ItemFilterConditionSpec.leftOf()),
                ProviderSpec.val(4, ItemFilterConditionSpec.sameRow()));

        ContentSpec fillOrVal = new ConditionalContentSpec(BLANK,
                AtomicContentSpec.val(colAvp, fill),
                AtomicContentSpec.val(colAvp));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(CellPattern.of(Quantifier.oneOrMore(),
                                AtomicContentSpec.attr(StringExtractor.UpperCase.INSTANCE))),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(Quantifier.exactly(3), fillOrVal),
                                CellPattern.of(Quantifier.exactly(3), AtomicContentSpec.val(colAvp)),
                                CellPattern.of(AtomicContentSpec.val(colAvp, rec))
                        )
                )
        );
    }
}
