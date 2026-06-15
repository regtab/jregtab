# API reference

This page covers the public Java API. All classes live under `ru.icc.regtab`.

---

## Entry points

| Class | Package | Purpose |
|---|---|---|
| `RtlCompiler` | `rtl` | Compile an RTL string → `TablePattern` |
| `TablePattern` | `atp.spec` | Build a pattern directly in Java |
| `AtpMatcher` | `atp` | Match a pattern against a `TableSyntax` |
| `TableInterpreter` | `interpret` | Interpret a matched table → `Recordset` |

---

## RTL compiler

### `RtlCompiler`

Utility class (no constructor). Compiles RTL source strings to `TablePattern` instances.

```java
TablePattern pattern = RtlCompiler.compile("""
    [ [ATTR]+ ]
    [ [VAL : (^COL)->AVP, (SR)->REC]+ ]+
    """);
```

| Method | Description |
|---|---|
| `static TablePattern compile(String rtl)` | Parses and compiles the RTL string. Throws `RtlCompileException` on syntax or semantic errors. |

---

## Pattern specification (`atp.spec`)

These classes mirror the formal ATP hierarchy: table → subtables → rows → subrows → cells.

### `TablePattern`

```java
// Two-subtable pattern
TablePattern p = TablePattern.of(headerSubtable, dataSubtable);
```

| Member | Description |
|---|---|
| `static TablePattern of(SubtablePattern... subtables)` | Factory; requires at least one subtable. |
| `TablePattern withTransformations(RecordsetTransformation... transforms)` | Returns a copy with new post-processing steps. |
| `List<SubtablePattern> subtablePatterns()` | Ordered subtable patterns (≥ 1). |
| `List<RecordsetTransformation> transformations()` | Applied to the recordset after interpretation. |

---

### `SubtablePattern`

```java
SubtablePattern.of(Quantifier.oneOrMore(), row1, row2)  // repeating subtable
SubtablePattern.of(row1, row2)                           // exactly-one (default)
```

| Member | Description |
|---|---|
| `static SubtablePattern of(RowPattern... rows)` | Default quantifier ONE. |
| `static SubtablePattern of(Quantifier q, RowPattern... rows)` | With explicit quantifier. |

---

### `RowPattern`

```java
RowPattern.of(cell1, cell2)                     // one subrow, quantifier ONE
RowPattern.of(Quantifier.oneOrMore(), cell1)    // repeating row
```

| Member | Description |
|---|---|
| `static RowPattern of(CellPattern... cells)` | Single implicit subrow. |
| `static RowPattern of(Quantifier q, CellPattern... cells)` | Repeating row. |
| `static RowPattern of(Quantifier q, SubrowPattern... subrows)` | Explicit subrow patterns. |
| `static RowPattern of(CellMatchCondition cond, Quantifier q, CellPattern... cells)` | With row-level condition. |

---

### `SubrowPattern`

```java
SubrowPattern.of(cell1, cell2)
SubrowPattern.of(Quantifier.exactly(3), cell)
```

| Member | Description |
|---|---|
| `static SubrowPattern of(CellPattern... cells)` | Default quantifier ONE. |
| `static SubrowPattern of(Quantifier q, CellPattern... cells)` | With explicit quantifier. |

---

### `CellPattern`

```java
CellPattern.skip()                              // absorbs a cell, produces no item
CellPattern.skip(Quantifier.oneOrMore())        // absorbs one-or-more cells
CellPattern.of(AtomicContentSpec.val())         // value cell, quantifier ONE
CellPattern.of(Quantifier.exactly(2), cs)       // two cells with content spec cs
CellPattern.of(cond, Quantifier.one(), cs)      // guarded cell
```

| Member | Description |
|---|---|
| `static CellPattern skip()` | Skip cell (null contentSpec). |
| `static CellPattern skip(Quantifier q)` | Skip with quantifier. |
| `static CellPattern of(ContentSpec cs)` | Quantifier ONE. |
| `static CellPattern of(Quantifier q, ContentSpec cs)` | With quantifier. |
| `static CellPattern of(CellMatchCondition cond, Quantifier q, ContentSpec cs)` | With guard condition. |

---

### Content specifications

All implement the sealed interface `ContentSpec`.

#### `AtomicContentSpec`

Derives a single item from a cell.

```java
AtomicContentSpec.val()                         // plain value, no actions
AtomicContentSpec.attr()                        // attribute item
AtomicContentSpec.aux()                         // auxiliary item
AtomicContentSpec.val(ActionSpec.rec(...))      // value with REC action
AtomicContentSpec.val(StringExtractor.WhitespaceNormalized.INSTANCE)
```

| Factory method | Description |
|---|---|
| `val()` / `attr()` / `aux()` | Item type, no extractor, no actions. |
| `val(ActionSpec... actions)` | Value with actions. |
| `attr(ActionSpec... actions)` | Attribute with actions. |
| `val(StringExtractor extractor, ActionSpec... actions)` | Value with custom text extractor. |
| `valTagged(String tag, ActionSpec... actions)` | Value restricted by tag. |

---

#### `DelimitedContentSpec`

