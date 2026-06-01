package ru.icc.regtab.atp;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.icc.regtab.atp.spec.TablePattern;
import ru.icc.regtab.rtl.AtpToRtlSerializer;
import ru.icc.regtab.rtl.RtlCompiler;
import ru.icc.regtab.rtl.RtlTaskBase;
import ru.icc.regtab.rtl.RtlTask001Test;
import ru.icc.regtab.rtl.RtlTask002Test;
import ru.icc.regtab.rtl.RtlTask003Test;
import ru.icc.regtab.rtl.RtlTask004Test;
import ru.icc.regtab.rtl.RtlTask005Test;
import ru.icc.regtab.rtl.RtlTask006Test;
import ru.icc.regtab.rtl.RtlTask007Test;
import ru.icc.regtab.rtl.RtlTask008Test;
import ru.icc.regtab.rtl.RtlTask009Test;
import ru.icc.regtab.rtl.RtlTask010Test;
import ru.icc.regtab.rtl.RtlTask011Test;
import ru.icc.regtab.rtl.RtlTask012Test;
import ru.icc.regtab.rtl.RtlTask013Test;
import ru.icc.regtab.rtl.RtlTask014Test;
import ru.icc.regtab.rtl.RtlTask015Test;
import ru.icc.regtab.rtl.RtlTask016Test;
import ru.icc.regtab.rtl.RtlTask017Test;
import ru.icc.regtab.rtl.RtlTask018Test;
import ru.icc.regtab.rtl.RtlTask019Test;
import ru.icc.regtab.rtl.RtlTask020Test;
import ru.icc.regtab.rtl.RtlTask021Test;
import ru.icc.regtab.rtl.RtlTask022Test;
import ru.icc.regtab.rtl.RtlTask023Test;
import ru.icc.regtab.rtl.RtlTask024Test;
import ru.icc.regtab.rtl.RtlTask025Test;
import ru.icc.regtab.rtl.RtlTask026Test;
import ru.icc.regtab.rtl.RtlTask027Test;
import ru.icc.regtab.rtl.RtlTask028Test;
import ru.icc.regtab.rtl.RtlTask029Test;
import ru.icc.regtab.rtl.RtlTask030Test;
import ru.icc.regtab.rtl.RtlTask031Test;
import ru.icc.regtab.rtl.RtlTask032Test;
import ru.icc.regtab.rtl.RtlTask033Test;
import ru.icc.regtab.rtl.RtlTask034Test;
import ru.icc.regtab.rtl.RtlTask035Test;
import ru.icc.regtab.rtl.RtlTask036Test;
import ru.icc.regtab.rtl.RtlTask037Test;
import ru.icc.regtab.rtl.RtlTask038Test;
import ru.icc.regtab.rtl.RtlTask039Test;
import ru.icc.regtab.rtl.RtlTask040Test;
import ru.icc.regtab.rtl.RtlTask041Test;
import ru.icc.regtab.rtl.RtlTask042Test;
import ru.icc.regtab.rtl.RtlTask043Test;
import ru.icc.regtab.rtl.RtlTask044Test;
import ru.icc.regtab.rtl.RtlTask045Test;
import ru.icc.regtab.rtl.RtlTask046Test;
import ru.icc.regtab.rtl.RtlTask047Test;
import ru.icc.regtab.rtl.RtlTask048Test;
import ru.icc.regtab.rtl.RtlTask049Test;
import ru.icc.regtab.rtl.RtlTask050Test;

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
                pair("001", new AtpTask001Test(), new RtlTask001Test()),
                pair("002", new AtpTask002Test(), new RtlTask002Test()),
                pair("003", new AtpTask003Test(), new RtlTask003Test()),
                pair("004", new AtpTask004Test(), new RtlTask004Test()),
                pair("005", new AtpTask005Test(), new RtlTask005Test()),
                pair("006", new AtpTask006Test(), new RtlTask006Test()),
                pair("007", new AtpTask007Test(), new RtlTask007Test()),
                pair("008", new AtpTask008Test(), new RtlTask008Test()),
                pair("009", new AtpTask009Test(), new RtlTask009Test()),
                pair("010", new AtpTask010Test(), new RtlTask010Test()),
                pair("011", new AtpTask011Test(), new RtlTask011Test()),
                pair("012", new AtpTask012Test(), new RtlTask012Test()),
                pair("013", new AtpTask013Test(), new RtlTask013Test()),
                pair("014", new AtpTask014Test(), new RtlTask014Test()),
                pair("015", new AtpTask015Test(), new RtlTask015Test()),
                pair("016", new AtpTask016Test(), new RtlTask016Test()),
                pair("017", new AtpTask017Test(), new RtlTask017Test()),
                pair("018", new AtpTask018Test(), new RtlTask018Test()),
                pair("019", new AtpTask019Test(), new RtlTask019Test()),
                pair("020", new AtpTask020Test(), new RtlTask020Test()),
                pair("021", new AtpTask021Test(), new RtlTask021Test()),
                pair("022", new AtpTask022Test(), new RtlTask022Test()),
                pair("023", new AtpTask023Test(), new RtlTask023Test()),
                pair("024", new AtpTask024Test(), new RtlTask024Test()),
                pair("025", new AtpTask025Test(), new RtlTask025Test()),
                pair("026", new AtpTask026Test(), new RtlTask026Test()),
                pair("027", new AtpTask027Test(), new RtlTask027Test()),
                pair("028", new AtpTask028Test(), new RtlTask028Test()),
                pair("029", new AtpTask029Test(), new RtlTask029Test()),
                pair("030", new AtpTask030Test(), new RtlTask030Test()),
                pair("031", new AtpTask031Test(), new RtlTask031Test()),
                pair("032", new AtpTask032Test(), new RtlTask032Test()),
                pair("033", new AtpTask033Test(), new RtlTask033Test()),
                pair("034", new AtpTask034Test(), new RtlTask034Test()),
                pair("035", new AtpTask035Test(), new RtlTask035Test()),
                pair("036", new AtpTask036Test(), new RtlTask036Test()),
                pair("037", new AtpTask037Test(), new RtlTask037Test()),
                pair("038", new AtpTask038Test(), new RtlTask038Test()),
                pair("039", new AtpTask039Test(), new RtlTask039Test()),
                pair("040", new AtpTask040Test(), new RtlTask040Test()),
                pair("041", new AtpTask041Test(), new RtlTask041Test()),
                pair("042", new AtpTask042Test(), new RtlTask042Test()),
                pair("043", new AtpTask043Test(), new RtlTask043Test()),
                pair("044", new AtpTask044Test(), new RtlTask044Test()),
                pair("045", new AtpTask045Test(), new RtlTask045Test()),
                pair("046", new AtpTask046Test(), new RtlTask046Test()),
                pair("047", new AtpTask047Test(), new RtlTask047Test()),
                pair("048", new AtpTask048Test(), new RtlTask048Test()),
                pair("049", new AtpTask049Test(), new RtlTask049Test()),
                pair("050", new AtpTask050Test(), new RtlTask050Test())
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
