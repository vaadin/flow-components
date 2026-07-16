<!-- Edit any field. This file is committed on the `repro/<issue>` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced | not reproduced (fixed | addressed | obsolete API | unknown) | partially reproduced | works as designed (likely misuse) | duplicate of #N
- **Hypothesis tested:** The bug is <X>, triggered by <Y>, observable as <Z>.
- **Regression?:** worked in <ver> / broke in <ver> | not a regression (broken since introduction) | unknown
- **Fixed by:** <repo>#PR (<one-line mechanism>) | broad rework (no single PR) | n/a
- **Duplicate of:** <repo>#N (<open/closed; the issue the fixing PR closed, if any>) | none found
- **Branch:** `repro/<issue>` — pushed to `vaadin/<repo>`
- **Reproduced on:** <repo> @ <version or branch>
- **Present on main?:** yes (still broken) | no (fixed in <line>) | n/a
- **Theme / Browser:** <theme> / <browser>
- **Screenshot** (static bug): ![<caption>](https://raw.githubusercontent.com/vaadin/<repo>/<commit-sha>/repro-<issue>.png) — embeds inline.
- **Demo video** (motion bug): `repro-<issue>-<symptom>.webm` (on the branch; drag into the comment for inline playback)

## Observed behavior

<What actually happened — cite the DOM snapshot, console output, or screenshot.>

## Expected behavior

<From the issue or the bug description.>

## Steps to reproduce

1. <step>
2. <step>
3. <step>

## Reproduction

How to run: start the server (`mvn … jetty:run`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-<issue>`
- **Scaffold:** `<path to the View committed on this branch>`

```
<key View source — the minimal reproduction>
```

## Root cause (suspected)

<short explanation of the problematic area>:

https://github.com/vaadin/<repo>/blob/<sha>/<path>#L<start>-L<end>

## Duplicate

<!-- Include when this issue duplicates another one — including an already-fixed issue discovered via the fixing PR; otherwise delete this section. -->
Same bug as **<repo>#N** (<open/closed; fixed by #PR if known>): identical root cause and trigger. <One line on why they match.> Recommend closing this issue as a duplicate of #N, citing the fixing PR when known.

## Notes

<Anything else: version-specific findings, dependencies added to an IT pom, dead ends, related issues.>
