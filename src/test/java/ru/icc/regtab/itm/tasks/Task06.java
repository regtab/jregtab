package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.pattern.ProviderSpec;
import ru.icc.regtab.itm.pattern.TablePattern;
import ru.icc.regtab.itm.model.syntax.Cell;
import ru.icc.regtab.itm.model.syntax.TableSyntax;

/**
 * Task 06 (Foofah exp0_7): свернуть блок из 5 строк в одну широкую запись — непустые ячейки в порядке
 * чтения (row-major), пустые пропускаются через {@code when(textBlank).skip().otherwise().val()}.
 * <p>
 * {@code O_rec} на левом верхнем value-якоре собирает все остальные value-ячейки того же подтабличного блока,
 * идущие строго после якоря в row-major порядке (одного {@code sameSubtable(a)} недостаточно — нужен порядок).
 */
public final class Task06 extends TaskBase {

    private static final ProviderSpec REC_AFTER_ANCHOR = 
        ProviderSpec.val((a, c) -> c.sameSubtable(a));

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().oneOrMore()
                .rows().one()
                .cells().one().val()
                .actions().rec(REC_AFTER_ANCHOR)
                .cells().oneOrMore().when(Cell::textBlank).skip().otherwise().val()
                .rows().exactly(4)
                .cells().oneOrMore().when(Cell::textBlank).skip().otherwise().val()
                .apply(syntax);
    }
}
