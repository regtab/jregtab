package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;

/**
 * ATP equivalent of Fluent API Task33.
 */
class AtpTask33Test extends AtpTaskBase {

    private static final ProviderSpec SAME_ROW = ProviderSpec.of((a, c) -> c.sameSubrow(a));
    private static final ProviderSpec SAME_GROUP_NEXT_ROWS =
            ProviderSpec.of((a, c) -> c.below(a).sameSubtable() && c.below(a).sameCol() && c.sameStr(a));

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
                                        ActionSpec.rec(SAME_ROW),
                                        ActionSpec.concat(SAME_GROUP_NEXT_ROWS)
                                )),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                        )
                )
        );
    }
}
