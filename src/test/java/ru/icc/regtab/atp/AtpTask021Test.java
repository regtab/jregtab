package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.StringExtractor;
import ru.icc.regtab.atp.spec.TablePattern;

/**
 * Task 21: repeated subtables where a multi-cell normalised header row collects
 * all values below (unbounded), followed by exactly 2 normalised data rows.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_021/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask021Test}
 */
class AtpTask021Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec BELOW = ItemFilterConditionSpec.below();

    @Override
    protected String taskId() {
        return "021";
    }

    @Override
    protected TablePattern buildPattern() {
        return TablePattern.of(
                SubtablePattern.of(Quantifier.oneOrMore(),
                        RowPattern.of(
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(
                                        StringExtractor.WhitespaceNormalized.INSTANCE,
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, BELOW))
                                ))
                        ),
                        RowPattern.of(Quantifier.exactly(2),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(
                                        StringExtractor.WhitespaceNormalized.INSTANCE
                                ))
                        )
                )
        );
    }
}