Splits a cell's text on a delimiter; applies an atomic spec to each segment.

```java
new DelimitedContentSpec(",", AtomicContentSpec.val(ActionSpec.rec()))
// equivalent RTL: (VAL){","}
```

| Component | Description |
|---|---|
| `delimiter()` | Split delimiter (non-empty). |
| `atomicSpec()` | Applied to each substring. |

---

#### `CompoundContentSpec`

Splits a cell into fixed segments separated by known delimiters.

```java
CompoundContentSpec.of(
    AtomicContentSpec.val(ActionSpec.avp("ND")),
    CompoundContentSpec.Segment.of(" ", AtomicContentSpec.val(ActionSpec.avp("MON")))
)
// matches a cell like "0 Jan" → ND=0, MON=Jan
```

| Member | Description |
|---|---|
| `static CompoundContentSpec of(ContentSpec first, Segment... rest)` | Leading spec + delimited segments. |
| `static Segment of(String delimiter, ContentSpec spec)` | One (delimiter, spec) pair. |

---

#### `ConditionalContentSpec`

Chooses between two content specs based on a cell condition.

```java
new ConditionalContentSpec(
    new CellMatchCondition(CellPredicate.Blank.INSTANCE),
    AtomicContentSpec.skip(),
    AtomicContentSpec.val()
)
// equivalent RTL: BLANK ? SKIP | VAL
```

---

### `ActionSpec`

Specifies how an item participates in the semantic layer. Actions are attached to an `AtomicContentSpec`.

```java
ActionSpec.rec(ProviderSpec.val(ItemFilterConditionSpec.sameRow()))   // REC
ActionSpec.avp("AIRLINE")                                             // AVP with literal attribute
ActionSpec.join(ProviderSpec.val(...))                                // JOIN
ActionSpec.fill("/", ProviderSpec.val(...))                           // FILL
ActionSpec.prefix(" ", ProviderSpec.val(...))                         // PREFIX
ActionSpec.suffix(" ", ProviderSpec.val(...))                         // SUFFIX
```

| Factory method | Description |
|---|---|
| `rec(ProviderSpec... providers)` | Record action; providers supply sibling items. |
| `rec(int anchorPos, ProviderSpec... providers)` | REC with schema anchor at position N. |
| `avp(ProviderSpec provider)` | Attribute-value pair via provider. |
| `avp(String literal)` | AVP with constant attribute name. |
| `join(ProviderSpec... providers)` | JOIN: merge co-anchored items. |
| `join(Set<Integer> keyPositions, ProviderSpec... providers)` | JOIN with key positions. |
| `fill(String delimiter, ProviderSpec... providers)` | Fill gap in REC sequence. |
| `prefix(String delimiter, ProviderSpec... providers)` | Prepend to anchor value. |
| `suffix(String delimiter, ProviderSpec... providers)` | Append to anchor value. |

---

### `ProviderSpec`

Specifies which items to collect when an action fires.

```java
ProviderSpec.val(ItemFilterConditionSpec.sameRow())           // one VAL in same row
ProviderSpec.val(3, ItemFilterConditionSpec.sameSubcol())     // up to 3 VAL in same subcolumn
ProviderSpec.attr(ItemFilterConditionSpec.sameCol())          // ATTR in same column
ProviderSpec.ctxVal("EUR")                                    // constant "EUR" injected as VAL
```

| Factory method | Description |
|---|---|
| `val(ItemFilterConditionSpec cond)` | VAL item, cardinality 1. |
| `val(int n, ItemFilterConditionSpec cond)` | VAL, up to n items. |
| `attr(ItemFilterConditionSpec cond)` | ATTR item, cardinality 1. |
| `aux(ItemFilterConditionSpec cond)` | AUX item, cardinality 1. |
| `any(int n, ItemFilterConditionSpec cond)` | Unrestricted type, up to n. |
| `ctxVal(String text)` | Context-derived constant value. |
| `ctxAttr(String text)` | Context-derived constant attribute. |

**Cardinality constant:** `ProviderSpec.UNBOUNDED = Integer.MAX_VALUE`

---

### `ItemFilterConditionSpec`

Spatial (and content) predicates that filter which items a provider collects.

```java
ItemFilterConditionSpec.sameRow()       // same row as anchor
ItemFilterConditionSpec.sameCol()       // same column as anchor
ItemFilterConditionSpec.sameSubtable()  // anywhere in same subtable
ItemFilterConditionSpec.sameSubcol()    // same subcolumn
ItemFilterConditionSpec.sameSubrow()    // same subrow
ItemFilterConditionSpec.sameCell()      // same cell (compound/delimited)
ItemFilterConditionSpec.below()         // row > anchor row
ItemFilterConditionSpec.above()         // row < anchor row
ItemFilterConditionSpec.rightOf()       // col > anchor col
ItemFilterConditionSpec.leftOf()        // col < anchor col
```

For custom conditions use `new ItemFilterConditionSpec.Custom("description", (anchor, candidate) -> ...)`.

---

### `Quantifier`

