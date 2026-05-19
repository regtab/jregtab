package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellMatchCondition;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.ConditionalContentSpec;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;
import ru.icc.regtab.itm.model.semantics.provider.TraversalOrder;

/**
 * ATP equivalent of Fluent API Task38: forward-fill blank value cells.
 * Each row: VAL (rec same-row), VAL, conditional (blank → VAL+fill(above) | VAL).
 */
class AtpTask38Test extends AtpTaskBase {

    private static final ItemFilterCondition SAME_SUBROW = (a, c) -> c.sameSubrow(a);
    private static final ItemFilterCondition ABOVE       = (a, c) -> c.above(a).sameSubtable() && c.above(a).sameCol();

    private static final CellMatchCondition BLANK = new CellMatchCondition(c -> c.textBlank());

    @Override
    protected String taskId() {
        return "38";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(ActionSpec.rec(ProviderSpec.of(SAME_SUBROW)))),
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.of(new ConditionalContentSpec(
                                        BLANK,
                                        AtomicContentSpec.val(ActionSpec.fill("", ProviderSpec.of(1, TraversalOrder.REVERSE_ROW_MAJOR, ABOVE))),
                                        AtomicContentSpec.val()))
                        )
                )
        );
    }
}
