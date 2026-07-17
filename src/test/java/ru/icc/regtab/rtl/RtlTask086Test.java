package ru.icc.regtab.rtl;

/**
 * Task 86: implicit ATTR header subtable (3 columns); repeating explicit subtables with
 * row-level COL-&gt;AVP — anchor VAL collects (RT*, R+1&amp;C2) into REC; exactly 2 plain
 * VAL cells follow; continuation row has 2 skip cells and a VAL labeled 'D'-&gt;AVP.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_086/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask086Test}
 * <pre>
 *   [ [ATTR]{3} ]
 * { [ COL-&gt;AVP [VAL: (RT*,R+1&amp;C2)-&gt;REC] [VAL]{2} ]
 *   [ []{2} [VAL: 'D'-&gt;AVP] ] }+
 * </pre>
 * The anchor VAL at col 0 collects same-row right-of items (RT*) and the single cell
 * one row below at absolute column 2 R+1&amp;C2. The continuation row's last cell
 * carries the literal attribute 'D', making the full record (A, B, C, D).
 */
public class RtlTask086Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "086"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                  [ [ATTR]{3} ]
                { [ COL->AVP [VAL: (RT*,R+1&C2)->REC] [VAL]{2} ]
                  [ []{2} [VAL: 'CtxAttr'->AVP] ] }+
                """;
    }
}
