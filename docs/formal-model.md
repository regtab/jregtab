# Formal Model

This document maps the formal definitions from the paper
(*RegTab: Pattern-Driven Data Extraction from Document Tables with Regular Structure*)
to their Java counterparts in jRegTab.

For a narrative explanation of each model see:
[Table interpretation model (ITM)](model/interpretation.md) ·
[Table patterns (ATP)](model/patterns.md)

---

## Interpretable Table Model (ITM)

### Syntactic layer

**Definition (Syntactic layer):** `L_syn = (C, P)` where `C` is the finite set of cells and
`P = P_layout ∪ P_format ∪ P_content` is the set of cell properties.

| Formal concept | Java class / field |
|---|---|
| Cell set `C` | `TableSyntax` — cells accessed via `getCell(row, col)` |
| Primary position `pos(c) = (row, col)` | `Cell.row()`, `Cell.col()` |
| Merged-cell bounding box `bbox(c)` | `Cell.bbox()` → `BoundingBox` |
| Merge flag `merg(c)` | `Cell.isMerged()` |
| Subtable membership `subtable(c)` | `Cell.subtable()` → `Subtable` |
| Subrow membership `subrow(c)` | `Cell.subrow()` → `Subrow` |
| Font family `fntFamily(c)` | `Cell.fontFamily()` → `FontFamily` (SERIF/SANS_SERIF/MONOSPACED) |
| Bold/italic/strikeout/underline | `Cell.isBold()`, `Cell.isItalic()`, … |
| Horizontal/vertical alignment | `Cell.horzAlign()` → `HorizontalAlignment`, `Cell.vertAlign()` → `VerticalAlignment` |
| Borders `ltBorder`, `tpBorder`, … | `Cell.hasLeftBorder()`, `Cell.hasTopBorder()`, … |
| Colors `bgColor`, `fgColor` | `Cell.bgColor()`, `Cell.fgColor()` → `CellColor` |
| Text `txt(c)` | `Cell.text()` |
| Blank flag `txtBlank(c)` | `Cell.isBlank()` |

### Substructure hierarchy

The syntactic layer represents a table as a row-oriented hierarchy:
**table → subtables → rows → subrows → cells**.

| Formal concept | Java |
|---|---|
| Subtable set `ST` | `TableSyntax.subtables()` → `List<Subtable>` |
| Row set (within subtable) | `Subtable.rows()` → `List<Row>` |
| Subrow set `SR` | `Row.subrows()` → `List<Subrow>` |
| Cell set (within subrow) | `Subrow.cells()` → `List<Cell>` |

### Semantic layer

**Definition (Semantic layer):** `L_sem = (I_tbl, I_ctx, A)` where `I_tbl` is the set of
cell-derived items, `I_ctx` is the set of context-derived items, and `A` is the set of
interpretation actions.

**Cell-derived item:** a triple `(s, u⃗, i)` where `s ∈ Σ*` is the item string, `u⃗ ∈ U*` is a sequence of user-defined tags, and `i ∈ ℕ₀` is the item index within the cell.

| Formal concept | Java class / method |
|---|---|
| Cell-derived item | `CellDerivedItem` |
| String `str(ι)` | `CellDerivedItem.str()` |
| Tags `tags(ι)` | `CellDerivedItem.tags()` |
| Index `pos(ι)` | `CellDerivedItem.index()` |
| Source cell `cell(ι)` | `CellDerivedItem.cell()` |
| Item type (VAL/ATTR/AUX) | `CellDerivedItem.type()` → `ItemType` |
| Context-derived item | `ContextDerivedItem` |
| Semantic layer container | `TableSemantics` |

### Working state

**Definition (Working state):** `ws = (V, A, val, attr, avp, rec)` — accumulated semantic
information built up during table interpretation.

| Formal component | Java |
|---|---|
| Value set `V ⊆ Σ*` | `WorkingState.allVal()` → `Map<Item, String>` |
| Attribute set `A ⊆ Σ⁺` | `WorkingState.allAttr()` → `Map<Item, String>` |
| `val(ι)` | `WorkingState.val(item)` |
| `attr(ι)` | `WorkingState.attr(item)` |
| `avp(ι)` | `WorkingState.avp(item)` → `AttributeValuePair(attribute, value)` |
| `rec(ι)` | `WorkingState.rec(item)` → `List<Item>` |
| Derived `assoc(ι)` | `WorkingState.assoc(item)` — attribute of `avp(ι)`, or `null` |

Six **working-state update operations** are applied during working state completion:

