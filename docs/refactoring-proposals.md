# Refactoring proposals for BRICS (assignment §12.4)

*Draft owned by Person A to unblock Week 3. Person B per
[`SPLIT.md`](SPLIT.md) owns §12 and is expected to review, extend, or
restructure this document. Each proposal follows the rubric-mandated
format: **affected classes/methods**, **problem**, **proposed
refactoring**, **expected benefits**, **risks/tradeoffs**.*

The four proposals below were surfaced organically while writing the
§6 JUnit5 test suite in Week 2 and the §11 differential test in
Week 3 — they are not synthetic invention. Each represents observed
friction between BRICS' documented contract and its actual behaviour.
Two ((#1) and (#3)) are outright defects; (#2) is an API-clarity
issue; (#4) is an API-default-value choice with real interoperability
consequences.

---

## Proposal 1 — Fix `Datatypes.exists()` to actually return `false`

### Affected class/method
[`dk.brics.automaton.Datatypes`](../src/main/java/dk/brics/automaton/Datatypes.java),
`public static boolean exists(String name)` — line 470.

### Problem
Current implementation:

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

`ClassLoader.getResource(...)` returns **`null`** when the resource is
absent from the class-path. Calling `.openStream()` on `null` throws
`NullPointerException`, which is *not* caught by the surrounding
`try/catch(IOException)`. Result: `exists("NoSuchAutomaton")` throws
NPE to the caller instead of returning `false`.

Reproducer test:
[`DatatypesTest.existsThrowsOnMissing`](../src/test/java/se/topics/t1/junitsuite/DatatypesTest.java)
already documents the buggy behaviour so any fix will trip our
assertion and force a paired test update.

### Proposed refactoring

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

Three changes:
1. Explicit null-check on `getResource(...)` — the actual "does not
   exist" signal from the class loader.
2. Try-with-resources on the `InputStream` — fixes an additional
   resource leak on the happy path (`.close()` was never called on
   the returned stream in the original).
3. Method behaviour now matches its Javadoc ("Checks whether a given
   automaton is available. @return true if the automaton is
   available").

### Expected benefits
- Restores contract: `exists(name)` is now a total function, never
  throws.
- Callers can use it as a guard without try/catch. Current callers
  wrapping `exists` in defensive try/catch(NullPointerException) can
  be simplified.
- Eliminates a resource leak on the happy path.

### Risks and tradeoffs
- **Behavioural change:** any caller that catches NPE from
  `exists()` (as a workaround for the current bug) will get `false`
  instead. That is the intended behaviour, but it's a change.
- Requires updating the paired test
  (`DatatypesTest.existsThrowsOnMissing`) which currently asserts
  the buggy behaviour.
- No performance impact — same number of I/O operations.
- Backwards compatibility is *broken by design*: the point is to
  behave correctly. Semantic version bump if BRICS is released.

---

## Proposal 2 — Clarify `Automaton.isTotal()` — semantic vs structural

### Affected class/method
[`dk.brics.automaton.BasicOperations`](../src/main/java/dk/brics/automaton/BasicOperations.java),
`public static boolean isTotal(Automaton a)` — line 583. (Also
`Automaton.isTotal()` at line 943, which delegates.)

### Problem
`isTotal()` reads like a semantic query — "does this automaton accept
every string?" The current implementation is a **purely structural**
check:

```java
public static boolean isTotal(Automaton a) {
    if (a.isSingleton())
        return false;
    if (a.initial.accept && a.initial.transitions.size() == 1) {
        Transition t = a.initial.transitions.iterator().next();
        return t.to == a.initial && t.min == Character.MIN_VALUE && t.max == Character.MAX_VALUE;
    }
    return false;
}
```

It returns `true` only if the automaton is in one specific canonical
shape: an accepting initial state with exactly one self-loop covering
`MIN_VALUE..MAX_VALUE`. Any semantically-total automaton in a
different shape returns `false`.

Concrete surprise we hit during test writing:
`BasicOperations.complement(makeEmpty())` produces an automaton whose
*language* is Σ*, but whose structure is not the canonical form →
`isTotal()` returns `false`. See
[`BasicOperationsTest.complementEmpty`](../src/test/java/se/topics/t1/junitsuite/BasicOperationsTest.java)
which had to be relaxed from `assertTrue(c.isTotal())` to `.run()`
assertions on multiple strings.

### Proposed refactoring
Two viable directions, present both so B can pick.

**Option A — rename to match reality (surgical):**
Rename `isTotal` → `isCanonicalTotal` (or `isTotalFast`). Its
current semantics become explicit in the name. Callers who want the
semantic check must build it themselves. Zero performance impact,
minimal code churn.

**Option B — make it semantic (compatible):**

```java
public static boolean isTotal(Automaton a) {
    // Fast path: canonical shape (existing check).
    if (canonicalTotal(a)) return true;
    // Slow path: any Σ* automaton in some other shape.
    return BasicOperations.subsetOf(BasicAutomata.makeAnyString(), a);
}
```

Preserves the fast path for the common case; falls back to a
semantic subset check when the structure isn't canonical. Costs one
`subsetOf` call (linear-to-quadratic depending on determinism).

### Expected benefits
- Removes a documented API surprise → cleaner Javadoc → less
  test-writing friction.
- Aligns the method's name and behaviour with what users reasonably
  expect from library code.
- Option A costs nothing; Option B adds semantic correctness at
  bounded cost.

### Risks and tradeoffs
- **Option A** is a **breaking change** for any user relying on the
  current name. Trivial to update but noisy.
- **Option B** silently changes complexity for callers who counted
  on the O(1) shape check. A hot-path caller inside a loop might
  regress. Would want to benchmark before shipping.
- Both options change observable behaviour: after Option B,
  `complement(makeEmpty()).isTotal()` returns `true` where before it
  returned `false`.

---

## Proposal 3 — Replace `getFiniteStrings(a, limit)` null-return convention

### Affected class/method
[`dk.brics.automaton.SpecialOperations`](../src/main/java/dk/brics/automaton/SpecialOperations.java),
`public static Set<String> getFiniteStrings(Automaton a, int limit)`
— line 493.

### Problem
When the language has more than `limit` strings, the method aborts
and returns **`null`** — collided with the empty-set return that a
naïve caller would expect. The method signature is `Set<String>`
(non-nullable in casual reading), so callers who write the obvious
loop:

```java
for (String s : SpecialOperations.getFiniteStrings(a, 100)) { ... }
```

get a `NullPointerException` when the language has more than 100
elements.

Reproducer test:
[`SpecialOperationsTest.getFiniteStringsWithLimit`](../src/test/java/se/topics/t1/junitsuite/SpecialOperationsTest.java)
had to be relaxed to `assertNull(...)` — locking in the surprising
behaviour so any fix trips it.

### Proposed refactoring
Two options — pick one; both are Java-idiomatic.

**Option A — throw an explicit exception (fail-loud):**

```java
public static Set<String> getFiniteStrings(Automaton a, int limit) {
    // ... existing logic, but instead of returning null:
    if (result.size() > limit) {
        throw new IllegalStateException(
            "Language has more than " + limit + " strings; increase limit or use isFinite() first");
    }
    return result;
}
```

**Option B — signal via `Optional`:**

```java
public static Optional<Set<String>> getFiniteStrings(Automaton a, int limit) {
    // ... returns Optional.empty() when limit exceeded
}
```

### Expected benefits
- No more silent NPEs downstream from a `Set<String>` return.
- The "language too large" signal becomes explicit at the call site
  → forces the caller to handle it rather than silently crash later.
- Option A is stronger; Option B is more permissive.

### Risks and tradeoffs
- **Both are source-incompatible.** Existing callers doing
  `if (result != null)` guards would need a mechanical rewrite.
- Option A is a checked-exception-ish contract via
  `IllegalStateException` — some code styles prefer return values
  over exceptions for control flow.
- Option B pollutes the type with a wrapper, some Java style guides
  push back on `Optional` in return types for collections.
- Neither changes worst-case runtime.

---

---

## Proposal 4 — Make BRICS' regex default flags conservative (literal-first)

### Affected class/method
[`dk.brics.automaton.RegExp`](../src/main/java/dk/brics/automaton/RegExp.java),
constructor `public RegExp(String s)` (line 188) — which delegates
to `RegExp(String s, int syntax_flags)` with `syntax_flags = ALL`.
The `ALL` default enables five BRICS-only metacharacters:
`INTERSECTION` (`&`), `COMPLEMENT` (`~`), `EMPTY` (`#`),
`ANYSTRING` (`@`), `AUTOMATON` (`<name>`).

### Problem
By defaulting to `ALL`, BRICS silently gives five ordinary ASCII
characters non-obvious meanings that no other mainstream regex
engine reserves:

| Char | BRICS meaning | Java/PCRE/POSIX meaning |
|------|---------------|--------------------------|
| `@`  | ANYSTRING (Σ*) — matches everything | literal `@` |
| `#`  | EMPTY — matches nothing | literal `#` |
| `~`  | complement of following expression | literal `~` |
| `&`  | intersection of two expressions | literal `&` |
| `<name>` | reference to a named automaton | literals |

Anyone porting a regex between BRICS and Java/PCRE gets silently
wrong matching. There is no crash, no warning, no error — just
different accept/reject decisions on the same input.

**Reproducer**: [`DifferentialTest.KnownDisagreements.atSymbolIsMetaInBrics`](../src/test/java/se/topics/t1/differential/DifferentialTest.java)
demonstrates that `[a-z]+@[a-z]+` accepts `"abc"` in BRICS (because
`@` = ANYSTRING can match empty) but not in Java (which requires a
literal `@`).

### Proposed refactoring
Flip the default: `public RegExp(String s)` should delegate to
`RegExp(String s, RegExp.NONE)` — i.e., all five metacharacters are
literal by default. Users who want the extensions opt in explicitly:

```java
// Before: silent metacharacter interpretation
new RegExp("[a-z]+@[a-z]+");

// After (proposed): literal @ by default
new RegExp("[a-z]+@[a-z]+");                 // matches "abc@def" only
new RegExp("[a-z]+@[a-z]+", RegExp.ANYSTRING); // opt in to @ = Σ*
```

Alternative (softer): change the default to
`RegExp.INTERSECTION | RegExp.COMPLEMENT` only — keep the two
"regex algebra" operators (which are BRICS' unique value-add) but
demote `@`, `#`, `<name>` to literals.

### Expected benefits
- Removes the biggest interoperability landmine BRICS ships with.
- Reduces "surprise" bugs in code ported from Java regex.
- Callers who want BRICS' extensions now do so explicitly — the
  regex source contains no invisible semantics.
- Matches the principle of least astonishment: if a character looks
  literal, it should be literal by default.

### Risks and tradeoffs
- **Breaking change** for every existing BRICS user relying on the
  default flags. This is the single most invasive refactoring
  proposal here.
- Users writing `new RegExp("a&b")` today expect intersection; after
  the change they get a literal three-char string. Old code needs
  a mechanical rewrite (`new RegExp("a&b", RegExp.ALL)`).
- Could split the change over two releases: first a deprecation of
  the current constructor, then flip the default in the next major.
- No performance impact — pure semantic change at parse time.

---

## Summary table

| # | Location | Type | Effort | Risk | Rubric hook |
|---|---|---|---|---|---|
| 1 | `Datatypes.exists`                    | Bug fix                        | Low     | Low     | §8 (surviving-defect example), §12.4 |
| 2 | `BasicOperations.isTotal`             | Method-contract clarity        | Low–Med | Med     | §12.1 (contract clarity), §12.4     |
| 3 | `SpecialOperations.getFiniteStrings`  | API-hygiene refactor           | Low     | Med     | §12.4                                |
| 4 | `RegExp` default syntax flags         | API-default choice (interop)   | Low     | **High**| §11 (differential finding), §12.4    |

All four surfaced from writing tests, not from a code review pass —
which is a talking point in its own right for the §7 discussion of
"types of faults exposed by different testing techniques."
