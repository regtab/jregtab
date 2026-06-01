package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.StringExtractor;
import ru.icc.regtab.atp.spec.SubrowPattern;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

/**
 * Task 90: header row (VAL, REPL strips asterisks) + one-or-more AUX data rows.
 * BW*-&gt;SUFFIX('/') folds column data into the header value; ()-&gt;REC emits a single-field record.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_090/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask090Test}
 */
class AtpTask090Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec BELOW = ItemFilterConditionSpec.below();

    @Override
    protected String taskId() { return "090"; }

    @Override
    protected TablePattern buildPattern() {
        StringExtractor extractor = new StringExtractor.Replaced("\\*", "");
        ActionSpec suffix = ActionSpec.suffix("/", ProviderSpec.any(ProviderSpec.UNBOUNDED, BELOW));
        ActionSpec rec    = ActionSpec.rec();

        CellPattern valPlus = CellPattern.of(Quantifier.oneOrMore(),
                AtomicContentSpec.val(extractor, suffix, rec));
        CellPattern auxPlus = CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.aux());

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(Quantifier.one(), SubrowPattern.of(valPlus)),
                        RowPattern.of(Quantifier.oneOrMore(), SubrowPattern.of(auxPlus))
                )
        );
    }
}
