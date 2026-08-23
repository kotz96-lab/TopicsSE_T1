# Static analysis (assignment §12 / §16)

§12 lists SpotBugs, Checkstyle, SonarQube, and IntelliJ inspections
as optional tools to complement the manual/AI-assisted design
analysis in [`solid-analysis.md`](solid-analysis.md) and
[`refactoring-proposals.md`](refactoring-proposals.md); §16 lists
"static-analysis reports" as a required reproducibility-artifact
item. This document is that artifact.

We used **SpotBugs 4.10.4.0** via the `spotbugs-maven-plugin`,
scoped to `dk.brics.automaton.*` only (see
[`spotbugs-include.xml`](../spotbugs-include.xml)) — same rationale
as PIT's `targetClasses` scoping: findings should be about the
library under evaluation, not our own test harness.

## Reproduce this report

```pwsh
./mvnw -Pspotbugs verify
# HTML report: target/spotbugs.html
# Raw XML:     target/spotbugsXml.xml
```

## Aggregate results

**33 findings** across the 26 vendored BRICS classes, all at Low
effort threshold (SpotBugs' most sensitive setting) so this is a
maximal, not minimal, count.

| Category | Count |
|---|---|
| `MALICIOUS_CODE` (mutable-state exposure) | 12 |
| `BAD_PRACTICE` | 13 |
| `STYLE` | 5 |
| `MT_CORRECTNESS` | 1 |
| `EXPERIMENTAL` | 1 |
| `SECURITY` | 1 |

| Priority | Count |
|---|---|
| 1 (high) | 1 |
| 2 (medium) | 21 |
| 3 (low) | 11 |

None of these are the kind of finding a build should fail on — no
priority-1 correctness bugs beyond the one discussed below — which
is itself informative: SpotBugs' style/encapsulation/API-safety
findings corroborate the *design*-quality gaps already identified
manually in [`solid-analysis.md`](solid-analysis.md) (mutable
internal state, the `Automaton` god-class) rather than surfacing new
correctness bugs the way PIT and our own test writing did (compare
[`mutation-analysis.md`](mutation-analysis.md) and the three
defects in `stuff_for_report.md`). That's the honest overall
takeaway: static analysis here is corroborating evidence for the
manual design review, not an independent bug-finding channel.

## Notable individual findings

### `EI_EXPOSE_REP` / `EI_EXPOSE_REP2` — mutable internal state exposure (12 of 33)

The single largest cluster. Example:
`Automaton.getInitialState()` returns the live `initial` field
directly, and `Automaton.setInitialState(State)` stores a caller-
supplied `State` directly, both without defensive copying. Also hits
`State`, `StatePair`, `Transition`, and `StringUnionOperations`.

**Why it matters:** this is a direct instance of the encapsulation
gap already flagged in `solid-analysis.md`'s SRP discussion —
`Automaton`'s internal graph is reachable and mutable from outside
callers, which is part of why `Automaton.equals()` uses identity/
hashCode rather than structural equality (a mutable object can't
safely be a hash-map key by content). Not something we'd propose
fixing (BRICS' performance model depends on avoiding defensive
copies of automaton graphs), but worth naming as a deliberate
trade-off rather than an oversight.

### `CT_CONSTRUCTOR_THROW` — partially-initialized objects on exception (4 of 33)

`MatchOnlyRunAutomaton(Automaton)` and `RegExp(String)` (two
constructors each) can throw before finishing initialization,
leaving a partially-constructed object reachable in theory to a
finalizer-attack. BRICS predates Java's `final` class hardening
conventions; low real-world risk since none of these classes are
designed for untrusted subclassing, but a legitimate class-design
smell.

### `MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR` — `RunAutomaton`

`RunAutomaton`'s constructor calls the overridable `setAlphabet()`.
This is the textbook "calling an overridable method from a
constructor" pitfall — a subclass overriding `setAlphabet()` would
have it invoked before the subclass's own fields are initialized.
Directly the same class of issue as the LSP discussion in
`solid-analysis.md`.

### `BC_EQUALS_METHOD_SHOULD_WORK_FOR_ALL_OBJECTS` + `NP_EQUALS_SHOULD_HANDLE_NULL_ARGUMENT` — `StringUnionOperations.State`

`equals(Object)` assumes its argument is already a
`StringUnionOperations.State` and doesn't null-check — violates the
`Object.equals` contract (`equals(null)` should return `false`, not
throw). This is a real, if narrow, correctness gap: any code that
calls `.equals(null)` or `.equals(somethingElse)` on this type risks
a `ClassCastException`/`NullPointerException` instead of a clean
`false`. We didn't add a `differential`/`metamorphic` test targeting
this specifically since `StringUnionOperations.State` is a package-
private implementation detail not reachable from our public-API test
surface, but it's worth flagging here as a finding a purely
public-API-driven test suite structurally cannot catch.

### `AA_ASSERTION_OF_ARGUMENTS` + `ASE_ASSERTION_WITH_SIDE_EFFECT_METHOD` — `StringUnionOperations.add`

Argument validation is done via a Java `assert` statement, which is
compiled out and silently skipped unless the JVM runs with `-ea`
(our tests do, by JUnit5/Surefire default — so our own test runs
exercise this validation, but a production embedder of BRICS running
without `-ea`, which is the JVM default, would not). SpotBugs also
flags that the asserted method call may have a side effect, so
disabling assertions doesn't just skip validation — it can change
behavior. A real, actionable API-robustness finding.

### `LI_LAZY_INIT_STATIC` / `ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD` — `Automaton.isDebug()` (and 2 more instances elsewhere)

Non-thread-safe lazy initialization of a static field from an
instance method. Corroborates `solid-analysis.md`'s note that
BRICS' global mutable static flags (`minimize_always`,
`minimization`, `allow_mutate`) are "arguably worse than a proper
singleton" — this is the same pattern, caught mechanically.

### `DM_EXIT` — `Datatypes.buildAll()` calls `System.exit(...)`

Confirms independently (via a different tool, not just our own
reading of the source) that `buildAll()` is genuinely build-tool
code, not library code meant to run inside a caller's JVM — a
`System.exit()` call inside library code that could be reached at
runtime would be a serious embeddability bug; here it reinforces why
we scoped it out of PIT (see `mutation-analysis.md`) rather than
treating it as reachable.

### `UC_USELESS_OBJECT` — `MinimizationOperations.minimizeHopcroft`

A local variable `split` is computed but never used. Minor, but a
concrete dead-computation instance worth naming — differs from the
`Datatypes.buildAll` dead-code story in that this is dead *within* a
reachable, tested method, not a whole unreachable subtree.

## What we did not do

We did not run Checkstyle or SonarQube — SpotBugs alone already
surfaced enough material to corroborate (rather than duplicate) the
manual SOLID/pattern analysis, and adding a second style-focused
linter (Checkstyle) would mostly report formatting-convention
findings orthogonal to this assignment's design-quality questions.
