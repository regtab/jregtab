package ru.icc.regtab.itm.rtl;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import ru.icc.regtab.itm.atp.spec.TablePattern;
import ru.icc.regtab.itm.rtl.internal.ATPBuilder;

/**
 * Compiles an RTL (Regular Table Language) string into a {@link TablePattern}.
 *
 * <p>Usage:
 * <pre>{@code
 * TablePattern p = RtlCompiler.compile("""
 *     [ [SKIP] [VAL : ('AIRLINE')->AVP]+ ]
 *     [ [VAL : ('AIRPORT')->AVP] [VAL : (UW{1})->REC, ('ND')->AVP]+ ]+
 * """);
 * }</pre>
 */
public final class RtlCompiler {

    private RtlCompiler() {}

    /**
     * Compiles the given RTL string into a {@link TablePattern}.
     *
     * @param rtl the RTL source string
     * @return compiled table pattern
     * @throws RtlCompileException if the string cannot be parsed or compiled
     */
    public static TablePattern compile(String rtl) {
        var lexer  = new RTLLexer(CharStreams.fromString(rtl));
        var tokens = new CommonTokenStream(lexer);
        var parser = new RTLParser(tokens);

        var errors = new ErrorCollector();
        lexer.removeErrorListeners();
        lexer.addErrorListener(errors);
        parser.removeErrorListeners();
        parser.addErrorListener(errors);

        var tree = parser.tablePattern();
        errors.throwIfAny();

        return new ATPBuilder().visitTablePattern(tree);
    }

    private static final class ErrorCollector extends BaseErrorListener {
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
