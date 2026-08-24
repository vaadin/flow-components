# AI Components

`vaadin-ai-components-flow-parent` is not a regular component wrapper group:
it provides server-side support for building AI-powered applications
(`AIOrchestrator`, LLM provider integrations, AI controllers for existing
components). The general chapters still apply; this chapter covers only what
works differently here.

## Module split and licensing

The group publishes two `-flow` artifacts — the only module group in the repo
that does:

- `vaadin-ai-core-flow` — Apache 2.0. Orchestrator, provider abstraction,
  UI-decoupling interfaces, shared value types.
- `vaadin-ai-extensions-flow` — commercial. Higher-level extensions built
  on top of core, such as the AI controllers for existing components.

Consequences of the split:

- Spotless enforces a different license header per module (Apache in core,
  commercial in extensions). Moving a file between the modules requires
  swapping the header.
- The pom-consolidation script (`scripts/updateJavaPOMs.js`) detects the
  extensions module as commercial from the `<licenses>` block in its pom —
  keep that block intact.
- Shared value types that appear in public API (e.g. the source-tracking
  types `ValueSource`, `SourceExtract`, `PageRegion` in
  `com.vaadin.flow.component.ai.common`) live in core even when their only
  consumer is a commercial controller, so the data types stay
  Apache-licensed. Put new shared data types there too.
- `AIExtensionsLicense.check()` must be called from every public entry point
  of the extensions module — every public controller constructor and public
  tool factory. It only enforces in dev mode; do **not** re-add a `cvdlName`
  manifest entry to the pom — Flow's production-build license validation
  flags any jar carrying one, and the module is covered by the
  `com.vaadin:vaadin` umbrella instead.

The group has no `-testbench` module and, apart from `FormFieldMarker`'s
`@JsModule`, no frontend resources or connectors.

## Architecture

- `AIOrchestrator` is a non-visual coordination engine, not a `Component` —
  it is never added to a layout. Construction is builder-only
  (`AIOrchestrator.builder(provider, systemPrompt)`).
- UI components are decoupled through the interfaces in the `.ui` package
  (`AIInput`, `AIMessageList`, `AIMessage`, `AIFileReceiver`) so
  applications can plug in non-Vaadin UI. Builder methods accept either the
  interface or the concrete Flow component; concrete components are adapted
  by package-private wrappers — extend that pattern rather than referencing
  component types from the orchestrator.
- Builder conventions: required arguments fail fast with
  `Objects.requireNonNull`; calling a `withX` method twice logs a warning
  via `warnIfAlreadySet`. Providers, UI components, and controllers are
  claimed exclusively on `build()` — reusing one in a second orchestrator
  throws `IllegalStateException`.
- The extensions module plugs into core only through core's public API —
  mainly `AIController`, `LLMProvider.ToolSpec`,
  `DatabaseProvider`/`DatabaseProviderAITools`, and the shared value types.
  There is no SPI, registry, or reflection between the modules; keep the
  boundary that narrow.
- Vendor LLM integrations (Spring AI, LangChain4j) live in core behind
  `optional` dependencies. `reactor-core` is a non-optional dependency
  because `Flux` is part of the `LLMProvider` API. A new vendor provider
  follows the same shape: optional dependency, `transient` model fields,
  documented as not serializable.

## Threading

- Async work is Reactor (`Flux`), not `CompletableFuture`. Streamed tokens
  and all other UI mutation from provider threads go through `ui.access()`.
- Work resumed later (e.g. a postponed prompt) uses `ui.accessLater(...)`
  instead of `ui.access` — a plain `access` on a UI that detaches after
  enqueue silently drops the task and leaves the orchestrator stuck busy.
- Interceptor/postponement timers run on `Schedulers.boundedElastic()`,
  never `parallel()` — listeners are allowed to block.
- The orchestrator's `isProcessing` busy flag must be released exactly once
  per prompt. The field comment in `AIOrchestrator` enumerates every release
  path; any new way for a prompt to end must be added there and must decide
  who releases the flag.

## Serialization and reconnect

- `AIOrchestrator` is serializable; the provider, tools, and controller are
  `transient` by design. After deserialization the application restores them
  with `orchestrator.reconnect(provider).withController(...).apply()`.
- The `RequestInterceptor` **is** serialized with the orchestrator —
  interceptor lambdas must capture only serializable state.
