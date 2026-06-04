package ru.icc.regtab.rtl;

/**
 * Task 76: implicit header subtable (skip + VALs); repeating explicit subtables each with
 * a label row (VAL + blanks) and data rows: trimmed VAL anchor, non-blank VAL cells
 * collecting COL + (ST &amp; C0) + ROW into REC.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_076/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask076Test}
 * <pre>
 *   [ []         [VAL]+ ]
 * { [ [VAL]      [BLANK]+ ]
 *   [ [VAL=TRIM] [!BLANK? VAL: (COL, (ST &amp; C0), ROW)-&gt;REC]+ ]+ }+
 * </pre>
 * (ST &amp; C0) targets column 0 within the same subtable, providing the subtable's own label
 * as a third dimension in the record alongside the column header (COL) and row label (ROW).
 */
public class RtlTask076Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "076"; }

    @Override
    protected String buildRtl() {
        return """
                  [ []         [VAL]+ ]
                { [ [VAL]      [BLANK]+ ]
                  [ [VAL=TRIM] [!BLANK? VAL: (COL, (ST & C0), ROW)->REC]+ ]+ }+
                """;
    }
}
