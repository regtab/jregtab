package ru.icc.regtab.rtl.internal;

import ru.icc.regtab.atp.spec.StringExtractor;
import ru.icc.regtab.rtl.RTLParser;

import java.util.List;

/** Builds a {@link StringExtractor} from an RTL {@code strExtr} parse context. */
final class StringExtractorFactory {

    private StringExtractorFactory() {}

    static StringExtractor from(RTLParser.StrExtrContext ctx) {
        List<RTLParser.StrExtrStepContext> steps = ctx.strExtrStep();
        if (steps.size() == 1) return fromStep(steps.get(0));
        List<StringExtractor> chain = steps.stream().map(StringExtractorFactory::fromStep).toList();
        return new StringExtractor.Chain(chain);
    }

    private static StringExtractor fromStep(RTLParser.StrExtrStepContext ctx) {
        if (ctx.substr()    != null) return fromSubstr(ctx.substr());
        if (ctx.replace()   != null) return fromReplace(ctx.replace());
        if (ctx.norm()      != null) return StringExtractor.WhitespaceNormalized.INSTANCE;
        if (ctx.upperCase() != null) return StringExtractor.UpperCase.INSTANCE;
        if (ctx.lowerCase() != null) return StringExtractor.LowerCase.INSTANCE;
        if (ctx.trim()      != null) return StringExtractor.Trimmed.INSTANCE;
        throw new IllegalStateException("Unknown strExtrStep alternative");
    }

    private static StringExtractor fromSubstr(RTLParser.SubstrContext ctx) {
        int begin  = Integer.parseInt(ctx.INT(0).getText());
        int length = Integer.parseInt(ctx.INT(1).getText());
        return new StringExtractor.Substring(begin, begin + length);
    }

    private static StringExtractor fromReplace(RTLParser.ReplaceContext ctx) {
        String pattern     = parseStringLiteral(ctx.STRING(0).getText());
        String replacement = parseStringLiteral(ctx.STRING(1).getText());
        return new StringExtractor.Replaced(pattern, replacement);
    }

    static String parseStringLiteral(String tokenText) {
        if (tokenText.isEmpty()) return tokenText;
        char first = tokenText.charAt(0);
        char last  = tokenText.charAt(tokenText.length() - 1);
        String inner;
        if ((first == '"' || first == '“') && (last == '"' || last == '”' || last == '″')) {
            inner = tokenText.substring(1, tokenText.length() - 1)
                             .replace("\"\"", "\"");
        } else if (first == '\'' && last == '\'') {
            inner = tokenText.substring(1, tokenText.length() - 1)
                             .replace("''", "'");
        } else {
            inner = tokenText;
        }
        return inner;
    }
}
