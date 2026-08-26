package ru.icc.regtab.atp.match;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.icc.regtab.atp.AtpMatcher;
import ru.icc.regtab.atp.spec.AtomicContentSpec;
import ru.icc.regtab.atp.spec.CellPattern;
import ru.icc.regtab.atp.spec.CompoundContentSpec;
import ru.icc.regtab.atp.spec.CompoundSegment;
import ru.icc.regtab.atp.spec.ContentSpec;
import ru.icc.regtab.atp.spec.DelimitedContentSpec;
import ru.icc.regtab.atp.spec.RowPattern;
import ru.icc.regtab.atp.spec.StringExtractor;
import ru.icc.regtab.atp.spec.SubtablePattern;
import ru.icc.regtab.atp.spec.TablePattern;
import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.syntax.TableSyntax;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Semantics of the delimited content specification S_delim
 * (def:delimited-content-spec) as realised by {@link SemanticConstructor}.
 * <p>
 * The contract under test: splitting is <em>verbatim</em> — substrings reach
 * S_atom exactly as the split produced them. Surrounding whitespace is kept and
 * empty substrings yield items with an empty string value, matching the formal
 * definition (s&#x2096; &#x2208; &#x3a3;*) and {@code pandas str.split}. Trimming is opt-in
 * through the atom's string extractor &#x3be; (RTL {@code =TRIM} / {@code =NORM}).
 */
class SemanticConstructorDelimitedTest {

    private static final String DELIM = ",";

    // ---- standalone delimited cell: RTL (VAL){","} ----

    @Test
    @DisplayName("(VAL){\",\"} keeps whitespace surrounding each token")
    void rawTokensKeepSurroundingWhitespace() {
        assertItems(new DelimitedContentSpec(DELIM, AtomicContentSpec.val()),
                "a, b", "a", " b");
    }

    @Test
    @DisplayName("(VAL){\",\"} derives an empty item for an empty token")
    void emptyTokenYieldsEmptyItem() {
        assertItems(new DelimitedContentSpec(DELIM, AtomicContentSpec.val()),
                "a,,b", "a", "", "b");
    }

    @Test
    @DisplayName("(VAL){\",\"} keeps the empty token produced by a trailing delimiter")
    void trailingDelimiterYieldsTrailingEmptyItem() {
        assertItems(new DelimitedContentSpec(DELIM, AtomicContentSpec.val()),
                "a,b,", "a", "b", "");
    }

    @Test
    @DisplayName("(VAL=TRIM){\",\"} restores the pre-0.5.0 trimming behaviour")
    void trimExtractorRestoresLegacyBehaviour() {
        assertItems(new DelimitedContentSpec(DELIM, trimmedVal()),
                "a, b", "a", "b");
    }

    @Test
    @DisplayName("(VAL=NORM){\",\"} normalises whitespace inside each token")
    void normExtractorAppliesPerToken() {
        assertItems(new DelimitedContentSpec(DELIM, AtomicContentSpec.val()
                        .extract(StringExtractor.WhitespaceNormalized.INSTANCE)),
                "  a  a ,  b ", "a a", "b");
    }

    // ---- delimited segment inside a compound cell: RTL VAL "," (VAL){","} ----

    @Test
    @DisplayName("compound segment VAL \",\" (VAL){\",\"} keeps raw tokens")
    void compoundDelimitedSegmentKeepsRawTokens() {
        assertItems(compound(AtomicContentSpec.val()),
                "x, a, b", "x", " a", " b");
    }

    @Test
    @DisplayName("compound segment VAL \",\" (VAL=TRIM){\",\"} trims each token")
    void compoundDelimitedSegmentHonoursTrimExtractor() {
        assertItems(compound(trimmedVal()),
                "x, a, b", "x", "a", "b");
    }

    @Test
    @DisplayName("compound segment (VAL){\",\"} keeps empty tokens and numbers items contiguously")
    void compoundDelimitedSegmentKeepsEmptyTokens() {
        assertItems(compound(AtomicContentSpec.val()),
                "x,a,,b", "x", "a", "", "b");
    }

    // ---- helpers ----

    private static AtomicContentSpec trimmedVal() {
        return AtomicContentSpec.val().extract(StringExtractor.Trimmed.INSTANCE);
    }

    /** Compound cell {@code VAL "," (delimited by ",")} with no trailing delimiter. */
    private static ContentSpec compound(AtomicContentSpec tokenSpec) {
        return new CompoundContentSpec(List.of(
                new CompoundSegment("", AtomicContentSpec.val()),
                new CompoundSegment(DELIM, new DelimitedContentSpec(DELIM, tokenSpec))
        ), "");
    }

    /**
     * Matches a one-cell table holding {@code cellText} against {@code spec} and asserts
     * the derived item strings, ordered by item index. Index order is asserted too: the
     * items must be numbered 0..n-1 without gaps.
     */
    private static void assertItems(ContentSpec spec, String cellText, String... expected) {
        var syntax = new TableSyntax(1, 1);
        syntax.getCell(0, 0).setText(cellText);

        InterpretableTable itm = AtpMatcher.match(
                        TablePattern.of(SubtablePattern.of(RowPattern.of(CellPattern.of(spec)))),
                        syntax)
                .orElseThrow(() -> new AssertionError("pattern did not match: '" + cellText + "'"));

        List<CellDerivedItem> items = itm.semantics().cellDerivedItems().stream()
                .sorted(Comparator.comparingInt(CellDerivedItem::index))
                .toList();

        assertEquals(List.of(expected), items.stream().map(CellDerivedItem::str).toList(),
                "derived item strings for '" + cellText + "'");
        assertEquals(java.util.stream.IntStream.range(0, expected.length).boxed().toList(),
                items.stream().map(CellDerivedItem::index).toList(),
                "item indices must be contiguous 0..n-1 for '" + cellText + "'");
    }
}
