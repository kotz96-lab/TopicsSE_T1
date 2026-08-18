# Findings scratch-pad — things worth writing up

> **What this file is.** A running scratch-pad of interesting things we
> stumble on while running the experiments. Each entry has enough
> context that it can be lifted straight into the final HTML website
> (§15) or narrated in the video (§17) without re-deriving anything.
> Append to the bottom as we go. When we start writing the website,
> triage into the right report sections.

---

## Finding 1 — CIT + strong oracle helps; CIT alone does not
### Where it belongs
Website §9.4 "Evaluation of Combinatorial Testing" and §7 discussion
of test-suite readability/effectiveness tradeoffs. Directly answers
RQ2 ("Does CIT improve mutation scores?") from §3 of the brief.

### The one-liner
Interaction strength alone is nearly useless (194 → 205 → 204
mutants killed at weak oracle); **oracle strength alone gives +15 to
+27 mutants**; **the two dimensions compound** — the strong-vs-weak
gap widens as interaction strength grows.

### The data (six isolated PIT runs, filtered via `-DincludedGroups=`)
| Oracle | 2-wise | 3-wise | 4-wise |
|---|---|---|---|
| **Weak** (smoke + double-neg on 1 input)     | 194 (9.6%)  | 205 (10.1%) | 204 (10.0%) |
| **Strong** (5 metamorphic props on 5 inputs) | 209 (10.3%) | 229 (11.3%) | 231 (11.4%) |

Test strength (kills among covered mutants):
| Oracle | 2-wise | 3-wise | 4-wise |
|---|---|---|---|
| Weak   | 40.8% | 42.5% | 42.1% |
| Strong | 43.5% | 47.6% | 48.0% |

Full write-up in [`docs/pict-strength-comparison.md`](docs/pict-strength-comparison.md).

### Why this is interesting
Textbook CIT results claim interaction strength moves the needle.
Our data says: **only if the oracle is strong enough to fire on the
extra combinations.** With weak oracles, extra combinations exercise
the same code paths in the same shallow way — each mutant can only be
killed once, so more invocations of the same assertion don't help.
With strong oracles (five metamorphic properties, five inputs each),
the extra combinations give the strong assertions more chances to fire
on distinct code paths.

**The compounding effect** — strong-vs-weak gap growing +15 → +24 → +27
across interaction strengths — is the strongest single finding.
Empirically confirms the "CIT rewards strong oracles" hypothesis with
a clean 3×2 grid.

### Diminishing returns story
Within the strong-oracle row, 2 → 3-wise gains **+20** mutants,
3 → 4-wise gains only **+2**. So even the strong oracle plateaus
past 3-wise. Practical recommendation: strong oracle at 3-wise is the
sweet spot for this codebase.

### A methodological note (also captured in AI_TOOLS.md)
An earlier version of this experiment used `-Dgroups=` to filter PIT
by tag — but that's a surefire-only flag; PIT silently runs every
tagged AND untagged test. Consequence: every "per-strength" run was
actually the full 1945-test suite, and the numbers we reported (all
~50.5%) were full-suite scores with PIT run-to-run noise. Human
review caught it by asking "check the oracle is actually stronger."
Corrected data is what appears in this table.

---

## Finding 2 — Real bug in `Datatypes.exists()`
### Where it belongs
Website §12 (SOLID / design analysis / refactoring proposals) as a
"potential refactoring candidate", and §8 as a "surviving-mutant
symptom that reflects a real defect." Could also be a talking point
in the video's "Interesting Findings" section (§17.2 suggested
structure).

### The one-liner
`Datatypes.exists(name)` throws `NullPointerException` instead of
returning `false` when the `.aut` classpath resource is missing.

### Where in the code
[`src/main/java/dk/brics/automaton/Datatypes.java`](src/main/java/dk/brics/automaton/Datatypes.java) line 471:

```java
public static boolean exists(String name) {
    try {
        Datatypes.class.getClassLoader().getResource(name + ".aut").openStream().close();
    } catch (IOException e) {
        return false;
    }
    return true;
}
```

