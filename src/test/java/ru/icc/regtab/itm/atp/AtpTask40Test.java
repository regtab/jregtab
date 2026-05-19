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
import ru.icc.regtab.itm.model.semantics.provider.ItemFilterCondition;

/**
 * ATP equivalent of Fluent API Task40.
 */
class AtpTask40Test extends AtpTaskBase {

    private static final CellMatchCondition REPORTED_CRIME_TITLE =
            new CellMatchCondition(c -> c.text().contains("Reported crime in"));

    private static final ItemFilterCondition SAME_SUBTABLE_COL1 = (a, c) -> c.sameSubtable(a) && c.col(1);
    private static final ItemFilterCondition SAME_SUBROW        = (a, c) -> c.sameSubrow(a);

    @Override
    protected String taskId() {
        return "40";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(REPORTED_CRIME_TITLE, Quantifier.one(), AtomicContentSpec.val(
                                        input -> input.replaceAll("Reported crime in", "").trim(),
                                        ActionSpec.avp(""),
                                        ActionSpec.rec(ProviderSpec.val(SAME_SUBTABLE_COL1))
                                )),
                                CellPattern.skip()
                        ),
                        RowPattern.of(
                                CellPattern.skip(Quantifier.exactly(2))
                        ),
                        RowPattern.of(Quantifier.exactly(5),
                                CellPattern.of(AtomicContentSpec.attr()),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.avp(ProviderSpec.attr(SAME_SUBROW))
                                ))
                        ),
                        RowPattern.of(Quantifier.zeroOrOne(),
                                CellPattern.skip(Quantifier.exactly(2))
                        )
                )
        );
    }
}
