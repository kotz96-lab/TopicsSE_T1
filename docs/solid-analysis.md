# §12 — SOLID and software-design evaluation of BRICS

*Draft owned by Person A to unblock Week 3. Partner B per
[`SPLIT.md`](SPLIT.md) owns §12 and should extend, restructure, or
argue back against any of the assessments below. Concrete refactoring
proposals live separately in
[`refactoring-proposals.md`](refactoring-proposals.md).*

**Scope of this document.** Assignment §12 asks for evaluation across
SOLID, design patterns, API design, maintainability, extensibility,
and testability. We treat SOLID as the primary structural lens and
weave the other topics in where they naturally attach.

**Codebase under review.** 18 Java source files at BRICS commit
`582d8f3`, all in the single package `dk.brics.automaton`. Total
~4,700 lines. Public API surface consists of ~90 methods across
`Automaton`, `RegExp`, `RunAutomaton`, `AutomatonMatcher`,
`BasicAutomata`, `BasicOperations`, `MinimizationOperations`,
`SpecialOperations`, `ShuffleOperations`, `Datatypes`, `State`,
`Transition`.

---

## SOLID — principle by principle

### S — Single Responsibility Principle

**Verdict: partially violated. Most impactful violation:
`Automaton.java` is a god-class.**

`Automaton.java` (~1150 lines, **78 public methods** by a `grep`
count) is simultaneously:

1. **Data structure** — holds `initial`, `singleton`, `deterministic`
   fields; is a mutable graph of `State` and `Transition`.
2. **Facade for every operation** — most of those 78 public methods are delegate methods like
   `union`, `intersection`, `complement`, `concatenate`, `repeat`,
   `optional`, `minimize`, `determinize`, `minus`, `subsetOf`,
   `isEmptyString`, `isTotal`, `getShortestExample`,
   `getFiniteStrings`, `getCommonPrefix`, `overlap`, `singleChars`,
   `trim`, `compress`, `subst`, `homomorph`, `projectChars`,
   `isFinite`, `getStrings`, `prefixClose`, `hexCases`,
   `replaceWhitespace`, `shuffle` — each of which delegates to a
   utility class (`BasicOperations`, `MinimizationOperations`,
   `SpecialOperations`, `ShuffleOperations`).
3. **Static factory** — `makeEmpty`, `makeEmptyString`,
   `makeAnyString`, `makeAnyChar`, `makeChar`, `makeCharRange`,
   `makeCharSet`, `makeString`, `makeStringUnion`, `makeInterval`,
   plus the numeric/XML factories. Each delegates to `BasicAutomata`
   but the public surface is on `Automaton`.
4. **Serialization endpoint** — `load(URL)`, `load(InputStream)`,
   `store(OutputStream)`, `equals` / `hashCode` based on structural
   equality.
5. **Global configuration** — `setMinimization(int)`,
   `setMinimizeAlways(boolean)`, `setAllowMutate(boolean)` — static
   flags that change behaviour across the entire library.

Each of these is a distinct responsibility. Any of them changing
touches `Automaton.java`. Concrete symptom during test writing: to
change how minimization is invoked, we could either touch
`MinimizationOperations` OR the four `Automaton.minimize*` methods
OR the `setMinimization` global flag — three surfaces for one
concept.

**Milder SRP concerns elsewhere:**

- `Datatypes.java` — mixes an XML-datatype registry (~50 pre-built
  automata), a Unicode-block registry (~120 blocks), a Unicode-
  category registry (~40 categories), the classpath resource loader,
  and a `main` that generates .aut files at compile time. Five
  distinct responsibilities.
- `BasicAutomata.java` — mixes literal-string automata factories
  (`makeChar`, `makeString`), numeric-interval automata
  (`makeInterval`, `makeMinInteger`, `makeMaxInteger`,
  `makeIntegerValue`, `makeDecimalValue`, `makeTotalDigits`,
  `makeFractionDigits`), and substring-matching automata
  (`makeStringMatcher`). Three distinct concerns.

