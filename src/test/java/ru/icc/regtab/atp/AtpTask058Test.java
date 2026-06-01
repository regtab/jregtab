package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CompoundContentSpec;
import ru.icc.regtab.atp.spec.CompoundSegment;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.StringExtractor;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

import java.util.List;


/**
 * Task 58: each row holds one-or-more key=value compound cells — trimmed anchor VAL
 * with same-cell REC splits on '=' to produce key and value.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_058/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask058Test}
 */
class AtpTask058Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_CELL = ItemFilterConditionSpec.sameCell();
    private static final StringExtractor         TRIM      = StringExtractor.Trimmed.INSTANCE;

    @Override
    protected String taskId() { return "058"; }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.one(),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(Quantifier.oneOrMore(), new CompoundContentSpec(List.of(
                                        new CompoundSegment("", AtomicContentSpec.val(TRIM,
                                                ActionSpec.rec(ProviderSpec.val(SAME_CELL))
                                        )),
                                        new CompoundSegment("=", AtomicContentSpec.val(TRIM))
                                ), ""))
                        )
                )
        );
    }
}
