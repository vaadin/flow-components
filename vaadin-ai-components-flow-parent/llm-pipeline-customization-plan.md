# LLM pipeline customization with vendor features: investigation plan

Status: investigation plan, 2026-10-28, with the prototype results of the
same day in section 8. Sections 1 to 7 are the plan as it was written before
the prototypes; where a prototype answered a question, the answer is in
section 8 and the earlier text is marked. The prototypes live on branches,
nothing is merged.
Module: `vaadin-ai-core-flow` (experimental, breaking changes allowed).
Vendor versions the facts below were checked against: LangChain4j 1.22.0 and
Spring AI 2.0.2, the versions pinned in the root pom.

## Purpose

An application that uses the AI components should be able to add
vendor-provided functionality to its LLM pipeline (retrieval-augmented
generation, MCP tools, guardrails, moderation, observability, model options)
with the vendor's own API, and still use the built-in providers for everything
the orchestrator needs from a provider. Today that works for Spring AI through
the `ChatClient` constructor of `SpringAILLMProvider` and does not work for
LangChain4j at all. The only LangChain4j route is a custom `LLMProvider`, which
means re-implementing attachment conversion, tool bridging, memory, the
tool-calling loop and metadata reporting.

This document describes the current state, lists the feasible approaches with
usage examples, analyses which orchestrator and provider APIs become redundant
once the pipeline is customizable, and ends with the spikes to run next. It is
a plan to continue research from, not a design decision.

## Decisions this plan builds on

- The vendor framework owns chat memory and the tool-calling loop in every
  path, including the plain "give me a model" path. One code path per
  provider. Vaadin policies that this gives up are listed per option, together
  with how they can be re-established through vendor configuration.
- Every orchestrator and provider API that overlaps with a vendor pipeline
  feature is evaluated as a removal or redesign candidate, not only the system
  prompt and `withTools()`.
- API parity between the two providers is preferred, not required.
- Breaking changes in `vaadin-ai-core-flow` are allowed.

## 1. Problem

The orchestrator sends each turn to an `LLMProvider` as an `LLMRequest`: user
message, attachments, system prompt, `@Tool` objects, controller `ToolSpec`
tools, session context and a metadata sink. The built-in providers turn that
into a vendor request, run the turn and stream text back.

Where a pipeline feature can be attached today:

| Feature | `SpringAILLMProvider` | `LangChain4JLLMProvider` |
| --- | --- | --- |
| RAG | `ChatClient` constructor, `QuestionAnswerAdvisor` | not possible |
| MCP tools | `ChatClient` constructor, `defaultTools(SyncMcpToolCallbackProvider)` | not possible (documented as "needs a custom provider") |
| Input or output guardrails | `ChatClient` constructor, `SafeGuardAdvisor` or a custom advisor | not possible |
| Moderation | custom advisor | not possible |
| Model options per client | `ChatClient.Builder.defaultOptions` | only what the model object was built with |
| Observability | `ChatClient` constructor, observation registry | `ChatModelListener` on the model object |
| Tool call limits | `ChatClient` constructor, `ToolCallingAdvisor` | provider setters |
| Tool error handling of `@Tool` objects | vendor default | vendor default (`DefaultToolExecutor`) |

The LangChain4j provider is built from a `ChatModel` or `StreamingChatModel`
and runs its own memory and tool-calling loop. LangChain4j's pipeline features
all live one level up, in `AiServices`, so the provider has nowhere to attach
them. The Spring AI provider already sits on `ChatClient`, where Spring AI's
features live, which is why it works there.

A second, smaller problem is the split between the orchestrator and the
vendor client on the Spring side: the orchestrator sends its system prompt on
every request, which overrides `defaultSystem()` on the client, and
`withTools()` objects are passed per request while MCP tools are registered on
the client. The pipeline is configured in two places.

## 2. What the provider does on top of the vendor framework

Whatever approach is chosen, the built-in providers keep a bridging role. The
orchestrator and the controllers are framework-agnostic, so these
responsibilities cannot move to the vendor builder:

| Responsibility | Today | Needed by |
| --- | --- | --- |
| Controller tools per turn | `ToolSpec` to `ToolSpecification` + `ToolExecutor` (LangChain4j) or `ToolCallback` (Spring AI), placeholder schema for parameterless tools | `AIController`, built-in controllers |
| Tool error policy for `ToolSpec` tools | only a `ToolException` message reaches the model, anything else becomes a generic string | controllers, `DatabaseProvider` |
| Attachments | `AIAttachment` to `Content` (LangChain4j) or `Media` (Spring AI), text files inlined as a tagged block | file receiver |
| Session context | appended to the user message text at the end of the prompt, to keep the prompt prefix cacheable | `Builder.withMetadata` |
| Streaming and blocking modes, background execution | per provider | orchestrator threading |
| Response metadata | finish reason and token usage summed over the tool round trips, published through `LLMRequest.metadataSink()` | `ResponseListener`, controllers |
| History restore | `setHistory()` writes the conversation into the provider's memory | `withHistory`, `reconnect` |
| Memory policy | user message and final answer only, tool traffic stays within its turn (both providers) | prompt size, memory window |
| Tool call limits | 40 per tool and 150 per turn, `ToolCallLimitExceededException` | `ResponseListener`, controllers |
| Cancellation | checked before every model call and tool round, so a turn the orchestrator abandons (its 600 s timeout, a cancelled subscription) stops calling the model and the tools | orchestrator timeout, cost control |

The memory policy, tool call limit and cancellation rows are the Vaadin
behaviours that the decision "vendor owns memory and the loop" puts in
question. The rest survives any of the approaches below, because the
conversion code stays in the provider.

### Evaluation criteria

Must have:

- RAG, MCP tools and guardrails configurable with the vendor's own types, for
  both frameworks, without implementing `LLMProvider`.
- Every bridge responsibility above stays available, or its loss is documented
  as a deliberate change.
- One mental model for both frameworks: the shape of the API matches even
  where the types differ.
- A documented answer for memory ownership and for `setHistory()`,
  `withHistory()` and `reconnect()`.
- The threading contract of `stream()` holds: subscribed on the calling
  thread, background execution decided by the provider.
- The orchestrator never learns vendor types.

Nice to have:

- Per-request decisions (model, options, tools) without rebuilding the
  pipeline.
- Custom `LLMProvider` implementations benefit too, for example through public
  conversion helpers.

## 3. Vendor extension points

### 3.1 LangChain4j 1.22.0: `AiServices`

`AiServices.builder(Class<T>)` builds a proxy of an application-defined
interface. Everything a pipeline needs is a builder method:

