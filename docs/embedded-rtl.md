# Embedded RTL

**Embedded RTL** is a Java API (package `ru.icc.regtab.dsl`) that mirrors RTL syntax while
remaining ordinary Java code. It combines the brevity of RTL with full Java integration:
lambdas as constraints, IDE completion, compile-time structure checking, and pattern
composition with plain variables and methods.

Patterns built with embedded RTL produce **exactly the same ATP objects** as
`RtlCompiler.compile(...)` — this equivalence is verified test-by-test for a representative
subset of the benchmark tasks (`DslSpikeTest`).

```java
import static ru.icc.regtab.dsl.Rtl.*;
```

## Quick example

RTL (task 001):

```rtl
{ [ [VAL : ST*->REC] [VAL]{2} []+ ]
  [ []               [VAL]{4} []+ ] }+
```

Embedded RTL:

```java
TablePattern p = table(
    subtable( row( cell(VAL, rec(ST.unbounded())), cell(VAL).exactly(2), skip().oneOrMore() ),
         row( skip(),                         cell(VAL).exactly(4), skip().oneOrMore() )
    ).oneOrMore());
```

The pattern is matched and interpreted exactly like any other `TablePattern`.

---

## Vocabulary

### Pattern levels and quantifiers

| RTL | Embedded RTL |
|---|---|
| table pattern | `table(subtable…)` |
| `{ rows }` subtable | `subtable(row…)` |
| `[ cells ]` row | `row(cell…)` |
| `{ cells }` explicit subrow | `subrow(cell…)`, mixed with implicit runs: `row(subrow(…), subrow(…))` |
| `+` `*` `?` `{n}` | postfix `.oneOrMore()` `.zeroOrMore()` `.zeroOrOne()` `.exactly(n)` |

A row with plain cells (`row(cell…)`) wraps them into one implicit subrow, exactly like the
compiler.

### Cells and guards

| RTL | Embedded RTL |
|---|---|
| `[]` | `skip()` |
| `[VAL : acts]` | `cell(VAL, acts…)` |
| `[!BLANK ? VAL : acts]` | `cell(notBlank(), VAL, acts…)` |
| `[BLANK]` (condition-only) | `cell(blank())` |
| guards `BLANK` `"re"` `~"s"` (+ `!`) | `blank()` `re("…")` `contains("…")` / `notBlank()` `notRe("…")` `notContains("…")` |
| — (escape hatch) | `cell(where("desc", c -> …), VAL)` |

### Content specifications

| RTL | Embedded RTL |
|---|---|
| `VAL` / `ATTR` / `AUX` / `_` | constants `VAL ATTR AUX SKIP`; atoms `val(acts…)` `attr(…)` `aux(…)` |
| `VAL #'tag'` | `val(…).tagged("tag")` |
| `VAL = NORM` (also `TRIM UC LC REPL SUBSTR`, chains) | `val(…).extract(NORM)`, `repl("rx","rep")`, `substr(b,e)`, `chain(…)` |
| compound `A "d" B` | `val(…).then(" ", val(…))` (chainable) |
| delimited `(VAL : …){","}` | `val(…).splitBy(",")` |
| conditional `BLANK ? _ \| VAL` | `when(blank(), SKIP, VAL)` (directives and specs mix freely) |

### Providers and constraints

| RTL | Embedded RTL |
|---|---|
| `LT RT AV BW ROW COL SR SC ST NCL CL STR` | constants with the same names (type `Prov`) |
| `Cn` / `Ca..b` / `C+n`, `C-n` / `C+a..b` / `C+a..*` | `C(n)` / `C(lo,hi)` / `Crel(±n)` / `Crel(lo,hi)` / `CrelFrom(lo)` |
| `Rn Ra..b R±n`, `Pn Pa..b P±n` | `R(…)`, `Rrel(…)`, `P(…)`, `Prel(…)` |
| `#'t'` / `!#'t'` | `tag("t")` / `notTag("t")` |
| item `"re"`, `~"s"`, `BLANK` (+ `!`) | `itemRe itemContains itemBlank` / `itemNotRe itemNotContains itemNotBlank` |
| `&` conjunction | `.and(…)` |
| `\|` disjunction | `.or(…)`; distribution matches the compiler: `A.and(B.or(C))` ≙ `(A&B)\|(A&C)` |
| `{n}` / `*` cardinality | `.card(n)` / `.unbounded()` |
| `-` / `^` / `-^` traversal | `.reversed()` / `.colMajor()` / `.reversedColMajor()` |
| — (escape hatch) | `ROW.where("desc", (anchor, candidate) -> …)` |

