package ru.icc.regtab.conformance;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Regenerates the positive part of the RTL conformance corpus
 * ({@code conformance/positive/}) from the task test suite.
 *
 * <p>Run explicitly after changing task RTL strings:
 * <pre>
 * mvn test-compile org.codehaus.mojo:exec-maven-plugin:3.5.0:java \
 *     -Dexec.mainClass=ru.icc.regtab.conformance.ConformanceCorpusGenerator \
 *     -Dexec.classpathScope=test
 * </pre>
 * The generated files are committed to git; {@link ConformanceCorpusFreshnessTest}
 * fails the build when they drift from the task sources. Negative cases
 * ({@code conformance/negative/}) are maintained by hand and are not touched here.
 */
public final class ConformanceCorpusGenerator {

    private ConformanceCorpusGenerator() {}

    public static void main(String[] args) throws IOException {
        var sources = ConformanceCorpus.collectSources();
        List<String> failures = new ArrayList<>();
        int written = 0;

        for (var entry : sources) {
            String canonical;
            try {
                canonical = ConformanceCorpus.canonical(entry.rtl());
            } catch (RuntimeException e) {
                failures.add(entry.id() + ": " + e);
                continue;
            }
            ConformanceCorpus.write(ConformanceCorpus.sourceFile(entry.id()), entry.rtl());
            ConformanceCorpus.write(ConformanceCorpus.expectedFile(entry.id()), canonical);
            written++;
        }

        Files.createDirectories(ConformanceCorpus.ROOT);
        Files.writeString(ConformanceCorpus.ROOT.resolve("VERSION"),
                "generated: " + LocalDate.now() + "\nsources: RtlTask001..150 + curated extras\n",
                StandardCharsets.UTF_8);

        System.out.println("Conformance corpus: " + written + "/" + sources.size()
                + " positive pairs written to " + ConformanceCorpus.POSITIVE.toAbsolutePath());
        if (!failures.isEmpty()) {
            System.out.println("FAILURES (" + failures.size() + ") — serializer/compiler gaps:");
            failures.forEach(f -> System.out.println("  " + f));
            System.exit(1);
        }
    }
}
