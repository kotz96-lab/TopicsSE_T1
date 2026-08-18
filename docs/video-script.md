# Video script — TopicsSE T1 project presentation

*Target: 7–8 minutes total (middle of the 5–10 min window §17
requires). English-only slides. Voiceover only, no camera on
student. ~15 slides, ~30 seconds each. Follow the layout &
narration exactly and pacing lands.*

**Recording tips:**
- Read at natural conversational speed (~140 words/minute).
- Slight pauses at paragraph breaks.
- Don't rush the numbers &mdash; the graders are listening for the
  quantitative story.
- Screen-record the slides at 1920×1080; use whatever slide tool
  you're comfortable with (Google Slides, PowerPoint, Keynote —
  they all export to MP4 or you screen-record).

---

## Slide 1 — Title (25 sec)

**On screen:**
```
Evaluating the BRICS Java Automata Library
TopicsSE T1 — Assignment 1
[Your name] · [Course · Semester]
```

**Say:**
> "Hi. This is a short walk-through of my empirical evaluation
> of the BRICS Java automata library, done as part of Topics in
> Software Engineering, Assignment 1. Over four weeks I built a
> rigorous test harness around the library, ran a controlled
> combinatorial-testing experiment, and surfaced several concrete
> defects and interoperability issues. I'll cover the results in
> about seven minutes."

---

## Slide 2 — What is BRICS + assignment goals (35 sec)

**On screen:**
- Left column: bullet list — "BRICS: 24-year-old Java library, finite automata + regular expressions, ~4700 lines, 18 classes"
- Right column: bullet list — the two main goals from §2:
  1. Evaluate BRICS' quality, correctness, and design
  2. Evaluate the effectiveness of combinatorial interaction testing

**Say:**
> "BRICS is an academic Java library from the University of
> Aarhus — it implements finite automata, regular expressions, and
> operations on them like union, intersection, complement, and
> minimization. About four thousand seven hundred lines across
> eighteen classes.
>
> The assignment has two goals. First, evaluate the library itself
> — coverage, mutation resistance, design quality. Second, and
> larger — evaluate whether combinatorial interaction testing,
> using Microsoft PICT, actually improves fault detection. That
> second question, RQ2, drives most of my experiment."

---

## Slide 3 — Repository structure screenshot (30 sec)

**On screen:**
- Screenshot of the repo's root layout in VSCode or GitHub (show
  `src/`, `pict/`, `docs/`, `scripts/`, `website/`, `pom.xml`,
  `README.md`).
- Overlay text or highlight box on: `src/main/java/dk/brics/automaton/`
  labeled "vendored BRICS (commit 582d8f3)".
- Overlay on `src/test/java/se/topics/t1/`
  labeled "our tests (six packages)".

**Say:**
> "BRICS is vendored into the repository at a pinned commit, so
> the surface being evaluated never drifts. Our tests live under
> six packages: junitsuite for hand-written unit tests, pict for
> combinatorial ones, metamorphic, differential, infra for
> testing our own harness, and a smoke package."

---

## Slide 4 — Test suite + coverage numbers (35 sec)

**On screen:**
- Big number box: "1,948 tests · all passing"
- Table of the coverage numbers:

| Metric | Value |
|---|---|
| Line coverage | 77.2% |
| Branch coverage | 70.6% |
| Method coverage | 78.8% |
| Class coverage | 96.2% |

- Small caption: "JaCoCo 0.8.12 · Temurin JDK 21 · full ./mvnw verify"

**Say:**
> "The final suite is one thousand nine hundred forty-eight tests,
> all passing, on the vendored BRICS commit. Coverage on the
> library itself is seventy-seven percent by line, seventy percent
> by branch. Every test is a real assertion — no crash-oracle
> shortcuts on the hand-written suite. The one class not fully
> covered is Datatypes, whose build-all path needs a Unicode data
> file BRICS doesn't ship separately."

---

## Slide 5 — PIT mutation testing screenshot (35 sec)

**On screen:**
- Screenshot of `target/pit-reports/index.html` showing the
  per-class mutation table (open the file in a browser, take a
  screenshot).
