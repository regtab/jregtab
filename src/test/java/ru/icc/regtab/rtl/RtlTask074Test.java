package ru.icc.regtab.rtl;

/**
 * Task 74: exactly 3 ATTR header cells; repeating data rows with inherited COL-&gt;AVP —
 * an anchor VAL collecting (RT*, @'D'='d') into REC, followed by exactly 2 plain VAL cells.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_074/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask074Test}
 * <pre>
 * [          [ATTR]{3} ]
 * [ COL-&gt;AVP [VAL: (RT*, @'D'='d')-&gt;REC][VAL]{2} ]+
 * </pre>
 * The context provider @'D'='d' injects a fixed attribute-value pair 'D'='d' into every
 * record. RT* collects all right-of VAL items. The {3} and {2} quantifiers enforce exact
 * column counts.
 */
public class RtlTask074Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "074"; }

    @Override
    protected String buildRtl() {
        return """
                [          [ATTR]{3} ]
                [ COL->AVP [VAL: (RT*, @'D'='d')->REC][VAL]{2} ]+
                """;
    }
}
