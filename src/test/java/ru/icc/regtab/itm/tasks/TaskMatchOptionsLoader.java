package ru.icc.regtab.itm.tasks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Loads {@link RecordsetMatchOptions} for task tests.
 * <p>
 * <b>Global</b> — {@code task_match_options.json} under {@code tasksRoot} (optional). Same as before for
 * {@link #load(Path)}: top-level {@code attributeOrder}, {@code recordOrder} only.
 * <p>
 * <b>Per-task</b> — {@link #load(Path, String)} merges, in order:
 * <ol>
 *   <li>defaults ({@link RecordsetMatchOptions#DEFAULT_STRICT})</li>
 *   <li>global file top-level fields</li>
 *   <li>global file {@code tasks["&lt;taskId&gt;"]} if present (e.g. {@code "05"})</li>
 *   <li>{@code task_&lt;taskId&gt;/task_match_options.json} if present (highest priority)</li>
 * </ol>
 * In each layer, only specified fields override; omitted fields keep the previous merge.
 */
public final class TaskMatchOptionsLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TaskMatchOptionsLoader() {}

    /**
     * Global options only (backward compatible). Ignores {@code tasks} map in the root file if present.
     */
    public static RecordsetMatchOptions load(Path tasksRoot) throws IOException {
        Path path = tasksRoot.resolve("task_match_options.json");
        if (!Files.isRegularFile(path)) {
            return RecordsetMatchOptions.DEFAULT_STRICT;
        }
        try (InputStream in = Files.newInputStream(path)) {
            RootOptionsFile root = MAPPER.readValue(in, RootOptionsFile.class);
            if (root == null) {
                return RecordsetMatchOptions.DEFAULT_STRICT;
            }
            return optionsFromTopLevel(root);
        }
    }

    /**
     * Merged options for a concrete task id (e.g. {@code "05"} for {@link ru.icc.regtab.itm.tasks.Task05}).
     */
    public static RecordsetMatchOptions load(Path tasksRoot, String taskId) throws IOException {
        Objects.requireNonNull(taskId, "taskId");
        RecordsetMatchOptions base = RecordsetMatchOptions.DEFAULT_STRICT;

        Path rootPath = tasksRoot.resolve("task_match_options.json");
        if (Files.isRegularFile(rootPath)) {
            try (InputStream in = Files.newInputStream(rootPath)) {
                RootOptionsFile root = MAPPER.readValue(in, RootOptionsFile.class);
                if (root != null) {
                    base = optionsFromTopLevel(root);
                    PartialOptions patch = root.tasks != null ? root.tasks.get(taskId) : null;
                    base = mergePartial(base, patch);
                }
            }
        }

        Path taskFile = tasksRoot.resolve("task_" + taskId).resolve("task_match_options.json");
        if (Files.isRegularFile(taskFile)) {
            try (InputStream in = Files.newInputStream(taskFile)) {
                PartialOptions patch = MAPPER.readValue(in, PartialOptions.class);
                base = mergePartial(base, patch);
            }
        }

        return base;
    }

    private static RecordsetMatchOptions optionsFromTopLevel(RootOptionsFile root) {
        OrderPolicy ao = parsePolicyOrDefault(root.attributeOrder, OrderPolicy.STRICT);
        OrderPolicy ro = parsePolicyOrDefault(root.recordOrder, OrderPolicy.STRICT);
        boolean hdr = root.expectedHasHeader != null ? root.expectedHasHeader : true;
        return new RecordsetMatchOptions(ao, ro, hdr);
    }

    private static RecordsetMatchOptions mergePartial(RecordsetMatchOptions base, PartialOptions patch) {
        if (patch == null) {
            return base;
        }
        OrderPolicy ao = patch.attributeOrder != null && !patch.attributeOrder.isBlank()
                ? parsePolicy(patch.attributeOrder)
                : base.attributeOrder();
        OrderPolicy ro = patch.recordOrder != null && !patch.recordOrder.isBlank()
                ? parsePolicy(patch.recordOrder)
                : base.recordOrder();
        boolean hdr = patch.expectedHasHeader != null ? patch.expectedHasHeader : base.expectedHasHeader();
        return new RecordsetMatchOptions(ao, ro, hdr);
    }

    private static OrderPolicy parsePolicyOrDefault(String raw, OrderPolicy defaultPolicy) {
        if (raw == null || raw.isBlank()) {
            return Objects.requireNonNull(defaultPolicy);
        }
        return parsePolicy(raw);
    }

    private static OrderPolicy parsePolicy(String raw) {
        String n = raw.trim().toUpperCase(Locale.ROOT);
        return switch (n) {
            case "STRICT" -> OrderPolicy.STRICT;
            case "FLEXIBLE" -> OrderPolicy.FLEXIBLE;
            default -> throw new IllegalArgumentException("Unknown order policy: " + raw
                    + " (use STRICT or FLEXIBLE)");
        };
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class RootOptionsFile {
        public String attributeOrder;
        public String recordOrder;
        public Boolean expectedHasHeader;
        public Map<String, PartialOptions> tasks;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class PartialOptions {
        public String attributeOrder;
        public String recordOrder;
        public Boolean expectedHasHeader;
    }

}
