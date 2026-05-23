# Provenance: Test Tasks

Test tasks come from two collections.

---

## Collection 1: Foofah (tasks 01–50)

Test data sourced from the [Foofah](https://github.com/umich-dbgroup/foofah) benchmark collection.

**Source:** [https://github.com/umich-dbgroup/foofah/tree/master/tests/data](https://github.com/umich-dbgroup/foofah/tree/master/tests/data)

**Structure:** 50 tasks × 5 variants = 250 test cases. Each variant has an input table (CSV) and optionally an expected recordset (CSV).

**Local conversion:** The original Foofah data (`.txt` in `tests/data`) was converted to CSV format with comma delimiter and stored in `foofah-csv-with-comma`. Each variant folder contains `TestingTable.csv`.

**Expected (ground truth):** `D:\YandexDisk\data\foofah-benchmarks\gt` — flat CSV files `{foofah_id}_{variant}.csv` (no header; header `$a_0`,`$a_1`,… is added when copying to `expected_Y.csv`).

### Task mapping: task_XX ↔ foofah_id

| task_id | foofah_id |
|---------|-----------|
| 01 | exp0_2 |
| 02 | exp0_3 |
| 03 | exp0_4 |
| 04 | exp0_5 |
| 05 | exp0_6 |
| 06 | exp0_7 |
| 07 | exp0_8 |
| 08 | exp0_10 |
| 09 | exp0_11 |
| 10 | exp0_12 |
| 11 | exp0_13 |
| 12 | exp0_15 |
| 13 | exp0_17 |
| 14 | exp0_18 |
| 15 | exp0_19 |
| 16 | exp0_22 |
| 17 | exp0_24 |
| 18 | exp0_25 |
| 19 | exp0_26 |
| 20 | exp0_27 |
| 21 | exp0_28 |
| 22 | exp0_29 |
| 23 | exp0_30 |
| 24 | exp0_33 |
| 25 | exp0_34 |
| 26 | exp0_36 |
| 27 | exp0_37 |
| 28 | exp0_40 |
| 29 | exp0_41 |
| 30 | exp0_43 |
| 31 | exp0_44 |
| 32 | exp0_45 |
| 33 | exp0_46 |
| 34 | exp0_47 |
| 35 | exp0_48 |
| 36 | exp0_49 |
| 37 | exp0_51 |
| 38 | exp0_agriculture |
| 39 | exp0_craigslist_data_wrangler |
| 40 | exp0_crime_data_wrangler |
| 41 | exp0_potters_wheel_divide |
| 42 | exp0_potters_wheel_fold |
| 43 | exp0_potters_wheel_fold_2 |
| 44 | exp0_potters_wheel_merge_split |
| 45 | exp0_potters_wheel_split_fold |
| 46 | exp0_potters_wheel_unfold |
| 47 | exp0_potters_wheel_unfold2 |
| 48 | exp0_proactive_wrangling_complex |
| 49 | exp0_proactive_wrangling_fold |
| 50 | exp0_reshape_table_structure_data_wrangler |

**Note:** `exp0_potters_wheel_fold` and `exp0_potters_wheel_fold_2` are distinct tasks. The latter uses variant folders `exp0_potters_wheel_fold_2_1` … `exp0_potters_wheel_fold_2_5`.

---

## Collection 2: RegTab (tasks 51–…)

Test data created as part of the jRegTab project to cover table patterns not present in Foofah.

**Source:** authored by the jRegTab team.

**Structure:** 5 variants per task. Each variant has an input table (CSV) and an expected recordset (CSV) with a header row.

### Task mapping: task_XX ↔ regtab_id

| task_id | regtab_id |
|---------|-----------|
| 51 | illus_exp_1 |
| 52 | illus_exp_2 |