| Formal operation | Java method | Anchor type |
|---|---|---|
| `O_fill^δ` | `WorkingState.applyFill(anchor, items, delimiter)` | VAL or ATTR |
| `O_prefix^δ` | `WorkingState.applyPrefix(anchor, items, delimiter)` | VAL or ATTR |
| `O_suffix^δ` | `WorkingState.applySuffix(anchor, items, delimiter)` | VAL or ATTR |
| `O_avp` | `WorkingState.applyAvp(anchor, items)` | VAL |
| `O_rec` | `WorkingState.applyRec(anchor, items)` | cell-derived VAL |
| `O_join^K` | `WorkingState.applyJoin(anchor, items, keyPositions)` | cell-derived VAL |

Consistency predicates:

| Predicate | Java method |
|---|---|
| Basic consistency: `rec(ι)[0] = ι` and `avp(ι) = (a,v) ⟹ val(ι) = v` | `WorkingState.isConsistent()` |
| Recordset-consistency: uniform anchor attribute + distinct non-anchor attributes per record | `WorkingState.isRecordsetConsistent()` |

### Item providers and filter conditions

**Item filter condition:** a predicate `κ : I_tbl × I_tbl → {false, true}` taking an anchor item and a candidate item.

| Concept | Java |
|---|---|
| Filter condition `κ` | `ItemFilterCondition` (functional interface), `ItemFilterConditionSpec` (structured) |
| Filter `Φ_κ(ι_anch, J)` | `ItemFilter.filter(anchor, items)` |
| Cell-derived provider spec `S_prov = (k, τ, κ)` | `ProviderSpec` (record: `cardinality`, `traversalOrder`, `filterCondition`, `targetItemKind`) |
| Cardinality `k` | `ProviderSpec.cardinality()` — use `ProviderSpec.UNBOUNDED` for ∞ |
| Traversal order `τ` | `ProviderSpec.traversalOrder()` → `TraversalOrder` (ROW_MAJOR / REVERSE_ROW_MAJOR / COLUMN_MAJOR / REVERSE_COLUMN_MAJOR) |
| Typed VAL provider `Υ_tbl^val` | `ProviderSpec.val(…)` |
| Typed ATTR provider `Υ_tbl^attr` | `ProviderSpec.attr(…)` — always cardinality 1 |
| Typed AUX provider `Υ_tbl^aux` | `ProviderSpec.aux(…)` |
| Unrestricted provider | `ProviderSpec.any(…)` |
| Context-derived provider | `ProviderSpec.ctxAttr(text)`, `ProviderSpec.ctxVal(text)`, `ProviderSpec.ctxAux(text)` |
| Runtime provider | `ItemProvider` (functional: `provide(anchor)`) |

**Atomic filter terms** (`FilterTerm` sealed interface, mirrors RTL `spatConstr`/`contConstr`):

| FilterTerm | RTL token | Condition |
|---|---|---|
| `SameSubtable` | `ST` | `sameSubtable(a) && !sameCell(a)` |
| `SameSubrow` | `SR` | `sameSubrow(a) && !sameCell(a)` |
| `SameSubcol` | `SC` | `sameSubcol(a) && !sameCell(a)` |
| `SameCell` | `CL` | `sameCell(a)` |
| `NotSameCell` | `NCL` | `!sameCell(a)` |
| `SameRow` | `ROW` | `sameRow(a) && !sameCell(a)` |
| `SameCol` | `COL` | `sameCol(a) && !sameCell(a)` |
| `RightOf` | `RT` | `sameSubrow(a) && col > col(a)` |
| `LeftOf` | `LT` | `sameSubrow(a) && col < col(a)` |
| `Below` | `BW` | `sameSubcol(a) && row > row(a)` |
| `Above` | `AV` | `sameSubcol(a) && row < row(a)` |
| `ColExact(n)` | `Cn` | `col == n` |
| `ColOffset(d)` | `C+n` / `C-n` | `col == col(a) + d` |
| `ColRange(f,t)` | `C+n..m` | `col(a)+f ≤ col ≤ col(a)+t` |
| `ColAbsoluteRange(lo,hi)` | `Clo..hi` | `lo ≤ col ≤ hi` |
| `RowExact(n)` | `Rn` | `row == n` |
| `RowOffset(d)` | `R+n` / `R-n` | `row == row(a) + d` |
| `PosExact(n)` | `Pn` | `index == n` |
| `PosOffset(d)` | `P+n` / `P-n` | `index == index(a) + d` |
| `PosRange(lo,hi)` | `Plo..hi` | `lo ≤ index ≤ hi` |
| `RegexMatched(pat)` | `"pat"` | `str.matches(pat)` |
| `NotRegexMatched(pat)` | `!"pat"` | `!str.matches(pat)` |
| `Contains(sub)` | `~"sub"` | `str.contains(sub)` |
| `NotContains(sub)` | `!~"sub"` | `!str.contains(sub)` |
| `Blank` | `BLANK` | `blankStr()` |
| `NotBlank` | `!BLANK` | `!blankStr()` |
| `Tagged(tags)` | `TAG #t1 #t2` | any tag matches (OR) |
| `NotTagged(tags)` | `!TAG #t1 #t2` | no tag matches |
| `SameStr` | `STR` | `sameStr(a)` |

