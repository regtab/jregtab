package ru.icc.regtab.rtl;

/**
 * Task 60: implicit ATTR header subtable; repeating explicit data subtables with inherited
 * COL-&gt;AVP. Each data row: non-blank anchor VAL (RT*-&gt;REC) and conditional cells — blank
 * cells are filled from the nearest non-blank above (reverse traversal), non-blank cells
 * stay as VAL. Each subtable ends with an optional blank trailer row.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_60/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask60Test}
 * <pre>
 * [ [ATTR]+ ]
 * { COL-&gt;AVP
 *   [ [!BLANK? VAL: RT*-&gt;REC] [(BLANK? VAL: -(AV &amp; !BLANK)-&gt;FILL | VAL)]+ ]+
 *   [ [BLANK?]+ ]? }+
 * </pre>
 * The COL-&gt;AVP action is inherited by all cells in the explicit subtable. FILL uses
 * reverse-row-major traversal (-(AV &amp; !BLANK)) to look upward for the nearest non-blank
 * same-column cell.
 */
public class RtlTask60Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "60"; }

    @Override
    protected String buildRtl() {
                return """
                  [ [ATTR]+ ]
                { COL->AVP
                  [ [!BLANK? VAL: RT*->REC] [(BLANK? VAL: -(AV & !BLANK)->FILL | VAL)]+ ]+
                  [ [BLANK?]+ ]? }+
                """;
    }
}
