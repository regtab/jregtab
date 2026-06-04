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
 * Task 26: repeated subtables with a three-cell header row (anchor with AVP and
 * column-2 REC) and exactly 5 data rows pairing ATTR with same-row AVP.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_026/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask026Test}
 */
class AtpTask026Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_SUBTABLE_COL2 = ItemFilterConditionSpec.and(FilterTerm.SameSubtable.INSTANCE, new FilterTerm.ColExact(2));
    private static final ItemFilterConditionSpec SAME_SUBROW        = ItemFilterConditionSpec.sameSubrow();

    @Override
    protected String taskId() {
        return "026";
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
                                        ActionSpec.avp(ProviderSpec.attr(SAME_SUBROW))
                                ))
                        ),
                        RowPattern.of(Quantifier.exactly(5),
                                CellPattern.skip(),
                                CellPattern.of(AtomicContentSpec.attr()),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.avp(ProviderSpec.attr(SAME_SUBROW))
                                ))
                        )
                )
        );
    }
}