!!! note "Why `C(n)` and `Crel(n)` are separate"
    Java's unary plus is a no-op — `C(+1)` is indistinguishable from `C(1)` — so absolute
    `C1` and anchor-relative `C+1` need distinct factories. `Crel` takes a signed delta:
    `Crel(1)` ≙ `C+1`, `Crel(-1)` ≙ `C-1`.

### Actions and context providers

| RTL | Embedded RTL |
|---|---|
| `(…)->REC` / `REC(n)` / `REC('s')` | `rec(…)` / `rec(n, …)` / `recSplit("s", …)` |
| `prov->AVP` / `'NAME'->AVP` | `avp(prov)` / `avp("NAME")` |
| `(…)->JOIN` / `JOIN(k)` | `join(…)` / `join(k, …)` |
| `(…)->FILL('d')`, `PREFIX`, `SUFFIX` | `fill("d", …)`, `prefix(…)`, `suffix(…)` (delimiter optional) |
| `'EUR'` context literal | `lit("EUR")` (VALUE under REC/JOIN, ATTRIBUTE otherwise — as in the compiler) |
| `@'K'='V'` | `ctxAvp("K", "V")` |

Provider kinds (VAL/ATTR/UNRESTRICTED) are inferred from the action, exactly as in the
RTL compiler — `rec(ST)` builds a VAL provider, `avp(SC)` an ATTR provider.

### Level-scoped (inherited) actions and conditions

RTL allows action specs and a condition at the table, subtable, row, and subrow level;
they are merged down into every atom below. Embedded RTL mirrors this with `acts(…)` and
`CellPredicate` overloads:

```java
// RTL: [ BW*->REC { [ATTR] [VAL] }* ]
row(acts(rec(BW.unbounded())),
    subrow(cell(ATTR), cell(VAL)).zeroOrMore())

// RTL: !BLANK ? BW*->REC [ [VAL] ]+
table(notBlank(), acts(rec(BW.unbounded())), subtable(row(cell(VAL)).oneOrMore()))
```

### Settings

RTL settings prefix maps to transformations:

```java
// RTL: <NORM,ANCH(1),SPLIT(",")> …
table(…).withTransformations(norm(), anch(1), split(","))
```

Inline `rec(n, …)` / `recSplit("s", …)` parameters are collected into transformations
automatically by `table(…)`, as in the compiler.

### Fragments are just Java

RTL named fragments `$name=[…]` become variables — and gain parameterisation for free:

```java
// RTL: $V=['\d+' ? VAL: (COL&#'H'*,ROW&#'S'*)->REC]  …  [$V]+
var v = cell(re("\\d+"), VAL, rec(COL.and(tag("H")).unbounded(),
                                  ROW.and(tag("S")).unbounded()));
row(cell(blank()).oneOrMore(), v.oneOrMore())
```

---

## Escape hatches into Java

The reason embedded RTL exists: anywhere the model accepts a predicate, plain Java works.

```java
TablePattern p = table(subtable(row(
    cell(where("isTotal", c -> c.text().startsWith("Total")), VAL,
         rec(ROW.where("isNum", (a, c) -> c.str().matches("\\d+")).unbounded())),
    cell(VAL).oneOrMore())));
```

Patterns containing `where(...)` cannot be serialized to RTL (same limitation as
`CellPredicate.Custom`); named `EXT('name')` [bindings](rtl-reference.md#external-java-bindings--extname)
remain the serializable alternative for string RTL.

---

## Relation to the other two APIs

| | RTL string | Embedded RTL | ATP API |
|---|---|---|---|
| Brevity | ✅✅ | ✅ | ❌ |
| Java lambdas | via `EXT` + `Bindings` | ✅ direct | ✅ direct |
| Compile-time checking, IDE support | ❌ | ✅ | ✅ |
| Layer | compiles to ATP | thin sugar over ATP | the model itself |

Embedded RTL is a construction layer only — it adds no expressive power beyond ATP, and
every pattern it builds is an ordinary `TablePattern`. The ATP API remains the documented
low-level layer.
