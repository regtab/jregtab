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

/**
 * ATP equivalent of Fluent API Task47.
 */
class AtpTask47Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(c -> !c.textBlank());

    private static final ProviderSpec SAME_ROW = ProviderSpec.val((a, c) -> c.sameRow(a));
    private static final ProviderSpec SAME_SURNAME_BELOW = ProviderSpec.val((a, c) ->
            c.is.below(a).sameCol() && c.has.sameStr(a));

    @Override
    protected String taskId() {
        return "47";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(
                                        ActionSpec.rec(SAME_ROW),
                                        ActionSpec.concat(SAME_SURNAME_BELOW)
                                )),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val())
                        )
                )
        );
    }
}
