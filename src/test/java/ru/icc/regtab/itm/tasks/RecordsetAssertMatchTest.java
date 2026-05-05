package ru.icc.regtab.itm.tasks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.opentest4j.AssertionFailedError;
import ru.icc.regtab.itm.recordset.Record;
import ru.icc.regtab.itm.recordset.Recordset;
import ru.icc.regtab.itm.recordset.Schema;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecordsetAssertMatchTest {

    @Test
    void strictAttributeOrder_rejectsDifferentColumnOrder() {
        Recordset expected = rs(
                List.of("a", "b"),
                List.of(Map.of("a", "1", "b", "2")));
        Recordset actual = rs(
                List.of("b", "a"),
                List.of(Map.of("a", "1", "b", "2")));
        assertThrows(AssertionFailedError.class,
                () -> RecordsetAssert.assertMatches(actual, expected, RecordsetMatchOptions.DEFAULT_STRICT));
    }

    @Test
    void flexibleAttributeOrder_acceptsDifferentColumnOrder() {
        Recordset expected = rs(
                List.of("a", "b"),
                List.of(Map.of("a", "1", "b", "2")));
        Recordset actual = rs(
                List.of("b", "a"),
                List.of(Map.of("a", "1", "b", "2")));
        var opts = new RecordsetMatchOptions(OrderPolicy.FLEXIBLE, OrderPolicy.STRICT);
        assertDoesNotThrow(() -> RecordsetAssert.assertMatches(actual, expected, opts));
    }

    @Test
    void strictRecordOrder_rejectsPermutedRows() {
        Recordset expected = rs(
                List.of("a"),
                List.of(Map.of("a", "1"), Map.of("a", "2")));
        Recordset actual = rs(
                List.of("a"),
                List.of(Map.of("a", "2"), Map.of("a", "1")));
        assertThrows(AssertionFailedError.class,
                () -> RecordsetAssert.assertMatches(actual, expected, RecordsetMatchOptions.DEFAULT_STRICT));
    }

    @Test
    void flexibleRecordOrder_acceptsPermutedRows() {
        Recordset expected = rs(
                List.of("a"),
                List.of(Map.of("a", "1"), Map.of("a", "2")));
        Recordset actual = rs(
                List.of("a"),
                List.of(Map.of("a", "2"), Map.of("a", "1")));
        var opts = new RecordsetMatchOptions(OrderPolicy.STRICT, OrderPolicy.FLEXIBLE);
        assertDoesNotThrow(() -> RecordsetAssert.assertMatches(actual, expected, opts));
    }

    @Test
    void taskMatchOptionsLoader_readsFlatJson(@TempDir Path tmp) throws IOException {
        String json = """
                {
                  "attributeOrder": "FLEXIBLE",
                  "recordOrder": "STRICT"
                }
                """;
        Files.writeString(tmp.resolve("task_match_options.json"), json);

        RecordsetMatchOptions o = TaskMatchOptionsLoader.load(tmp);
        assertEquals(OrderPolicy.FLEXIBLE, o.attributeOrder());
        assertEquals(OrderPolicy.STRICT, o.recordOrder());
    }

    @Test
    void taskMatchOptionsLoader_perTaskFile_overridesGlobal(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("task_match_options.json"), """
                {
                  "attributeOrder": "STRICT",
                  "recordOrder": "STRICT"
                }
                """);
        Path taskDir = Files.createDirectories(tmp.resolve("task_05"));
        Files.writeString(taskDir.resolve("task_match_options.json"), """
                {
                  "attributeOrder": "FLEXIBLE"
                }
                """);

        RecordsetMatchOptions o = TaskMatchOptionsLoader.load(tmp, "05");
        assertEquals(OrderPolicy.FLEXIBLE, o.attributeOrder());
        assertEquals(OrderPolicy.STRICT, o.recordOrder());
    }

    @Test
    void taskMatchOptionsLoader_rootTasksMap_overridesBeforeTaskFile(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("task_match_options.json"), """
                {
                  "attributeOrder": "STRICT",
                  "recordOrder": "STRICT",
                  "tasks": {
                    "07": { "recordOrder": "FLEXIBLE" }
                  }
                }
                """);

        RecordsetMatchOptions o = TaskMatchOptionsLoader.load(tmp, "07");
        assertEquals(OrderPolicy.STRICT, o.attributeOrder());
        assertEquals(OrderPolicy.FLEXIBLE, o.recordOrder());
    }

    private static Recordset rs(List<String> attrs, List<Map<String, String>> rows) {
        Schema schema = new Schema(attrs);
        List<Record> list = rows.stream()
                .map(m -> new Record(schema, new LinkedHashMap<>(m)))
                .toList();
        return new Recordset(schema, list);
    }
}
