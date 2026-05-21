package ru.icc.regtab.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.syntax.Cell;
import ru.icc.regtab.pattern.ProviderSpec;
import ru.icc.regtab.pattern.TablePattern;
import ru.icc.regtab.itm.syntax.TableSyntax;
import ru.icc.regtab.recordset.Recordset;

import java.util.function.Function;

/**
 * Task 09 (Foofah exp0_11): unpivot grades — колонка {@code Name}, заголовки предметов в первой строке
 * (пробелы убираются: {@code Subject 1} → {@code Subject1}); непустые оценки в строках, пустые ячейки пропускаются.
 * <p>
 * {@code rec}: якорь — ячейка оценки; провайдеры — имя в той же строке и заголовок предмета в той же колонке.
 * {@link AnchorAttributeAtPosition}{@code (2)}: эталон {@code Name}, {@code Subject}, {@code Grade}.
 */
public final class Task09 extends TaskBase {

    private static final ProviderSpec FIRST_IN_SAME_ROW =
            ProviderSpec.val((a, c) -> c.sameRow(a), 1);

    private static final ProviderSpec FIRST_IN_SAME_COLUMN =
            ProviderSpec.val((a, c) -> c.sameCol(a), 1);

    /** Header cell text: remove all whitespace (e.g. {@code Subject 1} → {@code Subject1}). */
    private static final Function<Cell, String> HEADER_VALUE_TEXT = 
        c -> c.text().replaceAll("\\s+", "");

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(2).apply(actual);
    }

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().one()
                .rows().one()
                .cells().one().skip()
                .cells().exactly(5).val(HEADER_VALUE_TEXT)
                .rows().oneOrMore()
                .cells().one().val()
                .cells().oneOrMore().when(Cell::textBlank).skip().otherwise().val()
                .actions().rec(FIRST_IN_SAME_ROW, FIRST_IN_SAME_COLUMN)
                .apply(syntax);
    }
}
