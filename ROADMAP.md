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
| **8**  | Mutation testing + 5-mutant analysis | 15 | ✅ | full analysis in [`docs/mutation-analysis.md`](docs/mutation-analysis.md) |
| **9**  | PICT + interaction-strength eval | 25 | ✅ | model + tests + per-strength comparison + rationale — see [`docs/pict-strength-comparison.md`](docs/pict-strength-comparison.md) |
| **10** | Metamorphic tests (≥10) | 15 | 🟡 | 15 draft properties landed; **Partner B to review, extend, or restructure** |
| **11** | Differential vs `java.util.regex` | 5 | 🟡 | draft landed with 38 compatible cases + known-disagreements; **Partner B to review, extend** |
| **12** | SOLID + refactoring proposals | 10 | 🟡 | 3 refactoring proposals drafted + full SOLID-per-principle narrative in [`docs/solid-analysis.md`](docs/solid-analysis.md); **Partner B to review** |
| **13** | Infrastructure validation | 3 | ✅ | [`RegexRowBuilderInfraTest`](src/test/java/se/topics/t1/infra/RegexRowBuilderInfraTest.java) with 12 methods + 186 parameterized invocations |
| **14** | Automation | *(integrated)* | ✅ | mvnw verify + `-Ppit` + generate-pict scripted |
| **15** | HTML website | 5 | ⬜ | Week 4 wrap-up. Content exists in docs; assembly + styling ~4–6 hrs |
| **16** | Reproducibility artifact | 5 | ✅ | CSVs committed + seed pinned + README artifact-checklist appended |
| **17** | Video (5–10 min) | 5 | ⬜ | Week 4. B narrates. Needs slides + screenshots + voiceover, ~3–4 hrs |
| **21** | Threats to validity + AI-tools writeup | 2 | ✅ | AI_TOOLS.md complete; [`docs/threats-to-validity.md`](docs/threats-to-validity.md) written |

---

## Ranked by effort-to-points ratio (do these first)

### Cheap high-value moves — Person A, Week 3

| Rank | Item | § | Points | Effort | Notes |
|------|------|---|--------|--------|-------|
| 1 | Mutant write-up | §8 | **15** | ~30 min | Prose against pre-picked candidates in [MUTANTS_TO_ANALYZE.md](MUTANTS_TO_ANALYZE.md). Highest ROI thing on the board. |
| 2 | Per-strength PIT table | §9 | *(completes §9)* | ~60 min | 40 min PIT + 20 min write-up. Uses `@Tag` on `PictRegexTest` to filter 2/3/4-wise individually. |
| 3 | Threats-to-validity paragraph | §21 | 2 | ~15 min | Straightforward write-up. |
| 4 | Infra tests | §13 | 3 | ~30 min | ~5 tests for `RegexRowBuilder` + generate-pict.ps1 wrapper. |

**Update:** all four items above are now **done** — see the
"What's actually left" section below.

### Partner B's Week 3 (mostly review now)

Person A drafted the technical scaffolding for §10, §11, §12 to
unblock B. Each file is marked at the top as an A-draft pending B
review — intent is unblock, not replace.

| Item | § | Points | Effort |
|------|---|--------|--------|
| Review metamorphic tests + extend if desired | §10 | 15 | 1–2 hrs |
| Review differential tests + add cases if desired | §11 | 5 | 1 hr |
| Review SOLID narrative + edit voice/angle | §12 | 10 | 1–2 hrs |
| Get onboarded to the repo | — | — | 30 min |

**Subtotal for B's Week 3:** ~3–5 hrs (down from ~6–8 hrs before A
drafted the scaffolding).

### Wrap-up phase — Week 4

| Item | § | Points | Effort |
|------|---|--------|--------|
| HTML website | §15 | 5 | 4–6 hrs |
| Video (voiceover + slides) | §17 | 5 | 3–4 hrs |

**Subtotal for Week 4:** ~7–10 hrs combined.

---

## Total remaining hours

- **Person A:** ~1 hr helping with website/README (Week 4).
- **Person B:** ~3–5 hrs (Week 3 reviews) + ~7–10 hrs Week 4 (website + video).
- **Combined:** ~11–16 hrs to finish across two people over ~two weeks.

Nothing on Person A's plate is blocked. Person B's items are all
independent and can start as soon as they're onboarded.

## What's actually left (short list)

1. **Get Partner B onboarded** — the schedule risk, not a code risk.
2. **B reviews:** metamorphic properties, differential tests, SOLID + refactoring proposals.
3. **Week 4:** HTML website and video.

Everything else (§6, §7, §8, §9, §13, §14, §16, §21) is done end-to-end.

---

## The single most important thing

**Get Partner B onboarded.** Their absence is the schedule risk, not
any code. Once B has cloned the repo and read
[`docs/STATUS.md`](docs/STATUS.md) + [`docs/week2-baseline.md`](docs/week2-baseline.md),
about 2 weeks of parallel work finishes everything.

---

## Numbers we already have (after Week-3 additions)

- **1191 tests**, all passing on vendored BRICS commit `582d8f3`.
- **77.2%** line coverage / **70.6%** branch / **78.8%** method on
  `dk.brics.automaton.*` (JaCoCo).
- **51%** mutation score with the full suite. **Confirmed: 2-wise
  alone (34 tests) hits 50.7%; 3-wise (152 tests) 50.8%; 4-wise
  (568 tests) 50.4% — flat curve across interaction strengths, which
  is the empirical answer to RQ2** (see [`docs/pict-strength-comparison.md`](docs/pict-strength-comparison.md)).
- PICT tables at 2/3/4-wise: **34 / 152 / 568** rows (99.7% / 98.8% /
  95.6% smaller than the full 12,960-combination cross-product).
- **3 refactoring proposals** in [`docs/refactoring-proposals.md`](docs/refactoring-proposals.md).
- **5 surviving-mutant analysis** in [`docs/mutation-analysis.md`](docs/mutation-analysis.md).
- **SOLID narrative** in [`docs/solid-analysis.md`](docs/solid-analysis.md).
- **Threats to validity** in [`docs/threats-to-validity.md`](docs/threats-to-validity.md).
- **Differential tests vs `java.util.regex`** at [`src/test/java/se/topics/t1/differential/DifferentialTest.java`](src/test/java/se/topics/t1/differential/DifferentialTest.java).
- **15 metamorphic properties** at [`src/test/java/se/topics/t1/metamorphic/MetamorphicPropertiesTest.java`](src/test/java/se/topics/t1/metamorphic/MetamorphicPropertiesTest.java).
- **§13 infra tests** at [`src/test/java/se/topics/t1/infra/RegexRowBuilderInfraTest.java`](src/test/java/se/topics/t1/infra/RegexRowBuilderInfraTest.java).
- **6 interesting findings** in [`stuff_for_report.md`](stuff_for_report.md) — including the new "BRICS reserves 5 default metacharacters Java doesn't" finding from writing the differential test.
- All AI-tool sessions logged in [`docs/AI_TOOLS.md`](docs/AI_TOOLS.md).

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
