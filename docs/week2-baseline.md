# Week 2 baseline — snapshot of measurements & state

*As of 2026-07-09, end of Person A's Week 2 work. Numbers here are the
frozen reference point for the §7 "test-suite evaluation" write-up
and the §9.4 "combinatorial testing evaluation" write-up.*

---

## What's in the suite right now

| Package                                | Files | Tests | Purpose                                     |
|----------------------------------------|-------|-------|---------------------------------------------|
| `se.topics.t1` (`SmokeTest`)           | 1     | 1     | classpath sanity                            |
| `se.topics.t1.junitsuite` (§6)         | 10    | 179   | hand + AI-written coverage of BRICS public API |
| `se.topics.t1.pict` (§9)               | 2     | 754   | PICT-generated parameterized regex tests    |
| `se.topics.t1.metamorphic` (§10)       | 0     | 0     | pending — Partner B                         |
| `se.topics.t1.differential` (§11)      | 0     | 0     | pending — Partner B                         |
| `se.topics.t1.infra` (§13)             | 0     | 0     | pending — Partner A (Week 3)                |
| **Total**                              | **13**| **934** |                                             |

All 934 pass on the vendored BRICS commit `582d8f3`.

## Coverage — `dk.brics.automaton.*` (JaCoCo)

| Metric      | Baseline | Notes |
|-------------|----------|-------|
| Line        | **84.2%** | 2814 / 3343 lines |
| Branch      | **70.8%** | 1210 / 1709 branches |
| Method      | **82.0%** | 305 / 372 methods |
| Class       | **96.2%** | 25 / 26 classes; only `DatatypesAutomatonProvider` (13-line adapter) at 0% |
| Instruction | **84.4%** | 13764 / 16294 |

**Report:** `target/site/jacoco/index.html` (regenerable via
`./mvnw verify`).

### Per-class line coverage — highlights

| Class | Line % | Note |
|---|---|---|
| MinimizationOperations & inner classes | 87–100% | strongest |
| MatchOnlyRunAutomaton | 96.8% | |
| ShuffleOperations$ShuffleConfiguration | 95.5% | |
| StringUnionOperations | 93.2% | |
| State | 92.5% | |
| BasicOperations | 91.5% | |
| RunAutomaton | 88.2% | |
| Transition | 88.0% | |
| Datatypes | 85.3% | mostly static-initializer paths; buildAll() not runnable — needs `src/Unicode.txt` we don't ship |
| SpecialOperations | 84.2% | |
| Automaton | 80.1% | |
| BasicAutomata | 78.1% | |
| RegExp | 77.6% | some AST kinds not built by our regex inputs |
| ShuffleOperations | 73.6% | |
| TransitionComparator | 46.9% | `t.to == null` branch not reachable via public API |
| DatatypesAutomatonProvider | 0% | 13-line adapter, unused by our tests |

## Mutation testing — PIT

### Overall baseline
- **Command:** `./mvnw -Ppit test`
- **PIT scope:** `targetClasses = dk.brics.automaton.*`, `targetTests = se.topics.t1.*`
- **Report:** `target/pit-reports/index.html`

### Two comparable runs

| Metric | 180 hand-tests only | 934 tests (hand + PICT) |
|---|---|---|
| Total mutations | 2087 | 2087 |
| Killed | 1075 | 1074 |
| **Mutation score** | **52%** | **51%** |
| Test strength (killed among covered) | 70% | 70% |
| Uncovered mutations | 556 | 554 |
| Wall time | ~60 s | ~740 s (~12 min) |

**Headline finding:** the PICT-driven tests **did not measurably move
the mutation score.** With our current oracles (smoke + double-negation
invariant), extra combinations don't add detection power because the
hand tests already cover the killable behaviours. This is the
empirical answer to RQ2 ("Does CIT improve mutation scores?") — and
it's a legitimate research finding, not a failure. See
[../stuff_for_report.md](../stuff_for_report.md) Finding #1 for the
narrative write-up.

### Per-class mutation score (highlights, both runs identical to ±1)

| Class | Kill % | Notes |
|---|---|---|
| MinimizationOperations$Partition | 86% | |
| MatchOnlyRunAutomaton | 78% | |
| MinimizationOperations | 69% | |
| RunAutomaton | 68% | |
| BasicOperations | 66% | |
| RegExp | 60% | |
| BasicAutomata | 56% | |
| Automaton | 54% | |
| SpecialOperations | 41% | many "operation ran, we didn't check the output" survivors |
| ShuffleOperations | 35% | |
| Transition | 33% | equality / compare edge cases |
| TransitionComparator | 11% | internal comparator not reachable from public API tests |
| Datatypes | 3.7% | expected — `buildAll()` needs `src/Unicode.txt` which isn't shipped |

