package ru.icc.regtab.itm.atp.spec;

import java.util.Objects;

/**
 * Conditional content specification S_cond (def:conditional-content-spec):
 * S_cond = (λ, S_x⁺, S_x⁻).
 * <p>
 * Branch S_x⁺ is selected if the matched cell c satisfies λ (c ⊨ λ),
 * and S_x⁻ otherwise; the selected branch is resolved as an atomic,
 * delimited, or compound specification.
 *
 * @param condition cell match condition λ
 * @param positive  content specification S_x⁺ selected when c ⊨ λ
 * @param negative  content specification S_x⁻ selected when c ⊭ λ
 */
public record ConditionalContentSpec(
        CellMatchCondition condition,
        ContentSpec positive,
        ContentSpec negative
) implements ContentSpec {

    public ConditionalContentSpec {
        Objects.requireNonNull(condition, "condition");
        Objects.requireNonNull(positive, "positive");
        Objects.requireNonNull(negative, "negative");
    }
}
