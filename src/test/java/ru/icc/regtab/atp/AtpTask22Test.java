package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.FilterTerm;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;
import ru.icc.regtab.itm.semantics.provider.TraversalOrder;

/**
 * Task 22: repeated subtables where the anchor collects values in columns 2–5
 * in column-major traversal order, plus a plain data row.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_22/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask22Test}
 */
class AtpTask22Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_SUBTABLE_COLS2_5 =
            ItemFilterConditionSpec.and(FilterTerm.SameSubtable.INSTANCE, new FilterTerm.ColAbsoluteRange(2,5));

    @Override
    protected String taskId() {
        return "22";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, TraversalOrder.COLUMN_MAJOR, SAME_SUBTABLE_COLS2_5))
                                )),
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                        ),
                        RowPattern.of(
                                CellPattern.skip(Quantifier.exactly(2)),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                        )
                )
        );
    }
}
