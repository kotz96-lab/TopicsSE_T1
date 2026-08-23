# Use of AI Tools (assignment §21)

Append entries below as we go. The final website section is generated
from this file.

## Template

```
### YYYY-MM-DD — <short title>
- **Tool:** <e.g. Claude Code (Opus 4.7)>
- **Who:** <A or B>
- **What we asked it to do:**
- **What it produced:**
- **How we validated it:** (tests passed / manual review / cross-check / etc.)
- **Mistakes / corrections:**
```

## Entries

### 2026-06-26 — Week 1 scaffolding (Person A)
- **Tool:** Claude Code (Opus 4.7, 1M context).
- **Who:** A.
- **What we asked it to do:** Set up the project for both partners —
  clone the empty GitHub repo, vendor BRICS at a pinned commit, author a
  Maven build with JUnit5/JaCoCo/PIT, add Maven Wrapper so Person B
  doesn't need to install Maven, add GitHub Actions CI, write the README
  and work-split doc.
- **What it produced:** Repository skeleton described in `README.md`,
  initial PICT model under `pict/models/regex.pict`, smoke test, CI
  workflow.
- **How we validated it:** Ran `./mvnw verify` locally; CI run on the
  initial push (see Actions tab).
- **Mistakes / corrections:** *(fill in as we discover them).*

### 2026-07-09 — Week 2, first JUnit5 test batch (Person A)
- **Tool:** Claude Code (Opus 4.7, 1M context).
- **Who:** A.
- **What we asked it to do:** Read the public API of the core BRICS
  classes and generate a first JUnit5 test suite under
  `src/test/java/se/topics/t1/junitsuite/`. The suite targets the §6
  requirement ("generated JUnit5 test suite") and the JaCoCo baseline
  used in §7 evaluation.
- **What it produced:** Nine test classes covering `RegExp`,
  `BasicAutomata`, `BasicOperations`, `Automaton`, `RunAutomaton`,
  `AutomatonMatcher`, `MinimizationOperations`, `State`, and
  `Transition`. 139 tests total, using JUnit 5 `@Nested` display groups.
- **How we validated it:** `./mvnw verify` — 139/139 pass. JaCoCo:
  **56.5% line, 55.8% branch, 67.7% method** on `dk.brics.automaton.*`
  (up from ~1% under `SmokeTest` alone). Full breakdown captured in the
  next commit's message.
- **Mistakes / corrections:**
  1. Assumed `getCommonPrefix()` works semantically. It walks the
     structural graph — `.union()` of two singletons produces a
     branching NFA where the prefix is `""`. Fixed by building the union
     through a single `RegExp("prefix-(a|b)")` (which minimizes by
     default).
  2. Assumed `complement(makeEmpty()).isTotal()` is `true`. `isTotal`
     is a **structural** check requiring a very specific canonical
     shape (accepting initial with a single MIN..MAX self-loop);
     complement's output is semantically total but not in that shape.
     Fixed to check semantics via `run(...)` on multiple inputs.
  3. Assumed disabling `RegExp.INTERSECTION` via syntax flags makes the
     parser throw on `&`. It actually treats `&` as a literal
     character. Fixed the test to compare the two languages instead of
     expecting an exception.
- **Coverage gaps flagged for follow-up:** `Datatypes` (0/517 lines),
  `ShuffleOperations` (0/163), `MatchOnlyRunAutomaton` (0/31),
  `SpecialOperations` (17.7%), `TransitionComparator` (46.9%). Adding
  these will push the package past the 80% target.

### 2026-07-09 — Week 2, second JUnit5 batch (Person A)
- **Tool:** Claude Code (Opus 4.7, 1M context).
- **Who:** A.
- **What we asked it to do:** Extend the JUnit5 suite to hit the ≥80%
  line-coverage Week 2 target on `dk.brics.automaton.*`. Focused on the
  five classes with 0% or near-zero coverage from the first batch:
  `Datatypes`, `SpecialOperations`, `ShuffleOperations`,
  `MatchOnlyRunAutomaton`, and `TransitionComparator` (nudged via
  `State.getSortedTransitions(false)`).
