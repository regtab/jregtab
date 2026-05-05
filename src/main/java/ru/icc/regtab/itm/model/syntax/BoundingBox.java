package ru.icc.regtab.itm.model.syntax;

/**
 * Merged-cell bounding box: (topLeft, bottomRight) grid positions.
 * For non-merged cells, topLeft == bottomRight == pos(c).
 */
public record BoundingBox(GridPosition topLeft, GridPosition bottomRight) {

    public BoundingBox {
        if (topLeft == null) throw new IllegalArgumentException("topLeft must not be null");
        if (bottomRight == null) throw new IllegalArgumentException("bottomRight must not be null");
        if (topLeft.row() > bottomRight.row() || topLeft.col() > bottomRight.col()) {
            throw new IllegalArgumentException(
                    "topLeft must not exceed bottomRight: " + topLeft + " vs " + bottomRight);
        }
    }

    public int rowSpan() {
        return bottomRight.row() - topLeft.row() + 1;
    }

    public int colSpan() {
        return bottomRight.col() - topLeft.col() + 1;
    }

    public static BoundingBox single(GridPosition pos) {
        return new BoundingBox(pos, pos);
    }
}