`ClassLoader.getResource(...)` **returns null** when the resource is
absent. Calling `.openStream()` on `null` throws
`NullPointerException` — which is *not* an `IOException` and so is
*not* caught. The method contract clearly intends to signal "does not
exist" via a `false` return; the bug means callers get an unhandled
NPE instead.

### Suggested fix
```java
public static boolean exists(String name) {
    URL url = Datatypes.class.getClassLoader().getResource(name + ".aut");
    if (url == null) return false;
    try (InputStream in = url.openStream()) {
        return true;
    } catch (IOException e) {
        return false;
    }
}
```
(also fixes a leaked InputStream on the happy path.)

### How we found it
While extending JaCoCo coverage from 57% to 84% in the second
JUnit5-test batch, we wrote `DatatypesTest.existsThrowsOnMissing()`
to assert the observed behaviour. The test surfaces the bug and locks
it in so any future fix will trip our tests and force a matching
test update.

### Rubric leverage
- §8 asks us to analyse ≥5 surviving mutants and identify "possible
  bugs." This is a bug we found through *coverage*, not mutation —
  which is worth noting: coverage tools and manual test-writing found
  it before PIT did. §8 rewards discussing "types of faults best
  exposed by X"; this shows types of faults NOT exposed by mutation
  testing.
- §12.4 requires ≥3 concrete refactoring proposals. This is one.

---

## Finding 3 — BRICS' `isTotal()` is structural, not semantic
### Where it belongs
Website §12 (SOLID / API-clarity analysis) as an API surprise worth
flagging. Also §7 as a lesson learned while writing tests.

### The one-liner
`Automaton.isTotal()` doesn't answer "does this automaton accept
every string?" It answers "is this automaton in one specific canonical
shape that BRICS uses to represent Σ*?"

### Details
`BasicOperations.isTotal(a)` returns true only if `a.initial` is
accepting AND has exactly one transition that is a self-loop covering
`Character.MIN_VALUE..Character.MAX_VALUE`. Any semantically-total
automaton in a different shape returns `false`.

Concrete surprise we hit: `BasicOperations.complement(makeEmpty())`
produces an automaton whose *language* is Σ*, but its *structure*
isn't the canonical form → `isTotal()` returns `false`.

### Why it matters
A user reading the Javadoc reasonably expects `isTotal` to be a
semantic query. The current behaviour is a leaky abstraction: it
exposes an optimisation shortcut as if it were an invariant.

### Suggested API-clarity refactoring
Either:
- Rename to `isCanonicalTotal()` or `isTotalFast()` to match the actual
  contract, or
- Make it semantic by falling back to `Automaton.minimize(a).equals(BasicAutomata.makeAnyString())`
  when the structural check fails. Costs one minimize but gives the
  intuitive answer.

### Rubric leverage
- §12.1 (API clarity) — direct hit
- §12.4 (refactoring proposals) — this + the `exists()` bug + one
  more gets us the required ≥3 proposals

---

## Finding 4 — `SpecialOperations.getFiniteStrings(a, limit)` silently returns `null`
### Where it belongs
Website §12 (API design) as another example of surprising API
behaviour.

### The one-liner
`getFiniteStrings(a, limit)` returns `null` (not an empty set, not a
truncated set) when the language has more than `limit` strings — but
the return type is `Set<String>` with no nullable annotation.

### Impact
Any caller using the method as documented (`Set<String>`, so iterate
over it) gets a `NullPointerException`. The method is essentially
saying "I refuse to answer" via a return value collision with an
empty result. A cleaner API would either throw a checked exception
("too many strings — increase your limit") or return an `Optional<Set<String>>`.

### How we found it
While writing `SpecialOperationsTest`, we assumed the method would
return whatever fits under the limit. It returned `null` for a
3-string language with `limit=2`.

---

<!-- Add new findings below this line as we go. -->