**Where SRP is well-handled:** `Transition`, `State`, `StatePair` are
each tightly scoped data types with one job.

### O — Open/Closed Principle

**Verdict: violated. Adding new operations requires modifying existing files rather than extending via new classes.**

BRICS has **no interfaces or abstract classes** in the automaton
module (except two marker interfaces: `Serializable` and
`Cloneable`). The one extension seam is `AutomatonProvider` — an
interface with a single method that lets callers plug in a custom
resolver for named automaton references like `<foo>` in regex source.

Everything else is closed:

- Cannot add a new operation without modifying `Automaton.java` (to
  add the delegate) and one of the `*Operations.java` classes (to
  add the implementation).
- Cannot swap minimization algorithms — the four algorithms
  (`minimize`, `minimizeHuffman`, `minimizeBrzozowski`,
  `minimizeHopcroft`, `minimizeValmari` = actually five) are dispatched
  via a static integer flag `Automaton.minimization`, hardcoded to
  one of `MINIMIZE_HUFFMAN`, `MINIMIZE_BRZOZOWSKI`,
  `MINIMIZE_HOPCROFT`, `MINIMIZE_VALMARI`. Adding a sixth algorithm
  requires editing `MinimizationOperations.minimize(Automaton)` to
  add another switch arm.
- Cannot substitute the underlying state representation — `State`
  and `Transition` are concrete classes with public fields (see LSP
  discussion below).

**Where OCP is well-handled:** the regex parser (`RegExp.parseRegExp`)
uses a switch over `Kind` enum values, and the `RegExp` class is
recursive on itself — adding a new operator would require touching
one file. This is closer to OCP-friendly than the operations classes.

### L — Liskov Substitution Principle

**Verdict: mostly vacuous because there's almost no inheritance.**

BRICS uses inheritance in exactly three places:

1. `Automaton implements Serializable, Cloneable` — Java's marker
   interfaces. LSP-neutral.
2. `Transition implements Serializable, Cloneable` — same.
3. `State implements Serializable, Comparable<State>` — the
   `Comparable` contract IS honoured; `compareTo` is consistent with
   `equals` per the Javadoc.

