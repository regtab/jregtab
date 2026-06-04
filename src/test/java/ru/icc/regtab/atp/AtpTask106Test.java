package ru.icc.regtab.atp;

import ru.icc.regtab.atp.spec.ActionSpec;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CompoundContentSpec;
import ru.icc.regtab.atp.spec.ItemFilterConditionSpec;
import ru.icc.regtab.atp.spec.ProviderSpec;
import ru.icc.regtab.atp.spec.Quantifier;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.StringExtractor;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;

/**
 * Task 106: month headers in row 1; data rows split "INDICATOR, UNIT" from the first
 * cell and collect the range string as MIN/MAX/AVE; each data cell becomes a record
 * keyed to the month header via (CL*,ROW{2},COL)->REC.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_106/}
 * RTL: {@link ru.icc.regtab.rtl.RtlTask106Test}
 * <pre>
 * [ [] [VAL: 'MON'-&gt;AVP]+ ]
 * [ [VAL: 'INDICATOR'-&gt;AVP ',' VAL=TRIM: 'UNIT'-&gt;AVP]
 *   [VAL: 'MIN'-&gt;AVP, (CL*,ROW{2},COL)-&gt;REC '-' VAL: 'MAX'-&gt;AVP '/' VAL: 'AVE'-&gt;AVP]+ ]+
 * </pre>
 */
class AtpTask106Test extends AtpTaskBase {

    private static final ItemFilterConditionSpec SAME_CELL = ItemFilterConditionSpec.sameCell();
    private static final ItemFilterConditionSpec SAME_ROW  = ItemFilterConditionSpec.sameRow();
    private static final ItemFilterConditionSpec SAME_COL  = ItemFilterConditionSpec.sameCol();

    @Override
    protected String taskId() { return "106"; }

    @Override
    protected TablePattern buildPattern() {
        // [ [] [VAL: 'MON'->AVP]+ ]
        ActionSpec monAvp = ActionSpec.avp(ProviderSpec.ctxAttr("MON"));

        // [VAL: 'INDICATOR'->AVP ',' VAL=TRIM: 'UNIT'->AVP]
        CompoundContentSpec indicatorUnit = CompoundContentSpec.of(
                AtomicContentSpec.val(ActionSpec.avp(ProviderSpec.ctxAttr("INDICATOR"))),
                CompoundContentSpec.Segment.of(",",
                        AtomicContentSpec.val(StringExtractor.Trimmed.INSTANCE,
                                ActionSpec.avp(ProviderSpec.ctxAttr("UNIT"))))
        );

        // [VAL: 'MIN'->AVP, (CL*,ROW{2},COL)->REC '-' VAL: 'MAX'->AVP '/' VAL: 'AVE'->AVP]
        CompoundContentSpec minMaxAve = CompoundContentSpec.of(
                AtomicContentSpec.val(
                        ActionSpec.avp(ProviderSpec.ctxAttr("MIN")),
                        ActionSpec.rec(
                                ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_CELL),
                                ProviderSpec.val(2, SAME_ROW),
                                ProviderSpec.val(SAME_COL)
                        )
                ),
                CompoundContentSpec.Segment.of("-",
                        AtomicContentSpec.val(ActionSpec.avp(ProviderSpec.ctxAttr("MAX")))),
                CompoundContentSpec.Segment.of("/",
                        AtomicContentSpec.val(ActionSpec.avp(ProviderSpec.ctxAttr("AVE"))))
        );

        return TablePattern.of(
                SubtablePattern.of(
                        RowPattern.of(
                                CellPattern.of(AtomicContentSpec.skip()),
                                CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(monAvp))
                        ),
                        RowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(indicatorUnit),
                                CellPattern.of(Quantifier.oneOrMore(), minMaxAve)
                        )
                )
        );
    }
}
