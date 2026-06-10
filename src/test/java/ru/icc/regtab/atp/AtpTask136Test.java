package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;

/**
 * Task 136: tourist-recreational areas table with location blocks and fill.
 * Row-level COL-&gt;AVP inherited. First 2 cols may be blank (fill). REC on 4th data
 * cell: ST (location) and ROW* (remaining row cells). Trailing 4 cells plain VAL.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_136/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask136Test}
 * <pre>
 * [ [ATTR=UC]+ ]
 * { [ [VAL : 'LOCATION'-&gt;AVP] []+ ]
 *   [ COL-&gt;AVP [BLANK ? VAL : -AV&amp;!BLANK-&gt;FILL | VAL]{2} [!BLANK ? VAL]
 *     [VAL : (ST,ROW*)-&gt;REC] [VAL]{4} ]+ }+
 * </pre>
 */
class AtpTask136Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK =
            new CellMatchCondition(CellPredicate.Blank.INSTANCE);
    private static final CellMatchCondition NOT_BLANK =
            new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);

    @Override
    protected String taskId() { return "136"; }

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
                                CellPattern.of(Quantifier.exactly(2), fillOrVal),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(colAvp)),
                                CellPattern.of(AtomicContentSpec.val(colAvp, rec)),
                                CellPattern.of(Quantifier.exactly(4), AtomicContentSpec.val(colAvp))
                        )
                )
        );
    }
}
