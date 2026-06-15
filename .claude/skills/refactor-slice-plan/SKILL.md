---
name: refactor-slice-plan
description: Produce a structured decomposition plan for one god class. Use this when the user asks for the next slice of an in-flight legacy refactor, or when you need to enumerate a god class's concerns before extracting helpers. Output is the same fixed shape every time — concerns, helpers, members touched, widening list, removal list, test plan, definition of done — so it can be reviewed and applied mechanically.
argument-hint: <god-class-file>
disable-model-invocation: true
allowed-tools: Read, Bash(find:*), Bash(grep:*), Bash(wc:*), Bash(git:*), Bash(awk:*), Bash(sed:*), Agent
---

# Refactor slice plan

Given a god class, produce one slice plan. A slice plan is the artifact reviewed before any code is changed — it forces the structural thinking up front, so the actual edit becomes mechanical. Skip the plan and you end up with cosmetic decomposition or surprise API expansion.

**Output is a STOP gate.** Produce the plan and wait. Do not write code in the same turn.

## Inputs

- `$0` — absolute path to the god class file. Required. Single file.

## The rules this skill enforces

These are the constraints every slice plan must respect. They're inlined here so this skill is self-contained — no other file needs to exist for the skill to work.

### Rule A — No new public/protected members on publicly-visible classes

A refactor does not expand the public surface. Do **not** add new `public` or `protected` members to classes that are already part of the published API. Every new public method becomes an obligation downstream code may bind to; widening the contract as a side effect of structural cleanup turns a refactor into a (silent) API change.

Permitted instead:

- New **package-private helpers** on visibility-frozen classes.
- New **package-private classes** with whatever internal `public` API they need — they stay hidden behind the package wall.
- **Same-package access** via existing `protected` or package-private members of the parent (no widening needed if the helper lives in the same package).
- **Widening visibility *downward*** — `private → package-private` is fine, it doesn't change the published contract.

What is **not** permitted:

- New `public` methods on a published class to expose helper state.
- Promoting `private` to `protected` or `public` to reach the helper.
- Removing existing `public` / `protected` members entirely — they must be preserved as **thin delegators** (one to four lines forwarding to the helper) so subclass override surface stays intact.

If the slice seems to require widening *any* visibility on a published class above package-private, stop and surface the problem in the plan. There is almost always a same-package helper or an internal class that achieves the goal without API expansion.

#### Rule A subtlety — `protected` fields cannot be moved out wholesale

A field with `protected` visibility on a publicly-visible class is part of the published contract — downstream subclasses can read and write it. If you move the field into a helper, those subclasses see a stale or null value. The field has to stay on the parent.

When clustering fields for an encapsulation slice, **separate the cluster's `protected` members from its `private` members**. The private ones move into the helper. The protected ones stay on the parent and the helper's javadoc explicitly notes the exclusion. Don't try to keep both copies in sync — that creates two sources of truth and the bug-surface for divergence.

When in doubt, grep the field's name across the codebase before moving it. If it shows up in test reflection or in any class outside the slice's package, treat it as protected-by-usage and leave it on the parent.

### Rule B — Decompose logic, not couplings

A refactor breaks the logic into smaller single-purpose pieces. Each helper must be describable in **one sentence** ("holds the command stack," "applies a command's side effects," "rewrites cell-reference indices in a formula string").

What does **not** count as decomposition:

- Extracting an interface in front of a monolith. The next maintainer still has the same 250 lines to read; the dependency direction is just hidden.
- Renaming a god class's inner anonymous class into a top-level file. That's a move, not a split.
- Pulling out a one-method "facade" that delegates everything back to the original. The logic didn't go anywhere.

A 250-line manager that becomes a 60-line coordinator + a 60-line stack helper + a 60-line side-effect helper **is** a refactor. The same 250-line manager with a 6-method interface in front of it is not.

