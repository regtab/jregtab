package ru.icc.regtab.rtl;

/**
 * Task 89: two ATTR header rows, each followed by VAL data rows; the first section
 * separates each VAL row with a BLANK row (grouped in an explicit repeating subtable),
 * the second section has consecutive VAL rows. Table-level COL-&gt;AVP (inherited) assigns
 * column names; RT*-&gt;REC on the first VAL cell collects the full row into one record.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_89/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask89Test}
 * <pre>
 * COL-&gt;AVP
 *   [ [ATTR]+ ]
 * { [ [VAL: RT*-&gt;REC] [VAL]+ ]
 *   [ [BLANK?]+ ] }+
 *   [ [ATTR]+ ]
 *   [ [VAL: RT*-&gt;REC] [VAL]+ ]+
 * </pre>
 */
public class RtlTask89Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "89"; }

    @Override
    protected String buildRtl() {
        return """
                COL->AVP
                  [ [ATTR]+ ]
                { [ [VAL: RT*->REC] [VAL]+ ]
                  [ [BLANK?]+ ] }+
                  [ [ATTR]+ ]
                  [ [VAL: RT*->REC] [VAL]+ ]+
                """;
    }
}
