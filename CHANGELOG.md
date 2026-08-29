# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.7.1] - 2026-08-29

### Fixed

- **Zero-width subrows no longer break the matcher.** An explicit subrow `{…}` whose cell
  patterns all matched zero cells (e.g. `{ [BLANK]* }` in a row without blank cells) used to
  throw `IllegalArgumentException: colEnd must be >= colStart` in the middle of a row and to
  silently fail ("pattern did not match") at the end of a row, because a trailing pattern was
  never attempted once the row was exhausted. Such a subrow now matches the empty sequence,
  as `*` does in regular expressions: it is tried at the end of the row as well, a repeated
  empty match (`{ [BLANK]* }+`, `{ [BLANK]* }*`, `{ [BLANK]* }{n}`) counts as a single empty
  iteration and never loops, and an empty subrow is not materialized in the interpretable
  table (it covers no cells, so the spatial index of 0.5.2 is unaffected). The same applies to
  a subtable whose row patterns are all optional. Found while evaluating RTL on ATBench
  (regtab-eval-on-atbench, `reports/full-run.md` §5); the workaround
  `{ [BLANK]* [!BLANK ? VAL: COL->AVP] [BLANK]* }+` keeps its outcome, and the natural form
  `{ [!BLANK ? VAL: COL->AVP] }+ { [BLANK]* }` now works.
- **`{1}` and `{0}` quantifiers are accepted.** `Quantifier.exactly(n)` required `n ≥ 2` and
  the RTL compiler let the raw `IllegalArgumentException: EXACTLY requires n >= 2` escape.
  `{1}` is now equivalent to no quantifier and `{0}` to zero occurrences (an empty match);
  only a negative `n` is rejected. An invalid `{n}` (negative, or out of `int` range) is
  reported as an `RtlCompileException` with the source position instead of a runtime exception.
- API: `MatchedSubrow` / `MatchedSubtable` allow an empty interval (`colEnd == colStart - 1`,
  `rowEnd == rowStart - 1`) and gain `empty(...)`, `isEmpty()`, `width()` / `height()`;
  `Quantifier.exactly(n)` accepts `n ≥ 0`.
- Conformance: semantic cases `subrow_zero_width_mid`, `subrow_zero_width_tail`,
  `subrow_zero_width_repeated` (`+`), `subrow_zero_width_repeated_star` (`*`),
  `quantifier_exactly_one`, `quantifier_exactly_zero`; negative case
  `quantifier_exactly_negative`; curated positive case `quantifier_small_n`.

## [0.7.0] - 2026-08-29

### Added
- **Named key references in `CONCAT(K)` / `JOIN(K)`.** The key `K` may now list attribute
  *names* (string literals) besides 0-based positions: `CONCAT(0, 1, 'A', 'B')`, `JOIN('Year')`,
  mixed in any order. A name is resolved per record — to the position of the item carrying that
  attribute — so the key no longer depends on the order of the fields (the rows
  `k1 | k11 | c1 | a1:A | b1:B` need `CONCAT(0,1,3,4)` positionally but `CONCAT(0,1,'A','B')` by
  name, the same key as for `k1 | k11 | a1:A | b1:B | c1`). A name missing from a record, or
  carried with a different value, is a key mismatch (`CONCAT`: no effect + diagnostic; `JOIN`: the
  pair is dropped). The semantics of the operations is unchanged; positional keys behave exactly
  as before. Canonical form: positions ascending, then names in lexicographic order,
  single-quoted. `CONCAT('')` is a compile error.
- API: `RecordKey(positions, names)` (`ru.icc.regtab.itm.semantics.operation`) with
  `RecordKey.positions(…)`, `RecordKey.names(…)`, `RecordKey.of(…)`, `RecordKey.EMPTY`;
  `ActionSpec.key()`, `ActionSpec.concat(String keyName, …)`, `ActionSpec.concat(RecordKey, …)`,
  `ActionSpec.join(String, …)`, `ActionSpec.join(RecordKey, …)`; the same overloads in the
  embedded DSL (`Rtl.concat`, `Rtl.join`).
