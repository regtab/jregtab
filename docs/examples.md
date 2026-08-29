# Examples

Six worked examples: five drawn from the benchmark test suite — tasks **052**, **053**, **046**,
**116**, and **051** — and one from the conformance corpus (`join_product`, the record product).
For each task the ATP pattern and its RTL equivalent are shown side by side.

---

## Example 1 — Task 052: cross-table unpivot with compound cells and an injected constant

A two-dimensional cross-tabulation is *unpivoted* into a flat recordset. Each body cell is a
compound `"ND MON"` value, and a constant `YEAR=2025` pair is injected into every record.

**Input table** (task 052, variant 1):

```
       | CA     | HU
IKT    | 0 Jan  | 8 Feb
SVO    | 31 Jan | 40 Feb
```

- Row 0: header with airline codes (`CA`, `HU`).
- Rows 1–2: airport code in column 0, then compound `"ND MON"` body cells.

**Schema:** `⟨ND, AIRLINE, AIRPORT, MON, YEAR⟩` — four records:

```
ND | AIRLINE | AIRPORT | MON | YEAR
0  | CA      | IKT     | Jan | 2025
8  | HU      | IKT     | Feb | 2025
31 | CA      | SVO     | Jan | 2025
40 | HU      | SVO     | Feb | 2025
```

### Item roles

Which item each cell yields (`YEAR` is injected as a constant, not read from a cell):

|           | col 0                | col 1                          | col 2                          |
|-----------|----------------------|--------------------------------|--------------------------------|
| **row 0** | — *(skip)*           | VAL `CA` → AIRLINE             | VAL `HU` → AIRLINE             |
| **row 1** | VAL `IKT` → AIRPORT  | VAL `0`→ND, VAL `Jan`→MON      | VAL `8`→ND, VAL `Feb`→MON      |
| **row 2** | VAL `SVO` → AIRPORT  | VAL `31`→ND, VAL `Jan`→MON     | VAL `40`→ND, VAL `Feb`→MON     |

### ATP pattern

```java
import ru.icc.regtab.atp.spec.*;

ItemFilterConditionSpec SAME_COL  = ItemFilterConditionSpec.sameCol();
ItemFilterConditionSpec SAME_ROW  = ItemFilterConditionSpec.sameRow();
ItemFilterConditionSpec SAME_CELL = ItemFilterConditionSpec.sameCell();

// Compound body cell: "0 Jan" → ND ("0") + MON ("Jan")
var dataCell = CompoundContentSpec.of(
        AtomicContentSpec.val(
                ActionSpec.rec(
                        ProviderSpec.val(1, SAME_COL),            // AIRLINE (column header)
                        ProviderSpec.val(1, SAME_ROW),            // AIRPORT (leftmost cell)
                        ProviderSpec.val(1, SAME_CELL),           // MON (same compound cell)
                        ProviderSpec.ctxAvp("YEAR", "2025")       // constant YEAR=2025
                ),
                ActionSpec.avp("ND")
        ),
        CompoundContentSpec.Segment.of(" ",
                AtomicContentSpec.val(ActionSpec.avp("MON"))
        )
);

TablePattern pattern = TablePattern.of(
        // Header subtable: skip the empty corner + one-or-more airline cells
        SubtablePattern.of(Quantifier.one(),
                RowPattern.of(
                        CellPattern.skip(),
                        CellPattern.of(Quantifier.oneOrMore(),
                                AtomicContentSpec.val(ActionSpec.avp("AIRLINE")))
                )
        ),
        // Data subtable: one-or-more rows of airport cell + one-or-more body cells
        SubtablePattern.of(Quantifier.one(),
                RowPattern.of(Quantifier.oneOrMore(),
                        CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("AIRPORT"))),
                        CellPattern.of(Quantifier.oneOrMore(), dataCell)
                )
        )
);
```

### RTL equivalent

```rtl
[ [] [VAL : 'AIRLINE'->AVP]+ ]
[ [VAL : 'AIRPORT'->AVP]
  [VAL : (COL, ROW, CL, @'YEAR'='2025')->REC, 'ND'->AVP " " VAL : 'MON'->AVP]+ ]+
```

### How it works

- **Header subtable** `[ [] [VAL : 'AIRLINE'->AVP]+ ]`: skip the empty corner cell `[]`, then derive
  one `AIRLINE` attribute-value per airline column (`CA`, `HU`).
- **Data subtable** `[ … ]+`: one-or-more rows. The first cell `[VAL : 'AIRPORT'->AVP]` yields the
  `AIRPORT` value (`IKT`, `SVO`).
