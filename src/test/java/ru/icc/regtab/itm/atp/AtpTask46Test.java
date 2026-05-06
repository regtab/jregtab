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
 * ATP equivalent of Fluent API Task46.
 */
class AtpTask46Test extends AtpTaskBase {

    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(c -> !c.textBlank());

    private static final ProviderSpec SAME_ROW_VAL = ProviderSpec.val((a, c) -> c.sameRow(a));
    private static final ProviderSpec SAME_ROW_ATTR = ProviderSpec.attr((a, c) -> c.sameRow(a));
    private static final ProviderSpec SAME_NAME_BELOW = ProviderSpec.val((a, c) ->
            c.is.below(a).sameCol() && c.has.sameStr(a));

    @Override
    protected String taskId() {
        return "46";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(
                                        ActionSpec.avp(""),
                                        ActionSpec.rec(SAME_ROW_VAL),
                                        ActionSpec.concat(SAME_NAME_BELOW)
                                )),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.attr()),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(
                                        ActionSpec.avp(SAME_ROW_ATTR)
                                ))
                        )
                )
        );
    }
}
