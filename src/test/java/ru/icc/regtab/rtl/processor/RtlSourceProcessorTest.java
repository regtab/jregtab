package ru.icc.regtab.rtl.processor;

import org.junit.jupiter.api.Test;
import ru.icc.regtab.rtl.RtlSource;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Compiles synthetic Java sources with {@link RtlSourceProcessor} attached explicitly
 * via {@code CompilationTask.setProcessors} (independent of the JDK's implicit
 * annotation-processing defaults) and asserts the produced diagnostics.
 */
class RtlSourceProcessorTest {

    @Test
    void validRtlConstantCompiles() throws IOException {
        Result r = compile(classWithField(
                "@RtlSource static final String P = \"[ [ATTR] [VAL]+ ]\";"));
        assertTrue(r.success(), () -> "expected success, got: " + r.errors());
        assertEquals(0, r.errors().size());
    }

    @Test
    void syntaxErrorIsReportedAsCompilationError() throws IOException {
        Result r = compile(classWithField(
                "@RtlSource static final String P = \"[VAL\";"));
        assertFalse(r.success());
        assertTrue(r.errors().stream().anyMatch(d -> message(d).contains("Invalid RTL")),
                () -> "expected an 'Invalid RTL' error, got: " + r.errors());
    }

    @Test
    void semanticErrorIsReportedAsCompilationError() throws IOException {
        // Syntactically valid, but REC(0) and REC(1) declare conflicting anchor positions.
        Result r = compile(classWithField(
                "@RtlSource static final String P = \"[ [VAL : (ST)->REC(0), (ST)->REC(1)] ]\";"));
        assertFalse(r.success());
        assertTrue(r.errors().stream().anyMatch(d -> message(d).contains("Invalid RTL")),
                () -> "expected an 'Invalid RTL' error, got: " + r.errors());
    }

    @Test
    void extBindingIsSyntaxCheckedOnly() throws IOException {
        // Unbound EXT('T') would fail full compilation; the processor must fall back
        // to syntax-only validation and accept it.
        Result r = compile(classWithField(
                "@RtlSource static final String P = \"[ [EXT('T') ? VAL] ]\";"));
        assertTrue(r.success(), () -> "expected success, got: " + r.errors());
        assertEquals(0, r.errors().size());
    }

    @Test
    void brokenExtLiteralStillFailsSyntaxCheck() throws IOException {
        Result r = compile(classWithField(
                "@RtlSource static final String P = \"[EXT('T') ? VAL\";"));
        assertFalse(r.success());
        assertTrue(r.errors().stream().anyMatch(d -> message(d).contains("Invalid RTL")),
                () -> "expected an 'Invalid RTL' error, got: " + r.errors());
    }

    @Test
    void nonConstantFieldIsSkipped() throws IOException {
        // Non-final field carries no constant value: invalid RTL must not break the build.
        Result r = compile(classWithField(
                "@RtlSource static String p = \"[VAL\";"));
        assertTrue(r.success(), () -> "expected success, got: " + r.errors());
        assertEquals(0, r.errors().size());
    }

    // -------- helpers --------

    private record Result(boolean success, List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        List<Diagnostic<? extends JavaFileObject>> errors() {
            return diagnostics.stream()
                    .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
                    .toList();
        }
    }

    private static String message(Diagnostic<?> d) {
        return d.getMessage(null);
    }

    private static String classWithField(String fieldDecl) {
        return """
                import ru.icc.regtab.rtl.RtlSource;

                class Sample {
                    %s
                }
                """.formatted(fieldDecl);
    }

    private static Result compile(String source) throws IOException {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        Path out = Files.createTempDirectory("rtl-proc-test");
        try (StandardJavaFileManager fm =
                     javac.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
            fm.setLocationFromPaths(StandardLocation.CLASS_OUTPUT, List.of(out));
            fm.setLocationFromPaths(StandardLocation.CLASS_PATH, classpath());

            JavaFileObject file = new SimpleJavaFileObject(
                    URI.create("string:///Sample.java"), JavaFileObject.Kind.SOURCE) {
                @Override
                public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                    return source;
                }
            };

            JavaCompiler.CompilationTask task =
                    javac.getTask(null, fm, diagnostics, null, null, List.of(file));
            task.setProcessors(List.of(new RtlSourceProcessor()));
            boolean success = task.call();
            return new Result(success, diagnostics.getDiagnostics());
        } finally {
            deleteRecursively(out);
        }
    }

    /** Classpath for the compiled source: regtab classes, ANTLR runtime, JetBrains annotations. */
    private static List<Path> classpath() {
        return Stream.of(
                        RtlSource.class,
                        org.antlr.v4.runtime.Lexer.class,
                        org.intellij.lang.annotations.Language.class)
                .map(RtlSourceProcessorTest::codeSourceOf)
                .distinct()
                .toList();
    }

    private static Path codeSourceOf(Class<?> c) {
        try {
            return Path.of(c.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void deleteRecursively(Path root) throws IOException {
        try (Stream<Path> walk = Files.walk(root)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                    // best effort: leftover temp files are harmless
                }
            });
        }
    }
}
