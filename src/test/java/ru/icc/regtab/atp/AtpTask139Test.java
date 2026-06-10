package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;

/**
 * Task 139: tourist routes visitor table with cluster fill and conditional REC.
 * Row-level COL-&gt;AVP inherited. First column may be blank (fill from above).
 * Third column: blank rows skipped, non-blank rows create REC via ROW*.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_139/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask139Test}
 * <pre>
 * [ [ATTR=UC]+ ]
 * [ COL-&gt;AVP [BLANK ? VAL : -AV&amp;!BLANK-&gt;FILL | VAL] [VAL]
 *   [BLANK ? _ | VAL : ROW*-&gt;REC] ]+
 * </pre>
 */
class AtpTask139Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK =
            new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    @Override
    protected String taskId() { return "139"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec colAvp = ActionSpec.avp(ProviderSpec.attr(ItemFilterConditionSpec.sameCol()))
                .asInherited();

        ItemFilterConditionSpec avNotBlank = ItemFilterConditionSpec.and(
                FilterTerm.Above.INSTANCE, FilterTerm.NotBlank.INSTANCE);
        ActionSpec fill = ActionSpec.fill("",
                ProviderSpec.any(1, TraversalOrder.REVERSE_ROW_MAJOR, avNotBlank));

        ActionSpec rec = ActionSpec.rec(
                ProviderSpec.val(ProviderSpec.UNBOUNDED, ItemFilterConditionSpec.sameRow()));

        ContentSpec fillOrVal = new ConditionalContentSpec(BLANK,
                AtomicContentSpec.val(colAvp, fill),
                AtomicContentSpec.val(colAvp));

        ContentSpec skipOrRec = new ConditionalContentSpec(BLANK,
                AtomicContentSpec.skip(),
                AtomicContentSpec.val(colAvp, rec));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(CellPattern.of(Quantifier.oneOrMore(),
                                AtomicContentSpec.attr(StringExtractor.UpperCase.INSTANCE))),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(fillOrVal),
                                CellPattern.of(AtomicContentSpec.val(colAvp)),
                                CellPattern.of(skipOrRec)
                        )
                )
        );
    }
}
