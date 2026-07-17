package ru.icc.regtab.rtl;

/**
 * Task 146: indicator table with 5 year columns and trailing skip columns.
 * Row 1: one ATTR=UC cell (indicator label), 5 YEAR->AVP cells, remaining skipped.
 * Data rows: first cell gets COL->AVP (indicator name from column header);
 * next 5 cells get 'DATA'->AVP and create REC via ROW (indicator) and COL (year);
 * remaining cells are skipped.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_146/}
 * <pre>
 * [ [ATTR=UC] [VAL : 'YEAR'-&gt;AVP]{5} []+ ]
 * [ [VAL : COL-&gt;AVP] [VAL : 'DATA'-&gt;AVP, (ROW,COL)-&gt;REC]{5} []+ ]+
 * </pre>
 */
public class RtlTask146Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "146"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ [ATTR=UC] [VAL : 'YEAR'->AVP]{5} []+ ]
                [ [VAL : COL->AVP] [VAL : 'DATA'->AVP, (ROW,COL)->REC]{5} []+ ]+
                """;
    }
}
