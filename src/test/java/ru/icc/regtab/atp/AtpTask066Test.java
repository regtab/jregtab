package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellMatchCondition;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CellPredicate;
import ru.icc.regtab.atp.spec.CompoundContentSpec;
import ru.icc.regtab.atp.spec.CompoundSegment;
import ru.icc.regtab.atp.spec.ConditionalContentSpec;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.StringExtractor;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

import java.util.List;


/**
 * Task 66: each cell conditionally splits on '=' (trimmed VAL:CL->REC + trimmed VAL)
 * or falls back to plain VAL with empty-string REC.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_066/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask066Test}
 */
class AtpTask066Test extends AtpTaskBase {

    private static final CellMatchCondition CONTAINS_EQ = new CellMatchCondition(new CellPredicate.Contains("="));
    private static final ItemFilterConditionSpec SAME_CELL = ItemFilterConditionSpec.sameCell();
    private static final StringExtractor         TRIM      = StringExtractor.Trimmed.INSTANCE;

    @Override
    protected String taskId() { return "066"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(Quantifier.oneOrMore(), new ConditionalContentSpec(
                                        CONTAINS_EQ,
                                        new CompoundContentSpec(List.of(
                                                new CompoundSegment("", AtomicContentSpec.val(TRIM,
                                                        ActionSpec.rec(ProviderSpec.val(SAME_CELL))
                                                )),
                                                new CompoundSegment("=", AtomicContentSpec.val(TRIM))
                                        ), ""),
                                        AtomicContentSpec.val(ActionSpec.rec(ProviderSpec.ctxVal("")))
                                ))
                        )
                )
        );
    }
}
