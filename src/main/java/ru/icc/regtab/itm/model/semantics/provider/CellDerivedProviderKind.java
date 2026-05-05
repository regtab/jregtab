package ru.icc.regtab.itm.model.semantics.provider;

/**
 * Named instances of cell-derived item providers (Remark in ITM: Υ<sub>tbl</sub><sup>val</sup>,
 * Υ<sub>tbl</sub><sup>attr</sup>, Υ<sub>tbl</sub><sup>aux</sup>).
 * <p>
 * {@link #UNRESTRICTED} matches the legacy behaviour: full cell-derived set J with no type restriction
 * before predicate κ (use unrestricted {@code ProviderSpec.of(...)} factories in the pattern API).
 */
public enum CellDerivedProviderKind {
    /**
     * No J restriction; no anchor-type check (legacy / explicit control via predicate).
     */
    UNRESTRICTED,
    /**
     * Υ<sub>tbl</sub><sup>val</sup>: ι<sub>anch</sub> ∈ I<sub>tbl</sub><sup>val</sup>, J = I<sub>tbl</sub><sup>val</sup>.
     */
    VAL,
    /**
     * Υ<sub>tbl</sub><sup>attr</sup>: ι<sub>anch</sub> ∈ I<sub>tbl</sub><sup>val</sup>, J = I<sub>tbl</sub><sup>attr</sup>, k = 1.
     */
    ATTR,
    /**
     * Υ<sub>tbl</sub><sup>aux</sup>: ι<sub>anch</sub> ∈ I<sub>tbl</sub><sup>val</sup> ∪ I<sub>tbl</sub><sup>attr</sup>, J = I<sub>tbl</sub>.
     */
    AUX
}
