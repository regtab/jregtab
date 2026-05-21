package ru.icc.regtab.itm.syntax;

import java.util.Objects;

/**
 * An atomic unit of a table occupying a distinct position in the row-column grid.
 * Carries layout, formatting, and content properties (def:syntactic-layer).
 */
public final class Cell {

    // --- Layout properties (Def. 2) ---
    private final GridPosition pos;
    private final BoundingBox bbox;
    private final boolean merged;
    private Row parentRow;
    private Subtable subtable;
    private Subrow subrow;

    // --- Formatting properties (Def. 3) ---
    private FontFamily fontFamily = FontFamily.SERIF;
    private boolean fontBold;
    private boolean fontItalic;
    private boolean fontStrikeout;
    private boolean fontUnderline;
    private HorizontalAlignment horzAlign = HorizontalAlignment.LEFT;
    private VerticalAlignment vertAlign = VerticalAlignment.TOP;
    private boolean leftBorder;
    private boolean topBorder;
    private boolean rightBorder;
    private boolean bottomBorder;
    private CellColor bgColor = CellColor.WHITE;
    private CellColor fgColor = CellColor.BLACK;
    private double rotation;

    // --- Content properties (Def. 4) ---
    private String text = "";
    private boolean textBlank = true;
    private boolean textMultiline;
    private int textIndent;

    public Cell(GridPosition pos, BoundingBox bbox, boolean merged) {
        this.pos = Objects.requireNonNull(pos, "pos");
        this.bbox = Objects.requireNonNull(bbox, "bbox");
        this.merged = merged;
    }

    public Cell(GridPosition pos) {
        this(pos, BoundingBox.single(pos), false);
    }

    // --- Layout getters ---

    public GridPosition pos() { return pos; }
    public int row() { return pos.row(); }
    public int col() { return pos.col(); }
    public BoundingBox bbox() { return bbox; }
    public boolean merged() { return merged; }
    public Row parentRow() { return parentRow; }
    public Subtable subtable() { return subtable; }
    public Subrow subrow() { return subrow; }

    // --- Layout setters (package-private, set during table construction) ---

    void setParentRow(Row parentRow) { this.parentRow = parentRow; }
    void setSubtable(Subtable subtable) { this.subtable = subtable; }
    void setSubrow(Subrow subrow) { this.subrow = subrow; }

    // --- Formatting getters ---

    public FontFamily fontFamily() { return fontFamily; }
    public boolean fontBold() { return fontBold; }
    public boolean fontItalic() { return fontItalic; }
    public boolean fontStrikeout() { return fontStrikeout; }
    public boolean fontUnderline() { return fontUnderline; }
    public HorizontalAlignment horzAlign() { return horzAlign; }
    public VerticalAlignment vertAlign() { return vertAlign; }
    public boolean leftBorder() { return leftBorder; }
    public boolean topBorder() { return topBorder; }
    public boolean rightBorder() { return rightBorder; }
    public boolean bottomBorder() { return bottomBorder; }
    public CellColor bgColor() { return bgColor; }
    public CellColor fgColor() { return fgColor; }
    public double rotation() { return rotation; }

    // --- Formatting setters ---

    public void setFontFamily(FontFamily fontFamily) { this.fontFamily = Objects.requireNonNull(fontFamily); }
    public void setFontBold(boolean fontBold) { this.fontBold = fontBold; }
    public void setFontItalic(boolean fontItalic) { this.fontItalic = fontItalic; }
    public void setFontStrikeout(boolean fontStrikeout) { this.fontStrikeout = fontStrikeout; }
    public void setFontUnderline(boolean fontUnderline) { this.fontUnderline = fontUnderline; }
    public void setHorzAlign(HorizontalAlignment horzAlign) { this.horzAlign = Objects.requireNonNull(horzAlign); }
    public void setVertAlign(VerticalAlignment vertAlign) { this.vertAlign = Objects.requireNonNull(vertAlign); }
    public void setLeftBorder(boolean leftBorder) { this.leftBorder = leftBorder; }
    public void setTopBorder(boolean topBorder) { this.topBorder = topBorder; }
    public void setRightBorder(boolean rightBorder) { this.rightBorder = rightBorder; }
    public void setBottomBorder(boolean bottomBorder) { this.bottomBorder = bottomBorder; }
    public void setBgColor(CellColor bgColor) { this.bgColor = Objects.requireNonNull(bgColor); }
    public void setFgColor(CellColor fgColor) { this.fgColor = Objects.requireNonNull(fgColor); }
    public void setRotation(double rotation) { this.rotation = rotation; }

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
