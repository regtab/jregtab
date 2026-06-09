<p align="center">
  <img src="assets/icon.svg" alt="jRegTab" width="100"/>
</p>

# jRegTab

**jRegTab** is an open-source Java library implementing **RegTab** — a method for pattern-driven data extraction from editable document tables with regular structure.

RegTab is described in the paper:

> Igor V. Bychkov, Alexey E. Hmelnov, and Alexey O. Shigarov.
> *RegTab: Pattern-Driven Data Extraction from Document Tables with Regular Structure.*
> Submitted to IEEE Transactions on Knowledge and Data Engineering.

---

## Overview

Tabular data in spreadsheets, text documents, and web pages are among the most common sources for data analysis. Extracting structured records from such tables is a critical but labour-intensive step in data wrangling. Source tables are typically designed for human readability and lack explicit semantics: cell meaning may be independent of position, cells may be compound, headers may be hierarchical, and relevant context may appear outside the table itself.

RegTab addresses this by matching editable document tables against *patterns* that capture their regular structure and interpretive logic. A successful match enriches the table with semantic information and yields a structured recordset.

The method is built around two formal models:

- **Interpretable Table Model (ITM)** — represents the syntactic and semantic structure of a table. The syntactic layer describes cells (their positions, formatting, and text content) together with a row-oriented substructure hierarchy: *subtables → rows → subrows → cells*. The semantic layer consists of *items* (value-associated, attribute-associated, and auxiliary) derived from cell content or supplied from external context, along with *interpretation actions* that establish how items form attribute–value pairs and record item sequences.

- **Abstract Table Pattern (ATP)** — specifies a class of tables and the rules for deriving structured records from them. An ATP instance mirrors the ITM hierarchy and contains *cell patterns* with *cell match conditions*, *content specifications*, and *interpretation action specifications*. Matching an ATP against an ITM instance populates the semantic layer automatically.

Table interpretation then proceeds in four phases: working-state initialisation, working-state completion (applying interpretation actions), recordset extraction, and optional post-processing.

---

## Documentation

- [Architecture](docs/architecture.md) — package map, data flow, interpretation phases, RTL compilation pipeline
- [Formal model](docs/formal-model.md) — ITM and ATP formal definitions mapped to Java classes
- [RTL reference](docs/rtl-reference.md) — complete RTL syntax reference with tables and examples
- [Examples](docs/examples.md) — worked examples with ATP and RTL patterns side by side

---

## Requirements

- Java 25 or later
- Maven 3.8+

---

## Build

```bash
mvn compile
```

To compile and run the full test suite (150 tasks, 1300 variants (550 ATP + 750 RTL)):

```bash
mvn test
```

---

## Usage

The three sections below use a common running example — a simplified cross-tabulation with schema `⟨ND, AIRLINE, AIRPORT⟩`:

```
       | CA | HU
IKT    |  5 |  3
SVO    | 31 | 40
```

