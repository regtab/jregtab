package ru.icc.regtab.conformance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the committed corpus against drift from the task test suite: regenerates
 * every positive pair in memory and compares with the files. On failure, rerun
 * {@link ConformanceCorpusGenerator} and commit the result.
 */
class ConformanceCorpusFreshnessTest {

    private static final String HINT =
            " — task RTL changed? Regenerate: see ConformanceCorpusGenerator javadoc";

    @TestFactory
    @DisplayName("Committed positive corpus matches the task sources")
    Stream<DynamicTest> positiveCorpusIsFresh() {
        return ConformanceCorpus.collectSources().stream()
                .map(entry -> DynamicTest.dynamicTest(entry.id(), () -> {
                    assertTrue(Files.exists(ConformanceCorpus.sourceFile(entry.id())),
                            "missing " + entry.id() + ".rtl" + HINT);
                    assertEquals(ConformanceCorpus.withTrailingNewline(entry.rtl()),
                            ConformanceCorpus.read(ConformanceCorpus.sourceFile(entry.id())),
                            entry.id() + ".rtl is stale" + HINT);
                    assertEquals(ConformanceCorpus.withTrailingNewline(
                                    ConformanceCorpus.canonical(entry.rtl())),
                            ConformanceCorpus.read(ConformanceCorpus.expectedFile(entry.id())),
                            entry.id() + ".expected.rtl is stale" + HINT);
                }));
    }

    @Test
    @DisplayName("No orphan files in the positive corpus")
    void noOrphans() throws IOException {
        Set<String> expected = ConformanceCorpus.collectSources().stream()
                .flatMap(e -> Stream.of(e.id() + ".rtl", e.id() + ".expected.rtl"))
                .collect(Collectors.toSet());
        try (Stream<java.nio.file.Path> files = Files.list(ConformanceCorpus.POSITIVE)) {
            Set<String> orphans = files
                    .map(p -> p.getFileName().toString())
                    .filter(n -> !expected.contains(n))
                    .collect(Collectors.toSet());
            assertTrue(orphans.isEmpty(), "orphan corpus files: " + orphans + HINT);
        }
    }
}
