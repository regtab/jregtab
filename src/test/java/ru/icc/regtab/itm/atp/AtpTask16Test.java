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
 * ATP equivalent of Fluent API Task16.
 */
class AtpTask16Test extends AtpTaskBase {

    private static final ProviderSpec REC_RIGHT =
            ProviderSpec.of(1, (a, c) -> c.is.rightOf(a).sameRow());

    private static final ProviderSpec CONCAT_SAME_LABEL_BELOW =
            ProviderSpec.of((a, c) -> c.is.below(a).sameCol() && c.has.sameStr(a));

    @Override
    protected String taskId() {
        return "16";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(REC_RIGHT),
                                        ActionSpec.concat(CONCAT_SAME_LABEL_BELOW)
                                )),
                                CellPattern.of(AtomicContentSpec.val())
                        )
                )
        );
    }
}
