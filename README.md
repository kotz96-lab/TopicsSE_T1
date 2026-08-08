# TopicsSE T1 — Evaluating the BRICS Automata Library

Empirical evaluation of [dk.brics.automaton][upstream] for the *Topics in
Software Engineering* course, Assignment 1. This repo is the shared
working tree for **Partner A** and **Partner B**.

- Assignment brief: [`docs/assignment.pdf`](docs/assignment.pdf)
- **Current status, milestone tracker, and next steps: [`docs/STATUS.md`](docs/STATUS.md)** ← start here

[upstream]: https://github.com/cs-au-dk/dk.brics.automaton

## Quick start

```pwsh
# JDK 21 must be on PATH and JAVA_HOME must be set. Everything else
# is bootstrapped by the Maven Wrapper.
./mvnw verify             # compile + JUnit5 + JaCoCo coverage
./mvnw -Ppit test         # PIT mutation coverage
pwsh scripts/run-all.ps1  # both, with output paths printed
```

> **Path requirement (Windows).** Keep the working tree under an
> ASCII-only path. Java's classpath resolver fails to load the Maven
> Wrapper jar from directories containing non-ASCII characters (e.g.
> Hebrew folder names) and the build aborts with
> `ClassNotFoundException: MavenWrapperMain`.

Reports:

| Artifact         | Path                                  |
|------------------|---------------------------------------|
| JUnit results    | `target/surefire-reports/`            |
| Coverage (HTML)  | `target/site/jacoco/index.html`       |
| Mutation (HTML)  | `target/pit-reports/index.html`       |

## Environment (assignment §5)

| Item            | Value                                         |
|-----------------|-----------------------------------------------|
| BRICS commit    | see [`BRICS_COMMIT`](BRICS_COMMIT)            |
| BRICS license   | BSD-3-Clause — see [`THIRD_PARTY_NOTICES.txt`](THIRD_PARTY_NOTICES.txt) |
| Java            | Temurin 21 (release target: 21)               |
| Build           | Maven (via wrapper, no system install needed) |
| Tests           | JUnit Jupiter 5.11.3                          |
| Coverage        | JaCoCo 0.8.12                                 |
| Mutation        | PIT 1.17.0 + pitest-junit5-plugin 1.2.1       |
| Combinatorial   | Microsoft PICT (installed separately)         |
| OS (developed)  | Windows 11 + Ubuntu in CI                     |

## Modifications to BRICS

The 18 BRICS source files under `src/main/java/dk/brics/automaton/` are an
**unmodified** copy of the upstream tree at the commit recorded in
`BRICS_COMMIT`. Vendoring (rather than depending on Maven Central) is
deliberate: it lets PIT mutate every class without classpath gymnastics
and gives us a stable target for the refactoring analysis in §12. The
upstream Ant `build.xml` and the upstream `pom.xml` are not used; we own
the build config.

If the upstream needs to be refreshed:

```pwsh
git clone --depth 1 https://github.com/cs-au-dk/dk.brics.automaton.git .upstream/dk.brics.automaton
cp .upstream/dk.brics.automaton/src/dk/brics/automaton/*.java src/main/java/dk/brics/automaton/
git -C .upstream/dk.brics.automaton rev-parse HEAD > BRICS_COMMIT
```

## Repository layout

```
.
├── pom.xml                              # Maven build (Java 21, JUnit5, JaCoCo, PIT)
├── mvnw / mvnw.cmd / .mvn/              # Maven Wrapper (no local mvn needed)
├── BRICS_COMMIT                         # pinned upstream commit hash
├── THIRD_PARTY_NOTICES.txt              # BRICS BSD-3-Clause license
├── src/main/java/dk/brics/automaton/    # vendored BRICS sources (do not modify)
├── src/test/java/se/topics/t1/
│   ├── SmokeTest.java                   # build sanity check
│   ├── junitsuite/                      # hand- / AI-written unit tests (A)
│   ├── pict/                            # parameterized tests fed by PICT (A)
│   ├── metamorphic/                     # metamorphic properties (B)
│   ├── differential/                    # BRICS vs java.util.regex (B)
│   └── infra/                           # our generators/parsers + their tests (A)
├── pict/
│   ├── models/                          # .pict input-space models
│   └── generated/                       # 2/3/4-wise tables (committed)
├── scripts/                             # automation (PowerShell)
├── docs/                                # SOLID notes, refactoring proposals, etc.
├── website/                             # static HTML report (B)
└── .github/workflows/ci.yml             # GitHub Actions: build + PIT
```

## Work split

