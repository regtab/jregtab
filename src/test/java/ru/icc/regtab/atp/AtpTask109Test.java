package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.FilterTerm;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubrowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

/**
 * Task 109: each row has two label anchors followed by ODD/EVEN data pairs.
 * Anchor 1 collects all same-row #ODD items; anchor 2 collects all #EVEN items.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_109/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask109Test}
 * <pre>
 * [ [VAL: ROW&amp;#'ODD'*-&gt;REC] [VAL: ROW&amp;#'EVEN'*-&gt;REC] { [VAL#'ODD'] [VAL#'EVEN'] }+ ]+
 * </pre>
 */
class AtpTask109Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec ROW_ODD  = ItemFilterConditionSpec.and(
            FilterTerm.SameRow.INSTANCE, new FilterTerm.Tagged("#ODD"));
    private static final ItemFilterConditionSpec ROW_EVEN = ItemFilterConditionSpec.and(
            FilterTerm.SameRow.INSTANCE, new FilterTerm.Tagged("#EVEN"));

    @Override
    protected String taskId() { return "109"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec recOdd  = ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, ROW_ODD));
        ActionSpec recEven = ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, ROW_EVEN));

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                SubrowPattern.of(
                                        CellPattern.of(AtomicContentSpec.val(recOdd)),
                                        CellPattern.of(AtomicContentSpec.val(recEven))
                                ),
                                SubrowPattern.of(Quantifier.oneOrMore(),
                                        CellPattern.of(AtomicContentSpec.valTagged("#ODD")),
                                        CellPattern.of(AtomicContentSpec.valTagged("#EVEN"))
                                )
                        )
                )
        );
    }
}
