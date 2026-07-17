package ru.icc.regtab.rtl;

/**
 * Task 52: cross-table unpivot with compound cell values ("ND Mon") and injected constant YEAR=2025.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_052/}
 * <pre>
 * [ [] [VAL : 'AIRLINE'->AVP]+ ]
 * [ [VAL : 'AIRPORT'->AVP]
 *   [VAL : (COL, ROW, CL, @'YEAR'='2025')->REC, 'ND'->AVP " " VAL : 'MON'->AVP]+ ]+
 * </pre>
 * Same as task 51 but adds a constant attribute-value pair YEAR=2025 to every record.
 */
public class RtlTask052Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "052"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ [] [VAL : 'AIRLINE'->AVP]+ ]
                [ [VAL : 'AIRPORT'->AVP]
                  [VAL : (COL, ROW, CL, @'YEAR'='2025')->REC, 'ND'->AVP " " VAL : 'MON'->AVP]+ ]+
                """;
    }
}
