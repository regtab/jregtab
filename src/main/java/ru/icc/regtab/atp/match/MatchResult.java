package ru.icc.regtab.atp.match;

import java.util.List;
import java.util.Objects;

/**
 * Result of syntactic layer matching.
 *
 * @param success          whether matching succeeded
 * @param matchedPairs     ordered list of matched cell pairs M (empty if failed)
 * @param matchedSubtables matched subtable intervals to apply on success
 * @param matchedSubrows   matched subrow intervals to apply on success
 */
public record MatchResult(
        boolean success,
        List<MatchedPair> matchedPairs,
        List<MatchedSubtable> matchedSubtables,
        List<MatchedSubrow> matchedSubrows
) {

    public MatchResult {
        matchedPairs = List.copyOf(Objects.requireNonNull(matchedPairs, "matchedPairs"));
        matchedSubtables = List.copyOf(Objects.requireNonNull(matchedSubtables, "matchedSubtables"));
        matchedSubrows = List.copyOf(Objects.requireNonNull(matchedSubrows, "matchedSubrows"));
    }

    /** A failed match result with no pairs. */
    public static MatchResult failure() {
        return new MatchResult(false, List.of(), List.of(), List.of());
    }

    /** A successful match result with the given pairs. */
    public static MatchResult success(
            List<MatchedPair> pairs,
            List<MatchedSubtable> matchedSubtables,
            List<MatchedSubrow> matchedSubrows) {
        return new MatchResult(true, pairs, matchedSubtables, matchedSubrows);
    }
}
