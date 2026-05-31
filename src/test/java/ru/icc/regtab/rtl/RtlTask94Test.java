package ru.icc.regtab.rtl;

/**
 * Task 94: single header row (groups separated by optional blank) above one-or-more
 * blank-separated data blocks; COL*-&gt;REC collects all same-column VALs regardless of subtable
 * boundaries, (ROW &amp; C+1.. &amp; STR)*-&gt;JOIN(0) merges sibling header columns into one record.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_94/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask94Test}
 * <pre>
 * [ { [!BLANK? VAL: COL*-&gt;REC, (ROW &amp; C+1.. &amp; STR)*-&gt;JOIN(0)]+ [BLANK?]? }+ ]
 * { [ { [!BLANK? VAL]+ [BLANK?]? }+ ]+
 *   [ [BLANK?]+ ]? }+
 * </pre>
 */
public class RtlTask94Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "94"; }

    @Override
    protected String buildRtl() {
        return """
                  [ { [!BLANK? VAL: COL*->REC, (ROW & C+1.. & STR)*->JOIN(0)]+ [BLANK?]? }+ ]
                { [ { [!BLANK? VAL]+ [BLANK?]? }+ ]+
                  [ [BLANK?]+ ]? }+
                """;
    }
}
