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
import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;

/**
 * ATP equivalent of Fluent API Task32.
 */
class AtpTask32Test extends AtpTaskBase {

    private static final ItemFilterCondition SAME_SUBROW    = (a, c) -> c.sameSubrow(a);
    private static final ItemFilterCondition SAME_SUBCOLUMN = (a, c) -> c.sameSubcol(a);

    private static final CellMatchCondition BLANK = new CellMatchCondition(c -> c.textBlank());

    @Override
    protected String taskId() {
        return "32";
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
