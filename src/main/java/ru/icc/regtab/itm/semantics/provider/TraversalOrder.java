package ru.icc.regtab.itm.semantics.provider;

/**
 * Traversal orders for scanning items by cell positions.
 * T = {→, ←, ↓, ↑}: row-major (→), reverse row-major (←),
 * column-major (↓), reverse column-major (↑).
 */
public enum TraversalOrder {
    ROW_MAJOR,
    REVERSE_ROW_MAJOR,
    COLUMN_MAJOR,
    REVERSE_COLUMN_MAJOR
}
