# Systematic Comprehension & Refactoring Pipeline for Legacy Code

> **Purpose.** A general methodology for an **LLM agent + human operator pair** to
> understand a legacy "spaghetti" codebase in detail and refactor it into smaller,
> well-bounded pieces **without changing observable behavior**. The methodology is
> language-, framework-, and domain-agnostic: it applies to a Java component, a
> TypeScript module, a Python service, a Go binary, an embedded C codebase, or any
> other software with hard-to-track legacy logic. The signals the methodology uses
> (source code, comments, tests, types, git history, issue / PR discussions, runtime
> behavior, UI interaction, telemetry, domain experts) are likewise the universal
> signals available in any legacy codebase — though their availability per project
> varies (some have no tests; some have no documentation; some have thin git history).
> The methodology is designed to **make the most of whatever signals exist** and to
> degrade gracefully when one is missing.
>
> **Running example.** Throughout this document, the **Vaadin Spreadsheet** component
> (in `vaadin/flow-components`, with legacy history in `vaadin/spreadsheet`) is used
> to illustrate the methodology in action. Concrete examples appear in clearly
> labeled **Example** subsections; the surrounding methodology is general. The
> companion file `SPREADSHEET_REFACTOR_LOG.md` is the *journal* of the Spreadsheet
> execution — read it for the live state and lessons that are still incubating
> before they generalize back into this document. When you apply this methodology
> to a different codebase, start a new journal next to it; this file stays
> normative.
>
> **How to use.** Read fully before touching code. Where it makes claims about a
> specific codebase shape, treat them as **hypotheses to verify in Stage 0**, not as
> facts.

---

## Pipeline shape — five phases

This pipeline runs in five phases. Each phase has a different goal, a different
shape of work, and a different definition of "done." The phases run roughly in
order, with backtracking allowed when later phases surface gaps in earlier
ones. The numbered §0–§11 sections that follow are the detailed normative
content for **Phase 1**; Phases 2–5 are outlined in §12–§15.

### Phase 1 — Structural decomposition (god classes → focused helpers)

Goal: break god classes into focused single-purpose helpers using
field-locality clustering. Public API stays unchanged; helpers are
package-private; existing tests continue passing. This is the foundation:
without a decomposed structure, the later phases have no place to attach
their work.

**Done when:** every god class is either (a) decomposed to its
indivisibility threshold via Rule D's three-question check, (b) judged
unsuitable for slice-by-slice decomposition (state-machine pattern,
§13.6 of the `refactor-concerns` skill) and explicitly stopped, or (c)
mostly orchestration / public-API surface.

### Phase 2 — Behavior pinning (comprehensive tests across tiers)

Goal: for every functional block left after Phase 1 — each helper, each
remaining orchestrator method — produce a comprehensive test set that
pins what the code actually does, including bugs and quirks. Tests span
multiple tiers (unit, integration, UI via Playwright CLI, golden-master
payload, characterization, fuzz, snapshot). The output is a **coverage
matrix**: rows = functional blocks, columns = test tiers, cells =
covered / gap / N/A. The matrix must reach a defined coverage threshold
before Phase 4 is allowed to start. See §12.

### Phase 3 — Specifications (natural-language behavior contracts)

Goal: produce a natural-language specification per functional block:
inputs, outputs, side effects, invariants, edge cases, error modes,
non-obvious quirks (with "this is intentional" / "this is preserved-bug"
annotations). Specs are LLM-drafted from the code + tests + git history,
then human-reviewed for accuracy. They become the "contract" any later
architectural refactor must preserve. See §13.

### Phase 4 — Architectural refactor (free internal redesign, contract preserved)

Goal: with comprehensive tests (Phase 2) and explicit specs (Phase 3) in
place, restructure internal architecture aggressively — rename classes,
restructure data flow, change algorithms, swap implementations — while
keeping the public API surface and the spec contracts intact. The slice
unit changes from "extract helper" to "swap implementation." See §14.

### Phase 5 — Method decomposition (raise abstraction levels within blocks)

Goal: within each block, break big complex methods into smaller methods
at higher abstraction levels so the flow becomes human-understandable.
Same-class extraction; no new classes. Methods over ~50 LOC with two or
more sub-concerns get split into intent-revealing helpers. See §15.

### Cross-cutting concerns

- **§16 Repo-history mining.** Multi-repo git history, PRs, issues, and
  PR/issue comments are a high-signal source for reasoning, decisions,
  and architecture trails. Integrate at every phase via the
  `refactor-history` skill.
- **§17 Human checkpoint policy.** Where human input adds value vs where
  automation should run. The pipeline is designed to minimize human
  input — humans are gated to decision points only.

### How the journal interacts

The companion `SPREADSHEET_REFACTOR_LOG.md` accumulates slice-by-slice
records throughout all five phases. The journal's flavor framing
(decomposition / encapsulation / abstraction) extends naturally to all
phases:

- Phase 1 slices: decomposition / encapsulation / abstraction (per §10)
- Phase 2 slices: test-tier addition (one tier per slice typically)
- Phase 3 slices: per-block spec
- Phase 4 slices: implementation swap (preserving contract)
- Phase 5 slices: intra-class method extraction

The current journal contents are all Phase 1.

---

## 0. The one rule that governs everything

We are doing **behavior-preserving refactoring** of legacy code. The safety net is a
suite of **characterization tests** (a.k.a. golden-master tests) that assert what the
code **actually does today — bugs included** — not what it *should* do. Every
structural change is measured against that pinned baseline.

Corollaries, in priority order:

1. **Observed behavior is ground truth.** Test oracles come from *running the current
   code and recording its output*, never from an LLM's model of intent and never from
   JavaDocs/comments (treat those as untrusted hypotheses — legacy comments lie).
2. **The serialization / public API contract is sacred.** Anything that crosses the
   server↔client boundary, or that external consumers call, must remain byte/shape
   compatible. Changing it is *not* a refactor. The frozen contract list (see §2.6)
   names this surface explicitly.
3. **Small steps, green between each.** Refactor in tiny behavior-preserving moves;
   run the characterization suite after every one. Keep refactoring commits strictly
   separate from any behavior-change commits.
4. **Coverage is the safety boundary.** Code with no characterization coverage is
   "refactor at your own risk." Add tests (or a seam) first.
5. **Static analysis is unsound under reflection / framework dispatch.** Do not
   delete "dead" code based on a static call graph alone; confirm reachability with
   runtime data first.
6. **Tools produce facts; the LLM annotates and reasons over those facts.** Never let
   the LLM invent structure (class names, signatures, call edges) — derive structure
   from a parser/index/grep and validate every claim against it.
7. **The methodology itself is hypothesis-driven.** Initial assumptions about the
   codebase's shape *will* be wrong somewhere. When discovery contradicts the
   methodology, update the methodology (this file), not just the journal.
8. **A refactor does not expand the public surface.** Do NOT add new
   `public`/`protected` members to publicly-visible classes during refactoring.
   Every new public method becomes an obligation downstream code may bind to;
   widening the contract as a side effect of structural cleanup turns a refactor
   into a (silent) API change. Permitted instead: new **package-private**
   helpers on public classes; new **package-private classes** with whatever
   internal `public` API they need (they remain hidden by the class's own
   visibility); same-package access via existing `protected`/package-private
   members. If the refactor seems to require widening visibility on a public
   class, stop and surface the problem — there is almost always a same-package
   helper or an internal class that achieves the goal without API expansion.
9. **A refactor decomposes logic; it does not just decouple it.** Extracting an
   interface in front of a monolith hides the dependency direction but leaves
   the spaghetti behind it. The actual goal is to **break the logic into
   smaller single-purpose pieces** — each describable in one sentence
   ("holds the command stack," "applies a command's side effects"). A 250-line
   manager that becomes a 60-line coordinator + a 60-line stack helper + a
   60-line side-effect helper is a refactor. The same 250-line manager with a
   6-method interface in front of it is not — the next maintainer still has the
   250 lines to read. Interface extraction may be necessary *scaffolding* for
   the decomposition; it is not the destination.
10. **A slice extracts multiple small classes, not one big helper.** Within a
    single slice, look for **sub-concerns** in the chunk you are moving. If
    your proposed extraction is going to be a 250-LOC "new manager," that's a
    sign you've stopped at the first level of decomposition; the new class is
    a smaller god class. Look harder — it probably contains a data-structure
    concern, an operation concern, a serialization concern, a loading concern,
    etc. Carve those out as separate package-private classes in the same slice,
    each describable in one sentence. The orchestrator's old method becomes a
    thin delegator across several focused helpers. Aim for three to five
    focused classes per slice, not a single big rename. **Note: slices come in
    two flavors — *decomposition* slices (multi-step logic redistributed into
    focused helpers — typically 100s of LOC moved per slice) and
    *encapsulation* slices (a cluster of related fields with thin wrapper
    accessors moved into a state holder — typically ~20–50 LOC moved, the win
    is cohesion not LOC).** Both satisfy this rule; both satisfy rule #9. State
    which flavor you're doing in the plan, because the reviewer's expectations
    differ — an encapsulation slice that the operator sold as a decomposition
    slice will read as "barely any LOC moved, was this worth it?" Lead with
    the cohesion argument when it's an encapsulation slice ("the parent loses
    N independent state concerns").
11. **Iterate until nothing more is worth extracting.** A god class is not
    "done" after one slice. After each completed slice, ask the same
    question again on the same class: *"is any functional cluster of
    fields + methods still worth pulling out?"* If yes, plan and run
    another slice. Repeat until the answer is honestly no — which means
    the remaining content is (a) genuinely orchestration glue (lifecycle,
    constructor wiring, listener registration, framework dispatch),
    (b) tightly coupled state that resists further decomposition without
    behavior change, or (c) so small it is no longer a god class. **Group
    by field locality**, not just by method name: a cluster of fields that
    always move together is one concern, regardless of how many methods
    touch them. Field clusters reveal hidden structure that method names
    sometimes obscure. The loop terminates by inspection, not by hitting a
    LOC target — a 5,000-LOC class can still be the right end state if
    the remaining content is genuinely indivisible, while a 1,500-LOC
    class may still have a concern worth extracting. Re-run §5's candidate
    scoring after every slice — the easiest target changes as the surface
    shrinks.
12. **Triangulate every claim across multiple signals.** No single signal
    source is sufficient on its own. Source code shows *what is*, comments
    and javadocs show *what was once intended* (often wrong), tests show
    *what is exercised* (always partial), types show *shape constraints*,
    git history shows *change motivation* (when commit messages aren't
    "wip"), issue/PR discussions show *what users hit* (when project
    discipline is good), telemetry shows *what runs in practice* (when it
    exists), domain experts show *the invariants the code is supposed to
    preserve*, and the running application shows *the actual end-to-end
    behavior*. **Each is partial; together they are sufficient.** When two
    signals disagree, prefer the one closer to runtime: live behavior >
    tests > types > code > comments > history. The LLM's job is to gather
    the signals and reconcile them, not to invent answers from any one of
    them. **A signal being absent is information, not a blocker** — the
    methodology has fallbacks per signal (see §2.10 signal inventory and
    §7 patterns for the absent-signal cases).

---

## 1. The legacy pattern this pipeline targets

The pipeline is tuned for one recurring legacy shape: an **orchestrator class with a
web of managers**. Concretely:

- A central class (let's call it `Orchestrator`) acts as the entry point and lifecycle
  owner of a feature.
- It constructs a fleet of collaborators by passing **`this`** to each one, e.g.
  `new ManagerA(this)`, `new ManagerB(this)`, ...
- Each collaborator holds a back-reference to `Orchestrator` and reaches back into
  its package-private / protected state at will.
- The orchestrator may also have a separate **external data-model dependency**
  (a library it doesn't own — POI, ANTLR, an ORM session, a parser AST) whose
  invariants it has to keep in sync with both its own state and the wire/UI.

That gives you **bidirectional coupling across three boundaries at once**: manager ↔
orchestrator (in-process), orchestrator ↔ external data model, and process ↔ outside
world (network, IPC, serialization, UI).

### What "refactor into smaller pieces / public API only" concretely means here

The orchestrator class itself is the largest god class in this pattern; the
managers are smaller god classes. **Both** are refactor targets — the
methodology applies recursively to every class of god-class shape, not just
the satellites.

For the **orchestrator**: identify cohesive chunks of its body (each
describable in one sentence — "row/column sizing", "listener registration",
"workbook lifecycle", "sheet navigation", etc.) and move each chunk into a
new package-private helper or an expanded existing manager. The
orchestrator's `public`/`protected` API stays byte-identical (rule #8); its
methods become thin delegators. The 6,000-LOC monster shrinks slice by slice
as concerns are extracted; eventually it becomes a small facade.

For each **manager**: identify the manager's own distinct concerns (stack
storage, side-effect application, etc.) and decompose into smaller
single-purpose helpers — same move as above, one level down.

Convention: name the interface `<Manager>Host` (e.g. `SpreadsheetHistoryHost`,
`CellSelectionShifterHost`). It reads as "the part of the orchestrator that this
manager hosts on." Keep it package-private unless the manager is itself a published
type.

### Example — Vaadin Spreadsheet's shape

Vaadin Spreadsheet is a server-side Java component that renders/edits Excel files in
the browser, using **Apache POI** as its external data model. The orchestrator is
`com.vaadin.flow.component.spreadsheet.Spreadsheet` (~6,200 LOC). Its managers are
`CellValueManager`, `CellSelectionManager`, `CellSelectionShifter`,
`ContextMenuManager`, `SpreadsheetHistoryManager`, `SpreadsheetStyleFactory`,
`ConditionalFormatter`, plus several factories and handlers. POI is the external data
model. The "wire" is a custom DOM event + element-property surface (see §2.6 example).

### Recognized variants of the orchestrator pattern

You will meet one of these in the wild. Pick the right Stage 0 substrate accordingly:

| Variant | Shape | Implication for the pipeline |
|---|---|---|
| **Pure server, headless data model** | Orchestrator + managers, external lib (POI, parser, ORM), no UI. | Easiest. Server-side golden masters alone are the safety net. |
| **Server + thin UI shim** | The above, plus a small client (Lit / React component). | Add UI-payload golden masters; browser tests for rendering only. |
| **Server + preserved-legacy-engine UI** | The above, but the "client" is itself a frozen old engine (e.g. GWT-compiled JS) wrapped in a shim. | The legacy engine is **part of the frozen contract** — treat its bytes as immutable. Refactor only the server and the shim. The Vaadin Spreadsheet Flow port is this variant. |
| **Cross-process / RPC** | Orchestrator dispatches through a wire protocol (gRPC, MQ). | The wire-protocol schema is the contract; payload recording on both ends. |
| **Plugin host** | Orchestrator loads collaborators via reflection / SPI / `Class.forName`. | Rule #5 dominates — static call graphs are very unsound. Stage 2 (dynamic) becomes mandatory before any deletion. |

### Pre-discovery hypotheses for the running example

These are the things I assumed when I first wrote this doc. **Several were wrong.** I
list both so future readers see how cheap it is to be confidently wrong about a
codebase you haven't read yet:

| Hypothesis | Status after Stage 0 |
|---|---|
| Flow client is TypeScript/Lit | **Wrong.** It's a Lit *shim* hosting a GWT-compiled module (`spreadsheet-export.js`). The contract has three layers, not two (see §2.6). |
| Vaadin-8 SharedState + RPC types are gone | **Wrong.** `client/`, `shared/`, `rpc/` packages are preserved as DTOs for the GWT widget. |
| Build is Maven multi-module under `vaadin-spreadsheet-flow-parent` | Correct. |
| Working with the code requires a Vaadin commercial license | Correct; `Spreadsheet`'s static initializer runs a license check at non-production startup. |

If your first-pass mental model contradicts Stage 0, **update §1 of the methodology**,
not just the journal. The doc is a hypothesis; the codebase is the truth.

---

## 2. Stage 0 — Discovery (do this first; STOP when done)

Produce a written report — for the running example, this lives in
`SPREADSHEET_REFACTOR_LOG.md`; for any other codebase, start a new journal next to
the methodology. Answer every item below. **Do not proceed to any stage past 0, and
do not edit any production code, until the report is approved by the human operator.**

### 2.0 Signal inventory — what evidence do we have?

Before enumerating modules and tests, take a pass over **all available signal
sources** and mark each as plentiful / thin / absent. The methodology adapts to
what is available; missing signals route to fallbacks. Do not skip this step
even on a "well-documented" codebase — operators routinely overestimate how
much documentation is real.

| Signal | What it tells us | If thin or absent, fall back to |
|---|---|---|
| Source code itself | Ground truth of what *is*, but inverted for the *why*. | Always present; primary source. |
| Type signatures (in typed languages) | Shape constraints, allowed callers. | In dynamic languages, infer from runtime traces. |
| Inline comments + Javadoc/JSDoc/docstrings | *Past intent* — often stale, sometimes inverted. | Treat as untrusted hypotheses (rule #1). Replace with characterization tests. |
| External documentation (READMEs, wikis, design docs) | Architectural framing. | LLM-driven architectural inference from code + Stage-2 dynamic traces. |
| Existing unit tests | What is exercised + asserted. | Bootstrap server-side characterization tests (§3 Stage 4) by driving public API and capturing outputs. |
| Existing integration / E2E tests | What user-visible behavior is exercised. | Bootstrap LLM-driven Playwright scripts against the running app (§3 Stage 4). |
| Manual / interactive UI behavior | The ground truth users see. | Capture via LLM-driven browser exploration → freeze as replayable scripts. |
| Telemetry / production logs | What actually runs at scale; reachability. | If unavailable, rely on Stage-2 dynamic-analysis traces from running the test suite. |
| Git commit history | Why a change was made (when messages are real). | Hash + author + diff still tell *when* a chunk arrived; cross-reference with issue IDs found in code. |
| Ancestor / migrated-from repository | Pre-migration history of the same code. | §2.1 explicitly asks for this. |
| Issue tracker entries | Bug reports, original requirements, edge cases users hit. | If empty, mine community forums, Stack Overflow tags for the project, support tickets. |
| Pull-request descriptions + reviewer discussions | The *contemporaneous* explanation of a non-obvious change. | Often more useful than commit messages; GitHub / GitLab APIs expose them. |
| Domain experts (humans) | Invariants the code is supposed to preserve, history not on disk. | When unavailable, the methodology proceeds; the operator is the substitute. |
| Type-checker / linter output | Mechanical correctness signals. | Always available where the tools run. |
| Coverage / mutation testing data | Where the tests bite vs. don't. | §3 Stage 4 mandates wiring these up before any slice if not already present. |

**The pipeline never requires all signals to be present.** When a signal is
absent or thin, the methodology routes work to a substitute. The substitution
table above is the explicit fallback graph; §7 lists the patterns in more
narrative form.

### 2.1 Repo & module shape

- Exact path of the target module(s) and their build coordinates (Maven GAVs, npm
  package names, etc.).
- Build-graph siblings: which modules in the same repo are dependencies, which are
  reverse dependencies?
- **Ancestor repo, if any.** Was this code migrated from somewhere else? If yes,
  the *legacy* git history is in the ancestor repo, not here, and Stage 3's
  bug-history mining must consult both. Record the URL.
- Branch / commit you pinned the baseline to (record the SHA).

### 2.2 Build / test / dev-UI commands

Verify each one runs against this checkout. Record the exact, working command lines:

- Compile the server-side code.
- Build the frontend (if any).
- Run unit tests, scoped to the module only and to a single class.
- Run integration / browser tests, scoped to the module only and to a single test.
- Launch a dev / demo UI for manual + browser-automation testing.
- Stop the dev UI cleanly (kills, ports).
- Format / lint check.

### 2.3 Class / file inventory

- Full list of source files in the target module.
- The orchestrator class — name, LOC, what framework superclass it extends.
- Each manager / factory / handler — name, LOC, constructor signature, back-reference
  shape.
- Other supporting types (commands, actions, value objects, DTOs).
- A subset of the file list flagged "**generated**" — do not refactor these directly.

### 2.4 External-data-model touch-points

- Which files import the external library (POI / parser / ORM / etc.).
- Which of those **mutate** state in the external library (writes) vs only **read**
  (reads). Mutation-only files are the ones that risk corrupting user data.

### 2.5 License / auth prerequisites

- Are there commercial-only license checks at runtime?
- Are integration tests gated on a TestBench / Sauce / vendor license?
- Are any tests gated on network access (cloud APIs, registries)?
- Document how a fresh checkout would fail on a CI machine without these.

### 2.6 The server↔client (or server↔outside) boundary — the **frozen contract list**

This is the most important Stage-0 deliverable. The contract has up to four layers;
fill in whichever apply:

1. **Property surface** — every named field/attribute set on the wire (e.g. Flow
   element properties via `getElement().setProperty(name, value)`). Each entry
   names the wire-name and the language type.
2. **Method invocation surface** — every RPC / `callJsFunction` / `@ClientCallable`
   / gRPC method invoked across the boundary, with name and signature.
3. **Event / message surface** — every event/message type the other side may send
   back, with its discriminator strings or schema.
4. **Implicit engine surface** — if a preserved legacy engine (§1 variant 3) sits
   on the other side, list the set of API entrypoints the shim calls on it. Those
   are part of the contract too.

**Rule:** every entry on this list is immutable for the duration of the refactor.
Changing one is not a refactor.

### Example — Vaadin Spreadsheet's frozen contract (live)

See `SPREADSHEET_REFACTOR_LOG.md` §1.5 for the live, line-referenced version. In
summary:

- ~50 element properties (server → client via `setProperty`).
- 14 `callJsFunction` invocations (server → client RPC).
- 42 `spreadsheet-event` discriminator strings (client → server via a single
  `@DomEvent("spreadsheet-event")` decoded by `SpreadsheetHandlerImpl`).
- The GWT widget's `api.set…` / `api.update…` method names (implicit engine
  surface).

### 2.7 Existing test inventory + baseline state

- Count and list of unit tests; testing framework version.
- Count and list of integration / browser tests; testing framework version.
- **Test-baseline capture: run the unit suite once on the pinned commit and record
  the result** (tests / failures / errors / skipped). This is the green-line we will
  hold every subsequent step against.
- Same for integration tests *if licenses & infra permit*; otherwise flag the gap.

### 2.8 Serialization / session-state posture

- Does the orchestrator implement `Serializable` (Java) or any analogous
  framework-required serialization?
- Do the managers?
- Is there a `serialVersionUID` declared anywhere? `@SuppressWarnings("serial")`?
- **Implication.** If yes, any extracted interface introduced in Stage 5 **must
  extend `Serializable`** (or the language equivalent). A field whose static type
  changes from a serializable class to a non-serializable interface silently breaks
  session replication. Audit field-shape changes against this rule.

### 2.9 Stage-0 deliverable summary

A single document (the journal — e.g. `SPREADSHEET_REFACTOR_LOG.md`) containing:

- §2.1–§2.8 answers, with file:line references where useful.
- The **frozen contract list** (§2.6) explicit and reviewable.
- A proposed first slice with members-used set, interface design, and test plan
  (see §5 for the heuristic that picks the slice).
- An **open questions / blockers** section. Anything you couldn't determine must
  be visible to the human operator.

When this is ready: **STOP and ask for approval** before Stage 1.

---

## 3. The pipeline (Stages 0–6)

Build Stage 0 once, then walk a **single vertical slice** all the way through 1→6
before broadening. The first slice will expose gaps in normalization, metrics, and
the browser layer — far cheaper to learn on one slice.

### Stage 0 — Substrate

Tools produce verified facts; store them so later stages query rather than re-derive.

Pick a **mode** based on codebase scale:

- **Lightweight mode** (~one component, ≤ ~200 source files). The LLM's context
  window plus `grep` / `find` / `Read` over the journal is the "graph." This is what
  worked for Vaadin Spreadsheet's Stage 0.
- **Heavyweight mode** (~whole monorepo, > ~1K source files). Index with a
  per-language indexer (see §8), compute a real call graph, persist it (SQLite/JSON
  to start; Postgres + pgvector if you also want semantic search over comments).

Both modes must produce the same set of **facts** even if the storage differs:

- **Class / function symbol table.** Per-ecosystem tools: scip-java + IntelliJ
  index (Java); ts-morph / `tsserver` (TS); pyright `--outputjson` (Python);
  `gopls` (Go).
- **Package / module dependency graph.** Tools: `jdeps` (Java); madge or
  dependency-cruiser (TS); pydeps (Python); `go mod graph` (Go).
- **Call graph** per class, per method (who-calls-who). Java: Spoon or
  OpenRewrite LST. TS: ts-morph walker. Python: pycg. Go: `go callgraph`.
  This is what tells you "is this method's only caller a sibling I can
  refactor with confidence" — answering it from a grep is unreliable.
- **The orchestrator ↔ manager bidirectional edges** named explicitly.
- **The contract nodes (§2.6) flagged immutable** in the graph; the slice
  picker filters them out.
- **Per-class cyclomatic complexity** and **per-class LCOM** (Lack of
  Cohesion of Methods). A high-LCOM class is mechanically a god class —
  the metric is a first-pass slice-picker signal (high LCOM = many
  unrelated methods that probably split into multiple concerns).
- **Per-class churn from git history.** Include the ancestor repo if the
  code was migrated (§2.1). `git log --numstat -- <file>` aggregated per
  file; rank by sum of insertions + deletions.
- **Coverage data** if a green test suite exists — Stage 4's required gate
  is per-class line + branch coverage. Without it, slice picks are
  speculative.

The output is a queryable dataset (SQLite, JSON, or just a markdown table in
the journal at the lightweight scale). Subsequent stages **query** rather than
re-grep — the LLM's job past Stage 0 is reasoning over facts, not
re-extracting them.

### Stage 1 — Comprehension (parallel, tool-anchored)

- **One analysis pass per god class — the orchestrator AND each manager.** Treat
  every class above a size threshold (start at ~400 LOC) as a refactor target.
  For each, produce a list of **distinct cohesive concerns** in the class, one
  sentence per concern. These are the slice candidates.
- For the orchestrator specifically, the concern list is typically long — load
  / save, sheet navigation, listener registration, row/column sizing, property
  wiring, merged regions, hyperlinks, comments, embedded components, etc. Each
  is its own potential slice.
- For each manager, enumerate **exactly which orchestrator fields/methods it uses**
  — this is the implicit collaboration surface. Treat it as a table:
  member, kind (field / method), used-at file:line, read-or-write.
- Look for **package-private field writes from sibling classes** — these are the
  hidden coupling that the visible method-call analysis misses. For each, plan a
  setter to introduce in Stage 5.
- Propose target module boundaries, **validate against coupling metrics** (LCOM on
  the orchestrator, efferent coupling per manager). Metric improvement is the
  stopping criterion for "granular enough."
- **UML outputs — generated from the index, never invented.** The pipeline
  uses three diagram kinds, all expressed in **PlantUML** or **Mermaid**
  (text formats so they live in the journal and diff cleanly):
  - **Class diagram per god class** (before refactor): every method/field
    on the class, with arrows for which other classes it touches. Surface
    the size of the class and the breadth of its dependencies visually.
    Generate with `plantuml-dependency` (Java) or by walking the symbol
    index produced in Stage 0.
  - **Component diagram of proposed boundaries** (after-refactor target):
    every helper class as a box, edges for collaboration, with the public
    API at the orchestrator boundary explicitly marked. This is the
    "public-API-only" target made concrete; reviewing it with the human
    operator catches design holes before code is written.
  - **Sequence diagram per slice's key use case**: the user action that
    flows through the concern being extracted (e.g. "user clicks merge
    cells → POI add → cascade style → element-property serialization").
    Drawn from a Stage-2 dynamic trace, not invented. Pins the call
    sequence the helper classes will reproduce.
  - **State machine** for any lifecycle that hides state across calls
    (cell edit / selection / formula-entry, for the running example).
  - **Storage**: text under `<journal>/diagrams/` so they're versioned
    alongside the code. The LLM regenerates them at the start of each
    slice; the human reviews the diff.

### Stage 2 — Dynamic analysis (find what static reading can't)

- Run the demo UI and existing integration tests under **JaCoCo in execution-data
  mode** for true coverage and reachability (or the language-equivalent, see §8).
- Trace orchestrator ↔ external-data-model interactions.
- Trace process ↔ outside-world round-trips.
- **Reconcile static vs dynamic.** Frameworks (Flow, Spring, JAX-RS, etc.) dispatch
  handlers through reflection or annotation processors that static call graphs miss.
  Anything static-dead but runtime-hit is framework / reflection coupling and a
  **dead-code-removal trap** — flag, don't delete.
- **Output:** real sequence diagrams from traces (e.g. cell edit → client → handler
  → `CellValueManager` → POI mutation → update cycle → payload back to client);
  confirmed dead-code candidates with the dynamic evidence attached.

### Stage 3 — Behavior model (use cases + edge cases via signal mining)

- **Enumerate the user-visible features.** For each feature, name what it does
  in one sentence; this list anchors Stage 4's test plan.
- **Mine every available signal** (§2.0 inventory) for edge cases. Each source
  contributes; none alone is enough.
  - **Commit history** for the file or directory: scan messages and diffs for
    bug-fix language ("fix", "regression", "edge case", "crash", locale, NPE,
    memory). Each historical fix is an edge case the methodology must preserve.
    For migrated codebases, mine *both* the current repo and the ancestor repo.
  - **Issue tracker** entries that mention the file, the feature, or its public
    API: read the *reproduction steps* — those are edge cases waiting to be
    pinned. Even closed issues with "wontfix" are useful (they document
    deliberate behavior).
  - **Pull-request descriptions and review discussions** for past changes to
    this code: reviewers often ask "what about X?" and the answer becomes a
    one-line behavior commitment. Mine those threads.
  - **External community signal** (Stack Overflow, forums, Discord): when the
    issue tracker is thin, third-party Q&A often has the real edge cases users
    hit. Search for the project name + the file/feature name.
  - **Property-based testing / fuzzing** over randomly generated inputs: drive
    the public API with randomized inputs and snapshot whatever doesn't crash.
    The snapshots characterize edge-of-spec behavior even when there are no
    written tests.
  - **LLM enumeration** of plausible edge cases (boundary values, empty / huge
    inputs, locale-dependent paths, Unicode, malformed inputs): the
    lowest-confidence source — always cross-check against the others.
  - **Domain-expert interviews** when humans are available: ask "what's the
    weird thing about this code that I wouldn't see by reading it?" Two
    questions of an expert usually beats a week of code archeology.
- **Output:** a use-case list, each linked to implementing code + traces + (next
  stage) tests — a traceability matrix. For each user-visible feature, the
  matrix shows which signals contributed evidence, so absent signals stay
  visible and can be re-mined later.
- **If history / issues / PRs / experts are all thin or absent**, lean
  harder on Stage 2 (dynamic analysis: trace the running app) and on the
  LLM-driven browser exploration in Stage 4. The methodology degrades to
  "what the running application currently does" as the sole oracle. That is
  still a viable refactor base — observed behavior IS ground truth (rule
  #1) — but the edge-case net will start coarse and tighten as new
  surprises are uncovered.

### Stage 4 — Characterization tests (the safety net)

- **Server-side golden masters (primary, when feasible).** If the component is
  usable headless, build it over a small input, drive it through public API, and
  capture both the resulting **external-data-model state** and the **serialized
  payloads** that would cross the contract boundary. Those payloads are the
  contract — pinning them pins client / consumer compatibility without a browser.
  Normalize nondeterminism (timestamps, hash-driven map iteration order, transient
  framework fields, allocation addresses) before freezing.
  - **For frameworks where the contract crosses a recordable boundary** (Flow's
    `Element.setProperty`/`callJsFunction`; gRPC client stubs; HTTP request
    builders; DOM mutations; logging APIs): build a **recording proxy** around
    that boundary that captures the *ordered sequence* of calls. This is the
    byte-shape of the contract; differential testing in Stage 6 diffs these
    sequences. The recording-proxy pattern is the cheapest possible
    characterization for any code whose output crosses a method-call interface.
- **Coverage / mutation gate.** Wire up the language's coverage tool (JaCoCo,
  c8, coverage.py, `go test -cover`, etc. — see §8) and a mutation-testing tool
  if one exists for the language (PIT for Java, Stryker for JS/TS, mutmut for
  Python, etc.). Mutation score tells you whether the golden masters actually
  *detect* changes. Green slices are safe to refactor. If no mature mutation tool
  exists, substitute very-aggressive property-based testing on slice boundaries.
- **LLM-driven E2E layer.** Point a browser-automation tool (Playwright for web;
  equivalent for native apps — Appium, Maestro, etc.) at the running app.
  - *Authoring mode (against current code):* the agent explores each use case
    interactively and **freezes replayable scripts** plus golden DOM/UI
    snapshots, screenshots, and network/IPC payloads.
  - *Verification mode (every run):* scripts replay deterministically with
    **hard DOM/network assertions** as the pass/fail gate; the LLM only
    **adjudicates ambiguous visual diffs**, it is not the raw gate.
  - When the view is canvas / custom-rendered, lean on network-payload
    assertions (already golden-mastered via recording proxy) and use visual
    diff for rendering.
- **Playwright CLI sanity battery (the per-slice gate).** The full IT / E2E
  suite is slow (minutes to hours). Between slices we want a *fast* gate that
  catches "I broke the rendered UI" within seconds. Split the Playwright
  layer into two tiers:
  - **Full E2E suite** — exhaustive coverage of each user-visible feature.
    Slow. Runs at end-of-batch.
  - **Per-feature sanity script** — ~20–60 LOC each, smoke-tests one
    feature: open the app at a known fixture, perform three or four
    deterministic actions, assert two or three load-bearing DOM/network
    states. Runs in seconds.
  - Authoring of a sanity script: use the Playwright MCP (or `npx playwright
    codegen`) to capture actions while the LLM agent drives the UI, then
    freeze the captured script into the repo. One script per concern named
    in §3 Stage 3's use-case list.
  - Replay of a sanity script: `npx playwright test path/to/<feature>.spec`
    in headless mode. Exit code is the gate. The LLM parses the JSON
    reporter output (`--reporter=json`) for failure context. Visual diff is
    optional and LLM-adjudicated; the hard gate is DOM + network
    assertions.
  - **Selecting which scripts to run per slice.** Each slice's plan names a
    "concerns touched" set. The per-iteration loop runs only the sanity
    scripts tied to those concerns (plus a tiny smoke set that always
    runs — load the app, navigate to one default view, assert app
    initialised). Five scripts in 30 seconds beats fifty scripts in 10
    minutes for the per-slice case.
  - **When the IT environment is broken locally** (selenium drift, Chrome
    version mismatch, etc. — see §7), the Playwright sanity scripts are
    often *still* runnable because Playwright bundles its own browser
    binaries. Keep this tier alive even if the heavyweight IT suite is
    red.
- **Bootstrap path when there are no existing tests at all.** A surprisingly
  common starting state. Sequence:
  1. Get the app running locally (or in a container). Lock dependencies and
     fixtures (Docker, lockfiles, sample data) so the "current behavior" is
     reproducible.
  2. Stand up the recording proxy at every external boundary you can reach
     (logs, network, DB calls, file writes, framework dispatch points).
     Capture a session of varied input.
  3. Replay the session against the same build; assert that the recorded
     output matches. That is your first characterization test, written from
     observed behavior alone.
  4. Add LLM-driven Playwright (or equivalent) authoring mode against the UI
     for each use case enumerated in Stage 3.
  5. With this minimal net in place, do **one tiny refactor as a calibration
     run**: rename a method, run the suite, verify it's green. If green:
     proceed to a real slice. If a noise diff appears: tighten the
     normalization in step 2 first.
- **Tests over the entire test pyramid count.** Unit tests pin internal
  behavior, integration tests pin module-to-module behavior, E2E tests pin
  end-to-end behavior. Each level catches a different class of regression;
  the safety net is the union, not any single tier.

### Stage 5 — Refactor loop (per slice)

- **Pick the slice** by §5's heuristic.
- **Plan the decomposition first.** Before touching code: describe the
  intended end state as a list of 2–4 single-purpose classes, each summarizable
  in one sentence ("holds the bounded command stack," "applies a command's
  side effects to the spreadsheet"). If you can't, you don't have a refactor
  plan, you have an interface-extraction plan — go back to Stage 1 and re-read
  the manager looking for distinct concerns rather than just "members used."
- **The repeating move is logic decomposition, not interface extraction.** The
  earlier wording in this section talked about extracting a narrow interface
  in front of the manager. That move is *scaffolding* — useful when the
  decomposed helpers can't easily access orchestrator state directly. It is
  not the whole refactor. See §0 rule #9.
- The repeating sequence per slice:
  1. Confirm characterization coverage (Stage 4) is in place — both
     state-based tests *and* the payload-recording test if a contract layer
     is involved. These tests must pass against both the pre-refactor and
     post-refactor trees (§6).
  2. From Stage 1, list the manager's **distinct concerns** (e.g. stack
     storage, side-effect application, public-API surface). One target class
     per concern.
  3. **For the slice as a whole, plan multiple small helpers — not one.** A
     slice that ends with one new 300-LOC class has only relocated the
     spaghetti. Look for sub-concerns within the chunk: a data-structure
     concern (what's stored), an operations concern (what mutates it), a
     loading/refresh concern (how it gets populated from the external data
     model), a serialization concern (what gets sent to the client). Each
     becomes a separate package-private class. The orchestrator's old method
     becomes a thin delegator across the new helpers, not a delegator to a
     single new class. See §0 rule #10.
  4. For each new helper class:
     - Declare it **package-private**.
     - Make its methods whatever visibility is convenient (`public` is fine on
       a package-private class — the class itself is the access boundary).
     - If `Serializable` (or the language analogue) is in play on the
       orchestrator, the helper should also be serializable.
     - Use **same-package access** to the orchestrator's existing
       `protected`/package-private members rather than adding new
       `public`/`protected` methods to the orchestrator. See §0 rule #8.
  5. Refactor in tiny steps, suite green between each:
     - Step A: Add the new helper class(es), empty or with no callers.
     - Step B: Move one cohesive chunk of logic from the manager into a
       helper. The manager's existing method becomes a thin delegator.
     - Step C: Repeat until the manager's logic is split.
     - Use IntelliJ's *Move Method* / *Extract Class* for the moves
       themselves — behavior-preserving by construction.
  6. **Preserve the manager's existing `public`/`protected` API.** Methods
     and fields that subclasses might rely on stay where they are; their
     implementations may now delegate to the helpers. Adding a thin
     delegating body to a `protected` method is API-preserving.
  7. Run the suite green between every step. Separate refactor commits from
     any behavior-change commits.

### Stage 6 — Verification / acceptance (per slice)

- Full characterization suite green.
- **Differential testing.** Run old vs new over the same inputs and diff **both**
  the external-data-model state and the recorded contract payload sequence. Byte-
  identical payloads (after normalization) is the strongest "didn't break it"
  signal we can produce without a browser.
- Re-run mutation testing on the changed code to confirm tests are still meaningful.
- Check performance / memory (a known fragile area for the running example).
- Human review, then merge.

---

## 4. Working agreement (how the LLM agent should operate)

- **Prefer IDE refactorings over free-text edits for structural moves.** *Extract
  Interface*, *Move Method*, *Rename*, *Extract Class* are behavior-preserving by
  construction in IntelliJ (and equivalents in other IDEs); free-text edits are not
  and tend to smuggle behavior changes. When a step is an IDE refactoring,
  **describe the exact IDE action for the human to perform** (menu path +
  parameters), then verify the result. Reserve direct edits for glue code, tests,
  and analysis artifacts.
- **Never change the frozen contract list (§0 rule 2, §2.6) without explicit
  approval.** If a refactor seems to require it, stop and surface it.
- **Always keep the characterization suite green.** If a step turns it red, the
  change is either wrong or revealed a pre-existing behavior the tests now pin —
  investigate before proceeding.
- **One slice at a time, narrated.** Show the members-used set, the proposed
  interface, and the diff plan before extracting.

### STOP gates — moments where the agent must wait for human approval

1. **After Stage 0 (discovery report).** Before any analysis past discovery, before
   any test authoring.
2. **After the first slice's test plan and interface design.** Before writing the
   characterization tests.
3. **After the characterization suite for the first slice is in place.** Before any
   production-code refactor.
4. **Before any change that would touch the frozen contract list (§2.6).** Always.
5. **Before deleting any code that looked statically dead.** Confirm via Stage 2
   evidence first.
6. **Before broadening from one slice to the next.** §9's per-iteration
   verification loop must complete (all gates green) first.
7. **When the per-iteration LLM code-reviewer (gate #9) flags a behavior change
   the operator can't explain.** Don't wave it off — investigate, then either
   re-plan the slice or document why the finding is a false positive.
8. **When a verification gate becomes flaky.** Don't loosen the gate to clear
   the loop; surface and fix.

The list is intentionally narrow; everything else is fair game for the agent to do
autonomously *within a slice* — including writing tests, capturing baselines,
proposing refactor sequences, running the suite, and committing in small steps.

### When the methodology itself is wrong

Discovery and the first slice *will* surface places where this document is wrong
about the codebase, the tools, or the procedure. When that happens:

- Record the finding in the journal (§6 of `SPREADSHEET_REFACTOR_LOG.md`).
- **Propose a patch to this file** that fixes the methodology, not just the
  finding. If the operator approves, land the patch.
- Add a one-line entry to §11 (Revision history).

---

## 5. Picking the first slice — a heuristic, not a name

The slice candidates after Stage 1 are: every distinct cohesive concern found
in the orchestrator, plus every god-class-shaped manager (and within each
such manager, its distinct concerns). A "slice" is moving ONE such concern
into a focused helper. Some slices live inside the orchestrator (e.g.
"extract row/column sizing into `RowColumnSizingManager`"); some are inside
a manager (e.g. "extract the bounded stack from `SpreadsheetHistoryManager`
into `CommandStack`").

Score each candidate on:

- **Size.** LOC, distinct public methods. Smaller is better.
- **Coupling to orchestrator.** Count of distinct orchestrator members touched.
  Smaller is better.
- **External data-model touch.** Does it directly mutate the external library, or
  only orchestrate higher-level intent? Indirect is better.
- **Position relative to the frozen contract.** Does it directly produce or consume
  contract payloads? Far from the contract is better.
- **Coupling to other managers.** Does it call several siblings? Solo is better.
- **Blast radius of a bug.** If we break this, what's the worst observable failure?
  Reversible / visible is better than silent / corrupting.
- **Existing coverage.** Already-tested is better, *but* if a small manager has no
  tests it's cheap to add some — don't over-weight this.
- **Churn × coupling (only when ranking ≥ 5 managers).** This is the
  classic refactor-priority signal; it dominates only once the obvious "smallest +
  least coupled" win has been spent.

Pick the candidate that maximizes most of those, **except** anything in the
orchestrator ↔ external-data-model hot path (the value/style/edit-cycle code
in this kind of component is the entangled heart — leave it for last). The
first slice should be a *peripheral* concern you can refactor as a warm-up.
Subsequent slices walk inward; the entangled heart is the last thing to
touch, when the safety net is dense enough to catch any small change.

### Example — Vaadin Spreadsheet

The heuristic, applied to the 7 managers in Stage 0:

| Manager | LOC | Members touched | Direct POI writes | On contract path | Slot |
|---|---|---|---|---|---|
| `SpreadsheetHistoryManager` | 247 | 5 | No (replays via `Command`) | No | **slice 1** |
| `CellSelectionShifter` | 890 | ~12 | Yes | No | slice 2 |
| `ContextMenuManager` | 339 | 8 | No | No | slice 3 |
| `CellSelectionManager` | 843 | many | Indirect | Indirect | slice 4 |
| `SpreadsheetStyleFactory` | 1107 | many | Yes (cell styles) | Yes (`cellStyleToCSSStyle`) | later |
| `ConditionalFormatter` | 883 | many | Yes | Yes (`conditionalFormattingStyles`) | later |
| `CellValueManager` | 1476 | huge | Yes | Yes (everything) | **leave for last** |

So slice 1 in this codebase is `SpreadsheetHistoryManager`. The doc previously
guessed "either history or shifter"; the heuristic rejects the shifter as 3× larger
and a direct POI mutator. The journal records the slice-1 plan (members used,
interface, test plan, refactor steps) under §2.

---

## 6. Definition of done (per slice)

**Operational gate** (mechanical): every gate in §9's per-iteration
verification loop is green. The loop is the gate; if it passes, the slice
is mechanically done.

**Structural conditions** (semantic — checked at slice review):

- **The manager's logic is decomposed** into smaller single-purpose classes,
  each describable in one sentence. Adding an interface in front of a
  still-monolithic class does not count (rule #9).
- **No public-API expansion** (rule #8). The slice's public/protected
  surface is byte-identical to the pre-slice state, or strictly shrunk.
- **Multiple helpers per slice**, not one big rename (rule #10).
- **Iteration step recorded**: was this slice's concern still worth
  extracting given what's already done? If the answer was "no" before the
  slice started, the slice shouldn't have happened (rule #11).
- New helper classes are package-private and (where applicable)
  `Serializable`.
- No change to the frozen contract list (§2.6).
- Performance / memory not visibly worse on at least one large-input
  scenario (relevant for memory-fragile components).
- Refactor commits are isolated from any behavior-change commits; commit
  history reads as a sequence of small, individually green moves.
- The journal's activity log records what was done and what was learned.

---

## 7. Patterns and pitfalls observed in real codebases

These are things the methodology learned by being applied. Add to this section
whenever a slice surfaces a new pattern that future slices (or future codebases)
will also meet.

- **Preserved legacy engine beneath a shim.** A "modern" component may host a
  frozen older client engine (Vaadin Spreadsheet Flow port: a Lit shim around a
  GWT module). The legacy engine is part of the contract, not part of the
  refactor.
- **Package-private field writes from sibling classes.** Stage 1's
  "members-used" enumeration must include field writes, not just method calls.
  Plan a setter as the first step of the slice's refactor sequence.
- **Static collaborators.** Static utility calls (`Factory.logMemoryUsage()`) are
  outside the extract-interface flow. They aren't broken coupling; document them
  per slice but don't try to abstract them.
- **Reflection / framework dispatch.** Anything reachable only from
  `@DomEvent` / `@ClientCallable` / `@RequestMapping` / `@EventListener` / etc. is
  static-dead-looking but runtime-live. Rule #5 dominates.
- **License-protected static initializers.** A `static {}` block that calls a
  license checker can throw at component construction time during testing,
  silently failing whole test classes. Plan around it: set the deployment-mode
  flag, or stub the checker.
- **Migrated-from-elsewhere git history.** `git log` in the current repo only
  shows post-migration commits. The legacy history's signal (bug fixes, original
  comments) lives in the ancestor repo. Stage 0 must name it; Stage 3 must mine it.
- **`@SuppressWarnings("serial")` everywhere.** A codebase that suppresses the
  serial warning instead of declaring `serialVersionUID` is implicitly assuming
  that field shapes never change. Stage 5's interface extraction is one of the
  field-shape changes — audit explicitly.
- **Recording-proxy as the contract test.** When the contract is "what arguments
  were passed to an `Element.setProperty` / `callJsFunction` API in what order,"
  the cheapest test is a recording proxy that captures the sequence. This works
  for any framework where the wire is a method-call sequence; it's particularly
  cheap in Flow.
- **The IT suite may be unrunnable on the operator's machine; that doesn't make
  it useless as a gate.** Browser ITs frequently have environment-specific
  failures unrelated to the refactor (Chrome version drift, headless rendering
  flakes, machine-specific Selenium timeouts, Mac vs Linux differences). When you
  run them post-refactor and see a wall of red, do NOT conclude that the refactor
  broke them. Run the same suite against the pre-refactor tree (stash + re-run
  pattern) and *diff per-test status + message*. If the failure pattern is
  identical, the refactor changed nothing; the failures are pre-existing
  environment problems and the IT layer is still a meaningful gate — it just
  isn't a *green* gate today. Record the pre-existing failures as a separate
  concern, don't let them block slice completion.

### Patterns for sparse-signal codebases

- **No unit tests exist.** Don't write speculative tests "from the
  specification" — there usually isn't one, and the LLM will invent
  behavior that doesn't match reality. Instead use the Stage 4 bootstrap:
  stand up a recording proxy at every external boundary, replay a session,
  diff. The first "test" is byte-identical replay of one observed run. Add
  more sessions covering more use cases. Coverage grows as you slice.
- **No documentation, only code.** Treat code as the only normative
  source. Run static analysis to extract symbols and call graphs (§8 per
  ecosystem). Run dynamic analysis to find what the code actually does at
  runtime (Stage 2). Compose the two into a generated map of the system —
  that map becomes the missing documentation, owned by the journal.
- **Git history is thin or rewritten** ("squashed commit of N changes",
  "initial commit", `wip`-only messages). Hash + author + date are still
  useful for *chronology* (which chunk arrived in which release). Cross-
  reference commit dates with the issue-tracker timeline: a bug filed two
  days before a commit landed is probably the reason for that commit, even
  if the commit message says nothing. Use this signal when you can; accept
  its absence and lean on Stage 2 dynamic analysis when you can't.
- **Issue tracker is thin or empty.** Search external sources: project's
  Stack Overflow tag, GitHub Discussions, community Discord/forums.
  Third-party Q&A is often where the real edge-case reports live for
  open-source projects whose maintainers don't triage actively.
- **No domain expert available.** The methodology proceeds without one;
  the operator (you, the human) is the substitute. When you have a "why
  does the code do this?" question that no signal answers, log it as an
  open question in the journal and proceed with characterization-based
  pinning. Sometimes the question stays open for the entire refactor;
  that's fine — you didn't change the answer.
- **The "preserved-legacy-engine" pattern.** A modern component may host a
  frozen older engine beneath a shim (a Lit element wrapping GWT-compiled
  output; a TypeScript module wrapping bundled WASM; a Go binary calling
  a vendored C library). The legacy engine is part of the contract — its
  bytes are part of rule #2's frozen surface. Refactor the shim and the
  server side; do not touch the engine.

---

## 8. Per-ecosystem substrate (appendix)

§3 Stage 0 talks about a "graph of verified facts" without saying how to produce it
in any specific language. This appendix names the equivalents per ecosystem so an
agent picking up the methodology against a non-Java codebase isn't guessing.

In every ecosystem we want, at minimum: (a) a symbol index, (b) a call graph, (c) a
dependency graph at the package/module level, (d) churn from git, (e) a coverage
tool, (f) a mutation testing tool. "Good enough" is sometimes a pile of CLI tools
glued in scripts.

### Java

- Symbol index: **scip-java** or IntelliJ's own index (queryable via the IDE).
- AST + type resolution: **Spoon**, **JavaParser** (the codebase already pulls in
  `javaparser-core`), or **OpenRewrite**'s LST.
- Dependency graph: **jdeps** for compiled classes; **`mvn dependency:tree`** for
  Maven module graph.
- Coverage: **JaCoCo**.
- Mutation: **PIT**.
- Churn: standard `git log --numstat`.
- For migrated codebases, run the churn pass against the *ancestor* repo too.

### TypeScript / JavaScript

- Symbol index: **`tsserver`** (programmatic API) or **ts-morph** (high-level
  wrapper).
- Call graph: ts-morph + a small graph walker, or **`@typescript-eslint`** rule
  walkers for narrow questions.
- Dependency graph: **madge** for ES modules; **dependency-cruiser** for stricter
  rules + visualization.
- Coverage: **c8** / **nyc** / **istanbul**.
- Mutation: **Stryker**.
- Churn: same git workflow.

### Python

- Symbol index: **pyright** (in `--outputjson` mode) or **jedi**.
- Call graph: **pycg** for static graphs; **py-spy** for runtime profiling.
- Dependency graph: **pydeps**; **pipdeptree** for package-level.
- Coverage: **coverage.py**.
- Mutation: **mutmut** or **cosmic-ray**.
- Churn: same git workflow.

### Go

- Symbol index: **gopls** (LSP API) for symbols and references.
- Call graph: `go callgraph` from **golang.org/x/tools** (algorithms: pta, cha, rta).
- Dependency graph: `go list -m all`; `go mod graph`.
- Coverage: built-in `go test -cover`.
- Mutation: **go-mutesting** (less mature than the Java / JS options).
- Churn: same git workflow.

### Other ecosystems

The pattern, language-agnostic:

1. Use the language server's index for symbols + references.
2. Use a dedicated tool (or a static analyzer's reachability rule) for the call
   graph.
3. Use the package manager's native dependency-listing command for the module
   graph.
4. Coverage and mutation tools vary widely; if mutation testing isn't available,
   substitute very-aggressive property-based testing on the boundaries of the
   slice (rule #4 still applies: coverage *plus a signal that the tests detect
   changes* is the safety boundary).

---

## 9. Per-iteration verification loop

Every slice — between the last test edit and "OK, move on" — runs the same
**verification battery**. The battery is the operator's contract with the
methodology: if every gate is green, the slice is behavior-preserving and
API-stable. If any gate fails, the slice is incomplete; do not move on.

The gates, in order of cost (cheap first):

1. **Compile.** Trivial gate. If this fails the rest are moot.
2. **Spotless / formatter check.** Catches "I accidentally indented
   wrong" before it shows up in the diff review.
3. **Linter / static analyzer.** Checkstyle, ESLint, ruff, etc.
4. **Unit test suite.** The characterization tests (Stage 4 server-side
   golden masters) must stay green. If they go red, the refactor either
   broke behavior or revealed a behavior the tests now pin —
   investigate; do not paper over.
5. **Coverage delta.** Slice-touched class's line + branch coverage did
   not drop. Tracked by JaCoCo (Java), c8 (JS), coverage.py (Python),
   etc.; baseline captured before the slice, compared after.
6. **Mutation score delta** (when wired). If PIT / Stryker / mutmut is
   set up, the mutation score on the slice's class did not drop. This
   confirms the tests still *detect* changes, not just exercise lines.
7. **Static-analysis-metric delta.** Per-class LCOM and per-method
   cyclomatic complexity should be **stable or better** after the slice
   on the *original* god class (the extraction should improve cohesion).
   Track in the journal; a metric regression on the orchestrator means
   the extraction created a worse abstraction than the original
   inlining.
8. **Playwright CLI sanity battery for the touched concerns** (§3
   Stage 4 sanity tier). Headless, headed only when adjudicating a
   visual diff. Seconds, not minutes.
9. **LLM code-review pass** (§10 agent orchestration). Run a fresh
   code-reviewer agent against the slice's diff. The agent has not
   seen the planning; it must derive intent from the diff alone. Its
   job: surface behavior changes the operator missed, missed
   callers, accidental API expansion, inconsistent style. Adjudicate
   findings; rerun the gate after fixes.
10. **Differential payload check** (Stage 6, optional per slice — but
    mandatory when the slice touches a class on the contract path
    §2.6). Stash the slice's production changes, replay the
    payload-recording test, diff the byte-shape sequence. Identical =
    contract preserved.
11. **Full IT / E2E suite** (end-of-batch, not per slice). Slow tier.
    Runs less often; result diffed against a pre-slice baseline.

Treat the loop as **structurally automatable**. A small shell or
`Makefile` target `verify-slice` running gates 1–9 in sequence is the
right shape — each gate's exit code feeds the next. The operator's job
is to read the failures, not to run the gates.

**When a gate is flaky**, fix the gate (suppress the flake, fix the
test, fix the linter rule), don't loosen it. Flaky gates train the
operator to ignore the loop, which defeats the safety net.

## 10. Agent orchestration

A single LLM agent doing every step serially is the slow path. Several
of the above stages and gates are independent and can fan out, and
several of the operations the agent does could equally well be done by
a specialized sub-agent with a different system prompt. The methodology
is designed to **leverage parallelism and specialization** when the
underlying agent framework supports them.

### Parallel sub-agents (independent work)

When two pieces of work have no data dependency, fan them out into
parallel sub-agents and reconcile their reports.

- **Stage-0 / Stage-1 enumeration.** One sub-agent per god class:
  the Explore agent reads the class, enumerates its concerns,
  returns a markdown report. Three god classes → three concurrent
  Explore agents → coordinator agent merges results.
- **Per-feature sanity-script authoring** (Stage 4). Each
  feature-script is independent; spawn one author agent per
  feature.
- **Per-slice signal mining** (Stage 3). One agent searches the
  issue tracker, another mines PR descriptions, another walks
  commit history — all for the same slice's concern. Merge findings
  in the journal.
- **Differential payload runs.** When checking that a refactor is
  byte-shape preserving against multiple scenarios, fan out one
  agent per scenario.

The reconciliation step is non-trivial: the coordinator agent reads
each sub-agent's report and decides what's actionable. Sub-agents
return *raw findings*; the coordinator does the synthesis.

### Specialized sub-agents (different responsibilities)

When the same artifact would benefit from being looked at through a
different lens, spawn a specialized agent for that lens.

- **Code-reviewer agent** — sees the diff but not the planning
  conversation. Job: catch behavior changes, missed callers,
  accidental API expansion, style drift. Returns a list of findings
  ranked by confidence. Run as gate #9 of the per-iteration loop.
- **Plan-architect agent** — sees the slice plan but not the test
  layout. Job: critique the proposed decomposition (rule #10
  granularity check, rule #11 "is this still worth extracting"
  check, rule #8 "any public-API expansion?" check). Run before
  Stage 5 code edits.
- **Test-author agent** — sees the production code but not the
  existing test layout. Job: write characterization tests
  bottom-up from the public API. Useful for Stage 4 when no tests
  exist.
- **Doc-mining agent** — sees the issue tracker / PR queue / commit
  history / community archives. Job: enumerate edge cases mentioned
  by users that the methodology should preserve (Stage 3 signal
  mining).

### Background / long-running agents

When work takes more than the human's attention span to complete,
fire it off in the background and continue. The agent framework
notifies the coordinator when the work finishes.

- **Heavyweight test suite runs** (IT, browser E2E, mutation
  testing). Fire after a slice, continue planning the next slice
  while it runs.
- **Static-analysis re-indexing** after large code changes.
- **Demo-server / dev-UI processes** that the Playwright agent
  drives.

### When NOT to parallelize

Parallel agents add coordination cost. Avoid the overhead when:

- The work is small enough that the human can read it directly
  (single-method change, small grep).
- The sub-agents would interfere with each other (writing to the
  same file, modifying overlapping state).
- The reconciliation step is harder than just doing the work
  serially.

A reasonable rule of thumb: **parallelize at the slice or feature
boundary**, not within a single edit. Per-slice spawn: 1 coordinator
+ 0–3 sub-agents.

### Tools, skills, MCP servers

The methodology assumes the agent has access to standard
tooling beyond shell:

- **Codebase tools** — the IDE's index, the build system's task
  runner (Maven / npm / Gradle), the test runner with selective
  filtering.
- **VCS tooling** — `git` for blame, log, stash, diff; `gh` /
  `glab` for issues, PRs, and comment threads.
- **Static-analysis tooling** per §8.
- **Browser-automation MCP servers** — Playwright MCP is the
  reference implementation for the sanity-loop tier. Without an
  MCP, fall back to `npx playwright codegen` for authoring and
  `npx playwright test` for replay.
- **Visual-diff tooling** when sanity scripts include screenshots
  (Argos, Percy, or local pixelmatch).
- **Skills / commands** — project-specific recipe wrappers (e.g.
  "run the spreadsheet IT subset", "regenerate the contract list",
  "rebuild static-analysis indexes"). Invest in these the second
  time you do something manually.

### Extracting recurring LLM prompts into skills

The pipeline has several recurring activities that are
cognitively heavy, easy to short-circuit, and structurally
identical from slice to slice — every iteration of the loop
runs them in the same shape. These are the high-value
candidates for promotion from ad-hoc prompt to a versioned,
reviewable **skill** (a Markdown file with YAML frontmatter
under `.claude/skills/<name>/SKILL.md`, invoked as
`/<name>`).

#### Pipeline is the spec; skills are derivations

The relationship matters: **this document is the methodology
spec** — the human-readable artifact where rules and
sequencing get debated, refined, and recorded. **Skills are
operational tools** derived from the spec. The derivation
happens at design time, not at runtime.

Practical consequence: every skill is **self-contained**. It
inlines the rules and output shape it needs; it does *not*
tell the agent to "go read this pipeline first." A skill
that depends on this document being present in the same repo
is a skill that breaks the moment it's copied elsewhere.
That defeats the point of formalizing the prompt.

When a rule in this document is edited, the affected skills
get edited in the same commit. The skills are not a cache of
the spec; they are independent implementations of it that
happen to share the same rule text by design. Both must move
together when the methodology evolves.

#### Why promote at all

- **Stability.** A skill pins the rules (Rule A, Rule B, the
  output shape) in one place per skill. Drift between slices
  comes from operator memory degrading across long sessions;
  pinned rules don't drift.
- **Reviewability.** A skill is text-in-repo. It gets PR
  review like code. Bad rules surface as bad outputs and get
  fixed at the source.
- **Composability.** Skills can call sub-agents, MCP
  servers, and other skills; an ad-hoc prompt cannot.
- **Portability.** A self-contained skill works in any repo
  the same way. The pipeline doc stays the spec; the skills
  travel.

#### Skills currently extracted

Each one is self-contained — it inlines the rules and
constraints it needs. None reads this document at runtime.

1. **`refactor-slice-plan`** — given a god class, produces
   a structured slice plan (concerns / helpers /
   members-used / widening / removal / test plan / DoD).
   The skill inlines Rules A/B/C/D (no new public surface,
   decompose logic, multi-class slices, iterate by field
   locality).
2. **`refactor-concerns`** — lighter subset of slice-plan:
   field-locality clustering only, no plan template.
   Useful for first-look reconnaissance before committing
   to a full plan.
3. **`refactor-characterize`** — scaffolds characterization
   tests for a method set before extraction. Inlines the
   "observed behavior is ground truth" / "bugs are part of
   the pinned behavior" principles and the fixture
   priority order (real → recording proxy → mock).
4. **`refactor-verify`** — runs the per-slice verification
   gates (compile / format / lint / tests / LOC / public
   API / helper visibility / payload / IT / coverage /
   mutation). Reports pass/fail/N/A per gate with deltas.
5. **`refactor-review`** — LLM code-review pass on a
   slice's diff. Checks Rules A/B/C/D plus the
   silent-change patterns (constant drift, conditional
   flip, switch fall-through, statement reordering,
   exception-type change, null-check addition / removal).

#### What not to promote to a skill

- One-off recipes that don't repeat ("run the spreadsheet
  IT subset" is one bash invocation — a shell alias or
  `Makefile` target is the right shape).
- Activities that depend heavily on conversation context
  (a skill starts in a fresh-conversation frame; if the
  activity needs you to recall five previous slices'
  decisions, the conversation thread is the right surface,
  not a skill).
- Anything you've only done twice. **Three repeats is the
  threshold.** Earlier than that, the abstraction is
  premature.

#### When skills overlap, factor or compose — don't cross-reference

If two skills end up inlining the same rule text and the
text drifts between them, the right fix is one of:

1. **Factor out a "constitution" skill** (e.g.
   `refactor-rules`) that holds the rule text and gets
   loaded by the others when they start. This is only
   worth doing once three or more skills duplicate the
   same text.
2. **Compose** — one skill explicitly delegates to another
   ("call `refactor-concerns` first, then use its output
   as input to this skill"). Composition is fine because
   it's explicit and at design time; runtime "see also"
   cross-references to a non-skill document are not.

The wrong fix is to have skills cross-reference this
document. That re-introduces the runtime dependency the
self-contained design was built to avoid.

## 11. Revision history

- **2026-06-05 (later)** — seventh-pass: skill-extraction
  guidance, then redesign to self-contained skills. Ugur first
  framed the iteration as "extract some LLM prompts into tools
  and skills to make the pipeline more stable and formal." First
  attempt added a `refactor-slice-plan` skill that cited the
  pipeline's Rules #8/#9/#10/#11 by number — i.e. it had a
  runtime dependency on this document. Ugur correctly pushed
  back: a skill that depends on a sibling file in the same repo
  isn't portable, and citing rules by number couples the skill
  to the pipeline's numbering. Redesigned the §10 skill
  subsection to position **the pipeline as the methodology spec
  (human-facing) and skills as derivations** — each skill is
  self-contained, inlines the rules and constraints it needs,
  and does not read this document at runtime. Rewrote
  `refactor-slice-plan` to inline Rules A/B/C/D (the same
  content as Rules #8/#9/#10/#11 in §0, but renamed to local
  letters to break the numbering coupling). Then created four
  sibling skills, each self-contained:
  `refactor-concerns` (field-locality clustering only),
  `refactor-characterize` (test scaffolding with the "observed
  behavior is ground truth" / "bugs are part of the pinned
  behavior" principles inlined),
  `refactor-verify` (the 11 per-slice gates as a single
  command with standardized output), and
  `refactor-review` (LLM code-review pass with the
  silent-change patterns inlined — constant drift, conditional
  flip, switch fall-through, statement reordering). Added an
  explicit "skills are derivations, not caches" framing plus a
  "factor or compose, don't cross-reference" rule for when
  skills overlap.
- **2026-06-05** — sixth-pass additions for tooling, verification, and
  agent orchestration. Ugur asked: "integrate Playwright CLI sanity tests
  the LLM can drive after each iteration; also code analysis, call
  graphs, UML diagrams, use-case extraction, code-review agents, parallel
  agents — leverage every useful thing." Added a **Playwright CLI sanity
  battery** subsection to Stage 4 — distinguishes the slow full-E2E tier
  from a fast per-slice sanity tier (~20–60 LOC scripts, seconds to run,
  LLM-authored via Playwright MCP / codegen, LLM-driven JSON-reporter
  parsing). Made Stage 0's static-analysis substrate concrete (per-
  ecosystem tools for symbol index / call graph / dependency graph /
  cyclomatic complexity / LCOM / per-class churn). Made Stage 1's UML
  deliverables concrete (class diagram before, component diagram after,
  sequence diagram per slice, state machine for hidden lifecycles — all
  in PlantUML / Mermaid stored under `<journal>/diagrams/`). Added
  **§9 "Per-iteration verification loop"** — the canonical 11-gate
  battery that runs between every slice (compile → format → lint → tests
  → coverage delta → mutation delta → static-metric delta → Playwright
  sanity → LLM code-review → differential payload → full IT at
  end-of-batch). Added **§10 "Agent orchestration"** — when to spawn
  parallel sub-agents, when to use specialized sub-agents (code-reviewer
  / plan-architect / test-author / doc-mining), when to fire long-running
  agents in the background, and when not to parallelize. Updated STOP
  gates (§4) with two new gates: when the per-iteration LLM code-reviewer
  flags a behavior change, and when a verification gate becomes flaky.
  Rewrote §6 Definition of Done as "operational gate = §9 loop green;
  structural conditions = the semantic checks." Renumbered original §9
  "Revision history" to §11.
- **2026-06-04 (evening)** — fifth-pass generalization. Ugur clarified the
  methodology's intent: it is **not Spreadsheet-specific** — it should work
  for any legacy refactor, in any language / framework / domain, on
  codebases that may lack tests, documentation, or git history. Reframed
  the title and intro accordingly (Spreadsheet is now explicitly the
  *running example*, not the target). Added rule #11 (triangulate every
  claim across multiple signals; absent signals route to fallbacks). Added
  §2.0 "Signal inventory" — an explicit table of every signal source the
  methodology uses and its fallback when absent. Expanded §3 Stage 3 from
  "history mining" to general "signal mining" (commits + issues + PRs +
  community Q&A + property-based testing + LLM enumeration + domain
  experts). Added a Stage-4 bootstrap path for codebases with no existing
  tests at all. Added §7 patterns for sparse-signal cases (no tests, no
  docs, thin history, no issue tracker, no domain expert) — each names
  the methodology's degradation strategy. The pipeline now degrades
  gracefully when signals are absent rather than assuming a fully-
  instrumented codebase.
- **2026-06-04 (later afternoon)** — fourth-pass corrections plus rule #10
  on multi-class slices. Added the "split into multiple small helpers, not
  one big rename" rule after Ugur observed that a single 300-LOC
  extraction is still a small god class. Rewrote slice 2 of the running
  example accordingly — the merge concern went into four classes
  (Lookup / Cascade / Operations / Synchronizer), not one. Added §3 Stage 5
  step 3 making the "look for sub-concerns within the slice" pass
  mandatory.
- **2026-06-04 (afternoon, also)** — third-pass corrections after Ugur
  pushed back on scope: "refactoring `SpreadsheetHistoryManager` is not
  enough — do the same to `Spreadsheet` and similar god classes." The
  earlier wording framed the orchestrator as the *target's host* and the
  managers as the *targets*, which made the 6,200-LOC `Spreadsheet` class
  implicitly exempt from decomposition. Rewrote §1 to make the orchestrator
  a first-class refactor target, §3 Stage 1 to enumerate concerns inside
  *every* god-class-shaped class (orchestrator and managers), and §5 to
  score slice candidates drawn from both. The methodology now applies
  recursively to every class above a size threshold, not just the
  satellites of one chosen class.
- **2026-06-04 (afternoon)** — second-pass corrections after Ugur reviewed the
  first slice and pushed back on two points. Added rule #8 (no public-API
  expansion during refactor) and rule #9 (refactor decomposes logic; it does
  not just decouple it). Rewrote §3 Stage 5 to be about logic decomposition
  with interface-extraction reframed as optional scaffolding. Tightened §6
  Definition of Done to make those rules into gates. The first version of
  this document treated interface extraction as the goal; in practice that
  produced a "refactor" that left the spaghetti in place and quietly widened
  the public surface — exactly the kind of silent change a methodology
  should prevent. Both rules are now first-class.
- **2026-06-04 (morning)** — substantial rewrite informed by the Stage-0 pass
  on Vaadin Spreadsheet. Generalized the "god-class + managers" framing as
  the orchestrator pattern (§1) with explicit variants. Expanded the
  discovery checklist (§2). Added a lightweight vs heavyweight substrate
  mode (§3 Stage 0). Added the `<Manager>Host` naming convention, the
  package-private-field-write check, and the Serializable interface rule
  (§3 Stage 5). Replaced the single-line "first slice" recommendation with a
  heuristic (§5). Added explicit STOP gates (§4). Added patterns/pitfalls
  (§7) and a per-ecosystem substrate appendix (§8) so the methodology can
  travel to non-Java codebases.
- **Pre-2026-06-04** — initial methodology, drafted from general knowledge of the
  V8/GWT codebase before the Flow port was inspected.

---

## 12. Phase 2 — Behavior pinning (comprehensive tests across tiers)

After Phase 1, every functional block is its own class or method. Phase 2's
job is to **pin what every block does**, with bug-preserving precision, via
the right test tier for each behavior. The output is a **coverage matrix**:
rows are functional blocks, columns are test tiers, cells are status
(covered / gap / N/A).

### 12.1 Tier inventory

Different behaviors only become visible at different tiers. A complete
test set uses many tiers, each pinning different aspects:

- **Unit (pure-logic)** — JUnit / pytest / Vitest on a single helper class
  with no fixture beyond plain data. Best for: pure-function clusters,
  algorithm correctness, edge cases.
- **Unit (real-fixture)** — same shape but with an in-memory dependency
  (XSSFWorkbook, SQLite, fake document). Best for: code that talks to a
  data library where the library's behavior is part of the contract.
- **Recording proxy / golden master** — wrap the parent in a recording
  proxy that captures interactions; serialize to JSON; commit as golden
  master. Best for: cross-class interaction sequences, RPC payload
  preservation, side-effect order.
- **Integration / Maven failsafe** — multi-component, JVM-level. Best
  for: framework dispatch, serialization, multi-class workflows.
- **TestBench / Selenium E2E** — full browser drive against a real app
  server. Best for: UI behaviors that depend on the rendered DOM, drag /
  drop, keyboard input.
- **Playwright CLI sanity** — fast per-feature sanity scripts, LLM-authored
  via codegen, LLM-observable via `--reporter=json` plus screenshots /
  DOM snapshots / console logs / network requests. Best for: per-slice
  smoke tests after each Phase 1 / Phase 4 / Phase 5 slice.
- **Snapshot tests** — capture a serialized state at a known checkpoint;
  re-run and compare. Best for: format-preserving operations, code
  generators.
- **Fuzz / property tests** — generate random inputs, assert
  invariants. Best for: parsers, serializers, anything with input space
  too large to enumerate.
- **Characterization** — write the test, run it deliberately failing,
  copy the actual output into the expected, commit. Different from
  golden master in scale: one input, one assertion, one bug pinned. Best
  for: legacy code where the docstring lies.

### 12.2 The coverage matrix

The matrix is the deliverable. Generate it programmatically:

```
                    | unit | unit-fix | golden | IT | E2E | playwright | snap | fuzz | char
ClientCellCache     |  ✓   |          |        |    |     |            |      |      |
ClientFlushQueue    |  ✓   |          |        |    |     |            |      |      |
ClientCacheFlusher  |      |    ✓     |   ✓    |    |     |            |      |      |   ✓
CellBorderStyleB.   |      |    ✓     |        | ✓  |  ✓  |     ✓      |  ✓   |      |
...
```

A cell is `✓` when there's a passing test of that tier covering the
block; `−` when the tier doesn't apply (e.g. fuzz on a static accessor);
empty when there's a gap. Gaps are the slice 2 backlog.

### 12.3 LLM-driven test generation

For each gap, the LLM authors a test using the available signals:

- **The helper's source code** — read the methods, infer the contract.
- **The relevant git history** — `git log -p` for the methods to find
  past bugs (which become characterization-test cases) and design
  motivations (via the `refactor-history` skill, §16).
- **The class's existing tests** — copy fixture setup, follow naming
  conventions.
- **For UI tiers**: drive Playwright via codegen — open the dev UI,
  perform the workflow, capture the generated script, edit for clarity,
  parameterize, commit.

**Output observability for UI tests:** the LLM needs to *see* what
happened. Playwright with `--reporter=json` provides:

- DOM snapshots before / after each action.
- Console logs from the browser.
- Network request log.
- Screenshots (auto on failure, optional on success).
- Trace files (`--trace=on`) for time-travel debugging.

The LLM consumes the JSON report to assert on observable facts; on
failure it reads the screenshot + trace and either fixes the test or
flags a regression.

### 12.4 Per-slice Phase-2 cadence

A Phase 2 slice picks **one block × one tier** at a time. Don't
parallelize across tiers within a slice — each tier has its own fixture
ceremony and adding two at once mixes concerns. Land one tier, run
all gates (§9), then start the next.

When a tier is N/A for a block (e.g. fuzz for a 5-line accessor),
explicitly record `N/A — <reason>` in the matrix. Future maintainers
shouldn't re-investigate the same not-applicable.

### 12.5 Definition of done

Phase 2 is done when every block has:

1. At least one tier showing `✓` (the floor).
2. At least one *characterization* or *golden master* `✓` if the block
   has any non-trivial logic.
3. At least one *Playwright sanity* `✓` if the block participates in a
   UI path.
4. Explicit `N/A` entries for tiers that genuinely don't apply.

The matrix is the operator's gate. Phase 4 (architectural refactor)
cannot start until the matrix passes this check.

---

## 13. Phase 3 — Specifications (natural-language contracts)

Tests pin behavior at the code level — they say "the implementation
produces value V for input I." That's necessary but not sufficient for a
safe architectural refactor: the operator looking at the test can't
always tell *which part is the contract and which part is the
implementation accident*.

Phase 3 produces a **natural-language specification** per block: what the
block is supposed to do, what its invariants are, which behaviors are
intentional versus preserved bugs, what its inputs / outputs / side
effects are. The spec is the contract the architectural refactor must
preserve. Tests pin the value; the spec pins the meaning.

### 13.1 Per-block spec template

```markdown
# Block: <BlockName>

## Purpose
<One-paragraph description of what this block exists to do.>

## Inputs
<List of inputs: parameters, parent-class state read, library APIs queried.
Format: each input → "type, range, when it can be null, what it represents."

## Outputs
<List of outputs: return values, state written, side effects emitted.>

## Invariants
<Things that are always true before / after calling the block.
Example: "after `pruneRowsRange(start, end)`, no key in `sentCells`
ends with any `"row" + i` for `i in [start, end]`.">

## Edge cases
<Cases worth pinning explicitly:
- empty input → behavior
- null input → behavior
- boundary inputs (first row, last column, max-size collection) → behavior
- environment-dependent inputs (locale, timezone, charset) → behavior
->

## Error modes
<What this block does on failure:
- throws? what exception type, when?
- swallows? logs at what level?
- returns sentinel? `null` / `-1` / `Optional.empty()`?
- fails silently? known bug or intentional?>

## Non-obvious quirks
<Behaviors that look wrong but are pinned because they're intentional, or
because they're preserved bugs to be fixed later.
Example: "`getCachedFormulaCellValue` returns the cell's last cached
result even if the formula has been re-evaluated since. This is
intentional — callers wanting fresh values must call
`getFormulaEvaluator().evaluateFormulaCell(cell)` first.">

## Source links
<Links to:
- The class's source file.
- The most informative PR that introduced or changed this block.
- The most informative issue that explains why this block exists.>
```

### 13.2 LLM-drafted, human-reviewed

The LLM drafts the spec from:

1. **The block's source code** — reads the methods, infers the contract.
2. **The block's tests** — every passing assertion is a contract point.
3. **The repo history** via `refactor-history` skill — finds the PR that
   introduced the block, the issue it was solving, follow-up bugs.
4. **The class's javadocs / comments** — *as untrusted hypotheses to
   verify against the code* (per the "comments lie" principle).

The human operator reviews:

- Does every invariant correspond to an actual assertion in a test? If
  not, either add the test or remove the invariant.
- Is every "intentional quirk" actually intentional? Verify against
  history.
- Are any contract points missing? Walk the source one more time after
  reading the spec.

The review is fast because the LLM has done the synthesis; the human
checks for synthesis errors.

### 13.3 Definition of done

Phase 3 is done when every block has a spec, every spec has been
human-reviewed, and every invariant in every spec maps to a passing
test. Specs live in `<journal>/specs/<BlockName>.md`. They are
versioned alongside code and updated whenever the block's contract
changes (which should be rare — that's a behavior change, not a
refactor).

---

## 14. Phase 4 — Architectural refactor (free internal redesign)

With Phase 2's tests and Phase 3's specs in place, the operator now has
two parallel safety nets: the tests verify code-level behavior, the
specs verify human-understandable behavior. The architectural refactor
can be aggressive.

### 14.1 What Phase 4 allows

- Rename classes freely (as long as their *public* names are preserved).
- Restructure data flow: a value previously passed via field can move to
  a parameter, or vice versa.
- Change algorithms: replace an O(n²) loop with an O(n log n) sort + scan.
- Swap implementations: a pure-Java POI loop can become a streamed
  pipeline.
- Re-architect collaboration: two helpers that talk through the parent
  can talk directly.

### 14.2 What Phase 4 still doesn't allow

- Change the public API surface (any class / method that downstream code
  could bind to).
- Change behavior pinned by tests or by Phase-3 spec entries.
- Introduce new dependencies on external libraries (those are scope
  expansions, not refactors).
- Skip the per-iteration verification loop (§9) — it still applies.

### 14.3 The slice unit changes

In Phase 1, the slice unit was "extract helper X." In Phase 4, it's
"swap implementation Y for implementation Z." The plan template (per
`refactor-slice-plan` skill) gets reshaped:

- **Concerns identified** → "the algorithm / data structure / collaboration
  pattern being swapped."
- **Proposed helpers** → "the new implementation's class graph."
- **Members-used set** → "what the new code reads / writes."
- **Test plan** → "which existing tests prove the swap preserves
  behavior; which new tests pin previously-untested edges."
- **Definition of done** → "compile + format + lint + tests + spec
  unchanged + payload diff = empty."

### 14.4 Differential payload check is critical

Phase 4 changes internals. For UI components like Vaadin Spreadsheet,
the RPC payload format is part of the public contract. Phase 4 must
include payload golden-master tests: capture the JSON / binary payload
on the pre-Phase-4 codebase, replay the same workflow post-Phase-4,
diff. Any difference is either a real behavior change (which must be
spec'd) or a serialization quirk (which must be fixed).

### 14.5 Definition of done

Phase 4 is done when the operator runs out of meaningful redesigns —
the architecture has settled into a shape that's clearly better than
the original on at least three of: readability, performance, testability,
extensibility, or correctness. The verdict is qualitative; the journal
records each Phase-4 slice's stated improvement.

---

## 15. Phase 5 — Method decomposition (raise abstraction within blocks)

Phase 5 is the smallest-scale phase. Within each class (god class
descendants from Phase 1, helpers from Phases 1 / 4), find methods that
are over ~50 LOC with two or more sub-concerns, and break them into
intent-revealing helper methods on the same class.

### 15.1 Recognize a Phase 5 candidate

A method is a Phase 5 candidate when:

- It has visible structural break points (blank-line-separated blocks,
  comment headers like `// step 1: ...`, distinct phases).
- It has multiple early returns / multiple state-mutation phases that
  could each be named.
- Reading the top of the method doesn't tell you the bottom of the
  method's effect.
- The method has 2+ levels of nesting most of the way through.

### 15.2 The extraction pattern

Each extracted method should:

- Be private (or package-private if it makes sense for testing).
- Be named after *what it does at the abstraction level of the caller*,
  not what it does internally. `appendBorderCss` is better than
  `iterateBordersAndWriteToStringBuilder`.
- Take only the arguments it needs (not the parent class state, unless
  it has to).
- Be small enough that its top-level statements are at most one
  abstraction layer below the caller's. (Robert Martin's "stepdown
  rule".)

### 15.3 Phase 5 is not about LOC reduction

After a Phase 5 pass, the class often gets longer (more methods, more
method overhead). The win is **readability**: the original 250-LOC
method becomes a 15-LOC orchestrator that names the steps + five
small focused helpers. Phase 5 isn't a decomposition flavor; it's a
*re-readability* flavor.

### 15.4 Done check

Phase 5 is done on a method when its top-level body reads as a sequence
of named operations, each at the same abstraction level, no inner
control flow more than one level deep. Phase 5 is done on a class when
no remaining method qualifies as a Phase 5 candidate per §15.1.

---

## 16. Repo-history mining (cross-cutting)

Git history, PR descriptions, PR-review comments, issue descriptions,
and issue comments are high-signal sources for:

- Why a piece of code exists (PR description + issue).
- Why a piece of code looks weird (PR review comments that discussed
  alternatives).
- When a behavior started or changed (`git log -p`, `git bisect`).
- Which incidents shaped which design decisions (issues linking to the
  PR).
- Which authors have domain knowledge (`git blame` + author distribution).

### 16.1 The multi-repo case

A long-lived project may have history split across multiple repos: the
current monorepo plus a legacy repo where the component used to live.
For the running example, the Vaadin Spreadsheet has:

- `vaadin/flow-components` — current location, post-Flow-port history.
- `vaadin/spreadsheet` — legacy, pre-Flow-port history, mostly archive.

The pipeline treats both as the same history graph for any given
file / method, and `refactor-history` skill queries both via the `gh`
CLI.

### 16.2 The `refactor-history` skill

A self-contained skill that mines repo history for a given target
(file, method, commit). See `.claude/skills/refactor-history/SKILL.md`.

It's invoked at:

- **Phase 1 Stage 0 discovery** — what's the history of this god class?
  When was it introduced? What incidents shaped it?
- **Phase 1 per-slice** — for the methods being extracted, what's the
  most informative PR? Useful when a method's purpose isn't obvious
  from the code.
- **Phase 2 test generation** — what bugs did this block have? Those
  bugs become characterization-test cases.
- **Phase 3 spec writing** — what does the original PR say this block
  is for? That goes into the spec's "Purpose" section.
- **Phase 4 architectural review** — what alternatives were considered
  and rejected? PR review comments often reveal these.

### 16.3 Signal filtering

Git history has noise. The skill filters:

- Dependabot / Renovate commits → skip.
- "wip" / "fix" / "tmp" commit messages → low priority, but check.
- Merge commits → traverse to the underlying commits.
- PR descriptions auto-generated from commit list → low priority.
- PR descriptions with > 100 words of human writing → high priority.
- Issue descriptions with reproducers → high priority.
- PR-review comments with > 50 words → high priority.

### 16.4 Output shape

The skill produces a **timeline** per target:

```
2018-04-12 — issue #4521 — "Conditional formatting border breaks on merged cells"
            Reproducer: load workbook X, open sheet, observe Y.
            → PR #4533 — fixed by Thomas. Introduced shiftedBorderTopStyles map.
              Review comment from Marc: "Why not just use existing border style?"
              Reply: "Because Excel's transferred-border semantics — see spec section X."

2020-09-08 — PR #6712 — "Speed up styles reload on large sheets"
            Refactored loadGrouping into nested loops; introduced LOG_MEMORY flag.

2023-11-30 — commit a1b2c3d — "fix: NPE in style index map" (no PR, no issue)
            → blame shows Maria touched this in a hotfix. Low-context fix.
```

The operator reads the timeline once per target; the LLM doesn't
re-query unless the target changes.

---

## 17. Human checkpoint policy

The pipeline is designed to minimize human input. Humans are gated to
decision points where their judgment adds value the LLM can't provide.
Everything else runs automated.

### 17.1 Where human input is high-value

These are the explicit checkpoints. The pipeline pauses at each;
without human approval it doesn't continue.

1. **End of Phase 1 Stage 0 (Discovery report).** Human approves the
   frozen contract list, the slice-candidate ranking, and the
   methodology updates this Stage-0 surfaced. Reason: the rest of the
   pipeline binds against the frozen contract; a wrong frozen contract
   is a load-bearing wrong assumption.

2. **Before each slice (slice plan).** Human approves the plan
   produced by `refactor-slice-plan`. Reason: the plan locks scope and
   flavor; a wrong flavor (e.g. an encapsulation slice sold as
   decomposition) wastes a slice and sets the wrong expectation.

3. **After each milestone batch (~5 slices).** Human sanity-checks
   direction. Reason: the journal accumulates lessons that may change
   the plan for upcoming slices; without a checkpoint, the LLM
   misses the inflection.

4. **End of Phase 1 (god classes done).** Human approves the
   Rule-D verdicts on each god class — which are fully decomposed,
   which are state-machines, which are utilities. Reason: this
   approval defines the block boundary for Phase 2's coverage matrix.

5. **End of Phase 2 (coverage matrix complete).** Human approves
   the matrix as comprehensive enough for Phase 4. Reason: this
   approval gates Phase 4's "free refactor" license.

6. **Per-spec review in Phase 3.** Human approves each natural-language
   spec for correctness against intent. Reason: the spec's "intentional
   vs preserved bug" annotations require domain knowledge the LLM can
   only guess at.

7. **Before each Phase 4 slice (architectural plan).** Human approves
   the redesign scope and the chosen target architecture. Reason: many
   architectures are defensible; the choice is taste.

8. **STOP gates (§4.1).** Human handles every STOP gate. Reason: STOP
   gates are by definition the moments where the LLM has detected
   something it can't proceed past.

### 17.2 Where human input is low-value (automate aggressively)

- **Mechanical slice execution** (writing helper code, updating call
  sites, removing orphaned methods).
- **Verification gates** (§9 — compile / format / lint / tests / LOC).
- **Cross-reference checking** (grep-before-compile, import cleanup).
- **Journal entry drafting** (LLM drafts; human can edit before commit).
- **Test scaffolding** (Phase 2 — LLM writes the boilerplate; human
  reviews only for fixture correctness).
- **History summarization** (Phase-1 Stage 0, Phase-3 spec drafting —
  LLM reads PRs / issues, human reads the summary).
- **LOC and helper-count accounting.**
- **Spotless / Checkstyle / Prettier / equivalent formatting.**

### 17.3 The asymmetry to preserve

Humans should be in the loop for things that **need a single right
answer the LLM can guess wrong**: domain meaning, intentional vs bug,
scope, architectural taste. Humans should be out of the loop for
things that **the LLM can verify against an oracle**: tests, compile,
lint, payload diff, spec-vs-test consistency.

When in doubt: if the verification has a yes/no answer and a way to
check it, automate. If it has a "this feels right" answer that comes
from experience, keep the human.

### 17.4 The "two emails" rule

A reasonable human checkpoint takes the operator **two emails** of
work: read the LLM's summary, write a one-line reply (approve / push
back / clarify). If a checkpoint takes more than two emails, it's
either over-loaded (split it into smaller checkpoints) or
under-prepared (the LLM should do more pre-work before asking).