| Concern | Builder method |
| --- | --- |
| Model | `chatModel(...)`, `streamingChatModel(...)` |
| System prompt | `systemMessage(String)`, `systemMessageProvider(...)` |
| Memory | `chatMemory(ChatMemory)`, `chatMemoryProvider(...)` |
| Tools | `tools(Object...)`, `tools(Map<ToolSpecification, ToolExecutor>)`, `toolProvider(...)`, `toolProviders(...)` |
| MCP | `McpToolProvider` from `langchain4j-mcp`, registered with `toolProvider(...)` |
| RAG | `contentRetriever(...)`, `retrievalAugmentor(...)`, `storeRetrievedContentInChatMemory(boolean)` |
| Guardrails | `inputGuardrails(...)`, `outputGuardrails(...)`, their config variants |
| Moderation | `moderationModel(...)` |
| Tool loop | `maxToolCallingRoundTrips(int)` (default 100; `maxSequentialToolsInvocations` is deprecated since 1.15.0 and writes the same value), `executeToolsConcurrently()` |
| Tool errors | `toolExecutionErrorHandler(...)`, `toolArgumentsErrorHandler(...)`, `hallucinatedToolNameStrategy(...)` |
| Hooks | `beforeToolExecution(...)`, `afterToolExecution(...)`, `chatRequestTransformer(...)`, `registerListener(...)` |

Facts that matter for a bridge, checked in the 1.22.0 sources:

- A service method cannot take a pre-built `UserMessage`, but it may take a
  parameter annotated `@UserMessage` of type `List<Content>`. With that as the
  only user-message parameter the template is empty and the contents become
  the user message as they are, without passing through `PromptTemplate`: the
  user's text and the session context go in as a `TextContent`, attachments
  as `ImageContent`, `PdfFileContent` and so on, and `{{...}}` in the user's
  text is harmless. A text parameter next to the list would be templated.
- `systemMessage(...)` and `systemMessageProvider(...)` do pass through
  `PromptTemplate`, so a system prompt containing `{{...}}` needs a check
  (spike S1). Checked: it does not survive, see 8.2.
- A parameter of type `InvocationParameters` is picked up by type and reaches
  `ToolProviderRequest.invocationParameters()` and `ToolErrorContext`. Every
  registered `ToolProvider` is asked once per invocation, that is once per
  turn, with the invocation context, so the controller's `ToolSpec` list of a
  turn can travel in the invocation parameters and be turned into tools by a
  Vaadin tool provider. `isDynamic()` only adds a refresh per round trip and
  is not needed for that. `toolProvider(...)` and `toolProviders(...)`
  accumulate, so a customizer registering an MCP provider does not displace
  Vaadin's.
- A method returning `TokenStream` streams. `TokenStream` has
  `onPartialResponse`, `onIntermediateResponse` (one per tool round trip),
  `onCompleteResponse` (token usage summed over the round trips), `onError`,
  `onToolExecuted` and `onRetrieved`. A blocking method returns
  `Result<String>` with `tokenUsage()` and `finishReason()`.
- A service interface may extend `ChatMemoryAccess`; the proxy then implements
  `getChatMemory(memoryId)`, which gives the provider the memory the
  application configured, whatever its type.
- The tool loop writes the `AiMessage` with tool requests and every
  `ToolExecutionResultMessage` into the `ChatMemory`
  (`ToolService.executeInferenceAndToolsLoop`), and reads the next request from
  `chatMemory.messages()`. This is the opposite of the current Vaadin policy and
  the cause of the memory window eviction issue in the project backlog.
- Tool call limits are a round-trip count, not a per-tool count. Exceeding
  it throws a plain `RuntimeException` with a fixed message.
- `AiServices` streaming does not support cancellation: the handler passes a
  `CancellationUnsupportedStreamingHandle` to every partial response, and
  `TokenStream` has no cancel method. A subscriber that goes away stops
  receiving tokens; the loop keeps calling the model and the tools until the
  turn ends on its own or hits the round-trip limit.
- With output guardrails configured, the streaming handler buffers every
  partial response and replays the text only after the guardrails have passed,
  so streaming becomes non-incremental for that service.

### 3.2 Spring AI 2.0.2: `ChatClient`

| Concern | API |
| --- | --- |
| Model, options | `ChatClient.builder(chatModel)`, `defaultOptions(...)` |
| System prompt | `defaultSystem(...)`, overridden by `.system(...)` per request |
| Memory | `MessageChatMemoryAdvisor` with a `ChatMemory` and a conversation id advisor parameter |
| Tools | `defaultTools(Object...)` and per request `.tools(Object...)`, both taking tool objects, `ToolCallback`s and `ToolCallbackProvider`s; the `defaultToolCallbacks(...)` and `toolCallbacks(...)` overloads are deprecated for removal since 2.0.0 |
| MCP | `SyncMcpToolCallbackProvider` from `spring-ai-mcp`, registered with `defaultTools(...)` |
| RAG | `QuestionAnswerAdvisor` (spring-ai-advisors-vector-store) |
| Guardrails, moderation | `SafeGuardAdvisor`, custom `CallAdvisor`/`StreamAdvisor` |
| Tool loop | `ToolCallingAdvisor` with `DefaultToolCallingManager` limits, Spring Boot `spring.ai.tools.limits` |
| Observability | observation registry on the builder |
| Reconfiguring a built client | `chatClient.mutate()` returns a builder preloaded with the client's defaults |

Facts that matter for a bridge:

- `ToolCallingAdvisor.DEFAULT_ORDER` is above the memory advisors' default
  order on purpose: memory advisors sit outside the tool loop and do not see
  the tool round trips. Spring AI therefore already keeps tool traffic out of
  memory, which is the current Vaadin policy. Nothing is lost on this side.
- `mutate()` lets the provider add its own memory advisor and default
  conversation id to a client the application built, so the "application owns
  memory, `setHistory()` does nothing" limitation of today's `ChatClient`
  constructor can go away.
- `DefaultChatClientRequestSpec.getAdvisors()` can tell whether a client already
  carries a `MemoryAdvisor`; the provider uses that for a warning today.
- Advisors run in one chain sorted by `getOrder()`, not by registration order,
  so adding Vaadin's memory advisor after the customizer ran does not prevent
  a second memory advisor registered by the customizer; the duplicate has to
  be detected and either skipped or rejected.
- `SpringAILLMProvider` calls the deprecated `toolCallbacks(...)` for the
  controller tools today; the replacement is `tools(Object...)`, a chore
  independent of this plan.
- Spring AI's own name for a builder callback is `ChatClientBuilderCustomizer`
  (`void customize(ChatClient.Builder)`); `ChatClientCustomizer` is deprecated.
  A Vaadin `customize(...)` method follows the same shape.

## 4. Approach options

Each option is given with the API shape, usage examples for both vendors, how
the bridge responsibilities of section 2 are met, what changes for the Vaadin
policies, and the open questions to research. The examples are deliberately
minimal; variables such as `retriever`, `mcpToolProvider`, `vectorStore` and
`mcpToolCallbackProvider` are created with the vendor's own API as documented
by the vendor.

### Option A: provider-built pipeline with a customizer hook

