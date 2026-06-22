# Tier 3 — GWT-elimination roadmap

> Drafted 2026-06-22 by slice 127. The user reaffirmed the long-term
> goal: **eliminate GWT dependence completely**. This document
> enumerates every site of GWT dependence in the repo + sequences
> their elimination.
> **Status:** survey done; execution split across Phase 3.6 (flip)
> and Phase 3.7 (cleanup).

## Why this document exists

Tier 3's Phase 3 sub-phases have so far been framed as "build the
TS/Lit replacement" — but the natural follow-up is "remove the GWT
client once the replacement is feature-complete". The endpoint isn't
just "the new code works"; it's "the old code is gone, the GWT
toolchain is unwound from the build, and the 1.2 MB blob is no
longer shipping to users". Without an explicit elimination plan,
that endpoint stays vague + the work coasts indefinitely with both
implementations alive.

## The full GWT-dependence inventory

### 1. GWT client source module — `vaadin-spreadsheet-flow-client/`

- **Size:** 25,965 LOC across 105 Java files.
- **Purpose:** GWT-Java source compiled into `spreadsheet-export.js`.
- **Pom:** declares gwt 2.10.0 + `gwt-maven-plugin`, source level
  forced to `auto` because gwt-maven-plugin doesn't support Java 17.
- **Verdict:** the entire module disappears in Phase 3.7. No
  partial reduction possible — it's the GWT side of the rewrite.

### 2. The 1.2 MB compiled JS blob — `spreadsheet-export.js`

Two committed copies, both 1,242,638 bytes:

- `vaadin-spreadsheet-flow/src/main/resources/META-INF/resources/frontend/vaadin-spreadsheet/spreadsheet-export.js`
- `vaadin-spreadsheet-flow-integration-tests/frontend/generated/jar-resources/vaadin-spreadsheet/spreadsheet-export.js`

The second is a generated copy (in `target`-adjacent territory) and
should already be regenerated from the JAR. The first is the
load-bearing committed artifact. Both go in Phase 3.7.

### 3. JS-side integration point — `vaadin-spreadsheet.js` (the Lit wrapper)

**This is the single most important line of code for the elimination.**
The wrapper imports the GWT-compiled Spreadsheet class:

```js
// vaadin-spreadsheet.js:11
import { Spreadsheet } from './spreadsheet-export.js';
```

Phase 3.6 flips this one import to point at the new TS/Lit
implementation:

```js
import { Spreadsheet } from './lit/Spreadsheet.js';
```

That single edit IS the user-visible cutover. Everything else
(removing the blob, deleting the GWT module, unwinding the Maven
profile) is cleanup that happens after.

### 4. Maven dependency from `vaadin-spreadsheet-flow` → `vaadin-spreadsheet-flow-client`

```xml
<!-- vaadin-spreadsheet-flow/pom.xml -->
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-spreadsheet-flow-client</artifactId>
    <version>${project.version}</version>
</dependency>
```

**Important finding:** the server-side module has ZERO Java
imports from the client module. The dependency exists purely so
the GWT JAR (which contains `spreadsheet-export.js` and the GWT
sources as resources) ends up on the classpath at runtime. Once
the blob is gone, this dep is dead weight.

### 5. Parent pom — `vaadin-spreadsheet-flow-parent/pom.xml`

Three GWT-aware sections:

- `<gwt.version>2.10.0</gwt.version>` property.
- GWT dependency management block (`<groupId>com.google.gwt</groupId>`).
- `<id>gwt</id>` build profile activated for GWT compilation.

All three go in Phase 3.7 cleanup.

### 6. TestBench DOM-class coupling

TestBench element helpers in `vaadin-spreadsheet-testbench/` hardcode
the GWT-side DOM class names:

```
vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-testbench/src/main/java/com/vaadin/flow/component/spreadsheet/testbench/
├── AddressUtil.java
├── SheetCellElement.java
├── SheetHeaderElement.java
└── SpreadsheetElement.java
```