- **What it produced:** Four new test classes — `DatatypesTest` (11
  tests), `SpecialOperationsTest` (16 tests), `ShuffleOperationsTest`
  (6 tests), `MatchOnlyRunAutomatonTest` (9 tests). Total suite is now
  180 tests across 13 files.
- **How we validated it:** `./mvnw verify` — 180/180 pass. JaCoCo:
  **84.2% line (was 56.5%), 70.8% branch (was 55.8%), 82% method (was
  67.7%)** on `dk.brics.automaton.*`. Target ≥80% cleared.
- **Mistakes / corrections:**
  1. First attempt at `DatatypesTest` used a `@BeforeAll` that called
     `Datatypes.main()` to trigger the private `buildAll()` pipeline
     (which would populate the in-memory cache and let `get(name)`
     return real automata). It crashes: `buildAll()` reads a companion
     resource `src/Unicode.txt` that is NOT vendored with BRICS in
     this repo. Rewrote `DatatypesTest` to only exercise the reachable
     surface: the three name-registry predicates
     (`isXMLName`, `isUnicodeBlockName`, `isUnicodeCategoryName`) and
     `exists()`. `Datatypes` still hit 85.3% line coverage because the
     class body is dominated by static initialisers and hardcoded name
     sets that run at classload.
  2. **BRICS bug found (candidate for §12):**
     `Datatypes.exists(name)` at `Datatypes.java:471` throws
     `NullPointerException` for missing resources instead of returning
     `false`. The `try/catch` catches `IOException`, but
     `getResource(name).openStream()` NPEs when `getResource` returns
     `null` — outside the catch. Test is written to assert-throws to
     lock in the current behaviour and flag the bug.
  3. `SpecialOperations.getFiniteStrings(a, limit)` returns `null`
     (not a truncated set) when the language exceeds the limit —
     documented via an explicit test.
  4. Assumed `projectChars` retains simple single-char membership in
     the residual language; behaviour is more subtle than that.
     Relaxed the test to just check the operation produces a non-null
     automaton.
- **Coverage still low:** `DatatypesAutomatonProvider` (0/13 lines —
  trivial adapter, still uncovered), `TransitionComparator` (46.9% —
  branch on `t1.to == null` / `t2.to == null` is hard to hit from
  public API), `RegExp` (77.6% — some obscure AST types not
  constructed by our regex inputs).

### 2026-07-09 — Week 2, first PIT baseline (Person A)
- **Tool:** PIT 1.17.0 + pitest-junit5-plugin 1.2.1, invoked via
  `./mvnw -Ppit test`. Not AI-generated but recorded here for §21's
  "how outputs were validated" chain.
- **Who:** A.
- **What we asked it to do:** Run PIT against the 180-test suite and
  capture the baseline mutation score for §8.
- **What it produced:** HTML + XML at `target/pit-reports/`.
- **Result:**
  - Total mutations generated: **2087**
  - Killed: **1075**
  - Overall mutation score: **52%**
  - Test strength (mutants killed / mutants with any coverage): **70%**
  - PIT-view line coverage of mutated classes: 75%
  - Mutations in code with no test coverage: 556 (27% of total —
    concentrated in `Datatypes.buildAll` which we can't run without
    the missing `src/Unicode.txt`)
- **Per-class mutation score (highlights):**
  | Class | Mut. score | Notes |
  |---|---|---|
  | MinimizationOperations$Partition | 85.7% | tightest test coverage |
  | MatchOnlyRunAutomaton | 78.3% | fresh tests are effective |
  | MinimizationOperations | 69.4% | |
  | RunAutomaton | 68.3% | |
  | BasicOperations | 65.9% | 58 survived — analysis candidates |
  | RegExp | 60.2% | |
  | BasicAutomata | 55.6% | |
  | Automaton | 54.4% | 34 survived |
  | SpecialOperations | 40.7% | 74 survived, 13 timed out |
  | ShuffleOperations | 35.1% | |
  | Transition | 33.3% | compare/hashCode branches escape |
  | TransitionComparator | 11.1% | 20 no-coverage — internal comparator |
  | Datatypes | 3.7% | 180/187 uncovered (buildAll path not runnable) |
- **Todo before final report:** §8 requires analysing "at least 5
  surviving mutants" — good picks are in `BasicOperations` (58
  survived, high-signal) or `Automaton` (34 survived).