Interface extraction may be necessary scaffolding for the decomposition; it is not the destination.

### Rule C — Multi-class slices (3–5 helpers per slice)

Within a single slice, look for **sub-concerns** inside the chunk you're moving. If your proposed extraction is going to be one 250-LOC "new manager," that's a sign you've stopped at the first level of decomposition — the new class is a smaller god class.

Look harder — it probably contains a data-structure concern, an operation concern, a serialization concern, a loading concern, etc. Carve those out as separate package-private classes in the same slice, each describable in one sentence. The orchestrator's old method becomes a thin delegator across several focused helpers.

**Aim for three to five focused classes per slice**, not a single big rename. Fewer than three is a sign the analysis hasn't gone deep enough. More than five is a sign the slice is trying to do too much in one step.

#### Three flavors of slice — name which one you're doing

Slices come in three shapes. All three are valid, but they cost and pay back differently. Pick deliberately and state which one in the plan, so the reviewer doesn't expect the wrong outcome.

**Decomposition slice** — moves multi-step logic into single-purpose helpers. The god class shrinks substantially (often 100s of LOC per slice) because long methods get redistributed across the new helpers as small focused methods. Examples: a sequence-detection cluster, a deletion-decision cluster, a directional-iteration orchestrator, a cache-flush state machine. These are what Rule B is primarily about. Run a decomposition slice when the cluster has *behavior* worth carving out — not just state.

**Encapsulation slice** — moves a cluster of related fields and their thin wrapper accessors into a focused state holder. The god class shrinks only modestly (often ~20–50 LOC) because the moved code was already short, and the new delegators occupy nearly the same line count as the originals. Examples: a viewport-bounds state object, a freeze-pane state holder, a buffer-config holder, a row/column dimension state. The win is *cohesion*, not LOC — fields that always change together now live together, the parent loses N independent state concerns. Run an encapsulation slice when the cluster is mostly state with no behavior beyond "set value + mirror to client" or similar uniform side effect.

**Abstraction slice** — replaces N occurrences of a uniform pattern with one generic helper plus N small instantiations. The god class shrinks proportional to the boilerplate per occurrence (often 30–50% of the affected setters). Examples: a `MirroredProperty<T>` that owns "store value + serialize + push to element property" replacing 30 manually-written setters; a `LazyLoadedCache<K,V>` replacing 5 manually-written "if not loaded then load else return" caches. The win is *less boilerplate to maintain* — when the pattern needs to change (e.g. add logging on every mirror), one place changes instead of 30. Run an abstraction slice when you've seen the same shape three or more times in the god class and they're functionally identical (only the field name + property name + serialization choice differ).

**Abstraction-slice design heuristic: when in doubt, create a sibling class, not a variant.** If the new pattern differs from your existing abstraction in *two or more* semantic dimensions (e.g. different defaulting + different set semantics), shoehorning it into the same class via lambdas / strategies obscures more than it shares. Create a sibling class — same package, same naming family (`MirroredProperty` + `MirroredNullableMap`), one-dimension-of-difference between them — and document the relationship in both classes' javadoc. Each one stays simple and readable in isolation. The reader's vocabulary cost is "remember both exist" rather than "remember how the strategy parameters change the behavior."

