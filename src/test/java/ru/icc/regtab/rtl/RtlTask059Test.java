package ru.icc.regtab.rtl;

/**
 * Task 59: repeating three-slot rows — anchor VAL (RT-&gt;REC, single right), a VAL that
 * appends suffix from unbounded right siblings (RT*-&gt;SUFFIX(', ')), and one-or-more AUX cells.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_059/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask059Test}
 * <pre>
 * [ [VAL: RT-&gt;REC] [VAL: RT*-&gt;SUFFIX(', ')] [AUX]+ ]+
 * </pre>
 * The first cell anchors the record with its single right neighbour. The second cell
 * collects all right-of items as a comma-space-separated suffix. The trailing cells
 * provide auxiliary content.
 */
public class RtlTask059Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "059"; }

    @Override
    protected String buildRtl() {
        return """
               [ [VAL: RT->REC] [VAL: RT*->SUFFIX(', ')] [AUX]+ ]+
                """;
    }
}
