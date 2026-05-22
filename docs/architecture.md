# Architecture

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
| **2. Completion** | Interpretation actions are applied in operation-type order: FILL/PREFIX/SUFFIX → AVP → REC → CONCAT; each action uses its providers to retrieve items relative to the anchor and updates the working state |
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
`AtpToRtlSerializer` performs the inverse: a `TablePattern` built via the ATP API can be round-tripped back to an RTL string (used in `AtpRtlRoundTripTest`).
