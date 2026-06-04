package ru.icc.regtab.tasks;
import org.junit.jupiter.api.Test;
import ru.icc.regtab.itm.syntax.TableSyntax;
import java.nio.file.Path;
public class CsvCellDiagTest {
    @Test
    void showCellBytes() throws Exception {
        TableSyntax t = CsvTableLoader.load(Path.of("src/test/resources/tasks/task_099/input_1.csv"));
        String cell = t.getCell(0, 0).text();
        System.out.println("Cell len=" + cell.length());
        for (int i = 0; i < Math.min(20, cell.length()); i++) {
            System.out.printf("  [%d] = 0x%02X ('%c')%n", i, (int)cell.charAt(i), cell.charAt(i) < 32 ? '?' : cell.charAt(i));
        }
    }
}
