#!/usr/bin/env python3
"""Generate website/*.html from website-src/ (assignment §14/§15).

The site has one shared shell (nav, head, footer credit line) and
9 pages that each contribute a title, a <main> content fragment, and
a small footer-links fragment (prev/next navigation, which doesn't
follow a single fixed rule across the site, so it's stored as data
rather than derived). This script stitches them together so the nav
bar and page shell live in exactly one place instead of being
hand-copied across 9 files.

Usage:
    python3 scripts/generate_website.py [--check]

--check exits non-zero if regenerating would change any file under
website/ (useful in CI to catch website-src/ and website/ drifting
apart) instead of writing the files.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "website-src"
OUT = ROOT / "website"

# Order here is nav order, top to bottom.
PAGES = [
    {"slug": "index",           "file": "index.html",           "nav": "Overview",
     "title": "Evaluating BRICS — Empirical study | TopicsSE T1"},
    {"slug": "junit",           "file": "junit.html",           "nav": "JUnit5",
     "title": "JUnit5 Test Suite | BRICS Evaluation"},
    {"slug": "cit",             "file": "cit.html",             "nav": "CIT",
     "title": "Combinatorial Interaction Testing | BRICS Evaluation"},
    {"slug": "metamorphic",     "file": "metamorphic.html",     "nav": "Metamorphic",
     "title": "Metamorphic Testing | BRICS Evaluation"},
    {"slug": "differential",    "file": "differential.html",    "nav": "Differential",
     "title": "Differential Testing | BRICS Evaluation"},
    {"slug": "design",          "file": "design.html",          "nav": "Design",
     "title": "Software Design Evaluation | BRICS Evaluation"},
    {"slug": "threats",         "file": "threats.html",         "nav": "Threats",
     "title": "Threats to Validity | BRICS Evaluation"},
    {"slug": "reproducibility", "file": "reproducibility.html", "nav": "Reproducibility",
     "title": "Reproducibility | BRICS Evaluation"},
    {"slug": "ai-tools",        "file": "ai-tools.html",        "nav": "AI Tools",
     "title": "Use of AI Tools | BRICS Evaluation"},
]


def render_nav(active_slug: str) -> str:
    lines = []
    for page in PAGES:
        active = ' class="active"' if page["slug"] == active_slug else ""
        lines.append(f'            <a href="{page["file"]}"{active}>{page["nav"]}</a>')
    return "\n".join(lines)


def _read_fragment(path: pathlib.Path) -> str:
    # Fragment files are stored with exactly one trailing newline (a
    # normal well-formed text file); strip only that one back off so
    # the reassembled page matches the original whitespace exactly,
    # including a deliberate blank line before the closing tag.
    text = path.read_text(encoding="utf-8")
    return text[:-1] if text.endswith("\n") else text


def render_page(layout: str, page: dict) -> str:
    main = _read_fragment(SRC / "pages" / f"{page['slug']}.html")
    footer = _read_fragment(SRC / "footers" / f"{page['slug']}.html")
    out = layout
    out = out.replace("{{TITLE}}", page["title"])
    out = out.replace("{{NAV}}", render_nav(page["slug"]))
    out = out.replace("{{MAIN}}", main)
    out = out.replace("{{FOOTER}}", footer)
    return out


def main() -> int:
    check_only = "--check" in sys.argv
    layout = (SRC / "layout.html").read_text(encoding="utf-8")

    drift = []
    for page in PAGES:
        rendered = render_page(layout, page)
        target = OUT / page["file"]
        if check_only:
            current = target.read_text(encoding="utf-8") if target.exists() else None
            if current != rendered:
                drift.append(page["file"])
        else:
            target.write_text(rendered, encoding="utf-8")
            print(f"wrote {target.relative_to(ROOT)}")

    if check_only:
        if drift:
            print("website/ is out of date for: " + ", ".join(drift))
            print("Run: python3 scripts/generate_website.py")
            return 1
        print("website/ matches website-src/ for all pages.")
        return 0

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
