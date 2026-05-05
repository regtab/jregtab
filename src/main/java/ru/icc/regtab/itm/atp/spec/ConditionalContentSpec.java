package ru.icc.regtab.itm.atp.spec;

import java.util.Objects;

/**
 * Conditional content specification (Def. 26):
 * CS_cond = (λ, CS_x⁺, CS_x⁻).
 * <p>
 * If the matched cell satisfies λ, then CS_x⁺ governs the cell;
 * otherwise CS_x⁻ governs it.
 *
 * @param condition cell match condition λ
 * @param positive  content specification when λ is satisfied (CS_x⁺)
 * @param negative  content specification when λ is not satisfied (CS_x⁻)
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
