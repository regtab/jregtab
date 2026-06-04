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

/**
 * Task 12: single-row header where the anchor collects all values in column 5
 * via unbounded REC, followed by data rows that only fill column 5.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_012/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask012Test}
 */
class AtpTask012Test extends AtpTaskBase {

    @Override
    protected String taskId() {
        return "012";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, ItemFilterConditionSpec.bare(new FilterTerm.ColExact(5))))
                                )),
                                CellPattern.skip(Quantifier.exactly(4)),
                                CellPattern.of(AtomicContentSpec.val())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.skip(Quantifier.exactly(5)),
                                CellPattern.of(AtomicContentSpec.val())
                        )
                )
        );
    }
}
