# How to record the video

## Step 1 &mdash; Open the slides

Just double-click `slides/index.html` &mdash; opens in your default browser. No install needed.

- **Arrow keys** or **space** &mdash; next slide
- **Left arrow** or **backspace** &mdash; previous slide
- **Home / End** &mdash; jump to first / last slide
- **F** &mdash; toggle the little nav-hint at the bottom (turn off before recording)
- Or just **click** &mdash; right half = next, left half = previous

## Step 2 &mdash; Make sure it looks right

- Press **F11** to go fullscreen. The slides fill the screen edge-to-edge.
- Press **F** to hide the "arrow keys to navigate" hint at the bottom.
- Click through all 15 slides once to make sure everything renders.

## Step 3 &mdash; Record

Any screen recorder works. Free options:

| Tool | Platform | How |
|---|---|---|
| **OBS Studio** | Win / Mac / Linux | Free, industry standard. Add a "Display Capture" source, hit record. |
| **Xbox Game Bar** | Windows 10/11 | Press <kbd>Win</kbd>+<kbd>G</kbd>, click record. Simplest option. |
| **QuickTime** | Mac | File → New Screen Recording. |
| **loom.com** | Any (browser) | Web-based, no install. |

**Recommended settings:**
- Resolution: 1920×1080 (or your native)
- Frame rate: 30 fps is plenty
- Audio: your default mic, no need for anything fancy

## Step 4 &mdash; Read the script

Full narration is in [`../docs/video-script.md`](../docs/video-script.md).
Each slide has:
- **Say (X seconds):** &mdash; word-for-word text you read
- Pacing table at the bottom &mdash; total target 8:30

Recommended flow:
1. Open `slides/index.html` in the browser (F11 fullscreen).
2. Open `docs/video-script.md` on another monitor, or print it, or put it on your phone.
3. Start recording.
4. Read the "Say" section for slide 1.
5. Advance with arrow key.
6. Repeat until slide 15.

## Step 5 &mdash; Practice once (optional but recommended)

Do a full silent run-through first (no recording) to get pacing.
Then do one real take.
If a slide comes out badly, you can re-record just that slide and stitch, but honestly one take is fine &mdash; graders don't need polish, they need clarity.

## Step 6 &mdash; Export

Save as **MP4**. Aim for the file under 500 MB (the recording tools usually default to that anyway).

- Name it something like `topics-se-t1-brics-evaluation.mp4`.

## Step 7 &mdash; Optional: mix in real repo screenshots

The script has "screenshot: X" hints on some slides (bottom-left corner) &mdash;
these note where a real repo screenshot would be even better than the slide's mockup.
If you want, replace the slide's text panels with real screenshots for these:

| Slide | What to screenshot |
|---|---|
| 3 (repo structure) | VS Code file tree of `TopicsSE_T1/` |
| 4 (coverage) | `target/site/jacoco/index.html` in browser |
| 5 (PIT) | `target/pit-reports/index.html` in browser |
| 7 (PICT model) | `pict/models/regex.pict` open in editor |
| 8 (PICT tables) | `pict/generated/regex-2wise.csv` open in editor |
| 14 (website) | `website/index.html` in browser |

Totally optional. The slides work fine as-is.

## Step 8 &mdash; Package for submission

Per the professor's instructions, submit 4 zips:

- `source.zip` &mdash; the full repo
- `reproduce.zip` &mdash; same but stripped of `.git/`, `target/`, `.idea/`, `.vscode/`
- `website.zip` &mdash; just the `website/` folder
- `video.zip` &mdash; your MP4 (zipped)
