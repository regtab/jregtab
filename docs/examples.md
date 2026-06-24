# Examples

Three worked examples drawn from the benchmark test suite — tasks **052**, **053**, and **046**.
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

```
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

### Running the test

```bash
mvn test -Dtest="AtpTask052Test"
mvn test -Dtest="RtlTask052Test"
```

---

## Example 2 — Task 053: compound attribute names and paired-row JOIN

Two physical rows describe one logical record. Attribute names are *composed* from a group header
(`REF`, `SPECS`) and a per-row qualifier (`TP`, `HV`, …), and the paired rows are merged by JOIN.

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
                                        ActionSpec.join(0, ProviderSpec.val(1, BELOW_STR)),
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

```
[ [] [AUX]+ ]
[ [VAL : ROW*->REC, BW&STR->JOIN(0), 'ID'->AVP]
  {[ATTR : AV->PREFIX('_')] [VAL : SR->AVP]}+ ]+
```

### How it works

- **Header subtable** `[ [] [AUX]+ ]`: skip the corner `[]`, then mark the group-name cells
  (`REF`, `REF`, `SPECS`, `SPECS`) as `AUX` — they are not values, they only supply name prefixes.
- **Data subtable** `[ … ]+`: one-or-more rows. The anchor cell
  `[VAL : ROW*->REC, BW&STR->JOIN(0), 'ID'->AVP]` is the `ID` value (`T-1`):
  - `ROW*->REC` collects all `VAL` items in the same row into one record.
  - `'ID'->AVP` names the anchor's attribute `ID`.
  - `BW&STR->JOIN(0)` (`Below` & `SameStr`) merges the next row whose `ID` string is identical below,
    so `T-1`'s two physical rows fold into a single record.
- The rest of each row is an **explicit subrow** `{[ATTR] [VAL]}+` repeated per qualifier/value pair:
  - `[ATTR : AV->PREFIX('_')]` — the qualifier cell (`TP`, `HV`, …) becomes an `ATTR`; `AV`
    (`Above`) prepends the group header from the cell above with `'_'`, forming `REF_TP`,
    `SPECS_HV`, ….
  - `[VAL : SR->AVP]` — the value cell (`D16`, `750`) takes its attribute name from the `ATTR`
    in the same subrow.

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
                                ActionSpec.join(0, ProviderSpec.val(ProviderSpec.UNBOUNDED, BELOW_STR))
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

```
{ [ [!BLANK? VAL : ''->AVP, SR*->REC, BW&STR*->JOIN(0)] [!BLANK? ATTR] [!BLANK? VAL : SR->AVP] ]+ }+
```

### How it works

- The whole list is matched by one-or-more subtables `{ … }+` of one-or-more three-cell rows `[ … ]+`;
  every cell is **guarded** `!BLANK?` (must be non-blank).
- Anchor cell `[!BLANK? VAL : ''->AVP, SR*->REC, BW&STR*->JOIN(0)]` — the name (`Anna`):
  - `''->AVP` binds the name to the **empty-named attribute** (the blank-header name column).
  - `SR*->REC` collects all same-subrow `VAL` items into the record.
  - `BW&STR*->JOIN(0)` merges every following row whose name string is identical below — `Anna`'s two
    rows collapse into one record, `Bob`'s two, and so on.
- `[!BLANK? ATTR]` — the subject cell (`Math`) becomes an `ATTR` (a schema attribute name).
- `[!BLANK? VAL : SR->AVP]` — the score cell (`43`) takes its attribute name from the `ATTR` in the
  same subrow → `Math=43`.
- The result is one record per student with a column per distinct subject; subject/student
  combinations that never appear stay blank.

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

```
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

See `RtlFragmentTest` for unit tests covering all four fragment levels (cell, row, subrow, subtable).

### Running the test

```bash
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

### Running the test

This example reproduces task 051 by hand; the same recordset is produced by the pattern-based tests:

```bash
mvn test -Dtest="AtpTask051Test"
mvn test -Dtest="RtlTask051Test"
```

---

## Running all examples

All five examples above are benchmark tasks (052, 053, 046, 116, 051). The `*Task<NN>Test`
wildcard runs both the ATP and the RTL test for each:

```bash
# ATP + RTL tests for the five examples on this page
mvn test -Dtest="*Task052Test,*Task053Test,*Task046Test,*Task116Test,*Task051Test"
```

To run the whole benchmark suite instead, use the `AtpTask*Test` / `RtlTask*Test` globs.
