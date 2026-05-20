package ru.icc.regtab.itm.atp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.icc.regtab.itm.atp.spec.TablePattern;
import ru.icc.regtab.itm.rtl.AtpToRtlSerializer;
import ru.icc.regtab.itm.rtl.RtlCompiler;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Round-trip test: ATP pattern → RTL string → compile → compare.
 *
 * <p>For each of the 50 tasks: serializes the ATP pattern to RTL,
 * recompiles it, and asserts structural equality with the original.
 */
class AtpRtlRoundTripTest {

    @ParameterizedTest(name = "task_{0}")
    @MethodSource("taskCases")
    void roundTrip(TaskCase tc) {
        TablePattern original = tc.pattern();
        String rtl = AtpToRtlSerializer.serialize(original);
        TablePattern compiled = RtlCompiler.compile(rtl);
        assertEquals(original, compiled,
                "Round-trip failed for task " + tc.name() + "\nRTL: " + rtl);
    }

    record TaskCase(String name, TablePattern pattern) {
        @Override public String toString() { return name; }
    }

    static Stream<TaskCase> taskCases() {
        return Stream.of(
                task("01", new AtpTask01Test()),
                task("02", new AtpTask02Test()),
                task("03", new AtpTask03Test()),
                task("04", new AtpTask04Test()),
                task("05", new AtpTask05Test()),
                task("06", new AtpTask06Test()),
                task("07", new AtpTask07Test()),
                task("08", new AtpTask08Test()),
                task("09", new AtpTask09Test()),
                task("10", new AtpTask10Test()),
                task("11", new AtpTask11Test()),
                task("12", new AtpTask12Test()),
                task("13", new AtpTask13Test()),
                task("14", new AtpTask14Test()),
                task("15", new AtpTask15Test()),
                task("16", new AtpTask16Test()),
                task("17", new AtpTask17Test()),
                task("18", new AtpTask18Test()),
                task("19", new AtpTask19Test()),
                task("20", new AtpTask20Test()),
                task("21", new AtpTask21Test()),
                task("22", new AtpTask22Test()),
                task("23", new AtpTask23Test()),
                task("24", new AtpTask24Test()),
                task("25", new AtpTask25Test()),
                task("26", new AtpTask26Test()),
                task("27", new AtpTask27Test()),
                task("28", new AtpTask28Test()),
                task("29", new AtpTask29Test()),
                task("30", new AtpTask30Test()),
                task("31", new AtpTask31Test()),
                task("32", new AtpTask32Test()),
                task("33", new AtpTask33Test()),
                task("34", new AtpTask34Test()),
                task("35", new AtpTask35Test()),
                task("36", new AtpTask36Test()),
                task("37", new AtpTask37Test()),
                task("38", new AtpTask38Test()),
                task("39", new AtpTask39Test()),
                task("40", new AtpTask40Test()),
                task("41", new AtpTask41Test()),
                task("42", new AtpTask42Test()),
                task("43", new AtpTask43Test()),
                task("44", new AtpTask44Test()),
                task("45", new AtpTask45Test()),
                task("46", new AtpTask46Test()),
                task("47", new AtpTask47Test()),
                task("48", new AtpTask48Test()),
                task("49", new AtpTask49Test()),
                task("50", new AtpTask50Test())
        );
    }

    private static TaskCase task(String name, AtpTaskBase base) {
        return new TaskCase(name, base.buildPattern());
    }

    /** Verifies that RtlTask02 and AtpTask02 produce structurally equal TablePatterns. */
    @Test
    void rtlTask02EqualsAtpTask02() {
        String rtl = """
                { [ [VAL=NORM] [] ]{2}
                  [ [!BLANK ? VAL : (SC{2}, SR)->REC(2)] [VAL] ]+
                  [ [BLANK?] [] ]? }+
                """;
        assertEquals(new AtpTask02Test().buildPattern(), RtlCompiler.compile(rtl),
                "RtlTask02 and AtpTask02 patterns differ");
    }

    /** Verifies that RtlTask25 and AtpTask25 produce structurally equal TablePatterns. */
    @Test
    void rtlTask25EqualsAtpTask25() {
        String rtl = """
                [ [VAL : RT->SUFFIX('/'), (RT & C+2..)*->REC('/'), (BW & STR)*->CONCAT] [VAL]+ ]+
                """;
        assertEquals(new AtpTask25Test().buildPattern(), RtlCompiler.compile(rtl),
                "RtlTask25 and AtpTask25 patterns differ");
    }
}
