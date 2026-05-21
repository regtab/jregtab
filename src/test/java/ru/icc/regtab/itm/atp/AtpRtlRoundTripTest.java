package ru.icc.regtab.itm.atp;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.icc.regtab.itm.atp.spec.TablePattern;
import ru.icc.regtab.itm.rtl.AtpToRtlSerializer;
import ru.icc.regtab.itm.rtl.RtlCompiler;
import ru.icc.regtab.itm.rtl.RtlTaskBase;
import ru.icc.regtab.itm.rtl.RtlTask01Test;
import ru.icc.regtab.itm.rtl.RtlTask02Test;
import ru.icc.regtab.itm.rtl.RtlTask03Test;
import ru.icc.regtab.itm.rtl.RtlTask04Test;
import ru.icc.regtab.itm.rtl.RtlTask05Test;
import ru.icc.regtab.itm.rtl.RtlTask06Test;
import ru.icc.regtab.itm.rtl.RtlTask07Test;
import ru.icc.regtab.itm.rtl.RtlTask08Test;
import ru.icc.regtab.itm.rtl.RtlTask09Test;
import ru.icc.regtab.itm.rtl.RtlTask10Test;
import ru.icc.regtab.itm.rtl.RtlTask11Test;
import ru.icc.regtab.itm.rtl.RtlTask12Test;
import ru.icc.regtab.itm.rtl.RtlTask13Test;
import ru.icc.regtab.itm.rtl.RtlTask14Test;
import ru.icc.regtab.itm.rtl.RtlTask15Test;
import ru.icc.regtab.itm.rtl.RtlTask16Test;
import ru.icc.regtab.itm.rtl.RtlTask17Test;
import ru.icc.regtab.itm.rtl.RtlTask18Test;
import ru.icc.regtab.itm.rtl.RtlTask19Test;
import ru.icc.regtab.itm.rtl.RtlTask20Test;
import ru.icc.regtab.itm.rtl.RtlTask21Test;
import ru.icc.regtab.itm.rtl.RtlTask22Test;
import ru.icc.regtab.itm.rtl.RtlTask23Test;
import ru.icc.regtab.itm.rtl.RtlTask24Test;
import ru.icc.regtab.itm.rtl.RtlTask25Test;
import ru.icc.regtab.itm.rtl.RtlTask26Test;
import ru.icc.regtab.itm.rtl.RtlTask27Test;
import ru.icc.regtab.itm.rtl.RtlTask28Test;
import ru.icc.regtab.itm.rtl.RtlTask29Test;
import ru.icc.regtab.itm.rtl.RtlTask30Test;
import ru.icc.regtab.itm.rtl.RtlTask31Test;
import ru.icc.regtab.itm.rtl.RtlTask32Test;
import ru.icc.regtab.itm.rtl.RtlTask33Test;
import ru.icc.regtab.itm.rtl.RtlTask34Test;
import ru.icc.regtab.itm.rtl.RtlTask35Test;
import ru.icc.regtab.itm.rtl.RtlTask36Test;
import ru.icc.regtab.itm.rtl.RtlTask37Test;
import ru.icc.regtab.itm.rtl.RtlTask38Test;
import ru.icc.regtab.itm.rtl.RtlTask39Test;
import ru.icc.regtab.itm.rtl.RtlTask40Test;
import ru.icc.regtab.itm.rtl.RtlTask41Test;
import ru.icc.regtab.itm.rtl.RtlTask42Test;
import ru.icc.regtab.itm.rtl.RtlTask43Test;
import ru.icc.regtab.itm.rtl.RtlTask44Test;
import ru.icc.regtab.itm.rtl.RtlTask45Test;
import ru.icc.regtab.itm.rtl.RtlTask46Test;
import ru.icc.regtab.itm.rtl.RtlTask47Test;
import ru.icc.regtab.itm.rtl.RtlTask48Test;
import ru.icc.regtab.itm.rtl.RtlTask49Test;
import ru.icc.regtab.itm.rtl.RtlTask50Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Cross-consistency and round-trip tests binding ATP and RTL task definitions.
 *
 * <p>{@link #atpEqualsRtl}: for each task, asserts that
 * {@code AtpTaskNN.buildPattern()} equals {@code RtlCompiler.compile(RtlTaskNN.buildRtl())}.
 * Any structural divergence between ATP and RTL definitions fails immediately.
 *
 * <p>{@link #roundTrip}: for each task, serializes the ATP pattern to RTL,
 * recompiles it, and asserts structural equality with the original.
 */
class AtpRtlRoundTripTest {

    // ---- ATP == RTL cross-comparison ----

    @ParameterizedTest(name = "task_{0}")
    @MethodSource("taskPairs")
    void atpEqualsRtl(TaskPair tp) {
        assertEquals(tp.atp(), tp.rtl(),
                "ATP and RTL patterns differ for task " + tp.name());
    }

    record TaskPair(String name, TablePattern atp, TablePattern rtl) {
        @Override public String toString() { return name; }
    }

    static Stream<TaskPair> taskPairs() {
        return Stream.of(
                pair("01", new AtpTask01Test(), new RtlTask01Test()),
                pair("02", new AtpTask02Test(), new RtlTask02Test()),
                pair("03", new AtpTask03Test(), new RtlTask03Test()),
                pair("04", new AtpTask04Test(), new RtlTask04Test()),
                pair("05", new AtpTask05Test(), new RtlTask05Test()),
                pair("06", new AtpTask06Test(), new RtlTask06Test()),
                pair("07", new AtpTask07Test(), new RtlTask07Test()),
                pair("08", new AtpTask08Test(), new RtlTask08Test()),
                pair("09", new AtpTask09Test(), new RtlTask09Test()),
                pair("10", new AtpTask10Test(), new RtlTask10Test()),
                pair("11", new AtpTask11Test(), new RtlTask11Test()),
                pair("12", new AtpTask12Test(), new RtlTask12Test()),
                pair("13", new AtpTask13Test(), new RtlTask13Test()),
                pair("14", new AtpTask14Test(), new RtlTask14Test()),
                pair("15", new AtpTask15Test(), new RtlTask15Test()),
                pair("16", new AtpTask16Test(), new RtlTask16Test()),
                pair("17", new AtpTask17Test(), new RtlTask17Test()),
                pair("18", new AtpTask18Test(), new RtlTask18Test()),
                pair("19", new AtpTask19Test(), new RtlTask19Test()),
                pair("20", new AtpTask20Test(), new RtlTask20Test()),
                pair("21", new AtpTask21Test(), new RtlTask21Test()),
                pair("22", new AtpTask22Test(), new RtlTask22Test()),
                pair("23", new AtpTask23Test(), new RtlTask23Test()),
                pair("24", new AtpTask24Test(), new RtlTask24Test()),
                pair("25", new AtpTask25Test(), new RtlTask25Test()),
                pair("26", new AtpTask26Test(), new RtlTask26Test()),
                pair("27", new AtpTask27Test(), new RtlTask27Test()),
                pair("28", new AtpTask28Test(), new RtlTask28Test()),
                pair("29", new AtpTask29Test(), new RtlTask29Test()),
                pair("30", new AtpTask30Test(), new RtlTask30Test()),
                pair("31", new AtpTask31Test(), new RtlTask31Test()),
                pair("32", new AtpTask32Test(), new RtlTask32Test()),
                pair("33", new AtpTask33Test(), new RtlTask33Test()),
                pair("34", new AtpTask34Test(), new RtlTask34Test()),
                pair("35", new AtpTask35Test(), new RtlTask35Test()),
                pair("36", new AtpTask36Test(), new RtlTask36Test()),
                pair("37", new AtpTask37Test(), new RtlTask37Test()),
                pair("38", new AtpTask38Test(), new RtlTask38Test()),
                pair("39", new AtpTask39Test(), new RtlTask39Test()),
                pair("40", new AtpTask40Test(), new RtlTask40Test()),
                pair("41", new AtpTask41Test(), new RtlTask41Test()),
                pair("42", new AtpTask42Test(), new RtlTask42Test()),
                pair("43", new AtpTask43Test(), new RtlTask43Test()),
                pair("44", new AtpTask44Test(), new RtlTask44Test()),
                pair("45", new AtpTask45Test(), new RtlTask45Test()),
                pair("46", new AtpTask46Test(), new RtlTask46Test()),
                pair("47", new AtpTask47Test(), new RtlTask47Test()),
                pair("48", new AtpTask48Test(), new RtlTask48Test()),
                pair("49", new AtpTask49Test(), new RtlTask49Test()),
                pair("50", new AtpTask50Test(), new RtlTask50Test())
        );
    }

    private static TaskPair pair(String name, AtpTaskBase atp, RtlTaskBase rtl) {
        return new TaskPair(name, atp.buildPattern(), rtl.buildPattern());
    }

    // ---- ATP round-trip ----

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
        return taskPairs().map(tp -> new TaskCase(tp.name(), tp.atp()));
    }
}