- Each body cell is **compound**: `[VAL : … " " VAL : 'MON'->AVP]` splits `"0 Jan"` at the space into
  `ND` (`"0"`) and `MON` (`"Jan"`).
- The `ND` item's `REC` action uses four providers:
  - `COL` (`sameCol`) — the `AIRLINE` value in the column header.
  - `ROW` (`sameRow`) — the `AIRPORT` value in the leftmost cell of the row.
  - `CL` (`sameCell`) — the `MON` value inside the same compound cell.
  - `@'YEAR'='2025'` — a **context provider** that injects the constant pair `YEAR=2025` into every record.

### Derivation

1. **AVP** (headers): `AIRLINE=CA`, `AIRLINE=HU`; `AIRPORT=IKT`, `AIRPORT=SVO`.
2. **AVP** (compound split): each body cell yields `ND=…` and `MON=…` — e.g. `ND=0`, `MON=Jan`.
3. **Context AVP**: `YEAR=2025` (the injected constant).
4. **REC** anchored on each `ND`, pulling `AIRLINE` (COL), `AIRPORT` (ROW), `MON` (CL), `YEAR` (ctx):
   - `⟨0, CA, IKT, Jan, 2025⟩`
   - `⟨8, HU, IKT, Feb, 2025⟩`
   - `⟨31, CA, SVO, Jan, 2025⟩`
   - `⟨40, HU, SVO, Feb, 2025⟩`

### Running the test

```bash
mvn test -Dtest="AtpTask052Test"
mvn test -Dtest="RtlTask052Test"
```

---

## Example 2 — Task 053: compound attribute names and paired-row CONCAT

Two physical rows describe one logical record. Attribute names are *composed* from a group header
(`REF`, `SPECS`) and a per-row qualifier (`TP`, `HV`, …), and the paired rows are concatenated
into one record by `CONCAT` — the counterpart of `pandas.concat(axis=1)` aligned on the `ID` key.

**Input table** (task 053, variant 1):

```
      | REF | REF | SPECS | SPECS
T-1   | TP  | D16 | HV    | 750
T-1   | SN  | 001 | LV    | 110
T-2   | TP  | D24 | HV    | 110
T-2   | SN  | 002 | LV    | 10
```

- Row 0: group-name header cells (`REF`, `SPECS`) — auxiliary, used only as name prefixes.
- Data rows come in **pairs** sharing the same `ID` (`T-1`, `T-1`).

**Schema:** `⟨ID, REF_TP, SPECS_HV, REF_SN, SPECS_LV⟩` — one record per ID:

```
ID  | REF_TP | SPECS_HV | REF_SN | SPECS_LV
T-1 | D16    | 750      | 001    | 110
T-2 | D24    | 110      | 002    | 10
```

### Item roles

|           | col 0            | col 1       | col 2      | col 3       | col 4      |
|-----------|------------------|-------------|------------|-------------|------------|
| **row 0** | —                | AUX `REF`   | —          | AUX `SPECS` | —          |
| **row 1** | VAL `T-1` → ID   | ATTR `TP`   | VAL `D16`  | ATTR `HV`   | VAL `750`  |
| **row 2** | VAL `T-1` → ID   | ATTR `SN`   | VAL `001`  | ATTR `LV`   | VAL `110`  |
| **row 3** | VAL `T-2` → ID   | ATTR `TP`   | VAL `D24`  | ATTR `HV`   | VAL `110`  |
| **row 4** | VAL `T-2` → ID   | ATTR `SN`   | VAL `002`  | ATTR `LV`   | VAL `10`   |

The `AUX` group headers feed the `PREFIX` action; each `ATTR` qualifier gets the header above it
prepended to form a compound attribute name.

### ATP pattern

```java
import ru.icc.regtab.atp.spec.*;

ItemFilterConditionSpec SAME_ROW    = ItemFilterConditionSpec.sameRow();
ItemFilterConditionSpec BELOW_STR   = ItemFilterConditionSpec.and(FilterTerm.Below.INSTANCE,
                                                                  FilterTerm.SameStr.INSTANCE);
ItemFilterConditionSpec ABOVE       = ItemFilterConditionSpec.above();
ItemFilterConditionSpec SAME_SUBROW = ItemFilterConditionSpec.sameSubrow();

TablePattern pattern = TablePattern.of(
        SubtablePattern.of(Quantifier.one(),
                // Header row: skip the corner, mark group names as AUX
                RowPattern.of(
                        CellPattern.skip(),
                        CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.aux())
                ),
                // Data rows (one-or-more)
                RowPattern.of(Quantifier.oneOrMore(),
                        // Anchor subrow: the ID cell
                        SubrowPattern.of(
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_ROW)),
                                        ActionSpec.concat(0, ProviderSpec.val(1, BELOW_STR)),
                                        ActionSpec.avp("ID")
                                ))
                        ),
                        // Repeated qualifier/value subrows
                        SubrowPattern.of(Quantifier.oneOrMore(),
                                CellPattern.of(AtomicContentSpec.attr(
                                        ActionSpec.prefix("_", ProviderSpec.any(1, ABOVE))
                                )),
                                CellPattern.of(AtomicContentSpec.val(
                                        ActionSpec.avp(ProviderSpec.attr(SAME_SUBROW))
                                ))
                        )
                )
        )
);
```

