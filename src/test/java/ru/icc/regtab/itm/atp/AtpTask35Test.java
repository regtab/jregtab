package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellMatchCondition;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.StringExtractor;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;

/**
 * ATP equivalent of Fluent API Task35.
 */
class AtpTask35Test extends AtpTaskBase {

    private static final ProviderSpec BELOW_SAME_COL =
            ProviderSpec.val((a, c) -> c.below(a).sameCol() && c.sameSubtable(a));

    private static final CellMatchCondition COMPANY_ROW = new CellMatchCondition(c -> c.text().contains("*Company"));
    private static final CellMatchCondition NOT_COMPANY_ROW = new CellMatchCondition(c -> !c.text().contains("*Company"));

    @Override
    protected String taskId() {
        return "35";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(COMPANY_ROW, Quantifier.one(), AtomicContentSpec.val(
                                        StringExtractor.replace("\\*", ""),
                                        ActionSpec.rec(BELOW_SAME_COL)
                                ))
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(NOT_COMPANY_ROW, Quantifier.one(), AtomicContentSpec.val())
                        )
                )
        );
    }
}
