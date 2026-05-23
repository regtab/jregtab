# RTL: константный AVP-провайдер и квотирование тегов

## Мотивация

Задача 52 требует инжекции константной пары (YEAR, '2025') в каждую запись
без опоры на какую-либо ячейку таблицы. Текущая грамматика не поддерживает
литеральные значения как провайдеры.

Ожидаемый RTL для задачи 52:
```
[ [] [VAL : 'AIRLINE'->AVP]+ ]
[ [VAL : 'AIRPORT'->AVP]
  [VAL : (^SC, -LT, CL, @'YEAR'='2025')->REC, 'ND'->AVP " " VAL : 'MON'->AVP]+ ]+
```

Фикстуры: `src/test/resources/tasks/task_52/` (скопированы из task_51, expected содержат YEAR).

---

## Дизайн-решение

**Принцип единообразия:** все именованные сущности в RTL — скавыченные строки с префиксом.

| Конструкция | Синтаксис | Роль |
|---|---|---|
| Имя атрибута | `'AIRLINE'->AVP` | контекстный ATTR-провайдер |
| Тег (ячейки) | `#'head'` | контентный тег |
| Тег (ограничение) | `TAG #'t1' #'t2'` | фильтр по тегам |
| Константный AVP | `@'YEAR'='2025'` | ATTR+VAL-провайдер в REC |

---

## Шаг 1: Квотирование тегов (`#tag` → `#'tag'`)

### RTL.g4

```antlr
// Было:
tags : TAG+ ;
tag  : EXCLAMATION? 'TAG' TAG+ ;
TAG  : '#' [a-z_] [a-z_0-9]* ;

// Станет:
tags    : tagItem+ ;
tag     : EXCLAMATION? 'TAG' tagItem+ ;
tagItem : HASH STRING ;
HASH    : '#' ;
```

`HASH` — новый односимвольный токен; `STRING` — уже существующий.

### ATPBuilder и ProviderTemplateResolver

Везде, где читается `TAG().getText()` — заменить на:
```java
StringExtractorFactory.parseStringLiteral(ctx.tagItem().STRING().getText())
```

### Тесты (только RtlCompilerTest, 2 строки)

```java
// Было:
compile("[ [VAL : (SR & TAG #t1 #t2)->REC] ]");
compile("[ [VAL #head #bold] ]");

// Станет:
compile("[ [VAL : (SR & TAG #'t1' #'t2')->REC] ]");
compile("[ [VAL #'head' #'bold'] ]");
```

---

## Шаг 2: Константный AVP-провайдер (`@'ATTR'='VALUE'`)

### RTL.g4

```antlr
ctxAvpSpec : AT STRING ASSIGN STRING ;   // @'ATTR'='VALUE'
AT         : '@' ;

// Расширение:
provSpec : tblProvSpec | ctxProvSpec | ctxAvpSpec ;
```

`AT` — новый токен; `ASSIGN` (`=`) уже существует.  
`ctxAvpSpec` разрешён только в `->REC` и `->CONCAT`; в `->AVP` — ошибка компиляции.

### ProviderSpec

```java
// ContextLiteralSpec расширяется вторым опциональным полем:
public record ContextLiteralSpec(String text, ItemType type, @Nullable String constValue) {}

// Новый фабричный метод:
public static ProviderSpec ctxAvp(String attrName, String value) {
    return new ProviderSpec(1, TraversalOrder.ROW_MAJOR, null, null,
            new ContextLiteralSpec(attrName, ItemType.ATTRIBUTE, value));
}
```

### ATPBuilder — buildProvSpec

```java
if (ctx.ctxAvpSpec() != null) {
    String name  = StringExtractorFactory.parseStringLiteral(
                       ctx.ctxAvpSpec().STRING(0).getText());
    String value = StringExtractorFactory.parseStringLiteral(
                       ctx.ctxAvpSpec().STRING(1).getText());
    return ProviderSpec.ctxAvp(name, value);
}
```

### WorkingState

Новый метод `injectConstAvp`, вызываемый из `applyRec` для каждого ctxAvp-провайдера:

```java
// applyRec: при обходе провайдеров записи
for (Item item : items) {
    if (item instanceof ContextDerivedItem cdi && cdi.spec().constValue() != null) {
        ws.injectConstAvp(cdi.str(), cdi.spec().constValue());
        continue;
    }
    sequence.add(item);
}
```

`injectConstAvp` добавляет константную пару в `avp`-маппинг без якорной ячейки.
Пара включается в каждую запись, использующую данный REC-якорь.

### Порядок атрибутов в схеме

Константные AVP-провайдеры вносятся в схему в порядке появления в `provSpecs`.  
Для задачи 52: ND, AIRLINE, AIRPORT, MON, YEAR  
(`@'YEAR'='2025'` — четвёртый в `(^SC, -LT, CL, @'YEAR'='2025')->REC`, после MON из CL).

---

## Шаг 3: RtlTask52Test

```java
public class RtlTask52Test extends RtlTaskBase {
    @Override protected String taskId() { return "52"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [VAL : 'AIRLINE'->AVP]+ ]
                [ [VAL : 'AIRPORT'->AVP]
                  [VAL : (^SC, -LT, CL, @'YEAR'='2025')->REC, 'ND'->AVP " " VAL : 'MON'->AVP]+ ]+
                """;
    }
}
```

---

## Шаг 4: AtpToRtlSerializer

Добавить сериализацию `ctxAvp`-провайдеров:
```java
// В методе сериализации provSpec:
if (ps.isContextLiteral() && ps.contextLiteral().constValue() != null) {
    return "@'" + ps.contextLiteral().text() + "'='"
           + ps.contextLiteral().constValue() + "'";
}
```

---

## Последовательность реализации

1. **RTL.g4** — добавить `HASH`, `tagItem`, `AT`, `ctxAvpSpec`; обновить `tags`, `tag`, `provSpec`
2. **ATPBuilder / ProviderTemplateResolver** — заменить `TAG().getText()` на `tagItem()`
3. **RtlCompilerTest** — исправить 2 строки с тегами
4. Запустить все тесты — должны быть зелёными (квотирование тегов не ломает задачи 01–51)
5. **ProviderSpec** — добавить `constValue` в `ContextLiteralSpec` и `ctxAvp(...)`
6. **ATPBuilder** — ветка `ctxAvpSpec` в `buildProvSpec`
7. **WorkingState** — `injectConstAvp` + интеграция в `applyRec`
8. **RtlTask52Test** — написать тест, запустить все 5 вариантов
9. **AtpToRtlSerializer** — сериализация `ctxAvp`
