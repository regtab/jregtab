package ru.icc.regtab.itm.rtl;

/**
 * Task 35: repeated subtables where the header row is identified by a glob
 * match on "*Company" and the anchor value is cleaned by stripping the asterisk.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_35/}
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask35Test}
 * <pre>
 * { [ [~'*Company' ? VAL = REPL('\*', '') : BW*->REC] ]
 *   [ [!~'*Company' ? VAL] ]+ }+
 * </pre>
 * Header row: cell matching glob pattern '*Company' whose VAL is stripped of
 * the leading asterisk via REPL and anchors an unbounded BW*->REC. Data rows:
 * one-or-more cells that do NOT match '*Company', each with a plain VAL.
 */
public class RtlTask35Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "35"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [~'*Company' ? VAL = REPL('\\*', '') : BW*->REC] ]
                  [ [!~'*Company' ? VAL] ]+ }+
                """;
    }
}
