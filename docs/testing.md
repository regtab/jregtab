# Testing

The test suite lives under `src/test/java/ru/icc/regtab/` and is split into two complementary
parts that exercise the same [benchmark tasks](benchmark.md) through both pattern
representations: the ATP fluent API and the RTL DSL.

---

## ATP benchmark tests

The primary benchmark tests are in the `atp` package. Each class `AtpTask{NN}Test` implements one
task as an ATP pattern using the formal `ru.icc.regtab.atp.spec` API:

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
    AtpTask111Test.java       # Baikal benchmark tasks 111–150
    ...
    AtpTask150Test.java
```

Each test class overrides two methods:

- `taskId()` — returns the three-digit task number (e.g. `"001"`)
- `buildPattern()` — constructs and returns the `TablePattern` for that task

`AtpTaskBase` runs five JUnit parameterized test variants (`@ValueSource(ints = {1,2,3,4,5})`),
one per source table. For each variant it:

1. Loads `src/test/resources/tasks/task_{NN}/input_{V}.csv` into a `TableSyntax`
2. Calls `AtpMatcher.match(pattern, syntax)` to populate the semantic layer
3. Interprets the enriched `InterpretableTable` with `TableInterpreter`
4. Applies optional post-processing (e.g. `WhitespaceNormalization`)
5. Asserts the result against `src/test/resources/tasks/task_{NN}/expected_{V}.csv`

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

---

## RTL benchmark tests

The `rtl` package mirrors the ATP benchmark: each `RtlTask{NN}Test` implements the same task as a
compact RTL string. These tests verify that the RTL compiler produces an ATP pattern equivalent
to the hand-crafted ATP counterpart.

```
src/test/java/ru/icc/regtab/rtl/
    RtlTaskBase.java          # loads CSV, compiles RTL → ATP, runs matcher, asserts output
    RtlTask001Test.java       # Foofah benchmark tasks 001–050
    ...
    RtlTask110Test.java       # RegTab benchmark tasks 051–110
    RtlTask111Test.java       # Baikal benchmark tasks 111–150
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

---

## Fixture data

Source and expected tables are stored as CSV files (UTF-8 without BOM):

```
src/test/resources/tasks/
    task_001/
        input_1.csv  …  input_5.csv
        expected_1.csv  …  expected_5.csv
    task_002/
        ...
    task_150/
        ...
```

When a pattern produces named schema attributes (via AVP actions), the `expected_{V}.csv`
files carry a header row; this is declared per task in
`src/test/resources/tasks/task_match_options.json`.

---

## Running the tests

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

To run a single task (ATP and RTL counterparts):

```bash
mvn test -Dtest="AtpTask001Test,RtlTask001Test"
```

The illustrative example from the paper has its own pair of tests:

```bash
mvn test -Dtest="AtpIllustrativeExampleTest,RtlIllustrativeExampleTest"
```
