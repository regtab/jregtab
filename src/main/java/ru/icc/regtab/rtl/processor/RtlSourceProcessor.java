package ru.icc.regtab.rtl.processor;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import ru.icc.regtab.rtl.RTLLexer;
import ru.icc.regtab.rtl.RTLParser;
import ru.icc.regtab.rtl.RtlCompileException;
import ru.icc.regtab.rtl.RtlCompiler;
import ru.icc.regtab.rtl.RtlSource;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates {@link RtlSource}-annotated compile-time {@code String} constants during
 * annotation processing: an invalid RTL literal is reported as a Java compilation error
 * attached to the annotated element.
 *
 * <p>Validation depth:
 * <ul>
 *   <li>literals containing {@code EXT(} are checked for <b>syntax only</b> — full
 *       compilation would require the {@link ru.icc.regtab.rtl.Bindings} that are
 *       supplied at runtime;</li>
 *   <li>all other literals are fully compiled via {@link RtlCompiler#compile(String)},
 *       which also catches semantic errors (provider templates, conflicting
 *       {@code REC(n)}/{@code REC('s')} parameters, etc.).</li>
 * </ul>
 *
 * <p>Only fields whose value is a compile-time constant are validated; other annotated
 * elements (parameters, methods, non-final fields) carry no constant value at this stage
 * and are skipped.
 *
 * <p>The processor is registered via {@code META-INF/services}. On JDK &le; 22 it runs
 * automatically when the regtab jar is on the compile classpath; since JDK 23 implicit
 * annotation processing is disabled — enable it explicitly ({@code -proc:full} or the
 * {@code <annotationProcessors>} configuration of maven-compiler-plugin).
 */
@SupportedAnnotationTypes("ru.icc.regtab.rtl.RtlSource")
public final class RtlSourceProcessor extends AbstractProcessor {

    // Conservative: an EXT(...) occurrence anywhere (even inside a regex literal)
    // downgrades validation to syntax-only rather than risking a false error.
    private static final Pattern CONTAINS_EXT = Pattern.compile("(?i)\\bEXT\\s*\\(");

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element instanceof VariableElement variable
                        && variable.getConstantValue() instanceof String rtl) {
                    validate(rtl, element);
                }
            }
        }
        return false;
    }

    private void validate(String rtl, Element element) {
        try {
            if (CONTAINS_EXT.matcher(rtl).find()) {
                checkSyntax(rtl);
            } else {
                RtlCompiler.compile(rtl);
            }
        } catch (RtlCompileException e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR, "Invalid RTL: " + e.getMessage(), element);
        } catch (RuntimeException e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.WARNING, "RTL validation failed unexpectedly: " + e, element);
        }
    }

    private static void checkSyntax(String rtl) {
        var lexer  = new RTLLexer(CharStreams.fromString(rtl));
        var tokens = new CommonTokenStream(lexer);
        var parser = new RTLParser(tokens);

        var listener = new FirstErrorListener();
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        parser.tablePattern();
        listener.throwIfAny();
    }

    private static final class FirstErrorListener extends BaseErrorListener {
        private RtlCompileException first;

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine,
                                String msg, RecognitionException e) {
            if (first == null) {
                first = new RtlCompileException(msg, line, charPositionInLine);
            }
        }

        void throwIfAny() {
            if (first != null) throw first;
        }
    }
}
