# Architecture

The library is organised around the following components:

| Component | Package | Description |
|---|---|---|
| **ITM Syntax** | `ru.icc.regtab.itm.syntax` | Syntactic layer of ITM: `TableSyntax`, `Cell`, `Row`, `Subrow`, `Subtable` |
| **ITM Semantics** | `ru.icc.regtab.itm.semantics` | Semantic layer of ITM: `TableSemantics`, `CellDerivedItem`, `ContextDerivedItem`, interpretation actions and providers |
| **ATP Spec** | `ru.icc.regtab.atp.spec` | Formal ATP types: `TablePattern`, `SubtablePattern`, `RowPattern`, `SubrowPattern`, `CellPattern`, content specifications (`AtomicContentSpec`, `DelimitedContentSpec`, `CompoundContentSpec`, `ConditionalContentSpec`), item provider specifications, and interpretation action specifications |
| **ATP Matcher** | `ru.icc.regtab.atp.match` | Matches an ATP instance against an ITM instance; on success populates the semantic layer |
| **RTL Compiler** | `ru.icc.regtab.rtl` | Compiles RTL DSL strings to ATP (`RtlCompiler`, ANTLR4 grammar) |
| **ATP→RTL Serializer** | `ru.icc.regtab.rtl` | Inverse direction: serializes a `TablePattern` back to an RTL string (`AtpToRtlSerializer`) |
| **Table Interpreter** | `ru.icc.regtab.interpret` | `TableInterpreter` derives a `Recordset` from an `InterpretableTable`; supports configurable `SchemaConstructionStrategy` and post-processing steps (`WhitespaceNormalization`, `FieldSplitting`, `SchemaReordering`) |
| **Recordset** | `ru.icc.regtab.recordset` | `Recordset`, `Record`, `Schema` |

## Package map

```
ru.icc.regtab
├── itm/
│   ├── InterpretableTable.java        — union of syntax + semantics layers
│   ├── syntax/                        — syntactic layer
│   │   ├── TableSyntax.java           — grid of cells
│   │   ├── Cell.java                  — cell with position, formatting, text
│   │   ├── Row.java / Subrow.java / Subtable.java
│   │   └── BoundingBox, GridPosition, CellColor, …
│   └── semantics/                     — semantic layer
│       ├── TableSemantics.java        — items + interpretation actions
│       ├── WorkingState.java          — mutable state during interpretation
│       ├── action/InterpretationAction.java
│       ├── item/                      — CellDerivedItem, ContextDerivedItem, ItemType
│       ├── operation/                 — RecOperation, AvpOperation, ConcatOperation, …
│       ├── predicate/                 — DirectionalModifier, IntRange, …
│       └── provider/                  — ItemProvider, ItemFilter, TraversalOrder, …
├── atp/
│   ├── spec/                          — ATP formal types
│   │   ├── TablePattern / SubtablePattern / RowPattern / SubrowPattern / CellPattern
│   │   ├── ContentSpec (sealed) — AtomicContentSpec, DelimitedContentSpec,
│   │   │                          CompoundContentSpec, ConditionalContentSpec
│   │   ├── ActionSpec             — S_act = (op, ⟨S_prov¹, …⟩)
│   │   ├── ProviderSpec           — S_prov = (k, τ, κ)
│   │   ├── ItemFilterConditionSpec (sealed) — Bare / And / Or / Custom
│   │   ├── FilterTerm (sealed)    — all atomic spatial/content constraints
│   │   └── Quantifier, CellMatchCondition, StringExtractor, …
│   ├── AtpMatcher.java            — entry point for matching
│   └── match/                     — SyntaxMatcher, SemanticConstructor, MatchResult, …
├── rtl/
│   ├── RtlCompiler.java           — compiles RTL string → TablePattern
│   ├── AtpToRtlSerializer.java    — TablePattern → RTL string
│   ├── RtlCompileException.java
│   └── internal/                  — ATPBuilder (ANTLR visitor), ProviderTemplateResolver, …
├── interpret/
│   ├── TableInterpreter.java      — 4-phase interpretation
│   ├── SchemaConstructionStrategy — RECORD_FIRST / …
│   ├── ActionApplicationStrategy  — ROW_FIRST / …
│   ├── MissingValueHandler
│   └── RecordsetTransformation    — WhitespaceNormalization, FieldSplitting, SchemaReordering, …
└── recordset/
    ├── Recordset.java
    ├── Record.java
    └── Schema.java
```

