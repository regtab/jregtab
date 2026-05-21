package ru.icc.regtab.tasks;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Runs all task variants. Parameterized by (taskId, variantId).
 * 50 tasks × 5 variants = 250 test cases. Skips when Task class is not implemented.
 */
class TaskRunnerTest {

    private static final Path TASKS_ROOT = Path.of("src/test/resources/tasks");

    @ParameterizedTest(name = "task_{0} variant_{1}")
    @MethodSource("taskVariants")
    @DisplayName("Run task variant")
    void runVariant(String taskId, int variantId) throws IOException {
        TaskBase task;
        try {
            task = createTask(taskId);
        } catch (IllegalArgumentException e) {
            Assumptions.assumeTrue(false, "Task" + taskId + " not implemented");
            return;
        }
        task.runVariant(TASKS_ROOT, variantId);
    }

    private static Stream<Arguments> taskVariants() {
        return IntStream.rangeClosed(1, 50)
                .boxed()
                .flatMap(taskNum -> IntStream.rangeClosed(1, 5)
                        .mapToObj(v -> Arguments.of(taskId(taskNum), v)));
    }

    private static String taskId(int taskNum) {
        return taskNum < 10 ? "0" + taskNum : String.valueOf(taskNum);
    }

    private static TaskBase createTask(String taskId) {
        try {
            Class<?> clazz = Class.forName("ru.icc.regtab.tasks.Task" + taskId);
            return (TaskBase) clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Task not found: Task" + taskId, e);
        }
    }
}