## Finding 5 — Methodology bug caught by human review: PIT ignores `-Dgroups=`
### Where it belongs
Website §21 (Threats to Validity) and the AI-tools-and-corrections
section. Also worth a mention in §9.4 as a caveat that future
research on this codebase should heed.

### The one-liner
An earlier version of the per-strength PIT experiment used
`-Dgroups=<tag>` to filter tests by JUnit5 `@Tag` — but that's a
Surefire flag and PIT runs its own test engine. Consequence:
every "per-strength" PIT run was actually running the entire
1945-test suite, and the reported ~50% mutation scores were
full-suite scores with PIT run-to-run noise, not per-strength
scores.

### How we caught it
Human reviewer asked to verify the "strong oracle" was actually
stronger than the weak one. Cross-checking the `killingTest`
field in `target/pit-reports/mutations.xml` revealed kills from
every test class in the suite — not only the tag-filtered one.

### The fix
PIT's own filter flag is `-DincludedGroups=<tag>` (note the plural
form). With that, PIT correctly restricts to only tagged tests,
and per-strength numbers reflect the isolated contribution. See
[`docs/pict-strength-comparison.md`](docs/pict-strength-comparison.md)
for the corrected 3×2 grid.

### Why this matters for the report
Two things it teaches:
1. Empirical software engineering is unforgiving when tool
   defaults surprise you. A quiet difference between two
   filter flags produced apparently-valid results that were
   scientifically meaningless.
2. This is a legitimate "AI-assisted testing" caution — the
   original bug was AI-written PIT invocation, and the AI
   didn't cross-check the flag semantics until asked. That
   ties directly into §21's discussion of AI tool
   limitations.

---

## Finding 6 — BRICS has 5 default regex metacharacters that Java doesn't
### Where it belongs
Website §11 (Differential testing) as a documented dialect divergence.
Also §7 as a "gotcha we hit while writing tests."

### The one-liner
`RegExp` in BRICS reserves `@`, `#`, `~`, `&`, `<...>` as
metacharacters by default. Java's `Pattern` treats all of them as
literals. Any regex containing them behaves differently between the
two engines.

### The five characters and their BRICS meanings
| Char | BRICS meaning | Java meaning |
|---|---|---|
| `@` | ANYSTRING (Σ*, matches everything) | literal `@` |
| `#` | EMPTY language (matches nothing) | literal `#` |
| `~` | complement of the following expression | literal `~` |
| `&` | intersection of two expressions | literal `&` |
| `<name>` | reference to a named automaton | literal `<`, `>`, `name` |

### How we found it
While writing the differential test in
[`src/test/java/se/topics/t1/differential/DifferentialTest.java`](src/test/java/se/topics/t1/differential/DifferentialTest.java),
one of the "compatible-subset" cases (`[a-z]+@[a-z]+` vs input `abc`)
failed the "PIT requires all tests green" precondition. The failing
test made the mismatch obvious: BRICS accepts `abc` because
`[a-z]+@[a-z]+` = "letters, anything, letters" (with @ = ANYSTRING
matching the empty middle), while Java requires a literal `@`.

Cross-referenced with `RegExp.java` — these are behind syntax-flag
constants `ANYSTRING`, `EMPTY`, `COMPLEMENT`, `INTERSECTION`,
`AUTOMATON`. Passing `new RegExp(pattern, RegExp.NONE)` or
`RegExp.ALL & ~RegExp.ANYSTRING` disables them individually.

### Impact
Anyone who ports a regex from Java to BRICS (or vice versa)
without knowing about these will get a silently-different language
— no crash, no warning, just wrong matching. The BRICS Javadoc
mentions the flags but doesn't call out that the *default* enables
all of them.

### Rubric leverage
- §11 (Differential testing) — direct headline finding.
- §7 — evidence for "types of faults exposed by different testing
  techniques" (this was found by differential + PIT, not by unit
  tests).
- §12 (API-clarity) — potential fourth refactoring proposal:
  BRICS' default flag set should probably be conservative
  (literal-first), letting users opt IN to the extensions.
