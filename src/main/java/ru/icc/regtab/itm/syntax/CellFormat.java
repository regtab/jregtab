package ru.icc.regtab.itm.syntax;

import java.util.Objects;

/**
 * Formatting properties of a cell (Def. 3): font, alignment, borders, colours, rotation.
 * <p>
 * Immutable; a {@link Cell} points to the shared {@link #DEFAULT} instance until one of its
 * formatting setters is called, and every setter derives a new instance (copy-on-write). Most
 * cells of a large table never change their formatting, so this keeps the per-cell footprint small.
 */
public record CellFormat(
        FontFamily fontFamily,
        boolean fontBold,
        boolean fontItalic,
        boolean fontStrikeout,
        boolean fontUnderline,
        HorizontalAlignment horzAlign,
        VerticalAlignment vertAlign,
        boolean leftBorder,
        boolean topBorder,
        boolean rightBorder,
        boolean bottomBorder,
        CellColor bgColor,
        CellColor fgColor,
        double rotation
) {
    public static final CellFormat DEFAULT = new CellFormat(
            FontFamily.SERIF, false, false, false, false,
            HorizontalAlignment.LEFT, VerticalAlignment.TOP,
            false, false, false, false,
            CellColor.WHITE, CellColor.BLACK, 0.0);

    public CellFormat {
        Objects.requireNonNull(fontFamily, "fontFamily");
        Objects.requireNonNull(horzAlign, "horzAlign");
        Objects.requireNonNull(vertAlign, "vertAlign");
        Objects.requireNonNull(bgColor, "bgColor");
        Objects.requireNonNull(fgColor, "fgColor");
    }

    public CellFormat withFontFamily(FontFamily v)       { return new CellFormat(v, fontBold, fontItalic, fontStrikeout, fontUnderline, horzAlign, vertAlign, leftBorder, topBorder, rightBorder, bottomBorder, bgColor, fgColor, rotation); }
    public CellFormat withFontBold(boolean v)            { return new CellFormat(fontFamily, v, fontItalic, fontStrikeout, fontUnderline, horzAlign, vertAlign, leftBorder, topBorder, rightBorder, bottomBorder, bgColor, fgColor, rotation); }
    public CellFormat withFontItalic(boolean v)          { return new CellFormat(fontFamily, fontBold, v, fontStrikeout, fontUnderline, horzAlign, vertAlign, leftBorder, topBorder, rightBorder, bottomBorder, bgColor, fgColor, rotation); }
    public CellFormat withFontStrikeout(boolean v)       { return new CellFormat(fontFamily, fontBold, fontItalic, v, fontUnderline, horzAlign, vertAlign, leftBorder, topBorder, rightBorder, bottomBorder, bgColor, fgColor, rotation); }
    public CellFormat withFontUnderline(boolean v)       { return new CellFormat(fontFamily, fontBold, fontItalic, fontStrikeout, v, horzAlign, vertAlign, leftBorder, topBorder, rightBorder, bottomBorder, bgColor, fgColor, rotation); }
    public CellFormat withHorzAlign(HorizontalAlignment v) { return new CellFormat(fontFamily, fontBold, fontItalic, fontStrikeout, fontUnderline, v, vertAlign, leftBorder, topBorder, rightBorder, bottomBorder, bgColor, fgColor, rotation); }
    public CellFormat withVertAlign(VerticalAlignment v) { return new CellFormat(fontFamily, fontBold, fontItalic, fontStrikeout, fontUnderline, horzAlign, v, leftBorder, topBorder, rightBorder, bottomBorder, bgColor, fgColor, rotation); }
    public CellFormat withLeftBorder(boolean v)          { return new CellFormat(fontFamily, fontBold, fontItalic, fontStrikeout, fontUnderline, horzAlign, vertAlign, v, topBorder, rightBorder, bottomBorder, bgColor, fgColor, rotation); }
    public CellFormat withTopBorder(boolean v)           { return new CellFormat(fontFamily, fontBold, fontItalic, fontStrikeout, fontUnderline, horzAlign, vertAlign, leftBorder, v, rightBorder, bottomBorder, bgColor, fgColor, rotation); }
    public CellFormat withRightBorder(boolean v)         { return new CellFormat(fontFamily, fontBold, fontItalic, fontStrikeout, fontUnderline, horzAlign, vertAlign, leftBorder, topBorder, v, bottomBorder, bgColor, fgColor, rotation); }
    public CellFormat withBottomBorder(boolean v)        { return new CellFormat(fontFamily, fontBold, fontItalic, fontStrikeout, fontUnderline, horzAlign, vertAlign, leftBorder, topBorder, rightBorder, v, bgColor, fgColor, rotation); }
    public CellFormat withBgColor(CellColor v)           { return new CellFormat(fontFamily, fontBold, fontItalic, fontStrikeout, fontUnderline, horzAlign, vertAlign, leftBorder, topBorder, rightBorder, bottomBorder, v, fgColor, rotation); }
    public CellFormat withFgColor(CellColor v)           { return new CellFormat(fontFamily, fontBold, fontItalic, fontStrikeout, fontUnderline, horzAlign, vertAlign, leftBorder, topBorder, rightBorder, bottomBorder, bgColor, v, rotation); }
    public CellFormat withRotation(double v)             { return new CellFormat(fontFamily, fontBold, fontItalic, fontStrikeout, fontUnderline, horzAlign, vertAlign, leftBorder, topBorder, rightBorder, bottomBorder, bgColor, fgColor, v); }
}
