package ru.icc.regtab.rtl;

/**
 * Task 51: cross-table unpivot with compound cell values ("ND Mon") split by space —
 * column headers tagged as AIRLINE, row headers as AIRPORT, first segment as ND, second as MON.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_051/}
 * <pre>
 * [ [] [VAL : 'AIRLINE'->AVP]+ ]
 * [ [VAL : 'AIRPORT'->AVP]
 *   [VAL : (COL, ROW, CL)->REC, 'ND'->AVP " " VAL : 'MON'->AVP]+ ]+
 * </pre>
 * Header row: skip cell then one-or-more non-blank column-header VALs tagged as AIRLINE.
 * Data rows: first cell tagged as AIRPORT; each remaining cell contains a compound value
 * "ND Mon" split by space — the first segment becomes ND via REC referencing column header
 * (COL), row header (ROW) and same-cell second segment (CL); the second segment becomes MON.
 */
public class RtlTask051Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "051"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [VAL : 'AIRLINE'->AVP]+ ]
                [ [VAL : 'AIRPORT'->AVP]
                  [VAL : (COL, ROW, CL)->REC, 'ND'->AVP " " VAL : 'MON'->AVP]+ ]+
                """;
    }
}
