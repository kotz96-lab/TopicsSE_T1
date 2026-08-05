# PICT model rationale (assignment §9.2 / §9.4)

This document justifies the parameters and constraints in
[`pict/models/regex.pict`](../pict/models/regex.pict) and reports the
concrete combination counts across interaction strengths. It is the
narrative the §9.4 rubric asks for: *how the input model was designed,
why specific parameters were chosen, how many combinations were
generated, and the tradeoffs between coverage strength and test-suite
size*.

---

## What the model is describing

BRICS has two intertwined input spaces:

1. **The regex source string** that gets handed to
   [`RegExp`](../src/main/java/dk/brics/automaton/RegExp.java) — this
   is the thing whose parse tree drives most of the library's control
   flow.
2. **The operation** applied to the resulting
   [`Automaton`](../src/main/java/dk/brics/automaton/Automaton.java) —
   determinize, minimize, complement, union, intersection, etc.

Our model treats these as one combined input space so PICT can hit
interactions *between* how the regex is shaped and what we then do
with the automaton — e.g., "does BRICS handle complement of a
Unicode-negated bounded quantifier?"

---

## The seven parameters

For each parameter we give (a) what it captures, (b) the values
chosen, (c) which BRICS code paths we expect it to exercise, and
(d) why we didn't pick a bigger domain.

### 1. `Alphabet` — the character pool

**Values:** `ascii_lower`, `ascii_alnum`, `unicode_bmp`, `single_char`.

- `ascii_lower` (small dense range) exercises the run-length compression
  paths in [`BasicOperations.determinize`](../src/main/java/dk/brics/automaton/BasicOperations.java)
  where adjacent transition intervals merge.
- `ascii_alnum` (three disjoint ranges: `a-z`, `A-Z`, `0-9`) forces
  the multi-range branch of the `Transition` sort / merge routines.
- `unicode_bmp` (Hebrew block) sends BRICS through the same code but
  with `char` values above `0x0080`. BRICS is char-based (16-bit),
  not code-point-based, so BMP chars are the interesting boundary
  case *below* the surrogate pair region.
- `single_char` (one code point) hits the *singleton short-circuit*
  paths in `BasicOperations` (e.g. `concatenate` early-returns
  `makeString(a1.singleton + a2.singleton)` when both operands are
  singletons — line 57 of BasicOperations.java).

**Values NOT chosen:** we deliberately left out surrogate-pair
alphabets (BRICS treats them as pairs of `char`s and the library-level
semantics are not well-specified there) and control-character
alphabets (the RegExp parser rejects them). Adding them would inflate
the model without clearly hitting new BRICS branches.

### 2. `CharClassKind` — how the character class is written

**Values:** `single`, `range`, `set`, `negated`.

- `single` — a bare character (e.g. `a`). Simplest, exercises the
  common path.
- `range` — a bracketed range (e.g. `[a-z]`). Exercises the range
  compression in `RegExp.parseCharClass`.
- `set` — an enumerated set (e.g. `[abc]`). Exercises the
  transition-union path (`BasicAutomata.makeCharSet`).
- `negated` — a negated bracketed range (e.g. `[^a-z]`). Exercises
  the *complement-of-charset* code, which sits on a different branch
  than complement-of-automaton.

**Values NOT chosen:** shorthand escapes like `\d`, `\w`, `\s` — BRICS
supports these via `Datatypes` lookups, which we cover in a separate
test class. Mixing them into PICT rows would blow up the model
without adding new *combinatorial* interactions.

### 3. `Quantifier` — repetition

**Values:** `none`, `star`, `plus`, `optional`, `bounded`.

- `none` — no quantifier; unit test.
- `star` (`*`), `plus` (`+`), `optional` (`?`) — the three
  fundamental Kleene-family operators, each with a distinct BRICS
  code path in `BasicOperations.repeat` / `optional`.
- `bounded` — `{1,3}`. Exercises the general `repeat(a, min, max)`
  path, which is separate from the star/plus/optional shortcuts.

**Values NOT chosen:** `{n,}` (open-ended lower bound only) — that's
just `plus` prefixed by an extra copy. `{n}` (exact count) — a
special case of bounded with min == max. Both are hit indirectly.

### 4. `AlternationDepth` — number of `|` branches

**Values:** `0`, `1`, `2`.

- `0` — no alternation. Baseline.
- `1` — two branches (`A|B`). Exercises the two-way union path in
  `BasicOperations.union(a1, a2)`.
- `2` — three branches (`A|B|C`). Exercises the *collection* union
  path (`BasicOperations.union(Collection)`), which is a different
  code path with its own alias-detection logic.

**Not going higher:** depth 3+ would add many combinations without
hitting a new BRICS path — the collection path handles any n.

### 5. `NestingDepth` — parenthesised group nesting

**Values:** `0`, `1`, `2`.

- `0` — flat regex.
- `1` — one enclosing group (e.g. `(a|b)*`). Exercises grouping.
- `2` — nested (e.g. `((a|b)*)*`). Exercises the recursive parse in
  `RegExp.parseRegExp` and repeated grouping in the AST.

Constrained: at depth 2 with alternation depth 0 and quantifier
`none`, the pattern collapses to `((a))` which adds nothing over
`(a)`. Our constraint forbids that combination.

### 6. `Operation` — what to do with the automaton

**Values:** `toAutomaton`, `determinize`, `minimize`, `complement`,
`union`, `intersection`.