- Big overlay text: "Mutation score: 51% (1074 of 2087 killed)"

**Say:**
> "For mutation testing I use PIT. It automatically generates
> two thousand eighty-seven mutations — each a small deliberate
> break of the BRICS code — and re-runs the test suite against
> each one. If any test fails, that mutant is killed; if all pass,
> it survives. My suite kills fifty-one percent, and among mutants
> the tests actually reach, the test strength is seventy percent.
> This is the baseline for the CIT experiment."

---

## Slide 6 — Five surviving mutants analyzed (30 sec)

**On screen:**
- Three-pattern grouping from `docs/mutation-analysis.md`:
  1. **Behavioural-oracle blindness** — mutants that change
     internal state (determinism flag, skipped minimize) without
     changing language
  2. **Boundary blindness** — mutants near `Character.MAX_VALUE`
     that our ASCII test inputs miss
  3. **Output-content blindness** — mutants that scramble
     `toString` output; our tests only assert non-null

**Say:**
> "Assignment section eight requires analysis of at least five
> surviving mutants. I picked one from each of BasicOperations,
> Automaton, BasicAutomata, RegExp, and Transition. They cluster
> into three failure patterns: our tests are blind to changes
> that don't affect language membership, blind to boundary
> characters at the extremes of the Unicode range, and blind to
> the content of toString output. Only one of the five looks
> genuinely equivalent under our oracle — the others are killable
> with more focused assertions."

---

## Slide 7 — PICT model screenshot (30 sec)

**On screen:**
- Screenshot of `pict/models/regex.pict` in an editor showing
  the parameter block and the four constraints.
- Small overlay: "7 parameters × 4 constraints"

**Say:**
> "For combinatorial testing I use Microsoft PICT. My input-space
> model has seven parameters — alphabet, character-class kind,
> quantifier, alternation depth, nesting depth, operation, input
> length — with four constraints to prune structurally impossible
> combinations. Rationale for each parameter is in
> docs/pict-rationale.md."

---

## Slide 8 — PICT tables generated (25 sec)

**On screen:**
- Screenshot of the three generated CSVs, or table:

| Interaction strength | PICT rows | vs full cross-product (12,960) |
|---|---|---|
| 2-wise | 34 | 99.7% smaller |
| 3-wise | 152 | 98.8% smaller |
| 4-wise | 568 | 95.6% smaller |

**Say:**
> "PICT reduces twelve thousand nine hundred sixty possible
> combinations to just thirty-four rows at two-wise, one hundred
> fifty-two at three-wise, and five hundred sixty-eight at
> four-wise. Each row is deterministically translated into a
> concrete BRICS regex and test input by a builder class, then
> fed into two parameterized JUnit test classes."

---

## Slide 9 — The RQ2 experiment: the 3×2 grid (60 sec — key slide, take your time)

**On screen:**
- Big centered table (main visual of the whole video):

| Oracle | 2-wise | 3-wise | 4-wise |
|---|---|---|---|
| **Weak** (smoke + double-neg) | 194 | 205 | 204 |
| **Strong** (5 metamorphic props × 5 inputs) | 209 | 229 | 231 |

*(mutation kills per PIT run, isolated by interaction strength)*

**Say:**
> "This is the headline experiment. I ran PIT six times — three
> interaction strengths crossed with two oracle strengths, each
> isolated so the numbers reflect only that combination.
>
> Read across the top row: with a weak oracle, going from
> two-wise to three-wise gains eleven mutants, three-wise to
> four-wise gains nothing — essentially flat.
>
> Bottom row: with a strong oracle — five metamorphic properties
> per row on five sampled inputs — two-wise to three-wise gains
> twenty mutants, then plateaus.
>
> Now read down the columns: the gap between weak and strong is
> fifteen at two-wise, twenty-four at three-wise, twenty-seven at
> four-wise. **The gap widens as strength grows.** The two
> dimensions compound rather than acting independently."

---

## Slide 10 — Interpretation of the RQ2 result (30 sec)