- Conformance: curated positive extra `named_key`; semantic cases `concat_named_key` and
  `join_named_key`; negative case `concat_empty_key_name`.
- **Diagnostics for a forgotten `REC`.** An explicit `CONCAT`/`JOIN` (written on the anchor's own
  content spec) whose anchor has no record, or none of whose provided items has a record, used to
  be skipped silently — the anchor just vanished from the recordset. Both cases are now reported
  through `TableInterpreter.diagnostics()` (`anchor has no record — REC missing?`,
  `none of the provided items has a record — REC missing on the provider side?`) and raise under
  `withStrictPreconditions(true)`. Inherited actions (row/subrow/subtable/table-level `actSpecs`)
  and anchors whose record was folded away by an earlier `CONCAT` are not reported. The semantics
  of the operations is unchanged: a violated precondition still has no effect.
- API: `InterpretationAction.inherited()` (new record component; the three-argument constructor is
  kept and means `inherited = false`); `WorkingState.report(anchor, operation, message)`,
  `WorkingState.isConcatenated(item)`, `WorkingState.allConcatenated()` — the set `C` of
  concatenated-away anchors, the counterpart of `J` for `CONCAT`.
- Tests: the task corpus (`RtlTask*Test`, `AtpTask*Test`) and the semantic conformance runner now
  assert that interpretation produces no diagnostics.

### Changed
- `InterpretationAction` gained a fourth record component `boolean inherited`: positional
  deconstruction patterns (`instanceof InterpretationAction(var a, var p, var o)`) need a fourth
  binding; construction through the three-argument constructor is unaffected.
- `ActionSpec`: the record component `Set<Integer> keyPositions` is replaced by `RecordKey key`;
  `ConcatOperation(RecordKey key)` and `JoinOperation(RecordKey key)` likewise, and
  `WorkingState.applyConcat/applyJoin` take a `RecordKey`. The previous `ActionSpec`
  constructor taking `Set<Integer> keyPositions`, the accessor `keyPositions()` and the factories
  `concat(int|Set<Integer>, …)` / `join(int|Set<Integer>, …)` are kept and delegate. Only code
  constructing `ActionSpec` through the 7-component canonical constructor with a `Set<Integer>`
  positional argument, or calling `new ConcatOperation(Set.of(…))`, needs to switch to
  `RecordKey.positions(…)`.
- Downstream: pyRegTab needs to implement named key references to pass the new conformance cases.

## [0.6.0] - 2026-08-28

### Changed (breaking)
- **`JOIN(K)` is now the record product; the folding operation is `CONCAT(K)`.** Up to 0.5.x
  `JOIN(K)` *folded* records: the records of the provided anchors were concatenated to the anchor's
  record (one wide record, the number of records strictly decreased) — what `pandas.concat(axis=1)`
  does, not what a join does. The operation keeps that semantics under its original name
  `CONCAT(K)` (RTL `CONCAT`, `CONCAT(k1, k2, …)`; `ActionSpec.concat(…)`, `Rtl.concat(…)`,
  `ConcatOperation`, `OperationType.CONCAT`, `WorkingState.applyConcat`). `JOIN(K)` is redefined
  as the **record product**: every record of the anchor is combined with every record of the
  provided anchors — a cross product for `K = ∅`, an equi-join on the key positions `K` otherwise;
  a named attribute shared by the two records is a natural-join condition (the pair is kept only
  if the values agree, the attribute occurs once). The provided anchors become *joined-away*
  (`J`): they are excluded from the recordset but their records stay available, so several
  anchors may join the same records irrespective of action order. Migration: replace `JOIN(K)`
  by `CONCAT(K)` in every existing pattern (`JOIN` → `CONCAT`, `JOIN(0)` → `CONCAT(0)`, …); the
  corpus tasks 016, 023, 025, 033, 046, 047, 050, 053, 069, 094, 097, 098 were migrated this way
  and produce byte-identical recordsets
