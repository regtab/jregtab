package ru.icc.regtab.interpret;

import ru.icc.regtab.recordset.Recordset;

/**
 * Optional post-processing transformation applied to the extracted recordset.
 */
public sealed interface RecordsetTransformation
        permits AnchorAttributeAtPosition,
                SchemaReordering,
                FieldSplitting,
                DelimitedFieldSplit,
                WhitespaceNormalization {

    Recordset apply(Recordset recordset);

    default RecordsetTransformation withAnonymousAttributeTemplate(String template) {
        return this;
    }
}
