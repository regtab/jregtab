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

import java.util.Set;

/**
 * Task 23: repeated subtables of exactly 3 rows, each combining AVP, REC, JOIN(0),
 * and SUFFIX actions across same-row and below-same-string providers.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_23/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask23Test}
 */
class AtpTask23Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_SUBROW = ItemFilterConditionSpec.sameSubrow();
    private static final ItemFilterConditionSpec RIGHT_OF    = ItemFilterConditionSpec.rightOf();
    private static final ItemFilterConditionSpec BELOW_STR   = ItemFilterConditionSpec.and(FilterTerm.Below.INSTANCE, FilterTerm.SameStr.INSTANCE);

    @Override
    protected String taskId() {
        return "23";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(Quantifier.exactly(3),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.avp(""),
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_SUBROW)),
                                        ActionSpec.join(0, ProviderSpec.val(ProviderSpec.UNBOUNDED, BELOW_STR))
                                )),
                                CellPattern.of(AtomicContentSpec.attr(
                                        ActionSpec.suffix("", ProviderSpec.any(1, TraversalOrder.ROW_MAJOR, RIGHT_OF))
                                )),
                                CellPattern.of(AtomicContentSpec.aux()),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.avp(ProviderSpec.attr(SAME_SUBROW))
                                ))
                        )
                )
        );
    }
}
