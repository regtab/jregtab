package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;
import ru.icc.regtab.itm.interpret.DelimitedFieldSplit;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * ATP equivalent of Fluent API Task25.
 */
class AtpTask25Test extends AtpTaskBase {

    private static final String SEP = "/";

    private static final ProviderSpec RIGHT_SAME_ROW =
            ProviderSpec.of(1, (a, c) -> c.is.rightOf(a).sameRow());

    private static final ProviderSpec SAME_GROUP_BELOW =
            ProviderSpec.of((a, c) -> c.is.below(a).sameCol() && c.has.sameStr(a));

    private static final ProviderSpec SAME_ROW_AFTER_ACCOUNT =
            ProviderSpec.of((a, c) -> c.is.in.sameRow(a) && c.cell().col() > a.cell().col() + 1);

    @Override
    protected String taskId() {
        return "25";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.suffix(SEP, RIGHT_SAME_ROW),
                                        ActionSpec.rec(SAME_ROW_AFTER_ACCOUNT),
                                        ActionSpec.concat(SAME_GROUP_BELOW)
                                )),
                                CellPattern.of(AtomicContentSpec.val()),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val())
                        )
                )
        );
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new DelimitedFieldSplit(SEP).apply(actual);
    }
}
