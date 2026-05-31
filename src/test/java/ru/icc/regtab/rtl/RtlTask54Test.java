package ru.icc.regtab.rtl;

/**
 * Task 54: flat table with repeating subrow groups — header subrows (skip + non-blank VALs)
 * and data subrows (VAL anchor + non-blank VALs collecting same-subcol and same-subrow items
 * into REC). Each subrow ends with an optional blank trailing cell.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_54/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask54Test}
 * <pre>
 * [ { []    [!BLANK? VAL]+               [BLANK?]? }+ ]
 * [ { [VAL] [!BLANK? VAL: (SC,SR)-&gt;REC]+ [BLANK?]? }+ ]+
 * </pre>
 * The first subtable groups header subrows: a skip cell followed by one or more non-blank
 * VAL header labels. Repeating data subtables: the first cell is a VAL anchor; subsequent
 * non-blank VAL cells are linked to the anchor via same-subcol (SC) and same-subrow (SR).
 */
public class RtlTask54Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "54"; }

    @Override
    protected String buildRtl() {
        return """
                [ { []    [!BLANK? VAL]+               [BLANK?]? }+ ]
                [ { [VAL] [!BLANK? VAL: (SC,SR)->REC]+ [BLANK?]? }+ ]+
                """;
    }
}
