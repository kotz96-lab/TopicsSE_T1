# Threats to validity (assignment §21)

This document catalogues the threats to validity of our empirical
evaluation of BRICS. Each threat is classed as **internal**,
**external**, or **construct**, notes the *specific* concrete reason
it applies to *our* setup (not generic caveats), the impact if it
holds, and what we've done to mitigate it.

Terminology follows the standard software-engineering research
categorisation:
- **Internal validity** — is the causal chain from our tests to our
  reported numbers sound within this study?
- **External validity** — would our conclusions generalise beyond
  BRICS commit `582d8f3`, JDK 21, and Windows / Ubuntu?
- **Construct validity** — do the metrics we report actually measure
  what we claim they measure?

---

## Internal validity threats

### I-1. Weak oracle in PICT tests limits mutation-score signal

**What:** `PictRegexTest` uses only two oracles per row —
"doesn't throw" and `A.complement().complement().run(input) == A.run(input)`.
Many mutants that corrupt language membership still satisfy both.

**Evidence it's real:** we measured this directly. Adding 754 PICT
tests on top of 180 hand tests moved the mutation score from 52% to
51% (see [`week2-baseline.md`](week2-baseline.md)). That's a null
result on the "CIT increases fault detection" question — but only
under our particular oracle choice, not intrinsic to CIT.

**Mitigation:** the finding is reported honestly rather than hidden.
Stronger oracles (metamorphic properties per row, differential
comparison against `java.util.regex`) would likely raise the score;
future work bullet in §9.4 write-up. §10 already ships 15
metamorphic properties as candidates for oracle strengthening.

### I-2. Coverage-based test selection in PIT

**What:** PIT decides which tests to re-run per mutant by which lines
each test covers. If our coverage tracking is inaccurate or
compilation elides code paths, some mutants may be counted as
"no coverage" that actually are covered.

**Evidence:** 556 of 2087 mutations (27%) are reported as "no
coverage" in our baseline PIT run. This is concentrated in
`Datatypes.buildAll` (not runnable without `src/Unicode.txt`) — which
we've documented — but a small residual could be a coverage-detection
artifact.

**Mitigation:** we ran PIT with the default coverage tracking; JaCoCo
independently reports ≥80% line coverage on the same suite, so our
tests genuinely reach most code that PIT thinks they do. The
"no coverage" bucket is dominated by known-unreachable code, not
artifacts.

### I-3. Non-deterministic mutation-testing timing

**What:** PIT reports 1–14 mutants as `TIMED_OUT` per class per run.
Machine load, JIT warm-up, and OS scheduling can shift which mutants
time out — a mutant that times out today might get killed tomorrow.

**Evidence:** we saw 14 timeouts across `BasicOperations` in the
baseline run; total timeout count varies between runs.

**Mitigation:** we did not change PIT's default timeout thresholds
(which would just shift the same problem). Timeouts are ~1% of total
mutations, so their variance can move the aggregate mutation score
by at most ~1 percentage point run-to-run. The 52% → 51% delta in
our before/after CIT comparison is within that noise band, which
strengthens (not weakens) our null-result finding on CIT.

### I-4. `Datatypes.buildAll` cannot be exercised

**What:** `Datatypes.buildAll()` — invoked only from `Datatypes.main` —
reads a companion resource `src/Unicode.txt` that BRICS ships
separately and is not part of our vendored source. As a result 180
of 187 `Datatypes` mutations (~96%) are reported as `NO_COVERAGE` and
skew the aggregate mutation score down by ~9 percentage points.

**Mitigation:** documented as a scoped limitation.
`Datatypes.buildAll` is compile-time-only in the original library
(it pre-builds `.aut` files that would be shipped in the jar); the
runtime paths that consume those `.aut` files are also unreachable
here, so exercising them would require both `Unicode.txt` and the
`.aut` outputs. We could vendor `Unicode.txt` in a follow-up
(§5 requires we document any modification) but chose not to for
Week 2 to keep the vendored copy of BRICS byte-identical to upstream.

---

## External validity threats

### E-1. Single-version, single-commit case study

**What:** we tested BRICS at commit `582d8f3` only. Bugs we found
(the `Datatypes.exists` NPE, the `isTotal` structural quirk, the
`getFiniteStrings` null return) may not be present in earlier or
later commits.

**Mitigation:** the commit hash is pinned in `BRICS_COMMIT` and
`README.md`, so anyone reproducing can see exactly which version we
made claims about. All findings cite a specific line number in the
vendored source that ships with our repo.

### E-2. Test-suite results specific to `dk.brics.automaton.*` scope

**What:** our JaCoCo and PIT numbers are scoped to
`dk.brics.automaton.*` targetClasses. Conclusions about
BRICS-vs-other libraries don't follow because we didn't measure other
libraries.

**Mitigation:** we don't claim comparative conclusions. All
quantitative statements are prefixed with "for BRICS ...".

### E-3. Windows / Temurin JDK 21 development environment

