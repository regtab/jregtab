package ru.icc.regtab.rtl;

/**
 * Task 103: vertical comparison table — rows alternate between ATTR headers and value pairs.
 * Table-level ST->AVP assigns each VAL its attribute from the same subtable ATTR.
 * Each VAL cell in the first implicit subtable anchors a column record (COL*->REC);
 * each explicit subtable contributes one attribute per column via inherited ST->AVP.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_103/}
 * ATP spec: {@link ru.icc.regtab.atp.AtpTask103Test}
 * <pre>
 * ST->AVP
 * [ [ATTR]+ ]
 * [ [VAL: COL*->REC]+ ]
 * { [ [ATTR]+ ]
 *   [ [VAL]+ ] }+
 * </pre>
 */
public class RtlTask103Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "103"; }

    @Override
    protected String buildRtl() {
        return """
                ST->AVP
                [ [ATTR]+ ]
                [ [VAL: COL*->REC]+ ]
                { [ [ATTR]+ ]
                  [ [VAL]+ ] }+
                """;
    }
}
