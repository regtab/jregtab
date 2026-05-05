package ru.icc.regtab.itm.model.semantics.predicate;

import org.junit.jupiter.api.Test;
import ru.icc.regtab.itm.model.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.model.semantics.item.ItemType;
import ru.icc.regtab.itm.model.syntax.CellColor;
import ru.icc.regtab.itm.model.syntax.FontFamily;
import ru.icc.regtab.itm.model.syntax.HorizontalAlignment;
import ru.icc.regtab.itm.model.syntax.TableSyntax;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the fluent predicate API (Has) on CellDerivedItem: formatting and content checks.
 */
class ItemPredicateHasApiTest {

    @Test
    void testFormattingBoldItalic() {
        TableSyntax syntax = new TableSyntax(2, 2);
        syntax.getCell(0, 0).setFontBold(true);
        syntax.getCell(0, 1).setFontItalic(true);
        syntax.getCell(1, 0).setFontBold(true);
        syntax.getCell(1, 0).setFontItalic(true);

        CellDerivedItem boldOnly = new CellDerivedItem("B", 0, syntax.getCell(0, 0), ItemType.VALUE);
        CellDerivedItem italicOnly = new CellDerivedItem("I", 0, syntax.getCell(0, 1), ItemType.VALUE);
        CellDerivedItem boldItalic = new CellDerivedItem("BI", 0, syntax.getCell(1, 0), ItemType.VALUE);

        assertTrue(boldOnly.has.bold());
        assertFalse(boldOnly.has.italic());
        assertTrue(italicOnly.has.italic());
        assertFalse(italicOnly.has.bold());
        assertTrue(boldItalic.has.bold());
        assertTrue(boldItalic.has.italic());
    }

    @Test
    void testFontFamily() {
        TableSyntax syntax = new TableSyntax(1, 3);
        syntax.getCell(0, 0).setFontFamily(FontFamily.SERIF);
        syntax.getCell(0, 1).setFontFamily(FontFamily.SANS_SERIF);
        syntax.getCell(0, 2).setFontFamily(FontFamily.MONOSPACED);

        CellDerivedItem serif = new CellDerivedItem("S", 0, syntax.getCell(0, 0), ItemType.VALUE);
        CellDerivedItem sans = new CellDerivedItem("SS", 0, syntax.getCell(0, 1), ItemType.VALUE);
        CellDerivedItem mono = new CellDerivedItem("M", 0, syntax.getCell(0, 2), ItemType.VALUE);

        assertTrue(serif.has.fontFamily(FontFamily.SERIF));
        assertTrue(sans.has.fontFamily(FontFamily.SANS_SERIF));
        assertTrue(mono.has.fontFamily(FontFamily.MONOSPACED));
        assertFalse(serif.has.fontFamily(FontFamily.SANS_SERIF));
    }

    @Test
    void testBgColor() {
        TableSyntax syntax = new TableSyntax(1, 2);
        syntax.getCell(0, 0).setBgColor(new CellColor(255, 255, 0));
        syntax.getCell(0, 1).setBgColor(CellColor.WHITE);

        CellDerivedItem yellow = new CellDerivedItem("Y", 0, syntax.getCell(0, 0), ItemType.VALUE);
        CellDerivedItem white = new CellDerivedItem("W", 0, syntax.getCell(0, 1), ItemType.VALUE);

        assertTrue(yellow.has.bgColor(255, 255, 0));
        assertTrue(yellow.has.bgColor(new CellColor(255, 255, 0)));
        assertTrue(white.has.bgColor(CellColor.WHITE));
        assertFalse(yellow.has.bgColor(CellColor.WHITE));
    }

    @Test
    void testHorzAlign() {
        TableSyntax syntax = new TableSyntax(1, 2);
        syntax.getCell(0, 0).setHorzAlign(HorizontalAlignment.CENTER);
        syntax.getCell(0, 1).setHorzAlign(HorizontalAlignment.RIGHT);

        CellDerivedItem center = new CellDerivedItem("C", 0, syntax.getCell(0, 0), ItemType.VALUE);
        CellDerivedItem right = new CellDerivedItem("R", 0, syntax.getCell(0, 1), ItemType.VALUE);

        assertTrue(center.has.horzAlign(HorizontalAlignment.CENTER));
        assertTrue(right.has.horzAlign(HorizontalAlignment.RIGHT));
        assertFalse(center.has.horzAlign(HorizontalAlignment.RIGHT));
    }

    @Test
    void testContentStr() {
        TableSyntax syntax = new TableSyntax(2, 2);
        syntax.getCell(0, 0).setText("Hello");
        syntax.getCell(0, 1).setText("");
        syntax.getCell(1, 0).setText("  ");

        CellDerivedItem hello = new CellDerivedItem("Hello", 0, syntax.getCell(0, 0), ItemType.VALUE);
        CellDerivedItem empty = new CellDerivedItem("", 0, syntax.getCell(0, 1), ItemType.VALUE);
        CellDerivedItem blanks = new CellDerivedItem("  ", 0, syntax.getCell(1, 0), ItemType.VALUE);

        assertTrue(hello.has.str("Hello"));
        assertFalse(hello.has.str("World"));
        assertTrue(empty.has.blankStr());
        assertTrue(blanks.has.blankStr());
        assertFalse(hello.has.blankStr());
    }

