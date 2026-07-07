package ru.icc.regtab.conformance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import ru.icc.regtab.rtl.RtlCompileException;
import ru.icc.regtab.rtl.RtlCompiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Executable contract of the RTL conformance corpus (see {@code conformance/README.md}).
 * Any RTL implementation (the reference jRegTab compiler here, the pyRegTab parser
 * in its own suite) must satisfy exactly these checks:
 * <ol>
 *   <li>every {@code positive/<id>.rtl} compiles;</li>
 *   <li>its canonical form equals {@code positive/<id>.expected.rtl} byte-for-byte;</li>
 *   <li>the canonical form is a fixed point: {@code canonical(expected) == expected};</li>
 *   <li>every {@code negative/*.rtl} is rejected with a compile error.</li>
 * </ol>
 */
class RtlConformanceTest {

    @TestFactory
    @DisplayName("Positive corpus: compiles, canonical form matches, fixed point")
    Stream<DynamicTest> positive() throws IOException {
        return listFiles(ConformanceCorpus.POSITIVE)
                .filter(p -> p.getFileName().toString().endsWith(".rtl"))
                .filter(p -> !p.getFileName().toString().endsWith(".expected.rtl"))
                .map(source -> DynamicTest.dynamicTest(idOf(source), () -> {
                    String id = idOf(source);
                    String rtl = ConformanceCorpus.read(source);
                    String expected = ConformanceCorpus.read(ConformanceCorpus.expectedFile(id));

                    String canonical = ConformanceCorpus.withTrailingNewline(
                            ConformanceCorpus.canonical(rtl));
                    assertEquals(expected, canonical, "canonical(" + id + ".rtl)");

                    String fixedPoint = ConformanceCorpus.withTrailingNewline(
                            ConformanceCorpus.canonical(expected));
                    assertEquals(expected, fixedPoint, "canonical form of " + id + " is not a fixed point");
                }));
    }

    @TestFactory
    @DisplayName("Negative corpus: every case is rejected with RtlCompileException")
    Stream<DynamicTest> negative() throws IOException {
        return listFiles(ConformanceCorpus.NEGATIVE)
                .filter(p -> p.getFileName().toString().endsWith(".rtl"))
                .map(source -> DynamicTest.dynamicTest(idOf(source), () -> {
                    String rtl = ConformanceCorpus.read(source);
                    assertThrows(RtlCompileException.class, () -> RtlCompiler.compile(rtl),
                            "negative case must be rejected: " + source.getFileName());
                }));
    }

    private static Stream<Path> listFiles(Path dir) throws IOException {
        return Files.list(dir).sorted();
    }

    private static String idOf(Path source) {
        String name = source.getFileName().toString();
        return name.substring(0, name.length() - ".rtl".length());
    }
}
