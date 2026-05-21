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
    /** O_rec: construct a record item sequence. */
    REC,
    /** O_concat: concatenate record item sequences. */
    CONCAT
}
