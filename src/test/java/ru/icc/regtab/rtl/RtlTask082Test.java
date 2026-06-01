package ru.icc.regtab.rtl;

/**
 * Task 82: repeating subtables with inherited SC-&gt;AVP (same-subcol) — each subtable has
 * one ATTR header row, one-or-more data rows (non-blank anchor with RT*-&gt;REC, then VALs),
 * and an optional blank trailer row.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_082/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask082Test}
 * <pre>
 * { SC-&gt;AVP
 *   [ [ATTR]+ ]
 *   [ [!BLANK? VAL: RT*-&gt;REC] [VAL]+ ]+
 *   [ [BLANK?]+ ]? }+
 * </pre>
 * SC-&gt;AVP looks up attributes in the same subcol, binding each VAL to its ATTR header
 * within the subrow column group. The !BLANK? guard on the anchor ensures rows with a
 * blank first cell are not mistakenly treated as data rows.
 */
public class RtlTask082Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "082"; }

    @Override
    protected String buildRtl() {
        return """
                { SC->AVP
                  [ [ATTR]+ ]
                  [ [!BLANK? VAL: RT*->REC] [VAL]+ ]+
                  [ [BLANK?]+ ]? }+
                """;
    }
}
