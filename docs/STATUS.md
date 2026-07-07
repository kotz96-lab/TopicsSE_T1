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
| 2    | A     | first full JUnit5 suite; PICT 2/3-wise tables; PIT baseline                  | not started |
| 2    | B     | first 5 metamorphic properties implemented under `metamorphic/`              | not started |
| 3    | A     | 4-wise comparison, mutation-score deltas, infra tests                        | not started |
| 3    | B     | full metamorphic suite, differential testing, draft refactoring proposals    | not started |
| 4    | both  | website, video, cleanup, final analysis                                      | not started |

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

### Person A — Week 2

1. Grow [`junitsuite/`](../src/test/java/se/topics/t1/junitsuite/) into
   a real test suite covering the BRICS public API. Target ≥80% line
   coverage on `dk.brics.automaton.*`.
2. Refine [`pict/models/regex.pict`](../pict/models/regex.pict) — write
   the parameter-selection rationale into `docs/pict-rationale.md`, add
   the constraints we care about, and generate 2-wise and 3-wise tables.
3. Write PICT-driven parameterized tests under
   [`pict/`](../src/test/java/se/topics/t1/pict/) that consume the
   generated tables via `@ParameterizedTest(name = ...)` +
   `@CsvFileSource`.
4. First PIT baseline: `./mvnw -Ppit test`, capture the JaCoCo + PIT
   scores in `docs/week2-baseline.md`.

### Person B — Week 2

1. Sketch ≥5 metamorphic properties as JUnit 5 tests under
   [`metamorphic/`](../src/test/java/se/topics/t1/metamorphic/). Use
   generators from `se.topics.t1.infra` (A will grow that package
   alongside).
2. First cut of differential tests vs `java.util.regex` in
   [`differential/`](../src/test/java/se/topics/t1/differential/).
3. Start collecting SOLID observations while reading BRICS source —
   drop notes into `docs/solid-notes.md` (create it).

Every AI-tool use → append to [`docs/AI_TOOLS.md`](AI_TOOLS.md). §21 asks
for evidence of what we asked, what we got, and what we fixed.

---

## Open questions / decisions to revisit

- Whether to enable branch protection on `main` (block force-push,
  require CI green on PRs). Right now nothing enforces the "no direct
  push to main" rule beyond social convention.
- Whether to add a `CODEOWNERS` file so PRs auto-tag the right partner.
  Small quality-of-life; not required.
- Whether to keep `docs/assignment.pdf` committed (currently yes) or
  gitignore it because the repo is public.
