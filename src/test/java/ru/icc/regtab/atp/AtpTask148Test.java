package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

/**
 * Task 148: infrastructure objects table with region blocks.
 * Row-level COL-&gt;AVP inherited. First data cell is REC anchor collecting
 * ROW* and ST (location). Remaining cells must be non-blank.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_148/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask148Test}
 * <pre>
 *   [ [ATTR=UC]+ ]
 * { [ [VAL : 'LOCATION'-&gt;AVP] []+ ]
 *   [ COL-&gt;AVP [VAL : (ROW*,ST)-&gt;REC] [!BLANK ? VAL]+ ]+ }+
 * </pre>
 */
class AtpTask148Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK =
            new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);

    @Override
    protected String taskId() { return "148"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec colAvp = ActionSpec.avp(ProviderSpec.attr(ItemFilterConditionSpec.sameCol()))
                .asInherited();

        ActionSpec rec = ActionSpec.rec(
                ProviderSpec.val(ProviderSpec.UNBOUNDED, ItemFilterConditionSpec.sameRow()),
                ProviderSpec.val(1, ItemFilterConditionSpec.sameSubtable()));

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
                                CellPattern.of(NOT_BLANK, Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(colAvp))
                        )
                )
        );
    }
}
