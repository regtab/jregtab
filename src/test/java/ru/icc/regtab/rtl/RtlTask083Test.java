package ru.icc.regtab.rtl;

/**
 * Task 83: implicit ATTR header subtable; repeating explicit subtables — first row has
 * inherited COL-&gt;AVP, an anchor VAL (RT*, BW)-&gt;REC, and plain VAL cells; second row has
 * a VAL with literal 'D'-&gt;AVP followed by skip cells.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_083/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask083Test}
 * <pre>
 *   [          [ATTR]+ ]
 * { [ COL-&gt;AVP [VAL: (RT*, BW)-&gt;REC][VAL]+ ]
 *   [          [VAL: 'D'-&gt;AVP ][]+ ] }+
 * </pre>
 * The anchor builds its record from all right-of items (RT*) plus a single below item (BW).
 * The second row labels its anchor with the literal attribute 'D' and fills remaining cells
 * with skip cells ([]).
 */
public class RtlTask083Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "083"; }

    @Override
    protected String buildRtl() {
        return """
                  [          [ATTR]+ ]
                { [ COL->AVP [VAL: (RT*, BW)->REC][VAL]+ ]
                  [          [VAL: 'D'->AVP ][]+ ] }+
                """;
    }
}
