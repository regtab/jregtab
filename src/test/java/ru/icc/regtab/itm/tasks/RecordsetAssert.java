package ru.icc.regtab.itm.tasks;

import org.junit.jupiter.api.Assertions;
import ru.icc.regtab.itm.recordset.Record;
import ru.icc.regtab.itm.recordset.Recordset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Comparison of actual vs expected {@link Recordset} for tests.
 * <p>
 * Use {@link RecordsetMatchOptions} to control attribute order and row order policies.
 */
public final class RecordsetAssert {

    private RecordsetAssert() {}

    /**
     * Same as {@link #assertMatches(Recordset, Recordset, RecordsetMatchOptions)} with
     * {@link RecordsetMatchOptions#DEFAULT_STRICT}.
     */
    public static void assertMatches(Recordset actual, Recordset expected) {
        assertMatches(actual, expected, RecordsetMatchOptions.DEFAULT_STRICT);
    }

    /**
     * Asserts that {@code actual} matches {@code expected} under the given options.
     * <ul>
     *   <li><b>attributeOrder STRICT</b>: schema attribute lists must be equal in order.</li>
     *   <li><b>attributeOrder FLEXIBLE</b>: attribute names must match as sets; values compared by name.</li>
     *   <li><b>recordOrder STRICT</b>: row {@code i} compared to row {@code i}.</li>
     *   <li><b>recordOrder FLEXIBLE</b>: multiset of rows must match (order-independent).</li>
     * </ul>
     */
    public static void assertMatches(Recordset actual, Recordset expected, RecordsetMatchOptions opts) {
        List<String> expectedAttrs = expected.schema().attributes();
        Set<String> expSet = new HashSet<>(expectedAttrs);
        Set<String> actSet = new HashSet<>(actual.schema().attributes());

        if (opts.attributeOrder() == OrderPolicy.STRICT) {
            Assertions.assertEquals(
                    expectedAttrs,
                    actual.schema().attributes(),
                    "Schema (attribute order) mismatch");
        } else {
            Assertions.assertEquals(expSet, actSet, "Schema (attribute set) mismatch");
        }

        Assertions.assertEquals(expected.size(), actual.size(), "Record count mismatch");

        if (opts.recordOrder() == OrderPolicy.STRICT) {
            for (int i = 0; i < expected.size(); i++) {
                assertRecordEqual(expected.get(i), actual.get(i), expectedAttrs, i);
            }
        } else {
            List<String> sortedAttrs = new ArrayList<>(expSet);
            Collections.sort(sortedAttrs);
            assertMultisetEqual(expected, actual, sortedAttrs);
        }
    }

    private static void assertRecordEqual(Record exp, Record act, List<String> expectedAttrOrder, int index) {
        for (String attr : expectedAttrOrder) {
            Assertions.assertEquals(
                    normalize(exp.get(attr)),
                    normalize(act.get(attr)),
                    "Record " + index + ", attribute '" + attr + "'");
        }
    }

    private static void assertMultisetEqual(Recordset expected, Recordset actual, List<String> sortedAttrs) {
        Map<String, Integer> expCounts = new HashMap<>();
        Map<String, Integer> actCounts = new HashMap<>();
        for (int i = 0; i < expected.size(); i++) {
            bump(expCounts, rowFingerprint(expected.get(i), sortedAttrs));
            bump(actCounts, rowFingerprint(actual.get(i), sortedAttrs));
        }
        Assertions.assertEquals(expCounts, actCounts, "Record multiset mismatch (order-independent)");
    }

    private static void bump(Map<String, Integer> counts, String key) {
        counts.merge(key, 1, Integer::sum);
    }

    private static String rowFingerprint(Record r, List<String> sortedAttrs) {
        StringBuilder sb = new StringBuilder();
        for (String a : sortedAttrs) {
            sb.append(a).append('\u0001').append(normalize(r.get(a))).append('\u0002');
        }
        return sb.toString();
    }

    private static String normalize(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }
}