### RTL equivalent

```rtl
[ [] [AUX]+ ]
[ [VAL : ROW*->REC, BW&STR->CONCAT(0), 'ID'->AVP]
  {[ATTR : AV->PREFIX('_')] [VAL : SR->AVP]}+ ]+
```

### How it works

- **Header subtable** `[ [] [AUX]+ ]`: skip the corner `[]`, then mark the group-name cells
  (`REF`, `REF`, `SPECS`, `SPECS`) as `AUX` — they are not values, they only supply name prefixes.
- **Data subtable** `[ … ]+`: one-or-more rows. The anchor cell
  `[VAL : ROW*->REC, BW&STR->CONCAT(0), 'ID'->AVP]` is the `ID` value (`T-1`):
  - `ROW*->REC` collects all `VAL` items in the same row into one record.
  - `'ID'->AVP` names the anchor's attribute `ID`.
  - `BW&STR->CONCAT(0)` (`Below` & `SameStr`) concatenates the record of the next row whose `ID`
    string is identical below to the anchor's record, so `T-1`'s two physical rows fold into a
    single wider record; key position `0` (the `ID`) is not repeated.
- The rest of each row is an **explicit subrow** `{[ATTR] [VAL]}+` repeated per qualifier/value pair:
  - `[ATTR : AV->PREFIX('_')]` — the qualifier cell (`TP`, `HV`, …) becomes an `ATTR`; `AV`
    (`Above`) prepends the group header from the cell above with `'_'`, forming `REF_TP`,
    `SPECS_HV`, ….
  - `[VAL : SR->AVP]` — the value cell (`D16`, `750`) takes its attribute name from the `ATTR`
    in the same subrow.

### Derivation

1. **PREFIX** builds the compound attribute names: `REF`+`TP`→`REF_TP`, `SPECS`+`HV`→`SPECS_HV`,
   `REF`+`SN`→`REF_SN`, `SPECS`+`LV`→`SPECS_LV`.
2. **AVP**: `ID=T-1` (rows 1–2), `ID=T-2` (rows 3–4); then `REF_TP=D16`, `SPECS_HV=750` (row 1);
   `REF_SN=001`, `SPECS_LV=110` (row 2); `REF_TP=D24`, `SPECS_HV=110` (row 3);
   `REF_SN=002`, `SPECS_LV=10` (row 4).
3. **REC** (one per row): `⟨T-1, D16, 750⟩`, `⟨T-1, 001, 110⟩`, `⟨T-2, D24, 110⟩`, `⟨T-2, 002, 10⟩`.
4. **CONCAT(0)** concatenates the record of the row below sharing the same `ID` (its position 0,
   the second `ID`, is dropped):
   - `⟨T-1, D16, 750, 001, 110⟩`
   - `⟨T-2, D24, 110, 002, 10⟩`

!!! note "CONCAT vs JOIN"
    `CONCAT` **folds** records: two records become one wider record. `JOIN` **multiplies** them:
    every record of the anchor is combined with every provided record, and the width stays fixed.
    Here each `ID` has exactly one row below, so `JOIN(0)` would give the same two records — the
    two operations coincide whenever a single record is provided. They diverge from two records
    on: with three rows per `ID`, `CONCAT(0)` yields one record of width 7, `JOIN(0)` yields two
    records of width 5 (see Example 6). A named attribute shared by the concatenated records
    (apart from the key) is a conflict: `CONCAT` leaves both records as they are and reports a
    diagnostic (`TableInterpreter.diagnostics()`); for `JOIN` the same situation is a natural-join
    condition. Up to jRegTab 0.5.x the folding operation was spelled `JOIN(K)`.

### Running the test

```bash
mvn test -Dtest="AtpTask053Test"
mvn test -Dtest="RtlTask053Test"
```

---

## Example 3 — Task 046: pivoting a flat list into a wide recordset