The provider builds the vendor pipeline object itself, pre-wired with what the
bridge needs (model, memory, the Vaadin tool provider or callbacks), and hands
the vendor builder to an application callback before building it. The
application adds vendor features with the vendor's own builder API and never
sees the bridge.

LangChain4j:

```java
var provider = new LangChain4JLLMProvider(streamingChatModel, service -> service
        .systemMessage("You are a support assistant for ACME.")
        .contentRetriever(retriever)          // RAG
        .toolProvider(mcpToolProvider)        // MCP
        .inputGuardrails(new PromptInjectionGuardrail())
        .tools(new WeatherTools()));          // @Tool objects
```

Spring AI:

```java
var provider = new SpringAILLMProvider(ChatClient.builder(chatModel)
        .defaultSystem("You are a support assistant for ACME.")
        .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
        .defaultAdvisors(new SafeGuardAdvisor(List.of("secret")))
        .defaultTools(mcpToolCallbackProvider, new WeatherTools()) // MCP and @Tool objects
        .build());
```

The plain path stays a one-liner: `new LangChain4JLLMProvider(model)`. In a
Spring Boot application the client is built from the auto-configured
`ChatClient.Builder` bean, so observability and `spring.ai.tools.limits` from
the application configuration are kept:
`new SpringAILLMProvider(chatClientBuilder.build())`.

(The examples above show the final shape. The plan first proposed
`builder(model).systemPrompt(..).customize(..).build()` on both providers;
section 8.7 records why the builders went.)

How the bridge is met, LangChain4j side:

- The provider declares a nested interface, for example
  `LangChain4JLLMProvider.ChatService extends ChatMemoryAccess`, with a
  `TokenStream stream(@UserMessage List<Content> message, InvocationParameters parameters)`
  method and a `Result<String> call(...)` counterpart for a blocking model.
  The customizer receives `AiServices<ChatService>`.
- Controller tools: the provider registers a `ToolProvider`, asked once per
  turn, that reads the turn's `ToolSpec` list from the invocation parameters
  and converts it the way the provider does today (placeholder schema,
  argument parsing, `ToolException` policy inside the `ToolExecutor`).
- Attachments and session context go into the `List<Content>`, converted as
  today.
- Streaming: `TokenStream` events are bridged to the `Flux<String>`; metadata
  comes from `onIntermediateResponse` and `onCompleteResponse`. Blocking mode
  uses the `Result<String>` method and `BackgroundExecution` as today.
- History restore: `setHistory()` writes into
  `service.getChatMemory(defaultMemoryId)`, so it works with the provider's
  default memory and with a memory the application configured in the
  customizer.

Spring AI side: the provider does what it does today, minus the per-request
system prompt and `@Tool` objects (both move to the builder), and keeps a
per-request `tools(...)` call for the controller tool callbacks. The memory
advisor and conversation id are added by the provider unless the customizer
registered a `MemoryAdvisor`; since advisors sort by `getOrder()`, that check
is the only thing that prevents two memory advisors, and an opt-out such as
`manageMemory(false)` keeps today's "application owns memory" behaviour
available.

Vaadin policies:

| Policy | LangChain4j with `AiServices` | Spring AI |
| --- | --- | --- |
| Tool traffic out of memory | lost by default; can be re-established with a Vaadin `ChatMemory` decorator that exposes the turn's tool messages while the turn runs and drops them when the final answer is added (research item S2) | kept by Spring AI's advisor ordering |
| `ToolException` policy for `ToolSpec` tools | kept, the executor is still Vaadin's | kept |
| Tool errors of `@Tool` objects | vendor default, as today; the application can set `toolExecutionErrorHandler(...)` in the customizer, which is better than today | vendor default, as today |
| Tool call limits | the provider setters go away; `maxToolCallingRoundTrips` replaces them with a different unit; mapping the vendor's `RuntimeException` to `ToolCallLimitExceededException` needs a spike (S3) | unchanged, configured on the client |
| Prompt caching order | unchanged, tools and system prompt stay stable, session context stays in the user message | unchanged |
| Cancellation | lost: `AiServices` offers no cancellation, so a turn abandoned by the orchestrator's timeout keeps calling the model and the tools until the round-trip limit; measured in S1 | kept, the client's stream is a `Flux` and cancellation propagates |
| Streaming with output guardrails | the whole answer is buffered until the guardrails pass, so the typing indicator stays for the turn; to document | advisor-dependent |

Parity: full. Both providers have `builder(model)`, `customize(...)` and
`build()`; the type the customizer receives is the vendor's own builder.

Breaking changes: constructors become builders; `LangChain4JLLMProvider`
loses `setMaxCallsPerTool` and `setMaxTotalToolCalls`; see section 5 for the
orchestrator side.

Open questions (answered by the prototype, see 8.2 and 8.3):

- A customizer that replaces what the provider pre-wired (its own
  `chatMemory(...)`, `tools(...)` for the controller tools) must either be
  detected or documented. `AiServices` offers no way to read the builder
  state back, but `ChatMemoryAccess` makes a replaced memory harmless, and the
  controller tools come through a tool provider the application cannot
  remove by accident.
- Whether the `TokenStream` route supports tool calls with every streaming
  model integration the same way the provider's own loop does today
  (partial tool call events, `null` text answers after a tool call).
- How an `InputGuardrail` failure or a `ModerationException` surfaces: it
  arrives as the error of the turn and shows the generic error text in the
  message list. That is acceptable for policy violations, but the
  `RequestInterceptor` remains the place for user-facing validation (5.3).
- Whether a system prompt that contains `{{...}}` (a JSON example, say)
  survives the `PromptTemplate` that `systemMessage(...)` applies.
- Precedence between a RAG augmentor or advisor that writes into the system
  message and the pipeline's own system prompt, and whether that is the
  vendor's documented behaviour or ours to document.
- Whether a duplicate memory advisor from the customizer is skipped or
  rejected on the Spring side, and whether `SpringAILLMProvider(ChatClient)`
  keeps its "application owns memory" meaning or gets memory added unless
  told otherwise.

### Option B: application-built pipeline, completed by the provider

The application builds the vendor pipeline object with the vendor's API and
passes it in. The provider completes it with the bridge pieces it needs. This
is what the `ChatClient` constructor of `SpringAILLMProvider` already is, made
whole with `mutate()`, and its LangChain4j counterpart.

LangChain4j:

```java
var service = AiServices.builder(LangChain4JLLMProvider.ChatService.class)
        .streamingChatModel(streamingChatModel)
        .systemMessage("You are a support assistant for ACME.")
        .chatMemory(MessageWindowChatMemory.withMaxMessages(30))
        .contentRetriever(retriever)
        .toolProviders(mcpToolProvider, LangChain4JLLMProvider.controllerTools())
        .build();
var provider = new LangChain4JLLMProvider(service);
```

