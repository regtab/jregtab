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
 * Tests for predicate methods on CellDerivedItem: formatting and content checks.
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

        assertTrue(boldOnly.bold());
        assertFalse(boldOnly.italic());
        assertTrue(italicOnly.italic());
        assertFalse(italicOnly.bold());
        assertTrue(boldItalic.bold());
        assertTrue(boldItalic.italic());
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

        assertTrue(serif.fontFamily(FontFamily.SERIF));
        assertTrue(sans.fontFamily(FontFamily.SANS_SERIF));
        assertTrue(mono.fontFamily(FontFamily.MONOSPACED));
        assertFalse(serif.fontFamily(FontFamily.SANS_SERIF));
    }

    @Test
    void testBgColor() {
        TableSyntax syntax = new TableSyntax(1, 2);
        syntax.getCell(0, 0).setBgColor(new CellColor(255, 255, 0));
        syntax.getCell(0, 1).setBgColor(CellColor.WHITE);

        CellDerivedItem yellow = new CellDerivedItem("Y", 0, syntax.getCell(0, 0), ItemType.VALUE);
        CellDerivedItem white = new CellDerivedItem("W", 0, syntax.getCell(0, 1), ItemType.VALUE);

        assertTrue(yellow.bgColor(255, 255, 0));
        assertTrue(yellow.bgColor(new CellColor(255, 255, 0)));
        assertTrue(white.bgColor(CellColor.WHITE));
        assertFalse(yellow.bgColor(CellColor.WHITE));
    }

    @Test
    void testHorzAlign() {
        TableSyntax syntax = new TableSyntax(1, 2);
        syntax.getCell(0, 0).setHorzAlign(HorizontalAlignment.CENTER);
        syntax.getCell(0, 1).setHorzAlign(HorizontalAlignment.RIGHT);

        CellDerivedItem center = new CellDerivedItem("C", 0, syntax.getCell(0, 0), ItemType.VALUE);
        CellDerivedItem right = new CellDerivedItem("R", 0, syntax.getCell(0, 1), ItemType.VALUE);

        assertTrue(center.horzAlign(HorizontalAlignment.CENTER));
        assertTrue(right.horzAlign(HorizontalAlignment.RIGHT));
        assertFalse(center.horzAlign(HorizontalAlignment.RIGHT));
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

        assertTrue(hello.str("Hello"));
        assertFalse(hello.str("World"));
        assertTrue(empty.blankStr());
        assertTrue(blanks.blankStr());
        assertFalse(hello.blankStr());
    }

    @Test
    void testContentCellText() {
        TableSyntax syntax = new TableSyntax(1, 1);
        syntax.getCell(0, 0).setText("Header");

        CellDerivedItem item = new CellDerivedItem("Header", 0, syntax.getCell(0, 0), ItemType.VALUE);

        assertTrue(item.cellText("Header"));
        assertFalse(item.cellText("Other"));
    }

    @Test
    void testContentTextBlank() {
        TableSyntax syntax = new TableSyntax(1, 2);
        syntax.getCell(0, 0).setText("X");
        syntax.getCell(0, 1).setText("");

        CellDerivedItem nonBlank = new CellDerivedItem("X", 0, syntax.getCell(0, 0), ItemType.VALUE);
        CellDerivedItem blank = new CellDerivedItem("", 0, syntax.getCell(0, 1), ItemType.VALUE);

        assertFalse(nonBlank.textBlank());
        assertTrue(blank.textBlank());
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

        assertTrue(a1.sameStr(a2));
        assertTrue(a2.sameStr(a1));
        assertFalse(a1.sameStr(b));
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

        assertTrue(y1.sameBgColor(y2));
        assertFalse(y1.sameBgColor(w));
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

        assertTrue(digits.strMatching("\\d+"));
        assertFalse(letters.strMatching("\\d+"));
        assertTrue(decimal.strMatching("\\d+\\.\\d+"));
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

        assertTrue(anna2.sameStr(anna1) && anna2.below(anna1).sameCol());
        assertFalse(bob.sameStr(anna1));
    }
}
