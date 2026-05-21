package ru.icc.regtab.itm.rtl;

/**
 * Task 25: flat table where each row's first cell is an ID anchor with a
 * slash-delimited SUFFIX, REC over values two-or-more positions right, and
 * CONCAT grouping rows with the same ID string.
 * <p>
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask25Test}
 * <pre>
 * [ [VAL : RT->SUFFIX('/'), (RT & C+2..)*->REC('/'), (BW & STR)*->CONCAT] [VAL]+ ]+
 * </pre>
 * Each data row: anchor VAL uses RT->SUFFIX('/') (appends the immediately
 * right cell with '/'), (RT & C+2..)*->REC('/') (slash-delimited REC of all
 * same-row cells from relative column +2 onward), and (BW & STR)*->CONCAT
 * (concatenates anchors in rows below that share the same ID string). One-or-more
 * plain VAL cells follow.
 */
public class RtlTask25Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "25"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : RT->SUFFIX('/'), (RT & C+2..)*->REC('/'), (BW & STR)*->CONCAT] [VAL]+ ]+
                """;
    }
}
