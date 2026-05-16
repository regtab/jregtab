package ru.icc.regtab.itm.rtl.internal;

import ru.icc.regtab.itm.atp.spec.StringExtractor;
import ru.icc.regtab.itm.rtl.RTLParser;

/** Builds a {@link StringExtractor} from an RTL {@code strExtr} parse context. */
final class StringExtractorFactory {

    private StringExtractorFactory() {}

    static StringExtractor from(RTLParser.StrExtrContext ctx) {
        if (ctx.substr() != null)    return fromSubstr(ctx.substr());
        if (ctx.replace() != null)   return fromReplace(ctx.replace());
        if (ctx.norm()    != null)   return StringExtractor.norm();
        if (ctx.upperCase() != null) return String::toUpperCase;
        if (ctx.lowerCase() != null) return String::toLowerCase;
        throw new IllegalStateException("Unknown strExtr alternative");
    }

    private static StringExtractor fromSubstr(RTLParser.SubstrContext ctx) {
        int begin = Integer.parseInt(ctx.INT(0).getText());
        int end   = Integer.parseInt(ctx.INT(1).getText());
        return StringExtractor.substring(begin, end);
    }

    private static StringExtractor fromReplace(RTLParser.ReplaceContext ctx) {
        String pattern     = parseStringLiteral(ctx.STRING(0).getText());
        String replacement = parseStringLiteral(ctx.STRING(1).getText());
        return StringExtractor.replace(pattern, replacement);
    }

    static String parseStringLiteral(String tokenText) {
        if (tokenText.isEmpty()) return tokenText;
        char first = tokenText.charAt(0);
        char last  = tokenText.charAt(tokenText.length() - 1);
        String inner;
        if ((first == '"' || first == '“') && (last == '"' || last == '"' || last == '″')) {
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
