package ru.icc.regtab.atp.spec;

/**
 * Operation type op (def:action-spec): identifies the working-state update
 * operation o to be applied when an interpretation action is executed.
 */
public enum OperationType {
    /** O_fill^δ: replace value/attribute with delimiter-joined provider strings. */
    FILL,
    /** O_prefix^δ: prepend delimiter-joined provider strings. */
    PREFIX,
    /** O_suffix^δ: append delimiter-joined provider strings. */
    SUFFIX,
    /** O_avp: construct an attribute–value pair. */
    AVP,
    /** O_rec: construct an item-based record. */
    REC,
    /** O_concat^K: concatenate item-based records into one wide record (key positions K not repeated). */
    CONCAT,
    /** O_join^K: record product — every record of the anchor by every joined record (equi-join on K). */
    JOIN
}
