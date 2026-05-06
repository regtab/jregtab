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

## Architecture

The library is organised around the following components:

| Component | Package | Description |
|---|---|---|
| **ITM API** | `ru.icc.regtab.itm.model` | Classes for the syntactic layer (`TableSyntax`, `Cell`, `Row`, `Subrow`, `Subtable`) and the semantic layer (`TableSemantics`, `CellDerivedItem`, `ContextDerivedItem`, interpretation actions and providers) |
| **Table Interpreter** | `ru.icc.regtab.itm.interpret` | `TableInterpreter` derives a `Recordset` from an `InterpretableTable`; supports configurable `SchemaConstructionStrategy` and post-processing steps (`WhitespaceNormalization`, `FieldSplitting`, `SchemaReordering`) |
| **ATP Spec** | `ru.icc.regtab.itm.atp.spec` | Formal ATP types: `TablePattern`, `SubtablePattern`, `RowPattern`, `SubrowPattern`, `CellPattern`, content specifications (`AtomicContentSpec`, `DelimitedContentSpec`, `CompoundContentSpec`, `ConditionalContentSpec`), item provider specifications, and interpretation action specifications |
| **Pattern Matcher** | `ru.icc.regtab.itm.atp.match` | Matches an ATP instance against an ITM instance; on success populates the semantic layer |
| **Recordset** | `ru.icc.regtab.itm.recordset` | `Recordset`, `Record`, `Schema` |

---

## Requirements

- Java 25 or later
- Maven 3.8+

---

## Build

```bash
mvn compile
```

To compile and run the full test suite (all 50 Foofah benchmark tasks, 250 variants):

```bash
mvn test
```

---

## Usage

### Low-level ITM construction

An `InterpretableTable` can be constructed directly by assembling the syntactic and semantic layers programmatically. This approach gives full control over items and interpretation actions.

```java
import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.interpret.TableInterpreter;
import ru.icc.regtab.itm.model.semantics.TableSemantics;
import ru.icc.regtab.itm.model.semantics.action.InterpretationAction;
import ru.icc.regtab.itm.model.semantics.item.*;
import ru.icc.regtab.itm.model.semantics.operation.*;
import ru.icc.regtab.itm.model.semantics.provider.*;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.recordset.Recordset;

// 1. Build the syntactic layer from your data source
TableSyntax syntax = new TableSyntax(3, 3);
syntax.getCell(0, 0).setText("");
syntax.getCell(0, 1).setText("CA");
syntax.getCell(0, 2).setText("HU");
syntax.getCell(1, 0).setText("IKT");
syntax.getCell(1, 1).setText("0 Jan");
syntax.getCell(1, 2).setText("8 Feb");
syntax.getCell(2, 0).setText("SVO");
syntax.getCell(2, 1).setText("31 Jan");
syntax.getCell(2, 2).setText("40 Feb");

// 2. Create cell-derived and context-derived items, wire up
//    interpretation actions, and build TableSemantics
// ...

// 3. Interpret
InterpretableTable itm = new InterpretableTable(syntax, semantics);
Recordset result = new TableInterpreter().interpret(itm);
```

### Using ATP patterns

The `ru.icc.regtab.itm.atp.spec` package provides the formal ATP types. Build a `TablePattern` from `SubtablePattern`, `RowPattern`, `SubrowPattern`, and `CellPattern` instances with their content and action specifications, then use the Pattern Matcher to match it against an ITM instance and populate the semantic layer automatically.

### Illustrative example

`AtpIllustrativeExampleTest` implements the worked example from Section VI of the paper — a table class listing the numbers of airline departures from airports by month. The target schema is `⟨ND, AIRLINE, AIRPORT, MON⟩`.

The test covers three cases:

- `paperExample_3x3_table_t0` — matches the 3 × 3 table from Figure 7 and verifies all four extracted records
- `extendedTable_4airlines_3airports` — matches a 4 × 5 table (4 airlines, 3 airports) and verifies 12 records
- `malformedTable_bodyCell_missingDelimiter_fails` — verifies that a table with malformed body cells does not match

To run it:

```bash
mvn test -Dtest="AtpIllustrativeExampleTest"
```

---

## Evaluation

RegTab has been evaluated on the **Foofah benchmark** — a well-established collection of 50 tabular data transformation tasks assembled by Jin et al. (2017) from real-world forums and related work (37 real-world cases, 13 synthetic). Each task provides five source tables from the same class and five corresponding target recordsets.

All 50 tasks are solved by ATP-based patterns implemented in jRegTab and verified by a JUnit 5 test suite (see [Testing](#testing) below). Automated comparison with ground-truth confirms that all **250 source-table variants** are transformed correctly (100 % accuracy).

The benchmark data (input and expected CSV files) is available at:
<https://github.com/umich-dbgroup/foofah>

---

## Testing

The test suite lives under `src/test/java/ru/icc/regtab/itm/` and is split into two complementary parts.

### ATP benchmark tests

The primary benchmark tests are in the `atp` package. Each class `AtpTask{NN}Test` implements one Foofah task as an ATP pattern using the formal `ru.icc.regtab.itm.atp.spec` API:

```
src/test/java/ru/icc/regtab/itm/atp/
    AtpTaskBase.java          # parameterised base: loads CSV, runs matcher, asserts output
    AtpTask01Test.java
    AtpTask02Test.java
    ...
    AtpTask50Test.java
```

Each test class overrides two methods:

- `taskId()` — returns the two-digit task number (e.g. `"01"`)
- `buildPattern()` — constructs and returns the `TablePattern` for that task

`AtpTaskBase` runs five JUnit parameterized test variants (`@ValueSource(ints = {1,2,3,4,5})`), one per source table. For each variant it:

1. Loads `src/test/resources/tasks/task_{NN}/input_{V}.csv` into a `TableSyntax`
2. Calls `AtpMatcher.match(pattern, syntax)` to populate the semantic layer
3. Interprets the enriched `InterpretableTable` with `TableInterpreter`
4. Applies optional post-processing (e.g. `WhitespaceNormalization`)
5. Asserts the result against `src/test/resources/tasks/task_{NN}/expected_{V}.csv`

Tasks 36–39 are currently covered by the Fluent API tests only (see below) and do not yet have dedicated `AtpTask{NN}Test` classes.

**Example — Task 01** (subtables with a `rec` action using the `sameSubtable` predicate):

```java
@Override
protected TablePattern buildPattern() {
    return TablePattern.of(
        SubtablePattern.of(Quantifier.oneOrMore(),
            RowPattern.of(
                CellPattern.of(AtomicContentSpec.val(
                    ActionSpec.rec(ProviderSpec.of((a, c) -> c.is.in.sameSubtable(a)))
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

### Fixture data

Source and expected tables are stored as CSV files:

```
src/test/resources/tasks/
    task_01/
        input_1.csv  …  input_5.csv
        expected_1.csv  …  expected_5.csv
    task_02/
        ...
    ...
    task_50/
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

To run a single task:

```bash
mvn test -Dtest="AtpTask01Test"
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