These query the rendered DOM by CSS class (`.cell`, `.col1.row1`,
`.sheet`, `.sheet-tabsheet`, etc.). The TS rewrite MUST preserve
these class names verbatim if the TestBench module is to keep
working unchanged. Verified via slice 125's CellKeys port — the
`toKey` + `toCssKey` functions preserve the `col1 row1` /
`.col1.row1` format byte-equivalent, exactly because the TestBench
classes match against them.

**Verdict:** no module deletion needed. Phase 3.6's DOM contract
honors the existing class-name convention; the TestBench module
stays the same.

### 7. Wire-format DTOs — `vaadin-spreadsheet-flow/src/main/java/com/vaadin/flow/component/spreadsheet/rpc/`

Server-side DTOs (`CellData.java`, `MergedRegion.java`,
`OverlayInfo.java`, `SpreadsheetActionDetails.java`,
`PopupButtonState.java`, `GroupingData.java`) emit JSON shapes
that BOTH the GWT and TS clients consume. Slice 106 pinned them
with characterization tests; slice 118 ported the TS interfaces.

**Verdict:** no change in Phase 3.7. These are the wire contract
the rewrite preserves. They survive GWT elimination.

### 8. Property-mirror system — `MirroredProperty<T>`

Server-side state is pushed to clients via
`Element.setProperty(name, value)` (from slice 26-37's
MirroredProperty rollout). The Lit wrapper subscribes to property
changes and forwards them to the GWT-Spreadsheet's setter methods
(see lines 174-305 of `vaadin-spreadsheet.js`).

After Phase 3.6, the wrapper still subscribes the same way but
forwards to the TS-Spreadsheet's setters (which the slice 126
audit verified match the wrapper's calls 1:1). MirroredProperty
itself is unchanged.

**Verdict:** preserved. The TS rewrite plugs into the SAME wire
pipe.

### 9. Documentation references

- `lit/README.md` mentions `spreadsheet-export.js` in 3 places —
  all describing the current GWT-blob setup the rewrite replaces.
  Update in Phase 3.7 to describe the new structure.
- `CLAUDE.md` mentions the component layout but not GWT specifically;
  audit + clean in Phase 3.7.
- `refactor-artifacts/TIER3_PLAN.md` documents the rewrite; should
  remain as historical record + cross-reference this roadmap.

## Phase ordering

```
Phase 3.1 — algorithm ports (slices 118-120, 124, 125)           ✓ done
Phase 3.2 — Spreadsheet class scaffolding (slices 121, 126)      ✓ skeleton complete (105-method API)
Phase 3.2 cont. — cell rendering + virtual scrolling             ◯ in progress
Phase 3.3 — editing + formula bar                                ◯ pending
Phase 3.4 — Vaadin Flow integration (custom editors, overlays)   ◯ pending
Phase 3.5 — polish + parity                                      ◯ pending
Phase 3.6 — flip the wrapper import (THE cutover)                ◯ this is the moment GWT stops shipping
Phase 3.7 — remove GWT artifacts (THIS roadmap's payoff)         ◯ cleanup; can be its own PR or merged into 3.6
```

## Phase 3.7 — the elimination steps (in order)

Once Phase 3.6 ships and the new client is verified in
production (or pre-merge via the visual-regression IT from slice
123), Phase 3.7 walks the inventory and removes each item:

### 3.7.1 — Delete the GWT client module

```sh
# 1. Remove from parent pom's <modules> list
# 2. Delete the directory
git rm -r vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow-client/
```

Removes 105 files / 25,965 LOC.

### 3.7.2 — Delete the compiled blob

```sh
git rm vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/main/resources/META-INF/resources/frontend/vaadin-spreadsheet/spreadsheet-export.js
```

The IT-module copy regenerates from JAR resources, so removing the
source-of-truth removes both.

### 3.7.3 — Remove the server-module dep on the client module

```xml
<!-- vaadin-spreadsheet-flow/pom.xml — delete this block -->
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-spreadsheet-flow-client</artifactId>
    <version>${project.version}</version>
</dependency>
```

Safe because slice 127 verified zero Java imports of client classes
from the server module.

### 3.7.4 — Remove GWT bits from the parent pom

