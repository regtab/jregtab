package ru.icc.regtab.itm.rtl;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import ru.icc.regtab.itm.atp.spec.TablePattern;
import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.interpret.DelimitedFieldSplit;
import ru.icc.regtab.itm.interpret.RecordsetTransformation;
import ru.icc.regtab.itm.interpret.WhitespaceNormalization;
import ru.icc.regtab.itm.rtl.internal.ATPBuilder;

import java.util.List;

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
    public static RtlProgram compile(String rtl) {
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

        List<RecordsetTransformation> transforms = buildTransformations(tree.settings());
        TablePattern tablePattern = new ATPBuilder().visitTablePattern(tree);
        return new RtlProgram(tablePattern, transforms);
    }

    private static List<RecordsetTransformation> buildTransformations(RTLParser.SettingsContext ctx) {
        if (ctx == null) return List.of();
        return ctx.setting().stream().map(RtlCompiler::buildSetting).toList();
    }

    private static RecordsetTransformation buildSetting(RTLParser.SettingContext ctx) {
        if (ctx.normSetting()  != null) return new WhitespaceNormalization();
        if (ctx.anchSetting()  != null)
            return new AnchorAttributeAtPosition(Integer.parseInt(ctx.anchSetting().INT().getText()));
        if (ctx.splitSetting() != null)
            return new DelimitedFieldSplit(stripQuotes(ctx.splitSetting().STRING().getText()));
        throw new RtlCompileException("Unknown setting");
    }

    private static String stripQuotes(String s) {
        return s.substring(1, s.length() - 1);
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