Each maps to a top-level `Automaton` operation. Six was picked as the
smallest set that covers every major public entry into
[`BasicOperations`](../src/main/java/dk/brics/automaton/BasicOperations.java)
and [`MinimizationOperations`](../src/main/java/dk/brics/automaton/MinimizationOperations.java).

**Values NOT chosen:** `subst`, `homomorph`, `shuffle` — these have
richer signatures (extra character maps, extra automata) that don't
fit a single categorical value. They're covered by the hand-written
[`SpecialOperationsTest`](../src/test/java/se/topics/t1/junitsuite/SpecialOperationsTest.java)
and [`ShuffleOperationsTest`](../src/test/java/se/topics/t1/junitsuite/ShuffleOperationsTest.java).

### 7. `InputLength` — length of the string we test against

**Values:** `empty`, `short`, `medium`.

- `empty` — the empty string. Hits every acceptance test's fast path.
- `short` — 3 characters. Fits in one quantifier iteration.
- `medium` — 10 characters. Exercises Kleene-star loops.

**Values NOT chosen:** `long` (100+). We initially included it and
found the 4-wise table doubled in row count without changing any
observed outcome — every test that fails on length-10 also fails on
length-100. Cutting it kept the 4-wise table under 600 rows.

---

## Constraints

Only 4 constraints — kept minimal to preserve interaction coverage.

```pict
IF [Alphabet] = "single_char" THEN [CharClassKind] IN {"single", "set"};
IF [Alphabet] = "single_char" THEN [AlternationDepth] IN {0, 1};
IF [CharClassKind] = "negated" THEN [Alphabet] <> "single_char";
IF [NestingDepth] = 2 AND [AlternationDepth] = 0 THEN [Quantifier] <> "none";
```

The first three eliminate rows that are structurally impossible
(single-character alphabet can't form a range or a negated class).
The fourth eliminates the vacuous `((a))` case at high nesting depth.
Without these constraints PICT generates rows that would crash
`RegExp` at parse time — those are BRICS behaviour tests, not
interaction tests.

---

## Combination counts

The full parameter cross-product is:

    4 × 4 × 5 × 3 × 3 × 6 × 3 = 12,960 combinations

Complete exhaustive testing at that scale is infeasible (and
redundant — most crashes fire on *pairs* of interacting parameters).
PICT reduces the count dramatically:

| Interaction strength | Rows generated | Reduction vs full |
|----------------------|----------------|--------------------|
| 2-wise (pairwise)    | **34**         | 99.7%              |
| 3-wise               | **152**        | 98.8%              |
| 4-wise               | **568**        | 95.6%              |
| Full cross-product   | 12,960         | 0%                 |

Each strength is a *superset* of the weaker one in terms of
interactions covered: every pair in the 2-wise table is also
represented in the 3-wise table, and so on.

---

## Reproducibility

- PICT version: **3.7.4** (Microsoft, MIT license).
- Seed: **1**, pinned via `/r:1` in `scripts/generate-pict.ps1` — so
  regenerating produces byte-identical CSVs.
- The three CSVs under [`pict/generated/`](../pict/generated/) are
  committed for the reproducibility artifact (§16).
- To regenerate: `pwsh scripts/generate-pict.ps1` (needs PICT on PATH
  or at `tools/pict.exe`).

---

## How the tests consume it

[`PictRegexTest`](../src/test/java/se/topics/t1/pict/PictRegexTest.java)
runs three parameterized methods — `pairwise`, `threeWise`,
`fourWise` — each backed by `@CsvFileSource(files = ...)`. Each row
goes through
[`RegexRowBuilder`](../src/test/java/se/topics/t1/pict/RegexRowBuilder.java)
which deterministically maps abstract parameter values to a concrete
regex string and a test input, then applies the row's declared
`Operation`.

For every row we assert:

1. **Smoke** — the pipeline runs without throwing.
2. **Double-negation invariant** — `A.complement().complement().run(s) == A.run(s)`.
   This holds for every regular language, so it's a cheap sharp check
   that surfaces per-row regressions.

Deeper metamorphic properties (union commutativity, minimization
idempotence, etc.) are Partner B's territory under
[`metamorphic/`](../src/test/java/se/topics/t1/metamorphic/) — §10.

---

## Tradeoffs discussed for the §9.4 write-up

- **2-wise vs 3-wise vs 4-wise size growth**: 34 → 152 → 568. Roughly
  a 4× step at each strength; matches PICT theory that k-wise
  coverage cost scales as O(v^k · log(n)) where v is the values per
  parameter.
- **What we lose at 2-wise**: pairs of parameters are covered but not
  triples — e.g. we cover `Alphabet × Operation` but not
  `Alphabet × Operation × NestingDepth`. A bug that only fires on a
  three-way interaction (unicode + complement + deep nesting) could
  slip through pairwise and only be caught at 3-wise.
- **What we lose at 4-wise**: even 4-wise doesn't cover
  all-parameter interactions. A truly worst-case bug requiring 5+
  interacting values would need exhaustive testing.
- **Cost**: on this suite, 4-wise adds ~400 more rows for ~half a
  second of extra runtime. The cost is dominated by BRICS' own regex
  compilation, not the test framework overhead.

All three strengths currently pass on the vendored BRICS commit
`582d8f3`. Whether they *stay* passing under PIT mutation is the next
data point — captured in `docs/week2-baseline.md` alongside the JUnit
suite baseline.