| Factory method | RTL equivalent | Description |
|---|---|---|
| `one()` | _(default)_ | Exactly one occurrence. |
| `zeroOrOne()` | `?` | Zero or one. |
| `oneOrMore()` | `+` | One or more. |
| `zeroOrMore()` | `*` | Zero or more. |
| `exactly(int n)` | `{n}` | Exactly n (n ≥ 2). |

---

### `ItemDerivationDirective`

| Constant | Description |
|---|---|
| `VAL` | Derive a value item from the cell. |
| `ATTR` | Derive an attribute item. |
| `AUX` | Derive an auxiliary item. |
| `SKIP` | Ignore cell; no item or action is created. |

---

## Matching

### `AtpMatcher`

Utility class (no constructor).

```java
Optional<InterpretableTable> result = AtpMatcher.match(pattern, syntax);
```

| Method | Description |
|---|---|
| `static Optional<InterpretableTable> match(TablePattern atp, TableSyntax syntax)` | Returns the enriched table on success, empty on mismatch. |
| `static Optional<InterpretableTable> match(TablePattern atp, TableSyntax syntax, Set<ContextDerivedItem> contextItems)` | Overload for externally supplied context items. |

---

## Interpretation

### `TableInterpreter`

Fluent API; all `with*` methods return `this`.

```java
Recordset rs = new TableInterpreter()
    .withStrategy(SchemaConstructionStrategy.RECORD_FIRST)
    .interpret(match.get());
```

| Method | Description |
|---|---|
| `Recordset interpret(InterpretableTable table)` | Runs all four interpretation phases and returns the recordset. |
| `withStrategy(SchemaConstructionStrategy s)` | Schema construction order (default: `RECORD_FIRST`). |
| `withActionApplicationStrategy(ActionApplicationStrategy s)` | Action application order (default: `ROW_FIRST`). |
| `withMissingValueHandler(MissingValueHandler h)` | Handling of missing attribute values (default: `NULL_HANDLER`). |
| `withTransformations(List<RecordsetTransformation> t)` | Post-processing transformations. |
| `withAnonymousAttributeTemplate(String template)` | Name template for unnamed attributes; `%i` → index. Default: `"$a_%i"`. |

---

### `SchemaConstructionStrategy`

| Constant | Description |
|---|---|
| `RECORD_FIRST` | For each anchor, collect all its positions. |
| `POSITION_FIRST` | For each position, collect all anchors. |

---

## Table syntax (`itm.syntax`)

### `TableSyntax`

```java
TableSyntax syntax = new TableSyntax(3, 2);
syntax.getCell(0, 0).setText("Name");
syntax.defineSubtables(0, 2);   // subtable 1: rows 0-1, subtable 2: row 2
```

| Method | Description |
|---|---|
| `TableSyntax(int numRows, int numCols)` | Creates a grid; all cells blank, one subtable. |
| `Cell getCell(int row, int col)` | Returns the cell at (row, col). |
| `List<Cell> allCells()` | All cells in row-major order. |
| `List<Row> rows()` | All rows in order. |
| `List<Subtable> subtables()` | Current subtable partitioning. |
| `void defineSubtables(int... boundaries)` | Redefine subtables by starting row indices. |
| `void defineSubrow(int row, int colStart, int colEnd)` | Add a subrow partition within a row. |
| `int numRows()` / `int numCols()` | Grid dimensions. |

---

### `Cell`

| Method group | Methods |
|---|---|
| Position | `pos()`, `row()`, `col()`, `bbox()`, `merged()` |
| Hierarchy | `parentRow()`, `subrow()`, `subtable()` |
| Content | `text()`, `textBlank()`, `textMultiline()`, `textIndent()`, `setText(String)` |
| Font | `fontBold()`, `fontItalic()`, `fontUnderline()`, `fontStrikeout()`, `fontFamily()` |
| Alignment | `horzAlign()`, `vertAlign()` |
| Borders | `leftBorder()`, `topBorder()`, `rightBorder()`, `bottomBorder()` |
| Colors | `bgColor()`, `fgColor()`, `rotation()` |

Formatting setters follow the pattern `setFontBold(boolean)`, `setHorzAlign(HorizontalAlignment)`, etc.

---

## Result types (`recordset`)

### `Recordset`

```java
Recordset rs = interpreter.interpret(itm);
rs.schema().attributes();        // ["Name", "Score"]
rs.records().get(0).get("Name"); // "Alice"
```

| Method | Description |
|---|---|
| `Schema schema()` | Attribute names of this recordset. |
| `List<Record> records()` | Immutable list of records. |
| `int size()` | Number of records. |
| `Record get(int index)` | Record at index. |

---

### `Record`

| Method | Description |
|---|---|
| `String get(String attribute)` | Value for attribute name (null if missing). |
| `String get(int index)` | Value at schema position. |
| `Map<String, String> values()` | Immutable attribute → value map. |
| `Schema schema()` | Schema this record belongs to. |

---

### `Schema`

| Method | Description |
|---|---|
| `List<String> attributes()` | Immutable ordered attribute list. |
| `int size()` | Number of attributes. |
| `boolean contains(String attribute)` | Attribute membership check. |
| `int indexOf(String attribute)` | Position of attribute (−1 if absent). |
