package ru.icc.regtab.rtl;

/**
 * Task 02: repeated subtables with two normalised header rows, one-or-more
 * data rows, and an optional blank-row footer.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_002/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask002Test}
 * <pre>
 * { [ [VAL=NORM] [] ]{2}
 *   [ [!BLANK ? VAL : (SC{2}, SR)->REC(2)] [VAL] ]+
 *   [ [BLANK]  [] ]? }+
 * </pre>
 * Header rows: 2 repetitions, VAL with whitespace normalisation. Data rows:
 * non-blank anchor with REC(2) — providers SC{2} (2 attrs from same subcol)
 * and SR (1 val from same subrow). Footer: optional blank-guard row.
 */
public class RtlTask002Test extends RtlTaskBase {

    @Override
    protected String taskId() {
        return "002";
    }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                { [ [VAL=NORM] [] ]{2}
                  [ [!BLANK ? VAL : (SC{2}, SR)->REC(2)] [VAL] ]+
                  [ [BLANK] [] ]? }+
                """;
    }
}
