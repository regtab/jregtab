# RTL Reference

RTL (Regular Table Language) is a compact textual DSL that compiles to ATP via `RtlCompiler.compile(rtl)`.
The ANTLR4 grammar is at `src/main/antlr4/ru/icc/regtab/rtl/RTL.g4`.
RTL tokens are case-insensitive.

---

## Pattern structure

```
tablePattern     : [cond ?] [<settings>] [acts] subtablePattern+

subtablePattern  : rowPattern+                          // implicit (no braces)
                 | { [cond ?] [acts] rowPattern+ } q?   // explicit

rowPattern       : [ [cond ?] [acts] subrowPattern+ ] q?

subrowPattern    : cellPattern+                         // implicit
                 | { [cond ?] [acts] cellPattern+ } q?  // explicit

cellPattern      : [ ] q?                               // skip cell
                 | [ cellPatternBody ] q?

cellPatternBody  : cond ? [acts] contSpec   // guarded: ? required when contSpec follows
                 | cond                    // condition-only: no ? (skip cell with guard)
                 | [acts] contSpec          // unguarded
```

**Inherited action specs** — `[acts]` placed at the table, subtable, row, or subrow level are
inherited by all descendant cells. Inherited actions are merged with any local actions on the
cell's `contSpec`. Incompatible inherited actions (e.g. `COL->AVP` on an `ATTR` anchor) are
silently skipped.

**Quantifiers** (suffix on any `{ }` or `[ ]` block):

| Syntax | Meaning |
|---|---|
| *(absent)* | exactly 1 |
| `?` | 0 or 1 |
| `*` | 0 or more |
| `+` | 1 or more |
| `{n}` | exactly *n* |

---

## Settings prefix

Optional prefix before the first subtable pattern: `<setting, …>`.

| Setting | Effect |
|---|---|
| `NORM` | Apply whitespace normalisation to all field values after extraction |
| `ANCH(n)` | Use position *n* in the first record as the attribute name for all records |
| `SPLIT("s")` | Split all field values by delimiter *s* after extraction |

Example: `<NORM, ANCH(2)> [ … ]` — normalise and anchor at position 2.

---

## Cell match conditions

A cell match condition guards pattern application; it tests the **cell**, not the item.

| Syntax | Condition |
|---|---|
| `"regex" ?` | Cell text matches the Java regex |
| `!"regex" ?` | Cell text does not match |
| `BLANK ?` | Cell text is blank |
| `!BLANK ?` | Cell text is not blank |
| `~"sub" ?` | Cell text contains the substring |

The `?` separator is required when a `contSpec` follows the condition (guarded form).
When the cell body contains **only** a condition and nothing else, `?` must be omitted:

| Form | Meaning |
|---|---|
| `[!BLANK ? VAL : …]` | Guarded cell — match non-blank, derive VAL |
| `[!BLANK]` | Condition-only skip cell — consume non-blank, produce no item |
| `[BLANK]` | Condition-only skip cell — consume blank, produce no item |

Examples: `[!BLANK ? VAL : (ST*)->REC]` — match only non-blank cells and build a record.
`[BLANK]*` — skip zero or more blank cells (separator columns).

---

## Content specifications

### Atomic — `contSpec`

```
itemDerivDir [tags] [= strExtr] [: actSpecs]
```

| `itemDerivDir` | Meaning |
|---|---|
| `VAL` | Value-associated item |
| `ATTR` | Attribute-associated item |
| `AUX` | Auxiliary item |
| `SKIP` or `_` | No item derived (cell is consumed but ignored) |

**Tag annotation** (user-defined tags, for use with `TAG` filter):

```
VAL #tag1 #tag2
```

**String extractor** (after `=`):

| Extractor | Effect |
|---|---|
| `NORM` | Collapse whitespace |
| `UC` | To upper case |
| `LC` | To lower case |
| `TRIM` | Trim |
| `SUBSTR(n,m)` | Substring starting at position *n*, length *m* |
| `REPL("a","b")` | Replace *a* with *b* (Java regex) |

Extractors can be chained with `.`: `=REPL(" ","_").LC`.

**Action specs** (after `:`):

```
(prov1, prov2, …)->op
```

or with a single provider:

```
prov->op
```

### Delimited

```
(VAL [tags] [= extr] [: acts]){"sep"}
```

Splits the cell text by `"sep"` and derives one item per token.

Example: `(VAL){","}` — split on comma.

### Compound

```
["open"] VAL [acts] "sep" VAL [acts] ["close"]
```

Matches a cell whose text is a sequence of segments separated by fixed delimiters.
Opening and closing delimiters are optional.

Example: `VAL " " VAL` — cell text is two tokens separated by a space (like `"0 Jan"`).

### Conditional

```
(cond ? trueSpec | falseSpec)
```

Branches on a cell match condition; both branches must be `atomContSpec`, `delimContSpec`, or `compContSpec`.

Example: `(BLANK ? SKIP | VAL)` — skip blank cells, derive a value from non-blank ones.

---

## Action specifications

```
provSpecs -> op
```

`provSpecs` is a single provider spec, a parenthesised comma-separated list, or empty parentheses
`()` (no additional providers — anchor only).

