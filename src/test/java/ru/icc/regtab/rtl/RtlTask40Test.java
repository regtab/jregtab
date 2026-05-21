package ru.icc.regtab.rtl;

/**
 * Task 40: repeated crime-report subtables — title row identified by glob
 * pattern, text-cleaned anchor, header skip row, five ATTR/VAL data rows,
 * and an optional trailing skip row.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_40/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask40Test}
 * <pre>
 * { [ [~'Reported crime in'? VAL = REPL('Reported crime in', '').TRIM : ''->AVP, (ST & C1)*->REC] [] ]
 *   [ []{2} ]
 *   [ [ATTR] [VAL : SR->AVP] ]{5}
 *   [ []{2} ]? }+
 * </pre>
 * Title row: cell matching glob 'Reported crime in', value trimmed of the prefix
 * text, with empty-literal AVP and unbounded REC over same-subtable column 1
 * (ST & C1); plus a skip cell. Then a two-cell skip row. Exactly 5 data rows
 * each with an ATTR and a VAL using SR->AVP (same-subrow attribute). Optional
 * two-cell footer skip row.
 */
public class RtlTask40Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "40"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [~'Reported crime in'? VAL = REPL('Reported crime in', '').TRIM : ''->AVP, (ST & C1)*->REC] [] ]
                  [ []{2} ]
                  [ [ATTR] [VAL : SR->AVP] ]{5}
                  [ []{2} ]? }+
                """;
    }
}
