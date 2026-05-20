package ru.icc.regtab.itm.atp;

import ru.icc.regtab.itm.atp.spec.ActionSpec;
import ru.icc.regtab.itm.atp.spec.AtomicContentSpec;
import ru.icc.regtab.itm.atp.spec.CellMatchCondition;
import ru.icc.regtab.itm.atp.spec.CellPattern;
import ru.icc.regtab.itm.atp.spec.CellPredicate;
import ru.icc.regtab.itm.atp.spec.Constraint;
import ru.icc.regtab.itm.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.itm.atp.spec.ProviderSpec;
import ru.icc.regtab.itm.atp.spec.Quantifier;
import ru.icc.regtab.itm.atp.spec.RowPattern;
import ru.icc.regtab.itm.atp.spec.StringExtractor;
import ru.icc.regtab.itm.atp.spec.SubtablePattern;
import ru.icc.regtab.itm.atp.spec.TablePattern;

import java.util.List;

/**
 * ATP equivalent of Fluent API Task40.
 */
class AtpTask40Test extends AtpTaskBase {

    private static final CellMatchCondition REPORTED_CRIME_TITLE =
            new CellMatchCondition(new CellPredicate.Contains("Reported crime in"));

    private static final ItemFilterConditionSpec SAME_SUBTABLE_COL1 = ItemFilterConditionSpec.and(Constraint.SameSubtable.INSTANCE, new Constraint.ColExact(1));
    private static final ItemFilterConditionSpec SAME_SUBROW        = ItemFilterConditionSpec.sameSubrow();

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
                                        new StringExtractor.Chain(List.of(new StringExtractor.Replaced("Reported crime in", ""), StringExtractor.Trimmed.INSTANCE)),
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