`ChatService` is the interface described under option A, published by Vaadin
because the provider has to call it. `controllerTools()` is the tool provider
that hands the orchestrator's controller tools to the model; the application
has to register it, which is the one line that option A hides.

Spring AI:

```java
var chatClient = chatClientBuilder   // Spring Boot's auto-configured builder
        .defaultSystem("You are a support assistant for ACME.")
        .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
        .defaultTools(mcpToolCallbackProvider)
        .build();
var provider = new SpringAILLMProvider(chatClient);
```

The provider calls `chatClient.mutate()` to add its memory advisor and
conversation id when the client has no `MemoryAdvisor`, so `setHistory()`
works unless the application chose to own memory.

How the bridge is met: the same code as option A, only the construction order
differs. History restore on the LangChain4j side goes through
`ChatMemoryAccess` again.

Vaadin policies: identical to option A.

Parity: good. Both take a built vendor object. The LangChain4j side needs the
extra `controllerTools()` registration and a Vaadin-defined interface, the
Spring side needs neither.

Breaking changes: the `ChatModel` and `StreamingChatModel` constructors of
`LangChain4JLLMProvider` go away or become sugar over a provider-built
service, which brings option A back in through the side door. A pure option B
means every LangChain4j user writes the `AiServices` builder, including the
plain case.

Open questions (the prototype took a different shape, see 8.4):

- A forgotten `controllerTools()` registration silently drops every
  controller tool. The provider can detect it: the tools travel in the
  invocation parameters, so if its tool provider was never asked during the
  first turn, fail that turn with an `IllegalStateException` that names the
  missing call. A failed first turn is better than a quietly tool-less
  assistant.
- Whether a built `AiServices` proxy can be told apart from any other
  `ChatService` implementation; an application may also implement the
  interface by hand, which is a legitimate way to plug a custom pipeline in.

### Option C: low-level bridge for custom providers

Instead of (or next to) a pipeline hook, make the bridge itself public: a
vendor-typed request with every conversion already done, and a stream function
the application writes. This is the shape of the earlier draft in the project
backlog ("Low-level vendor-specific streaming API for LLM providers"), recorded
here for comparison rather than as the recommended path.

```java
var provider = new LangChain4JLLMProvider((LangChain4JLLMRequest request) -> {
    // request.messages(): system prompt, history, user message with attachments
    // request.toolSpecifications(), request.toolExecutors(): controller and @Tool tools
    var chatRequest = ChatRequest.builder()
            .messages(request.messages())
            .toolSpecifications(request.toolSpecifications())
            .build();
    return Flux.just(chatModel.chat(chatRequest).aiMessage().text());
});
```

What it gives: full control of the model call per turn, model switching per
request, token-level transforms of the response, and a custom loop. Of these,
per-request model or options is the one use case options A and B do not
cover on the Spring side; on LangChain4j `chatRequestTransformer(...)` in the
customizer does it. Record that as a gap rather than keep the stream function
for it.

What it does not give: convenient access to the vendor's pipeline features.
RAG, MCP and guardrails live in `AiServices`, and an `AiServices` proxy takes
its tools at build time or from a `ToolProvider`, not from the converted lists
in the request. An application that wants those features ends up building the
same service as in option B and writing the tool provider itself. Memory and
the tool loop are the function's responsibility, so `setHistory()` and the
metadata sink have nothing to attach to unless the function reports back.

Where it still earns a place: as the documented way to write a custom
`LLMProvider` without re-implementing the conversions. The conversions could
equally be published as static helpers (`AIAttachment` to `Content` or
`Media`, `ToolSpec` to `ToolSpecification` and `ToolExecutor` or
`ToolCallback`, `ChatMessage` history to vendor messages) and used from any
`LLMProvider` implementation, which is a smaller API surface than two new
request types and two new constructors.

Parity: full by construction. Breaking changes: none, purely additive.

Recommendation within this option: if pursued, publish the helpers rather
than the stream-function constructors, and keep it as a complement to A or B.

### Set aside

- Replicating `AiServices` inside the Vaadin loop (setters for
  `RetrievalAugmentor`, guardrails, `ToolProvider` on
  `LangChain4JLLMProvider`): keeps the Vaadin memory and loop policies, but
  every vendor feature has to be wired by Vaadin, and each LangChain4j release
  adds more of them. Excluded by the decision that the vendor owns the loop.
- Vendor-neutral pipeline stages in `ai-core` (Vaadin interfaces for
  retrieval, guardrails, moderation): the goal is to reach vendor-provided
  functionality, not to abstract it. `RequestInterceptor` already covers the
  vendor-neutral input side where it has to be vendor-neutral (5.3). One
  piece of that idea may survive as a small complement for custom providers:
  an adapter that turns a vendor tool provider's result (`McpToolProvider`,
  `SyncMcpToolCallbackProvider`) into `ToolSpec`s.
- A builder on the `LLMProvider` interface that takes vendor types
  (`LLMProvider.builder(chatModel)`, the backlog's "Add builder to LLMProvider"
  item): the earlier `LLMProvider.from(ChatModel)` factory failed to link
  without LangChain4j on the classpath (issue 9005) and was removed. Builders
  live on the vendor provider classes, as in options A and B.

## 5. Overlapping orchestrator and provider APIs

With a customizable pipeline, several things the orchestrator or provider
configures today are configuration of the vendor pipeline. Each is evaluated
below; conclusions are tentative and belong to spike S5.

### 5.1 System prompt (`AIOrchestrator.builder(provider, systemPrompt)`, `LLMRequest.systemPrompt()`)

The system prompt is pipeline configuration: `AiServices.systemMessage()` and
`ChatClient.Builder.defaultSystem()` exist for it, and nothing in the
orchestrator or the controllers contributes to it (controllers use instruction
tools). Sending it per request from the orchestrator is what overrides a
`defaultSystem()` on the Spring client today, which the docs have to explain
away.

Proposal: remove the parameter from the orchestrator builder and
`systemPrompt()` from `LLMRequest`. The provider builders of options A and B
take it (`systemPrompt(...)` on the Vaadin builder, or the vendor's own
method), and the "no system prompt" warning moves to the provider. Custom
`LLMProvider` implementations carry their own prompt; the docs'
`MockLLMProvider` never read it.

Blast radius: 196 `AIOrchestrator.builder(` call sites in the repository, most
of them tests, 14 mentions across the docs pages and demo sources.

Risk: a system prompt per orchestrator was a way to run two orchestrators with
different prompts on one provider class. That stays possible, each orchestrator
needs its own provider instance anyway (claimed on `build()`).

What is lost: the framework-agnostic first line of every example, and the
fact that the prompt is serialized with the orchestrator today and survives a
session restore without being re-supplied. After the move it has to be
re-applied with the provider, like the streaming and background execution
settings already are.

