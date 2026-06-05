package ru.icc.regtab.rtl;

import org.junit.jupiter.api.Test;
import ru.icc.regtab.atp.AtpMatcher;
import ru.icc.regtab.atp.spec.TablePattern;
import ru.icc.regtab.interpret.SchemaConstructionStrategy;
import ru.icc.regtab.interpret.TableInterpreter;
import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.recordset.Recordset;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RTL equivalent of {@link ru.icc.regtab.atp.AtpIllustrativeExampleTest}.
 * Implements the worked example from Section VI of the paper using RTL.
 * <p>
 * The RTL string encodes the same pattern as the ATP counterpart:
 * <pre>
 * [ [] [VAL: 'AIRLINE'-&gt;AVP]+ ]
 * [ [VAL: 'AIRPORT'-&gt;AVP] [VAL: (COL,ROW,CL)-&gt;REC, 'ND'-&gt;AVP ' ' VAL: 'MON'-&gt;AVP]+ ]+
 * </pre>
 */
class RtlIllustrativeExampleTest {

    private static final String RTL = """
            [ [] [VAL: 'AIRLINE'->AVP]+ ]
            [ [VAL: 'AIRPORT'->AVP] [VAL: (COL,ROW,CL)->REC, 'ND'->AVP ' ' VAL: 'MON'->AVP]+ ]+
            """;

    private TablePattern buildPattern() {
        return RtlCompiler.compile(RTL);
    }

    private static TableSyntax buildTable(String[][] data) {
        var syntax = new TableSyntax(data.length, data[0].length);
        for (int r = 0; r < data.length; r++)
            for (int c = 0; c < data[r].length; c++)
                syntax.getCell(r, c).setText(data[r][c]);
        return syntax;
    }

    @Test
    void paperExample_3x3_table_t0() {
        var syntax = buildTable(new String[][]{
                {"",    "CA",    "HU"    },
                {"IKT", "0 Jan", "8 Feb" },
                {"SVO", "31 Jan","40 Feb"}
        });

        Optional<InterpretableTable> result = AtpMatcher.match(buildPattern(), syntax);
        assertTrue(result.isPresent(), "Pattern must match table t_0");

        Recordset rs = new TableInterpreter()
                .withStrategy(SchemaConstructionStrategy.RECORD_FIRST)
                .interpret(result.get());

        assertEquals(List.of("ND", "AIRLINE", "AIRPORT", "MON"), rs.schema().attributes());
        assertEquals(4, rs.records().size());

        var r0 = rs.records().get(0);
        assertEquals("0",   r0.get("ND"));
        assertEquals("CA",  r0.get("AIRLINE"));
        assertEquals("IKT", r0.get("AIRPORT"));
        assertEquals("Jan", r0.get("MON"));

        var r1 = rs.records().get(1);
        assertEquals("8",   r1.get("ND"));
        assertEquals("HU",  r1.get("AIRLINE"));
        assertEquals("IKT", r1.get("AIRPORT"));
        assertEquals("Feb", r1.get("MON"));

        var r2 = rs.records().get(2);
        assertEquals("31",  r2.get("ND"));
        assertEquals("CA",  r2.get("AIRLINE"));
        assertEquals("SVO", r2.get("AIRPORT"));
        assertEquals("Jan", r2.get("MON"));

        var r3 = rs.records().get(3);
        assertEquals("40",  r3.get("ND"));
        assertEquals("HU",  r3.get("AIRLINE"));
        assertEquals("SVO", r3.get("AIRPORT"));
        assertEquals("Feb", r3.get("MON"));
    }

    @Test
    void extendedTable_4airlines_3airports() {
        var syntax = buildTable(new String[][]{
                {"",    "SU",     "S7",    "YC",    "U6"    },
                {"ARH", "11 Jan", "0 Jan", "0 Dec", "3 Feb" },
                {"IKT", "0 Jan",  "8 Feb", "5 Mar", "2 Apr" },
                {"SVO", "31 Jan", "40 Feb","27 Mar", "14 Apr"}
        });

        Optional<InterpretableTable> result = AtpMatcher.match(buildPattern(), syntax);
        assertTrue(result.isPresent(), "Pattern must match extended table");

        Recordset rs = new TableInterpreter()
                .withStrategy(SchemaConstructionStrategy.RECORD_FIRST)
                .interpret(result.get());

        assertEquals(List.of("ND", "AIRLINE", "AIRPORT", "MON"), rs.schema().attributes());
        assertEquals(12, rs.records().size());

        var r0 = rs.records().get(0);
        assertEquals("11",  r0.get("ND"));
        assertEquals("SU",  r0.get("AIRLINE"));
        assertEquals("ARH", r0.get("AIRPORT"));
        assertEquals("Jan", r0.get("MON"));

        var r11 = rs.records().get(11);
        assertEquals("14",  r11.get("ND"));
        assertEquals("U6",  r11.get("AIRLINE"));
        assertEquals("SVO", r11.get("AIRPORT"));
        assertEquals("Apr", r11.get("MON"));
    }

    @Test
    void malformedTable_bodyCell_missingDelimiter_fails() {
        var syntax = buildTable(new String[][]{
                {"",    "CA" },
                {"IKT", "0"  }
        });

        Optional<InterpretableTable> result = AtpMatcher.match(buildPattern(), syntax);
        assertTrue(result.isEmpty(), "Pattern must not match table with malformed body cell");
    }
}
