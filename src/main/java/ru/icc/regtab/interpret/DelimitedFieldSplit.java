package ru.icc.regtab.interpret;

import ru.icc.regtab.recordset.Record;
import ru.icc.regtab.recordset.Recordset;
import ru.icc.regtab.recordset.Schema;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Expands fields that contain a literal delimiter into several atomic columns. For each source attribute (in schema
 * order), the maximum segment count over all records is computed with {@code String.split} on the delimiter (quoted
 * for regex safety). If that count is one, the column is left as a single field; otherwise it is replaced
 * by that many columns. The result schema uses the configured anonymous-attribute
 * template with 1-based positional indices; short segment lists are padded with empty strings.
 * <p>
 * {@code onlyAttributes}: when {@code null} or empty, every attribute is eligible (columns without multiple segments
 * stay width 1). When non-empty, only attributes whose names are in the set are considered for splitting; all others
 * keep width 1.
 */
public record DelimitedFieldSplit(String delimiter, Set<String> onlyAttributes, String anonymousAttributeTemplate)
        implements RecordsetTransformation {

    public DelimitedFieldSplit(String delimiter) {
        this(delimiter, null, "$a_%i");
    }

    public DelimitedFieldSplit(String delimiter, Set<String> onlyAttributes) {
        this(delimiter, onlyAttributes, "$a_%i");
    }

    public DelimitedFieldSplit {
        Objects.requireNonNull(delimiter, "delimiter");
        if (delimiter.isEmpty()) {
            throw new IllegalArgumentException("delimiter must be non-empty");
        }
        Objects.requireNonNull(anonymousAttributeTemplate, "anonymousAttributeTemplate");
        if (!anonymousAttributeTemplate.contains("%i")) {
            throw new IllegalArgumentException(
                    "Anonymous attribute template must contain the placeholder %i: " + anonymousAttributeTemplate);
        }
        onlyAttributes =
                onlyAttributes == null || onlyAttributes.isEmpty() ? null : Set.copyOf(onlyAttributes);
    }

    @Override
    public RecordsetTransformation withAnonymousAttributeTemplate(String template) {
        return new DelimitedFieldSplit(delimiter, onlyAttributes, template);
    }

    @Override
    public Recordset apply(Recordset recordset) {
        Schema oldSchema = recordset.schema();
        List<String> attrs = oldSchema.attributes();
        if (attrs.isEmpty()) {
            return recordset;
        }
        String quotedDelim = Pattern.quote(delimiter);
        int n = attrs.size();
        int[] width = new int[n];
        boolean anySplit = false;
        for (int i = 0; i < n; i++) {
            String name = attrs.get(i);
            if (onlyAttributes != null && !onlyAttributes.contains(name)) {
                width[i] = 1;
                continue;
            }
            int maxParts = 1;
            for (Record r : recordset.records()) {
                String val = r.get(name);
                if (val == null) {
                    val = "";
                }
                maxParts = Math.max(maxParts, val.split(quotedDelim, -1).length);
            }
            width[i] = maxParts;
            if (maxParts > 1) {
                anySplit = true;
            }
        }
        if (!anySplit) {
            return recordset;
        }
        int total = 0;
        for (int w : width) {
            total += w;
        }
        List<String> newAttrNames = new ArrayList<>(total);
        for (int j = 0; j < total; j++) {
            newAttrNames.add(anonymousAttribute(j + 1));
        }
        Schema newSchema = new Schema(newAttrNames);
        List<Record> out = new ArrayList<>(recordset.size());
        for (Record r : recordset.records()) {
            List<String> cells = new ArrayList<>(total);
            for (int i = 0; i < n; i++) {
                String name = attrs.get(i);
                String val = r.get(name);
                if (val == null) {
                    val = "";
                }
                if (width[i] == 1) {
                    cells.add(val);
                } else {
                    String[] parts = val.split(quotedDelim, -1);
                    for (int p = 0; p < width[i]; p++) {
                        cells.add(p < parts.length ? parts[p] : "");
                    }
                }
            }
            Map<String, String> map = new LinkedHashMap<>();
            for (int j = 0; j < total; j++) {
                map.put(newAttrNames.get(j), cells.get(j));
            }
            out.add(new Record(newSchema, map));
        }
        return new Recordset(newSchema, out);
    }

    private String anonymousAttribute(int index) {
        return anonymousAttributeTemplate.replace("%i", Integer.toString(index));
    }
}