The inverse of Example 1: a flat *(name, subject, score)* list is *pivoted* so that each distinct
subject becomes a schema attribute and the rows of each student collapse into one record.

**Input table** (task 046, variant 1):

```
Anna | Math    | 43
Anna | French  | 78
Bob  | English | 96
Bob  | French  | 54
Joan | English | 79
Tom  | Math    | 90
Tom  | French  | 85
Rob  | English | 87
Rob  | French  | 92
```

Every row has three non-blank cells: a name, a subject, and a score. Consecutive rows with the
same name belong to the same student.

**Schema:** `⟨"", Math, French, English⟩` — the blank-named first attribute holds the student name,
the rest are derived from the subject column:

```
     | Math | French | English
Anna | 43   | 78     |
Bob  |      | 54     | 96
Joan |      |        | 79
Tom  | 90   | 85     |
Rob  |      | 92     | 87
```

### Item roles

Every row plays the same three roles (shown for the first rows):

|           | col 0             | col 1          | col 2     |
|-----------|-------------------|----------------|-----------|
| **row 1** | VAL `Anna` → `""` | ATTR `Math`    | VAL `43`  |
| **row 2** | VAL `Anna` → `""` | ATTR `French`  | VAL `78`  |
| **row 3** | VAL `Bob` → `""`  | ATTR `English` | VAL `96`  |
| …         | …                 | …              | …         |

The blank-named `""` attribute holds the student name (the anchor); each `ATTR` subject names its
adjacent score `VAL`.

### ATP pattern

```java
import ru.icc.regtab.atp.spec.*;

CellMatchCondition NOT_BLANK   = new CellMatchCondition(CellPredicate.NotBlank.INSTANCE);
ItemFilterConditionSpec SAME_SUBROW = ItemFilterConditionSpec.sameSubrow();
ItemFilterConditionSpec BELOW_STR   = ItemFilterConditionSpec.and(FilterTerm.Below.INSTANCE,
                                                                  FilterTerm.SameStr.INSTANCE);

TablePattern pattern = TablePattern.of(
        SubtablePattern.of(Quantifier.oneOrMore(),
                RowPattern.of(Quantifier.oneOrMore(),
                        // Anchor: non-blank name cell
                        CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(
                                ActionSpec.avp(""),                                       // blank-named attribute
                                ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, SAME_SUBROW)),
                                ActionSpec.concat(0, ProviderSpec.val(ProviderSpec.UNBOUNDED, BELOW_STR))
                        )),
                        // Subject cell → ATTR
                        CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.attr()),
                        // Score cell → VAL named by the same-subrow ATTR
                        CellPattern.of(NOT_BLANK, Quantifier.one(), AtomicContentSpec.val(
                                ActionSpec.avp(ProviderSpec.attr(SAME_SUBROW))
                        ))
                )
        )
);
```

### RTL equivalent

```rtl
{ [ [!BLANK? VAL : ''->AVP, SR*->REC, BW&STR*->CONCAT(0)] [!BLANK? ATTR] [!BLANK? VAL : SR->AVP] ]+ }+
```

### How it works

- The whole list is matched by one-or-more subtables `{ … }+` of one-or-more three-cell rows `[ … ]+`;
  every cell is **guarded** `!BLANK?` (must be non-blank).
- Anchor cell `[!BLANK? VAL : ''->AVP, SR*->REC, BW&STR*->CONCAT(0)]` — the name (`Anna`):
  - `''->AVP` binds the name to the **empty-named attribute** (the blank-header name column).
  - `SR*->REC` collects all same-subrow `VAL` items into the record.
  - `BW&STR*->CONCAT(0)` concatenates the records of every following row whose name string is
    identical below — `Anna`'s two rows fold into one record, `Bob`'s two, and so on. This is a
    fold, not a join: three rows per student would still give one record (see Example 6 for the
    record product).
- `[!BLANK? ATTR]` — the subject cell (`Math`) becomes an `ATTR` (a schema attribute name).
- `[!BLANK? VAL : SR->AVP]` — the score cell (`43`) takes its attribute name from the `ATTR` in the
  same subrow → `Math=43`.
- The result is one record per student with a column per distinct subject; subject/student
  combinations that never appear stay blank.

### Derivation

1. **AVP** (score ← same-subrow subject): `Math=43`, `French=78` (Anna); `English=96`, `French=54`
   (Bob); `English=79` (Joan); `Math=90`, `French=85` (Tom); `English=87`, `French=92` (Rob).
2. **REC** (one per row, anchored on the name): `⟨Anna,43⟩`, `⟨Anna,78⟩`, `⟨Bob,96⟩`, `⟨Bob,54⟩`,
   `⟨Joan,79⟩`, `⟨Tom,90⟩`, `⟨Tom,85⟩`, `⟨Rob,87⟩`, `⟨Rob,92⟩`.
