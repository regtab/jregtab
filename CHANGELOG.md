# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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