Compound conditions: `ItemFilterConditionSpec.and(terms…)` → `(c1 & c2 & …)`;
`ItemFilterConditionSpec.or(groups…)` → `(g1 | g2 | …)`.

### Interpretation actions

**Action spec:** `S_act = (op, ⟨S_prov¹, …, S_provⁿ⟩)` where `op` is a working-state update operation.

| Operation | Java factory | Effect |
|---|---|---|
| `REC` | `ActionSpec.rec(providers…)` | Anchor item → record; providers supply the remaining fields |
| `AVP` | `ActionSpec.avp(provider)` | Associates a VAL item (anchor) with an ATTR item from the provider |
| `JOIN` | `ActionSpec.join(providers…)` | Joins item-based records: all items included, then dedup by named attribute (K=∅) |
| `JOIN(K)` | `ActionSpec.join(Set.of(0), providers…)` | Joins with key positions K dropped; `JOIN(0)` = old CONCAT |
| `FILL` | `ActionSpec.fill(delimiter, providers…)` | Fills anchor value forward using provider values |
| `PREFIX` | `ActionSpec.prefix(delimiter, providers…)` | Prepends provider values to the anchor |
| `SUFFIX` | `ActionSpec.suffix(delimiter, providers…)` | Appends provider values to the anchor |
| AVP with literal | `ActionSpec.avp("AIRLINE")` | Uses a context-derived ATTR constant |

`ActionSpec.rec(int anchorPos, providers…)` adds an `AnchorAttributeAtPosition` post-processing step (RTL: `REC(n)`).
`ActionSpec.rec(String splitDelimiter, providers…)` adds a `DelimitedFieldSplit` step (RTL: `REC('s')`).

### Recordset and schema

**Definition (Recordset):** given a schema `S = ⟨a₁, …, aₙ⟩`, a *record* is an
n-tuple `⟨(a₁,v₁), …, (aₙ,vₙ)⟩`; a *recordset* is a finite sequence of records.

| Formal concept | Java class / method |
|---|---|
| Schema `S = ⟨a₁, …, aₙ⟩` | `Schema` — `attributes()` → `List<String>` |
| Record `⟨(a₁,v₁), …, (aₙ,vₙ)⟩` | `Record` — `get(attribute)`, `get(index)` |
| Recordset | `Recordset` — `schema()`, `records()`, `size()`, `get(index)` |

### Table interpretation

