package ru.icc.regtab.itm.syntax;

/**
 * An RGB color with components in [0, 255].
 */
public record CellColor(int r, int g, int b) {

    public CellColor {
        validate(r, "r");
        validate(g, "g");
        validate(b, "b");
    }

    private static void validate(int value, String name) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException(name + " must be in [0, 255]: " + value);
        }
    }

    public static final CellColor BLACK = new CellColor(0, 0, 0);
    public static final CellColor WHITE = new CellColor(255, 255, 255);
}
