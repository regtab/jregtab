package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;

/**
 * Task 20: flat table with a two-cell header row (anchor collecting all
 * same-subtable values via REC) and one-or-more two-cell data rows.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_20/}
 * RTL: {@link ru.icc.regtab.itm.rtl.RtlTask20Test}
 */
class AtpTask20Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_SUBTABLE = ItemFilterConditionSpec.sameSubtable();

    @Override
    protected String taskId() {
        return "20";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_SUBTABLE))
                                )),
                                CellPattern.of(AtomicContentSpec.val())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(Quantifier.exactly(2), AtomicContentSpec.val())
                        )
                )
        );
    }
}
