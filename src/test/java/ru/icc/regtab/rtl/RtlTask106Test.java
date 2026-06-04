package ru.icc.regtab.rtl;

/**
 * Task 106: month headers in row 1; data rows split "INDICATOR, UNIT" from the first
 * cell and collect the range string as MIN; each data cell becomes a record keyed to
 * the month header via (ROW{2},COL)->REC.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_106/}
 * <pre>
 * [ [] [VAL: 'MON'->AVP]+ ]
 * [ [VAL: 'INDICATOR'->AVP ',' VAL=TRIM: 'UNIT'->AVP] [VAL: 'MIN'->AVP, (ROW{2},COL)->REC]+ ]+
 * </pre>
 */
public class RtlTask106Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "106"; }

    @Override
    protected String buildRtl() {
        return "[ [] [VAL: 'MON'->AVP]+ ]\n" +
               "[ [VAL: 'INDICATOR'->AVP ',' VAL=TRIM: 'UNIT'->AVP] [VAL: 'MIN'->AVP, (CL*,ROW{2},COL)->REC '-' VAL: 'MAX'->AVP '/' VAL: 'AVE'->AVP]+ ]+";
    }
}
