package ru.icc.regtab.rtl;

/**
 * Task 147: cross-tab tourism statistics with two header rows and fill.
 * Row 1: 3 ATTR=UC cells (zone, subject, municipality), then 5 INDICATOR->AVP
 * cells (one per indicator), rest empty. Row 2: 3 skip cells, then YEAR->AVP
 * values (3 years × 5 indicators interleaved). Data rows: first 2 columns may
 * be blank (fill from above) and use COL->AVP; 3rd column non-blank with COL->AVP;
 * first 5 data cells create REC via ROW{3} (zone, subject, municipality) and
 * COL{2} (indicator + year from both header rows); remaining cells skipped.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_147/}
 * <pre>
 * [ [ATTR=UC]{3} [VAL : 'INDICATOR'-&gt;AVP]+ ]
 * [ []{3} [VAL : 'YEAR'-&gt;AVP]+ ]
 * [ [BLANK ? VAL : -AV&amp;!BLANK-&gt;FILL, COL-&gt;AVP | VAL : COL-&gt;AVP]{2}
 *   [VAL : COL-&gt;AVP] [VAL : 'DATA'-&gt;AVP, (ROW{3},COL{2})-&gt;REC]{5} []+ ]+
 * </pre>
 */
public class RtlTask147Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "147"; }

    @Override
    protected String buildRtl() {
        return """
                [ [ATTR=UC]{3} [VAL : 'INDICATOR'->AVP]+ ]
                [ []{3} [VAL : 'YEAR'->AVP]+ ]
                [ [BLANK ? VAL : -AV&!BLANK->FILL, COL->AVP | VAL : COL->AVP]{2}
                  [VAL : COL->AVP] [VAL : 'DATA'->AVP, (ROW{3},COL{2})->REC]{5} []+ ]+
                """;
    }
}
