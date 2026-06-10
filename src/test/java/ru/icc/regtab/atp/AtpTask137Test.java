package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;

/**
 * Task 137: protected areas table with ecological zone blocks and fill.
 * Row-level COL-&gt;AVP inherited. REC anchor on first data cell: ST + ROW*.
 * Third column may be blank (fill from above). Last 2 columns guarded !BLANK.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_137/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask137Test}
 * <pre>
 * [ [ATTR=UC]+ ]
 * { [ [VAL : 'LOCATION'-&gt;AVP] []+ ]
 *   [ COL-&gt;AVP [VAL : (ST,ROW*)-&gt;REC] [VAL]
 *     [BLANK ? VAL : -AV&amp;!BLANK-&gt;FILL | VAL] [!BLANK ? VAL]{2} ]+ }+
 * </pre>
 */
class AtpTask137Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK =
            new CellMatchCondition(CellPredicate.Blank.INSTANCE);
    private static final CellMatchCondition NOT_BLANK =
            new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);

    @Override
    protected String taskId() { return "137"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec colAvp = ActionSpec.avp(ProviderSpec.attr(ItemFilterConditionSpec.sameCol()))
                .asInherited();

        ItemFilterConditionSpec avNotBlank = ItemFilterConditionSpec.and(
                FilterTerm.Above.INSTANCE, FilterTerm.NotBlank.INSTANCE);
        ActionSpec fill = ActionSpec.fill("",
                ProviderSpec.any(1, TraversalOrder.REVERSE_ROW_MAJOR, avNotBlank));

        ActionSpec rec = ActionSpec.rec(
                ProviderSpec.val(1, ItemFilterConditionSpec.sameSubtable()),
                ProviderSpec.val(ProviderSpec.UNBOUNDED, ItemFilterConditionSpec.sameRow()));

        ContentSpec fillOrVal = new ConditionalContentSpec(BLANK,
                AtomicContentSpec.val(colAvp, fill),
                AtomicContentSpec.val(colAvp));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(CellPattern.of(Quantifier.oneOrMore(),
                                AtomicContentSpec.attr(StringExtractor.UpperCase.INSTANCE)))
                ),
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("LOCATION"))),
                                CellPattern.skip(Quantifier.oneOrMore())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(colAvp, rec)),
                                CellPattern.of(AtomicContentSpec.val(colAvp)),
                                CellPattern.of(fillOrVal),
                                CellPattern.of(NOT_BLANK, Quantifier.exactly(2), AtomicContentSpec.val(colAvp))
                        )
                )
        );
    }
}
