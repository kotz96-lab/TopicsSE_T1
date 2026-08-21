# Video script — TopicsSE T1 project presentation

*Target: 8–9 minutes total (middle of the 5–10 min window §17
requires). English-only slides. Voiceover only, no camera on
student. 17 slides. Follow the "Say" text under each slide.*

**Recording tips:**
- Read at natural conversational speed (~140 words/minute).
- Slight pauses at paragraph breaks.
- Don't rush the numbers — the graders are listening for the
  quantitative story.
- Screen-record the slides at 1920×1080. Any recorder works
  (see slides/README.md for options).

---

## Slide 1 — Title (25 sec)

**Say:**
> "Hi. This is a short walk-through of my empirical evaluation of
> the BRICS Java automata library, done as part of Topics in
> Software Engineering, Assignment 1. Over four weeks I built a
> test harness around the library, ran a controlled experiment on
> combinatorial interaction testing, and surfaced several real
> defects in the library. Around eight minutes total."

---

## Slide 2 — Two goals (35 sec)

**Say:**
> "BRICS is an academic Java library implementing finite automata
> and regular expressions — union, intersection, complement,
> minimization. About four thousand seven hundred lines across
> eighteen classes.
>
> The assignment has two goals. First, evaluate BRICS itself:
> coverage, mutation resistance, design quality. Second, and
> larger, evaluate whether combinatorial interaction testing —
> using Microsoft PICT — actually improves fault detection. That
> second question, RQ2, drives most of my experiment."

---

## Slide 3 — Repository structure (20 sec)

**Say:**
> "BRICS is vendored at a pinned commit. Our tests live in six
> packages: hand-written units, PICT-driven combinatorial,
> metamorphic properties, differential vs java-util-regex, and
> infra tests for our own harness."

---

## Slide 4 — Test suite + coverage (35 sec)

**Say:**
> "The final suite is one thousand nine hundred forty-eight tests,
> all passing, on the vendored BRICS commit. Coverage on the
> library is seventy-seven percent by line, seventy-one percent
> by branch. The one class not fully covered is Datatypes, whose
> build-all path needs a Unicode data file BRICS doesn't ship
> separately."

---

## Slide 5 — PIT mutation testing baseline (35 sec)

**Say:**
> "For mutation testing I use PIT. It injects two thousand eighty-
> seven small mutations into the BRICS code — each a deliberate
> break — and re-runs our tests against every mutant. If any test
> fails, that mutant is killed; if all pass, it survives. My suite
> kills fifty-one percent, and among mutants that our tests
> actually reach, the test strength is seventy percent. This is
> the baseline for the CIT experiment."

---

## Slide 6 — Test-count progression (40 sec)

**Say:**
> "This table shows how the mutation score changed as I added
> different kinds of tests. With just the hand-written suite —
> 180 tests — the score is already fifty-one and a half percent.
> Adding the PICT combinatorial tests with a weak oracle brings
> us to 934 tests, but the score doesn't move. Adding metamorphic,
> differential, and strong-oracle PICT tests brings the total to
> 1948, and the score actually slightly drops to fifty point seven
> percent — because the additional slow tests cause PIT to time
> out on more mutants, which are then classified separately from
> kills.
>
> The takeaway: extra test flavors overlap with what hand tests
> already cover, so they don't measurably add kills. That's the
> setup for the RQ2 experiment on the next few slides — isolating
> what CIT actually contributes."

---

## Slide 7 — 5 surviving mutants (35 sec)

**Say:**
> "Section eight requires I analyze at least five surviving
> mutants. I picked five and grouped them by why they survived.
> The three patterns are: mutations that only change internal
> state without affecting which strings the automaton accepts —
> our tests are blind to those. Mutations inside private helpers
> where our test inputs happen to not cover the exact case the
> mutation affects. And mutations that scramble toString's output
> — our tests just check the result isn't null. One of the five is
> truly equivalent; the other four could be killed by adding more
> targeted assertions."

---

## Slide 8 — PICT model (30 sec)

