package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;

/**
 * Task 141: SEZ characteristics table — transposed layout (zones in columns,
 * attributes in rows). Zone header row: first cell skipped, remaining cells get
 * 'ZONE'-&gt;AVP and COL*-&gt;REC. Attribute rows: first cell ATTR=UC, remaining cells
 * get ROW-&gt;AVP (attribute name from same-row first cell).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_141/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask141Test}
 * <pre>
 * [ [] [VAL : 'ZONE'-&gt;AVP, COL*-&gt;REC]+ ]
 * [ [ATTR=UC] [VAL : ROW-&gt;AVP]+ ]+
 * </pre>
 */
class AtpTask141Test extends AtpTaskBase {

    @Override
    protected String taskId() { return "141"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec recCol = ActionSpec.rec(
                ProviderSpec.val(ProviderSpec.UNBOUNDED, ItemFilterConditionSpec.sameCol()));

        ActionSpec rowAvp = ActionSpec.avp(ProviderSpec.attr(ItemFilterConditionSpec.sameRow()));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.oneOrMore(),
                                        AtomicContentSpec.val(ActionSpec.avp("ZONE"), recCol))
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.attr(StringExtractor.UpperCase.INSTANCE)),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(rowAvp))
                        )
                )
        );
    }
}