- **`CONCAT(K)` no longer deduplicates named attributes.** The former `JOIN(K)` silently kept the
  first occurrence of a named attribute shared by the concatenated records, which hid
  specification errors (a pattern that joined several stacked cells to one token lost all but
  the first `value`). Now a shared named attribute (apart from the key positions `K`) is a
  precondition violation: the action has **no effect** — both records survive — and a
  `Diagnostic` is recorded (`TableInterpreter.diagnostics()`); `withStrictPreconditions(true)`
  raises an `IllegalStateException` instead. The key positions are likewise checked (`compat_K`).
  In the corpus this changed one pattern: task 098 lists its full group key,
  `(BW&STR)*->CONCAT(0,1,2,3)` instead of `JOIN(0,1)` — the named attributes `A`/`B` repeated on
  every row of a group are part of the key, not duplicates to drop; the expected recordsets are unchanged
- **Working state: `rec` is multi-valued, recordsets are multisets.** `rec(ι)` is a non-empty
  sequence of item-based records (`WorkingState.rec(item)` → `List<List<Item>>`, a single record
  until a join multiplies it); the working state gains the component `J` (`WorkingState.allJoined()`,
  `isJoined(item)`), and `WorkingState.allRec()` returns the **live** anchors `dom(rec) \ J` only —
  exactly what recordset extraction sees. `SchemaConstructionStrategy.buildVisitOrder` visits
  `(anchor, record, position)` triples. The order of records in a `Recordset` is a documented
  default (anchor visit order, then nested-loop order of the join), not part of the semantics
- Grammar: `concatOp : CONCAT (LPAREN INT (COMMA INT)* RPAREN)?`, keyword `CONCAT`; the ATP→RTL
  serializer emits `CONCAT(k1, k2)`; the VS Code grammar highlights `CONCAT`

### Added
- `Diagnostic` (`ru.icc.regtab.itm.semantics`), `WorkingState.diagnostics()`,
  `TableInterpreter.diagnostics()`, `TableInterpreter.withStrictPreconditions(boolean)`,
  `WorkingState(boolean strictPreconditions)`
- Conformance corpus, semantic section: `concat_by_key` (task 016 shape), `join_product`
  (explode × stack — six records from two rows), `join_equi_key` (`JOIN(0)` on a positional key)
- Docs: Example 6 (record join) and a `CONCAT` vs `JOIN` comparison in `examples.md`;
  `rtl-reference.md`, `model/itm.md`, `model/atp.md`, `api.md`, `architecture.md`,
  `embedded-rtl.md` updated

## [0.5.3] - 2026-08-27

### Changed
- **performance (memory)** — the per-cell footprint of the three layers is roughly halved, with no change in results or public API (only additions): `IntRange` no longer allocates a capturing lambda per instance (factories `ofRow`/`ofCol`/`ofValue`; `CellDerivedItem.rows/cols/pos` use them); cell formatting lives in an immutable, shared `CellFormat` holder with copy-on-write setters (`Cell.format()`/`setFormat()` added, all existing getters/setters unchanged) and a non-merged cell no longer carries its own `BoundingBox`; `WorkingState` keeps `val`/`attr`/`avp` in identity maps (items have identity semantics; only `rec` order is observable and it is unchanged); `Record` stores its values positionally (`Record(Schema, String[])` added, `Schema.indexOf` is O(1)). Live heap on a 1.19 M-cell table (identity pattern): 764 MB → 451 MB, minimal working `-Xmx` 1g → 576m; recordsets are byte-identical

## [0.5.2] - 2026-08-26

