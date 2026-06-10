# Documenting

## Class Javadoc

A brief description, then a longer explanation of key concepts, use cases, and
limitations. Include a short code example where useful, and a `Validation`
section for field components (built-in constraints, `setManualValidation`, how
errors surface).

## Method Javadoc

- Describe WHAT it does and WHY a caller uses it — not how it works internally.
- Mention side-effects (DOM updates, re-rendering) and document exceptions
  (`@throws …`).
- For boolean parameters, say what `true` vs `false` does.
- Cross-link related methods with `{@link …}`.

## @since and @deprecated

- `@since` on every new public class and method.
- `@deprecated` with a `{@link}` / `@see` pointing to the replacement.
  Deprecated APIs stay at least one major version before removal.

## README.md

Each parent module has a `README.md` with a brief description, a link to the
vaadin.com docs, a minimal `new Component(...)` example, the `<dependency>`
snippet, and the license notice.