`TableInterpreter.interpret(table)` executes the four phases defined in
[Table interpretation](model/interpretation.md#table-interpretation):

| Phase | Java entry point |
|---|---|
| 1 — Working state initialisation | `initWorkingState(sem)` (private) |
| 2 — Working state completion | `completeWorkingState(ws, actions)` (private) |
| 3 — Recordset extraction | `extractRecordset(ws)` → `constructSchema` + `generateRecords` |
| 4 — Recordset transformation | `transformRecordset(recordset)` |

**Strategies and options** (all configurable via `TableInterpreter.with*(…)`):

| Option | Java type | Values |
|---|---|---|
| Action application strategy `Γ_row` / `Γ_col` | `ActionApplicationStrategy` | `ROW_FIRST` (default), `COLUMN_FIRST` |
| Schema construction strategy `Γ_rec` / `Γ_pos` | `SchemaConstructionStrategy` | `RECORD_FIRST` (default), `POSITION_FIRST` |
| Anonymous attribute template | `String` | Pattern with `%i` placeholder; default `"$a_%i"` |
| Missing value handler `μ` | `MissingValueHandler` | Functional interface; default returns `null` |

**Recordset transformations** (`RecordsetTransformation` sealed interface):

| Transformation | Java class | RTL / API trigger |
|---|---|---|
| Schema reordering | `SchemaReordering` | explicit attribute list |
| Delimited field split | `DelimitedFieldSplit` | `ActionSpec.rec(String delimiter, …)` — RTL `REC('s')` |
| Field splitting | `FieldSplitting` | explicit split spec |
| Whitespace normalisation | `WhitespaceNormalization` | added via `withTransformations(…)` |
| Anchor attribute at position | `AnchorAttributeAtPosition` | `ActionSpec.rec(int pos, …)` — RTL `REC(n)` |

---

## Abstract Table Pattern (ATP)

An ATP instance mirrors the ITM hierarchy: `TablePattern → SubtablePattern → RowPattern → SubrowPattern → CellPattern`.

### Pattern hierarchy

| Level | Java class | Key fields |
|---|---|---|
| Table | `TablePattern` | `subtablePatterns: List<SubtablePattern>` |
| Subtable | `SubtablePattern` | `quantifier`, `rowPatterns: List<RowPattern>` |
| Row | `RowPattern` | `quantifier`, `condition?`, `actSpecs?`, `subrowPatterns: List<SubrowPattern>` |
| Subrow | `SubrowPattern` | `quantifier`, `condition?`, `actSpecs?`, `cellPatterns: List<CellPattern>` |
| Cell | `CellPattern` | `condition?`, `quantifier`, `contentSpec?` |

All levels except `TablePattern` carry a `Quantifier` (zeroOrOne / zeroOrMore / oneOrMore / exactly(n)).

### Content specifications

`ContentSpec` is a sealed interface permitting four variants:

| Variant | Java class | Usage |
|---|---|---|
| Atomic | `AtomicContentSpec` | Single item: `AtomicContentSpec.val(actSpec)`, `.attr(actSpec)`, `.aux(actSpec)`, `.skip()` |
| Delimited | `DelimitedContentSpec` | Repeated items split by a delimiter: `DelimitedContentSpec.of(atomSpec, separator)` |
| Compound | `CompoundContentSpec` | Multiple segments within one cell text: header + one or more `Segment(separator, atomSpec)` |
| Conditional | `ConditionalContentSpec` | Branch on a cell match condition: if true → spec A, else → spec B |

### Cell match condition

`CellMatchCondition` wraps a `CellPredicate` applied to the cell (not the item) at match time.
Available predicates: `CellPredicate.Blank`, `CellPredicate.NotBlank`, `CellPredicate.Regex(pattern)`, `CellPredicate.NotRegex(pattern)`.

In RTL, a cell match condition appears at several levels:
- **Cell (guarded):** `[cond ? contSpec]` — the `?` is a required separator before the content spec, e.g. `[!BLANK ? VAL : …]`.
- **Cell (bare conditional):** `[cond ? trueSpec | falseSpec]` — `ConditionalContentSpec`; parentheses are not allowed. Action specs may precede the condition: `[acts cond ? x | y]`.
- **Cell (condition-only):** `[cond]` — no `?`; the cell is consumed if the condition holds but produces no item, e.g. `[BLANK]*` or `[!BLANK]`.
- **Subrow / row / subtable / table:** `{ cond ? … }` or `[ cond ? … ]` — `?` is always required at these levels since content always follows.

### Quantifier

| Java factory | Meaning |
|---|---|
| `Quantifier.zeroOrOne()` | 0 or 1 repetition (`?`) |
| `Quantifier.zeroOrMore()` | 0 or more (`*`) |
| `Quantifier.oneOrMore()` | 1 or more (`+`) |
| `Quantifier.exactly(n)` | exactly n (`{n}`) |
| `Quantifier.one()` | exactly 1 (default; no RTL suffix) |

### String extractors

`StringExtractor` transforms the raw cell text when deriving items.

| Java constant / factory | RTL | Effect |
|---|---|---|
| `StringExtractor.WhitespaceNormalized.INSTANCE` | `=NORM` | Collapse whitespace |
| `StringExtractor.UpperCase.INSTANCE` | `=UC` | To upper case |
| `StringExtractor.LowerCase.INSTANCE` | `=LC` | To lower case |
| `StringExtractor.Trimmed.INSTANCE` | `=TRIM` | Trim leading/trailing whitespace |
| `StringExtractor.Substring(begin, end)` | `=SUBSTR(n,m)` | Substring starting at *n*, length *m* (internally stored as `[begin, end)`) |
| `StringExtractor.Replaced(pat, repl)` | `=REPL("a","b")` | Replace pattern with replacement |
| Chained extractors | `=REPL("x","").NORM` | Applied left-to-right |
