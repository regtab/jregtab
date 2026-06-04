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
 * Task 36: repeated student-grade subtables — header row carries the student
 * name anchor, then exactly 11 subject/grade rows share the same left-attr AVP pattern.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_036/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask036Test}
 */
class AtpTask036Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_SUBTABLE_COL2 = ItemFilterConditionSpec.and(FilterTerm.SameSubtable.INSTANCE, new FilterTerm.ColExact(2));
    private static final ItemFilterConditionSpec LEFT_OF             = ItemFilterConditionSpec.leftOf();

    @Override
    protected String taskId() {
        return "036";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.avp(""),
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_SUBTABLE_COL2))
                                )),
                                CellPattern.of(AtomicContentSpec.attr()),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.avp(ProviderSpec.attr(LEFT_OF))
                                ))
                        ),
                        RowPattern.of(Quantifier.exactly(11),
                                CellPattern.skip(),
                                CellPattern.of(AtomicContentSpec.attr()),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.avp(ProviderSpec.attr(LEFT_OF))
                                ))
                        )
                )
        );
    }
}
