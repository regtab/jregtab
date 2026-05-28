package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;


/**
 * Task 80: header VAL row; data rows with COL->REC(1) — inline anchor at position 1.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_80/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask80Test}
 */
class AtpTask80Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_COL = ItemFilterConditionSpec.sameCol();

    @Override
    protected String taskId() { return "80"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(
                                        ActionSpec.rec(1, ProviderSpec.val(SAME_COL))
                                ))
                        )
                )
        );
    }
}
