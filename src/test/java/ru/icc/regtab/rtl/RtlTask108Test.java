package ru.icc.regtab.rtl;

/**
 * Task 108: table with repeating "A B C" column groups separated by blank columns and blank rows.
 * Header row creates ATTR items for COL->AVP; data rows use explicit subrows to
 * group each triple, with the middle cell as REC anchor gathering the right cell via RT->REC.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_108/}
 * <pre>
 *   [ [ATTR]+ ]
 * { COL->AVP
 *   [ { [!BLANK] [!BLANK? VAL: RT->REC] [!BLANK? VAL] [BLANK]* }+ ]
 *   [ [BLANK]+ ]*
 * }+
 * </pre>
 */
public class RtlTask108Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "108"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                  [ [ATTR]+ ]
                { COL->AVP
                  [ { [!BLANK] [!BLANK? VAL: RT->REC] [!BLANK? VAL] [BLANK]* }+ ]
                  [ [BLANK]+ ]*
                }+
                """;
    }
}
