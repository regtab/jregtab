package ru.icc.regtab.rtl;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import ru.icc.regtab.atp.spec.TablePattern;
import ru.icc.regtab.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.interpret.DelimitedFieldSplit;
import ru.icc.regtab.interpret.RecordsetTransformation;
import ru.icc.regtab.interpret.WhitespaceNormalization;
import ru.icc.regtab.rtl.internal.ATPBuilder;

import java.util.ArrayList;
import java.util.LinkedHashSet;
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
    public static TablePattern compile(String rtl) {
        return compile(rtl, Bindings.of());
    }

    /**
     * Compiles the given RTL string, resolving {@code EXT('name')} constraints against
     * the supplied {@link Bindings}.
     *
     * @param rtl      the RTL source string
     * @param bindings named Java predicates for {@code EXT('name')} constraints
     * @return compiled table pattern
     * @throws RtlCompileException if the string cannot be parsed or compiled, or if it
     *                             references an {@code EXT} name absent from {@code bindings}
     */
    public static TablePattern compile(String rtl, Bindings bindings) {
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

        InlineRecParams inline = extractInlineRecParams(tree);
        List<RecordsetTransformation> transforms = buildTransformations(tree.settings(), inline);
        TablePattern tablePattern = new ATPBuilder(bindings).visitTablePattern(tree);
        if (transforms.isEmpty()) return tablePattern;
        return new TablePattern(tablePattern.condition(), tablePattern.subtablePatterns(), transforms);
    }

    private record InlineRecParams(Integer anchorPos, String splitDelimiter) {}

    private static InlineRecParams extractInlineRecParams(RTLParser.TablePatternContext tree) {
        List<Integer> anchors = new ArrayList<>();
        List<String>  splits  = new ArrayList<>();
        collectRecParams(tree, anchors, splits);
        if (new LinkedHashSet<>(anchors).size() > 1)
            throw new RtlCompileException("Conflicting REC(n) anchor positions: " + anchors);
        if (new LinkedHashSet<>(splits).size() > 1)
            throw new RtlCompileException("Conflicting REC('s') split delimiters: " + splits);
        return new InlineRecParams(
                anchors.isEmpty() ? null : anchors.get(0),
                splits.isEmpty()  ? null : splits.get(0));
    }

    private static void collectRecParams(org.antlr.v4.runtime.tree.ParseTree node,
                                         List<Integer> anchors, List<String> splits) {
        if (node instanceof RTLParser.RecOpContext rec) {
            if (rec.INT()    != null) anchors.add(Integer.parseInt(rec.INT().getText()));
            if (rec.STRING() != null) splits.add(stripQuotes(rec.STRING().getText()));
        }
        for (int i = 0; i < node.getChildCount(); i++)
            collectRecParams(node.getChild(i), anchors, splits);
    }

    private static List<RecordsetTransformation> buildTransformations(RTLParser.SettingsContext ctx,
                                                                       InlineRecParams inline) {
        List<RecordsetTransformation> result = new ArrayList<>();
        Integer settingAnchor = null;
        String  settingSplit  = null;

        if (ctx != null) {
            for (var s : ctx.setting()) {
                if (s.normSetting() != null) {
                    result.add(new WhitespaceNormalization());
                } else if (s.anchSetting() != null) {
                    settingAnchor = Integer.parseInt(s.anchSetting().INT().getText());
                    result.add(new AnchorAttributeAtPosition(settingAnchor));
                } else if (s.splitSetting() != null) {
                    settingSplit = stripQuotes(s.splitSetting().STRING().getText());
                    result.add(new DelimitedFieldSplit(settingSplit));
                } else {
                    throw new RtlCompileException("Unknown setting");
                }
            }
        }

        if (inline.anchorPos() != null) {
            if (settingAnchor != null && !settingAnchor.equals(inline.anchorPos()))
                throw new RtlCompileException(
                        "Conflicting ANCH(" + settingAnchor + ") and REC(" + inline.anchorPos() + ")");
            if (settingAnchor == null)
                result.add(new AnchorAttributeAtPosition(inline.anchorPos()));
        }

        if (inline.splitDelimiter() != null) {
            if (settingSplit != null && !settingSplit.equals(inline.splitDelimiter()))
                throw new RtlCompileException(
                        "Conflicting SPLIT(\"" + settingSplit + "\") and REC('" + inline.splitDelimiter() + "')");
            if (settingSplit == null)
                result.add(new DelimitedFieldSplit(inline.splitDelimiter()));
        }

        return List.copyOf(result);
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
