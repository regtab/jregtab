# Benchmark and evaluation

RegTab has been evaluated on three task collections totalling **150 tasks** and
**1 500 test variants (750 ATP + 750 RTL)**, all solved with 100 % accuracy. Each task
provides several source tables from the same class together with the corresponding target
recordsets; a single ATP pattern (and its RTL counterpart) must transform every variant
correctly.

| Collection | Tasks | Origin |
|---|---|---|
| Foofah | 001–050 | Established public benchmark (Jin et al., 2017) |
| RegTab | 051–110 | Original collection covering advanced RegTab features |
| Baikal | 111–150 | Tourism and environmental-monitoring tables (synthetic values) |

---

## Foofah benchmark (tasks 001–050)

A well-established collection of 50 tabular data transformation tasks assembled by Jin et al.
(2017) from real-world forums and related work (37 real-world cases, 13 synthetic). Each task
provides five source tables from the same class and five corresponding target recordsets.

The benchmark data (input and expected CSV files) is available at
<https://github.com/umich-dbgroup/foofah>.

---

## RegTab benchmark (tasks 051–110)

An original collection of 60 tasks designed to cover advanced RegTab features not present in the
Foofah benchmark: multi-level headers, cross-tabulations, conditional and delimited content,
grouped and tagged rows, and compound provider specifications.

All 110 tasks (001–110) are solved by ATP-based patterns and verified by a JUnit 5 test suite
(see [Testing](testing.md)). Automated comparison with ground truth confirms that all
**1 100 test variants (550 ATP + 550 RTL)** are transformed correctly (100 % accuracy).

---

## Baikal benchmark (tasks 111–150)

40 tasks based on tourism and environmental monitoring tables from the Lake Baikal region. The
tables do not contain authentic data: they were derived from original source tables by preserving
the full layout while replacing all numerical values with similar synthetic ones and applying
automatic translation to English.

- **Tasks 111–132** are derived from tables published in annual state reports on the ecological
  state of Lake Baikal and conservation measures (Russian Ministry of Natural Resources,
  2018–2022).
- **Tasks 133–150** are derived from tables provided by the Institute of Geography, Siberian
  Branch of the Russian Academy of Sciences.

Tasks 111–150 add **400 further test variants (200 ATP + 200 RTL)**, bringing the total to
**1 500 variants (750 ATP + 750 RTL)** across all 150 tasks.

!!! note "Scope of the paper"
    The accompanying paper formally reports the Foofah benchmark (50 tasks, 250 variants). The
    RegTab and Baikal collections extend the evaluation in the implementation and are exercised
    by the test suite shipped with jRegTab.