### Changed
- **performance** — cell-derived item providers no longer copy and sort the whole item set J on every call. `CellDerivedItemProvider.provide()` was O(|J|) in time and allocated three table-sized sets per action, which made interpretation quadratic in the number of cells (7 318 × 5 cells — 198 s; 32 777 × 3 — over 30 min). Now a spatial index over J (`CellDerivedItemIndex`: row-major and column-major orderings with row/column offsets, built once per table, lazily) yields the candidates already in traversal order, and a `CandidateScope` derived statically from the filter specification (`CandidateScopes.of`) restricts the scan to the anchor's row, column, cell, row range or subtable — the filter κ is still applied to every candidate of the scope, and the scan stops after k matches, so the result of Υ^{J,k}_{τ,κ} is unchanged (pinned by `CellDerivedItemProviderEquivalenceTest` against the reference definition on random tables). `SemanticConstructor` now instantiates one provider / operation per `ProviderSpec` / `ActionSpec` (they are immutable and share the index) instead of one per action, so the memory of the semantic layer is linear in the number of cells with a small constant (1.19 M cells — identity pattern — runs in ~5 s within `-Xmx1g`). Related constant-factor fixes: `And`/`Or` filter conditions no longer re-create their term predicates per candidate, regex terms compile their pattern once, `Schema.contains` is O(1). Recordsets are byte-identical to 0.5.1; the public API only gains the new classes and one constructor

## [0.5.1] - 2026-08-26

### Fixed
- `ANCH(n)` / `REC(n)` (`AnchorAttributeAtPosition`) moved only the *values* to the requested position while the schema kept its original order, so with **named** attributes (produced by `AVP`) the attribute-value binding broke: the column carrying the anchor's name received another attribute's values. The transformation now moves the anchor **attribute** — its name travels with its values — so every record keeps its attribute-value pairs and only the schema order changes. All three ways of requesting it behave identically: the `<ANCH(n)>` settings prefix, inline `REC(n)` on an atomic content specification, and inline `REC(n)` inside a delimited one (`[(VAL: ROW*->REC(1)){','}]`)
- Conformance corpus: two semantic cases pinning the rule — `anch_named_attrs` (settings prefix, named schema) and `anch_named_inline_delim` (inline `REC(n)` under a delimited specification)

### Changed
- `ANCH(n)` / `REC(n)` on an **anonymous** schema: values and their positions are unchanged, but the anonymous names now travel with their attributes instead of being reassigned positionally, so the schema reads `$a_2, $a_3, $a_1, $a_4` rather than `$a_1, $a_2, $a_3, $a_4` — one rule for named and anonymous attributes alike, and the name shows which attribute was moved. Only visible to code that prints the schema or exports CSV with a header row; recordset contents are identical

## [0.5.0] - 2026-08-26

### Changed
- **BREAKING (semantics)** — delimited content specification `(VAL){"δ"}` now passes tokens through verbatim: surrounding whitespace is preserved and empty tokens are no longer dropped, so `n` substrings always derive `n` items (`"a, b"` → `"a"`, `" b"`; `"a,,b"` → `"a"`, `""`, `"b"`). This aligns the implementation with `def:delimited-content-spec` (where `sₖ ∈ Σ*`) and with `pandas str.split`, and makes the delimited specification behave like the atomic and compound ones, which already receive their text raw. Trimming is now opt-in via the atom's string extractor. **Migration:** if you relied on the implicit trimming, add `=TRIM` (or `=NORM`) to the delimited atom — `(VAL){","}` → `(VAL=TRIM){","}`. Atomic and compound specifications are unaffected; RTL syntax, the grammar and ATP→RTL serialization do not change
- Conformance corpus: new positive case `delim_raw` pinning both forms, and a normative “Semantics of S_delim” section in `conformance/README.md`

### Added
- Conformance corpus: executable **semantic** section (`conformance/semantic/<case>/` with `pattern.rtl`, `input.csv`, `expected.csv` and an optional `options.json`), run by `RtlSemanticConformanceTest` and added to the `conformance` CI job. Contract item 5: matching `pattern.rtl` against `input.csv` and interpreting the result must yield `expected.csv`. Items 1–4 pin syntax and canonical form only — two implementations can agree on the canonical RTL of a pattern and still execute it differently. Starter cases pin the S_delim rules: `delim_raw_tokens`, `delim_empty_tokens`, `delim_trim`, `compound_delim_raw`

## [0.4.2] - 2026-07-19

