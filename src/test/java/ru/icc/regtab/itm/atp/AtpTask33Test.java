package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;

/**
 * ATP equivalent of Fluent API Task33.
 */
class AtpTask33Test extends AtpTaskBase {

    private static final ItemFilterCondition SAME_SUBROW = (a, c) -> c.sameSubrow(a);
    private static final ItemFilterCondition BELOW_STR   = (a, c) -> c.below(a).sameSubtable() && c.below(a).sameCol() && c.sameStr(a);

    @Override
    protected String taskId() {
        return "33";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.of(SAME_SUBROW)),
                                        ActionSpec.concat(ProviderSpec.of(BELOW_STR))
                                )),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                        )
                )
        );
    }
}
