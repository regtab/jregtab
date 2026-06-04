package ru.icc.regtab.rtl;

/**
 * Task 13: header row with five ATTR cells; data rows use AVP for the first
 * five columns and REC referencing four specific column positions in the same row.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_013/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask013Test}
 * <pre>
 * [ [ATTR]{5} []+ ]
 * [ [VAL : SC->AVP, ((SR & C2), (SR & C4), (SR & C1), (SR & C3))->REC] [VAL : SC->AVP]{4} []+ ]+
 * </pre>
 * Header row: exactly 5 ATTR cells (column names) then trailing skips. Data
 * rows: anchor VAL uses SC->AVP (column-header attribute) and a four-provider
 * REC gathering values from same-subrow columns C2, C4, C1, C3. The next four
 * cells also use SC->AVP. Remaining cells are skipped.
 */
public class RtlTask013Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "013"; }

    @Override
    protected String buildRtl() {
        return """
                [ [ATTR]{5} []+ ]
                [ [VAL : SC->AVP, ((SR & C2), (SR & C4), (SR & C1), (SR & C3))->REC] [VAL : SC->AVP]{4} []+ ]+
                """;
    }
}