See [`docs/SPLIT.md`](docs/SPLIT.md). A owns infrastructure, JUnit5 suite,
PICT/CIT, PIT, infra validation, automation. B owns metamorphic tests,
differential tests, SOLID/design analysis, website, video, threats to
validity.

## Milestones (assignment §18)

| Week | Owner | Deliverables                                                                 |
|------|-------|------------------------------------------------------------------------------|
| 1    | A     | repo + build + CI + skeleton + baseline coverage + initial PICT model        |
| 1    | B     | initial metamorphic property list (text), initial SOLID-analysis sketch      |
| 2    | A     | first full JUnit5 suite; PICT 2/3-wise tables; PIT baseline                  |
| 2    | B     | first 5 metamorphic properties implemented under `metamorphic/`              |
| 3    | A     | 4-wise comparison, mutation-score deltas, infra tests                        |
| 3    | B     | full metamorphic suite, differential testing, draft refactoring proposals   |
| 4    | both  | website, video, cleanup, final analysis                                      |

For the current state see [`docs/STATUS.md`](docs/STATUS.md); for the
remaining work see [`ROADMAP.md`](ROADMAP.md).

## Reproducibility artifact checklist (§16)

The final artifact must contain every item below. Each links to
either the file or the command that produces it.

| §16 item                        | Where in this repo                                                    | How to regenerate                                                |
|---------------------------------|-----------------------------------------------------------------------|------------------------------------------------------------------|
| Source code (vendored BRICS)    | [`src/main/java/dk/brics/automaton/`](src/main/java/dk/brics/automaton/) | pinned by hash in [`BRICS_COMMIT`](BRICS_COMMIT)                 |
| Generated JUnit tests           | [`src/test/java/se/topics/t1/junitsuite/`](src/test/java/se/topics/t1/junitsuite/) | `./mvnw verify` (also runs them)                                 |
| Metamorphic tests               | [`src/test/java/se/topics/t1/metamorphic/`](src/test/java/se/topics/t1/metamorphic/) | `./mvnw verify`                                                  |
| PICT models                     | [`pict/models/regex.pict`](pict/models/regex.pict)                    | authored by hand                                                 |
| Generated 2/3/4-wise combinations | [`pict/generated/regex-{2,3,4}wise.csv`](pict/generated/)             | `pwsh scripts/generate-pict.ps1` (needs `tools/pict.exe`)        |
| Generated combinatorial tests   | [`src/test/java/se/topics/t1/pict/`](src/test/java/se/topics/t1/pict/) | `./mvnw verify`                                                  |
| PIT mutation reports            | `target/pit-reports/index.html`                                       | `./mvnw -Ppit test`                                              |
| Coverage reports (JaCoCo)       | `target/site/jacoco/index.html`                                       | `./mvnw verify`                                                  |
| Software-design analysis        | [`docs/refactoring-proposals.md`](docs/refactoring-proposals.md), [`stuff_for_report.md`](stuff_for_report.md) | authored — see also [`docs/mutation-analysis.md`](docs/mutation-analysis.md) |
| SOLID evaluation notes          | *(Partner B — pending)*                                               | authored                                                         |
| Refactoring proposals (≥3)      | [`docs/refactoring-proposals.md`](docs/refactoring-proposals.md)      | authored                                                         |
| Scripts                         | [`scripts/`](scripts/)                                                | committed                                                        |
| Website                         | *(Partner B — pending, Week 4)*                                       | authored                                                         |
| README                          | *this file*                                                           | committed                                                        |
| Threats to validity             | [`docs/threats-to-validity.md`](docs/threats-to-validity.md)          | authored                                                         |
| AI-tool usage log (§21)         | [`docs/AI_TOOLS.md`](docs/AI_TOOLS.md)                                | appended every session                                           |
| Frozen Week-2 measurements      | [`docs/week2-baseline.md`](docs/week2-baseline.md)                    | snapshot                                                         |
| §8 mutant analysis (5 required) | [`docs/mutation-analysis.md`](docs/mutation-analysis.md), [`MUTANTS_TO_ANALYZE.md`](MUTANTS_TO_ANALYZE.md) | authored                                                         |
| §9 PICT rationale               | [`docs/pict-rationale.md`](docs/pict-rationale.md)                    | authored                                                         |

**One-command reproduction** of every number reported in this repo:

```pwsh
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot"
./mvnw verify              # 950-test suite + JaCoCo (~10s)
./mvnw -Ppit test          # PIT mutation report (~12 min)
# CSVs under pict/generated/ are already committed; regenerate optionally:
pwsh scripts/generate-pict.ps1
```
