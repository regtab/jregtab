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
 * Task 69: single subtable with SR->AVP — first row anchors BW*->REC and joins
 * same-row tagged items; second optional rows contain plain ATTR+VAL{2} subrows.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_69/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask69Test}
 */
class AtpTask69Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_SUBROW = ItemFilterConditionSpec.sameSubrow();
    private static final ItemFilterConditionSpec BELOW       = ItemFilterConditionSpec.below();

    private static final ItemFilterConditionSpec ROW_TAG1 = ItemFilterConditionSpec.and(
            FilterTerm.SameRow.INSTANCE, new FilterTerm.Tagged("#1"));
    private static final ItemFilterConditionSpec ROW_TAG2 = ItemFilterConditionSpec.and(
            FilterTerm.SameRow.INSTANCE, new FilterTerm.Tagged("#2"));

    @Override
    protected String taskId() { return "69"; }

    @Override
    protected TablePattern buildPattern() {
        ActionSpec avpSR = ActionSpec.avp(ProviderSpec.attr(SAME_SUBROW));
        ActionSpec recBW = ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, BELOW));

        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.one(),
                                SubrowPattern.of(Quantifier.zeroOrMore(),
                                        CellPattern.of(AtomicContentSpec.attr()),
                                        CellPattern.of(AtomicContentSpec.valTagged("#1",
                                                avpSR, recBW,
                                                ActionSpec.join(ProviderSpec.val(ProviderSpec.UNBOUNDED, ROW_TAG1))
                                        )),
                                        CellPattern.of(AtomicContentSpec.valTagged("#2",
                                                avpSR, recBW,
                                                ActionSpec.join(ProviderSpec.val(ProviderSpec.UNBOUNDED, ROW_TAG2))
                                        ))
                                )
                        ),
                        RowPattern.of(Quantifier.zeroOrMore(),
                                SubrowPattern.of(Quantifier.zeroOrMore(),
                                        CellPattern.of(AtomicContentSpec.attr()),
                                        CellPattern.of(Quantifier.exactly(2), AtomicContentSpec.val(avpSR))
                                )
                        )
                )
        );
    }
}
