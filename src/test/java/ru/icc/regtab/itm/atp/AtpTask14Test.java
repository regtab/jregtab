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
import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;

/**
 * ATP equivalent of Fluent API Task14.
 */
class AtpTask14Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_SUBTABLE_COL0 = ItemFilterConditionSpec.and(FilterTerm.SameSubtable.INSTANCE, new FilterTerm.ColExact(0));
    private static final ItemFilterConditionSpec SAME_SUBTABLE_COL1 = ItemFilterConditionSpec.and(FilterTerm.SameSubtable.INSTANCE, new FilterTerm.ColExact(1));
    private static final ItemFilterConditionSpec SAME_SUBROW        = ItemFilterConditionSpec.sameSubrow();

    private static final CellMatchCondition BLANK     = new CellMatchCondition(CellPredicate.Blank.INSTANCE);
    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);

    @Override
    protected String taskId() {
        return "14";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(NOT_BLANK, Quantifier.exactly(2), AtomicContentSpec.val()),
                                CellPattern.of(BLANK, Quantifier.one(), null)
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_BLANK, Quantifier.exactly(2), AtomicContentSpec.val()),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(1, SAME_SUBTABLE_COL0),
                                                       ProviderSpec.val(1, SAME_SUBTABLE_COL1),
                                                       ProviderSpec.val(2, SAME_SUBROW))
                                ))
                        )
                )
        ).withTransformations(new AnchorAttributeAtPosition(4));
    }
}