- Drop `<gwt.version>` property.
- Drop the `<groupId>com.google.gwt</groupId>` dep-management entry.
- Drop the `<id>gwt</id>` build profile.

### 3.7.5 — Update lit/README.md + add a HISTORY note

The README's "TODAY: this.api = new Spreadsheet from the 1.3 MB GWT
blob" diagram becomes obsolete. Reframe to describe the
post-elimination structure (just `lit/Spreadsheet.ts`, no escape
hatch). Add a brief HISTORY note pointing at this roadmap +
TIER3_PLAN for context.

### 3.7.6 — Remove the `spreadsheetWidget` escape hatch

The `readonly spreadsheetWidget = undefined` field added in slice
126 exists for the GWT-era wrapper's
`api.spreadsheetWidget.sheetWidget.definedRowHeights` existence
check. After Phase 3.6, the wrapper itself updates and doesn't
check for that property anymore. After Phase 3.7, the field
disappears too.

Also: lit-spreadsheet-skeleton.test.ts's "legacy spreadsheetWidget
escape hatch" test (added in slice 126) gets deleted.

### 3.7.7 — Verify nothing in the broader flow-components repo depends on the client module

```sh
# Should return nothing AFTER 3.7.3:
grep -rl "vaadin-spreadsheet-flow-client" --include=pom.xml
```

If any other module (charts? grid?) accidentally depends on the
client, surface that BEFORE the removal commit. The slice 127 grep
returned only the spreadsheet's own pom — the surface is contained.

## Risks + open questions

### R1 — TestBench DOM-class regression

The TS rewrite MUST emit DOM with the same class names as GWT
(`col1 row1`, `.sheet`, `.sheet-tabsheet`, `.cell-input`,
`.cell-range`, `.namedrangebox`, `.addressfield`, etc.). If a
class name shifts, the TestBench element classes break + ALL
integration tests fail.

**Mitigation:** the visual-regression IT (slices 122-123) catches
ANY visible DOM change including class-name-driven CSS shifts. Run
it as the Phase 3.6 acceptance gate.

### R2 — Hidden API surface still in use

Slice 126's comm-diff found 54 missing methods that slice 120
missed. It's possible the EVENT side has similar misses — i.e.
events fired by the GWT client that the wrapper handles, but
which aren't yet in the TS Spreadsheet's emit surface.

**Mitigation:** symmetric comm-diff against the wrapper's
event-listener registrations + the SpreadsheetEvent discriminated
union. Audit before Phase 3.6.

### R3 — Customer code depending on internal classes

Vaadin Spreadsheet has been GA for years. Some customers may have
reached into `com.vaadin.addon.spreadsheet.client.*` via Maven for
custom widgets. Removing the client module would break them.

**Mitigation:** check Vaadin Commercial customer reports + the
spreadsheet GitHub issues. If any external usage exists, gate the
removal behind a major-version bump + deprecation cycle.

### R4 — Build tooling drift

GWT 2.10.0 + gwt-maven-plugin are old (Java source level forced to
"auto"). Removing them simplifies the build matrix. But: any CI
script that explicitly mentions the gwt profile (`mvn -Pgwt
package`) breaks. Audit CI configs before 3.7.4.

### R5 — Documentation links external to this repo

Anything pointing to file paths inside `vaadin-spreadsheet-flow-client`
(Vaadin docs, blog posts, customer training material) breaks. Out
of scope for this repo, but the team should sweep documentation
holdings before / after Phase 3.7.

## What this roadmap enables in the SHORT term

The roadmap doesn't change what slice 128 does (continue Phase
3.2). It does:

1. Make Phase 3.6 + 3.7 concrete, planned steps instead of
   "...and then we delete everything someday".
2. Give every future Phase 3.x slice a yardstick — "does this move
   us toward the elimination items above?". A slice that doesn't is
   harder to justify.
3. Document the verified facts (zero Java cross-imports, JS one-line
   integration, classpath-only dep) so future-Claude doesn't
   re-survey.
4. Identify Phase 3.6 acceptance criteria upfront: visual-regression
   IT green, comm-diff symmetric on events, no external classpath
   usage of client classes.
