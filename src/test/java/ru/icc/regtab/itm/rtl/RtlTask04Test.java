package ru.icc.regtab.itm.rtl;

/**
 * Task 04: one skip row followed by data rows with a row-key anchor and
 * one-or-more value cells referencing it via same-subrow.
 * <p>
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask04Test}
 * <pre>
 * [ []+ ]
 * [ [VAL] [VAL : SR->REC(1)]+ ]+
 * </pre>
 * The first row skips one-or-more cells. Subsequent data rows begin with a
 * plain VAL anchor; each following cell uses REC(1) with provider SR (same
 * subrow) to attach the row-key to the value.
 */
public class RtlTask04Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "04"; }

    @Override
    protected String buildRtl() {
        return """
                [ []+ ]
                [ [VAL] [VAL : SR->REC(1)]+ ]+
                """;
    }
}
