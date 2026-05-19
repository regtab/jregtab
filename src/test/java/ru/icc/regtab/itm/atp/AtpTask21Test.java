package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;
import ru.icc.regtab.itm.interpret.WhitespaceNormalization;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;

/**
 * ATP equivalent of Fluent API Task21.
 */
class AtpTask21Test extends AtpTaskBase {

    private static final ItemFilterCondition BELOW = (a, c) -> c.below(a).sameSubtable() && c.below(a).sameCol();

    @Override
    protected String taskId() {
        return "21";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.of(BELOW))
                                ))
                        ),
                        RowPattern.of(Quantifier.exactly(2),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                        )
                )
        ).withTransformations(new WhitespaceNormalization());
    }
}
