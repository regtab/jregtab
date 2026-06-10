package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

/**
 * Task 134: tourism load table with month and day-count header rows, location blocks.
 * REC on DATA: ROW (INDICATOR), COL{2} (MONTH + DAY), ST (LOCATION).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_134/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask134Test}
 * <pre>
 *   [ [] [VAL : 'MONTH'-&gt;AVP]+ ]
 *   [ [] [VAL : 'DAY'-&gt;AVP]+ ]
 * { [ [VAL : 'LOCATION'-&gt;AVP] []+ ]
 *   [ [VAL : 'INDICATOR'-&gt;AVP] [!BLANK ? VAL : 'DATA'-&gt;AVP, (ROW,COL{2},ST)-&gt;REC]+ ]+ }+
 * </pre>
 */
class AtpTask134Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK =
            new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);

    @Override
    protected String taskId() { return "134"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec recData = ActionSpec.rec(
                ProviderSpec.val(1, ItemFilterConditionSpec.sameRow()),
                ProviderSpec.val(2, ItemFilterConditionSpec.sameCol()),
                ProviderSpec.val(1, ItemFilterConditionSpec.sameSubtable()));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(ActionSpec.avp("MONTH")))
                        ),
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(ActionSpec.avp("DAY")))
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
