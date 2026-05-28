package ru.icc.regtab.rtl;

/**
 * Task 81: repeating two-row subtables — first row VAL anchors with BW-&gt;REC (single cell
 * directly below); second row plain VAL cells.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_81/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask81Test}
 * <pre>
 * { [ [VAL: BW-&gt;REC]+ ]
 *   [ [VAL]+ ] }+
 * </pre>
 * BW-&gt;REC (without *) collects exactly one item — the single cell directly below the anchor.
 * Each anchor cell in the first row therefore forms a two-item record with its counterpart
 * in the second row.
 */
public class RtlTask81Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "81"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL: BW->REC]+ ]
                  [ [VAL]+ ] }+
                """;
    }
}
