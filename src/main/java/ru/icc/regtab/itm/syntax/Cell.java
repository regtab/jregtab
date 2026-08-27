package ru.icc.regtab.itm.syntax;

import java.util.Objects;

/**
 * An atomic unit of a table occupying a distinct position in the row-column grid.
 * Carries layout, formatting, and content properties (def:syntactic-layer).
 */
public final class Cell {

    // --- Layout properties (Def. 2) ---
    private final GridPosition pos;
    /** {@code null} for a non-merged cell: the single-cell box is derived from {@link #pos} on demand. */
    private final BoundingBox bbox;
    private final boolean merged;
    private Row parentRow;
    private Subtable subtable;
    private Subrow subrow;

    // --- Formatting properties (Def. 3): shared immutable holder, copy-on-write ---
    private CellFormat format = CellFormat.DEFAULT;

    // --- Content properties (Def. 4) ---
    private String text = "";
    private boolean textBlank = true;
    private boolean textMultiline;
    private int textIndent;

    public Cell(GridPosition pos, BoundingBox bbox, boolean merged) {
        this.pos = Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(bbox, "bbox");
        this.bbox = bbox.equals(BoundingBox.single(pos)) ? null : bbox;
        this.merged = merged;
    }

    public Cell(GridPosition pos) {
        this.pos = Objects.requireNonNull(pos, "pos");
        this.bbox = null;
        this.merged = false;
    }

    // --- Layout getters ---

    public GridPosition pos() { return pos; }
    public int row() { return pos.row(); }
    public int col() { return pos.col(); }
    public BoundingBox bbox() { return bbox != null ? bbox : BoundingBox.single(pos); }
    public boolean merged() { return merged; }
    public Row parentRow() { return parentRow; }
    public Subtable subtable() { return subtable; }
    public Subrow subrow() { return subrow; }

    // --- Layout setters (package-private, set during table construction) ---

    void setParentRow(Row parentRow) { this.parentRow = parentRow; }
    void setSubtable(Subtable subtable) { this.subtable = subtable; }
    void setSubrow(Subrow subrow) { this.subrow = subrow; }

    // --- Formatting getters ---

    public CellFormat format() { return format; }
    public FontFamily fontFamily() { return format.fontFamily(); }
    public boolean fontBold() { return format.fontBold(); }
    public boolean fontItalic() { return format.fontItalic(); }
    public boolean fontStrikeout() { return format.fontStrikeout(); }
    public boolean fontUnderline() { return format.fontUnderline(); }
    public HorizontalAlignment horzAlign() { return format.horzAlign(); }
    public VerticalAlignment vertAlign() { return format.vertAlign(); }
    public boolean leftBorder() { return format.leftBorder(); }
    public boolean topBorder() { return format.topBorder(); }
    public boolean rightBorder() { return format.rightBorder(); }
    public boolean bottomBorder() { return format.bottomBorder(); }
    public CellColor bgColor() { return format.bgColor(); }
    public CellColor fgColor() { return format.fgColor(); }
    public double rotation() { return format.rotation(); }

    // --- Formatting setters ---

    public void setFormat(CellFormat format) { this.format = Objects.requireNonNull(format); }
    public void setFontFamily(FontFamily fontFamily) { format = format.withFontFamily(Objects.requireNonNull(fontFamily)); }
    public void setFontBold(boolean fontBold) { format = format.withFontBold(fontBold); }
    public void setFontItalic(boolean fontItalic) { format = format.withFontItalic(fontItalic); }
    public void setFontStrikeout(boolean fontStrikeout) { format = format.withFontStrikeout(fontStrikeout); }
    public void setFontUnderline(boolean fontUnderline) { format = format.withFontUnderline(fontUnderline); }
    public void setHorzAlign(HorizontalAlignment horzAlign) { format = format.withHorzAlign(Objects.requireNonNull(horzAlign)); }
    public void setVertAlign(VerticalAlignment vertAlign) { format = format.withVertAlign(Objects.requireNonNull(vertAlign)); }
    public void setLeftBorder(boolean leftBorder) { format = format.withLeftBorder(leftBorder); }
    public void setTopBorder(boolean topBorder) { format = format.withTopBorder(topBorder); }
    public void setRightBorder(boolean rightBorder) { format = format.withRightBorder(rightBorder); }
    public void setBottomBorder(boolean bottomBorder) { format = format.withBottomBorder(bottomBorder); }
    public void setBgColor(CellColor bgColor) { format = format.withBgColor(Objects.requireNonNull(bgColor)); }
    public void setFgColor(CellColor fgColor) { format = format.withFgColor(Objects.requireNonNull(fgColor)); }
    public void setRotation(double rotation) { format = format.withRotation(rotation); }

    // --- Content getters ---

    public String text() { return text; }
    public boolean textBlank() { return textBlank; }
    public boolean textMultiline() { return textMultiline; }
    public int textIndent() { return textIndent; }

    // --- Content setters ---

    public void setText(String text) {
        this.text = Objects.requireNonNull(text);
        this.textBlank = text.isBlank();
        this.textMultiline = text.contains("\n");
        this.textIndent = computeIndent(text);
    }

    private static int computeIndent(String text) {
        int indent = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ' ') indent++;
            else break;
        }
        return indent;
    }

    @Override
    public String toString() {
        return "Cell[pos=" + pos + ", text=\"" + text + "\"]";
    }
}
