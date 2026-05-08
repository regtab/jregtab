package ru.icc.regtab.itm.model.semantics.item;

import ru.icc.regtab.itm.model.semantics.predicate.DirectionalModifier;
import ru.icc.regtab.itm.model.semantics.predicate.Has;
import ru.icc.regtab.itm.model.semantics.predicate.IntRangeStart;
import ru.icc.regtab.itm.model.semantics.predicate.Is;
import ru.icc.regtab.itm.model.syntax.Cell;
import ru.icc.regtab.itm.model.syntax.CellColor;
import ru.icc.regtab.itm.model.syntax.FontFamily;
import ru.icc.regtab.itm.model.syntax.HorizontalAlignment;
import ru.icc.regtab.itm.model.syntax.VerticalAlignment;

import java.util.List;
import java.util.Objects;

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
    /** Entry point for fluent predicate checks on this item as candidate (position). */
    public final Is is;
    /** Entry point for fluent predicate checks on this item as candidate (formatting, content). */
    public final Has has;
    /** Range check shortcut: rows.from(lo).to(hi). */
    public final IntRangeStart rows;
    /** Range check shortcut: cols.from(lo).to(hi). */
    public final IntRangeStart cols;
    /** Range check shortcut: pos.from(lo).to(hi). */
    public final IntRangeStart pos;

    public CellDerivedItem(String str, List<String> tags, int index, Cell cell, ItemType type) {
        this.str = Objects.requireNonNull(str, "str");
        this.tags = List.copyOf(Objects.requireNonNull(tags, "tags"));
        if (index < 0) throw new IllegalArgumentException("index must be non-negative: " + index);
        this.index = index;
        this.cell = Objects.requireNonNull(cell, "cell");
        this.type = Objects.requireNonNull(type, "type");
        this.is = new Is(this);
        this.has = new Has(this);
        this.rows = is.in.rows;
        this.cols = is.in.cols;
        this.pos = is.in.pos;
    }

    public CellDerivedItem(String str, int index, Cell cell, ItemType type) {
        this(str, List.of(), index, cell, type);
    }

    // --- Position shortcuts (is.in.*) ---

    public boolean sameCol(CellDerivedItem anchor)      { return is.in.sameCol(anchor); }
    public boolean sameRow(CellDerivedItem anchor)      { return is.in.sameRow(anchor); }
    public boolean sameCell(CellDerivedItem anchor)     { return is.in.sameCell(anchor); }
    public boolean sameSubtable(CellDerivedItem anchor) { return is.in.sameSubtable(anchor); }
    public boolean sameSubrow(CellDerivedItem anchor)   { return is.in.sameSubrow(anchor); }
    public boolean row(int i)                           { return is.in.row(i); }
    public boolean col(int j)                           { return is.in.col(j); }
    public boolean posIndex(int k)                      { return is.in.pos(k); }

    // --- Directional shortcuts (is.*) ---

    public DirectionalModifier above(CellDerivedItem anchor)   { return is.above(anchor); }
    public DirectionalModifier below(CellDerivedItem anchor)   { return is.below(anchor); }
    public DirectionalModifier leftOf(CellDerivedItem anchor)  { return is.leftOf(anchor); }
    public DirectionalModifier rightOf(CellDerivedItem anchor) { return is.rightOf(anchor); }

    // --- Formatting shortcuts (has.*) ---

    public boolean bold()                                { return has.bold(); }
    public boolean italic()                              { return has.italic(); }
    public boolean underline()                           { return has.underline(); }
    public boolean strikeout()                           { return has.strikeout(); }
    public boolean fontFamily(FontFamily ff)             { return has.fontFamily(ff); }
    public boolean horzAlign(HorizontalAlignment ha)     { return has.horzAlign(ha); }
    public boolean vertAlign(VerticalAlignment va)       { return has.vertAlign(va); }
    public boolean leftBorder()                          { return has.leftBorder(); }
    public boolean topBorder()                           { return has.topBorder(); }
    public boolean rightBorder()                         { return has.rightBorder(); }
    public boolean bottomBorder()                        { return has.bottomBorder(); }
    public boolean bgColor(int r, int g, int b)          { return has.bgColor(r, g, b); }
    public boolean bgColor(CellColor color)              { return has.bgColor(color); }
    public boolean fgColor(int r, int g, int b)          { return has.fgColor(r, g, b); }
    public boolean fgColor(CellColor color)              { return has.fgColor(color); }
    public boolean rotation(double degrees)              { return has.rotation(degrees); }
    public boolean sameBgColor(CellDerivedItem anchor)   { return has.sameBgColor(anchor); }
    public boolean sameFgColor(CellDerivedItem anchor)   { return has.sameFgColor(anchor); }
    public boolean sameFont(CellDerivedItem anchor)      { return has.sameFont(anchor); }
    public boolean sameFormat(CellDerivedItem anchor)    { return has.sameFormat(anchor); }

    // --- Content shortcuts (has.*) ---

    public boolean textBlank()                           { return has.textBlank(); }
    public boolean multilineCell()                       { return has.multilineCell(); }
    public boolean cellText(String text)                 { return has.cellText(text); }
    public boolean str(String s)                         { return has.str(s); }
    public boolean blankStr()                            { return has.blankStr(); }
    public boolean strMatching(String regex)             { return has.strMatching(regex); }
    public boolean hasTag(String tag)                    { return has.hasTag(tag); }
    public boolean sameStr(CellDerivedItem anchor)       { return has.sameStr(anchor); }

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