- Intentionally non-serializable classes are excluded in the module's
  `ClassesSerializableTest` subclass; exclusions carry inline comments
  explaining why. Add both when introducing one.

## LLM tools

- Tool names are `snake_case` and must match `^[a-zA-Z0-9_-]{1,64}$`
  (validated on registration).
- Each controller keeps its tool definitions in a sibling `XxxAITools`
  factory class with a nested `Callbacks` interface implemented by the
  controller, keeping tool JSON and descriptions decoupled from the
  component type.
- Instructions the model must always see go into a tool's *description*
  (the `get_*_instructions` and session-context tools), with `execute()`
  returning the same text — the model reads the manifest without a call.
- Tools validate their input eagerly so errors round-trip to the LLM within
  the turn, but stage the result and apply it once in `onResponse(null)`;
  `onResponse(error)` discards the pending state and keeps the last good
  render.
- Error hygiene toward the model: forward only deliberately-safe validation
  messages verbatim; replace any other exception with a generic string so
  SQL, schema names, or paths never leak. Tool failures are returned as
  strings to the model, never thrown.
- Never send secrets to the LLM — `FormAIController` auto-ignores password
  fields; preserve that property for new field handling.

## Feature flag

The umbrella flag `aiComponents` gates the whole group. Unlike a component
flag it is checked in exactly one place — `AIOrchestrator` on the first
prompt (the orchestrator has no attach lifecycle) — and the result is
memoized per instance. The extensions module has no checks of its own: the
controllers are only reachable through the orchestrator. A new entry point
that bypasses the orchestrator needs its own check.

## Client-side contracts

There are no connectors. The one client integration, `FormFieldMarker`,
drives `<vaadin-ai-field-marker>` through the raw `Element` API on purpose:
the web component marks/unmarks purely by being attached to / removed from
a field, and a plain element in the state tree is recreated verbatim on
detach/re-attach. Its i18n is set per marker instance, not via the web
component's page-global defaults, so multiple controllers on a page cannot
clobber each other.

Contracts of the AI-related web components that the server side must
respect (none of them is client-side feature-flagged — gating is
server-side only):

- Streaming renders by resending the **full accumulated text**, not deltas:
  `vaadin-markdown` diffs the new content into the existing DOM, and
  `vaadin-message-list` re-renders from a full `items` reassignment —
  mutating an item in place does nothing.
- `vaadin-message-list` owns stick-to-bottom scrolling during streaming
  (including respecting a user who scrolled up) — do not reimplement it
  server-side.
- `submit` on `vaadin-message-input` and `attachment-click` on
  `vaadin-message-list` do not bubble — listen on the element itself. The
  input clears its own value on submit; the server must not clear it again.
- `ai-field-revert` bubbles from the **field**, not the marker, and the
  server is responsible for restoring the value.
- While `working` is set, the marker forces client-side read-only and
  delays value assignment on the field — server code must not fight these
  side effects.

## Testing

- Unit and integration tests use the same JUnit setup as the rest of the
  repo. Both published modules need the Mockito java-agent `surefire.argLine`
  (already in the poms) because `EnableFeatureFlagExtension` uses
  `mockStatic`.
- No test ever uses a real LLM or API key. Unit tests mock `LLMProvider`
  (`Flux.just(...)` + `ArgumentCaptor<LLMRequest>` to assert the built
  request); provider tests mock the vendor model types and hand-build
  responses; IT views define small provider classes inline — an echo
  provider that splits the reply into tokens to simulate streaming, or a
  background-thread provider (with `PushMode.AUTOMATIC` on the view) to
  reproduce real tool-call timing.
- ITs are smoke tests proving the wiring reaches the browser; the scenario
  matrix belongs in unit tests asserting server-side state (see
  `AIFieldMarkerIT` vs `FormAIControllerTest` for the established split).
- Log-output assertions use the `slf4j-test` `TestLogger`.

## Other conventions

- The long implementation comments in `AIOrchestrator`,
  `FormAIController`, `FormFieldMarker`, and `BinderReflection` explaining
  ordering, threading, and catch decisions are load-bearing — keep them,
  and comment new non-obvious code in these files the same way.
- `@since` tags are reconciled against actual release history — add an
  accurate one to every new public member, including nested ones.
