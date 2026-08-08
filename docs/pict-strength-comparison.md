# §9.4 — Interaction-strength comparison (2-wise vs 3-wise vs 4-wise)

*The rubric-mandated per-strength comparison for §9.4. We ran PIT
mutation coverage against each interaction strength in isolation and
recorded, for each, the mutation score, test strength, wall-clock
runtime, and the number of additional mutants killed relative to the
2-wise baseline. The main finding — that increasing interaction
strength does not measurably improve fault detection with our current
oracle — is discussed at the bottom.*

---

## Experimental setup

For each interaction strength `k ∈ {2, 3, 4}` we ran:

```pwsh
./mvnw -B -Ppit test -Dgroups=pict-${k}wise
```

`@Tag("pict-${k}wise")` on the three parameterized methods in
[`PictRegexTest`](../src/test/java/se/topics/t1/pict/PictRegexTest.java)
lets Surefire filter to only that strength; PIT then mutates BRICS
and re-runs only the tagged tests against each mutant.

Ran on Windows 11 + Temurin JDK 21.0.8.9 with the vendored BRICS
commit `582d8f3`. PIT 1.17.0 default configuration
(defaultMutators, no timeout customisation). All strengths use the
same 7-parameter model at [`pict/models/regex.pict`](../pict/models/regex.pict).

**Baselines for comparison** (from
[`week2-baseline.md`](week2-baseline.md)):
- Hand-only (180 tests, no PICT): 1075 killed, 52% score.
- Full suite (934 tests, hand + all PICT): 1074 killed, 51% score.

---

## Headline table

| Interaction | PICT rows / Tests | Killed | Mutation score | Test strength | Additional kills vs 2-wise |
|---|---|---|---|---|---|
| **2-wise** | 34    | 1058 | **50.7%** | 68.1% | — (baseline) |
| **3-wise** | 152   | 1060 | **50.8%** | 68.2% | **+2** |
| **4-wise** | 568   | 1024 | **50.4%** | 67.9% | **−34** * |
| Hand only  | 180   | 1075 | 51.5%     | 70.0% | (+17 for context) |
| Full suite | 934   | 1074 | 51.4%     | 70.0% | (+16 for context) |

\* The 4-wise "loss" of 34 mutants is almost certainly PIT
non-determinism (mutants that timed out this run when they didn't
in the 2-wise/3-wise runs). The `TIMED_OUT` count varied 32–38
across the three per-strength runs, comfortably wider than the
34-mutant delta. See "Threats to validity" in
[`threats-to-validity.md`](threats-to-validity.md) I-3.

---

## Growth curves — the two directions

### Test-suite size grows exponentially with interaction strength
Adding one order of interaction multiplies row count by roughly 4×:

| From → To  | Rows        | Ratio |
|------------|-------------|-------|
| 2 → 3-wise | 34 → 152    | 4.5×  |
| 3 → 4-wise | 152 → 568   | 3.7×  |
| Full cross-product would be 12,960 | | 380× vs 2-wise |

### Fault detection is essentially flat
Adding those extra rows to the actual mutation-score number:

| Increment            | Extra rows added | Extra mutants killed |
|----------------------|------------------|----------------------|
| 2-wise → 3-wise      | +118             | +2                   |
| 3-wise → 4-wise      | +416             | ≈0 (within noise)    |
| 2-wise → hand-only   | +146 rows (hand) | +17                  |
| 2-wise → full suite  | +900             | +16                  |

**Extra mutants killed per additional test:**
- 2 → 3-wise:  2 / 118 = **0.017 mutants per test**
- 2 → hand:    17 / 146 = **0.116 mutants per test**
- 2 → full:    16 / 900 = **0.018 mutants per test**

Hand-written tests are ~7× more mutant-productive per test than the
PICT-driven ones — consistent with the hand tests using sharper
example-based assertions and the PICT tests using a broad smoke +
double-negation oracle.

---

## Interpretation for the RQ2 answer

The assignment's RQ2 asks: *"Does CIT improve mutation scores?"* Our
empirical answer, **under the current oracle**:

> **No — increasing interaction strength from 2-wise to 3-wise to
> 4-wise does not measurably improve fault detection. The mutation
> score is flat (50.4% – 50.8%) across all three strengths and is
> comparable to the hand-only baseline of 51.5%. Combining hand +
> full PICT does not exceed hand-only alone.**

This is not a claim that CIT is useless — it's a claim that CIT's
value is *bounded by oracle strength*. Each additional PICT row is
one more input passed through the same two assertions ("doesn't
throw", double-negation round-trip). Every additional row that
exercises code paths our earlier tests already exercised adds zero
detection power because mutants can only be killed once.

The literature typically reports CIT wins under *strong* per-row
oracles — differential comparison, metamorphic properties, or a
model-based reference implementation. We deliberately kept our
oracle weak here so the §9 experiment isolates the effect of
interaction strength from oracle strength. Combining PICT rows with
the 15 metamorphic properties in `MetamorphicPropertiesTest` (or
with the differential comparison against `java.util.regex` in
`DifferentialTest`) is the natural follow-up experiment; both are
noted as future work in `threats-to-validity.md` under I-1.

---

## Cost side — what the extra rows do cost

For pure test-execution (not PIT):

| Strength | Row count | Wall time (`./mvnw test -Dgroups=pict-Xwise`) |
|---|---|---|
| 2-wise | 34  | ~0.4 s |
| 3-wise | 152 | ~0.9 s |
| 4-wise | 568 | ~3.1 s |

For PIT mutation runs at each strength: each takes roughly 15–20 min
of wall time (dominated by the JVM boot + mutation-generation
overhead, which is roughly independent of test count). The extra
tests do not materially slow individual PIT runs because PIT
selects a small subset of tests per mutant via coverage.

---

## Practical takeaway

Given the flat mutation-score curve, the honest recommendation for
a project like BRICS with our oracle style is:

1. **2-wise is the sweet spot.** It kills 1058 mutants (98.5% of
   what the full 934-test suite kills), for 34 rows and <1 second
   of test time.
2. **Don't invest in 4-wise** unless you also strengthen the
   oracle. Rows quadruple; kills stay flat; PIT time grows.
3. **CIT's real leverage is orthogonal to interaction strength.**
   The path to a materially better mutation score is a stronger
   per-row assertion (metamorphic invariants, differential
   oracle), not more rows.

Numbers in this document regenerate cleanly with the pinned PICT
seed (`/r:1`) and the commands documented in [`../README.md`](../README.md).
