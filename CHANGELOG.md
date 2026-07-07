# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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
