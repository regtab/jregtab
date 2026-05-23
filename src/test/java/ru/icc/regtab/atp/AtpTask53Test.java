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
 * Task 53: two-row group table with compound attribute names (group header + qualifier).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_53/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask53Test}
 */
class AtpTask53Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_ROW   = ItemFilterConditionSpec.sameRow();
    private static final ItemFilterConditionSpec BELOW_STR  = ItemFilterConditionSpec.and(FilterTerm.Below.INSTANCE, FilterTerm.SameStr.INSTANCE);
    private static final ItemFilterConditionSpec ABOVE       = ItemFilterConditionSpec.above();
    private static final ItemFilterConditionSpec SAME_SUBROW = ItemFilterConditionSpec.sameSubrow();

    @Override
    protected String taskId() { return "53"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.aux())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                SubrowPattern.of(
                                        CellPattern.of(AtomicContentSpec.val(
                                                ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_ROW)),
                                                ActionSpec.concat(ProviderSpec.val(1, BELOW_STR)),
                                                ActionSpec.avp("ID")
                                        ))
                                ),
                                SubrowPattern.of(Quantifier.oneOrMore(),
                                        CellPattern.of(AtomicContentSpec.attr(
                                                ActionSpec.prefix("_", ProviderSpec.any(1, ABOVE))
                                        )),
                                        CellPattern.of(AtomicContentSpec.val(
                                                ActionSpec.avp(ProviderSpec.attr(SAME_SUBROW))
                                        ))
                                )
                        )
                )
        );
    }
}