Condition for the move, to be checked in spike S5: the plain-model setup must
stay one statement per setting on the provider builder, need no vendor builder
code beyond the provider builder itself, re-applying the prompt after session
restore must be no harder than for the streaming setting, and custom
`LLMProvider` implementations must need nothing new. If the spike cannot show
that, the prompt stays on the orchestrator. The case for moving it is weaker
than for `withTools`: the prompt is the one thing every application writes,
and keeping it framework-agnostic has value on its own.

Decision, 2026-10-28, after the prototype: no `systemPrompt(...)` on the
Vaadin builders either. The prompt is set with the vendor's own method in the
customizer, `systemMessage(...)` on LangChain4j and `defaultSystem(...)` on
Spring AI. Reasons: one place to set it, so there is no precedence question
between a Vaadin prompt and the vendor's (the prototype had the customizer win
on Spring AI and the Vaadin prompt win on LangChain4j); and once the deprecated
orchestrator prompt is removed, the providers carry no prompt logic at all. The
convenience lost is one lambda in the plain case. `builder(provider,
systemPrompt)` on the orchestrator stays deprecated and keeps working: the
prompt it sends replaces the vendor's on that turn, sent as it is.

### 5.2 `withTools(Object...)`, `Reconnector.withTools`, `LLMRequest.tools()`

`@Tool` objects are vendor-specific already (the annotation comes from the
vendor) and the vendor builders register them (`AiServices.tools(...)`,
`ChatClient.Builder.defaultTools(...)`). Passing them through the orchestrator
adds a second place to configure tools and a per-request registration that
is converted on every turn.

Proposal: remove them all. `LLMRequest` keeps `explicitTools()` for the
controller's `ToolSpec` tools, which are the framework-agnostic ones and change
per turn. Tool name validation on registration stays limited to controller
tools, as today for MCP tools.

Blast radius: 13 call sites in the repository, the tool-calling docs page.

Condition: a framework-agnostic way to add a tool without writing a controller
must remain, for example `withTools(ToolSpec...)` taking over the name on the
orchestrator. Otherwise a small application loses today's one-line path and
has to implement `AIController` for a single tool.

Decision, 2026-10-28: `@Tool` objects are registered with `tools(...)` on the
LangChain4j service and `defaultTools(...)` on the Spring AI client through the
customizer; `withTools` stays deprecated and keeps working. The chat page shows
one method carrying both vendors' tool annotations, registered on both
pipelines, and the provider tests cover a tool object from the customizer.
The `withTools(ToolSpec...)` condition above is still open.

### 5.3 `RequestInterceptor`

Overlaps with LangChain4j `InputGuardrail` and Spring AI `SafeGuardAdvisor`
for content validation. It does things a vendor guardrail cannot do, because
it runs before the orchestrator acts on the prompt: reject before the message
is shown or recorded, show a rejection reply next to the original message,
replace attachments before they enter the history, postpone with UI feedback.
A vendor guardrail runs after the user message is displayed and recorded, and
its failure arrives as the error of the turn.

Proposal: keep it unchanged, and document the split: model-facing policy
(prompt injection, PII masking for the model, output checks) belongs to the
vendor pipeline; user-facing validation with UI consequences belongs to the
interceptor.

### 5.4 History restore: `withHistory`, `reconnect`, `LLMProvider.setHistory`

With vendor-owned memory the provider needs a handle on that memory to
restore a conversation. Option A and B give it one: `ChatMemoryAccess` on the
LangChain4j service, the provider-added memory advisor on the Spring client.
Only an application that registers its own Spring memory advisor is out of
reach, which is today's documented limitation of the `ChatClient`
constructor.

