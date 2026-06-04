package ru.icc.regtab.rtl;

/**
 * Task 87: repeating rows, each with one-or-more explicit subrows — every subrow has an
 * anchor VAL (RT*-&gt;REC) and exactly 2 plain VAL cells. Explicit subrow boundaries limit
 * RT* to items within the same subrow iteration.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_087/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask087Test}
 * <pre>
 * [ { [VAL: RT*-&gt;REC] [VAL]{2} }+ ]+
 * </pre>
 * Each physical row is divided into groups of 3 cells by the {@code { }+} subrow pattern.
 * The anchor in each group collects its 2 right-of siblings into a record.
 */
public class RtlTask087Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "087"; }

    @Override
    protected String buildRtl() {
        return """
                [ { [VAL: RT*->REC] [VAL]{2} }+ ]+
                """;
    }
}
