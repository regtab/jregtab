package ru.icc.regtab.itm.model.semantics.provider;

/**
 * Named instances of context-derived item providers (Remark in ITM: Υ<sub>ctx</sub><sup>val</sup>,
 * Υ<sub>ctx</sub><sup>attr</sup>, Υ<sub>ctx</sub><sup>aux</sup>).
 * <p>
 * {@link #UNRESTRICTED}: fixed sequence returned for any anchor (legacy).
 */
public enum ContextDerivedProviderKind {
    UNRESTRICTED,
    /** Υ<sub>ctx</sub><sup>val</sup>: fixed sequence of context value items. */
    VAL,
    /** Υ<sub>ctx</sub><sup>attr</sup>: exactly one context attribute item. */
    ATTR,
    /** Υ<sub>ctx</sub><sup>aux</sup>: fixed sequence of context auxiliary items. */
    AUX
}
