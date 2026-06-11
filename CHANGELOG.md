# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2026-06-11

### Added
- Core RegTab library: Interpretable Table Model (ITM) and Regular Table Language (RTL)
- RTL compiler: grammar (`RTL.g4`), `ATPBuilder`, `ProviderTemplateResolver`
- RTL/ATP tests for tasks 001–150 (including Baikal benchmark, tasks 133–150)
- RTL grammar extensions: `ST` (sameSubtable), `STR` (sameStr), TAG OR-semantics, bare `&`-conjunctions, bare `condContSpec`
- Automatic `CellDerivedProviderKind` inference in `ATPBuilder`
- Published to Maven Central: `ru.icc.regtab:regtab:0.1.0`