**Abstraction-slice trap: not every visual match fits the abstraction.** When grepping for `setProperty` call sites or any other "uniform pattern," carefully split the matches into:
- **Has-cache (fits)**: `this.X = value; getElement().setProperty(name, value)` — a server-side field + a mirror to the client. The abstraction holds both halves.
- **Write-only (doesn't fit)**: `getElement().setProperty(name, value)` with no server-side field. Reads go through `getElement().getProperty(name)`; the client is the single source of truth. There's nothing to abstract — the abstraction would force adding a server-side cache that didn't exist.
- **Null-coalescing (needs variant)**: `if (x == null) this.X.clear(); else this.X = x; getElement().setProperty(name, Serializer.serialize(x))` — has a cache, but with special null handling. The plain abstraction loses the null-clear semantic.

Migrate only the "Has-cache" group. Leave "Write-only" as-is. Defer "Null-coalescing" until you design a variant. A naive grep that includes all three groups overstates the migration win and risks subtle behavior changes for the Write-only group.

Abstraction slices are riskier than the other two because the new generic helper becomes a **vocabulary** the rest of the codebase has to learn — readers seeing `new MirroredProperty<>(...)` need to know what it means. The risk is acceptable when the pattern is already pervasive (it's already vocabulary, just written out longhand) and unacceptable when only 2-3 occurrences exist (the abstraction overhead outweighs the savings). Three repetitions is roughly the threshold.

**Honest framing in the plan**: lead with the cohesion argument for encapsulation slices ("Spreadsheet had 10 viewport-related fields scattered across the class; after this slice it has 3 helper fields and no direct viewport state"). Lead with the line-count argument for abstraction slices ("30 setters of the same shape collapse to 30 one-line constructor calls"). Lead with the readability argument for decomposition slices ("the 65-LOC `updateMarkedCellValues` method becomes a 1-line delegator to a focused helper that names what it does"). Don't sell one as another.

### Rule D — Iterate until nothing is worth extracting

A god class is not "done" after one slice. After each completed slice, run this skill *again* on the same class and ask: *"is any functional cluster of fields + methods still worth pulling out?"* If yes, plan and run another slice. Repeat until the honest answer is no — which means the remaining content is:

- (a) genuine orchestration glue — lifecycle, constructor wiring, listener registration, framework dispatch;
- (b) tightly coupled state that resists further decomposition without behavior change;
- (c) so small it's no longer a god class.

**Group by field locality**, not by method name. A cluster of fields that always move together is one concern, regardless of how many methods touch them. Field clusters reveal hidden structure that method names sometimes obscure (a method called `loadAll()` often touches three unrelated field clusters).

The loop terminates by inspection, not by hitting a LOC target — a 5,000-LOC class can still be the right end state if the remaining content is genuinely indivisible; a 1,500-LOC class may still have a concern worth extracting.

#### Concrete "done" check

Before declaring a god class done, walk the remaining content and ask three questions. If you can answer all three "no," stop iterating on this class.

1. **Is there any cluster of 3+ fields that always move together and aren't already owned by a helper?** If yes, that's a candidate encapsulation slice. If no, move to (2).

2. **Is there any method body over 50 LOC that decomposes into 2+ sub-concerns** (each with a clear one-sentence purpose)? If yes, that's a candidate decomposition slice. If no, move to (3).

3. **Is there any pattern repeated 3+ times across the remaining methods/setters** (e.g. uniform "set value + mirror to client" setters)? If yes, that's a candidate abstraction slice. If no — you're done.

If all three answers are no, the remainder is what Rule D calls (a)/(b)/(c): orchestration glue, indivisible state, or small enough that further decomposition would hurt readability. Stop and document the verdict in the journal: "Class X reached Rule D's indivisibility threshold at N LOC; remaining content is M% orchestration / K% public API surface / L% small private utilities." That sentence is the artifact future maintainers need to know iteration is intentionally complete.

#### When a "done" verdict is wrong

The three-question check fails when the operator has been counting helpers extracted, not concerns identified. If you finish a god class with 2 helpers, the check might say "done" because no 3-field clusters remain — but the right read may be "the decomposition wasn't aggressive enough; sub-clusters exist within the helpers we extracted." Re-run `refactor-concerns` on each *new* helper after a few slices land; if any of them flags a sub-cluster worth extracting, the parent class isn't done yet — it's just that the next slice operates on the helper, not the parent.

This is Rule D applied recursively. A "god class" can hide inside a "helper" you extracted last week.

## Steps

### 1. Read the god class end-to-end

Read the full file. Don't sample — concerns are revealed by which methods touch which fields, and you cannot see that from excerpts.

For files over ~1,200 LOC, delegate the field-locality clustering to an Explore sub-agent (see step 2) so the full file doesn't sit in your own context window.

### 2. Build the field-locality map

For each method in the god class, list:

- The instance fields it reads.
- The instance fields it writes.
- The same-class methods it calls.

Then **group methods that touch overlapping field sets**. Each group is a candidate concern.

Heuristic: a method that touches a field no other method touches is the seed of its own concern. A method that touches fields used by two different clusters is a **hinge** — name it explicitly in the plan; it usually belongs to one cluster, with the other reached via collaboration.

For large files, this step is well-suited to an Explore sub-agent prompt:

> Read `<path>` end-to-end. For each method, list which instance fields it reads and which it writes. Group methods by overlapping field sets. Name each cluster with a one-sentence purpose. Note hinge methods that touch two clusters. List external collaborators (parent class methods, other helpers, library APIs) per cluster. Rank clusters by extractability — most independent first. Report under ~800 words.

### 3. Identify each concern's collaborators outside the file

For each cluster:

- Which methods on the parent class get called?
- Which other helpers from prior slices get called?
- Which external libraries / framework APIs are touched?

A concern that interacts with the parent only via pure data inputs is a candidate for **pure static helper** (`final class X { private X() {} static ... }`). A concern that needs the parent's state is an **instance helper** with a constructor that takes the parent.

### 4. Decide this slice's scope

Pick 3–5 helpers (Rule C). The natural candidates are the 3–5 clusters with the *lowest collaborator counts* and the *most independent state*. Defer clusters that entangle deeply with the parent's public methods until later slices — they usually need characterization tests written first.

For each helper, decide:

- **Shape**: pure static (no parent dep) or instance (constructor takes parent + sibling helpers).
- **Visibility**: package-private class. Methods can be `public` internally — they remain hidden behind the class's package-private visibility.
- **Serializable?** If the helper holds state and the parent class is `Serializable`, the helper must be too.

### 5. Write the plan in this fixed shape

Don't reorder the sections. The reviewer reads them top-to-bottom.

Placeholders use `{curly-braces}`. Replace with concrete values when writing the plan.

```markdown
# Slice {N} plan — {god-class-name}

## Concerns identified
- **Concern {letter}: {name}** — {one-sentence-purpose}. Fields: {field-list}. Methods: {method-list}.
- ...

## Proposed helpers (this slice)
- **`{HelperName}`** (~{LOC}) — {pure-static OR instance-with-constructor-signature}. Responsibility: {one-sentence}. Owns: {fields-and-constants}. Tests: {pure-logic-unit OR XSSFWorkbook-fixture OR characterization OR ...}.
- ...

## Members-used set (per helper)
- `{HelperName}`:
  - Reads: {parent-fields-and-methods}
  - Calls: {other-helpers-and-external-APIs}
  - Writes back: {parent-state-via-setters OR "none"}

## Widening list (parent class)
- `{member}` — `private` → package-private. Reason: {which-helper-needs-same-package-access}.
- ...
(or "none this slice" if no widening is needed)

## Removal list (parent class)
- `{field-or-private-method}` — moved to `{helper}`, removed.
- `{public-or-protected-method}` — preserved as thin delegator (Rule A — published surface).
- ...

## Test plan
- Existing characterization tests covering this area: {list OR "none — characterize first"}.
- New characterization tests (this slice, before extraction): {list OR "none — IT coverage sufficient"}.
- New helper unit tests (after extraction): {list-with-one-line-scope-each}.
- Fixture: {pure-data OR XSSFWorkbook OR MockUIExtension OR TestBench OR mixed}.

## Definition of done
- [ ] Compile / format / lint clean.
- [ ] Full unit suite green (target count: {N}).
- [ ] God-class LOC reduced by at least {estimate}.
- [ ] {N} new helpers, each one-sentence-describable.
- [ ] No new public/protected members on visibility-frozen classes.
- [ ] All new helpers package-private; `Serializable` where they hold state.
- [ ] Log entry appended to {journal-path} with LOC accounting + helper count + test count.

## What stays (this slice)
- {methods-and-fields-that-remain-in-the-god-class-after-this-slice}.

## Out of scope (deferred to next slice)
- {clusters-not-addressed-and-why}.
```

### 6. Stop. Wait for approval.

The plan is a STOP gate. Don't write code in the same turn. The reviewer reads the plan, pushes back on scope or approach, and approves before you touch any file.

### 7. After approval — grep before you compile

Once code edits begin, the most common failure mode is missed call sites. When a field moves out of the parent class into a helper, **grep for the field name across the whole file** before relying on the compiler. Compile catches the references that don't compile, but it's faster to find them all in one pass than to fix-compile-fix-compile.

```sh
grep -nE "\b{field-name}\b" {parent-class-file}
grep -nE "\b{method-name}\b" {parent-class-file}
```

For a slice that moves N fields, do N greps. Two minutes of grepping saves five rounds of `mvn compile`.

### 8. Cross-class access patterns you don't have to fight

A few accidental friction points that come up when wiring up helpers — none of these require widening visibility, despite first appearances.

- **Package-private static constants are fully visible from same-package helpers.** If your parent has `static final Map<...> BORDER = ...` (no modifier — package-private), a same-package helper can use `ParentClass.BORDER` directly. You don't need to make it `public`; you don't need to copy it; you don't need a getter.
- **Protected methods on the parent are visible from same-package helpers without subclassing.** Java's `protected` includes same-package access — a helper in the same package can call any of the parent's `protected` methods without declaring itself a subclass.
- **Constructor-order constraints when helpers depend on parent-init side effects.** If the parent's constructor calls a private setup method (e.g. `setupColorMap()` populates `colorConverter`), helpers that need that field must be initialized **after** the setup call. That means the helper fields can't be `final` field-initializers — they need constructor-body assignment after the setup runs. If a slice is hitting "cannot reference X before it's defined," check whether the setup runs before or after the helper init point.

These are not rules — they're traps that look like rules at first encounter.

## When the plan reveals a problem — STOP earlier

These are the cases where the plan itself is the wrong artifact and you should surface the issue instead of completing the template.

**Only one cluster forms.** The file isn't actually a god class — confirm with the user before slicing it.

**Every cluster's only collaborator is the public API surface.** The slice would be a re-export, not a refactor (Rule B). Reframe the concerns or move to a different file.

**Every cluster requires widening ≥ 5 parent members above private.** The parent's encapsulation is already broken in a way the refactor cannot fix slice-by-slice. Surface this — a structural fix to the parent (e.g., promoting a field cluster to its own object on the parent first) may be a precondition.

**The "smallest" helper is still > 300 LOC.** You stopped at the first level of decomposition. Apply Rule C again to that helper before writing the plan.

**No characterization tests exist for the area.** Add a "characterize-first" stage to the plan. Don't skip it because IT covers the round-trip — IT covers happy paths, characterization covers the *current actual behavior* including locale edge cases, null handling, and bugs that must be preserved.

## Output

Write the plan to standard output. Don't save it to a file unless the user asks — the plan is short-lived working state, captured in the conversation. The durable artifact is the slice's eventual journal entry (which the user authors based on the approved plan).

---

*Related skills (each self-contained):*
*`refactor-concerns` — lighter subset of this skill: field-locality clustering only, no plan template. Useful for first-look reconnaissance.*
*`refactor-characterize` — scaffolds a characterization test class for a method set + fixture type.*
*`refactor-verify` — runs the per-iteration verification gates after a slice lands.*
*`refactor-review` — LLM code-review pass on a slice's diff against these same rules.*
