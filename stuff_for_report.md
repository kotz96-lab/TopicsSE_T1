# Findings scratch-pad — things worth writing up

> **What this file is.** A running scratch-pad of interesting things we
> stumble on while running the experiments. Each entry has enough
> context that it can be lifted straight into the final HTML website
> (§15) or narrated in the video (§17) without re-deriving anything.
> Append to the bottom as we go. When we start writing the website,
> triage into the right report sections.

---

## Finding 1 — CIT didn't move the mutation score with a weak oracle
### Where it belongs
Website §9.4 "Evaluation of Combinatorial Testing" and §7 discussion
of test-suite readability/effectiveness tradeoffs. Also directly
answers RQ2 ("Does CIT improve mutation scores?") from §3 of the brief.

### The one-liner
Adding 754 PICT-driven tests (2wise + 3wise + 4wise) on top of 180
hand-written tests moved the mutation score from **52% to 51%** — no
measurable improvement, and cost **+11 minutes of PIT wall time**.

### The data
| Metric | 180-test baseline | 934-test (with PICT) |
|---|---|---|
| Total mutations | 2087 | 2087 |
| Killed | 1075 | 1074 |
| Mutation score | 52% | 51% |
| Test strength (killed / covered) | 70% | 70% |
| PIT wall time | ~60s | ~740s |

### Why (root cause)
The 180 hand tests use **example-based** assertions (`assertTrue(a.union(b).run("a"))`)
— sharp per-mutant signal. Each assertion "fingerprints" a specific
behaviour, so a mutant that flips it gets killed.

The 754 PICT tests use only two oracles per row:
1. **Smoke** — the pipeline doesn't throw.
2. **Double-negation** — `A.complement().complement().run(input) == A.run(input)`.

Both are broad but shallow. Many mutants that would corrupt language
membership still satisfy them. And extra test *invocations* against
the same *assertions* don't add detection power — each mutant can only
be killed once, and the hand tests already got most of them.

### Why this is actually an interesting finding
The assignment's RQ2 asks whether CIT improves mutation scores. Papers
often report "yes." **Our result: with a weak oracle, no.** This
matches the CIT literature's caveat that combinatorial testing's value
is bounded by oracle strength — combinations only find bugs if the
oracle can *see* the bug when it fires. Worth stating explicitly in
the write-up: CIT rewards strong per-row oracles (metamorphic
properties, differential comparison against `java.util.regex`), not
just more rows.

### What would change the story
Adding stronger per-row assertions — union commutativity, minimize
preserves language, intersection subset relation — would likely move
the score. Those live in Partner B's §10 territory though; combining
PICT with metamorphic tests is exactly what §9.3 and §10 suggest.

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
