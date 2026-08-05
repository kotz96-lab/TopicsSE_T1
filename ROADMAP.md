# Roadmap — what's left to the finish line

*Snapshot as of end of Week 2 (2026-07-09). This document is a living
plan; update as items complete. Frozen measurements from the same
moment live in [`docs/week2-baseline.md`](docs/week2-baseline.md).*

The assignment is scored out of **100 points** across 12 rubric
sections. Below is each section's state, what remains, and a rough
effort estimate.

---

## Section-by-section state

| §  | Deliverable | Pts | Done? | What remains |
|----|-------------|-----|-------|--------------|
| **6**  | JUnit5 suite | 20 | ✅ | write-up paragraph for §7 & website |
| **7**  | Suite evaluation | *(part of §6)* | ✅ | write-up in website |
| **8**  | Mutation testing + 5-mutant analysis | 15 | 🟡 | ~30-min write-up using [`MUTANTS_TO_ANALYZE.md`](MUTANTS_TO_ANALYZE.md) as source |
| **9**  | PICT + interaction-strength eval | 25 | 🟡 | **per-strength PIT comparison table** (2-wise vs 3-wise vs 4-wise, each alone). ~40 min PIT time + short write-up |
| **10** | Metamorphic tests (≥10) | 15 | 🟡 | 15 draft properties landed; **Partner B to review, extend, or restructure** |
| **11** | Differential vs `java.util.regex` | 5 | ⬜ | not started — B territory, ~1–2 hrs |
| **12** | SOLID + refactoring proposals | 10 | 🟡 | 3 refactoring proposals drafted; **SOLID-per-principle narrative still needed** (B) |
| **13** | Infrastructure validation | 3 | ⬜ | not started — A Week 3, ~30 min |
| **14** | Automation | *(integrated)* | ✅ | mvnw verify + `-Ppit` + generate-pict scripted |
| **15** | HTML website | 5 | ⬜ | Week 4 wrap-up. Content exists in docs; assembly + styling ~4–6 hrs |
| **16** | Reproducibility artifact | 5 | 🟡 | CSVs committed, seed pinned; README needs final artifact-checklist per §16 |
| **17** | Video (5–10 min) | 5 | ⬜ | Week 4. B narrates. Needs slides + screenshots + voiceover, ~3–4 hrs |
| **21** | Threats to validity + AI-tools writeup | 2 | 🟡 | AI_TOOLS.md complete; threats-to-validity paragraph missing, ~15 min |

---

## Ranked by effort-to-points ratio (do these first)

### Cheap high-value moves — Person A, Week 3

| Rank | Item | § | Points | Effort | Notes |
|------|------|---|--------|--------|-------|
| 1 | Mutant write-up | §8 | **15** | ~30 min | Prose against pre-picked candidates in [MUTANTS_TO_ANALYZE.md](MUTANTS_TO_ANALYZE.md). Highest ROI thing on the board. |
| 2 | Per-strength PIT table | §9 | *(completes §9)* | ~60 min | 40 min PIT + 20 min write-up. Uses `@Tag` on `PictRegexTest` to filter 2/3/4-wise individually. |
| 3 | Threats-to-validity paragraph | §21 | 2 | ~15 min | Straightforward write-up. |
| 4 | Infra tests | §13 | 3 | ~30 min | ~5 tests for `RegexRowBuilder` + generate-pict.ps1 wrapper. |

**Subtotal for A's Week 3:** ~2 hrs of work, closes out §8, §13, §21 and rounds out §9.

### Partner B's Week 3 (needs coordination)

| Rank | Item | § | Points | Effort | Notes |
|------|------|---|--------|--------|-------|
| 5 | Review + extend metamorphic tests | §10 | 15 | 2–3 hrs | B decides: accept the 15 drafted properties, extend to 20+, or restructure. Could grow into a PICT-driven metamorphic test that would also boost §9's mutation score. |
| 6 | Differential tests | §11 | 5 | 1–2 hrs | Build from scratch. Compatible-subset regex generator + `java.util.regex` comparison. |
| 7 | SOLID narrative | §12 | 10 | 2–3 hrs | Refactoring proposals done; needs SOLID-per-principle discussion (SRP violations, OCP-friendliness, LSP behavior, ISP inheritance chains, DIP concrete deps in BRICS). |

**Subtotal for B's Week 3:** ~6–8 hrs.

### Wrap-up phase — Week 4

| Rank | Item | § | Points | Effort | Notes |
|------|------|---|--------|--------|-------|
| 8 | HTML website | §15 | 5 | 4–6 hrs | All content exists in `docs/`. HTML/CSS assembly + navigation. |
| 9 | Video | §17 | 5 | 3–4 hrs | B narrates per SPLIT.md. Slides + screenshots + voiceover. |
| 10 | README artifact checklist | §16 | 5 | 30 min | Confirms every item required by §16 (source, tests, PICT models, reports, docs, README) has a link and reproduction command. |

**Subtotal for Week 4:** ~8–10 hrs combined.

---

## Total remaining hours

- **Person A:** ~2 hrs (Week 3) + ~1 hr helping with website/README (Week 4).
- **Person B:** ~6–8 hrs (Week 3) + ~7–10 hrs Week-4 wrap-up (website + video primarily).
- **Combined:** ~15–20 hrs to finish across two people over two weeks.

Nothing on Person A's plate is blocked. Person B's items are all
independent and can start as soon as they're onboarded.

---

## The single most important thing

**Get Partner B onboarded.** Their absence is the schedule risk, not
any code. Once B has cloned the repo and read
[`docs/STATUS.md`](docs/STATUS.md) + [`docs/week2-baseline.md`](docs/week2-baseline.md),
about 2 weeks of parallel work finishes everything.

---

## Numbers we already have (from Week 2 baseline)

- 950 tests, all passing on vendored BRICS commit `582d8f3`.
- **84.2%** line coverage / **70.8%** branch / **82%** method on
  `dk.brics.automaton.*` (JaCoCo).
- **51%** mutation score with the full suite (was 52% with the
  180-test hand-written suite alone — the 754 PICT tests did not
  measurably move the score with a weak oracle, which is itself the
  empirical answer to RQ2).
- PICT tables at 2/3/4-wise: **34 / 152 / 568** rows (99.7% / 98.8% /
  95.6% smaller than the full 12,960-combination cross-product).
- 3 refactoring proposals in [`docs/refactoring-proposals.md`](docs/refactoring-proposals.md).
- 5 pre-selected surviving mutants in [`MUTANTS_TO_ANALYZE.md`](MUTANTS_TO_ANALYZE.md).
- 4 interesting findings in [`stuff_for_report.md`](stuff_for_report.md).
- All 6 AI-tool sessions logged in [`docs/AI_TOOLS.md`](docs/AI_TOOLS.md).

---

## Reproduce every number in this file

```pwsh
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot"

# JUnit5 + JaCoCo (~5 s)
./mvnw verify
# → target/site/jacoco/index.html for coverage numbers

# PIT mutation testing (~12 min with full 934-test suite)
./mvnw -Ppit test
# → target/pit-reports/index.html for mutation score

# Regenerate PICT tables (needs pict.exe locally under tools/ or on PATH)
pwsh scripts/generate-pict.ps1
# → pict/generated/regex-{2,3,4}wise.csv (byte-identical thanks to /r:1)
```