**Genuine LSP hazard: `Automaton.isTotal()` is structural, not
semantic.** A caller reading the Javadoc reasonably expects
"is this automaton total?" to return `true` for any Σ*-accepting
automaton. Implementation only returns `true` for one canonical
shape. This is not an LSP violation in the class-hierarchy sense
(there's no subclass), but it *is* an LSP violation in the
"method contract vs behaviour" sense — the same method name
promises one thing and delivers another.

Formal proposal to fix in
[`refactoring-proposals.md`](refactoring-proposals.md) Proposal 2.

### I — Interface Segregation Principle

**Verdict: vacuously satisfied because there are no
domain interfaces.**

The only interface BRICS defines in-package is `AutomatonProvider`,
which is a single-method interface — the epitome of ISP compliance.

The god-class problem in `Automaton.java` is not an ISP violation
per se because `Automaton` is not an interface; but its ~60 delegate
methods do force any *user* of BRICS to import a single type that
depends on `BasicOperations`, `MinimizationOperations`,
`SpecialOperations`, `ShuffleOperations`, and their transitive
dependencies. A hypothetical `IAutomaton` interface split into
`IAcceptor` (`run`), `IAlgebra` (`union`, `intersection`,
`complement`), `IMinimizable` (`minimize`, `determinize`), and
`ISerializable` (`store`) would let clients depend only on the
capabilities they use.

### D — Dependency Inversion Principle

**Verdict: violated systematically. Every dependency is concrete.**

Everywhere in BRICS, concrete class names appear in signatures and
call sites:

- `BasicOperations.union(Automaton a1, Automaton a2)` — no
  interface, callers can't substitute a mock or an alternative
  implementation.
- `RegExp` depends directly on `Automaton`, `BasicAutomata`,
  `BasicOperations`.
- `Datatypes` depends directly on `Automaton.load(URL)` — which
  couples the datatype registry to Java's URL/classpath
  infrastructure.

**Practical consequence** for our test-writing: to test
`SpecialOperations.getFiniteStrings` we cannot inject a synthetic
automaton — we must build a real BRICS `Automaton`. That's not a
huge burden because BRICS' `Automaton` is straightforward to build,
but it does mean every test in `SpecialOperationsTest` incidentally
exercises `BasicAutomata` too. A DIP-friendly design with an
`IAutomaton` abstraction would let us test each module in strict
isolation.

**Fair caveat.** BRICS is a self-contained library, not an
application layer. In library code, taking concrete types in
signatures is often justified: there's typically no substitution
concern (no user is going to swap in an alternative `Automaton`),
and interface indirection would add ceremony without benefit. So
while BRICS is DIP-hostile in the strict OO-textbook sense, the
verdict should be read as "opinionated, not obviously wrong" rather
than a clear defect.

---

## Design patterns actually used

The assignment §12.3 asks us to identify important design patterns
and discuss their role, advantages, disadvantages, alternatives.

### Static factory method (widely used)

**Where:** `BasicAutomata.make*`, `Automaton.make*`. Instead of
public constructors, callers get automata via named static methods
that hide the singleton/deterministic short-circuits.

**Why it was used:** intent-revealing names (`makeEmpty` vs
`new Automaton()`), and the ability to return a subtype or a shared
instance if needed.

**Advantages:** readable at call sites (`BasicAutomata.makeAnyChar()`
tells the reader exactly what they get). Encapsulates optimisation
shortcuts (a `makeString(s)` sets `.singleton = s` internally).

**Disadvantages:** the number of factory methods on `Automaton` has
sprawled into the god-class problem discussed under SRP.

**Alternatives:** builder pattern for complex construction, but
BRICS' automata are so simple that a builder would be over-
engineered.

### Utility class / static-method module (widely used)

**Where:** `BasicOperations`, `MinimizationOperations`,
`SpecialOperations`, `ShuffleOperations` are all `final class`es
with private constructors and static methods only.

**Why it was used:** groups related pure functions on `Automaton`
without introducing a class hierarchy.

**Advantages:** zero object-lifecycle overhead; easy to test
independently (we did — see `BasicOperationsTest`,
`MinimizationOperationsTest`, `SpecialOperationsTest`,
`ShuffleOperationsTest`); readable dispatch (`BasicOperations.union`
tells you exactly what you're calling).

**Disadvantages:** locks in the OCP violation discussed above —
adding a new operation requires modifying an existing class.
Encourages the god-class facade pattern in `Automaton` (each utility
class's operations are also exposed as instance methods on
`Automaton` for ergonomic reasons).

**Alternatives:** strategy pattern (each operation as its own class
implementing an `AutomatonOperation` interface). Would improve OCP
at the cost of instantiation overhead and more classes.

### Comparator pattern

**Where:** `TransitionComparator implements Comparator<Transition>`.
Used by `State.getSortedTransitions()`.

**Why it was used:** exposes two distinct orderings (by-source-range
and by-destination-first) via a constructor flag `to_first`.

**Advantages:** minimal, well-scoped, follows Java's stdlib
convention.

**Disadvantages:** the `to_first` boolean flag is a code smell —
would be cleaner as two named comparators (`Transition.byRange()`,
`Transition.byDestination()`). But this is minor.

### Visitor-adjacent recursive walk

**Where:** `RegExp.toAutomaton` recurses over the parse tree by
switching on `Kind`. Not a full Visitor pattern (no accept method),
but the same intent: separate the parse-tree structure from the
transformation to an automaton.

**Advantages:** the switch is centralised, easy to read.

**Disadvantages:** classic switch-over-type-tag OCP issue — adding
a new AST kind requires editing this switch statement.

### Not used but sometimes assumed

- **Builder** — no builders. Given `Automaton` construction is
  fluent (`makeChar('a').union(makeChar('b')).repeat()`), a builder
  is unnecessary.
- **Iterator** — BRICS returns `Set<State>`, `Set<String>`, or bare
  arrays; iteration happens via the standard `for-each` on those.
  No custom iterators.
- **Singleton** — no singletons. Global state is via static fields
  on `Automaton` (`minimize_always`, `minimization`, `allow_mutate`)
  which is arguably worse than a proper singleton.

---

## Cross-cutting design observations

### Testability

BRICS is **remarkably testable for what it is**. Despite the DIP
violations noted above, every operation has a simple `Automaton →
Automaton` or `Automaton × Automaton → Automaton` or
`Automaton → boolean` signature, so unit-testing is straightforward
example-based work. We landed at 84% line coverage and 51% mutation
score in ~200 hand-written test methods with essentially no mocks
or fixtures.

The one class we struggled to test — `Datatypes.buildAll` —
struggles because of an external file dependency (`src/Unicode.txt`),
not because of a design flaw.

### Maintainability

Mixed:
- **Good:** every operation is a pure function on `Automaton`. Easy
  to read and modify one operation at a time.
- **Good:** the JavaDoc is unusually thorough — every public method
  has a description and complexity note.
- **Bad:** the god-class problem in `Automaton.java`. A 1150-line
  file with 60+ delegates is exhausting to navigate.
- **Bad:** package structure is flat. `dk.brics.automaton` has 18
  files; a rearrangement into `dk.brics.automaton.core`,
  `.operations`, `.regex`, `.datatypes` would improve navigability.
- **Bad:** public mutable fields on `State` and `Transition`
  (`accept`, `transitions`, `min`, `max`, `to`, `number`). These
  are intentional performance shortcuts but they mean any consumer
  who reads BRICS' internals is coupled to its concrete data
  representation.

### Extensibility

**Poor.** Discussed under OCP: no interfaces, static utility
classes, dispatch-via-integer-flag. To add a new operation you must
either fork the library or write your own wrapper that calls out to
BRICS.

### API clarity

**Mostly good, with the caveats logged as refactoring proposals:**

- `isTotal()` is structural not semantic → Proposal 2.
- `getFiniteStrings(limit)` returns `null` on overflow → Proposal 3.
- `Datatypes.exists()` throws NPE instead of returning `false` →
  Proposal 1 (fixes an actual bug).

The rest of the API surface — `union`, `intersection`, `complement`,
`run`, `minimize`, etc. — does what its name says.

### Complexity

BRICS' individual methods are usually straightforward (loops over
states/transitions, standard automata algorithms). The complexity
is *algorithmic* (subset construction is exponential, Brzozowski is
polynomial-ish, Hopcroft is O(n log n)) rather than
*code-structural*. The one long method is
`BasicAutomata.makeStringMatcher` at ~50 lines of dense transition
construction — well-commented, but dense.

### Duplication

Notable duplication:

- The `make*` static factory methods appear on both `Automaton` and
  `BasicAutomata` — identical signatures, `Automaton` delegates.
- The instance `union/intersection/complement/etc.` methods on
  `Automaton` are one-line delegates to the corresponding
  `BasicOperations` static methods. This is intentional API
  ergonomics but does mean the type appears in two places.
- The five minimization algorithms in `MinimizationOperations`
  share the "compute equivalence classes and collapse" skeleton but
  each has its own implementation of the equivalence computation.

### Documentation quality

**Above average for an academic library.** Every public method has
a Javadoc block with description, parameters, complexity note, and
often a `@see` cross-reference. The refactoring proposals in this
package would be materially harder to write without this Javadoc.

---

## Summary

BRICS is **well-implemented but rigidly-architected**. The
algorithms are correct and well-tested (as our JaCoCo + PIT numbers
show); the class-level design is closed to extension, dependency-
heavy on concrete types, and organised around a god-class. This is
typical of research code from the era it was written (early 2000s
Java, pre-generics fully-adopted, pre-lambda), and the refactoring
proposals in `refactoring-proposals.md` show the friction is
concrete but bounded — three specific defects/API smells that could
be fixed with surgical changes rather than an architectural rewrite.
