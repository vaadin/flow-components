---
name: refactor-concerns
description: Field-locality clustering only. Lighter than refactor-slice-plan — produces just the cluster report (concerns, fields, methods, collaborators, extractability ranking), no plan template. Use this when you want a first-look reconnaissance of a god class before deciding whether to commit to a full slice plan, or when the parent skill needs the cluster report as an input.
argument-hint: <god-class-file>
disable-model-invocation: true
allowed-tools: Read, Bash(find:*), Bash(grep:*), Bash(wc:*), Bash(awk:*), Agent
---

# Refactor concerns

Given a god class, produce a **field-locality cluster report**. Methods are grouped by which instance fields they read and write. Each group is a candidate concern for a future slice.

This skill is a subset of `refactor-slice-plan`. Use it when you only need the cluster map — for example, when picking *which* god class to refactor next, before committing to a full plan.

## Inputs

- `$0` — absolute path to the god class file. Required.

## What this skill does NOT do

- Does not propose helper classes or names.
- Does not write a slice plan.
- Does not edit any code.
- Does not invoke `refactor-slice-plan`. That's the operator's next step if they want one.

## The clustering heuristic

**Group methods by overlapping field-touch sets.** A method that touches a field no other method touches seeds its own concern. A method that touches fields used by two different clusters is a **hinge** — name it explicitly.

**Why field-locality and not method-name similarity:** legacy methods often have names that don't reflect what they do (a method named `loadAll()` may touch three unrelated field clusters; a method named `clearCache()` may be the entry point to a deeper concern). Field touches are observable from the parser; names are a hypothesis.

**Why field clusters and not method clusters:** fields that always move together represent one piece of state. The methods that touch them are the operations on that state. Names of methods come and go; the state stays.

## Steps

### 1. Read the file end-to-end

Don't sample. For files over ~1,200 LOC, delegate to an Explore sub-agent with the prompt template in step 2.

### 2. Build the field-locality map

For each method, list which instance fields it reads, writes, and which sibling methods it calls. Then group methods by overlapping field-touch sets.

For a sub-agent, use this prompt (substitute the actual path):

> Read `{path}` end-to-end. Then produce a field-locality cluster report.
>
> Output sections:
>
> 1. **Field inventory** — every instance field, with type and one-sentence purpose. Skip static constants unless they're scoped to one concern.
> 2. **Method → field-touches map** — for each method (grouped by visibility), one line: `methodName: reads {f1, f2}, writes {f3}, calls {sibling1}`.
> 3. **Field-locality clusters** — groups of methods that touch overlapping field sets. Name each cluster with a one-sentence purpose. List the methods in it and the fields it owns.
> 4. **Hinge methods** — methods that touch fields belonging to two or more clusters. For each, name the two clusters and a one-sentence judgment about which one owns it.
> 5. **External collaborators per cluster** — which parent-class methods, sibling helpers, and library APIs each cluster touches.
> 6. **Extractability ranking** — clusters ranked from "most independently extractable" to "most entangled with the rest of the class." Brief reason for the ranking.
>
> Don't propose helper names or class shapes. That's a downstream step.
>
> Report under ~800 words.

### 3. Sanity-check the agent's output

Read the report. Flag any of these as problems:

- **Fewer than 3 clusters in a class > 600 LOC.** The agent missed sub-concerns. Re-run with a more aggressive split prompt.
- **One cluster contains > 60% of the methods.** That cluster is itself a god-cluster; ask the agent to subdivide it.
- **Many fields show up in many clusters.** The class may use a few "god fields" everything touches (often a `Map` of state). Note this — it's a structural problem the refactor will hit later.
- **Cluster names that just repeat method names.** The agent fell back to name-similarity instead of doing field-locality. Push back.

### 3.5. The agent's clusters are a hypothesis, not a verdict

The cluster report is a starting point, not the final design. **The code wins ties.** Two common cases:

- **Agent split, code says merge.** If the agent proposes cluster X and cluster Y as separate, but reading the source shows a single method writing to both clusters' state in one pass (e.g., a `doCustomStyling(cell)` that updates both shifted-border and merged-border caches), they're really one concern. Merging avoids a forced "helper X calls into helper Y" coupling that's worse than a slightly larger combined helper. Cohesion via shared state trumps the agent's preferred number of clusters.

- **Agent merged, code says split.** If the agent puts methods together because they share a field, but reading the source shows two distinct algorithms that just happen to touch the same data structure for unrelated reasons (e.g., one builds a CSS rule, another searches for a CSS rule), splitting them produces clearer helpers even though they share a map. The shared field can move to one helper and the other becomes a consumer.

Read the actual method bodies for the two largest clusters before locking in a design. A 5-minute read of the source resolves most of these.

### 3.6. State-machine god classes don't decompose by field clustering

A god class that's actually a **state machine** — where many methods mutate the same handful of fields in lockstep, atomically — looks like a clustering target but fights every extraction. The pattern: 5-15 `on<Event>()` methods, each of which reads several state fields and writes back to several state fields. The agent will happily cluster the state fields by usage, but the resulting "helpers" are pure state-holders that every `on<Event>()` method has to call in lockstep. You traded one class with N fields for one class with N helpers, plus N times more orchestration code.

**How to recognize the pattern:**

- Most public/protected methods are named `on<Something>` or `handle<Something>`.
- Most fields appear in most methods' read AND write sets.
- The "clusters" the agent identifies are each ~1-3 fields and a handful of accessors.
- The agent's flavor verdict says "decomposition" but the extractions are all small state-encapsulation wrappers.

**What to do instead:**

- Document the verdict in the journal: "Class X is a state machine; field-locality clustering doesn't decompose it cleanly."
- If refactoring is still desired, the right move is **architectural**: design a single value object (e.g. `SelectionState`) that encapsulates all the state atomically; the on-event methods become "compute next state from event + current state." That's a different shape of refactor — bigger than a slice, owned by the operator, not by this skill.
- Until then, the class stays as-is. That's Rule D's "tightly coupled state that resists further decomposition without behavior change" outcome.

If half or more of the agent's proposed extractions feel like encapsulation slices, ask the question: is this a state machine? If yes, stop and surface the verdict.

### 4. Output

Write the cluster report verbatim to standard output. Don't save to disk unless asked.

If the cluster report suggests a clear next move (e.g. "extract the three smallest clusters in slice N"), state that observation in one sentence after the report. Don't elaborate — that's `refactor-slice-plan`'s job.

## When the report reveals the class isn't a god class

If only one cluster forms, or the class is < 300 LOC, surface that to the operator: "this isn't a god-class candidate, suggest a different target." Don't try to invent concerns that aren't there.