Proposal: keep `withHistory` and `reconnect` as they are (they also restore
the message list and the orchestrator's own history). Keep `setHistory()` on
`LLMProvider`, but change the default from throwing
`UnsupportedOperationException` to doing nothing with a debug log, since a
provider whose memory the application owns is now a normal case rather than
an error. Document per provider when it reaches the vendor memory.

Alternative to evaluate: drop `setHistory()` and make loading the vendor
memory the application's job in every case. Simpler contract, but it breaks
the plain path (`builder(model).build()` plus `withHistory`), which should
keep working without vendor code.

### 5.5 Session context (`withMetadata`)

Stays. It is per turn, vendor-neutral, and placed at the end of the user
message for prompt caching. The vendor alternatives (`systemMessageProvider`,
template variables) would put it in the system prompt and invalidate the
cached prefix on every turn.

### 5.6 `setMaxCallsPerTool`, `setMaxTotalToolCalls` on `LangChain4JLLMProvider`

Belong to the loop, which the vendor owns. Proposal: remove them; the limit is
set in the customizer (`maxToolCallingRoundTrips`), as it is on the Spring
`ChatClient` today. Whether the turn still fails with
`ToolCallLimitExceededException` is spike S3; the docs promise that today for
both providers. Prototype outcome: the setters stay, see 8.3.

### 5.7 `setStreaming`, `setBackgroundExecution`

Stay on the provider. Streaming is a property of which service method the
provider calls, background execution is orchestration threading; neither is
pipeline configuration.

## 6. Comparison

| | A: customizer hook | B: application-built object | C: low-level bridge |
| --- | --- | --- | --- |
| Plain path stays a one-liner | yes | Spring yes, LangChain4j no | yes (existing constructors) |
| Vendor features through the vendor API | yes | yes | no, only the model call |
| Hidden bridge wiring | all of it | all but `controllerTools()` on LangChain4j | none, application writes it |
| Spring Boot `ChatClient.Builder` bean | via `builder(chatClientBuilder)` | natural | n/a |
| Parity | full | good | full |
| Removes system prompt and `withTools` from the orchestrator | yes | yes | no |
| New public types | 2 builders, `ChatService` | `ChatService`, `controllerTools()` | 2 request types or helper classes |
| Fits a hand-written `ChatService` or custom loop | no | yes (interface) | yes |
| Cancellation stops the loop | Spring yes; LangChain4j refuses the next model call and tool, the in-flight call completes (8.3) | same as A | yes, the application's loop |

Recommendation to carry into the spikes: option A as the primary API, with
the Spring side also accepting a `ChatClient.Builder` and a built
`ChatClient` (option B is one more entry point there, not a separate design),
and option C reduced to public conversion helpers for custom providers. A pure
option B on the LangChain4j side costs every user the `AiServices` boilerplate
in the plain case, and a pure option C does not reach the vendor features.

The decisive unknowns are on the LangChain4j side: whether the `AiServices`
proxy can carry the bridge without regressions (S1), and what to do about tool
traffic in memory (S2).

## 7. Research plan

Ordered so that each spike settles something the next one depends on. Each
produces a throwaway branch and a short note; nothing ships from a spike.

Status after the prototypes of 2026-10-28 (details in section 8): S1, S2, S3,
S4 and S6 are answered on `prototype/pipeline-customizer`. S5 is half done,
the orchestrator APIs are deprecated rather than removed, by decision, and
the condition check of 5.1 and 5.2 is still to run before a removal. S7 is
open.

S1. LangChain4j bridge on `AiServices` (option A core). About 2 days.
Build `ChatService` with the `@UserMessage List<Content>` and
`InvocationParameters` parameters, the controller tool provider (static, asked
once per turn), the `TokenStream` to `Flux` bridge with metadata, and the
`Result<String>` blocking path. Drive it with the existing
`LangChain4JLLMProviderTest` scenarios (they mock `ChatModel.chat`, which
`AiServices` still calls): attachments, session context placement, placeholder
schema, `ToolException` relay, cancellation, metadata accumulation. Record
which scenarios change meaning and why. Must prove: streaming with tool calls
works through `TokenStream`; a `null` text answer after a tool call does not
break the next turn; a system prompt containing `{{...}}` survives
`systemMessage(...)`; what a turn abandoned by the orchestrator's timeout costs
when the loop cannot be cancelled (model calls and tool executions after the
cancel), and whether a `beforeToolExecution` hook that checks a cancelled flag
and throws can end it; how output guardrails change the streaming behaviour.

S2. Memory policy with vendor-owned memory. About 1 day, after S1.
Prototype a two-list `ChatMemory`: the durable conversation (user messages
and final answers, windowed as today) and the current turn (everything added
since the turn started). `messages()` returns both; when the provider sees the
`Flux` complete it calls `endTurn()`, which drops the turn list and appends
the final answer, skipping an answer without text. A memory that drops tool
messages on `add()` instead breaks the loop, which reads the memory back
before every round trip. Run the eviction reproduction from the backlog issue
("Long tool-calling turns evict the conversation") against it, under the
streaming handler as well. Decide between shipping it as the provider's
default memory, or accepting LangChain4j's behaviour and documenting the window
consequence. Check the interplay with `MessageWindowChatMemory` eviction and
`storeRetrievedContentInChatMemory`.

S3. Tool call limits and `ToolCallLimitExceededException`. Half a day, after S1.
Options: map the vendor's round-trip `RuntimeException` by message (fragile),
count calls in `beforeToolExecution` or in Vaadin's `ToolExecutor` wrapper and
throw the Vaadin exception from there, or drop the exception promise for
LangChain4j and document the vendor's error. The second option only works if
a throw from there ends the turn instead of being returned to the model as an
error string; verify that first, together with the limit boundary (whether a
final answer arriving as round trip N + 1 is discarded with the exception).
Pick one and note the docs change.

S4. Spring AI builder and `mutate()`. About 1 day, independent of S1.
`SpringAILLMProvider.builder(chatModel | chatClientBuilder)` with
`systemPrompt` and `customize`, plus `mutate()`-based memory injection for a
built `ChatClient` without a `MemoryAdvisor`, the duplicate memory advisor
check (advisors sort by `getOrder()`, so the check is the only guard) with a
skip-or-reject decision, and an opt-out that keeps "application owns memory".
Verify `defaultSystem()` is honoured once the provider stops sending
`.system()` per request, and that `spring.ai.tools.limits` from Boot survive
the builder path. Replace the deprecated `toolCallbacks(...)` call with
`tools(...)` on the way.

S5. Orchestrator API removal. About 1 day, after S1 and S4.
Write the plain-model setup and the MCP plus RAG example twice, with and
without the orchestrator-level system prompt and `withTools`, and check the
condition of 5.1 and 5.2 against them (one statement per setting, no vendor
code in the plain case, restore no harder than today, custom providers
untouched, a `ToolSpec` one-liner left for tools). Only then remove the system
prompt parameter, `withTools`, `Reconnector.withTools`,
`LLMRequest.systemPrompt()` and `LLMRequest.tools()`, and move the "no system
prompt" warning to the providers. Measure the test churn (196 builder call
sites) and list the docs changes: `llm-providers.adoc` ("Framework Features",
the "MCP With LangChain4j" note, the memory window note, the system prompt
tip), `tool-calling.adoc`, `controllers.adoc`, `conversation-history.adoc`
and the index page.

S6. History restore semantics. Half a day, after S2 and S4.
Define `setHistory()` for the new memory ownership: `ChatMemoryAccess` on the
LangChain4j side, provider-added advisor on the Spring side, no-op default.
Decide on the alternative of dropping it.

S7. Regression gate. After S4 and S5.
Run the AI benchmark suite base versus changed with the Spring AI provider;
pass rates must not drop. Add an IT view that builds a provider through the
customizer with a fake RAG retriever or a fake tool provider, so the wiring to
the browser is proven once per vendor.

Out of scope for the spikes: a Vaadin-shipped MCP or RAG convenience, and any
new vendor-neutral abstraction in `ai-core`.

## 8. Prototype results, 2026-10-28

Each option was prototyped on its own branch off
`research/llm-pipeline-vendor-customization`, with the OpenAI chat page
`ChatPage` of the AI integration test module (route
`vaadin-ai/chat/{spring-ai|langchain4j}`) changed to carry the same demo
pipeline: a fixed-fact content retriever (the codename "Bluefin") and an input
guardrail that blocks the word "password". Every branch was checked in a
browser on both routes against gpt-5.4-mini: the retrieved fact reaches the
answer, and the blocked word ends the turn (Spring AI's `SafeGuardAdvisor`
replies with its failure text, LangChain4j's `InputGuardrail` fails the turn
and the message list shows the generic error).

| Branch | Option | Shape |
| --- | --- | --- |
| `prototype/pipeline-customizer` | A | `new LangChain4JLLMProvider(model, Consumer<AiServices<ChatService>>)`, `new SpringAILLMProvider(chatClient)` with memory added through `mutate()`, `AIOrchestrator.builder(provider)`; the branch first had `builder(model).systemPrompt(..).customize(..).build()` on both providers, see 8.7 |
| `prototype/pipeline-byo` | B | `new LangChain4JLLMProvider(AiServices<ChatService>, streaming)`, `new SpringAILLMProvider(chatClient)` adding memory through `mutate()` |
| `prototype/pipeline-bridge-helpers` | C | public `LangChain4JBridge` and `SpringAIBridge` conversion helpers, the built-in providers delegate to them, the page runs two custom providers written on them |

Decision taken on the results: continue with A. B's Spring entry point is
folded into A (the `ChatClient` constructor now adds memory the same way the
builder does), C's helpers stay available as a follow-up for custom
providers.

### 8.1 The LangChain4j bridge on `AiServices` (S1)

Branches A and B share the rebuild of `LangChain4JLLMProvider` on
`AiServices`:

- `ChatService extends ChatMemoryAccess` with
  `TokenStream stream(@UserMessage List<Content>, InvocationParameters)` and
  `Result<String> call(...)`. The turn travels in the invocation parameters.