**Say:**
> "For the combinatorial testing itself, I use Microsoft PICT. My
> input-space model has seven parameters — alphabet, character-
> class kind, quantifier, alternation depth, nesting depth,
> operation, input length — plus four constraints to prune
> combinations that can't actually exist. The full per-parameter
> rationale is in docs/pict-rationale.md."

---

## Slide 9 — PICT tables at 2/3/4-wise (30 sec)

**Say:**
> "Testing every possible combination of those seven parameters
> would be almost thirteen thousand tests. PICT's clever trick is
> picking a smaller subset that still guarantees every pair — or
> triple, or quadruple — of parameter values appears together at
> least once. At two-wise, thirty-four rows. At three-wise, one
> hundred fifty-two. At four-wise, five hundred sixty-eight.
> Massive reductions from the full cross-product."

---

## Slide 10 — The 3×2 grid (60 sec — key slide, take your time)

**Say:**
> "This is the headline experiment. I ran PIT six times — three
> interaction strengths crossed with two checker strengths, each
> isolated so the numbers reflect only that combination.
>
> Read across the top row: with a weak checker — just 'did it
> compile and does double-negation round-trip on one input' — going
> from two-wise to three-wise gains eleven mutants. Three-wise to
> four-wise gains nothing. Flat.
>
> Bottom row: with a stricter checker — five mathematical
> properties on five inputs each — two-wise to three-wise gains
> twenty mutants, then plateaus at four-wise. Diminishing returns.
>
> Now read down the columns: the gap between weak and strict is
> fifteen at two-wise, twenty-four at three-wise, twenty-seven at
> four-wise. The gap widens as interaction strength grows —
> meaning the two dimensions compound rather than acting alone."

---

## Slide 11 — RQ2 answer, plain English (30 sec)

**Say:**
> "In plain English: more test inputs alone don't help — the
> checks are shallow, so more inputs just repeat shallow checks.
> Stricter checks alone don't help either — without varied inputs
> they only fire on a handful of cases. Combining them helps.
> Even then, going past three-wise adds almost nothing. The
> improvement is real but modest — about thirteen percent more
> mutants killed at four-wise compared to the weak checker."

---

## Slide 12 — Metamorphic testing (30 sec)

**Say:**
> "For correctness I implemented eighteen metamorphic properties
> — mathematical identities BRICS must respect regardless of
> which specific automaton you throw at them. Union commutativity,
> De Morgan, minimize preserves language, Kleene-star idempotence,
> a regex round-trip property, and so on. All eighteen hold across
> the tested automata — a positive result: BRICS' regular-language
> algebra is correctly implemented on the operations I exercised."

---

## Slide 13 — Differential vs java.util.regex (40 sec)

**Say:**
> "Section eleven asks for differential testing against Java's
> built-in regex library. The idea is simple: feed the same regex
> to both engines, compare their answers. On the compatible subset
> — literals, ranges, quantifiers, alternation — they agree on
> every test case.
>
> But I found five ordinary ASCII characters that BRICS treats as
> metacharacters by default: at-sign, hash, tilde, ampersand, and
> angle brackets. Java treats all five as literals. So the same
> regex source string produces different match decisions in the
> two engines. Someone porting a regex between the two gets
> silently wrong matching — no crash, no warning, just different
> results. This becomes refactoring proposal four on the next
> slide."

---

## Slide 14 — SOLID by principle (45 sec)

**Say:**
> "For section twelve, the design analysis. SOLID is five
> object-oriented design principles from Robert Martin. On BRICS:
>
> Single responsibility is partially violated. The Automaton class
> is a so-called god class — seventy-eight public methods doing
> multiple jobs at once: it's a data structure, a factory, a facade
> for every operation, a serializer, and a global-settings holder.
>
> Open-closed: BRICS is closed. Almost no interfaces exist, so
> adding a new operation means modifying existing files.
>
> Liskov substitution is vacuous — BRICS uses almost no
> inheritance. Interface segregation is vacuously satisfied.
>
> Dependency inversion is violated — every dependency is on a
> concrete class, not an abstraction. But that's defensible for
> a self-contained library where there's nothing to substitute in
> anyway."

---

## Slide 15 — Refactoring proposals (35 sec)

