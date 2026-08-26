package ru.icc.regtab.atp.spec;

import ru.icc.regtab.itm.semantics.provider.CandidateScope;
import ru.icc.regtab.itm.semantics.provider.CandidateScope.Interval;

/**
 * Derives a {@link CandidateScope} — a static over-approximation of the support of κ — from an
 * {@link ItemFilterConditionSpec}, so that a cell-derived provider can scan one row, column,
 * cell or subtable of the item index instead of the whole target set.
 * <p>
 * Soundness: for every term, the scope contains every candidate for which the term can hold
 * (a term that does not constrain the position contributes nothing); a conjunction is the
 * intersection of its terms' scopes; a disjunction or an opaque condition yields
 * {@link CandidateScope#ALL}. The filter κ is still applied to every candidate of the scope,
 * so the provider result is unchanged.
 */
public final class CandidateScopes {

    private CandidateScopes() {
    }

    public static CandidateScope of(ItemFilterConditionSpec spec) {
        return switch (spec) {
            case ItemFilterConditionSpec.Bare b -> of(b.constraint());
            case ItemFilterConditionSpec.And a -> {
                CandidateScope scope = CandidateScope.ALL;
                for (FilterTerm term : a.terms()) {
                    scope = scope.and(of(term));
                }
                yield scope;
            }
            case ItemFilterConditionSpec.Or ignored -> CandidateScope.ALL;
            case ItemFilterConditionSpec.Custom ignored -> CandidateScope.ALL;
        };
    }

    public static CandidateScope of(FilterTerm term) {
        return switch (term) {
            // c.sameSubrow(a) && col < col(a): same row, columns to the left
            case FilterTerm.LeftOf ignored ->
                    new CandidateScope(Interval.relative(0), Interval.relative(Interval.OPEN_LO, -1), false);
            case FilterTerm.RightOf ignored ->
                    new CandidateScope(Interval.relative(0), Interval.relative(1, Interval.OPEN_HI), false);
            // c.sameSubcol(a) && row < row(a): same subtable, same column, rows above
            case FilterTerm.Above ignored ->
                    new CandidateScope(Interval.relative(Interval.OPEN_LO, -1), Interval.relative(0), true);
            case FilterTerm.Below ignored ->
                    new CandidateScope(Interval.relative(1, Interval.OPEN_HI), Interval.relative(0), true);
            case FilterTerm.SameSubrow ignored -> CandidateScope.rows(Interval.relative(0));
            case FilterTerm.SameSubcol ignored -> new CandidateScope(null, Interval.relative(0), true);
            case FilterTerm.SameSubtable ignored -> CandidateScope.subtable();
            case FilterTerm.SameRow ignored -> CandidateScope.rows(Interval.relative(0));
            case FilterTerm.SameCol ignored -> CandidateScope.cols(Interval.relative(0));
            case FilterTerm.SameCell ignored ->
                    new CandidateScope(Interval.relative(0), Interval.relative(0), false);
            case FilterTerm.ColExact c -> CandidateScope.cols(Interval.absolute(c.n()));
            case FilterTerm.ColOffset c -> CandidateScope.cols(Interval.relative(c.delta()));
            case FilterTerm.ColRange c ->
                    CandidateScope.cols(Interval.relative(c.from(), openHi(c.to())));
            case FilterTerm.ColAbsoluteRange c ->
                    CandidateScope.cols(Interval.absolute(c.lo(), openHi(c.hi())));
            case FilterTerm.RowExact r -> CandidateScope.rows(Interval.absolute(r.n()));
            case FilterTerm.RowOffset r -> CandidateScope.rows(Interval.relative(r.delta()));
            case FilterTerm.RowAbsoluteRange r ->
                    CandidateScope.rows(Interval.absolute(r.lo(), openHi(r.hi())));
            // position, content, sameStr, external / custom predicates and NCL do not restrict the position
            default -> CandidateScope.ALL;
        };
    }

    /** {@code Integer.MAX_VALUE} denotes an open upper end in the range terms. */
    private static int openHi(int hi) {
        return hi == Integer.MAX_VALUE ? Interval.OPEN_HI : hi;
    }
}
