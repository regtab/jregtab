package ru.icc.regtab.rtl;

/**
 * Task 96: two header rows set up three attributes (A at col 0, B and C both at col 1),
 * then each data subtable has two rows: first row with two VALs (col 0 gets attribute A
 * via COL->AVP, col 1 gets B via the same inherited COL->AVP picking the first match),
 * second row with one VAL that explicitly resolves C via (COL&R1)->AVP.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_096/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask096Test}
 * <pre>
 *   [ [ATTR]{2} ]
 *   [ [] [ATTR] ]
 * { [ COL->AVP [VAL : ST*->REC] [VAL] ]
 *   [ [] [VAL: (COL&R1)->AVP] ] }+
 * </pre>
 */
public class RtlTask096Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "096"; }

    @Override
    protected String buildRtl() {
        return """
                  [ [ATTR]{2} ]
                  [ [] [ATTR] ]
                { [ COL->AVP [VAL : ST*->REC] [VAL] ]
                  [ [] [VAL: (COL&R1)->AVP] ] }+
                """;
    }
}