**On screen:**
- Big quote box (accent color):
> "Does CIT improve mutation scores? **It depends on the
> oracle.** Interaction strength alone barely helps. Oracle
> strength alone helps modestly. Together they compound. And
> even the best combination shows diminishing returns past
> 3-wise."

**Say:**
> "The literature typically says higher interaction strength
> catches more bugs. My data says: not automatically. CIT's
> value is bounded by the oracle. With a weak oracle, extra
> combinations exercise the same code paths in the same shallow
> way — each mutant can only be killed once. With a strong
> oracle, the extra combinations get to fire more distinct
> assertions. The practical recommendation: use three-wise with a
> strong oracle. Anything beyond that shows diminishing returns."

---

## Slide 11 — Metamorphic testing (30 sec)

**On screen:**
- Header: "18 metamorphic properties (§10 requires ≥10)"
- Sample properties list (pick 5-6):
  - Union commutativity: L(A ∪ B) = L(B ∪ A)
  - Double complement: L(¬¬A) = L(A)
  - De Morgan: L(¬(A ∪ B)) = L(¬A ∩ ¬B)
  - Minimize preserves language
  - Kleene star idempotence: L((A*)*) = L(A*)
  - Regex round-trip: L(parse(pattern.toString())) = L(parse(pattern))

**Say:**
> "For correctness I have eighteen metamorphic properties —
> mathematical identities BRICS must respect regardless of input.
> Things like union commutativity, De Morgan, minimize preserves
> language, Kleene-star idempotence, and a regex round-trip
> property. All eighteen hold across the tested automata — a
> positive result: BRICS' algebra of regular languages is
> correctly implemented."

---

## Slide 12 — Differential testing + finding (35 sec)

**On screen:**
- Header: "Differential: BRICS vs java.util.regex"
- Callout box (red accent) titled "Finding":
  - "BRICS reserves **5 default metacharacters** that Java treats as literals"
  - Table:
    - `@` → BRICS ANYSTRING · Java literal
    - `#` → BRICS EMPTY · Java literal
    - `~` → BRICS complement · Java literal
    - `&` → BRICS intersection · Java literal
    - `<name>` → BRICS automaton ref · Java literals

**Say:**
> "Section eleven asks for differential testing against Java's
> built-in regex. I compare on the compatible subset — literals,
> ranges, quantifiers, alternation — and they agree everywhere.
> But I found five ordinary ASCII characters that BRICS treats as
> metacharacters by default: at-sign, hash, tilde, ampersand, and
> angle brackets. Java treats all five as literals. Someone porting
> a regex between the two engines gets silently wrong matching —
> no crash, no warning, just different accept-reject decisions."

---

## Slide 13 — SOLID + 4 refactoring proposals (40 sec)

**On screen:**
- Two-column layout:
  - Left: "SOLID verdict" — 5 principles with a color-coded verdict each:
    - S: Partially violated (Automaton is a god-class, 78 public methods)
    - O: Closed for extension (no interfaces to plug into)
    - L: Vacuous (no inheritance)
    - I: Vacuously satisfied
    - D: Violated but arguably acceptable for a library
  - Right: "4 refactoring proposals":
    1. Fix `Datatypes.exists()` NPE bug
    2. Clarify `isTotal()` (semantic vs structural)
    3. Replace `getFiniteStrings` null-return
    4. Make BRICS' regex default flags literal-first

**Say:**
> "Section twelve asks for a SOLID review and at least three
> refactoring proposals. Verdict: the Automaton class is a
> god-class with seventy-eight public methods, the library is
> closed for extension because it has no interfaces, and the
> concrete-dependency issue is opinionated rather than broken
> since BRICS is a library, not an application.
>
> Four concrete refactoring proposals, all surfaced organically
> from writing tests: fix a null-pointer bug in
> Datatypes.exists, clarify the isTotal contract, replace
> getFiniteStrings' surprising null-return, and — the biggest one
> — flip BRICS' default regex flags to literal-first so those
> five metacharacters stop silently breaking ported patterns."

---

## Slide 14 — Website screenshot (25 sec)

**On screen:**
- Screenshot of the deployed website (index.html) with the nav bar
  and the headline numbers table visible.