**Say:**
> "Four concrete refactoring proposals — all surfaced organically
> while writing tests, not from a code-review pass.
>
> One: fix a null-pointer bug in Datatypes.exists. The method is
> supposed to return false for missing resources but instead
> throws NPE.
>
> Two: clarify isTotal — the method's name reads semantic but the
> implementation is a structural shape check only.
>
> Three: replace getFiniteStrings' null-return with either an
> exception or an Optional — right now it silently returns null
> on overflow, causing surprise NPEs.
>
> Four: the biggest one — flip RegExp's default flags so those
> five metacharacters we found are literal by default. Users opt
> in to the extensions explicitly."

---

## Slide 16 — Website (15 sec)

**Say:**
> "The full report lives at website slash index dot html — nine
> pages, one per rubric area. Every number on the site regenerates
> from a clean clone with two Maven commands."

---

## Slide 17 — Conclusions (45 sec)

**Say:**
> "To summarize. RQ2 has an empirically-backed answer: combinatorial
> testing only pays off when the checker is strict enough to notice
> subtle differences, and even then more than 3-wise gives you
> almost nothing. The gain is real but modest — about thirteen
> percent more mutants killed.
>
> On the correctness side, all eighteen metamorphic properties
> hold — BRICS' algebra is correct.
>
> On the defect side, I found four real issues in BRICS: one bug,
> two API-clarity problems, and one interoperability landmine.
> All from writing tests, not from reading the code.
>
> Limitations: single BRICS commit, one strict-oracle definition
> tested, and one automation path unreachable without an extra
> data file. All documented in the threats-to-validity page.
>
> Thanks for watching. Source on GitHub."

---

## Pacing check

| Slide | Time | Running |
|-------|------|---------|
| 1     | 0:25 | 0:25    |
| 2     | 0:35 | 1:00    |
| 3     | 0:20 | 1:20    |
| 4     | 0:35 | 1:55    |
| 5     | 0:35 | 2:30    |
| 6     | 0:40 | 3:10    |
| 7     | 0:35 | 3:45    |
| 8     | 0:30 | 4:15    |
| 9     | 0:30 | 4:45    |
| 10    | 1:00 | 5:45    |
| 11    | 0:30 | 6:15    |
| 12    | 0:30 | 6:45    |
| 13    | 0:40 | 7:25    |
| 14    | 0:45 | 8:10    |
| 15    | 0:35 | 8:45    |
| 16    | 0:15 | 9:00    |
| 17    | 0:45 | **9:45** |

Target lands around **10 minutes** — right at the top of the 5-10
allowed range. Slide 7 is the longest (55s) because it walks
through three code examples; slide 10 is second-longest (1:00)
because it's the money slide with the 3×2 grid.

---

## Screenshots checklist (§17.3 requires all of these)

Take these before you start recording:

| Screenshot | Where to grab it | Which slide |
|---|---|---|
| Repository structure | VSCode file tree or GitHub root | Slide 3 |
| Generated JUnit tests | Any `junitsuite/*.java` open in editor | Slide 4 (optional) |
| PICT model | `pict/models/regex.pict` open in editor | Slide 8 |
| Generated combinations | `pict/generated/regex-2wise.csv` | Slide 9 (optional) |
| PIT report | `target/pit-reports/index.html` in browser | Slide 5 |
| Coverage report | `target/site/jacoco/index.html` in browser | Slide 4 (optional) |
| Metamorphic tests | `MetamorphicPropertiesTest.java` | Slide 12 (optional) |
| Differential-testing results | `DifferentialTest.java` | Slide 13 (optional) |
| Refactoring examples | `docs/refactoring-proposals.md` rendered | Slide 15 (optional) |
| HTML website | `website/index.html` in browser | Slide 16 |

---

## Recording checklist

- [ ] Slides in English (§17.1)
- [ ] Voiceover throughout (no gaps)
- [ ] No student on camera
- [ ] 5–10 minutes total (target 8:30–10:00)
- [ ] Exported as MP4
- [ ] File size under 500 MB
- [ ] Practice-read the script once before final take
