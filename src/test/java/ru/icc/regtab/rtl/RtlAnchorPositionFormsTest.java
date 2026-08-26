package ru.icc.regtab.rtl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.icc.regtab.atp.AtpMatcher;
import ru.icc.regtab.atp.spec.TablePattern;
import ru.icc.regtab.interpret.SchemaConstructionStrategy;
import ru.icc.regtab.interpret.TableInterpreter;
import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.recordset.Record;
import ru.icc.regtab.recordset.Recordset;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The anchor-position transformation is requested in three interchangeable ways — the
 * {@code <ANCH(n)>} settings prefix, inline {@code REC(n)} on an atomic content specification
 * and inline {@code REC(n)} inside a delimited one — and all three must produce the same
 * recordset. With named attributes (AVP) the anchor attribute moves together with its values:
 * the schema is reordered, the attribute-value binding of each record is not.
 *
 * @see ru.icc.regtab.interpret.AnchorAttributeAtPosition
 */
class RtlAnchorPositionFormsTest {

    /** Header row plus two data rows; the anchor is the middle column. */
    private static final String[][] TABLE = {
            {"Dato",  "Lokaler", "Klasse"},
            {"20.05", "AU",      "0"},
            {"11.06", "A2.1",    "1"},
    };

    private static final String SETTINGS_PREFIX = """
            <ANCH(1)>
            [ [ATTR]+ ]
            [ COL->AVP [VAL] [VAL: ROW*->REC] [VAL] ]+
            """;

    private static final String INLINE_ATOMIC = """
            [ [ATTR]+ ]
            [ COL->AVP [VAL] [VAL: ROW*->REC(1)] [VAL] ]+
            """;

    private static final String INLINE_DELIMITED = """
            [ [ATTR]+ ]
            [ COL->AVP [VAL] [(VAL: ROW*->REC(1)){','}] [VAL] ]+
            """;

    @Test
    @DisplayName("ANCH(n) moves the named anchor attribute, values stay under their own names")
    void settingsPrefixMovesTheAnchorAttribute() {
        Recordset rs = run(SETTINGS_PREFIX, TABLE);

        assertEquals(List.of("Dato", "Lokaler", "Klasse"), rs.schema().attributes());
        assertEquals(List.of("20.05", "AU", "0"), values(rs.get(0)));
        assertEquals(List.of("11.06", "A2.1", "1"), values(rs.get(1)));
    }

    @Test
    @DisplayName("All three forms of the transformation yield the same recordset")
    void allThreeFormsAgree() {
        Recordset viaSettings  = run(SETTINGS_PREFIX,  TABLE);
        Recordset viaAtomic    = run(INLINE_ATOMIC,    TABLE);
        Recordset viaDelimited = run(INLINE_DELIMITED, TABLE);

        assertEquals(dump(viaSettings), dump(viaAtomic),
                "inline REC(n) on an atomic content specification");
        assertEquals(dump(viaSettings), dump(viaDelimited),
                "inline REC(n) inside a delimited content specification");
    }

    @Test
    @DisplayName("REC(n) inside a delimited specification: one record per token, names intact")
    void inlineRecInsideDelimitedSpecification() {
        String[][] table = {
                {"Dato",  "Lokaler",  "Klasse"},
                {"20.05", "AU, C1.1", "0"},
                {"11.06", "A2.1",     "1"},
        };

        Recordset rs = run(INLINE_DELIMITED, table);

        assertEquals(List.of("Dato", "Lokaler", "Klasse"), rs.schema().attributes());
        assertEquals(3, rs.size());
        assertEquals(List.of("20.05", "AU", "0"), values(rs.get(0)));
        assertEquals(List.of("20.05", " C1.1", "0"), values(rs.get(1)));
        assertEquals(List.of("11.06", "A2.1", "1"), values(rs.get(2)));
    }

    // --- helpers ---

    private static Recordset run(String rtl, String[][] cells) {
        TableSyntax syntax = new TableSyntax(cells.length, cells[0].length);
        for (int row = 0; row < cells.length; row++) {
            for (int col = 0; col < cells[row].length; col++) {
                syntax.getCell(row, col).setText(cells[row][col]);
            }
        }
        TablePattern pattern = RtlCompiler.compile(rtl);
        InterpretableTable itm = AtpMatcher.match(pattern, syntax)
                .orElseThrow(() -> new AssertionError("pattern did not match:\n" + rtl));
        return pattern.transform(new TableInterpreter()
                .withStrategy(SchemaConstructionStrategy.RECORD_FIRST)
                .interpret(itm));
    }

    private static List<String> values(Record r) {
        List<String> out = new ArrayList<>();
        for (String attr : r.schema().attributes()) {
            out.add(r.get(attr));
        }
        return out;
    }

    /** Schema and records as text, so a mismatch shows what actually differs. */
    private static String dump(Recordset rs) {
        StringBuilder sb = new StringBuilder(rs.schema().attributes().toString()).append('\n');
        for (Record r : rs.records()) {
            sb.append(values(r)).append('\n');
        }
        return sb.toString();
    }
}
