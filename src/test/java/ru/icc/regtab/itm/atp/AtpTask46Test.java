package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellMatchCondition;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.CellPredicate;
import ru.icc.regtab.itm.atp.spec.FilterTerm;
import ru.icc.regtab.itm.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;

/**
 * Task 46: repeated subtables with one-or-more non-blank three-cell rows —
 * anchor VAL (AVP + same-row REC + below-same-string CONCAT), ATTR, and AVP VAL.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_46/}
 * RTL: {@link ru.icc.regtab.itm.rtl.RtlTask46Test}
 */
class AtpTask46Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);

    private static final ItemFilterConditionSpec SAME_SUBROW = ItemFilterConditionSpec.sameSubrow();
    private static final ItemFilterConditionSpec BELOW_STR   = ItemFilterConditionSpec.and(FilterTerm.Below.INSTANCE, FilterTerm.SameStr.INSTANCE);

    @Override
    protected String taskId() {
        return "46";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(
                                        ActionSpec.avp(""),
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_SUBROW)),
                                        ActionSpec.concat(ProviderSpec.val(ProviderSpec.UNBOUNDED, BELOW_STR))
                                )),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.attr()),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(
                                        ActionSpec.avp(ProviderSpec.attr(SAME_SUBROW))
                                ))
                        )
                )
        );
    }
}
