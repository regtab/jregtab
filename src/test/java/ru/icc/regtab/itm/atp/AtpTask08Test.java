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
 * Task 08: one skip row followed by data rows with a row-key anchor and
 * one-or-more value cells referencing it via same-subrow REC(1).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_08/}
 * RTL: {@link ru.icc.regtab.itm.rtl.RtlTask08Test}
 */
class AtpTask08Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_SUBROW = ItemFilterConditionSpec.sameSubrow();

    @Override
    protected String taskId() {
        return "08";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.skip(Quantifier.oneOrMore())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(
                                        ActionSpec.rec(1, ProviderSpec.val(1, SAME_SUBROW))
                                ))
                        )
                ));
    }
}
