package ru.icc.regtab.rtl;

/**
 * Task 102: multi-section equipment passport table. First row is a header (ATTR + skips).
 * Repeating explicit subtables: the first row anchors each equipment name as VAL (COL->AVP,
 * ST*->REC); continuation rows start with a blank skip cell. Both first and continuation
 * rows contain paired subrows (attr|skip, val|skip) using SR->AVP.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_102/}
 * ATP spec: {@link ru.icc.regtab.atp.AtpTask102Test}
 * <pre>
 * [ [ATTR] []+ ]
 * { [ [!BLANK? VAL: COL->AVP, ST*->REC] { [(BLANK ? _ | ATTR)] [(BLANK ? _ | VAL: SR->AVP)] }+ ]
 *   [ [BLANK]                          { [(BLANK ? _ | ATTR)] [(BLANK ? _ | VAL: SR->AVP)] }+ ]+ }+
 * </pre>
 */
public class RtlTask102Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "102"; }

    @Override
    protected String buildRtl() {
        return """
                [ [ATTR] []+ ]
                { [ [!BLANK? VAL: COL->AVP, ST*->REC] { [(BLANK ? _ | ATTR)] [(BLANK ? _ | VAL: SR->AVP)] }+ ]
                  [ [BLANK]                          { [(BLANK ? _ | ATTR)] [(BLANK ? _ | VAL: SR->AVP)] }+ ]+ }+
                """;
    }
}