### 5 surviving mutants pre-selected for §8 write-up

[`../MUTANTS_TO_ANALYZE.md`](../MUTANTS_TO_ANALYZE.md) at repo root —
one from each of BasicOperations, Automaton, BasicAutomata, RegExp,
Transition. Each has source snippet, mutator type, why it survived,
how to kill it, and an equivalent-mutant judgment. §8 write-up can
be done later without re-mining the PIT report.

## Combinatorial testing — PICT

- **Model:** [`../pict/models/regex.pict`](../pict/models/regex.pict) —
  7 parameters × 4 constraints.
- **Rationale:** [`pict-rationale.md`](pict-rationale.md).
- **Tables:** committed under [`../pict/generated/`](../pict/generated/):

| Strength | Rows generated | vs full cross-product (12,960) |
|---|---|---|
| 2-wise  | **34**  | 99.7% smaller |
| 3-wise  | **152** | 98.8% smaller |
| 4-wise  | **568** | 95.6% smaller |

- **PICT binary:** `tools/pict.exe` — Microsoft PICT v3.7.4 (207 KB).
- **Seed:** pinned `/r:1` so CSVs regenerate byte-identically.
- **Follow-up available (not run yet):** the assignment §9.4 also
  wants a *per-strength* mutation-score comparison (2-wise alone vs
  3-wise alone vs 4-wise alone). Given the overall finding above,
  each strength is expected to land near ~51%. Requires 3 more PIT
  runs (~36 min total) if we want the table with actual numbers
  rather than an extrapolation from the aggregate result.

## Rubric progress after Week 2

| §  | Deliverable                                                       | Pts | State |
|----|-------------------------------------------------------------------|-----|-------|
| 6  | Generated JUnit5 test suite                                       | 20  | ✅ done — 180 tests |
| 7  | Evaluate suite (coverage etc.)                                    | (with §6) | ✅ numbers captured here |
| 8  | Mutation testing & 5-mutant analysis                              | 15  | 🟡 baseline done; MUTANTS_TO_ANALYZE.md pre-drafted; write-up pending |
| 9  | PICT + interaction-strength evaluation                            | 25  | 🟡 model + tables + tests + rationale done; per-strength PIT comparison pending |
| 10 | Metamorphic tests                                                 | 15  | ⬜ Partner B — not started |
| 11 | Differential vs `java.util.regex`                                 | 5   | ⬜ Partner B — not started |
| 12 | Code-quality, SOLID, refactoring                                  | 10  | 🟡 3 refactoring candidates surfaced in stuff_for_report.md; Partner B writeup |
| 13 | Infrastructure validation                                         | 3   | ⬜ Person A — Week 3 |
| 14 | Automation                                                        | (integrated) | ✅ verify + PIT + generate-pict scripted |
| 15 | HTML website                                                      | 5   | ⬜ Partner B — Week 4 |
| 16 | Reproducibility artifact                                          | 5   | 🟡 CSVs + seed committed; still need full checklist Week 4 |
| 17 | Video presentation                                                | 5   | ⬜ Partner B — Week 4 |
| 21 | Threats to validity + AI-tools writeup                            | 2   | 🟡 AI_TOOLS.md is the running log; T2V pending |

## How to reproduce everything in this file

```pwsh
# Set JAVA_HOME to a Temurin 21 install
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot"

# JUnit5 + JaCoCo (~5 s, produces target/site/jacoco/index.html)
./mvnw verify

# PIT (~12 min with the full 934-test suite, produces target/pit-reports/index.html)
./mvnw -Ppit test

# Regenerate PICT tables (needs tools/pict.exe or pict on PATH; ~1 s)
pwsh scripts/generate-pict.ps1
```

All three commands are idempotent and can be run in any order.

## Handoff notes

For whoever picks this up next session (Person A or a fresh Claude):

- **Do not run PIT casually** — 12 minutes with the full suite. Only
  when the mutation number is actually going to change.
- The PICT tests deliberately use a weak oracle (smoke + double
  negation). Strengthening them by mixing in metamorphic properties
  would likely improve the mutation score, but overlaps with Partner
  B's §10 territory — coordinate before doing it.
- `Datatypes.buildAll()` is not runnable without a companion resource
  `src/Unicode.txt` that BRICS' upstream ships separately. If we ever
  want to test that code path, the fix is to vendor that file too —
  which counts as a "modification made" that §5 requires us to
  document.
- The `git status` at end-of-session should be clean-modified for
  files we edited; committed CSVs under `pict/generated/` are
  intentional (see §16 reproducibility).