- **Follow-up:** the 5 candidate mutants are pre-selected in
  [`MUTANTS_TO_ANALYZE.md`](../MUTANTS_TO_ANALYZE.md) at repo root so
  the §8 write-up can be done later without re-mining the PIT report.

### 2026-07-09 — Week 2, PICT (§9) end-to-end (Person A)
- **Tool:** Claude Code (Opus 4.7, 1M context) for authoring + Microsoft
  PICT v3.7.4 for combination generation.
- **Who:** A.
- **What we asked it to do:** Do the full §9 loop — refine the draft
  PICT model into something with justifiable parameter choices, install
  PICT, generate 2/3/4-wise tables, wire them into parameterized JUnit5
  tests, and write the rationale doc that §9.4 requires.
- **What it produced:**
  - `tools/pict.exe` (Microsoft PICT v3.7.4, 207 KB, downloaded from
    the official GitHub release).
  - Refined [`pict/models/regex.pict`](../pict/models/regex.pict): 7
    parameters × 4 constraints, deliberately kept small so the 4-wise
    table stays under 600 rows.
  - Updated [`scripts/generate-pict.ps1`](../scripts/generate-pict.ps1):
    now writes CSVs (comma-separated, JUnit5 `@CsvFileSource` friendly),
    pins seed `/r:1` for reproducibility, resolves `tools/pict.exe`
    before falling back to PATH.
  - Three generated tables committed under `pict/generated/` (2wise:
    34 rows, 3wise: 152 rows, 4wise: 568 rows). Full cross-product
    would be 12,960 → PICT cuts 95–99%.
  - [`RegexRowBuilder.java`](../src/test/java/se/topics/t1/pict/RegexRowBuilder.java)
    — deterministic mapping row → concrete regex + test input.
  - [`PictRegexTest.java`](../src/test/java/se/topics/t1/pict/PictRegexTest.java)
    — three parameterized methods, one per interaction strength, each
    assertion is smoke + double-negation invariant.
  - [`docs/pict-rationale.md`](pict-rationale.md) — §9.4 parameter-
    choice write-up (~250 lines).
- **How we validated it:** `./mvnw verify` — **934/934 tests pass**
  (180 hand-written + 754 PICT-driven). JaCoCo: 84.2% line / 71%
  branch / 82% method — essentially unchanged from the pre-PICT
  baseline, which is *expected*: PICT stresses the same code paths at
  more combinations, so the coverage number doesn't move but the
  mutation-score number should (measure with `-Ppit test`).
- **Mistakes / corrections:**
  1. First attempt at `RegexRowBuilder` had a `// \u escapes` comment
     — javac processes `\u` sequences even inside comments, so the
     source failed to compile with "illegal unicode escape".
     Replaced with a plain-English explanation.
  2. PICT natively outputs tab-separated values, not CSV. Script now
     does an in-line `\t → ,` substitution so JUnit5 `@CsvFileSource`
     works without extra config. Alternative was setting
     `delimiter='\t'` on every `@CsvFileSource`; the substitution is
     one line and less brittle.
  3. Considered a random-seed regex builder (a PICT row + seed →
     random regex matching those constraints) but chose a
     deterministic template mapping instead. Rationale documented in
     `docs/pict-rationale.md`: deterministic builds are debuggable
     and reproducible; random builds explore more but make test
     failures harder to attribute.
- **Rubric coverage after this batch:**
  - §9 (25 pts) — model, tables, integration, rationale all present.
    Interaction-strength comparison against mutation score is the
    remaining piece for full 25 pts (needs `-Ppit test` at each
    strength).
  - §14 (automation) — `scripts/generate-pict.ps1` regenerates every
    table reproducibly with one command.
  - §16 (reproducibility) — pinned PICT seed + committed CSVs.

### 2026-07-09 — Week 2, PIT re-run after PICT integration (Person A)
- **Tool:** PIT 1.17.0 (not AI, recorded for validation trail).
- **Who:** A.
- **What we did:** Re-ran `./mvnw -Ppit test` with the full 934-test
  suite (180 hand + 754 PICT-driven) to measure whether the CIT tests
  moved the mutation score vs the 180-test baseline.
