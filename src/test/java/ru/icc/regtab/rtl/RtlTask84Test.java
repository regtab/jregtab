package ru.icc.regtab.rtl;

/**
 * Task 84: repeating rows with a leading plain VAL and one-or-more explicit subrows each
 * containing a VAL anchor (ROW, RT)-&gt;REC(1) and a plain VAL.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_84/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask84Test}
 * <pre>
 * [ [VAL] { [VAL: (ROW, RT)-&gt;REC(1) ] [VAL] }+ ]+
 * </pre>
 * Each anchor cell builds its record from same-row (ROW) and right-of (RT) providers;
 * REC(1) places the anchor itself at position 1, inserting the leading row VAL before it
 * and the right-of VAL after it.
 */
public class RtlTask84Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "84"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL] { [VAL: (ROW, RT)->REC(1) ] [VAL] }+ ]+
                """;
    }
}
