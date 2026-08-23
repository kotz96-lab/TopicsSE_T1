# §9.4 — Interaction-strength × oracle-strength comparison

*The rubric-mandated per-strength comparison for §9.4, extended
into a 3×2 grid so we can separate the effect of interaction
strength from the effect of oracle strength. The final data set is
six PIT runs — three interaction strengths (2-wise, 3-wise, 4-wise)
crossed with two oracle strengths (weak, strong) — each isolated
via PIT's own `-DincludedGroups=` filter.*

> **Note (2026-08-23):** `pom.xml`'s PIT profile was since scoped to
> exclude `Datatypes.buildAll`/`main` and five helpers only
> `buildAll` calls — genuinely unreachable build-time code (see
> [`mutation-analysis.md`](mutation-analysis.md)). That drops PIT's
> total mutation count from 2031 to 1862 project-wide. The raw
> **kill counts** below are unaffected (those 169 mutations were
> `NO_COVERAGE` in every run, isolated or full-suite, so no test
> subset could ever have killed them) — but this doc's **percentages
> and "test strength" figures were not recomputed against the new
> denominator**, since test strength here depends on each isolated
> run's own no-coverage count, which we have not re-measured for all
> six runs. Treat the percentages/test-strength numbers below as the
> pre-fix baseline; the current full-suite headline number is **57%**
> (`mutation-analysis.md`), up from the 51% referenced here.

---

## Experimental setup

For each of the six combinations we ran:

```pwsh
./mvnw -Ppit test -DincludedGroups=<tag>
```

where `<tag>` is one of `pict-2wise`, `pict-3wise`, `pict-4wise`
(weak oracle, in [`PictRegexTest`](../src/test/java/se/topics/t1/pict/PictRegexTest.java))
or `pict-strong-2wise`, `pict-strong-3wise`, `pict-strong-4wise`
(strong oracle, in [`PictMetamorphicTest`](../src/test/java/se/topics/t1/pict/PictMetamorphicTest.java)).

The filter isolates each interaction strength × oracle combination
to see its *individual* mutation-killing power, independent of the
other tests in the suite.

**Two oracles compared:**

- **Weak** (`PictRegexTest`): per PICT row, two assertions —
  (1) the pipeline compiles + runs without throwing, and
  (2) `A.complement().complement().run(input) == A.run(input)` on a
  single test input.
- **Strong** (`PictMetamorphicTest`): per PICT row, five
  metamorphic properties (double complement, union idempotence,
  intersection idempotence, minimize preserves language, determinize
  preserves language), each asserted on **five sampled inputs**
  (empty, in-alphabet char × 3, out-of-alphabet char).

Both use the same 34 / 152 / 568 PICT rows at 2-wise / 3-wise /
4-wise from `pict/generated/*.csv`. Same JDK 21, same PIT 1.17.0
default config, same vendored BRICS commit `582d8f3`.

---

## Headline table — mutations killed

| Oracle | 2-wise (34 rows) | 3-wise (152 rows) | 4-wise (568 rows) |
|---|---|---|---|
| **Weak**   | **194** killed (9.6%)  | **205** killed (10.1%) | **204** killed (10.0%) |
| **Strong** | **209** killed (10.3%) | **229** killed (11.3%) | **231** killed (11.4%) |

Numbers are absolute kill counts out of 2031 mutations PIT produced
in each run. Percentages are `killed / total`.

### Test strength (kills among mutants the tests actually reach)

| Oracle | 2-wise | 3-wise | 4-wise |
|---|---|---|---|
| **Weak**   | 40.8% | 42.5% | 42.1% |
| **Strong** | 43.5% | 47.6% | 48.0% |

Test strength strips out the ~1550 mutants that live in code these
filtered suites never reach (mostly `Datatypes.buildAll` and other
BRICS internals not exercised by our regex-construction pipeline).
It's the sharper signal for comparing oracle behaviour on the code
we do reach.

### Absolute scores look small because these runs are isolated

Every score is <12%. That's expected — we're deliberately isolating
each row of the grid, so each run consists of only 34–568 tests
against 2031 mutants scattered across 25 BRICS classes. About 76% of
mutations are `NO_COVERAGE` because the isolated PICT suite doesn't
touch classes like `Datatypes`, `AutomatonMatcher`, or
`StringUnionOperations`. The *baselines* for context are:
- Hand-written suite alone: 51.5% (from earlier measurements)
- Full 1945-test suite: ~51% (as reported in `week2-baseline.md`)

So these per-strength numbers are the *incremental contribution*
each strength + oracle combo would make **if it were the only
testing you did**.

---

## Direction 1 — effect of increasing interaction strength

For each oracle, holding oracle fixed and varying strength:

| Transition | Weak-oracle delta | Strong-oracle delta |
|---|---|---|
| 2-wise → 3-wise | **+11** kills | **+20** kills |
| 3-wise → 4-wise | **−1** kill (noise) | **+2** kills |

