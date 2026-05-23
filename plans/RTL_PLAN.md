# RTL DSL Compiler: техническая документация

> **Статус: реализовано (задачи 01–51).** Все 850 тестов проходят (RtlTask01–51, AtpTask01–50, RtlCompilerTest, TaskRunnerTest).
>
> **Запланировано:** расширение грамматики — константный AVP-провайдер `@'ATTR'='VALUE'` и квотирование тегов `#'tag'`. Подробности в разделе [«Запланированные расширения»](#запланированные-расширения).

## Контекст

Проект jRegTab реализует метод RegTab для извлечения структурированных данных из таблиц.
RTL-компилятор транслирует текстовые строки на языке RTL в объекты ATP-спецификации.

```java
TablePattern p = RtlCompiler.compile("""
    [ [SKIP] [VAL : ('AIRLINE')->AVP]+ ]
    [ [VAL : ('AIRPORT')->AVP] [VAL : (CM{1}, LW{1}, CL{1})->REC, ('ND')->AVP " " VAL : ('MON')->AVP]+ ]+
""");
Optional<InterpretableTable> t = AtpMatcher.match(p, syntax);
```

---

## Структура файлов

```
src/
├── main/
│   ├── antlr4/ru/icc/regtab/rtl/
│   │   └── RTL.g4                             # грамматика RTL
│   └── java/ru/icc/regtab/rtl/
│       ├── RtlCompiler.java                   # публичный API
│       ├── RtlCompileException.java           # unchecked-исключение
│       └── internal/
│           ├── ATPBuilder.java                # RTLBaseVisitor → ATP-объекты
│           ├── ProviderTemplateResolver.java  # пространственные/контентные ограничения → condition+order
│           └── StringExtractorFactory.java    # SUBSTR/REPL/UC/LC → StringExtractor
└── test/
    └── java/ru/icc/regtab/rtl/
        ├── RtlCompilerTest.java               # unit-тесты парсера + интеграционные тесты
        └── RtlTask01Test … RtlTask51Test      # RTL-эквиваленты AtpTask01–51
```

---

## Грамматика RTL (ключевые правила)

```antlr
tablePattern     : subtablePattern+ ;
subtablePattern  : implSubtablePattern | explSubtablePattern ;
implSubtablePattern  : rowPattern+ ;
explSubtablePattern  : LCURLY subtablePatternBody RCURLY quantifier? ;

rowPattern    : LSQUARE rowPatternBody RSQUARE quantifier? ;
cellPattern   : LSQUARE cellPatternBody RSQUARE quantifier? ;
cellPatternBody : (cellMatchCond QUESTION)? actSpecs? contSpec ;

contSpec   : atomContSpec | delimContSpec | compContSpec | condContSpec ;
atomContSpec : itemDerivDir tags? (ASSIGN strExtr)? (COLON actSpecs)? ;
compContSpec : openDelim? atomContSpec (separator atomContSpec)* closeDelim? ;
delimContSpec : LPAREN atomContSpec RPAREN LCURLY separator RCURLY ;
condContSpec : LPAREN cellMatchCond QUESTION (xContSpec VBAR xContSpec) RPAREN ;

actSpecs   : actSpec (COMMA actSpec)* ;
actSpec    : provSpecs RIGHT_ARROW op ;
provSpecs  : provSpec | (LPAREN provSpec (COMMA provSpec)* RPAREN) ;
provSpec   : tblProvSpec | ctxProvSpec ;
tblProvSpec : provTemplate cardinality? constraints? ;
ctxProvSpec : STRING ;

cellMatchCond  : regex | blank ;
blank          : EXCLAMATION? 'BLANK' ;
```

---

## ATPBuilder: правила преобразования

### Кванторы
| RTL | Java |
|-----|------|
| (отсутствует) | `Quantifier.one()` |
| `?` | `Quantifier.zeroOrOne()` |
| `*` | `Quantifier.zeroOrMore()` |
| `+` | `Quantifier.oneOrMore()` |
| `{n}` | `Quantifier.exactly(n)` |

### Условия совпадения ячеек
| RTL | Java |
|-----|------|
| `"pattern"?` | `new CellMatchCondition(c -> c.text().matches("pattern"))` |
| `!"pattern"?` | `new CellMatchCondition(c -> !c.text().matches("pattern"))` |
| `BLANK?` | `new CellMatchCondition(c -> c.textBlank())` |
| `!BLANK?` | `new CellMatchCondition(c -> !c.textBlank())` |

### ContentSpec
| RTL | Java |
|-----|------|
| `ATTR` / `VAL` / `AUX` / `SKIP` | `AtomicContentSpec.attr/val/aux()` / `CellPattern.skip()` |
| `(atom){","}` | `DelimitedContentSpec(",", atom)` |
| `atom1 "sep" atom2` | `CompoundContentSpec.of(atom1, Segment("sep", atom2))` |
| `(BLANK ? xs \| xs)` | `ConditionalContentSpec(BLANK, xs+, xs-)` |

### Строковые экстракторы
| RTL | Java |
|-----|------|
| `VAL = UC` | `s -> s.toUpperCase()` |
| `VAL = LC` | `s -> s.toLowerCase()` |
| `VAL = SUBSTR(n, m)` | `StringExtractor.substring(n, m)` |
| `VAL = REPL('pat', 'repl')` | `StringExtractor.replace("pat", "repl")` |

### Спецификации действий
| RTL | Java |
|-----|------|
| `(prov1, prov2)->REC` | `ActionSpec.rec(prov1, prov2)` |
| `(prov)->AVP` | `ActionSpec.avp(prov)` |
| `('literal')->AVP` | `ActionSpec.avp("literal")` (ctxAttr) |
| `(prov)->CONCAT` | `ActionSpec.concat(prov)` |
| `(prov)->FILL` | `ActionSpec.fill("", prov)` |
| `(prov)->FILL('/')` | `ActionSpec.fill("/", prov)` |
| `(prov)->PREFIX(' ')` | `ActionSpec.prefix(" ", prov)` |
| `(prov)->SUFFIX(',')` | `ActionSpec.suffix(",", prov)` |

### Автоматический вывод CellDerivedProviderKind
Применяется для провайдеров в actSpecs, явно привязанных к atomContSpec:

| Действие | Якорь | Kind | Кардинальность |
|----------|-------|------|---------------|
| REC, CONCAT | VAL | VAL | как указано |
| AVP | VAL | ATTR | авто-1 если не указана |
| FILL, PREFIX, SUFFIX | любой | UNRESTRICTED | как указано |

Для inherited actSpecs (уровень subtable/row/subrow/cell до contSpec) — всегда UNRESTRICTED.

Context-провайдеры (`('literal')`) → всегда `ProviderSpec.ctxAttr(literal)`.

---

## ProviderTemplateResolver: шаблоны и ограничения

### Базовые условия шаблонов
| Шаблон | TraversalOrder | Базовое условие κ |
|--------|----------------|-------------------|
| `LW` | REVERSE_ROW_MAJOR | `c.sameSubrow(a) && col < col(a)` |
| `RW` | ROW_MAJOR | `c.sameSubrow(a) && col > col(a)` |
| `UW` | REVERSE_COLUMN_MAJOR | `c.sameSubtable(a) && c.sameCol(a) && row < row(a)` |
| `DW` | COLUMN_MAJOR | `c.sameSubtable(a) && c.sameCol(a) && row > row(a)` |
| `RM` | ROW_MAJOR | `c.sameSubrow(a) && !c.sameCell(a)` |
| `CM` | COLUMN_MAJOR | `c.sameSubtable(a) && c.sameCol(a) && !c.sameCell(a)` |
| `CL` | ROW_MAJOR | `c.sameCell(a)` |

### Пространственные ограничения
Ограничения **заменяют** базовое пространственное условие (не добавляют к нему):

| Ограничение | Заменяет у LW/RW/RM | Заменяет у UW/DW/CM | Заменяет у CL |
|-------------|--------------------|--------------------|--------------|
| `COL+n` | — | `sameCol(a)` → `col == col(a)+n` | `sameCell(a)` → `col == col(a)+n` |
| `COLn` (абс.) | — | `sameCol(a)` → `col == n` | `sameCell(a)` → `col == n` |
| `COL a..b` | — | `sameCol(a)` → `col ∈ [a,b]` | `sameCell(a)` → `col ∈ [a,b]` |
| `ROW+n` | `sameSubrow(a)` → `row == row(a)+n` | `sameSubtable(a)` → `row == row(a)+n` | `sameCell(a)` → `row == row(a)+n` |
| `POSn` | — | — | `sameCell(a)` → `pos == n` |
| `ST` | `sameSubrow(a)` → `sameSubtable(a)` | убирает `sameCol(a)` | `sameCell(a)` → `sameSubtable(a)` |

Несколько ограничений комбинируются через AND: `CL(ROW+0, COL0)` = `sameRow(a) && col == 0`.

### Контентные ограничения (добавляются через AND)
| Ограничение | Условие |
|-------------|---------|
| `"regex"` | `c.strMatching("regex")` |
| `BLANK` | `c.blankStr()` |
| `!BLANK` | `!c.blankStr()` |
| `TAG #'t1' #'t2'` | `c.hasAnyTag(["#t1","#t2"])` (OR-семантика) |
| `STR` | `c.sameStr(a)` |

### Часто используемые комбинации
| RTL | Семантика |
|-----|-----------|
| `CL` | sameCell(a) |
| `CL(ST)` | sameSubtable(a) |
| `CL{1}(ROW+0)` | sameRow(a), card=1 |
| `CL{1}(COL+0)` | sameCol(a), card=1 |
| `CL(ST, COL1)` | sameSubtable(a) && col==1 |
| `CL(ROW+0, COL0)` | sameRow(a) && col==0 |
| `DW(STR)` | below(a) && sameCol(a) && sameStr(a) |
| `DW(ST)` | sameSubtable(a) && row > row(a) |
| `RM(ST)` | sameSubtable(a) && !sameCell(a) |
| `RW{1}(ROW+0)` | rightOf(a) && sameRow(a), card=1 |

---

## ItemLinearization: порядок обхода

Исправление (в этой ветке): `REVERSE_COLUMN_MAJOR` вторичный ключ — убывающая строка,
чтобы `UW{1}` возвращал ближайшую сверху ячейку, а не верхнюю в столбце.

| TraversalOrder | Первичный ключ | Вторичный ключ |
|----------------|----------------|----------------|
| ROW_MAJOR (→) | row ↑ | col ↑ |
| REVERSE_ROW_MAJOR (←) | row ↓ | col ↑ |
| COLUMN_MAJOR (↓) | col ↑ | row ↑ |
| REVERSE_COLUMN_MAJOR (↑) | col ↓ | row ↓ |

Следствие: `UW{1}` = REVERSE_COLUMN_MAJOR, card=1 → ближайшая сверху.
`CM{1}` = COLUMN_MAJOR, card=1 → верхняя в столбце.

---

## Расширения, добавленные в ветке `feature/rtl-compiler`

1. **`ST`** — пространственное ограничение `sameSubtable` (RTL.g4 + ProviderTemplateResolver)
2. **`STR`** — контентное ограничение `sameStr(a)` (RTL.g4 + ProviderTemplateResolver)
3. **TAG OR-семантика** — `TAG #'a' #'b'` = anyMatch (было allMatch)
4. **Исправление `ItemLinearization`** — REVERSE_COLUMN_MAJOR вторичный ключ row ↓ (было row ↑)

---

## Запланированные расширения

Детальный план: [RTL_CONTEXT_AVP_PLAN.md](RTL_CONTEXT_AVP_PLAN.md)

Две взаимосвязанные задачи:
- **Квотирование тегов** `#'tag'` вместо `#tag` — единообразие синтаксиса
- **Константный AVP-провайдер** `@'ATTR'='VALUE'` внутри `->REC` — для задачи 52 (YEAR='2025')
