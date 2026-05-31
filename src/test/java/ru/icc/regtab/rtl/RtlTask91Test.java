package ru.icc.regtab.rtl;

/**
 * Task 91: repeating rows with inline ATTR/VAL pairs; -LT->AVP (inherited) assigns
 * each VAL its nearest left ATTR as attribute name; ROW*->REC on the anchor VAL
 * collects all same-row VAL items into one record.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_91/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask91Test}
 * <pre>
 * { [ !BLANK? -LT-&gt;AVP [ATTR] [VAL: ROW*-&gt;REC] { [ATTR] [VAL] }+ ]+
 *   [ [BLANK?]+ ]? }+
 * </pre>
 */
public class RtlTask91Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "91"; }

    @Override
    protected String buildRtl() {
        return """
                { [ !BLANK? -LT->AVP [ATTR] [VAL: ROW*->REC] { [ATTR] [VAL] }+ ]+
                  [ [BLANK?]+ ]? }+
                """;
    }
}
