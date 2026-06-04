package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellMatchCondition;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CellPredicate;
import ru.icc.regtab.atp.spec.CompoundContentSpec;
import ru.icc.regtab.atp.spec.FilterTerm;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

/**
 * Task 48: a fixed 2-row skip header, then repeated person subtables each with
 * a name-anchor row, a blank+compound ATTR:VAL row, and an optional blank footer.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_048/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask048Test}
 */
class AtpTask048Test extends AtpTaskBase {

    private static final CellMatchCondition BLANK     = new CellMatchCondition(CellPredicate.Blank.INSTANCE);
    private static final CellMatchCondition NOT_BLANK = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);

    private static final ItemFilterConditionSpec SAME_SUBTABLE_COL1 = ItemFilterConditionSpec.and(FilterTerm.SameSubtable.INSTANCE, new FilterTerm.ColExact(1));
    private static final ItemFilterConditionSpec SAME_CELL          = ItemFilterConditionSpec.sameCell();

    @Override
    protected String taskId() {
        return "048";
    }

    @Override
    protected TablePattern buildPattern() {
        CompoundContentSpec telFaxSpec = CompoundContentSpec.of(
                AtomicContentSpec.attr(),
                CompoundContentSpec.Segment.of(":", AtomicContentSpec.val(ActionSpec.avp(ProviderSpec.attr(SAME_CELL))))
        );

        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.exactly(2),
                                CellPattern.skip(Quantifier.exactly(2))
                        )
                ),
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(
                                        ActionSpec.avp(""),
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_SUBTABLE_COL1))
                                )),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), telFaxSpec)
                        ),
                        RowPattern.of(
                                CellPattern.of(BLANK, Quantifier.one(), null),
                                CellPattern.of(NOT_BLANK, Quantifier.one(), telFaxSpec)
                        ),
                        RowPattern.of(Quantifier.zeroOrOne(),
                                CellPattern.of(BLANK, Quantifier.exactly(2), null)
                        )
                )
        );
    }
}