This is a stripped-down version of the illustrative example from Section VI of the paper (see [Illustrative example](#illustrative-example) below). The full paper example adds a `MON` field extracted from compound cells like `"0 Jan"`, which requires `CompoundContentSpec` and a third `sameCell()` provider in the `rec` action. The simplified version isolates the core pattern structure without the compound-cell machinery.

### Low-level ITM construction

An `InterpretableTable` can be constructed directly by assembling the syntactic and semantic layers programmatically. This approach gives full control over items and interpretation actions, and is suited to use cases where the ATP pattern language does not suffice. For most cases, the ATP and RTL paths are simpler.

The example below builds this cross-tabulation from scratch:

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

// 1. Build the syntactic layer
TableSyntax syntax = new TableSyntax(3, 3);
syntax.getCell(0, 0).setText("");
syntax.getCell(0, 1).setText("CA");
syntax.getCell(0, 2).setText("HU");
syntax.getCell(1, 0).setText("IKT");
syntax.getCell(1, 1).setText("5");
syntax.getCell(1, 2).setText("3");
syntax.getCell(2, 0).setText("SVO");
syntax.getCell(2, 1).setText("31");
syntax.getCell(2, 2).setText("40");

// 2a. Cell-derived items (ι): one VALUE item per cell
//     Index 0 = first (and only) item derived from that cell.
CellDerivedItem iotaCA  = new CellDerivedItem("CA",  0, syntax.getCell(0, 1), ItemType.VALUE);
CellDerivedItem iotaHU  = new CellDerivedItem("HU",  0, syntax.getCell(0, 2), ItemType.VALUE);
CellDerivedItem iotaIKT = new CellDerivedItem("IKT", 0, syntax.getCell(1, 0), ItemType.VALUE);
CellDerivedItem iotaSVO = new CellDerivedItem("SVO", 0, syntax.getCell(2, 0), ItemType.VALUE);
CellDerivedItem iota11  = new CellDerivedItem("5",   0, syntax.getCell(1, 1), ItemType.VALUE);
CellDerivedItem iota12  = new CellDerivedItem("3",   0, syntax.getCell(1, 2), ItemType.VALUE);
CellDerivedItem iota21  = new CellDerivedItem("31",  0, syntax.getCell(2, 1), ItemType.VALUE);
CellDerivedItem iota22  = new CellDerivedItem("40",  0, syntax.getCell(2, 2), ItemType.VALUE);

Set<CellDerivedItem> allCdi = Set.of(
        iotaCA, iotaHU, iotaIKT, iotaSVO, iota11, iota12, iota21, iota22);

// 2b. Context-derived items (β): named ATTRIBUTE constants that define the schema fields
ContextDerivedItem betaND      = new ContextDerivedItem("ND",      ItemType.ATTRIBUTE);
ContextDerivedItem betaAIRLINE = new ContextDerivedItem("AIRLINE", ItemType.ATTRIBUTE);
ContextDerivedItem betaAIRPORT = new ContextDerivedItem("AIRPORT", ItemType.ATTRIBUTE);
Set<ContextDerivedItem> allCtx = Set.of(betaND, betaAIRLINE, betaAIRPORT);

// 2c. Interpretation actions
// AVP: pair each VALUE item with its named ATTRIBUTE (establishes the schema field name).
// REC: anchor on each body cell; providers select the same-column airline and same-row airport.
ItemFilterCondition sameCol = (a, c) -> c.sameCol(a) && !c.sameCell(a);
ItemFilterCondition sameRow = (a, c) -> c.sameRow(a) && !c.sameCell(a);

List<InterpretationAction> actions = List.of(
        // AVP actions: bind header items to named attributes
        new InterpretationAction(iotaCA,
                List.of(new ContextDerivedItemProvider(List.of(betaAIRLINE))), new AvpOperation()),
        new InterpretationAction(iotaHU,
                List.of(new ContextDerivedItemProvider(List.of(betaAIRLINE))), new AvpOperation()),
        new InterpretationAction(iotaIKT,
                List.of(new ContextDerivedItemProvider(List.of(betaAIRPORT))), new AvpOperation()),
        new InterpretationAction(iotaSVO,
                List.of(new ContextDerivedItemProvider(List.of(betaAIRPORT))), new AvpOperation()),
        new InterpretationAction(iota11,
                List.of(new ContextDerivedItemProvider(List.of(betaND))), new AvpOperation()),
        new InterpretationAction(iota12,
                List.of(new ContextDerivedItemProvider(List.of(betaND))), new AvpOperation()),
        new InterpretationAction(iota21,
                List.of(new ContextDerivedItemProvider(List.of(betaND))), new AvpOperation()),
        new InterpretationAction(iota22,
                List.of(new ContextDerivedItemProvider(List.of(betaND))), new AvpOperation()),
        // REC actions: form one record per body cell
        new InterpretationAction(iota11, List.of(
                new CellDerivedItemProvider(sameCol, allCdi, 1),   // → iotaCA
                new CellDerivedItemProvider(sameRow, allCdi, 1)),  // → iotaIKT
                new RecOperation()),
        new InterpretationAction(iota12, List.of(
                new CellDerivedItemProvider(sameCol, allCdi, 1),   // → iotaHU
                new CellDerivedItemProvider(sameRow, allCdi, 1)),  // → iotaIKT
                new RecOperation()),
        new InterpretationAction(iota21, List.of(
                new CellDerivedItemProvider(sameCol, allCdi, 1),   // → iotaCA
                new CellDerivedItemProvider(sameRow, allCdi, 1)),  // → iotaSVO
                new RecOperation()),
        new InterpretationAction(iota22, List.of(
                new CellDerivedItemProvider(sameCol, allCdi, 1),   // → iotaHU
                new CellDerivedItemProvider(sameRow, allCdi, 1)),  // → iotaSVO
                new RecOperation())
);

// 3. Build the semantic layer and interpret
TableSemantics semantics = new TableSemantics(allCdi, allCtx, actions);
InterpretableTable itm = new InterpretableTable(syntax, semantics);
Recordset result = new TableInterpreter().interpret(itm);
// schema ⟨ND, AIRLINE, AIRPORT⟩; four records:
// ⟨5, CA, IKT⟩  ⟨3, HU, IKT⟩  ⟨31, CA, SVO⟩  ⟨40, HU, SVO⟩
```

For cells that yield multiple items (e.g. `"0 Jan"` → `"0"` and `"Jan"`), create one `CellDerivedItem` per item with distinct index values. See `CrosstabMinMaxTest` for a worked example.

### Using ATP patterns

The `ru.icc.regtab.atp.spec` package provides the formal ATP types. A `TablePattern` is assembled hierarchically from `SubtablePattern`, `RowPattern`, `SubrowPattern`, and `CellPattern` instances. Each `CellPattern` carries a `ContentSpec` that says how items are derived from the matched cell and which interpretation actions to apply. `AtpMatcher.match()` then performs structural matching against a `TableSyntax`, populates the semantic layer automatically, and returns an `InterpretableTable` ready for interpretation.

**Example** — the same cross-tabulation expressed as an ATP pattern:

```java
import ru.icc.regtab.atp.AtpMatcher;
import ru.icc.regtab.atp.spec.*;
import ru.icc.regtab.interpret.TableInterpreter;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.recordset.Recordset;

TableSyntax syntax = new TableSyntax(3, 3);
syntax.getCell(0, 0).setText("");   syntax.getCell(0, 1).setText("CA");
syntax.getCell(0, 2).setText("HU");
syntax.getCell(1, 0).setText("IKT"); syntax.getCell(1, 1).setText("5");
syntax.getCell(1, 2).setText("3");
syntax.getCell(2, 0).setText("SVO"); syntax.getCell(2, 1).setText("31");
syntax.getCell(2, 2).setText("40");

TablePattern pattern = TablePattern.of(
        SubtablePattern.of(
                // header row: skip first cell, then one-or-more airline-code cells
                RowPattern.of(
                        CellPattern.skip(),
                        CellPattern.of(Quantifier.oneOrMore(),
                                AtomicContentSpec.val(ActionSpec.avp("AIRLINE")))
                ),
                // data rows: airport cell + one-or-more ND cells
                RowPattern.of(Quantifier.oneOrMore(),
                        CellPattern.of(AtomicContentSpec.val(ActionSpec.avp("AIRPORT"))),
                        CellPattern.of(Quantifier.oneOrMore(),
                                AtomicContentSpec.val(
                                        ActionSpec.avp("ND"),
                                        ActionSpec.rec(1,
                                                ItemFilterConditionSpec.sameCol(), // airline, same column
                                                ItemFilterConditionSpec.sameRow()  // airport, same row
                                        )
                                )
                        )
                )
        )
);

Recordset result = AtpMatcher.match(pattern, syntax)
        .map(itm -> new TableInterpreter().interpret(itm))
        .orElseThrow(() -> new IllegalStateException("Pattern did not match"));
// schema ⟨ND, AIRLINE, AIRPORT⟩; four records:
// ⟨5, CA, IKT⟩  ⟨3, HU, IKT⟩  ⟨31, CA, SVO⟩  ⟨40, HU, SVO⟩
```

Key building blocks:

| Type | Role |
|------|------|
| `TablePattern` / `SubtablePattern` / `RowPattern` / `CellPattern` | Structural hierarchy mirroring the ITM |
| `Quantifier` | How many times a pattern element repeats (`one()`, `oneOrMore()`, `zeroOrMore()`, `exactly(n)`) |
| `AtomicContentSpec` | How one item is derived from a cell (`val`, `attr`, `aux`, `skip`) |
| `ActionSpec` | Interpretation action: `avp("NAME")` names a field, `rec(k, …)` creates a record |
| `ItemFilterConditionSpec` | Predicate selecting provider items: `sameCol()`, `sameRow()`, `sameSubtable()`, … |
| `ProviderSpec` | Bundles a filter condition with cardinality and traversal order |
| `AtpMatcher.match()` | Structural matching + automatic semantic-layer construction |

### Using RTL patterns

RTL (Regular Table Language) is a compact textual DSL that compiles to ATP.
Use `RtlCompiler.compile(rtl)` to obtain a `TablePattern`, then proceed identically to the ATP path.

**Example** — the same cross-tabulation expressed as an RTL string:

```java
import ru.icc.regtab.atp.AtpMatcher;
import ru.icc.regtab.atp.spec.TablePattern;
import ru.icc.regtab.interpret.TableInterpreter;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.recordset.Recordset;
import ru.icc.regtab.rtl.RtlCompiler;

TableSyntax syntax = new TableSyntax(3, 3);
syntax.getCell(0, 0).setText("");   syntax.getCell(0, 1).setText("CA");
syntax.getCell(0, 2).setText("HU");
syntax.getCell(1, 0).setText("IKT"); syntax.getCell(1, 1).setText("5");
syntax.getCell(1, 2).setText("3");
syntax.getCell(2, 0).setText("SVO"); syntax.getCell(2, 1).setText("31");
syntax.getCell(2, 2).setText("40");

TablePattern pattern = RtlCompiler.compile("""
        [ [] [VAL: 'AIRLINE'->AVP]+ ]
        [ [VAL: 'AIRPORT'->AVP] [VAL: 'ND'->AVP, (COL,ROW)->REC]+ ]+
        """);

Recordset result = AtpMatcher.match(pattern, syntax)
        .map(itm -> new TableInterpreter().interpret(itm))
        .orElseThrow(() -> new IllegalStateException("Pattern did not match"));
// schema ⟨ND, AIRLINE, AIRPORT⟩; four records:
// ⟨5, CA, IKT⟩  ⟨3, HU, IKT⟩  ⟨31, CA, SVO⟩  ⟨40, HU, SVO⟩
```

The RTL string is a compact encoding of the ATP pattern shown in the previous section:

| RTL token | ATP equivalent |
|-----------|---------------|
| `[]` | `CellPattern.skip()` |
| `[VAL: 'AIRLINE'->AVP]+` | `CellPattern.of(Quantifier.oneOrMore(), AtomicContentSpec.val(ActionSpec.avp("AIRLINE")))` |
| `(COL,ROW)->REC` | `ActionSpec.rec(1, ItemFilterConditionSpec.sameCol(), ItemFilterConditionSpec.sameRow())` |
| `[ ... ]+` | `RowPattern.of(Quantifier.oneOrMore(), ...)` |

### Illustrative example

`AtpIllustrativeExampleTest` and `RtlIllustrativeExampleTest` implement the worked example from Section VI of the paper — a table class listing the numbers of airline departures from airports by month. The target schema is `⟨ND, AIRLINE, AIRPORT, MON⟩`. The ATP and RTL tests are exact counterparts: same tables, same assertions, different pattern representation.

Each test covers three cases:

- `paperExample_3x3_table_t0` — matches the 3 × 3 table from Figure 7 and verifies all four extracted records
- `extendedTable_4airlines_3airports` — matches a 4 × 5 table (4 airlines, 3 airports) and verifies 12 records
- `malformedTable_bodyCell_missingDelimiter_fails` — verifies that a table with malformed body cells does not match

To run both:

```bash
mvn test -Dtest="AtpIllustrativeExampleTest,RtlIllustrativeExampleTest"
```

---

## Evaluation

RegTab has been evaluated on four task collections.

**Foofah benchmark (tasks 001–050)** — a well-established collection of 50 tabular data transformation tasks assembled by Jin et al. (2017) from real-world forums and related work (37 real-world cases, 13 synthetic). Each task provides five source tables from the same class and five corresponding target recordsets.

The benchmark data (input and expected CSV files) is available at:
<https://github.com/umich-dbgroup/foofah>

**RegTab benchmark (tasks 051–110)** — an original collection of 60 tasks designed to cover advanced RegTab features not present in the Foofah benchmark: multi-level headers, cross-tabulations, conditional and delimited content, grouped and tagged rows, and compound provider specifications.

All 110 tasks (001–110) are solved by ATP-based patterns and verified by a JUnit 5 test suite (see [Testing](#testing) below). Automated comparison with ground-truth confirms that all **1100 test variants (550 ATP + 550 RTL)** are transformed correctly (100 % accuracy).

**Baikal collection (tasks 111–150)** — 40 tasks based on real tourism and environmental monitoring tables from the Lake Baikal region. RTL patterns only.

Tasks 111–150 add **200 further RTL test variants**, bringing the total to **1300 variants (550 ATP + 750 RTL)** across all 150 tasks.

---

## Testing

The test suite lives under `src/test/java/ru/icc/regtab/` and is split into two complementary parts.

### ATP benchmark tests

The primary benchmark tests are in the `atp` package. Each class `AtpTask{NN}Test` implements one task (Foofah benchmark: 001–050, RegTab benchmark: 051–110) as an ATP pattern using the formal `ru.icc.regtab.atp.spec` API:

```
src/test/java/ru/icc/regtab/atp/
    AtpTaskBase.java          # parameterised base: loads CSV, runs matcher, asserts output
    AtpTask001Test.java       # Foofah benchmark tasks 001–050
    AtpTask002Test.java
    ...
    AtpTask050Test.java
    AtpTask051Test.java       # RegTab benchmark tasks 051–110
    ...
    AtpTask110Test.java
```

Each test class overrides two methods:

- `taskId()` — returns the three-digit task number (e.g. `"001"`)
- `buildPattern()` — constructs and returns the `TablePattern` for that task

`AtpTaskBase` runs five JUnit parameterized test variants (`@ValueSource(ints = {1,2,3,4,5})`), one per source table. For each variant it:

1. Loads `src/test/resources/tasks/task_{NN}/input_{V}.csv` into a `TableSyntax`
2. Calls `AtpMatcher.match(pattern, syntax)` to populate the semantic layer
3. Interprets the enriched `InterpretableTable` with `TableInterpreter`
4. Applies optional post-processing (e.g. `WhitespaceNormalization`)
5. Asserts the result against `src/test/resources/tasks/task_{NN}/expected_{V}.csv`

All 110 tasks have dedicated `AtpTask{NN}Test` classes.

**Example — Task 001** (subtables with a `rec` action using the `sameSubtable` predicate):

```java
import ru.icc.regtab.atp.spec.*;

@Override
protected TablePattern buildPattern() {
    var sameSubtable = ItemFilterConditionSpec.sameSubtable();
    return TablePattern.of(
        SubtablePattern.of(Quantifier.oneOrMore(),
            RowPattern.of(
                CellPattern.of(AtomicContentSpec.val(
                    ActionSpec.rec(ProviderSpec.val(ProviderSpec.UNBOUNDED, sameSubtable))
                )),
                CellPattern.of(Quantifier.exactly(2), AtomicContentSpec.val()),
                CellPattern.skip(Quantifier.oneOrMore())
            ),
            RowPattern.of(
                CellPattern.skip(),
                CellPattern.of(Quantifier.exactly(4), AtomicContentSpec.val()),
                CellPattern.skip(Quantifier.oneOrMore())
            )
        )
    );
}
```

### RTL benchmark tests

The `rtl` package mirrors the ATP benchmark: each `RtlTask{NN}Test` implements the same task as a compact RTL string. These tests verify that the RTL compiler produces an ATP pattern equivalent to the hand-crafted ATP counterpart.

```
src/test/java/ru/icc/regtab/rtl/
    RtlTaskBase.java          # loads CSV, compiles RTL → ATP, runs matcher, asserts output
    RtlTask001Test.java       # Foofah benchmark tasks 001–050
    RtlTask002Test.java
    ...
    RtlTask050Test.java
    RtlTask051Test.java       # RegTab benchmark tasks 051–110
    ...
    RtlTask110Test.java
    RtlTask111Test.java       # Baikal collection I tasks 111–132
    ...
    RtlTask132Test.java
    RtlTask133Test.java       # Baikal collection II tasks 133–150
    ...
    RtlTask150Test.java
```

Each test class overrides two methods:

- `taskId()` — returns the three-digit task number (e.g. `"001"`)
- `buildRtl()` — returns the RTL string for that task

**Example — Task 01:**

```java
@Override
protected String buildRtl() {
    return """
            { [ [VAL : ST*->REC] [VAL]{2} []+ ]
              [ []               [VAL]{4} []+ ] }+
            """;
}
```

### Fixture data

Source and expected tables are stored as CSV files:

```
src/test/resources/tasks/
    task_001/
        input_1.csv  …  input_5.csv
        expected_1.csv  …  expected_5.csv
    task_002/
        ...
    ...
    task_050/
        ...
    task_051/
        ...
    ...
    task_110/
        ...
    task_111/
        ...
    ...
    task_150/
        ...
```

### Running the tests

Run the entire test suite with Maven:

```bash
mvn test
```

To run only the ATP benchmark tests:

```bash
mvn test -Dtest="AtpTask*Test"
```

To run only the RTL benchmark tests:

```bash
mvn test -Dtest="RtlTask*Test"
```

To run a single task:

```bash
mvn test -Dtest="AtpTask001Test"
```

---

## Related work

jRegTab builds on and supersedes **TabbyXL** (<https://github.com/tabbydoc/tabbyxl>), an earlier platform for tabular-data understanding based on the CRL domain-specific language.

---

## Citation

If you use jRegTab in your research, please cite:

```
Igor V. Bychkov, Alexey E. Hmelnov, and Alexey O. Shigarov.
RegTab: Pattern-Driven Data Extraction from Document Tables with Regular Structure.
Submitted to IEEE Transactions on Knowledge and Data Engineering, 2025.
```

---

## License

This project is distributed under the terms of the MIT License. See [LICENSE](LICENSE) for details.
