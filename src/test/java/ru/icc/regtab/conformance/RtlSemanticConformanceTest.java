package ru.icc.regtab.conformance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import ru.icc.regtab.atp.AtpMatcher;
import ru.icc.regtab.atp.spec.TablePattern;
import ru.icc.regtab.interpret.SchemaConstructionStrategy;
import ru.icc.regtab.interpret.TableInterpreter;
import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.recordset.Recordset;
import ru.icc.regtab.rtl.RtlCompiler;
import ru.icc.regtab.tasks.CsvRecordsetLoader;
import ru.icc.regtab.tasks.CsvTableLoader;
import ru.icc.regtab.tasks.OrderPolicy;
import ru.icc.regtab.tasks.RecordsetAssert;
import ru.icc.regtab.tasks.RecordsetMatchOptions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Executable <em>semantic</em> contract of the RTL conformance corpus
 * (see {@code conformance/README.md}). Complements {@link RtlConformanceTest},
 * which pins syntax and canonical form only: two implementations can agree on the
 * canonical RTL of a pattern and still execute it differently.
 * <p>
 * For every {@code conformance/semantic/<case>/}, matching {@code pattern.rtl} against
 * {@code input.csv} and interpreting the result must yield {@code expected.csv}.
 */
class RtlSemanticConformanceTest {

    private static final String PATTERN  = "pattern.rtl";
    private static final String INPUT    = "input.csv";
    private static final String EXPECTED = "expected.csv";
    private static final String OPTIONS  = "options.json";

    /**
     * Semantic cases default to a header-less {@code expected.csv} compared positionally
     * against the schema the pattern produced, so that attribute names invented by the
     * implementation never leak into the contract. A case whose pattern names its
     * attributes (via AVP) can set {@code expectedHasHeader} in {@code options.json}.
     */
    private static final RecordsetMatchOptions DEFAULTS =
            new RecordsetMatchOptions(OrderPolicy.STRICT, OrderPolicy.STRICT, false);

    @TestFactory
    @DisplayName("Semantic corpus: pattern over input.csv yields expected.csv")
    Stream<DynamicTest> semantic() throws IOException {
        return caseDirs().map(dir -> DynamicTest.dynamicTest(dir.getFileName().toString(),
                () -> runCase(dir)));
    }

    @TestFactory
    @DisplayName("Semantic corpus: every case is complete")
    Stream<DynamicTest> everyCaseIsComplete() throws IOException {
        return caseDirs().map(dir -> DynamicTest.dynamicTest(dir.getFileName().toString(), () -> {
            for (String required : new String[]{PATTERN, INPUT, EXPECTED}) {
                assertTrue(Files.isRegularFile(dir.resolve(required)),
                        "missing " + required + " in " + dir);
            }
        }));
    }

    private static void runCase(Path dir) throws IOException {
        TableSyntax syntax = CsvTableLoader.load(dir.resolve(INPUT));
        TablePattern pattern = RtlCompiler.compile(
                Files.readString(dir.resolve(PATTERN), StandardCharsets.UTF_8));

        InterpretableTable itm = AtpMatcher.match(pattern, syntax)
                .orElseThrow(() -> new AssertionError(
                        "pattern did not match " + dir.getFileName() + "/" + INPUT));

        Recordset actual = pattern.transform(new TableInterpreter()
                .withStrategy(SchemaConstructionStrategy.RECORD_FIRST)
                .interpret(itm));

        RecordsetMatchOptions opts = loadOptions(dir);
        Path expectedPath = dir.resolve(EXPECTED);
        Recordset expected = opts.expectedHasHeader()
                ? CsvRecordsetLoader.load(expectedPath)
                : CsvRecordsetLoader.load(expectedPath, actual.schema());

        RecordsetAssert.assertMatches(actual, expected, opts);
    }

    /** Case directories, sorted for a stable test order. */
    private static Stream<Path> caseDirs() throws IOException {
        try (Stream<Path> entries = Files.list(ConformanceCorpus.SEMANTIC)) {
            return entries.filter(Files::isDirectory).sorted().toList().stream();
        }
    }

    private static RecordsetMatchOptions loadOptions(Path dir) throws IOException {
        Path file = dir.resolve(OPTIONS);
        if (!Files.isRegularFile(file)) {
            return DEFAULTS;
        }
        try (InputStream in = Files.newInputStream(file)) {
            CaseOptions patch = new ObjectMapper().readValue(in, CaseOptions.class);
            if (patch == null) {
                return DEFAULTS;
            }
            return new RecordsetMatchOptions(
                    policy(patch.attributeOrder, DEFAULTS.attributeOrder()),
                    policy(patch.recordOrder, DEFAULTS.recordOrder()),
                    patch.expectedHasHeader != null ? patch.expectedHasHeader : DEFAULTS.expectedHasHeader());
        }
    }

    private static OrderPolicy policy(String raw, OrderPolicy fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        return switch (raw.trim().toUpperCase(Locale.ROOT)) {
            case "STRICT"   -> OrderPolicy.STRICT;
            case "FLEXIBLE" -> OrderPolicy.FLEXIBLE;
            default -> throw new IllegalArgumentException(
                    "Unknown order policy: " + raw + " (use STRICT or FLEXIBLE)");
        };
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class CaseOptions {
        public String attributeOrder;
        public String recordOrder;
        public Boolean expectedHasHeader;
    }
}
