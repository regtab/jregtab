package ru.icc.regtab.tasks;

/**
 * How to compare actual vs expected recordsets for tests.
 * <ul>
 *   <li>{@link #STRICT} — order matters (schema column order / row order).</li>
 *   <li>{@link #FLEXIBLE} — order ignored (same multiset of columns as sets / same multiset of rows).</li>
 * </ul>
 */
public enum OrderPolicy {
    STRICT,
    FLEXIBLE
}
