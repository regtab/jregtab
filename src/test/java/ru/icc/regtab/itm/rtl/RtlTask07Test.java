package ru.icc.regtab.itm.rtl;

/**
 * Task 07: cross-table with three row-key columns, one column-header row,
 * and data cells binding three row keys plus one column key.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_07/}
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask07Test}
 * <pre>
 * [ []{3} [VAL]+ ]
 * [ [VAL]{3} [VAL : (SR{3}, SC)->REC(4)]+ ]+
 * </pre>
 * First row: three skipped cells then one-or-more column-header VALs. Data
 * rows: exactly three VAL row-key anchors followed by value cells each
 * producing REC(4) with providers SR{3} (3 same-subrow row-keys) and SC
 * (1 same-subcol col-key), performing a three-plus-one unpivot.
 */
public class RtlTask07Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "07"; }

    @Override
    protected String buildRtl() {
        return """
                [ []{3} [VAL]+ ]
                [ [VAL]{3} [VAL : (SR{3}, SC)->REC(4)]+ ]+
                """;
    }
}
