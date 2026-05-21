package ru.icc.regtab.itm.rtl;

/**
 * Task 03: flat table with a row-key anchor followed by exactly two value
 * cells per row, each referencing the anchor via same-subrow.
 * <p>
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask03Test}
 * <pre>
 * [ [VAL] [VAL : SR->REC(1)]{2} ]+
 * </pre>
 * Each data row starts with a plain VAL anchor. The next two cells each carry
 * REC(1) with provider SR (same subrow, cardinality 1), binding the row-key
 * anchor to every value cell.
 */
public class RtlTask03Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "03"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL] [VAL : SR->REC(1)]{2} ]+
                """;
    }
}