**Say:**
> "The full report is a static HTML website in the repository
> under the website folder. Nine pages, one per rubric area,
> cross-linked to the source code and docs on GitHub. All the
> numbers on the site regenerate with two Maven commands — the
> whole thing is reproducible from a fresh clone."

---

## Slide 15 — Conclusions (45 sec)

**On screen:**
- Header: "Takeaways"
- Bullet list:
  - **RQ2 answered empirically:** CIT + strong oracle helps up to 3-wise; either dimension alone doesn't
  - **Real defects found:** 1 NPE bug + 2 API-clarity issues + 1 interoperability landmine
  - **BRICS' algebra is correct:** all 18 metamorphic properties hold
  - **Reproducible:** every number regenerable with two commands, byte-identical CSVs
- Small footer: "Limitations: single BRICS commit, Datatypes.buildAll unreachable, weak-vs-strong oracle only tested at one strong-oracle definition"

**Say:**
> "To summarize. RQ2 has an empirically-backed answer: CIT's
> value is real but conditional on oracle strength. Along the
> way I found four concrete defects and API issues in BRICS, all
> from test-writing friction rather than a code review pass. On
> the positive side, all eighteen metamorphic properties hold,
> which means BRICS' regular-language algebra is correct on the
> operations I exercised.
>
> Limitations: single commit, one library, and the strong oracle
> is defined by a specific set of five metamorphic properties — a
> different strong oracle might land differently. All documented
> in the threats-to-validity page.
>
> Thanks for watching. Source code and full report at
> github dot com slash kotz96-lab slash TopicsSE_T1."

---

## Total pacing check

| Slide | Time    | Running |
|-------|---------|---------|
| 1     | 0:25    | 0:25    |
| 2     | 0:35    | 1:00    |
| 3     | 0:30    | 1:30    |
| 4     | 0:35    | 2:05    |
| 5     | 0:35    | 2:40    |
| 6     | 0:30    | 3:10    |
| 7     | 0:30    | 3:40    |
| 8     | 0:25    | 4:05    |
| 9     | 1:00    | 5:05    |
| 10    | 0:30    | 5:35    |
| 11    | 0:30    | 6:05    |
| 12    | 0:35    | 6:40    |
| 13    | 0:40    | 7:20    |
| 14    | 0:25    | 7:45    |
| 15    | 0:45    | 8:30    |

**Target: 8:30 total** — comfortably inside the 5–10 minute window
with room to breathe on the technical slides.

---

## Screenshots checklist (§17.3 requires all of these)

Take these before you start recording so you can drop them into
slides:

| Screenshot | Where to grab it | Which slide |
|---|---|---|
| Repository structure | VSCode file tree or GitHub root | Slide 3 |
| Generated JUnit tests | Open any `junitsuite/*.java` in editor | Slide 4 (optional) |
| PICT model | Open `pict/models/regex.pict` in editor | Slide 7 |
| Generated combinations | Open `pict/generated/regex-2wise.csv` | Slide 8 (optional) |
| PIT report | `target/pit-reports/index.html` in browser | Slide 5 |
| Coverage report | `target/site/jacoco/index.html` in browser | Slide 4 (optional) |
| Metamorphic tests | Open `MetamorphicPropertiesTest.java` | Slide 11 (optional) |
| Differential-testing results | Open `DifferentialTest.java` | Slide 12 (optional) |
| Static-analysis / SOLID | Open `docs/solid-analysis.md` rendered | Slide 13 (optional) |
| HTML website | Open `website/index.html` in browser | Slide 14 |
| Source-code example | Any BRICS class showing an issue | Slide 13 (optional) |
| Refactoring example | Open `docs/refactoring-proposals.md` rendered | Slide 13 (optional) |

---

## Recording checklist

- [ ] Slides in English (§17.1 requires English slides)
- [ ] Voiceover throughout (no gaps)
- [ ] No student on camera
- [ ] 5–10 minutes total (target 8:30)
- [ ] Exported as MP4
- [ ] File size reasonable (under 500 MB — recompress if larger)
- [ ] Screenshots readable at final resolution
- [ ] Practice-read the script once before final take