**Weak oracle:** essentially flat between 3-wise and 4-wise. Small
lift going 2 → 3; nothing detectable 3 → 4.

**Strong oracle:** real +20 lift 2 → 3, then almost nothing 3 → 4.
Classic diminishing-returns curve.

**Interpretation:** interaction strength alone gives a modest boost
up to 3-wise and then plateaus. This matches the CIT literature's
usual "3-wise catches most of what 4-wise catches, for a fraction
of the cost" empirical observation.

---

## Direction 2 — effect of stronger oracle

For each interaction strength, holding strength fixed and varying
oracle:

| Strength | Weak → Strong delta |
|---|---|
| 2-wise | **+15** kills (194 → 209) |
| 3-wise | **+24** kills (205 → 229) |
| 4-wise | **+27** kills (204 → 231) |

**The gap between weak and strong oracle widens as interaction
strength grows.** At 2-wise the strong oracle is only marginally
better; at 4-wise it's meaningfully better.

**Interpretation:** the two dimensions *compound*. More combinations
give the stronger oracle more scenarios in which to fire; more
oracle sensitivity gives the extra combinations more opportunity
to catch differences. Neither dimension alone is very valuable —
together they are.

---

## Cost side — is the strong oracle expensive?

Wall-clock time for each filtered PIT run (approximate):

| Oracle | 2-wise | 3-wise | 4-wise |
|---|---|---|---|
| **Weak**   | ~2 min | ~4 min | ~9 min |
| **Strong** | ~3 min | ~9 min | ~19 min |

The strong oracle costs roughly 2× the weak oracle at the same
interaction strength, because each PICT row does 5 metamorphic
properties × 5 inputs = 25 assertions instead of 2.

Absolute cost is still cheap: even strong 4-wise finishes in under
20 minutes on a single laptop.

---

## Direct answer to RQ2

The assignment's RQ2 asks: *"Does CIT improve mutation scores?"*

Our data supports a nuanced answer:

> **It depends on the oracle.** With a weak oracle that only
> observes that the pipeline runs and that double-negation
> round-trips on one input, adding more interaction rows doesn't
> measurably help (194 → 205 → 204). With a strong oracle that
> asserts five metamorphic invariants on five inputs per row,
> higher interaction strength genuinely helps up to 3-wise
> (209 → 229) and shows diminishing returns after (229 → 231).
> The two dimensions compound: the strong-vs-weak gap widens as
> interaction strength grows (+15 → +24 → +27 additional mutants
> killed).

This is a stronger empirical finding than either "CIT works" or
"CIT doesn't work" would be, because it isolates the two variables
independently and shows how they interact.

---

## Practical recommendations for a real project

Based on these numbers, for a library like BRICS with a comparable
input-space model:

1. **Use at least a 3-wise CIT model**, not 2-wise. The +20-mutant
   gain at strong oracle justifies the extra 118 test rows
   (~50 seconds of runtime).
2. **Don't invest in 4-wise** unless you have very sensitive
   oracles that can distinguish subtle mutants (we don't in this
   codebase — the returns are diminishing).
3. **Put oracle strength ahead of interaction strength.** A strong
   oracle at 3-wise (229 kills) beats a weak oracle at 4-wise
   (204 kills) — even though 4-wise has almost 4× the rows.
4. **Combine both.** The strong + 3-wise sweet spot kills 229
   mutants in ~9 minutes; a full-suite PIT run kills ~1074 in ~12
   minutes but requires 1945 tests to have been written.

---

## Reproducing every number in this document

```pwsh
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot"

# One combination at a time:
./mvnw -Ppit test -DincludedGroups=pict-2wise         # weak,   2-wise
./mvnw -Ppit test -DincludedGroups=pict-3wise         # weak,   3-wise
./mvnw -Ppit test -DincludedGroups=pict-4wise         # weak,   4-wise
./mvnw -Ppit test -DincludedGroups=pict-strong-2wise  # strong, 2-wise
./mvnw -Ppit test -DincludedGroups=pict-strong-3wise  # strong, 3-wise
./mvnw -Ppit test -DincludedGroups=pict-strong-4wise  # strong, 4-wise
```

Each run writes `target/pit-reports/mutations.xml` and
`target/pit-reports/index.html`. PIT's `-DincludedGroups` is
critical — the older `-Dgroups` flag is a surefire-only filter and
PIT silently ignores it, causing every run to include the full
1945-test suite. That mistake and its correction are documented in
[`AI_TOOLS.md`](AI_TOOLS.md).

## A methodological note

An earlier version of this comparison used `-Dgroups=` (a surefire
filter that PIT ignores) instead of `-DincludedGroups=` (PIT's own
filter). The consequence: every "per-strength" PIT run actually
included the full 1945-test suite, and the reported per-strength
scores were all near-identical full-suite scores with PIT's
run-to-run noise. When we verified the filter by inspecting the
`killingTest` field in the XML reports (per reviewer request), we
saw kills coming from every test class, not just the tagged one.

That earlier data has been discarded and this document is the
corrected version.
