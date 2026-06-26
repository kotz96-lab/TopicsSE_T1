# Work split (65 / 35)

This is the working split agreed before kickoff; refine via PR comments,
not by silently moving work around.

## Partner A — 65%

Owns everything that makes the experiments reproducible and gradeable.

- **Repo + build + CI** (this scaffolding).
- **JUnit5 test suite** (assignment §6/§7 — 20 pts). Manual + AI-assisted
  tests under `src/test/java/se/topics/t1/junitsuite/`. Target: high
  line+branch coverage on `dk.brics.automaton.*`.
- **PICT / Combinatorial Interaction Testing** (§9 — **25 pts, biggest
  single piece**). Model design under `pict/models/`, 2/3/4-wise tables
  under `pict/generated/`, parameterized tests under `pict/` test package,
  comparison tables/plots in the final website.
- **PIT mutation testing** (§8 — 15 pts). Baseline + per-strength deltas
  + ≥5 surviving-mutant write-ups.
- **Infrastructure validation** (§13 — 3 pts). Tests for `infra/` code.
- **Automation scripts** (§14). `scripts/run-all.ps1`, PICT regen,
  report aggregation.

## Partner B — 35%

Owns the analysis and presentation surface that A's results feed into.

- **Metamorphic testing** (§10 — 15 pts). ≥10 properties under
  `src/test/java/se/topics/t1/metamorphic/`. Use generators from
  `se.topics.t1.infra` rather than rolling your own.
- **Differential testing vs `java.util.regex`** (§11 — 5 pts). Lives in
  `differential/`.
- **Software-design evaluation** (§12 — 10 pts). SOLID notes, design
  patterns, ≥3 refactoring proposals — write into `docs/`.
- **HTML website** (§15 — 5 pts). Consumes A's PIT/JaCoCo/PICT outputs.
- **Video presentation** (§17 — 5 pts).
- **Threats to validity + AI tools section** (§21 — 2 pts).

## Working agreement

- Open a PR per non-trivial change. Don't push to `main` directly.
- If you change something the other owns, ping in the PR description.
- Add to `docs/AI_TOOLS.md` every time you use an AI tool — what you
  asked, what you got, what you fixed. Section 21 needs this evidence.
