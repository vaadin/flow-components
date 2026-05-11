# CI Validation Workflow Strategies

Three strategies have been considered for the sharded validation pipeline.
Each makes a different trade-off between **feedback speed**, **resource usage**,
and **build status clarity**.

---

## Strategy A — Cancel-on-failure with a fast-tests gate

```
                 ┌─────────────┐
                 │    BUILD    │
                 └──────┬──────┘
                        │
            ┌───────────┴───────────┐
            │                       │
            ▼                       ▼
┌───────────────────────┐   ┌───────────────────┐
│       FAST-TESTS      │   │    PACKAGE-WAR    │
│  ┌────────┬─────────┐ │   └────────┬──────────┘
│  │  UNIT  │   WTR   │ │            │
│  └────────┴─────────┘ │            ▼
└───────────┬───────────┘   ┌───────────────────┐
            │ (on failure)  │   ITs (shards)    │
            ▼               └────────┬──────────┘
┌───────────────────┐                │
│    CANCELLER      │                │
└─────────┬─────────┘                │
          └────────────┬─────────────┘
                       ▼
              ┌───────────────────┐
              │  "Collect tests"  │
              └───────────────────┘
```

Unit and WTR tests run inside a matrix job (`fast-tests`). Package-WAR and the
IT shards start in parallel, immediately after BUILD. If either unit or WTR
fails, the matrix emits a dedicated `canceller` job that calls
`gh run cancel`, aborting all running IT shards.

**Pros**
- Developers see exactly which fast test type failed (unit vs WTR is a
  separate named job).
- Cancellation stops wasting IT runner minutes the moment a fast test breaks.
- `Collect tests` still runs and surfaces the failure report.

**Cons**
- The overall workflow run is marked **cancelled**, not **failed**. Status
  checks that look for a red build won't fire; merge queues and branch
  protection rules may need to be configured to treat `cancelled` as a
  blocking state.
- IT shards may already be running (and consuming runners) for a few minutes
  before the canceller fires, because Package-WAR and ITs start in parallel
  with fast tests.
- The extra `canceller` job adds a small layer of indirection that is
  non-obvious to new contributors.

---

## Strategy B — Sequential gate (traditional)

```
           ┌───────────┐
           │   BUILD   │
           └─────┬─────┘
                 │
                 ▼
┌────────────────────────────────────────────┐
│  ┌────────────┐ ┌───────────┐ ┌──────────┐ │
│  │ UNIT-TESTS │ │ WTR-TESTS │ │PACKAGE-  │ │
│  └────────────┘ └───────────┘ │WAR       │ │
│                                └──────────┘ │
└─────────────────────┬──────────────────────┘
                      │
                      ▼
           ┌──────────────────────┐
           │  INTEGRATION-TESTS   │
           │      (shards)        │
           └──────────┬───────────┘
                      │
                      ▼
           ┌─────────────────────┐
           │       RESULTS       │
           └─────────────────────┘
```

Unit tests, WTR tests, and Package-WAR all run in a first parallel stage.
IT shards only start once **all three** succeed. If any job in the first stage
fails the others in that stage are cancelled, the IT stage is skipped
entirely, and the workflow is marked **failed**.

**Pros**
- The overall run is always **red** on any failure — no ambiguity for branch
  protection rules, merge queues, or status-check dashboards.
- IT runners are never started if fast tests or the WAR build fail, saving
  the most resources.
- Simple mental model: stage 1 must be green before stage 2 begins.

**Cons**
- IT shards must wait for the slowest job in stage 1 to finish (~2 min for
  unit tests on a warm cache) even when only the WAR build matters for them.
- If unit tests are slow, the overall wall-clock time is longer than necessary.

---

## Strategy C — Parallel everything, cancel-on-failure (current `main`)

```
                 ┌─────────────┐
                 │    BUILD    │
                 └──────┬──────┘
                        │
          ┌─────────────┼──────────────────┐
          │             │                  │
          ▼             ▼                  ▼
  ┌──────────────┐ ┌──────────────┐ ┌───────────────────┐
  │  UNIT-TESTS  │ │  WTR-TESTS   │ │    PACKAGE-WAR    │
  │ (cancel on   │ │ (cancel on   │ └────────┬──────────┘
  │   failure)   │ │   failure)   │          │
  └──────┬───────┘ └──────┬───────┘          ▼
         │                │        ┌───────────────────┐
         │                │        │   ITs (shards)    │
         │                │        └────────┬──────────┘
         └────────────────┼─────────────────┘
                          │ (always, if build succeeded)
                          ▼
                ┌───────────────────┐
                │  "Collect tests"  │
                └───────────────────┘
```

All three jobs (unit, WTR, package-WAR) start immediately after BUILD. IT
shards start as soon as Package-WAR finishes, without waiting for unit or WTR
results. If unit or WTR fail they each call `gh run cancel` directly (no
separate canceller job), aborting the whole workflow.

**Pros**
- IT shards start at the earliest possible moment — no waiting for unit tests.
- On a green build this gives the shortest possible wall-clock time.
- No extra indirection layer (no dedicated canceller job).

**Cons**
- Same as Strategy A: the workflow is marked **cancelled**, not **failed**.
- Worst-case resource waste: IT shards may have been running for several
  minutes (the full Package-WAR + shard startup time) before the cancellation
  arrives from a failing unit/WTR job.
- Two independent cancellers (unit and WTR) can race; if both fire at once
  the second cancel is a no-op but adds noise.
- Slightly harder to reason about: the dependency graph has no explicit gate
  between fast tests and ITs.

---

## Summary

| | Strategy A | Strategy B | Strategy C (current) |
|---|---|---|---|
| Build status on failure | `cancelled` | `failed` | `cancelled` |
| ITs blocked until fast tests pass | No (parallel) | **Yes** | No (parallel) |
| IT runner waste on fast-test failure | Low (canceller) | **None** | Medium |
| Extra wall-clock time (green builds) | Minimal | ~2 min | **None** |
| Complexity | Medium | **Low** | Low |
