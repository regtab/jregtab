package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;

/**
 * Task 143: tourist-recreational cluster table with region blocks and fill.
 * Row-level COL-&gt;AVP inherited. First column may be blank (fill + REC or plain REC).
 * Second column may be blank (fill only). Last 2 columns guarded !BLANK.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_143/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask143Test}
 * <pre>
 *   [ [ATTR=UC]+ ]
 * { [ [VAL#'LOC' : 'LOCATION'-&gt;AVP] []+ ]
 *   [ COL-&gt;AVP [BLANK ? VAL : -AV&amp;!BLANK-&gt;FILL, ROW*-&gt;REC | VAL : ROW*-&gt;REC]
 *     [BLANK ? VAL : -AV&amp;!BLANK-&gt;FILL | VAL] [!BLANK ? VAL]{2} ]+ }+
 * </pre>
 */
class AtpTask143Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK =
            new CellMatchCondition(CellPredicate.Blank.INSTANCE);
    private static final CellMatchCondition NOT_BLANK =
            new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);

    @Override
    protected String taskId() { return "143"; }

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

        ContentSpec firstCell = new ConditionalContentSpec(BLANK,
                AtomicContentSpec.val(colAvp, fill, rec),
                AtomicContentSpec.val(colAvp, rec));

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
                                CellPattern.of(AtomicContentSpec.valTagged("#LOC",
                                        ActionSpec.avp("LOCATION"))),
                                CellPattern.skip(Quantifier.oneOrMore())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(firstCell),
                                CellPattern.of(fillOrVal),
                                CellPattern.of(NOT_BLANK, Quantifier.exactly(2),
                                        AtomicContentSpec.val(colAvp))
                        )
                )
        );
    }
}
