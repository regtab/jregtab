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
 * {@code conformance/negative/<name>.rtl} (must fail to compile).
 */
public final class ConformanceCorpus {

    public static final Path ROOT     = Path.of("conformance");
    public static final Path POSITIVE = ROOT.resolve("positive");
    public static final Path NEGATIVE = ROOT.resolve("negative");

    /** One positive source: corpus id and the RTL text. */
    public record Entry(String id, String rtl) {}

    /**
     * Curated non-task sources (docs examples). Kept in sync manually;
     * the freshness test guards the generated files against drift.
     */
    private static final Map<String, String> EXTRA_SOURCES = Map.of(
            "illustrative", """
                    [ [] [VAL: 'AIRLINE'->AVP]+ ]
                    [ [VAL: 'AIRPORT'->AVP] [VAL: (COL,ROW,CL)->REC, 'ND'->AVP ' ' VAL: 'MON'->AVP]+ ]+
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