3. **CONCAT(0)** concatenates the records of every row whose name repeats directly below, folding
   each student into one record: `⟨Anna, 43, 78⟩`, `⟨Bob, 96, 54⟩`, `⟨Joan, 79⟩`, `⟨Tom, 90, 85⟩`,
   `⟨Rob, 87, 92⟩`.

**Schema-flexible result.** Different students list different subjects, so the records are *ragged*:
the schema is the union `⟨"", Math, French, English⟩`, but a subject/student combination that never
occurs has **no value item at all**. For example Joan's record is just `⟨Joan, 79⟩` (English), so its
`Math` and `French` values are `⊥` (the manuscript's term for a missing value). In the API
`record.get("Math")` returns `null` for Joan, and the CSV renders it as an empty field.

### Running the test

```bash
mvn test -Dtest="AtpTask046Test"
mvn test -Dtest="RtlTask046Test"
```

---

## Example 4 — Task 116: named fragments (de-duplicating repeated sub-patterns)

When the same sub-pattern appears in multiple non-adjacent positions, **named fragment
definitions** eliminate the repetition. Fragments are declared in the RTL preamble
(before the first `[` or `{`) and referenced by `[$N]` or `{$N}` at the appropriate level.

**Task 116** — environmental monitoring table with repeating column groups.
Without fragments, `[VAL: -AV->PREFIX(', ')]` appears five times and
`[VAL: 'VALUE'->AVP, (ROW, COL&R1..3*, -AV&#'IND')->REC]` appears eight times.

```rtl
$V1=[VAL: -AV->PREFIX(', ')]
$V2=[VAL: 'VALUE'->AVP, (ROW, COL&R1..3*, -AV&#'IND')->REC]
[ []+ ]
[ [] [VAL: 'TERRITORY'->AVP]+ ]
[ [AUX]+ ]
[ 'LOCATION'->AVP [] [$V1]{4} [VAL] []
                     [VAL] [$V1] [VAL]
                     [$V1] [VAL] []
                     { [VAL] [$V1] [VAL] [] }? ]
{ [ [VAL#'IND': 'INDICATOR'->AVP ',' VAL: 'UNIT'->AVP]+ ]
  [ ['20\\d\\d' ? VAL: 'YEAR'->AVP]
    { [$V2]{5} [] }{2}
    { [$V2]{3} [] }?
  ]+
}+
```

- `$V1` = cell fragment; `[$V1]{4}` — four cells with PREFIX action, `[$V1]` — single cell.
- `$V2` = cell fragment; `[$V2]{5}` — five VALUE cells with REC, `[$V2]{3}` — three.
- Quantifiers on references are independent of the definition.

This example reproduces **task 116** — the full environmental-monitoring pattern, here written
with named fragments to avoid repeating the two recurring cell sub-patterns; the same recordset
is produced by the benchmark tests:

```bash
mvn test -Dtest="AtpTask116Test"
mvn test -Dtest="RtlTask116Test"
```

---

## Example 5 — Task 051: low-level ITM construction (without a pattern)

The ATP and RTL paths populate the semantic layer automatically by matching a pattern. For full
control — or for use cases the pattern language does not cover — an `InterpretableTable` can also
be assembled **by hand**: you create the cell-derived items, the context-derived items, and the
interpretation actions yourself, then interpret the result directly. No `AtpMatcher` is involved.

This example reproduces **task 051** — the cross-table unpivot from the
[Getting started](getting-started.md) guide — entirely by hand (schema `⟨ND, AIRLINE, AIRPORT, MON⟩`).
Each compound body cell yields **two** cell-derived items, which is the key difference from a
one-item-per-cell table:

```
       | CA     | HU
IKT    | 0 Jan  | 8 Feb
SVO    | 31 Jan | 40 Feb
```

```java
import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.interpret.TableInterpreter;
import ru.icc.regtab.itm.semantics.TableSemantics;
import ru.icc.regtab.itm.semantics.action.InterpretationAction;
import ru.icc.regtab.itm.semantics.item.*;
import ru.icc.regtab.itm.semantics.operation.*;
import ru.icc.regtab.itm.semantics.provider.*;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.recordset.Recordset;

import java.util.List;
import java.util.Set;

// 1. Build the syntactic layer (the empty corner cell defaults to "")
TableSyntax syntax = new TableSyntax(3, 3);
syntax.getCell(0, 1).setText("CA");
syntax.getCell(0, 2).setText("HU");
syntax.getCell(1, 0).setText("IKT");
syntax.getCell(1, 1).setText("0 Jan");
syntax.getCell(1, 2).setText("8 Feb");
syntax.getCell(2, 0).setText("SVO");
syntax.getCell(2, 1).setText("31 Jan");
syntax.getCell(2, 2).setText("40 Feb");

// 2a. Cell-derived items (ι).
//     Header and row-header cells yield one VALUE item each (index 0).
CellDerivedItem iotaCA  = new CellDerivedItem("CA",  0, syntax.getCell(0, 1), ItemType.VALUE);
CellDerivedItem iotaHU  = new CellDerivedItem("HU",  0, syntax.getCell(0, 2), ItemType.VALUE);
CellDerivedItem iotaIKT = new CellDerivedItem("IKT", 0, syntax.getCell(1, 0), ItemType.VALUE);
CellDerivedItem iotaSVO = new CellDerivedItem("SVO", 0, syntax.getCell(2, 0), ItemType.VALUE);
//     Each compound body cell "ND MON" yields TWO items: ND at index 0, MON at index 1.
CellDerivedItem nd11 = new CellDerivedItem("0",   0, syntax.getCell(1, 1), ItemType.VALUE);
CellDerivedItem mo11 = new CellDerivedItem("Jan", 1, syntax.getCell(1, 1), ItemType.VALUE);
CellDerivedItem nd12 = new CellDerivedItem("8",   0, syntax.getCell(1, 2), ItemType.VALUE);
CellDerivedItem mo12 = new CellDerivedItem("Feb", 1, syntax.getCell(1, 2), ItemType.VALUE);
CellDerivedItem nd21 = new CellDerivedItem("31",  0, syntax.getCell(2, 1), ItemType.VALUE);
CellDerivedItem mo21 = new CellDerivedItem("Jan", 1, syntax.getCell(2, 1), ItemType.VALUE);
CellDerivedItem nd22 = new CellDerivedItem("40",  0, syntax.getCell(2, 2), ItemType.VALUE);
CellDerivedItem mo22 = new CellDerivedItem("Feb", 1, syntax.getCell(2, 2), ItemType.VALUE);

Set<CellDerivedItem> allCdi = Set.of(
        iotaCA, iotaHU, iotaIKT, iotaSVO,
        nd11, mo11, nd12, mo12, nd21, mo21, nd22, mo22);

// 2b. Context-derived items (β): named ATTRIBUTE constants that define the schema fields
ContextDerivedItem betaND      = new ContextDerivedItem("ND",      ItemType.ATTRIBUTE);
ContextDerivedItem betaAIRLINE = new ContextDerivedItem("AIRLINE", ItemType.ATTRIBUTE);
ContextDerivedItem betaAIRPORT = new ContextDerivedItem("AIRPORT", ItemType.ATTRIBUTE);
ContextDerivedItem betaMON     = new ContextDerivedItem("MON",     ItemType.ATTRIBUTE);
Set<ContextDerivedItem> allCtx = Set.of(betaND, betaAIRLINE, betaAIRPORT, betaMON);

// 2c. Interpretation actions
// AVP: pair each VALUE item with its named ATTRIBUTE (establishes the schema field name).
// REC: anchor on each ND item; providers select the same-column airline, same-row airport,
//      and the same-cell MON sibling. The provider excludes the anchor itself, and !sameCell
//      keeps sameCol/sameRow from picking the ND item's own MON sibling.
ItemFilterCondition sameCol  = (a, c) -> c.sameCol(a) && !c.sameCell(a);
ItemFilterCondition sameRow  = (a, c) -> c.sameRow(a) && !c.sameCell(a);
ItemFilterCondition sameCell = (a, c) -> c.sameCell(a);

List<InterpretationAction> actions = List.of(
        // AVP actions: bind header / row-header items to named attributes
        new InterpretationAction(iotaCA,
                List.of(new ContextDerivedItemProvider(List.of(betaAIRLINE))), new AvpOperation()),
        new InterpretationAction(iotaHU,
                List.of(new ContextDerivedItemProvider(List.of(betaAIRLINE))), new AvpOperation()),
        new InterpretationAction(iotaIKT,
                List.of(new ContextDerivedItemProvider(List.of(betaAIRPORT))), new AvpOperation()),
        new InterpretationAction(iotaSVO,
                List.of(new ContextDerivedItemProvider(List.of(betaAIRPORT))), new AvpOperation()),
        // AVP actions: bind each ND segment to ND and each MON segment to MON
        new InterpretationAction(nd11,
                List.of(new ContextDerivedItemProvider(List.of(betaND))), new AvpOperation()),
        new InterpretationAction(nd12,
                List.of(new ContextDerivedItemProvider(List.of(betaND))), new AvpOperation()),
        new InterpretationAction(nd21,
                List.of(new ContextDerivedItemProvider(List.of(betaND))), new AvpOperation()),
        new InterpretationAction(nd22,
                List.of(new ContextDerivedItemProvider(List.of(betaND))), new AvpOperation()),
        new InterpretationAction(mo11,
                List.of(new ContextDerivedItemProvider(List.of(betaMON))), new AvpOperation()),
        new InterpretationAction(mo12,
                List.of(new ContextDerivedItemProvider(List.of(betaMON))), new AvpOperation()),
        new InterpretationAction(mo21,
                List.of(new ContextDerivedItemProvider(List.of(betaMON))), new AvpOperation()),
        new InterpretationAction(mo22,
                List.of(new ContextDerivedItemProvider(List.of(betaMON))), new AvpOperation()),
        // REC actions: anchor on each ND item → ⟨ND, AIRLINE, AIRPORT, MON⟩
        new InterpretationAction(nd11, List.of(
                new CellDerivedItemProvider(sameCol,  allCdi, 1),   // → iotaCA  (AIRLINE)
                new CellDerivedItemProvider(sameRow,  allCdi, 1),   // → iotaIKT (AIRPORT)
                new CellDerivedItemProvider(sameCell, allCdi, 1)),  // → mo11    (MON)
                new RecOperation()),
        new InterpretationAction(nd12, List.of(
                new CellDerivedItemProvider(sameCol,  allCdi, 1),   // → iotaHU
                new CellDerivedItemProvider(sameRow,  allCdi, 1),   // → iotaIKT
                new CellDerivedItemProvider(sameCell, allCdi, 1)),  // → mo12
                new RecOperation()),
        new InterpretationAction(nd21, List.of(
                new CellDerivedItemProvider(sameCol,  allCdi, 1),   // → iotaCA
                new CellDerivedItemProvider(sameRow,  allCdi, 1),   // → iotaSVO
                new CellDerivedItemProvider(sameCell, allCdi, 1)),  // → mo21
                new RecOperation()),
        new InterpretationAction(nd22, List.of(
                new CellDerivedItemProvider(sameCol,  allCdi, 1),   // → iotaHU
                new CellDerivedItemProvider(sameRow,  allCdi, 1),   // → iotaSVO
                new CellDerivedItemProvider(sameCell, allCdi, 1)),  // → mo22
                new RecOperation())
);

// 3. Build the semantic layer and interpret
TableSemantics semantics = new TableSemantics(allCdi, allCtx, actions);
InterpretableTable itm = new InterpretableTable(syntax, semantics);
Recordset result = new TableInterpreter().interpret(itm);
// schema ⟨ND, AIRLINE, AIRPORT, MON⟩; four records:
// ⟨0, CA, IKT, Jan⟩  ⟨8, HU, IKT, Feb⟩  ⟨31, CA, SVO, Jan⟩  ⟨40, HU, SVO, Feb⟩
```

This is exactly the recordset that task 051 produces from the two-line RTL pattern — here every
cell-derived item, attribute, provider, and action is spelled out by hand. The compound cells show
the general rule: a cell that yields multiple items gets one `CellDerivedItem` per item with a
distinct index. See `CrosstabMinMaxTest` for another worked example.

> This is the lowest-level entry point. In practice the [ATP](model/atp.md) and
> [RTL](rtl-reference.md) paths express the same result far more compactly — the entire block
> above collapses to the two-line RTL pattern shown in the
> [Getting started](getting-started.md) guide.

This example reproduces task 051 by hand; the same recordset is produced by the pattern-based tests:

```bash
mvn test -Dtest="AtpTask051Test"
mvn test -Dtest="RtlTask051Test"
```

---

## Example 6 — Record join (product): exploding a delimited key against stacked columns

Examples 2 and 3 *fold* records with `CONCAT`. This example *multiplies* them with `JOIN` — the
record product. A key cell lists several identifiers separated by `;`, and the numbers in the row
apply to each of them; every (identifier, column) combination must become a record of its own.

**Input table** (conformance case `join_product`):

```
id   | x | y
a;b  | 1 | 2
c    | 3 | 4
```

**Schema:** `⟨id, value, var⟩` — six records, |tokens| × |columns| per row:

```
id | var | value
a  | x   | 1
a  | y   | 2
b  | x   | 1
b  | y   | 2
c  | x   | 3
c  | y   | 4
```

### Item roles

|           | col 0                                   | col 1                | col 2                |
|-----------|-----------------------------------------|----------------------|----------------------|
| **row 0** | ATTR `id`                               | VAL `x` → var        | VAL `y` → var        |
| **row 1** | VAL `a` → id, VAL `b` → id *(one cell)* | VAL `1` → value      | VAL `2` → value      |
| **row 2** | VAL `c` → id                            | VAL `3` → value      | VAL `4` → value      |

The delimited key cell `a;b` yields **two** cell-derived items (`a` at index 0, `b` at index 1);
each is an anchor of its own.

### RTL pattern

```rtl
[ [ATTR] [VAL: 'var'->AVP]+ ]
[ [(VAL: COL->AVP, ()->REC, RT*->JOIN){';'}] [VAL: 'value'->AVP, COL->REC]+ ]+
```

### How it works

- **Header row** `[ [ATTR] [VAL: 'var'->AVP]+ ]`: `id` is the attribute of the key column; the
  column names `x`, `y` are *values* named `var` — they will travel into the records.
- **Data rows** `[ … ]+`: the key cell is **delimited** `(VAL: …){';'}` — one item per token:
  - `COL->AVP` names each token `id` (the `ATTR` in the same column);
  - `()->REC` gives each token a record of its own, `⟨id:a⟩`, `⟨id:b⟩`, `⟨id:c⟩`;
  - `RT*->JOIN` multiplies the token's record by the records of all cells to its right.
- Each number cell `[VAL: 'value'->AVP, COL->REC]` is named `value` and builds the record
  `⟨value:1, var:x⟩` with the column name above it (`COL`, cardinality 1, row-major → the header).

### Derivation

1. **AVP**: `var=x`, `var=y`; `id=a`, `id=b`, `id=c`; `value=1`, `value=2`, `value=3`, `value=4`.
2. **REC**: `rec(a) = ⟨a⟩`, `rec(b) = ⟨b⟩`, `rec(c) = ⟨c⟩`; `rec(1) = ⟨1, x⟩`, `rec(2) = ⟨2, y⟩`,
   `rec(3) = ⟨3, x⟩`, `rec(4) = ⟨4, y⟩`.
3. **JOIN** at `a` (providers: cells `1`, `2`): `rec(a) = ⟨⟨a, 1, x⟩, ⟨a, 2, y⟩⟩` — one record per
   provided record, in nested-loop order; the cells `1` and `2` become *joined-away* anchors.
4. **JOIN** at `b`: the records of `1` and `2` are still available (joined-away anchors are
   retired lazily, not removed), so `rec(b) = ⟨⟨b, 1, x⟩, ⟨b, 2, y⟩⟩`. With immediate removal,
   `b` would have found nothing to join.
5. **JOIN** at `c`: `rec(c) = ⟨⟨c, 3, x⟩, ⟨c, 4, y⟩⟩`.
6. Recordset extraction visits the live anchors `a`, `b`, `c` only — six records.

### CONCAT vs JOIN

| | `CONCAT(K)` | `JOIN(K)` |
|---|---|---|
| direction | n records → 1 | 1 record → n |
| what grows | the width of the record | the number of records |
| SQL counterpart | `GROUP BY` + collect into columns, `pandas.concat(axis=1)` | `CROSS JOIN`, `LATERAL`, `pandas.merge` |
| key `K` (positions and/or attribute names) | must agree in all records; not repeated | a record pair is combined only if it agrees there; not repeated |
| shared named attribute | a conflict: no effect + diagnostic | a natural-join condition: kept once if the values agree, pair dropped otherwise |
| one provided record | one wider record | the same record — the two coincide |
| two or more provided records | still one record | one record each — the two diverge |

### Running the test

```bash
mvn test -Dtest="RtlSemanticConformanceTest"        # conformance/semantic/join_product
mvn test -Dtest="TableInterpreterMultiRecordTest"    # the same table, both schema strategies
mvn test -Dtest="WorkingStateJoinTest"               # the operation on the working state
```

---

## Running all examples

Examples 1–5 above are benchmark tasks (052, 053, 046, 116, 051). The `*Task<NN>Test`
wildcard runs both the ATP and the RTL test for each; Example 6 is a conformance case:

```bash
# ATP + RTL tests for the five benchmark examples on this page
mvn test -Dtest="*Task052Test,*Task053Test,*Task046Test,*Task116Test,*Task051Test"
# Example 6
mvn test -Dtest="RtlSemanticConformanceTest,TableInterpreterMultiRecordTest"
```

To run the whole benchmark suite instead, use the `AtpTask*Test` / `RtlTask*Test` globs.
