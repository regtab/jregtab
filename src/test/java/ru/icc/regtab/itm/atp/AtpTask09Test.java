package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellMatchCondition;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.ConditionalContentSpec;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.StringExtractor;
import ru.icc.regtab.itm.atp.spec.SubrowPattern;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;
import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * ATP equivalent of Fluent API Task09.
 */
class AtpTask09Test extends AtpTaskBase {

    private static final ProviderSpec FIRST_IN_SAME_ROW = ProviderSpec.of(1, (a, c) -> c.sameRow(a));
    private static final ProviderSpec FIRST_IN_SAME_COLUMN = ProviderSpec.of(1, (a, c) -> c.sameCol(a));

    private static final CellMatchCondition BLANK = new CellMatchCondition(c -> c.textBlank());

    private static final ConditionalContentSpec BLANK_SKIP_OTHERWISE_VAL_WITH_REC = new ConditionalContentSpec(
            BLANK,
            AtomicContentSpec.skip(),
            AtomicContentSpec.val(ActionSpec.rec(FIRST_IN_SAME_ROW, FIRST_IN_SAME_COLUMN)));

    @Override
    protected String taskId() {
        return "09";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.skip(),
                                CellPattern.of(Quantifier.exactly(5), AtomicContentSpec.val(StringExtractor.replace("\\s+", "")))
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                SubrowPattern.of(
                                        CellPattern.of(AtomicContentSpec.val()),
                                        CellPattern.of(Quantifier.oneOrMore(), BLANK_SKIP_OTHERWISE_VAL_WITH_REC)
                                )
                        )
                )
        );
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(2).apply(actual);
    }
}
