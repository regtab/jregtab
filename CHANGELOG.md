# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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