| Operation | Syntax | Effect |
|---|---|---|
| `REC` | `prov->REC` | Anchor item → record entry; provider supplies additional field values |
| `REC` | `()->REC` | Anchor item → single-field record (no additional providers; useful after `SUFFIX`/`PREFIX`/`FILL` has enriched the anchor value) |
| `REC(n)` | `prov->REC(n)` | Same + use attribute at position *n* as the record's attribute name |
| `REC('s')` | `prov->REC('s')` | Same + split field values by delimiter *s* |
| `AVP` | `prov->AVP` | Associate anchor (VAL) with an attribute from the provider (ATTR) |
| `JOIN` | `prov->JOIN` | Join item-based records: all items included, then dedup by named attribute (K=∅) |
| `JOIN(K)` | `prov->JOIN(0)` | Join with key positions K dropped from each joined record before dedup; `JOIN(0)` = old CONCAT |
| `FILL('s')` | `prov->FILL('/')` | Fill anchor value forward from provider, separated by *s* |
| `PREFIX('s')` | `prov->PREFIX(' ')` | Prepend provider value to anchor, separated by *s* |
| `SUFFIX('s')` | `prov->SUFFIX(' ')` | Append provider value to anchor, separated by *s* |

---

## Provider specifications

### Cell-derived provider (tblProvSpec)

```
[traversal] (spatConstr | (constraints)) [cardinality]
```

**Traversal order** (prefix, default = ROW_MAJOR):

| Symbol | Order |
|---|---|
| *(absent)* | ROW_MAJOR |
| `-` | REVERSE_ROW_MAJOR |
| `^` | COLUMN_MAJOR |
| `-^` | REVERSE_COLUMN_MAJOR |

**Spatial constraints** (single bare form or inside parentheses):

| Token | Condition |
|---|---|
| `ST` | `sameSubtable(a) && !sameCell(a)` |
| `SR` | `sameSubrow(a) && !sameCell(a)` |
| `SC` | `sameSubcol(a) && !sameCell(a)` |
| `CL` | `sameCell(a)` |
| `NCL` | `!sameCell(a)` |
| `ROW` | `sameRow(a) && !sameCell(a)` |
| `COL` | `sameCol(a) && !sameCell(a)` |
| `RT` | `sameSubrow(a) && col > col(a)` |
| `LT` | `sameSubrow(a) && col < col(a)` |
| `BW` | `sameSubcol(a) && row > row(a)` |
| `AV` | `sameSubcol(a) && row < row(a)` |

**Positional constraints** (spatial, absolute or relative):

| Token | Condition |
|---|---|
| `Cn` | `col == n` |
| `C+n` / `C-n` | `col == col(a) + n` |
| `Ca..b` | `a ≤ col ≤ b` (absolute) |
| `C+a..b` | `col(a)+a ≤ col ≤ col(a)+b` (relative) |
| `Rn` | `row == n` |
| `R+n` / `R-n` | `row == row(a) + n` |
| `Pn` | `index == n` |
| `P+n` / `P-n` | `index == index(a) + n` |
| `Pa..b` | `a ≤ index ≤ b` |

**Content constraints** (used inside parentheses, combined with `&` / `|`):

| Token | Condition |
|---|---|
| `"regex"` | `str.matches(regex)` |
| `!"regex"` | `!str.matches(regex)` |
| `~"sub"` | `str.contains(sub)` |
| `!~"sub"` | `!str.contains(sub)` |
| `BLANK` | `blankStr()` |
| `!BLANK` | `!blankStr()` |
| `TAG #t1 #t2` | any of the given tags matches (OR) |
| `!TAG #t1 #t2` | none of the given tags matches |
| `STR` | `sameStr(a)` (same string as the anchor) |

**Compound constraints** with `&` and `|`:

```
(ST & !BLANK)          — same subtable AND not blank
(ROW | COL)            — same row OR same column
```

**Cardinality** (suffix):

| Token | Meaning |
|---|---|
| *(absent)* | at most 1 (default) |
| `{n}` | at most *n* |
| `*` | unbounded |

Examples:
- `ST*` — all items in the same subtable
- `SC{2}` — at most 2 items in the same subcolumn
- `^COL` — items in the same column, column-major traversal

### Context-derived provider (literal)

A quoted string literal supplies a fixed string as an attribute or value:

```
('AIRLINE')->AVP
```

The item type is inferred from the action: `->AVP` → ATTR, `->REC` → VAL.

---

## Frequently used combinations

| RTL | Meaning |
|---|---|
| `ST*->REC` | Collect all same-subtable values into one record |
| `(SC{2}, SR)->REC(2)` | Two items from same subcolumn (headers) + one from same subrow |
| `^COL->AVP` | Associate with an attribute from the same column (column-major) |
| `('LABEL')->AVP` | Associate with a fixed string attribute |
| `(ST*)->REC` (in parentheses) | Same as `ST*->REC` but explicit grouping |
| `CL->JOIN(0)` | Join (drop anchor) another item from the same cell |
| `(COL)->FILL('/')` | Fill forward from same-column values, delimiter `/` |
