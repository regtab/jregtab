package ru.icc.regtab.itm.model.semantics.predicate;

import ru.icc.regtab.itm.model.semantics.item.CellDerivedItem;
import ru.icc.regtab.itm.model.syntax.Cell;
import ru.icc.regtab.itm.model.syntax.CellColor;
import ru.icc.regtab.itm.model.syntax.FontFamily;
import ru.icc.regtab.itm.model.syntax.HorizontalAlignment;
import ru.icc.regtab.itm.model.syntax.VerticalAlignment;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Entry point for fluent predicate checks on candidate item's cell formatting and content.
 * Use {@code cand.has} inside an ItemPredicate lambda.
 */
public final class Has {

    private final CellDerivedItem candidate;

    public Has(CellDerivedItem candidate) {
        this.candidate = Objects.requireNonNull(candidate, "candidate");
    }

    private Cell cell() {
        return candidate.cell();
    }

    // --- Formatting checks ---

    public boolean bold() {
        return cell().fontBold();
    }

    public boolean italic() {
        return cell().fontItalic();
    }

    public boolean underline() {
        return cell().fontUnderline();
    }

    public boolean strikeout() {
        return cell().fontStrikeout();
    }

    public boolean fontFamily(FontFamily ff) {
        return cell().fontFamily() == ff;
    }

    public boolean horzAlign(HorizontalAlignment ha) {
        return cell().horzAlign() == ha;
    }

    public boolean vertAlign(VerticalAlignment va) {
        return cell().vertAlign() == va;
    }

    public boolean leftBorder() {
        return cell().leftBorder();
    }

    public boolean topBorder() {
        return cell().topBorder();
    }

    public boolean rightBorder() {
        return cell().rightBorder();
    }

    public boolean bottomBorder() {
        return cell().bottomBorder();
    }

    public boolean bgColor(int r, int g, int b) {
        return cell().bgColor().equals(new CellColor(r, g, b));
    }

    public boolean bgColor(CellColor color) {
        return cell().bgColor().equals(color);
    }

    public boolean fgColor(int r, int g, int b) {
        return cell().fgColor().equals(new CellColor(r, g, b));
    }

    public boolean fgColor(CellColor color) {
        return cell().fgColor().equals(color);
    }

    public boolean rotation(double degrees) {
        return Double.compare(cell().rotation(), degrees) == 0;
    }

    // --- Formatting comparisons with anchor ---

    public boolean sameBgColor(CellDerivedItem anchor) {
        return cell().bgColor().equals(anchor.cell().bgColor());
    }

    public boolean sameFgColor(CellDerivedItem anchor) {
        return cell().fgColor().equals(anchor.cell().fgColor());
    }

    public boolean sameFont(CellDerivedItem anchor) {
        Cell c = cell();
        Cell a = anchor.cell();
        return c.fontFamily() == a.fontFamily()
                && c.fontBold() == a.fontBold()
                && c.fontItalic() == a.fontItalic()
                && c.fontUnderline() == a.fontUnderline()
                && c.fontStrikeout() == a.fontStrikeout();
    }

    public boolean sameFormat(CellDerivedItem anchor) {
        Cell c = cell();
        Cell a = anchor.cell();
        return c.fontFamily() == a.fontFamily()
                && c.fontBold() == a.fontBold()
                && c.fontItalic() == a.fontItalic()
                && c.fontUnderline() == a.fontUnderline()
                && c.fontStrikeout() == a.fontStrikeout()
                && c.horzAlign() == a.horzAlign()
                && c.vertAlign() == a.vertAlign()
                && c.leftBorder() == a.leftBorder()
                && c.topBorder() == a.topBorder()
                && c.rightBorder() == a.rightBorder()
                && c.bottomBorder() == a.bottomBorder()
                && c.bgColor().equals(a.bgColor())
                && c.fgColor().equals(a.fgColor())
                && Double.compare(c.rotation(), a.rotation()) == 0;
    }

    // --- Content checks (cell-level) ---

    public boolean textBlank() {
        return cell().textBlank();
    }

    public boolean multilineCell() {
        return cell().textMultiline();
    }

    public boolean cellText(String text) {
        return cell().text().equals(text);
    }

    // --- Content checks (item-level) ---

    public boolean str(String s) {
        return candidate.str().equals(s);
    }

    public boolean blankStr() {
        return candidate.str().isBlank();
    }

    public boolean strMatching(String regex) {
        return Pattern.matches(regex, candidate.str());
    }

    public boolean hasTag(String tag) {
        return candidate.tags().contains(tag);
    }

    // --- Content comparisons with anchor ---

    public boolean sameStr(CellDerivedItem anchor) {
        return candidate.str().equals(anchor.str());
    }
}
