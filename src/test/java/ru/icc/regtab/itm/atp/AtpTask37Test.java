package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellMatchCondition;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.CellPredicate;
import ru.icc.regtab.itm.atp.spec.ConditionalContentSpec;
import ru.icc.regtab.itm.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;
import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;

/**
 * ATP equivalent of Fluent API Task37: corner skip + qual-header row, then per-person rows
 * where date cells are conditional (blank → skip, non-blank → val with rec first-in-row + first-in-col).
 */
class AtpTask37Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_SUBROW    = ItemFilterConditionSpec.sameSubrow();
    private static final ItemFilterConditionSpec SAME_SUBCOLUMN = ItemFilterConditionSpec.sameSubcol();

    private static final CellMatchCondition BLANK = new CellMatchCondition(CellPredicate.Blank.INSTANCE);

    @Override
    protected String taskId() {
        return "37";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.of(Quantifier.oneOrMore(),
                        new ConditionalContentSpec(
                                BLANK,
                                AtomicContentSpec.skip(),
                                AtomicContentSpec.val(ActionSpec.rec(ProviderSpec.val(1, SAME_SUBROW), ProviderSpec.val(1, SAME_SUBCOLUMN)))))
                        )
                )
        ).withTransformations(new AnchorAttributeAtPosition(2));
    }
}
