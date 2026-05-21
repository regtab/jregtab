package ru.icc.regtab.itm.rtl;

/**
 * Task 45: flat table where each row has a non-blank anchor cell and a non-blank
 * delimited cell whose comma-separated values each reference same-subrow column 0.
 * <p>
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask45Test}
 * <pre>
 * [ [!BLANK? VAL] [!BLANK? (VAL : (SR & C0)->REC(1)){','}] ]+
 * </pre>
 * Each data row: a non-blank plain VAL anchor, then a non-blank delimited cell
 * where each comma-separated token is a VAL with REC(1) using provider SR & C0
 * (same-subrow column 0), binding the row-key anchor to every delimited value.
 */
public class RtlTask45Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "45"; }

    @Override
    protected String buildRtl() {
        return """
                [ [!BLANK? VAL] [!BLANK? (VAL : (SR & C0)->REC(1)){','}] ]+
                """;
    }
}
