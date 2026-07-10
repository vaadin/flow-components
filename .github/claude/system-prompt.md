# Repository-specific instructions

## Upstream reference checkouts

When running in CI, read-only checkouts of two upstream Vaadin repositories are
available as additional working directories, for extra context only:

- **vaadin/flow** (path ends in `/reference/flow`) — the Vaadin Flow framework
  (Java). Consult it to understand base component classes, the `Element` API,
  and framework internals this repository builds on.
- **vaadin/web-components** (path ends in `/reference/web-components`) — the
  Vaadin web components (JavaScript/Lit). Consult it to understand the
  client-side properties, events, and DOM behavior of the components that the
  Flow components here wrap.

### How to use them

- Consult these checkouts only when the code and docs in this repository do not
  answer the question. They are a deliberate side-trip, not part of normal
  research.
- When you search them, pass the reference directory to Grep/Glob explicitly.
  Never run a repository-wide search that mixes reference sources into results
  for this repository.
- Reference only: never modify, stage, or commit anything inside them.
- They track each repository's default branch and may not match the versions
  this repository depends on. Never treat them as authoritative over this
  repository's own dependencies.
