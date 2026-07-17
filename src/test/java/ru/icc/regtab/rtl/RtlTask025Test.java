package ru.icc.regtab.rtl;

/**
 * Task 25: flat table where each row's first cell is an ID anchor with a
 * slash-delimited SUFFIX, REC over values two-or-more positions right, and
 * JOIN(0) grouping rows with the same ID string.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_025/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask025Test}
 * <pre>
 * [ [VAL : RT->SUFFIX('/'), RT&C+2..*->REC('/'), BW&STR*->JOIN(0)] [VAL]+ ]+
 * </pre>
 * Each data row: anchor VAL uses RT->SUFFIX('/') (appends the immediately
 * right cell with '/'), RT&C+2..*->REC('/') (slash-delimited REC of all
 * same-row cells from relative column +2 onward), and BW&STR*->JOIN(0)
 * (concatenates anchors in rows below that share the same ID string). One-or-more
 * plain VAL cells follow.
 */
public class RtlTask025Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "025"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ [VAL : RT->SUFFIX('/'), RT&C+2..*->REC('/'), BW&STR*->JOIN(0)] [VAL]+ ]+
                """;
    }
}
