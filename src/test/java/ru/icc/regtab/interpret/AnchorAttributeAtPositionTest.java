package ru.icc.regtab.interpret;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.icc.regtab.recordset.Record;
import ru.icc.regtab.recordset.Recordset;
import ru.icc.regtab.recordset.Schema;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link AnchorAttributeAtPosition} moves the anchor <em>attribute</em> — name together with
 * values — to the requested position. The attribute-value binding of every record is preserved,
 * and the rule is the same for named and anonymous attributes: anonymous names are not
 * renumbered, they travel with their attribute.
 */
class AnchorAttributeAtPositionTest {

    @Test
    @DisplayName("Named attributes: the anchor attribute moves, values stay with their names")
    void namedAttributesKeepTheirValues() {
        Recordset in = recordset(
                List.of("Lokaler", "Dato", "Tid"),
                List.of("AU", "20.05.2019", "08.30-11.30"),
                List.of("A2.1", "11.06.2019", "0"));

        Recordset out = new AnchorAttributeAtPosition(2).apply(in);

        assertEquals(List.of("Dato", "Tid", "Lokaler"), out.schema().attributes());
        assertEquals(List.of("20.05.2019", "08.30-11.30", "AU"), values(out.get(0)));
        assertEquals(List.of("11.06.2019", "0", "A2.1"), values(out.get(1)));
    }

    @Test
    @DisplayName("Named attributes: position 1 inserts the anchor after the first attribute")
    void namedAttributesAtPositionOne() {
        Recordset in = recordset(
                List.of("Lokaler", "Dato", "Tid"),
                List.of("AU", "20.05.2019", "08.30-11.30"));

        Recordset out = new AnchorAttributeAtPosition(1).apply(in);

        assertEquals(List.of("Dato", "Lokaler", "Tid"), out.schema().attributes());
        assertEquals(List.of("20.05.2019", "AU", "08.30-11.30"), values(out.get(0)));
    }

    @Test
    @DisplayName("Anonymous attributes: names travel with the attribute, they are not renumbered")
    void anonymousAttributesAreNotRenumbered() {
        Recordset in = recordset(
                List.of("$a_1", "$a_2", "$a_3", "$a_4"),
                List.of("anchor", "v2", "v3", "v4"));

        Recordset out = new AnchorAttributeAtPosition(2).apply(in);

        assertEquals(List.of("$a_2", "$a_3", "$a_1", "$a_4"), out.schema().attributes());
        assertEquals("anchor", out.get(0).get("$a_1"));
        assertEquals("v2", out.get(0).get("$a_2"));
    }

    @Test
    @DisplayName("Anonymous attributes: the sequence of values by position is unchanged (regression)")
    void anonymousValueOrderMatchesLegacyBehaviour() {
        Recordset in = recordset(
                List.of("$a_1", "$a_2", "$a_3", "$a_4"),
                List.of("anchor", "v2", "v3", "v4"));

        Recordset out = new AnchorAttributeAtPosition(2).apply(in);

        // Header-less fixtures compare positionally: this order pins the 17 ANCH/REC(n) tasks.
        assertEquals(List.of("v2", "v3", "anchor", "v4"), values(out.get(0)));
    }

    @Test
    @DisplayName("Mixed schema: one rule for named and anonymous attributes alike")
    void mixedSchemaKeepsEveryName() {
        Recordset in = recordset(
                List.of("Lokaler", "$a_2", "Klasse"),
                List.of("AU", "v2", "0"));

        Recordset out = new AnchorAttributeAtPosition(1).apply(in);

        assertEquals(List.of("$a_2", "Lokaler", "Klasse"), out.schema().attributes());
        assertEquals(List.of("v2", "AU", "0"), values(out.get(0)));
    }

    @Test
    @DisplayName("Position 0, a position beyond the schema and a single attribute are no-ops")
    void degenerateCasesReturnTheInput() {
        Recordset three = recordset(List.of("a", "b", "c"), List.of("1", "2", "3"));
        assertSame(three, new AnchorAttributeAtPosition(0).apply(three));
        assertSame(three, new AnchorAttributeAtPosition(3).apply(three));
        assertSame(three, new AnchorAttributeAtPosition(7).apply(three));

        Recordset single = recordset(List.of("a"), List.of("1"));
        assertSame(single, new AnchorAttributeAtPosition(1).apply(single));
    }

    // --- helpers ---

    @SafeVarargs
    private static Recordset recordset(List<String> attributes, List<String>... rows) {
        Schema schema = new Schema(attributes);
        List<Record> records = new ArrayList<>(rows.length);
        for (List<String> row : rows) {
            Map<String, String> values = new LinkedHashMap<>();
            for (int i = 0; i < attributes.size(); i++) {
                values.put(attributes.get(i), row.get(i));
            }
            records.add(new Record(schema, values));
        }
        return new Recordset(schema, records);
    }

    private static List<String> values(Record r) {
        List<String> out = new ArrayList<>();
        for (String attr : r.schema().attributes()) {
            out.add(r.get(attr));
        }
        return out;
    }
}