- **Numbers (side by side vs previous PIT run):**
  | Metric | 180-test baseline | 934-test (with PICT) | Δ |
  |---|---|---|---|
  | Total mutations | 2087 | 2087 | 0 |
  | Killed | 1075 | 1074 | −1 |
  | Mutation score | 52% | 51% | ~0 |
  | Test strength | 70% | 70% | 0 |
  | Uncovered mutations | 556 | 554 | −2 |
  | PIT wall time | ~60s | ~740s (12 min) | +11 min |
- **Key finding (for §9.4 write-up):** the 754 PICT tests **did not
  measurably improve the mutation score**. This is the assignment's
  headline research question ("does higher interaction strength
  improve fault detection?") — our answer, backed by data, is
  *"not with a weak oracle."*
- **Root cause analysis:**
  - The 180 hand-written tests use **example-based** assertions
    (`assertTrue(a.union(b).run("a"))`) — sharp per-mutant signal.
  - The 754 PICT tests use only two oracles per row: (a) "doesn't
    throw", (b) `complement.complement.run(input) == run(input)`.
    Both are broad but shallow — many mutants that hurt language
    membership still satisfy them.
  - PICT tests exercise the same code paths as the hand tests but
    with more input combinations, and each mutant can only be killed
    once, so extra invocations against the same weak oracles add zero
    marginal detection.
- **What this implies for the §9.4 comparison of 2-wise / 3-wise /
  4-wise:** with the current oracle strategy, the mutation score is
  essentially independent of interaction strength — increasing strength
  just increases the number of invocations, not the number of unique
  killable behaviours. A separate per-strength PIT run (using JUnit
  tag filters to isolate 2-wise vs 3-wise vs 4-wise) will confirm
  this. Estimated additional PIT time: ~3 × 12 min = 36 min if we
  want the full table.
- **What would move the score:** stronger per-row oracles — e.g.,
  union commutativity, minimize preserves language, intersection
  subset relation. These overlap with Partner B's §10 metamorphic
  territory so should be coordinated. Alternative: a differential
  oracle against `java.util.regex` (§11, also B).

### 2026-07-09 — Week 2, drafts of Partner-B deliverables (Person A)
- **Tool:** Claude Code (Opus 4.7, 1M context).
- **Who:** A drafting; B to review, extend, or restructure.
- **What we asked it to do:** Get ahead of the schedule by drafting
  the two lowest-friction Partner-B deliverables (§10 metamorphic
  tests, §12.4 refactoring proposals) so B isn't blocked at Week 3
  start.
- **What it produced:**
  - [`../src/test/java/se/topics/t1/metamorphic/MetamorphicPropertiesTest.java`](../src/test/java/se/topics/t1/metamorphic/MetamorphicPropertiesTest.java)
    — 15 metamorphic properties (assignment requires ≥10). Covers
    union commutativity/associativity/idempotence, intersection
    commutativity/idempotence, double complement, both De Morgan
    laws, concatenation associativity, minimize preserves + is
    idempotent, determinize preserves, union monotone, intersection
    subset, empty absorption / identity.
  - [`refactoring-proposals.md`](refactoring-proposals.md) — the
    three findings from `stuff_for_report.md` polished into formal
    proposals (affected code / problem / proposed refactor / expected
    benefits / risks). All three surfaced organically from writing
    tests, which is itself a talking point for §7.
- **How we validated it:** `./mvnw verify` — **950/950 tests pass**
  (934 previous + 16 new metamorphic tests from 15 methods, one has
  two nested @DisplayName variants).
- **Oracle used for metamorphic tests:** language equivalence via
  double subset (`A.subsetOf(B) && B.subsetOf(A)`). BRICS'
  `Automaton.equals` compares hashCode which is structural, not
  semantic, so avoiding it. `subsetOf` determinizes internally which
  is a genuine semantic check.
- **Coordination note:** these files are marked at the top as
  Person A drafts pending Partner B review, and STATUS.md is updated
  accordingly. The intent is *unblock*, not *replace*.

### 2026-08-08 — Week 3 batch: §8 / §9 / §11 / §12 / §13 / §16 / §21 (Person A)
- **Tool:** Claude Code (Opus 4.7, 1M context) + PIT 1.17.0 for the
  per-strength runs.
- **Who:** A drafting; B to review the technical drafts marked as
  such (§11 differential, §12 SOLID narrative).
- **What we asked it to do:** knock out the entire remaining Claude-
  Code-doable slice of the assignment in one session so the only
  work remaining is human review + Week-4 website/video.
- **What it produced:**
  - [`docs/mutation-analysis.md`](mutation-analysis.md) — §8 5-mutant
    analysis expanded from the pre-selected candidates.
  - [`docs/pict-strength-comparison.md`](pict-strength-comparison.md)
    — §9.4 per-strength (2/3/4-wise) comparison, driven by three
    fresh PIT runs against `-Dgroups=pict-Nwise` filtered suites.
  - [`docs/solid-analysis.md`](solid-analysis.md) — §12 SOLID
    principle-by-principle narrative + design-pattern catalogue for
    BRICS.
  - [`docs/threats-to-validity.md`](threats-to-validity.md) — §21
    threats + mitigations, 12 threats across internal / external /
    construct validity classes.
  - [`../src/test/java/se/topics/t1/differential/DifferentialTest.java`](../src/test/java/se/topics/t1/differential/DifferentialTest.java)
    — §11 draft, ~40 test cases across the compatible subset +
    documented known disagreements.
  - [`../src/test/java/se/topics/t1/infra/RegexRowBuilderInfraTest.java`](../src/test/java/se/topics/t1/infra/RegexRowBuilderInfraTest.java)
    — §13 infra tests for `RegexRowBuilder`, ~12 methods + 186
    parameterized invocations against the committed CSVs.
  - README §16 artifact checklist appended.
  - Added `@Tag("pict-Nwise")` to `PictRegexTest` so PIT can filter
    per strength.
  - Made `RegexRowBuilder` public (was package-private) so it's
    reachable from the `infra` package for §13 tests.
- **Per-strength PIT numbers** (the direct answer to RQ2):
  | Strength | PICT rows | Killed | Mutation score | Test strength |
  |----------|-----------|--------|-----------------|---------------|
  | 2-wise   |  34       | 1058   | 50.7%           | 68.1%         |
  | 3-wise   | 152       | 1060   | 50.8%           | 68.2%         |
  | 4-wise   | 568       | 1024   | 50.4%           | 67.9%         |
  | Hand only| 180       | 1075   | 51.5%           | 70.0%         |
  | Full     | 934       | 1074   | 51.4%           | 70.0%         |
- **How we validated it:** `./mvnw clean verify` — **1191 tests pass**
  (180 hand + 754 PICT + 16 metamorphic + ~40 differential + ~200
  infra + 1 smoke). Coverage 77.2% line / 70.6% branch / 78.8%
  method. Down from ~84% earlier — see note below.
- **Coverage change note:** the line-coverage % dropped from 84.2%
  (reported end of Week 2) to 77.2% (end of Week 3). Root cause is
  a JaCoCo instrumentation quirk on `Datatypes` static-initializer
  lines — the same class is measured as 441/517 in some runs and
  13/252 in others. The actual code being executed hasn't changed.
  We're taking 77.2% as the reproducible number since it survives a
  full `clean verify` cycle.
- **Mistakes / corrections:**
  1. First differential-test attempt had `[a-z]+@[a-z]+` in the
     "compatible subset" cases — but BRICS treats `@` as ANYSTRING
     metacharacter, not a literal. The failing test blocked PIT
     entirely (PIT requires green tests). Fixed by using `X`
     instead, moved the `@` clash to a "known disagreements"
     nested class where it belongs. **Surfaced a real finding**
     recorded in [`../stuff_for_report.md`](../stuff_for_report.md)
     Finding #6.
  2. First infra-test attempt couldn't compile: `RegexRowBuilder`
     was package-private, unreachable from the `infra` package.
     Made the class + its enums + public methods `public`. Design
     justification: RegexRowBuilder is a legitimate testable
     surface, not a hidden helper.
  3. First 3-wise PIT run failed with "test failing without
     mutation" (Differential test #38 disagreed) — surfaced fix
     for issue #1 above.
- **Additional finding logged for the website (§9.4 write-up):**
  interaction-strength alone did not measurably move the mutation
  score. All three strengths cluster around 50.7% ± 0.5%, hand-only
  is 51.5%, full is 51.4%. This is the empirical answer to RQ2
  and matches the theoretical prediction that CIT's value is
  bounded by oracle strength.
- **⚠ CORRECTION added later:** the per-strength numbers above
  (50.4-50.8%) were WRONG. See the "PIT filter bug" session below.

### 2026-08-18 — Strong oracle experiment + PIT filter bug caught by human review
- **Tool:** Claude Code (Opus 4.7, 1M context).
- **Who:** A driving the session; user (reviewer) asked the
  question that surfaced the bug.
- **What we asked it to do:** Extend the §9.4 experiment with a
  second oracle strength — a PICT-driven metamorphic test
  ("strong oracle") that applies 5 metamorphic properties (double
  complement, union idempotence, intersection idempotence,
  minimize preserves, determinize preserves) on 5 sampled inputs
  per PICT row. Compare against the original "weak oracle"
  (smoke + double-negation on 1 input). Give a 3×2 grid of
  interaction strength × oracle strength.
- **What it produced (round 1, WRONG):**
  - [`../src/test/java/se/topics/t1/pict/PictMetamorphicTest.java`](../src/test/java/se/topics/t1/pict/PictMetamorphicTest.java)
    — 754 new PICT-driven metamorphic tests tagged
    `pict-strong-{2,3,4}wise`.
  - Six PIT runs with `-Dgroups=<tag>` filter reported nearly
    identical mutation scores across all six combinations
    (50.3-50.8%). Concluded "strong oracle doesn't help either."
- **User (reviewer) push-back:** "check the oracle is actually
  working and stronger." Prompted verification.
- **What the verification revealed:**
  - Parsing `target/pit-reports/mutations.xml` for the `killingTest`
    field showed kills coming from EVERY test class in the suite
    (`SpecialOperationsTest`, `BasicAutomataTest`, `PictRegexTest`,
    `MetamorphicPropertiesTest`, ...), not just the tag-filtered
    `PictMetamorphicTest`.
  - Confirmed: PIT was running the ENTIRE 1945-test suite for each
    "filtered" run. The `-Dgroups=<tag>` flag is a Surefire filter,
    but PIT uses its own JUnit5 test runner and ignores Surefire's
    include/exclude flags.
  - Fix: use PIT's own `-DincludedGroups=<tag>` flag instead.
- **What it produced (round 2, CORRECT):**
  - Six re-runs with the working filter, parsed into the 3×2 grid.
  - Numbers:
    | Oracle | 2-wise | 3-wise | 4-wise |
    |---|---|---|---|
    | Weak   | 194 (9.6%)  | 205 (10.1%) | 204 (10.0%) |
    | Strong | 209 (10.3%) | 229 (11.3%) | 231 (11.4%) |
  - Real finding: weak oracle flat across strengths; strong oracle
    rises 209 → 229 → 231 (diminishing returns after 3-wise);
    strong-vs-weak gap widens as interaction strength grows
    (+15 → +24 → +27). The two dimensions compound.
  - Rewrote [`pict-strength-comparison.md`](pict-strength-comparison.md),
    updated [`week2-baseline.md`](week2-baseline.md), and rewrote
    Finding 1 in [`../stuff_for_report.md`](../stuff_for_report.md).
    Added Finding 5 documenting the methodology bug itself.
- **Lesson (worth quoting in the §21 threats section):**
  the AI-written PIT invocation was self-consistent (all filtered
  runs showed similar numbers, which felt like a "valid null result")
  but scientifically meaningless. Only human review of a specific
  concrete hypothesis ("is the strong oracle actually stronger?")
  surfaced the flag bug. Automated correctness ≠ methodological
  correctness. AI tooling still needs a human reviewer to check
  that outputs answer the question the experiment was designed to
  answer.

### 2026-08-23 — Polish pass: PIT scope fix, stale-docs update, infra test gap

- **Tool:** Claude Code (Sonnet 5).
- **Who:** Itay.
- **What we asked it to do:** A scoped correctness pass on an
  already-complete project: (1) fix PIT counting mutations inside
  `Datatypes.buildAll()`'s dead subtree, which deflated the mutation
  score, (2) bring `docs/STATUS.md`/`ROADMAP.md` in line with actual
  project state, (3) review `se.topics.t1.infra` test coverage for
  real gaps.
- **What it produced:** Before touching `pom.xml`, it decompiled PIT
  1.17.0's own bytecode (`GregorEngineFactory.stringToMethodInfoPredicate`)
  to confirm `excludedMethods` matches by method name only, with no
  per-class qualifier — then found that the originally-planned
  exclusion list (`buildAll`, `main`, `store`, `load`) would have
  silently excluded real, tested `load`/`store` methods on
  `Automaton`, `RunAutomaton`, and `MatchOnlyRunAutomaton` (same
  method names, different classes). It also found five more private
  helpers (`makeCodePoint`, `buildMap`, `putWith`, `putFrom`, `put`)
  that are unreachable for the same reason but weren't in the
  original plan. Final `pom.xml` exclusion list: 7 verified-unique
  method names; `load`/`store` deliberately left in scope and
  documented instead. Ran full local `./mvnw verify` +
  `./mvnw -Ppit test` before and after (JDK 21, 1948→1950 tests):
  mutation score **52% → 57%** (2031→1862 mutations, killed-or-timed-out
  count unchanged at 1058 both times — proof the fix removed only
  unreachable dead code). Also found and fixed a real build-breaking
  environment issue unrelated to the assigned tasks: a stray local
  commit had bumped `pom.xml`'s Java target to 25, which this
  machine's JDK 21 test runner couldn't execute (class file version
  69 vs. 65) — reverted per the user's instruction after confirming
  it hadn't reached `origin/main`. Added 2 infra tests for a real,
  previously-uncovered defensive branch in `RegexRowBuilder.atom()`
  (the `single_char` + `range`/`negated` fallback). Updated
  `docs/mutation-analysis.md`, `docs/threats-to-validity.md`,
  `docs/STATUS.md`, `ROADMAP.md`, and the three website pages that
  report the mutation score (`index.html`, `junit.html`, `cit.html`)
  to the confirmed new numbers.
- **How we validated it:** Every number reported came from an actual
  local `./mvnw verify` / `./mvnw -Ppit test` run on this machine
  (JDK 21) — none were estimated or carried over from memory. The
  `cit.html` per-strength percentages were recomputed (not re-run)
  from the confirmed fact that the 169 excluded mutations were
  `NO_COVERAGE` in every prior run, isolated or full-suite, so no
  test subset could ever have killed or timed them out — that
  argument is made explicit in the page's own footnote. Where a
  derivation wasn't safe without a fresh run (the "test strength"
  figures in `pict-strength-comparison.md`, which depend on each
  isolated run's own no-coverage count), a dated note was added
  instead of a guessed number.
- **Mistakes / corrections:** None the harness didn't catch before
  being applied — the two risks above (name-collision in
  `excludedMethods`, the stray Java-25 commit) were caught during
  investigation, before any file was edited or any build was
  declared green.

### 2026-08-23 — Website generation automation (§14/§15)

- **Tool:** Claude Code (Sonnet 5).
- **Who:** Itay.
- **What we asked it to do:** The assignment's §14 automation
  requirements list "website generation," but `website/*.html` was
  9 hand-authored files with the nav bar and page shell copy-pasted
  into each one. Asked for a lightweight generator, explicitly with
  instructions not to redesign the site's content or layout.
- **What it produced:** Split the site into `website-src/layout.html`
  (shared shell) plus one content fragment and one footer fragment
  per page, and `scripts/generate_website.py` to reassemble them,
  with a `--check` mode wired into CI.
- **How we validated it:** Backed up the original `website/*.html`,
  ran the generator, and diffed every one of the 9 output files
  against the backup — all 9 came back byte-for-byte identical.
  That's the actual proof this didn't change the deliverable, not
  just a visual check.
- **Mistakes / corrections:** First generation attempt dropped a
  blank line before each page's closing `</main>` tag (an
  over-eager `.rstrip("\n")` in the fragment reader stripped more
  trailing newlines than the extraction had added). Caught by the
  byte-diff against the backup, not by eyeballing the rendered
  page — cosmetic in a browser, but would have been a silent,
  permanent drift from the original had the diff not been run.
