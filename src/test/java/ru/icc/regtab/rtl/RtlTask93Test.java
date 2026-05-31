package ru.icc.regtab.rtl;

/**
 * Task 93: repeating subtables, each with a grouped ATTR header row (groups separated by
 * required blank) and one-or-more grouped data rows; SC-&gt;AVP links each VAL to its header
 * ATTR in the same subcol, RT*-&gt;REC on the anchor collects sibling VALs to the right.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_93/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask93Test}
 * <pre>
 * { [ { [!BLANK? ATTR]+ [BLANK?] }+ ]
 *   [ { SC-&gt;AVP [!BLANK? VAL: RT*-&gt;REC] [!BLANK? VAL]+ [BLANK?] }+ ]+
 *   [ [BLANK?]+ ]? }+
 * </pre>
 * Note: separator uses [BLANK?]+ to consume all blank cells in the separator row
 * (rows in a fixed-width CSV table always have the same column count).
 */
public class RtlTask93Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "93"; }

    @Override
    protected String buildRtl() {
        return """
                { [ { [!BLANK? ATTR]+ [BLANK?] }+ ]
                  [ { SC->AVP [!BLANK? VAL: RT*->REC] [!BLANK? VAL]+ [BLANK?] }+ ]+
                  [ [BLANK?]+ ]? }+
                """;
    }
}