---

## Data flow

```
   [Source data]           [ATP pattern]
        │                  (ATP API or
        ▼                   RtlCompiler.compile(rtl))
  TableSyntax                    │
        │                        │
        └──────────┬─────────────┘
                   ▼
       AtpMatcher.match(pattern, syntax)
                   │
                   ▼
      Optional<InterpretableTable>
        (syntax + populated semantic layer)
                   │
                   ▼
       TableInterpreter.interpret(itm)
                   │
                   ▼
               Recordset
```

If the pattern does not match, `AtpMatcher.match` returns `Optional.empty()`.

---

## Interpretation phases

`TableInterpreter.interpret(itm)` executes four phases defined in Section 3.3 of the paper:

| Phase | What happens |
|---|---|
| **1. Initialisation** | Each cell-derived and context-derived item of type VAL/ATTR is entered into the working state with its string value |
| **2. Completion** | Interpretation actions are applied in operation-type order: FILL/PREFIX/SUFFIX → AVP → REC → JOIN; each action uses its providers to retrieve items relative to the anchor and updates the working state |
| **3. Extraction** | The working state is traversed to build the schema (attribute list) and generate records |
| **4. Transformation** | Optional post-processing steps are applied: `WhitespaceNormalization`, `FieldSplitting`, `SchemaReordering` |

---

## RTL compilation pipeline

```
RTL string
    │
    ▼  ANTLR4 lexer+parser (RTLLexer / RTLParser)
 Parse tree
    │
    ▼  ATPBuilder (RTLBaseVisitor)
  ATP objects (TablePattern, …, CellPattern)
    │
    ▼  ProviderTemplateResolver
  Resolved ProviderSpec instances
    │
    ▼  RtlCompiler (wraps transformations)
 TablePattern  [+ List<RecordsetTransformation>]
```

The grammar lives at `src/main/antlr4/ru/icc/regtab/rtl/RTL.g4`.

Named fragment definitions (`$name=[body]`) in the RTL preamble are resolved during the
`ATPBuilder` pass: each reference expands to a fresh pattern object (syntactic substitution).
Fragments are supported at all four pattern levels: cell, row, subrow, and subtable.

---

## ATP→RTL serialization

`AtpToRtlSerializer.serialize(TablePattern)` is the inverse of `RtlCompiler.compile()`: it traverses a `TablePattern` object graph and produces the corresponding RTL string.

```
TablePattern  ──►  AtpToRtlSerializer.serialize()  ──►  RTL string
     ▲                                                       │
     └────────────  RtlCompiler.compile()  ◄─────────────────┘
```

The round-trip property — serialize then compile gives back the original pattern — is verified in `AtpRtlRoundTripTest` for all 50 Foofah benchmark tasks (001–050).

**What is serialized:**

| ATP construct | RTL output |
|---|---|
| `SubtablePattern` with `Quantifier.ONE` and no condition | implicit (no `{ }`) |
| `SubtablePattern` with other quantifier or condition | `{ ... }q` |
| `RowPattern`, `SubrowPattern`, `CellPattern` | `[ ... ]q`, `{ ... }q`, `[ ... ]q` |
| `AtomicContentSpec` with tags | `VAL #'tag'` |
| `AtomicContentSpec` with extractor | `VAL = TRIM` |
| `ActionSpec` (avp, rec, join, fill, prefix, suffix) | `'NAME'->AVP`, `(prov…)->REC`, etc. |
| `ProviderSpec` with traversal order | leading `-` / `^` / `-^` |
| `ProviderSpec` with cardinality | `{n}` / `*` |
| `RecordsetTransformation` settings | `<NORM>`, `<ANCH(n)>`, `<SPLIT("d")>` |

**Limitations:**

- Actions are emitted at the atom level (after `:`). Inherited action specs — those declared at subtable, row, or subrow level and merged down into `AtomicContentSpec.actions()` — are not reconstructed at their original level; they appear as cell-level actions in the output.
- `CellPredicate.Custom` and `ItemFilterConditionSpec.Custom` throw `UnsupportedOperationException` — only patterns without custom predicates can be serialized.
