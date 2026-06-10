package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

/**
 * Task 133: tourism statistics table with location blocks and year columns.
 * REC on DATA: ROW (INDICATOR), COL (YEAR), ST (LOCATION).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_133/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask133Test}
 * <pre>
 *   [ [] [VAL : 'YEAR'-&gt;AVP]+ ]
 * { [ [VAL : 'LOCATION'-&gt;AVP] []+ ]
 *   [ [VAL : 'INDICATOR'-&gt;AVP] [!BLANK ? VAL : 'DATA'-&gt;AVP, (ROW,COL,ST)-&gt;REC]+ ]+ }+
 * </pre>
 */
class AtpTask133Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK =
            new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);

    @Override
    protected String taskId() { return "133"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec recData = ActionSpec.rec(
                ProviderSpec.val(1, ItemFilterConditionSpec.sameRow()),
                ProviderSpec.val(1, ItemFilterConditionSpec.sameCol()),
                ProviderSpec.val(1, ItemFilterConditionSpec.sameSubtable()));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(ActionSpec.avp("YEAR")))
                        )
                ),
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("LOCATION"))),
                                CellPattern.skip(Quantifier.oneOrMore())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("INDICATOR"))),
                                CellPattern.of(NOT_BLANK, Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(ActionSpec.avp("DATA"), recData))
                        )
                )
        );
    }
}
