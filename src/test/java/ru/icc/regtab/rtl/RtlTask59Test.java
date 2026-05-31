package ru.icc.regtab.rtl;

/**
 * Task 59: repeating three-slot rows — anchor VAL (RT-&gt;REC, single right), a VAL that
 * appends suffix from unbounded right siblings (RT*-&gt;SUFFIX(', ')), and one-or-more AUX cells.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_59/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask59Test}
 * <pre>
 * [ [VAL: RT-&gt;REC] [VAL: RT*-&gt;SUFFIX(', ')] [AUX]+ ]+
 * </pre>
 * The first cell anchors the record with its single right neighbour. The second cell
 * collects all right-of items as a comma-space-separated suffix. The trailing cells
 * provide auxiliary content.
 */
public class RtlTask59Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "59"; }

    @Override
    protected String buildRtl() {
        return """
               [ [VAL: RT->REC] [VAL: RT*->SUFFIX(', ')] [AUX]+ ]+
                """;
    }
}