- `TurnToolProvider`, a static `ToolProvider` asked once per turn, converts
  the controller `ToolSpec`s and the deprecated `@Tool` objects of the turn.
  Every executor is wrapped in the turn's guard, which enforces the Vaadin
  tool call limits and the cancellation check before each tool. LangChain4j's
  loop invokes the wrapped executors, so the `ToolException` policy is kept.
- `ResponseObserver`, an `AiServiceListener<AiServiceResponseReceivedEvent>`,
  collects finish reason and token usage per round trip and screens the
  limits before the tools of a round run.
- `prepare()` runs before the customizer and lifts `maxToolCallingRoundTrips`
  to `Integer.MAX_VALUE` and makes a made-up tool name an error result for
  the model; `wire()` runs after the customizer and composes with what it
  configured: memory only when none is set, the tool provider next to the
  customizer's, the request transformer and the tool execution error handler
  wrapped around the configured ones.
- Streaming with tool calls works through `TokenStream`. A response without
  an AI message is not stored (the service hands the memory a `null`, which
  the window memory would store and fail the next turn on) and fails the
  turn; the related tests were adapted to that.
- All 132 `LangChain4JLLMProviderTest` scenarios pass on the rebuild, with
  the adaptations noted: `ChatResponse` mocks need `metadata()` stubbed, and
  a request carries the window plus the user message, so the durable window
  is one message short of `MAX_MESSAGES`.

### 8.2 Answers to the option A questions

- System prompt with braces: through `systemMessage(...)` a prompt containing
  `{{"answer": ...}}` fails the turn with
  `IllegalArgumentException: Value for the variable ... is missing`, and
  `{{it}}` with `IllegalConfigurationException`. Fixed on A for the prompt a
  request carries, the deprecated orchestrator prompt: the request transformer
  places it on the `ChatRequest` in place of the service's own system
  message, as it is. A `systemMessage(...)` set in the customizer is a
  LangChain4j template, which the `customize` Javadoc documents together with
  the special date variables; that is vendor behaviour and stays.
- Retrieval with attachments: `DefaultContentInjector` calls `singleText()`
  on the user message, so `contentRetriever(...)` fails a turn that carries an
  image or a file. Fixed on A: `wire()` wraps `context.retrievalAugmentor`
  in `AttachmentKeepingAugmentor`, which hands the augmentor the text content
  alone and puts the other contents back on the augmented message.
- Guardrail and moderation failures arrive as the error of the turn, as
  predicted. `RequestInterceptor` stays the place for user-facing validation.
- Output guardrails buffer the stream until they pass; vendor behaviour,
  documented, unchanged.
- The duplicate memory question on the Spring side: a `MemoryAdvisor` on the
  client is detected through `DefaultChatClientRequestSpec.getAdvisors()`,
  and the provider then skips its own advisor and `setHistory()` does
  nothing. A client that cannot be inspected is assumed to own its memory.
  The `SpringAILLMProvider(ChatClient)` constructor now adds memory the same
  way, so its "application owns memory" meaning is gone unless the client
  carries a memory advisor, and its "no chat memory advisor" warning with it.
- Replaced pre-wiring: a customizer's `chatMemory(...)` replaces the
  provider's memory and `setHistory()` still reaches it through
  `ChatMemoryAccess`; the controller tools come through the provider's own
  tool provider and cannot be dropped by a customizer.

### 8.3 Memory, limits, cancellation and history (S2, S3, S6)

- Memory: `TurnScopedChatMemory` is the provider's default memory. It keeps a
  durable `MessageWindowChatMemory` and a turn list: tool requests and
  results go to the turn list, the final answer ends the turn and is added to
  the durable memory unless its text is empty, `messages()` returns both,
  and a failed turn drops its list. This re-establishes the Vaadin memory
  policy on top of LangChain4j's loop. A memory configured in the customizer
  replaces it and gets LangChain4j's tool-traffic-in-memory behaviour.
- Limits: the provider setters `setMaxCallsPerTool` and
  `setMaxTotalToolCalls` stay, so 5.6's proposal is withdrawn. The turn's
  guard throws `ToolCallLimitExceededException` from the executor wrapper,
  and the provider's tool execution error handler rethrows it instead of
  sending it to the model, which ends the turn in both modes: the streaming
  handler routes a handler exception to `onError`. The response observer
  screens a round that would breach a limit before its tools run.
- Cancellation: `AiServices` has none. The provider checks the cancelled flag
  in the request transformer before each model call and in the guard before
  each tool, so an abandoned turn costs at most the in-flight model response.
  Tokens of that response are not forwarded. The `beforeToolExecution` hook
  was not needed, the executor wrapper does it.
- History: `setHistory()` writes through
  `service.getChatMemory(ChatMemoryService.DEFAULT)` on LangChain4j and into
  the provider's memory on Spring AI, and does nothing when the application
  owns the memory. The throwing default on `LLMProvider` is unchanged.

### 8.4 Spring AI (S4) and the controller tools

- `SpringAILLMProvider.builder(chatModel)` and `builder(ChatClient.Builder)`
  with `customize(...)`, where the system prompt is `defaultSystem(...)`; the
  provider completes the client with `mutate()` and its memory advisor unless
  a `MemoryAdvisor` is present. `defaultSystem()` is honoured because the
  provider only sends `.system(...)` per request when the request carries a
  prompt, which is the deprecated orchestrator path.
- Default tools and per-request tools combine: `prompt()` copies the client's
  default callbacks and providers into the request spec, `tools(...)` appends,
  and `DefaultChatClientUtils` merges callbacks and providers into the chat
  options. The "runtime tools completely override the defaults" note in the
  `defaultTools` Javadoc refers to the chat options of the model, not to the
  client defaults. MCP tools on the client and controller tools per request
  therefore coexist.
- The deprecated `toolCallbacks(...)` call is replaced with `tools(List)`.
- `DefaultChatClientUtils` attaches the tools of a request to the model's
  options only when `ChatModel.getOptions()` returns a
  `ToolCallingChatOptions`; a `ChatModel` that keeps the plain default never
  sees a tool, and the tool call its response carries is not executed.
- Controller tools: on A and B the controller `ToolSpec`s reach the model
  through `TurnToolProvider` on LangChain4j and through per-request
  `tools(...)` callbacks on Spring AI, where `ToolCallingAdvisor` invokes
  them. On C the built-in providers behave as before through the helpers; the
  page's custom Spring provider passes `SpringAIBridge.toToolCallbacks(...)`
  per request, the page's custom LangChain4j provider does not wire tools,
  because an `AiServices` proxy takes its tools at build time or from a
  `ToolProvider`, as 4.C predicted.

### 8.5 Option B and C findings

- B on LangChain4j: the application's `AiServices` builder is opaque, so the
  provider overwrites the tool execution error handler slot (a handler the
  application set is lost; A composes with it), a chat memory is mandatory
  (`ChatMemoryAccess` validation throws `IllegalConfigurationException`
  without one), the streaming mode has to be passed as a flag, and a system
  prompt on a request cannot be applied (warned once). B is weaker than A for
  the same use cases, which is why only its Spring entry point is kept.
