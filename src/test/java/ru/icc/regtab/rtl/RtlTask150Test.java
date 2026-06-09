package ru.icc.regtab.rtl;

/**
 * Task 150: population statistics table with location rows and year columns.
 * Row 1: one ATTR cell (column label) then YEAR->AVP values. Data rows:
 * first cell is LOCATION->AVP; remaining cells use ST->AVP (attribute name
 * from the same-subtable ATTR cell = column label) and create REC via
 * ROW (location) and COL (year).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_150/}
 * <pre>
 * [ [ATTR] [VAL : 'YEAR'-&gt;AVP]+ ]
 * [ [VAL : 'LOCATION'-&gt;AVP] [VAL : ST-&gt;AVP, (ROW,COL)-&gt;REC]+ ]+
 * </pre>
 */
public class RtlTask150Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "150"; }

    @Override
    protected String buildRtl() {
        return """
                [ [ATTR] [VAL : 'YEAR'->AVP]+ ]
                [ [VAL : 'LOCATION'->AVP] [VAL : ST->AVP, (ROW,COL)->REC]+ ]+
                """;
    }
}
