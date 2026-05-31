package ru.icc.regtab.rtl;

/**
 * Task 99: each cell is a compound "KEY=VAL\r\nKEY=VAL\r\nKEY=VAL" value —
 * ATTRs name the VALs via AVP; the first VAL anchors the record via CL*->REC.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_99/}
 * ATP spec: {@link ru.icc.regtab.atp.AtpTask99Test}
 * <pre>
 * [ [ATTR '=' VAL: (CL&P0)->AVP, CL*->REC '\r\n'
 *    ATTR '=' VAL: (CL&P2)->AVP '\r\n'
 *    ATTR '=' VAL: (CL&P4)->AVP]+ ]+
 * </pre>
 * The separators '\r\n' contain actual CR+LF bytes, matching the cell content
 * of input_*.csv where fields use CRLF as the internal line separator.
 */
public class RtlTask99Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "99"; }

    @Override
    protected String buildRtl() {
        return "[ [ATTR '=' VAL: (CL&P0)->AVP, CL*->REC '\r\n' ATTR '=' VAL: (CL&P2)->AVP '\r\n' ATTR '=' VAL: (CL&P4)->AVP]+ ]+";
    }
}
