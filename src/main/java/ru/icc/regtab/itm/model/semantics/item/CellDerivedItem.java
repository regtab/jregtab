package ru.icc.regtab.itm.model.semantics.item;

import ru.icc.regtab.itm.model.semantics.predicate.DirectionalModifier;
import ru.icc.regtab.itm.model.semantics.predicate.IntRange;
import ru.icc.regtab.itm.model.syntax.Cell;
import ru.icc.regtab.itm.model.syntax.CellColor;
import ru.icc.regtab.itm.model.syntax.FontFamily;
import ru.icc.regtab.itm.model.syntax.HorizontalAlignment;
import ru.icc.regtab.itm.model.syntax.VerticalAlignment;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A cell-derived item: a triple (s, u_vec, i) where s is a string obtained from
 * the source cell, u_vec is a sequence of user-defined tags, and i is a zero-based
 * index of the item within the cell (Def. 5).
 */
public final class CellDerivedItem implements Item {

    private final String str;
    private final List<String> tags;
    private final int index;
    private final Cell cell;
    private final ItemType type;
    /** Range check: rows.from(lo).to(hi) yields lo &lt;= row &lt;= hi. */
    public final IntRange rows;
    /** Range check: cols.from(lo).to(hi) yields lo &lt;= col &lt;= hi. */
    public final IntRange cols;
    /** Range check: pos.from(lo).to(hi) yields lo &lt;= index &lt;= hi. */
    public final IntRange pos;

    public CellDerivedItem(String str, List<String> tags, int index, Cell cell, ItemType type) {
        this.str = Objects.requireNonNull(str, "str");
        this.tags = List.copyOf(Objects.requireNonNull(tags, "tags"));
        if (index < 0) throw new IllegalArgumentException("index must be non-negative: " + index);
        this.index = index;
        this.cell = Objects.requireNonNull(cell, "cell");
        this.type = Objects.requireNonNull(type, "type");
        this.rows = new IntRange(cell::row);
        this.cols = new IntRange(cell::col);
        this.pos = new IntRange(() -> index);
    }

    public CellDerivedItem(String str, int index, Cell cell, ItemType type) {
        this(str, List.of(), index, cell, type);
    }

    // --- Position checks ---

    public boolean sameCol(CellDerivedItem anchor)      { return cell.col() == anchor.cell().col(); }
    public boolean sameRow(CellDerivedItem anchor)      { return cell.row() == anchor.cell().row(); }
    public boolean sameCell(CellDerivedItem anchor)     { return cell == anchor.cell(); }
    public boolean sameSubtable(CellDerivedItem anchor) { return cell.subtable() != null && cell.subtable() == anchor.cell().subtable(); }
    public boolean sameSubrow(CellDerivedItem anchor)   { return cell.subrow() != null && cell.subrow() == anchor.cell().subrow(); }
    public boolean sameSubcol(CellDerivedItem anchor)   { return sameSubtable(anchor) && sameCol(anchor); }
    public boolean row(int i)                           { return cell.row() == i; }
    public boolean col(int j)                           { return cell.col() == j; }
    public boolean posIndex(int k)                      { return index == k; }

    // --- Directional checks ---

    public DirectionalModifier above(CellDerivedItem anchor)   { return new DirectionalModifier(this, anchor, cell.row() < anchor.cell().row()); }
    public DirectionalModifier below(CellDerivedItem anchor)   { return new DirectionalModifier(this, anchor, cell.row() > anchor.cell().row()); }
    public DirectionalModifier leftOf(CellDerivedItem anchor)  { return new DirectionalModifier(this, anchor, cell.col() < anchor.cell().col()); }
    public DirectionalModifier rightOf(CellDerivedItem anchor) { return new DirectionalModifier(this, anchor, cell.col() > anchor.cell().col()); }

    // --- Formatting checks ---

    public boolean bold()                                { return cell.fontBold(); }
    public boolean italic()                              { return cell.fontItalic(); }
    public boolean underline()                           { return cell.fontUnderline(); }
    public boolean strikeout()                           { return cell.fontStrikeout(); }
    public boolean fontFamily(FontFamily ff)             { return cell.fontFamily() == ff; }
    public boolean horzAlign(HorizontalAlignment ha)     { return cell.horzAlign() == ha; }
    public boolean vertAlign(VerticalAlignment va)       { return cell.vertAlign() == va; }
    public boolean leftBorder()                          { return cell.leftBorder(); }
    public boolean topBorder()                           { return cell.topBorder(); }
    public boolean rightBorder()                         { return cell.rightBorder(); }
    public boolean bottomBorder()                        { return cell.bottomBorder(); }
    public boolean bgColor(int r, int g, int b)          { return cell.bgColor().equals(new CellColor(r, g, b)); }
    public boolean bgColor(CellColor color)              { return cell.bgColor().equals(color); }
    public boolean fgColor(int r, int g, int b)          { return cell.fgColor().equals(new CellColor(r, g, b)); }
    public boolean fgColor(CellColor color)              { return cell.fgColor().equals(color); }
    public boolean rotation(double degrees)              { return Double.compare(cell.rotation(), degrees) == 0; }
    public boolean sameBgColor(CellDerivedItem anchor)   { return cell.bgColor().equals(anchor.cell().bgColor()); }
    public boolean sameFgColor(CellDerivedItem anchor)   { return cell.fgColor().equals(anchor.cell().fgColor()); }

    public boolean sameFont(CellDerivedItem anchor) {
        Cell a = anchor.cell();
        return cell.fontFamily() == a.fontFamily()
                && cell.fontBold() == a.fontBold()
                && cell.fontItalic() == a.fontItalic()
                && cell.fontUnderline() == a.fontUnderline()
                && cell.fontStrikeout() == a.fontStrikeout();
    }

    public boolean sameFormat(CellDerivedItem anchor) {
        Cell a = anchor.cell();
        return cell.fontFamily() == a.fontFamily()
                && cell.fontBold() == a.fontBold()
                && cell.fontItalic() == a.fontItalic()
                && cell.fontUnderline() == a.fontUnderline()
                && cell.fontStrikeout() == a.fontStrikeout()
                && cell.horzAlign() == a.horzAlign()
                && cell.vertAlign() == a.vertAlign()
                && cell.leftBorder() == a.leftBorder()
                && cell.topBorder() == a.topBorder()
                && cell.rightBorder() == a.rightBorder()
                && cell.bottomBorder() == a.bottomBorder()
                && cell.bgColor().equals(a.bgColor())
                && cell.fgColor().equals(a.fgColor())
                && Double.compare(cell.rotation(), a.rotation()) == 0;
    }

    // --- Content checks ---

    public boolean textBlank()                           { return cell.textBlank(); }
    public boolean multilineCell()                       { return cell.textMultiline(); }
    public boolean cellText(String text)                 { return cell.text().equals(text); }
    public boolean str(String s)                         { return str.equals(s); }
    public boolean blankStr()                            { return str.isBlank(); }
    public boolean strMatching(String regex)             { return Pattern.matches(regex, str); }
    public boolean hasTag(String tag)                    { return tags.contains(tag); }
    public boolean sameStr(CellDerivedItem anchor)       { return str.equals(anchor.str()); }

    @Override
    public String str() { return str; }

    @Override
    public ItemType type() { return type; }

    public List<String> tags() { return tags; }

    public int index() { return index; }

    public Cell cell() { return cell; }

    @Override
    public String toString() {
        return "CellDerivedItem[str=\"" + str + "\", index=" + index
                + ", cell=" + cell.pos() + ", type=" + type + "]";
    }
}
