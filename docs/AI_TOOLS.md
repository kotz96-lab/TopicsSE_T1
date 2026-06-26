# Use of AI Tools (assignment §21)

Append entries below as we go. The final website section is generated
from this file.

## Template

```
### YYYY-MM-DD — <short title>
- **Tool:** <e.g. Claude Code (Opus 4.7)>
- **Who:** <A or B>
- **What we asked it to do:**
- **What it produced:**
- **How we validated it:** (tests passed / manual review / cross-check / etc.)
- **Mistakes / corrections:**
```

## Entries

### 2026-06-26 — Week 1 scaffolding (Person A)
- **Tool:** Claude Code (Opus 4.7, 1M context).
- **Who:** A.
- **What we asked it to do:** Set up the project for both partners —
  clone the empty GitHub repo, vendor BRICS at a pinned commit, author a
  Maven build with JUnit5/JaCoCo/PIT, add Maven Wrapper so Person B
  doesn't need to install Maven, add GitHub Actions CI, write the README
  and work-split doc.
- **What it produced:** Repository skeleton described in `README.md`,
  initial PICT model under `pict/models/regex.pict`, smoke test, CI
  workflow.
- **How we validated it:** Ran `./mvnw verify` locally; CI run on the
  initial push (see Actions tab).
- **Mistakes / corrections:** *(fill in as we discover them).*
