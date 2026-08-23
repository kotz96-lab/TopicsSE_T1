# Project status — living state document

This file is the "resume from here" document. Update it as work progresses
(especially at the end of a work session) so a fresh conversation with
Claude — or the other partner — can catch up without re-deriving context
from chat history.

**Assignment brief:** [`docs/assignment.pdf`](assignment.pdf) — Topics in
Software Engineering, Assignment 1. Four-week empirical study of the
BRICS Java automata library. 100 points total, weighted rubric in §20.

**Repo:** https://github.com/kotz96-lab/TopicsSE_T1
**Local working copy:** `C:\Users\kotz9\OneDrive\Desktop\school\current\TopicsSE_T1`
**Working directory MUST be ASCII** on Windows — see [§Environment](#environment).

---

## Roles

| | Who | Share | Owns |
|---|---|---|---|
| **A** | Shai (`kotz96-lab`, this working copy) | 65% | Repo, build, CI, JUnit5 suite, PICT/CIT (25 pts — biggest), PIT, infrastructure validation, automation scripts |
| **B** | *(to be invited)* | 35% | Metamorphic tests (§10), differential tests vs `java.util.regex` (§11), SOLID / design analysis / ≥3 refactoring proposals (§12), HTML website (§15), video (§17), threats to validity + AI-tools writeup (§21) |

Full split in [`docs/SPLIT.md`](SPLIT.md). Working agreement (PR-per-change,
no direct push to `main`, log AI-tool use in [`docs/AI_TOOLS.md`](AI_TOOLS.md))
is documented there.

---

## Milestone tracker

| Week | Owner | Deliverables                                                                 | Status |
|------|-------|------------------------------------------------------------------------------|--------|
| 1    | A     | repo + build + CI + skeleton + baseline coverage + initial PICT model        | **done** |
| 1    | B     | initial metamorphic property list (text), initial SOLID sketch               | pending |
| 2    | A     | JUnit5 suite (180 tests, 84% line cov); PICT 2/3/4-wise + tests; PIT baseline | **done** — see [`week2-baseline.md`](week2-baseline.md) |
| 2    | B     | first 5 metamorphic properties implemented under `metamorphic/`              | **Person A drafted 15 properties** — see [`refactoring-proposals.md`](refactoring-proposals.md) and [`../src/test/java/se/topics/t1/metamorphic/MetamorphicPropertiesTest.java`](../src/test/java/se/topics/t1/metamorphic/MetamorphicPropertiesTest.java). B to review/extend. |
| 3    | A     | per-strength PIT comparison; §8 5-mutant analysis; infra tests               | **done** — see [`pict-strength-comparison.md`](pict-strength-comparison.md), [`mutation-analysis.md`](mutation-analysis.md), [`../src/test/java/se/topics/t1/infra/`](../src/test/java/se/topics/t1/infra/) |
| 3    | A→B   | draft §11 differential + §12 SOLID narrative to unblock B                    | **A drafted, B to review** |
| 3    | A     | threats-to-validity; README §16 artifact checklist                           | **done** — see [`threats-to-validity.md`](threats-to-validity.md), [`../README.md`](../README.md) |
| 3    | B     | full metamorphic suite, differential testing, draft refactoring proposals    | **done** — 18 metamorphic properties, 6 differential tests, 4 refactoring proposals finalized |
| 4    | both  | website, video, cleanup, final analysis                                      | **website done** — 9/9 required sections in `website/`. **Video not yet recorded** — slides (`slides/index.html`) + full narration script (`docs/video-script.md`) are ready; recording + export is the one remaining human step (see [`slides/README.md`](../slides/README.md)) |

---

## Week 1 — done (Person A)

Verified: `./mvnw verify` and `./mvnw -Ppit test` both green locally
and in CI. See [latest run](https://github.com/kotz96-lab/TopicsSE_T1/actions).

### What was built

1. **Empty repo cloned** from `git@github.com:kotz96-lab/TopicsSE_T1.git`
   into an ASCII-only working directory. GitHub auth is configured for the
   `kotz96-lab` account (there is also a stale `aguyshayb` account in `gh`
   — switch with `gh auth switch --user kotz96-lab` if git push starts
   failing with 403).
2. **BRICS vendored** at commit `582d8f3` (recorded in
   [`BRICS_COMMIT`](../BRICS_COMMIT)) under
   `src/main/java/dk/brics/automaton/`. 18 files, unmodified.
   BSD-3-Clause preserved in
   [`THIRD_PARTY_NOTICES.txt`](../THIRD_PARTY_NOTICES.txt). Vendoring
   (rather than depending on Maven Central) is deliberate — it lets PIT
   mutate every BRICS class directly and gives §12 refactoring analysis a
   stable target.
3. **Maven build** ([`pom.xml`](../pom.xml)) on Java 21 release, with
   JUnit Jupiter 5.11.3, JaCoCo 0.8.12, and an opt-in `-Ppit` profile
   that runs PIT 1.17.0 with the JUnit 5 test plugin. PIT is scoped
   to `dk.brics.automaton.*` targetClasses and `se.topics.t1.*`
   targetTests, so mutation results reflect our tests against BRICS —
   nothing else.
4. **Maven Wrapper 3.3.2** committed so neither partner needs to install
   Maven. JDK 21 must be present with `JAVA_HOME` set.
5. **Test package skeleton** under `se.topics.t1.{junitsuite,pict,
   metamorphic,differential,infra}`. Each package has a
   `package-info.java` pinning ownership (A vs B) and the assignment
   section it satisfies. One `SmokeTest.java` at the root proves BRICS
   is loadable and a trivial regex parses+matches.
6. **PICT input-space model** at [`pict/models/regex.pict`](../pict/models/regex.pict):
   Alphabet × Quantifier × AlternationDepth × NestingDepth × RegexLength
   × Operation × InputLength, with one starter constraint. Justification
   is deferred to a Week 2 doc.
7. **Automation scripts** ([`scripts/`](../scripts/)):
   - `generate-pict.ps1` regenerates 2/3/4-wise tables for every
     `.pict` model into `pict/generated/` (requires `pict.exe` on PATH).
   - `run-all.ps1` runs `mvnw verify` then `mvnw -Ppit test` and prints
     report paths.
8. **GitHub Actions CI** ([`.github/workflows/ci.yml`](../.github/workflows/ci.yml)):
   `build` job compiles + runs JUnit 5 with JaCoCo + uploads the coverage
   report; `pit` job depends on `build`, runs `-Ppit test`, uploads the
   PIT HTML report. Both artifacts are downloadable from the Actions tab.
9. **Docs seeded**: [`README.md`](../README.md) covers §5 environment
   requirements and repository layout;
   [`docs/SPLIT.md`](SPLIT.md) records the 65/35 partner split and
   working agreement; [`docs/AI_TOOLS.md`](AI_TOOLS.md) is the running
   log for §21 evidence — **append to it every time we use an AI tool**.

### Bumps we hit and how we resolved them

- **Java can't load `mvnw` jar under non-ASCII paths.** Initial working
  copy was under a Hebrew directory (`הנדסה\שיעורי בית\תרגילים\…`) and
  `./mvnw` failed with `ClassNotFoundException: MavenWrapperMain`. We
  first added `scripts/mvn.cmd` / `scripts/mvn.ps1` that re-launched the
  wrapper from the Windows 8.3 short name, then moved the working copy
  to `C:\Users\kotz9\OneDrive\Desktop\school\current\TopicsSE_T1` and
  deleted the workaround. **Person B must also clone into an ASCII path**
  — README documents this.
- **`mvnw` lost its `+x` bit when committed from Windows.** CI failed in
  11s on the first push with `./mvnw: Permission denied`. Fixed with
  `git update-index --chmod=+x mvnw` (commit `178371b`). `.gitattributes`
  also pins the `mvnw` script to LF line endings so it stays exec'able
  on Linux checkouts.
- **`gh auth` intermittently swaps back to the wrong account.** We
  previously used `aguyshayb`; the repo lives under `kotz96-lab`. If a
  push fails with 403 `Permission denied to aguyshayb`, run
  `gh auth switch --user kotz96-lab && gh auth setup-git` and retry.

### Verified working

| Command                              | Outcome (local Windows + CI Ubuntu)       |
|--------------------------------------|-------------------------------------------|
| `./mvnw verify`                      | BUILD SUCCESS, SmokeTest passes, JaCoCo HTML at `target/site/jacoco/index.html` |
| `./mvnw -Ppit test`                  | BUILD SUCCESS, PIT scans 2031 mutations on `dk.brics.automaton.*`, HTML at `target/pit-reports/index.html` |
| GitHub Actions on push to `main`     | `build` (21s) + `pit` (56s) both green, `jacoco-report` and `pit-report` artifacts uploaded |

---

## Environment

| Item              | Value                                                             |
|-------------------|-------------------------------------------------------------------|
| JDK               | Temurin 21 (`JAVA_HOME` must be set)                              |
| Working path      | must be ASCII (Windows Java classpath bug otherwise)              |
| Build             | Maven (via committed wrapper — no system install needed)          |
| Test framework    | JUnit Jupiter 5.11.3                                              |
| Coverage          | JaCoCo 0.8.12                                                     |
| Mutation testing  | PIT 1.17.0 + pitest-junit5-plugin 1.2.1                           |
| Combinatorial     | Microsoft PICT (install separately, put `pict.exe` on PATH)       |
| OS (developed)    | Windows 11                                                        |
| OS (CI)           | Ubuntu latest (GitHub Actions)                                    |
| BRICS commit      | `582d8f3` — see [`BRICS_COMMIT`](../BRICS_COMMIT)                 |

---

## How to run things

```pwsh
# Compile + JUnit 5 + JaCoCo → target/site/jacoco/
./mvnw verify

# PIT mutation coverage → target/pit-reports/
./mvnw -Ppit test

# Both, with paths printed
pwsh scripts/run-all.ps1

# Regenerate PICT 2/3/4-wise tables (needs pict.exe on PATH)
pwsh scripts/generate-pict.ps1
```

---

## Where things live

- **BRICS code (do not modify):** `src/main/java/dk/brics/automaton/`
- **My (A) tests:** `src/test/java/se/topics/t1/{junitsuite,pict}/`
- **B's tests:** `src/test/java/se/topics/t1/{metamorphic,differential}/`
- **Shared generators/parsers:** `src/test/java/se/topics/t1/infra/`
  (owned by A; B should use these instead of rolling their own)
- **PICT models & tables:** `pict/models/`, `pict/generated/`
- **Automation scripts:** `scripts/`
- **Analysis docs (SOLID, refactoring, plots, notes):** `docs/`
- **B's HTML website (§15):** `website/`
- **Placeholder for downloaded tools like `pict.exe`:** `tools/`

---

## What's next

### Person A — Week 2 (**done**)

1. ~~Grow `junitsuite/` — target ≥80% line coverage.~~ Landed at 84.2%
   line / 70.8% branch / 82% method. 180 hand tests across 10 files.
2. ~~Refine `pict/models/regex.pict` + rationale doc + generate 2/3-wise
   tables.~~ Model has 7 params + 4 constraints. Rationale in
   [`pict-rationale.md`](pict-rationale.md). Tables generated at 2/3/4-wise
   (34/152/568 rows) under `pict/generated/`.
3. ~~PICT-driven parameterized tests via `@CsvFileSource`.~~
   Implemented as `PictRegexTest` + `RegexRowBuilder`. 754 tests.
4. ~~First PIT baseline.~~ Full numbers in [`week2-baseline.md`](week2-baseline.md).
   **Headline: mutation score 51% — PICT tests didn't move it vs the
   hand-test-only baseline (52%).** Written up as a legitimate research
   finding for §9.4 in [`../stuff_for_report.md`](../stuff_for_report.md).

### Person A — Week 3

1. §8 analysis of 5 surviving mutants — draft candidates already picked
   in [`../MUTANTS_TO_ANALYZE.md`](../MUTANTS_TO_ANALYZE.md); needs the
   discussion write-up (~30 min).
2. Per-interaction-strength PIT run: separate `-Ppit test` filtered by
   `@Tag` on `PictRegexTest` for 2/3/4-wise individually, to produce
   the strength-vs-mutation-score comparison table §9.4 asks for.
   (~40 min PIT time.)
3. Infrastructure validation tests under `se.topics.t1.infra` (§13).

### Person B — Week 2 (drafts landed from Person A — review & extend)

Person A got ahead of schedule and drafted the two lowest-friction
Partner-B deliverables. B should review and either accept, extend, or
rewrite from scratch — the intent is *unblock*, not *replace*:

- **§10 metamorphic tests** — 15 properties in
  [`../src/test/java/se/topics/t1/metamorphic/MetamorphicPropertiesTest.java`](../src/test/java/se/topics/t1/metamorphic/MetamorphicPropertiesTest.java).
  Assignment requires ≥10. Covers union / intersection / complement
  / concatenation / minimize / determinize identities. All passing on
  the vendored BRICS commit. Room to grow: extend into a
  parameterized version driven by PICT rows (which would give the
  PICT tests a stronger oracle and likely move the mutation score
  above 51%).
- **§12.4 refactoring proposals** — three formal proposals in
  [`refactoring-proposals.md`](refactoring-proposals.md). Assignment
  requires ≥3. All were surfaced organically while writing tests
  (not synthetic) — bug in `Datatypes.exists`, API-clarity issue in
  `isTotal`, null-return convention in `getFiniteStrings`.

### Person B — Week 2 (done)

All three items below landed and were finalized in later weeks — kept
here for history rather than as open TODOs:

1. ~~Sketch ≥5 metamorphic properties~~ — 18 properties in
   [`MetamorphicPropertiesTest.java`](../src/test/java/se/topics/t1/metamorphic/MetamorphicPropertiesTest.java)
   (requirement was ≥10).
2. ~~First cut of differential tests~~ — 6 tests in
   [`DifferentialTest.java`](../src/test/java/se/topics/t1/differential/DifferentialTest.java).
3. ~~SOLID observations~~ — full narrative in
   [`solid-analysis.md`](solid-analysis.md), not a separate notes file.

Every AI-tool use → append to [`docs/AI_TOOLS.md`](AI_TOOLS.md). §21 asks
for evidence of what we asked, what we got, and what we fixed.

---

## Current state (as of 2026-08-23 — this pass)

A polish/correctness pass found and fixed real issues beyond the
stale-docs problem this section used to describe:

- **Local build was broken** by an environment issue (a stray commit
  bumped `pom.xml`'s `maven.compiler.release` to 25, which this
  machine's JDK 21 test runner can't execute — class file version 69
  vs. the 65 the JRE supports). Reverted; the project targets **Java
  21** as originally documented. If `./mvnw verify` or `-Ppit test`
  fail with a `NoClassDefFoundError` avalanche or "class file version
  NN, this JRE only recognizes up to 65", check `JAVA_HOME` first.
- **Confirmed live numbers** (`./mvnw verify` + `./mvnw -Ppit test`,
  JDK 21, 2026-08-23): **1948 tests**, **73% instruction / 70% branch
  / 78% line / 79% method** coverage (JaCoCo), **2031 mutations, 1058
  killed → 52% mutation score, 529 no-coverage, 70% test strength**
  (PIT, before the `Datatypes` exclusion below). These are the real
  current numbers — treat the 84.2%/70.8%/82% coverage figures
  elsewhere in this file as the Week-2 snapshot they are, superseded
  by [`pict-strength-comparison.md`](pict-strength-comparison.md) and
  by this run.
- **PIT scope fixed**: `Datatypes.buildAll`/`main` and five private
  helpers only `buildAll` calls (`makeCodePoint`, `buildMap`,
  `putWith`, `putFrom`, `put`) are genuinely unreachable (need a
  `src/Unicode.txt` and `.aut` classpath resources this repo doesn't
  ship) and are now excluded from PIT via `<excludedMethods>` in
  `pom.xml`. `load`/`store` were deliberately **not** added to that
  list — PIT's `excludedMethods` matches by method name only, and
  those names collide with real, tested methods on `Automaton`,
  `RunAutomaton`, and `MatchOnlyRunAutomaton`. See
  [`mutation-analysis.md`](mutation-analysis.md) for the full
  before/after numbers and rationale.

---

## Open questions / decisions to revisit

- Whether to enable branch protection on `main` (block force-push,
  require CI green on PRs). Right now nothing enforces the "no direct
  push to main" rule beyond social convention.
- Whether to add a `CODEOWNERS` file so PRs auto-tag the right partner.
  Small quality-of-life; not required.
- Whether to keep `docs/assignment.pdf` committed (currently yes) or
  gitignore it because the repo is public.