**What:** all development happened on Windows 11 with Temurin JDK
21.0.8.9. PICT is a Windows binary. If BRICS' behaviour differs on
non-Windows or non-Temurin JVMs (locale-sensitive parsers, char
handling), we didn't detect it.

**Mitigation:** GitHub Actions CI runs the same suite on
Ubuntu-latest (JUnit + JaCoCo + PIT), so the JUnit and PIT numbers
we report have been reproduced on a Linux JVM. The `pict.exe` binary
is Windows-only; regenerating the CSVs on Linux would need building
PICT from source (`microsoft/pict` is portable C++). CSVs are
committed and reproducible via the pinned `/r:1` seed regardless of
platform.

### E-4. AI-authored tests may share systematic blind spots with AI-authored code

**What:** most of the JUnit suite, all of the PICT integration, and
the metamorphic property drafts were AI-authored by Claude Opus 4.7.
If Claude has a systematic weakness (e.g., forgetting Unicode edge
cases, always testing ASCII), that bias is baked into the whole
suite and would inflate our coverage/mutation numbers uniformly.

**Mitigation:** every AI session is logged in
[`AI_TOOLS.md`](AI_TOOLS.md) with the prompt intent, what was
produced, and — critically — the mistakes we discovered and fixed.
Four concrete mistakes are documented from the JUnit-batch sessions
alone (wrong assumption about `getCommonPrefix`, wrong assumption
about `isTotal`, wrong assumption about `RegExp` syntax flags, and
the `Datatypes.main` crash). Human review sits between every AI
draft and the committed code. The metamorphic and refactoring drafts
are explicitly marked as A-drafts pending B-review.

---

## Construct validity threats

### C-1. Line coverage overstates the strength of the suite

**What:** JaCoCo reports 84.2% line coverage. Line coverage counts
whether a line was executed, not whether the test asserted anything
meaningful about that line's behaviour. Our own PIT result
demonstrates the gap: at 84.2% line coverage the mutation score is
only 52%. A "high coverage" suite can still miss a lot of bugs.

**Mitigation:** we always report line coverage, branch coverage,
AND mutation score together — never line coverage alone. The §7
write-up will explicitly discuss the coverage-vs-mutation gap using
our numbers as evidence.

### C-2. Mutation score is not equivalence-mutant-adjusted

**What:** PIT reports raw kill percentage. Some fraction of the 456
surviving mutants are *equivalent mutants* — semantically identical
to the original code and therefore unkillable by any test. Our raw
mutation score therefore under-reports the *effective* score.

**Mitigation:** the §8 5-mutant analysis
([`MUTANTS_TO_ANALYZE.md`](../MUTANTS_TO_ANALYZE.md)) explicitly asks
"is this equivalent?" for each candidate — 4 of our 5 are non-
equivalent, 1 (the `RegExp.minimize` skip) is behaviourally
equivalent under the specific oracle "language membership only",
giving readers a concrete sense of the equivalence rate in this
codebase.

### C-3. Kill rate ≠ semantic fault detection

**What:** a killed mutant means a test noticed a *change*, not that
the test would have noticed a *real bug*. Real bugs are rarely
single-operator mutations; mutation score is a proxy for suite
strength, not a direct measure of bug-finding.

**Mitigation:** we ran diverse test flavours (example-based
JUnit, PICT-driven parameterized, metamorphic properties, coverage-
guided) — different flavours catch different fault classes. The
`Datatypes.exists` NPE we found was surfaced by *coverage-guided
test writing*, not by mutation. Reporting a mix of techniques
addresses the fact that no single metric is complete.

### C-4. PICT model design bias

**What:** the 7 parameters and 4 constraints in
[`pict/models/regex.pict`](../pict/models/regex.pict) reflect *our*
judgment about which BRICS input dimensions matter. Combinations
outside that model (surrogate pairs, control chars, extremely deep
nesting) are not tested by our PICT suite.

**Mitigation:** the model is committed and versioned; parameter
choices are individually justified in
[`docs/pict-rationale.md`](pict-rationale.md) with an explicit
"values NOT chosen" section per parameter, so reviewers can see
exactly what we excluded and why. Future work can extend the model
without changing our reported numbers.

---

## Summary — what would strengthen the study

If we had another two weeks and no scope constraint, we would:

1. Vendor `src/Unicode.txt` and re-run PIT with `Datatypes.buildAll`
   reachable — likely lifts aggregate mutation score by ~5–8 points.
2. Strengthen the PICT oracle by driving the metamorphic properties
   from PICT rows — likely lifts mutation score meaningfully and
   validates the "CIT works when oracle is strong" hypothesis.
3. Run the same suite against a second BRICS commit (or the upstream
   Maven Central release) to test consistency of findings.
4. Have a second reviewer independently rate each of the 456
   surviving mutants for equivalence, so we can report a corrected
   mutation score.

None of these are blockers for the current submission; all are
noted as future work in the §9.4 write-up.
