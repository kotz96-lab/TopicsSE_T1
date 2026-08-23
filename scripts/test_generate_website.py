#!/usr/bin/env python3
"""Tests for generate_website.py (assignment §13 — infrastructure
validation must cover HTML-generation scripts).

Run with:
    python3 -m unittest scripts/test_generate_website.py -v
or, from the scripts/ directory:
    python3 -m unittest test_generate_website -v
"""
import pathlib
import sys
import tempfile
import unittest

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import generate_website as gw  # noqa: E402


class ReadFragmentTests(unittest.TestCase):
    def test_strips_exactly_one_trailing_newline(self):
        with tempfile.NamedTemporaryFile("w", suffix=".html", delete=False) as f:
            f.write("line one\nline two\n\n")  # two trailing newlines = a blank line
            path = pathlib.Path(f.name)
        try:
            # Only the file's own trailing newline should be stripped,
            # leaving the deliberate blank line intact — this is the
            # exact bug caught during development (see AI_TOOLS.md):
            # an earlier version used .rstrip("\n") and ate the blank
            # line before every page's closing </main> tag.
            self.assertEqual(gw._read_fragment(path), "line one\nline two\n")
        finally:
            path.unlink()

    def test_no_trailing_newline_is_unchanged(self):
        with tempfile.NamedTemporaryFile("w", suffix=".html", delete=False) as f:
            f.write("no trailing newline here")
            path = pathlib.Path(f.name)
        try:
            self.assertEqual(gw._read_fragment(path), "no trailing newline here")
        finally:
            path.unlink()


class RenderNavTests(unittest.TestCase):
    def test_exactly_one_page_marked_active(self):
        nav = gw.render_nav("cit")
        self.assertEqual(nav.count('class="active"'), 1)
        self.assertIn('<a href="cit.html" class="active">CIT</a>', nav)

    def test_every_page_present_and_correctly_ordered(self):
        nav = gw.render_nav("index")
        expected_order = [p["file"] for p in gw.PAGES]
        positions = [nav.index(f'href="{f}"') for f in expected_order]
        self.assertEqual(positions, sorted(positions),
                          "nav links must appear in PAGES order")

    def test_unknown_slug_marks_nothing_active(self):
        nav = gw.render_nav("no-such-page")
        self.assertNotIn("active", nav)


class RenderPageAgainstRealFixturesTests(unittest.TestCase):
    """Integration-style checks against the real website-src/ content,
    not synthetic fixtures — this is what actually ships."""

    @classmethod
    def setUpClass(cls):
        cls.layout = (gw.SRC / "layout.html").read_text(encoding="utf-8")

    def test_every_page_renders_with_no_placeholders_left(self):
        for page in gw.PAGES:
            with self.subTest(page=page["slug"]):
                rendered = gw.render_page(self.layout, page)
                self.assertNotIn("{{", rendered,
                                  f"{page['file']}: unreplaced template placeholder")
                self.assertIn(f"<title>{page['title']}</title>", rendered)

    def test_generated_output_matches_committed_website(self):
        """The regression test: regenerating website/ from website-src/
        must reproduce exactly what's committed. Fails the moment
        someone edits website/*.html by hand instead of website-src/,
        or edits a fragment without regenerating."""
        for page in gw.PAGES:
            with self.subTest(page=page["slug"]):
                rendered = gw.render_page(self.layout, page)
                committed = (gw.OUT / page["file"]).read_text(encoding="utf-8")
                self.assertEqual(
                    rendered, committed,
                    f"{page['file']} is out of sync with website-src/ — "
                    "run: python3 scripts/generate_website.py",
                )


if __name__ == "__main__":
    unittest.main()