- C: the helpers are small and tested (`LangChain4JBridgeTest`,
  `SpringAIBridgeTest`), but a custom provider still re-implements memory,
  the loop, limits, cancellation and metadata, as 4.C said.

### 8.6 Orchestrator APIs (S5, partial)

`AIOrchestrator.builder(provider)` is the new entry point.
`builder(provider, systemPrompt)`, `Builder.withTools` and
`Reconnector.withTools` are deprecated since 25.4 and kept working, by
decision, so existing call sites need no change; the "no system prompt"
warning only fires on the deprecated path. The condition check of 5.1 and 5.2
(one statement per setting, no vendor code in the plain case, restore no
harder than today, a `ToolSpec` one-liner) is still to run before any removal.

### 8.7 Decisions taken on the results, same day

- `systemPrompt(...)` dropped from both builders, see 5.1.
- `@Tool` objects through the vendor API in the customizer, see 5.2; the
  deprecated orchestrator prompt and `withTools` point at those in their
  Javadoc.
- The chat page sets the prompt through the customizer on both routes and
  registers one dual-annotated tool method on both pipelines; the pipeline
  integration test pages set the prompt the same way.
- The builders went too. With the prompt gone, each held one method, and a
  builder for one argument is a smell. LangChain4j still needs a hook, since
  the provider must own the `AiServices` build (its defaults before the
  application's configuration, its wiring after); the hook is now a
  constructor overload, `LangChain4JLLMProvider(model, customizer)`, next to
  the existing constructors. Spring AI needs no hook: `ChatClient.Builder` is
  the vendor builder already, and the `ChatClient` constructor with memory
  through `mutate()` covers everything the Vaadin builder did, including the
  Boot builder bean through its `build()`. The asymmetry is deliberate: on
  Spring the application calls `build()`, on LangChain4j the provider must.

### 8.8 Remaining work on `prototype/pipeline-customizer`

1. S7, second half: the AI benchmark base versus branch with the Spring AI
   provider. The first half is done: `LangChain4JPipelinePage` and
   `SpringAIPipelinePage` (routes `vaadin-ai/pipeline/langchain4j` and
   `vaadin-ai/pipeline/spring-ai`) run a scripted model behind a provider
   built with a retriever or advisor, a tool provider standing in for MCP, a
   guardrail and a controller tool; `LangChain4JPipelineIT` and
   `SpringAIPipelineIT` check the retrieved fact, both tools and the
   guardrail in the browser, 4 tests each.
2. Docs pages listed under S5, once the API is final.
3. Split the branch into reviewable pull requests: the `AiServices` rebuild
   with `TurnScopedChatMemory`, the two builders, the orchestrator
   deprecations, docs. Decide whether the OpenAI chat page and the vendor
   dependencies it brought into the integration test module stay.

## Appendix: verified facts and where they come from

LangChain4j 1.22.0, from the sources jar in the local Maven repository:

- `DefaultAiServices.java:111`: valid parameter annotations are
  `@UserMessage`, `@V`, `@MemoryId`, `@UserName`.
- `DefaultAiServices.java:217`: an `InvocationParameters` argument is found by
  type and used for the invocation.
- `DefaultAiServices.java:1562`: `prepareUserMessage` builds the user message
  from `Content` and `List<Content>` arguments when no template is set.
- `DefaultAiServices.java:139`: methods declared on `ChatMemoryAccess` are
  implemented by the proxy.
- `DefaultAiServices.java:354`: a `TokenStream` return type selects streaming.
- `ToolService.java:122`: `maxToolCallingRoundTrips` defaults to 100.
- `ToolService.java:139` and `:148`: `toolProvider(...)` and
  `toolProviders(...)` add to a list.
- `ToolService.java:549` to `:590`: `createContext` asks every static tool
  provider once per invocation with the invocation context; dynamic providers
  are refreshed per round trip.
- `ToolService.java:410`: `maxSequentialToolsInvocations` is deprecated since
  1.15.0 and writes `maxToolCallingRoundTrips`.
- `ToolService.java:661` and `:698`: the loop adds the `AiMessage` and the
  tool results to the `ChatMemory`; `:1183` reads the next request from the
  memory messages.
- `AiServiceStreamingResponseHandler.java:365`: token usage is summed over the
  round trips before `onCompleteResponse`.
- `AiServiceStreamingResponseHandler.java:170`: partial responses carry a
  `CancellationUnsupportedStreamingHandle`; `:95` and `:164`: with output
  guardrails the partial responses go into `responseBuffer`.
- `DefaultAiServices.java:1521`: the system message template goes through
  `PromptTemplate`; `:1588`: the user message does so only when a template
  exists; `:1673`: a single unannotated `List<Content>` argument is not a
  template either.
- `AiServiceContext.java:49`: `storeRetrievedContentInChatMemory` defaults to
  `true`.
- `AiServices` builder methods: listed in 3.1, read with `javap` from
  `langchain4j-1.22.0.jar`.

Spring AI 2.0.2, from `spring-ai-client-chat-2.0.2`:

- `ToolCallingAdvisor.java`, `DEFAULT_ORDER` comment: memory advisors at their
  default order are placed outside the tool loop.
- `ChatClient.mutate()` returns a `Builder`; `ChatClient.Builder` has
  `defaultAdvisors`, `defaultSystem`, `defaultTools`, `defaultOptions`.
- `ChatClient.java:567` to `:585` and `:450` to `:468`: the
  `defaultToolCallbacks(...)` and `toolCallbacks(...)` overloads are
  deprecated since 2.0.0 for removal, in favour of `defaultTools(Object...)`
  and `tools(Object...)`.
- `ChatClientCustomizer.java:31`: deprecated for removal;
  `ChatClientBuilderCustomizer` is the replacement.
- `ChatClientRequestSpec` has per-request `system`, `user`, `tools`,
  `advisors`.
- `SyncMcpToolCallbackProvider` and `AsyncMcpToolCallbackProvider` live in
  `spring-ai-mcp`.

Current code in this repository:

- `SpringAILLMProvider.getPromptSpec`: sends `.system(...)` per request,
  `.tools(request.tools())` and `.toolCallbacks(...)` for controller tools,
  and the conversation id parameter only with managed memory.
- `LangChain4JLLMProvider.stream` and `handleResponse`: own loop, own
  `MessageWindowChatMemory` of 30 messages, tool traffic kept in a per-turn
  list, limits 40 per tool and 150 per turn.
- `AIOrchestrator.buildRequest`: the request carries the orchestrator's system
  prompt, `@Tool` objects, controller tools, session context and the metadata
  sink.
- Call sites: 196 `AIOrchestrator.builder(` and 13 `.withTools(` in the
  repository, 14 mentions of the builder, `withTools` or the interceptor in the
  docs' AI pages and demo sources.
