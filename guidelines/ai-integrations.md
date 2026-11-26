# AI Integrations

`vaadin-ai-integrations-flow-parent` is not a regular component wrapper group:
it provides server-side support for building AI-powered applications
(`AiOrchestrator`, LLM provider integrations, AI controllers for existing
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
- `AiExtensionsLicense.check()` must be called from every public entry point
  of the extensions module — every public controller constructor and public
  tool factory. It only enforces in dev mode; do **not** re-add a `cvdlName`
  manifest entry to the pom — Flow's production-build license validation
  flags any jar carrying one, and the module is covered by the
  `com.vaadin:vaadin` umbrella instead.

The group has no `-testbench` module and, apart from `FormFieldMarker`'s
`@JsModule`, no frontend resources or connectors.

## Architecture

- `AiOrchestrator` is a non-visual coordination engine, not a `Component` —
  it is never added to a layout. Construction is builder-only
  (`AiOrchestrator.builder(provider, systemPrompt)`).
- UI components are decoupled through the interfaces in the `.ui` package
  (`AiInput`, `AiMessageList`, `AiMessage`, `AiFileReceiver`) so
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
  mainly `AiController`, `LlmProvider.ToolSpec`,
  `DatabaseProvider`/`DatabaseProviderAiTools`, and the shared value types.
  There is no SPI, registry, or reflection between the modules; keep the
  boundary that narrow.
- Vendor LLM integrations (Spring AI, LangChain4j) live in core behind
  `optional` dependencies. `reactor-core` is a non-optional dependency
  because `Flux` is part of the `LlmProvider` API. A new vendor provider
  follows the same shape: optional dependency, `transient` model fields,
  documented as not serializable.
- A vendor integration uses the vendor's public API only. Because the
  dependency is optional, the application picks the vendor version, so a
  member that disappears fails at the application's site, not in this
  repository's build. LangChain4j marks such types `@Internal`
  (`AiServiceContext`, `ToolService`, `CapturedContextSupport`).
- `LangChain4jLlmProvider` runs each turn through a LangChain4j `AiServices`
  service, and the customizer constructors hand the application the builder so
  it can add RAG, MCP tool providers, guardrails and so on. The builder has
  setters only, so the provider never reads back what the customizer
  configured: it sets its replaceable defaults (memory, the made-up tool
  result) before the customizer runs, and adds what must not
  be replaced afterwards (its tool provider and listeners, which add up with
  the customizer's, and the constructor's model). The provider cannot see
  the tools the customizer adds, so it does not resolve a name clash with
  the tools of the turn: LangChain4j rejects the turn, and the class Javadoc
  asks for distinct names. Tools added in the
  customizer keep LangChain4j's tool error handling, like any vendor `@Tool`
  tool (see the error hygiene rule below). Only a provider without a
  customizer sets the error handlers, which none of its own tools reach, so
  that LangChain4j logs no notice asking for them. With a customizer,
  LangChain4j logs that notice unless the customizer sets both handlers, also
  when it adds no tools, since the provider's tool provider counts as tools.
  The provider does not set handlers of its own choosing there. Any explicit
  handler decides how the application's tools fail, and that stays with the
  vendor's defaults and the application, as it does for the tools of a
  `ChatClient` given to `SpringAiLlmProvider`. LangChain4j plans to change
  these defaults, which is what the notice announces. A different default is
  a decision for both providers together. The checks of a turn
  (cancellation, and the orchestrator's system prompt, which
  wins over a customizer's `systemMessage`) sit in a proxy around that model
  instead of in a builder setting the application might also use. The session
  context goes into the user text before the service call, as with
  `SpringAiLlmProvider`, so retrieval and input guardrails see it. The proxy
  does not pass on to the service the model response of a cancelled turn: the
  service would otherwise run the tools the response requests, including the
  customizer's. On a streaming model, the held-back response is replaced
  with an error on the response handler, which ends the turn. That does not
  work once the service has the answer of the turn: a model call after it
  belongs to an output guardrail's reprompt, and LangChain4j waits for the
  response of the reprompt's first call on the thread that runs the
  guardrails, behind a latch that only a complete response releases
  (`StreamingToSynchronousChatExecutor`). An error there would end the turn
  and leave that thread blocked. So from the answer on, the provider ends a
  cancelled turn itself and gives the handler an answer without text or tool
  calls, on which the guardrails finish. A
  model call and a memory access do not say which turn they belong to, so the
  provider binds the turn to the thread that starts it and to each streaming
  callback, where the follow-up model calls happen. That keeps a cancelled
  turn that is still running from passing the next turn's checks or touching
  its tool traffic. With output guardrails, LangChain4j replays the original
  streamed tokens after the guardrails ran, also when a guardrail rewrote the
  answer, so the provider passes on the completed response's text instead.
  Retrieval and input guardrails run inside the service call that starts a
  turn, also for a `TokenStream`, so `setBackgroundExecution(true)` moves the
  start of a streaming turn off the subscribing thread too. A moderation model
  set in the customizer is not used: LangChain4j moderates only `@Moderate`
  methods, never checks the result for a `TokenStream`, and rejects a
  `@Moderate` method when no moderation model is set. An input guardrail does
  that job.
- Provider chat memory holds the user messages and the assistant's final
  answer of each turn. Tool calls and results are sent to the model only
  within their own turn, with both built-in providers, so a controller must
  not rely on the model remembering a previous turn's tool result; the
  instruction tools already tell the model to read the state again each
  turn. LangChain4j's loop writes every tool message into the chat memory,
  so `LangChain4jLlmProvider`'s own memory (`TurnScopedChatMemory`) keeps
  them in a list of the running turn instead. The turn's answers go to that
  list too, after its tool messages, because an output guardrail that
  reprompts sends the memory again and the model still needs the tool
  results. With
  `storeRetrievedContentInChatMemory(false)`, LangChain4j stores the user
  message as the template rendered it, without the attachments the provider
  passes separately, so the provider's memory adds them back, to the first
  user message the turn stores only. A memory rewrite within the turn stores
  earlier questions again, and one of them may have the same text. When
  LangChain4j rolls back tools (`compensateOnToolErrors`), it rewrites the
  memory with `set()` within the turn. The provider's memory puts each
  message back where it was and stores it as given, so an earlier answer
  stays ahead of the turn's question and a stored user message does not get
  the attachments of the turn. With output
  guardrails, LangChain4j leaves the answer in the memory as the model gave
  it, also when a guardrail rewrote it or accepted a later one after a
  reprompt. The provider replaces it with the answer the turn delivered, so
  that later turns and a history restore show the model the conversation the
  user saw. `SpringAiLlmProvider` gets the same from Spring AI: its memory
  advisor sees a response after the advisors ordered behind it changed it. A
  turn that a guardrail fails keeps the rejected answer in the memory unless
  the guardrail fails it with message removal. That is LangChain4j's default
  and LangChain4j's option, and both are left as they are. A memory set in
  the customizer replaces the provider's own and gets LangChain4j's behavior.
  `setHistory` leaves such a memory as it is, as `SpringAiLlmProvider` leaves
  the memory of a `ChatClient` that brings its own. The application owns it,
  and it may hold more than a restore writes back: tool calls, their results
  and retrieved content, in a store that may be persistent.
  `LangChain4jLlmProvider` also leaves a final answer without text out of the
  memory. A model may answer that way once a tool call has done the work,
  LangChain4j hands the answer over as an `AiMessage` with a `null` text, and
  OpenAI's Chat Completions API rejects a replayed assistant message that has
  neither content nor tool calls, which would fail every later turn of the
  conversation.
- The response stream carries text only; everything else the model said
  about the turn goes to `LlmRequest.metadataSink()` as `ResponseMetadata`.
  A provider publishes whenever it learns more — each publish carries the
  state of the turn so far and replaces the previous one, so a turn that
  fails midway has still reported what was observed. Finish reasons are
  relayed as the underlying framework words them and are never mapped to a
  Vaadin enum: every vendor words them differently and keeps adding values
  (the same truncation is `length` on OpenAI and `max_tokens` on Anthropic),
  so any fixed set of our own would go stale. LangChain4j shows what mapping
  costs — it collapses the vendor's word onto a five-constant enum before we
  see it, per integration and losing what it does not recognize, so an
  unrecognized value arrives as `null` from its OpenAI integration and as
  `OTHER` from its Anthropic one. Framework code therefore never branches on
  the content of a finish reason: checks that must tell a completed turn from
  a truncated one use structure instead — a missing finish reason, or tool
  calls still pending.

## Threading

- Async work is Reactor (`Flux`), not `CompletableFuture`. Streamed tokens
  and all other UI mutation from provider threads go through `ui.access()`.
- Work resumed later (e.g. a postponed prompt) uses `ui.accessLater(...)`
  instead of `ui.access` — a plain `access` on a UI that detaches after
  enqueue silently drops the task and leaves the orchestrator stuck busy.
- Interceptor/postponement timers run on `Schedulers.boundedElastic()`,
  never `parallel()`: the end-of-turn hooks may run inline on the timer
  thread and application code in them may block.
- The end-of-turn hooks, `AiController.onResponse` and then the
  `ResponseListener`, run as one `ui.accessLater` task whether or not a
  controller is attached, so the listener has a single threading contract
  (session locked) and a detached UI still gets the listener.
- The orchestrator's `isProcessing` busy flag must be released exactly once
  per prompt. The field comment in `AiOrchestrator` enumerates every release
  path; any new way for a prompt to end must be added there and must decide
  who releases the flag.

## Serialization and reconnect

- `AiOrchestrator` is serializable; the provider, tools, and controller are
  `transient` by design. After deserialization the application restores them
  with `orchestrator.reconnect(provider).withController(...).apply()`.
- The `RequestInterceptor` **is** serialized with the orchestrator —
  interceptor lambdas must capture only serializable state.
- Intentionally non-serializable classes are excluded in the module's
  `ClassesSerializableTest` subclass; exclusions carry inline comments
  explaining why. Add both when introducing one.

## LLM tools

- Tool names are `snake_case` and must match `^[a-zA-Z0-9_-]{1,64}$`, and a
  declared parameters schema must be a JSON object (both validated on
  registration, so a broken definition fails fast and names the tool).
- Each controller keeps its tool definitions in a sibling `XxxAiTools`
  factory class with a nested `Callbacks` interface implemented by the
  controller, keeping tool JSON and descriptions decoupled from the
  component type.
- Instructions the model must always see go into a tool's *description*
  (the `get_*_instructions` tools), with `execute()` returning the same
  text — the model reads the manifest without a call.
- Keep tool definitions identical from turn to turn. LLM providers cache the
  prompt prefix in the order tools, system prompt, messages, and any change
  to a tool name, description or schema invalidates all of it. Per-turn
  content therefore never goes into a tool description or the system
  prompt: the session context (`Builder.withMetadata`) travels in
  `LlmRequest.sessionContext()` and the providers append it to the user
  message, at the very end of the prompt.
- Tools validate their input eagerly so errors round-trip to the LLM within
  the turn, but stage the result and apply it once in `onResponse(null)`;
  `onResponse(error)` discards the pending state and keeps the last good
  render. A throw from `onResponse(null)` is the turn's error: the
  orchestrator hands the `ResponseListener` the throw instead of a success
  event, so a controller must not swallow its own apply failures.
- Error hygiene toward the model: only the message of a
  `ToolException` (public, in `com.vaadin.flow.component.ai.provider`) is
  forwarded verbatim — throw it, from built-in tool code or an application's
  `DatabaseProvider`/`ToolSpec`, only with deliberately-safe text. Any other
  exception is caught, logged, and replaced with a generic string so SQL,
  schema names, or paths never leak. Tool failures are returned as strings
  to the model, never thrown. This contract covers only framework-agnostic
  `ToolSpec` tools; vendor-annotated tools (LangChain4j/Spring AI `@Tool`)
  are executed by the vendor framework, whose default error handling relays
  the raw message of any exception to the model — they are deliberately out
  of scope, so route error-sensitive tools through `ToolSpec`.
- The tool-calling loop is bounded by the vendor framework that drives it,
  never by the orchestrator or a provider. A limit in a provider is logic on
  top of the vendor's default, and it has to stop tools the provider does not
  own, such as those a customizer adds. `LangChain4jLlmProvider` leaves
  LangChain4j's `maxToolCallingRoundTrips` at its default, which the
  application changes in the customizer, and passes LangChain4j's error on as
  the error of the turn. LangChain4j counts rounds, not calls, and has no
  limit per tool. With a streaming model it throws the error out of the
  completion handler, so the turn ends when the model reports that failure
  to the error handler, as LangChain4j's model integrations do and
  `mockStreamingResponsesOnOtherThreads` in the provider tests copies. The
  tool traffic of the failed
  turn is discarded with it, so the next turn continues from the chat memory
  as the last completed turn left it; a cancelled turn likewise stops calling
  the model and runs no tool for a response that arrives after the
  cancellation. `SpringAiLlmProvider` adds no cap either: Spring AI bounds the
  loop itself and ends the turn with a reply it writes itself, carrying the
  finish reason `toolCallLimitExceeded`. The provider passes that reply on
  like any other answer, so the turn completes and the controllers apply what
  the model staged. The two providers therefore end such a turn differently,
  each the way its vendor does. Turning Spring AI's reply into a failed turn
  would be Vaadin deciding what one of the vendor's finish reasons means, and
  overriding what the application can configure on its own `ChatClient`.
  There is no Vaadin exception type for an exceeded limit either, for the
  reason finish reasons are not mapped: the limit is the vendor's, and Vaadin
  gives a type only to a condition it enforces itself. The application
  recognizes the case with the vendor's own means, such as Spring AI's finish
  reason in the response metadata. LangChain4j's error is a plain
  `RuntimeException` that only its message identifies, and the provider does
  not match that message to give it a type.
- Never send secrets to the LLM — `FormAiController` auto-ignores password
  fields; preserve that property for new field handling.

## Feature flag

The umbrella flag `aiComponents` gates the whole group. Unlike a component
flag it is checked in exactly one place — `AiOrchestrator` on the first
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
- No unit or integration test uses a real LLM or API key. Unit tests mock `LlmProvider`
  (`Flux.just(...)` + `ArgumentCaptor<LlmRequest>` to assert the built
  request); provider tests mock the vendor model types and hand-build
  responses; IT views define small provider classes inline — an echo
  provider that splits the reply into tokens to simulate streaming, or a
  background-thread provider (with `PushMode.AUTOMATIC` on the view) to
  reproduce real tool-call timing.
- ITs are smoke tests proving the wiring reaches the browser; the scenario
  matrix belongs in unit tests asserting server-side state (see
  `AiFieldMarkerIT` vs `FormAiControllerTest` for the established split).
- Log-output assertions use the `slf4j-test` `TestLogger`.
- The only place a real LLM is used is the unpublished
  `vaadin-ai-integrations-flow-benchmark` module. Its JUnit scenarios drive the
  controllers server-side through `AiOrchestrator` with the Spring AI
  provider, score the resulting state deterministically (field values and the
  sources recorded for them, rows returned by the produced SQL, chart type and
  series), and repeat each scenario to absorb nondeterminism. The suite is
  skipped unless `AI_BENCHMARK_MODEL` is set, so it never runs in PR
  validation. See the module README for configuration and output.

## Other conventions

- The long implementation comments in `AiOrchestrator`,
  `FormAiController`, `FormFieldMarker`, and `BinderReflection` explaining
  ordering, threading, and catch decisions are load-bearing — keep them,
  and comment new non-obvious code in these files the same way.
- `@since` tags are reconciled against actual release history — add an
  accurate one to every new public member, including nested ones.
