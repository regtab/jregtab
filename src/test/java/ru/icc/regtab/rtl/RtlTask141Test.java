package ru.icc.regtab.rtl;

/**
 * Task 141: SEZ characteristics table — transposed layout (zones in columns,
 * attributes in rows). Header row: first cell skipped, remaining cells are zone
 * names — each gets 'ZONE'->AVP and COL*->REC (record collects all column cells
 * below). Attribute rows: first cell is ATTR=UC (characteristic name), remaining
 * cells get ROW->AVP so the attribute name is taken from the row's first cell.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_141/}
 * <pre>
 * [ [] [VAL : 'ZONE'-&gt;AVP, COL*-&gt;REC]+ ]
 * [ [ATTR=UC] [VAL : ROW-&gt;AVP]+ ]+
 * </pre>
 */
public class RtlTask141Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "141"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [VAL : 'ZONE'->AVP, COL*->REC]+ ]
                [ [ATTR=UC] [VAL : ROW->AVP]+ ]+
                """;
    }
}
