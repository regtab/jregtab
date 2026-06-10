package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.*;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;

/**
 * Task 142: SEZ TRT residents table with fill on the first column.
 * Row-level COL-&gt;AVP inherited. First column may be blank: both branches create
 * REC via ROW*; blank branch also fills from nearest non-blank above.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_142/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask142Test}
 * <pre>
 * [ [ATTR=UC]+ ]
 * [ COL-&gt;AVP [BLANK ? VAL : -AV&amp;!BLANK-&gt;FILL, ROW*-&gt;REC | VAL : ROW*-&gt;REC] [VAL]+ ]+
 * </pre>
 */
class AtpTask142Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK =
            new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    @Override
    protected String taskId() { return "142"; }

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

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(CellPattern.of(Quantifier.oneOrMore(),
                                AtomicContentSpec.attr(StringExtractor.UpperCase.INSTANCE))),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(firstCell),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(colAvp))
                        )
                )
        );
    }
}
