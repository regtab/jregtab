package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellMatchCondition;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;
import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;

/**
 * ATP equivalent of Fluent API Task14.
 */
class AtpTask14Test extends AtpTaskBase {

    private static final ItemFilterCondition SAME_SUBTABLE_COL0 = (a, c) -> c.sameSubtable(a) && c.col(0);
    private static final ItemFilterCondition SAME_SUBTABLE_COL1 = (a, c) -> c.sameSubtable(a) && c.col(1);
    private static final ItemFilterCondition SAME_SUBROW        = (a, c) -> c.sameSubrow(a);

    private static final CellMatchCondition BLANK     = new CellMatchCondition(c -> c.textBlank());
    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(c -> !c.textBlank());

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
                                        ActionSpec.rec(ProviderSpec.of(1, SAME_SUBTABLE_COL0),
                                                       ProviderSpec.of(1, SAME_SUBTABLE_COL1),
                                                       ProviderSpec.of(2, SAME_SUBROW))
                                ))
                        )
                )
        ).withTransformations(new AnchorAttributeAtPosition(4));
    }
}
