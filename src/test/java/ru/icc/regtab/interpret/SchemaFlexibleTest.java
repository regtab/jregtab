package ru.icc.regtab.interpret;

import org.junit.jupiter.api.Test;
import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.semantics.TableSemantics;
import ru.icc.regtab.itm.semantics.action.InterpretationAction;
import ru.icc.regtab.itm.semantics.item.*;
import ru.icc.regtab.itm.semantics.operation.*;
import ru.icc.regtab.itm.semantics.provider.*;
import ru.icc.regtab.itm.syntax.*;
import ru.icc.regtab.recordset.Record;
import ru.icc.regtab.recordset.Recordset;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reproduces Example 3 from the paper: schema-flexible case with anonymous attributes
 * and O_concat (adapted from Foofah's test tasks).
 *
 * Table (5 rows x 3 cols):
 * Anna | Math    | 43
 * Anna | French  | 78
 * Bob  | English | 96
 * Bob  | French  | 54
 * Joan | English | 79
 *
 * Expected schema: S = <$a_1, Math, French, English>
 * Expected records (3, after O_concat merges same-name anchors):
 *   Anna: Math=43, French=78, English=null
 *   Bob:  Math=null, French=54, English=96
 *   Joan: Math=null, French=null, English=79
 */
class SchemaFlexibleTest {

    @Test
    void testSchemaFlexible() {
        TableSyntax syntax = new TableSyntax(5, 3);
        syntax.getCell(0, 0).setText("Anna");  syntax.getCell(0, 1).setText("Math");    syntax.getCell(0, 2).setText("43");
        syntax.getCell(1, 0).setText("Anna");  syntax.getCell(1, 1).setText("French");  syntax.getCell(1, 2).setText("78");
        syntax.getCell(2, 0).setText("Bob");   syntax.getCell(2, 1).setText("English"); syntax.getCell(2, 2).setText("96");
        syntax.getCell(3, 0).setText("Bob");   syntax.getCell(3, 1).setText("French");  syntax.getCell(3, 2).setText("54");
        syntax.getCell(4, 0).setText("Joan");  syntax.getCell(4, 1).setText("English"); syntax.getCell(4, 2).setText("79");

        CellDerivedItem[] names = new CellDerivedItem[5];
        CellDerivedItem[] subjects = new CellDerivedItem[5];
        CellDerivedItem[] scores = new CellDerivedItem[5];

        for (int i = 0; i < 5; i++) {
            names[i] = new CellDerivedItem(syntax.getCell(i, 0).text(), 0, syntax.getCell(i, 0), ItemType.VALUE);
            subjects[i] = new CellDerivedItem(syntax.getCell(i, 1).text(), 0, syntax.getCell(i, 1), ItemType.ATTRIBUTE);
            scores[i] = new CellDerivedItem(syntax.getCell(i, 2).text(), 0, syntax.getCell(i, 2), ItemType.VALUE);
        }

        Set<CellDerivedItem> allCdi = new LinkedHashSet<>();
        allCdi.addAll(List.of(names));
        allCdi.addAll(List.of(subjects));
        allCdi.addAll(List.of(scores));

        List<InterpretationAction> actions = new ArrayList<>();

        // O_avp: associate each score with its subject attribute
        for (int i = 0; i < 5; i++) {
            CellDerivedItem subj = subjects[i];
            actions.add(new InterpretationAction(scores[i],
                    List.of(new CellDerivedItemProvider(
                            (a, cand) -> cand == subj, TraversalOrder.ROW_MAJOR,
                            allCdi, 1)),
                    new AvpOperation()));
        }

        // O_rec: create item-based record for each name with its score
        for (int i = 0; i < 5; i++) {
            CellDerivedItem score = scores[i];
            actions.add(new InterpretationAction(names[i],
                    List.of(new CellDerivedItemProvider(
                            (a, cand) -> cand == score, TraversalOrder.ROW_MAJOR,
                            allCdi, 1)),
                    new RecOperation()));
        }

        // O_concat: for each name-item, merge records with same value below
        ItemFilterCondition sameNameBelow = (anchor, cand) ->
                cand.below(anchor).sameCol() && cand.sameStr(anchor);
        for (int i = 0; i < 5; i++) {
            actions.add(new InterpretationAction(names[i],
                    List.of(new CellDerivedItemProvider(
                            sameNameBelow, TraversalOrder.ROW_MAJOR,
                            allCdi)),
                    new JoinOperation(Set.of(0))));
        }

        TableSemantics semantics = new TableSemantics(allCdi, Set.of(), actions);
        InterpretableTable itm = new InterpretableTable(syntax, semantics);

        Recordset result = new TableInterpreter()
                .withStrategy(SchemaConstructionStrategy.RECORD_FIRST)
                .interpret(itm);

        List<String> schema = result.schema().attributes();
        assertEquals(4, schema.size());
        assertEquals("$a_1", schema.get(0));
        assertTrue(schema.contains("Math"));
        assertTrue(schema.contains("French"));
        assertTrue(schema.contains("English"));

        assertEquals(3, result.size());

        Record anna = result.records().stream()
                .filter(r -> "Anna".equals(r.get("$a_1")))
                .findFirst().orElseThrow();
        assertEquals("43", anna.get("Math"));
        assertEquals("78", anna.get("French"));
        assertNull(anna.get("English"));

        Record bob = result.records().stream()
                .filter(r -> "Bob".equals(r.get("$a_1")))
                .findFirst().orElseThrow();
        assertNull(bob.get("Math"));
        assertEquals("54", bob.get("French"));
        assertEquals("96", bob.get("English"));

        Record joan = result.records().stream()
                .filter(r -> "Joan".equals(r.get("$a_1")))
                .findFirst().orElseThrow();
        assertNull(joan.get("Math"));
        assertNull(joan.get("French"));
        assertEquals("79", joan.get("English"));
    }

    @Test
    void testCustomAnonymousAttributeTemplate() {
        TableSyntax syntax = new TableSyntax(5, 3);
        syntax.getCell(0, 0).setText("Anna");  syntax.getCell(0, 1).setText("Math");    syntax.getCell(0, 2).setText("43");
        syntax.getCell(1, 0).setText("Anna");  syntax.getCell(1, 1).setText("French");  syntax.getCell(1, 2).setText("78");
        syntax.getCell(2, 0).setText("Bob");   syntax.getCell(2, 1).setText("English"); syntax.getCell(2, 2).setText("96");
        syntax.getCell(3, 0).setText("Bob");   syntax.getCell(3, 1).setText("French");  syntax.getCell(3, 2).setText("54");
        syntax.getCell(4, 0).setText("Joan");  syntax.getCell(4, 1).setText("English"); syntax.getCell(4, 2).setText("79");

        CellDerivedItem[] names = new CellDerivedItem[5];
        CellDerivedItem[] subjects = new CellDerivedItem[5];
        CellDerivedItem[] scores = new CellDerivedItem[5];
        for (int i = 0; i < 5; i++) {
            names[i] = new CellDerivedItem(syntax.getCell(i, 0).text(), 0, syntax.getCell(i, 0), ItemType.VALUE);
            subjects[i] = new CellDerivedItem(syntax.getCell(i, 1).text(), 0, syntax.getCell(i, 1), ItemType.ATTRIBUTE);
            scores[i] = new CellDerivedItem(syntax.getCell(i, 2).text(), 0, syntax.getCell(i, 2), ItemType.VALUE);
        }

        Set<CellDerivedItem> allCdi = new LinkedHashSet<>();
        allCdi.addAll(List.of(names));
        allCdi.addAll(List.of(subjects));
        allCdi.addAll(List.of(scores));

        List<InterpretationAction> actions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            CellDerivedItem subj = subjects[i];
            actions.add(new InterpretationAction(scores[i],
                    List.of(new CellDerivedItemProvider(
                            (a, cand) -> cand == subj, TraversalOrder.ROW_MAJOR,
                            allCdi, 1)),
                    new AvpOperation()));
        }
        for (int i = 0; i < 5; i++) {
            CellDerivedItem score = scores[i];
            actions.add(new InterpretationAction(names[i],
                    List.of(new CellDerivedItemProvider(
                            (a, cand) -> cand == score, TraversalOrder.ROW_MAJOR,
                            allCdi, 1)),
                    new RecOperation()));
        }
        ItemFilterCondition sameNameBelow = (anchor, cand) ->
                cand.below(anchor).sameCol() && cand.sameStr(anchor);
        for (int i = 0; i < 5; i++) {
            actions.add(new InterpretationAction(names[i],
                    List.of(new CellDerivedItemProvider(
                            sameNameBelow, TraversalOrder.ROW_MAJOR,
                            allCdi)),
                    new JoinOperation(Set.of(0))));
        }

        TableSemantics semantics = new TableSemantics(allCdi, Set.of(), actions);
        InterpretableTable itm = new InterpretableTable(syntax, semantics);

        Recordset result = new TableInterpreter()
                .withStrategy(SchemaConstructionStrategy.RECORD_FIRST)
                .withAnonymousAttributeTemplate("A%i")
                .interpret(itm);

        assertEquals("A1", result.schema().attributes().get(0));
        assertEquals("Anna", result.records().stream()
                .filter(r -> "Anna".equals(r.get("A1")))
                .findFirst().orElseThrow().get("A1"));
    }
}
