package ru.icc.regtab.itm.rtl;

/**
 * Task 02: repeated subtables with two normalised header rows, one-or-more
 * data rows, and an optional blank-row footer.
 * <p>
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask02Test}
 * <pre>
 * { [ [VAL=NORM] [] ]{2}
 *   [ [!BLANK ? VAL : (SC{2}, SR)->REC(2)] [VAL] ]+
 *   [ [BLANK?]  [] ]? }+
 * </pre>
 * Header rows: 2 repetitions, VAL with whitespace normalisation. Data rows:
 * non-blank anchor with REC(2) — providers SC{2} (2 attrs from same subcol)
 * and SR (1 val from same subrow). Footer: optional blank-guard row.
 */
public class RtlTask02Test extends RtlTaskBase {

    @Override
    protected String taskId() {
        return "02";
    }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL=NORM] [] ]{2}
                  [ [!BLANK ? VAL : (SC{2}, SR)->REC(2)] [VAL] ]+
                  [ [BLANK?] [] ]? }+
                """;
    }
}
