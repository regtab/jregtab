package ru.icc.regtab.conformance;

import ru.icc.regtab.atp.spec.TablePattern;
import ru.icc.regtab.rtl.AtpToRtlSerializer;
import ru.icc.regtab.rtl.RtlCompiler;
import ru.icc.regtab.rtl.RtlTaskBase;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared logic of the RTL conformance corpus: source collection, canonicalization,
 * and file conventions. Used by {@link ConformanceCorpusGenerator},
 * {@link RtlConformanceTest}, and {@link ConformanceCorpusFreshnessTest}.
 *
 * <p>File layout (see {@code conformance/README.md}):
 * {@code conformance/positive/<id>.rtl} + {@code <id>.expected.rtl} (canonical form),
 * {@code conformance/negative/<name>.rtl} (must fail to compile),
 * {@code conformance/semantic/<case>/} (pattern + table + expected recordset).
 */
public final class ConformanceCorpus {

    public static final Path ROOT     = Path.of("conformance");
    public static final Path POSITIVE = ROOT.resolve("positive");
    public static final Path NEGATIVE = ROOT.resolve("negative");
    /** Hand-maintained cases pinning execution semantics; see {@code RtlSemanticConformanceTest}. */
    public static final Path SEMANTIC = ROOT.resolve("semantic");

    /** One positive source: corpus id and the RTL text. */
    public record Entry(String id, String rtl) {}

    /**
     * Curated non-task sources (docs examples). Kept in sync manually;
     * the freshness test guards the generated files against drift.
     */
    private static final Map<String, String> EXTRA_SOURCES = Map.of(
            "illustrative", /* language=RTL */ """
                    [ [] [VAL: 'AIRLINE'->AVP]+ ]
                    [ [VAL: 'AIRPORT'->AVP] [VAL: (COL,ROW,CL)->REC, 'ND'->AVP ' ' VAL: 'MON'->AVP]+ ]+
                    """,
            // Delimited content specification, both forms side by side: the bare form
            // splits verbatim (token whitespace and empty tokens are preserved), the
            // "=TRIM" form opts into trimming. See "Semantics of S_delim" in README.md.
            "delim_raw", /* language=RTL */ """
                    [ [(VAL : CL*->REC){','}] [(VAL=TRIM : CL*->REC){','}] ]
                    """,
            // Key K of CONCAT/JOIN given as positions and attribute names in any order and
            // with either quote style; the canonical form sorts positions, then names, and
            // uses single quotes. See semantic/concat_named_key and semantic/join_named_key.
            "named_key", /* language=RTL */ """
                    [ [] [] [ATTR]+ ]
                    [ [VAL: RT*->REC, (BW&STR)*->CONCAT("B", 'A', 1, 0)] [VAL] [VAL] [VAL: COL->AVP]{2} ]+
                    [ [VAL: COL->AVP, RT->REC, C2*->JOIN('k')] [VAL: COL->AVP] [VAL: COL->AVP, RT->REC] [VAL: COL->AVP] ]+
                    """,
            // Quantifier {n} with n < 2: {1} is equivalent to no quantifier, {0} to zero
            // occurrences. Both are kept verbatim in the canonical form. Execution of
            // zero-width subrows is pinned by semantic/subrow_zero_width_* and
            // semantic/quantifier_exactly_*.
            "quantifier_small_n", /* language=RTL */ """
                    [ [VAL]{1} [VAL]{0} { [BLANK]* }{1} ]
                    """
    );

    private ConformanceCorpus() {}

    /** All positive sources: RTL strings of tasks 001–150 plus curated extras. */
    public static List<Entry> collectSources() {
        List<Entry> result = new ArrayList<>();
        for (int i = 1; i <= 150; i++) {
            String num = String.format("%03d", i);
            RtlTaskBase task = instantiate("ru.icc.regtab.rtl.RtlTask" + num + "Test");
            if (task.rtl().isBlank())
                throw new IllegalStateException("Blank RTL in task " + num);
            result.add(new Entry("task_" + task.id(), task.rtl()));
        }
        new LinkedHashMap<>(EXTRA_SOURCES)
                .forEach((id, rtl) -> result.add(new Entry(id, rtl)));
        return result;
    }

    private static RtlTaskBase instantiate(String className) {
        try {
            var ctor = Class.forName(className).getDeclaredConstructor();
            ctor.setAccessible(true);
            return (RtlTaskBase) ctor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot instantiate " + className, e);
        }
    }

    /** Canonical form: {@code serialize(compile(rtl))}. */
    public static String canonical(String rtl) {
        TablePattern pattern = RtlCompiler.compile(rtl);
        return AtpToRtlSerializer.serialize(pattern);
    }

    public static Path sourceFile(String id)   { return POSITIVE.resolve(id + ".rtl"); }
    public static Path expectedFile(String id) { return POSITIVE.resolve(id + ".expected.rtl"); }

    /** Reads a corpus file (UTF-8). */
    public static String read(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Writes a corpus file (UTF-8, no BOM), ensuring a single trailing newline. */
    public static void write(Path file, String content) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, withTrailingNewline(content), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String withTrailingNewline(String s) {
        return s.endsWith("\n") ? s : s + "\n";
    }
}