    @Test
    void testContentCellText() {
        TableSyntax syntax = new TableSyntax(1, 1);
        syntax.getCell(0, 0).setText("Header");

        CellDerivedItem item = new CellDerivedItem("Header", 0, syntax.getCell(0, 0), ItemType.VALUE);

        assertTrue(item.has.cellText("Header"));
        assertFalse(item.has.cellText("Other"));
    }

    @Test
    void testContentTextBlank() {
        TableSyntax syntax = new TableSyntax(1, 2);
        syntax.getCell(0, 0).setText("X");
        syntax.getCell(0, 1).setText("");

        CellDerivedItem nonBlank = new CellDerivedItem("X", 0, syntax.getCell(0, 0), ItemType.VALUE);
        CellDerivedItem blank = new CellDerivedItem("", 0, syntax.getCell(0, 1), ItemType.VALUE);

        assertFalse(nonBlank.has.textBlank());
        assertTrue(blank.has.textBlank());
    }

    @Test
    void testSameStr() {
        TableSyntax syntax = new TableSyntax(2, 2);
        syntax.getCell(0, 0).setText("Anna");
        syntax.getCell(1, 0).setText("Anna");
        syntax.getCell(0, 1).setText("Bob");

        CellDerivedItem a1 = new CellDerivedItem("Anna", 0, syntax.getCell(0, 0), ItemType.VALUE);
        CellDerivedItem a2 = new CellDerivedItem("Anna", 0, syntax.getCell(1, 0), ItemType.VALUE);
        CellDerivedItem b = new CellDerivedItem("Bob", 0, syntax.getCell(0, 1), ItemType.VALUE);

        assertTrue(a1.has.sameStr(a2));
        assertTrue(a2.has.sameStr(a1));
        assertFalse(a1.has.sameStr(b));
    }

    @Test
    void testSameBgColor() {
        TableSyntax syntax = new TableSyntax(2, 2);
        CellColor yellow = new CellColor(255, 255, 0);
        syntax.getCell(0, 0).setBgColor(yellow);
        syntax.getCell(1, 0).setBgColor(yellow);
        syntax.getCell(0, 1).setBgColor(CellColor.WHITE);

        CellDerivedItem y1 = new CellDerivedItem("Y1", 0, syntax.getCell(0, 0), ItemType.VALUE);
        CellDerivedItem y2 = new CellDerivedItem("Y2", 0, syntax.getCell(1, 0), ItemType.VALUE);
        CellDerivedItem w = new CellDerivedItem("W", 0, syntax.getCell(0, 1), ItemType.VALUE);

        assertTrue(y1.has.sameBgColor(y2));
        assertFalse(y1.has.sameBgColor(w));
    }

    @Test
    void testStrMatching() {
        TableSyntax syntax = new TableSyntax(1, 3);
        syntax.getCell(0, 0).setText("123");
        syntax.getCell(0, 1).setText("abc");
        syntax.getCell(0, 2).setText("12.34");

        CellDerivedItem digits = new CellDerivedItem("123", 0, syntax.getCell(0, 0), ItemType.VALUE);
        CellDerivedItem letters = new CellDerivedItem("abc", 0, syntax.getCell(0, 1), ItemType.VALUE);
        CellDerivedItem decimal = new CellDerivedItem("12.34", 0, syntax.getCell(0, 2), ItemType.VALUE);

        assertTrue(digits.has.strMatching("\\d+"));
        assertFalse(letters.has.strMatching("\\d+"));
        assertTrue(decimal.has.strMatching("\\d+\\.\\d+"));
    }

    @Test
    void testCombinedWithIs() {
        TableSyntax syntax = new TableSyntax(3, 2);
        syntax.getCell(0, 0).setText("Anna");
        syntax.getCell(0, 1).setText("43");
        syntax.getCell(1, 0).setText("Anna");
        syntax.getCell(1, 1).setText("78");
        syntax.getCell(2, 0).setText("Bob");
        syntax.getCell(2, 1).setText("96");

        CellDerivedItem anna1 = new CellDerivedItem("Anna", 0, syntax.getCell(0, 0), ItemType.VALUE);
        CellDerivedItem anna2 = new CellDerivedItem("Anna", 0, syntax.getCell(1, 0), ItemType.VALUE);
        CellDerivedItem bob = new CellDerivedItem("Bob", 0, syntax.getCell(2, 0), ItemType.VALUE);

        // Same as SchemaFlexibleTest: cand below anchor, same col, same str
        assertTrue(anna2.has.sameStr(anna1) && anna2.is.below(anna1).sameCol());
        assertFalse(bob.has.sameStr(anna1));
    }
}
