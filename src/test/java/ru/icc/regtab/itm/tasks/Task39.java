package ru.icc.regtab.itm.tasks;

import ru.icc.regtab.itm.InterpretableTable;
import ru.icc.regtab.itm.model.syntax.TableSyntax;
import ru.icc.regtab.itm.pattern.TablePattern;

/**
 * Task 39 (Foofah exp0_craigslist_data_wrangler). Compound: цена, затем {@code " / "}, число спален, затем {@code "br"},
 * остаток заголовка — {@link ru.icc.regtab.itm.pattern.TablePattern.CompoundBuilder#skip()}.
 */
public final class Task39 extends TaskBase {

    @Override
    protected InterpretableTable buildItm(TableSyntax syntax) {
        return TablePattern.define()
                .subtables().one()
                .rows().oneOrMore()
                .cells().one().compound()
                .val().actions().rec((a, c) -> c.is.in.sameCell(a))
                .sep(" / ")
                .val()
                .sep("br")
                .skip()
                .apply(syntax);
    }
}