### Changed
- Docs: added a Pygments lexer (`rtl-lexer/`) for RTL code blocks, used by the docs site's syntax highlighting
- Docs: replaced em-dash annotations with `//` RTL comments in `docs/rtl-reference.md` code snippets so they are valid, correctly highlighted RTL
- Docs: VS Code install instructions now point at the dedicated [regtab/vscode-rtl](https://github.com/regtab/vscode-rtl) extension (VSIX releases with a bundled `rtl-lsp` language server) instead of copying `ide/vscode` manually

## [0.4.1] - 2026-07-10

### Fixed
- Clean build failure: `maven-compiler-plugin` no longer runs implicit annotation processing on this module's own sources, which previously failed a from-scratch build (e.g. on CI) because the self-registered `RtlSourceProcessor` was not yet compiled when the compiler tried to load it

### Changed
- Bumped `jackson-databind` (test scope) to 2.18.9, addressing known CVEs in that version range
- Central Publishing Portal auto-publish is now opt-in per release via Maven properties, without editing `pom.xml`; manual portal confirmation remains the default

## [0.4.0] - 2026-07-08

### Added
- IDE support for RTL (`ide/`): TextMate grammar and a VS Code extension highlighting `.rtl` files and RTL inside Java string literals (`RtlCompiler.compile(...)`, `/* language=RTL */` marker); the same directory imports into IntelliJ as a TextMate bundle
- `@Language("RTL")` on `RtlCompiler.compile(...)` parameters and the new `@RtlSource` annotation (`ru.icc.regtab.rtl`) for IDE language injection (`org.jetbrains:annotations`, provided scope)
- `RtlSourceProcessor` — annotation processor validating `@RtlSource`-annotated `String` constants at `javac` time; invalid RTL literals become Java compilation errors (documented in `docs/ide-support.md`)

## [0.3.0] - 2026-07-07

### Changed
- Embedded RTL: renamed `Rtl.sub(...)` to `Rtl.subtable(...)` for naming symmetry with `Rtl.subrow(...)` (both mirror `SubtablePattern`/`SubrowPattern`)

## [0.2.0] - 2026-07-07

### Added
- `EXT('name')` — external Java predicate bindings for RTL, supplied via the new `Bindings` class; usable in cell match conditions (`Predicate<Cell>`) and provider constraints (`BiPredicate<CellDerivedItem, CellDerivedItem>`); `EXT` constraints survive ATP→RTL serialization
- Embedded RTL: new `ru.icc.regtab.dsl` package (`Rtl`, `Prov`) — a Java DSL mirroring the RTL vocabulary 1:1 (combinators, providers, quantifiers, positional/content constraints, tags, actions, level-scoped action specs, fragments as Java variables, escape hatches), documented in `docs/embedded-rtl.md`
- RTL conformance corpus (`conformance/`) with an executable contract and a CI workflow (`.github/workflows/ci.yml`)

## [0.1.1] - 2026-06-21

### Added
- MkDocs documentation site with GitHub Pages deployment
- README badges (license, Maven Central, Java), links to docs site and Javadoc
- License reference for Foofah benchmark data in PROVENANCE

### Changed
- Consolidated and expanded model docs (itm.md, atp.md, RTL reference, API reference)
- Trimmed README to a showcase, moved low-level/benchmark content into docs

### Removed
- Stale CONCAT/ConcatOperation references from docs

## [0.1.0] - 2026-06-11

### Added
- Core RegTab library: Interpretable Table Model (ITM) and Regular Table Language (RTL)
- RTL compiler: grammar (`RTL.g4`), `ATPBuilder`, `ProviderTemplateResolver`
- RTL/ATP tests for tasks 001–150 (including Baikal benchmark, tasks 133–150)
- RTL grammar extensions: `ST` (sameSubtable), `STR` (sameStr), TAG OR-semantics, bare `&`-conjunctions, bare `condContSpec`
- Automatic `CellDerivedProviderKind` inference in `ATPBuilder`
- Published to Maven Central: `ru.icc.regtab:regtab:0.1.0`
