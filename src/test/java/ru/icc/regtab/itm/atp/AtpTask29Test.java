package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubrowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;
import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;

/**
 * ATP equivalent of Fluent API Task29.
 */
class AtpTask29Test extends AtpTaskBase {

    private static final ItemFilterCondition SAME_ROW = (a, c) -> c.sameRow(a);
    private static final ItemFilterCondition RIGHT_OF = (a, c) -> c.rightOf(a).sameSubrow();

    @Override
    protected String taskId() {
        return "29";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                SubrowPattern.of(
                                        CellPattern.of(Quantifier.exactly(6), AtomicContentSpec.val())
                                ),
                                SubrowPattern.of(Quantifier.oneOrMore(),
                                        CellPattern.of(AtomicContentSpec.val(
                                                ActionSpec.rec(ProviderSpec.val(6, SAME_ROW), ProviderSpec.val(RIGHT_OF))
                                        )),
                                        CellPattern.of(Quantifier.exactly(3), AtomicContentSpec.val())
                                )
                        )
                )
        ).withTransformations(new AnchorAttributeAtPosition(6));
    }
}
