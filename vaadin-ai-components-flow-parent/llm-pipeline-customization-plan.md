# LLM pipeline customization with vendor features: investigation plan

Status: investigation plan, 2026-10-28. Nothing here is implemented.
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
| MCP tools | `ChatClient` constructor, `defaultToolCallbacks(SyncMcpToolCallbackProvider)` | not possible (documented as "needs a custom provider") |
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

The last two rows are the Vaadin policies that the decision "vendor owns memory
and the loop" puts in question. The rest survives any of the approaches below,
because the conversion code stays in the provider.

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
| Tool loop | `maxToolCallingRoundTrips(int)` (default 100), `maxSequentialToolsInvocations(int)`, `executeToolsConcurrently()` |
| Tool errors | `toolExecutionErrorHandler(...)`, `toolArgumentsErrorHandler(...)`, `hallucinatedToolNameStrategy(...)` |
| Hooks | `beforeToolExecution(...)`, `afterToolExecution(...)`, `chatRequestTransformer(...)`, `registerListener(...)` |

Facts that matter for a bridge, checked in the 1.22.0 sources:

- A service method may take a parameter annotated `@UserMessage` of type
  `List<Content>`. The contents become the user message as they are, so
  attachments can be passed as `ImageContent`, `PdfFileContent` and so on, and
  the session context as part of the `TextContent`.
- A parameter of type `InvocationParameters` is picked up by type and reaches
  `ToolProviderRequest.invocationParameters()` and `ToolErrorContext`. A
  `ToolProvider` whose `isDynamic()` returns `true` is re-evaluated on every
  request, so the controller's `ToolSpec` list of a turn can travel in the
  invocation parameters and be turned into tools by a Vaadin tool provider.
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

### 3.2 Spring AI 2.0.2: `ChatClient`

| Concern | API |
| --- | --- |
| Model, options | `ChatClient.builder(chatModel)`, `defaultOptions(...)` |
| System prompt | `defaultSystem(...)`, overridden by `.system(...)` per request |
| Memory | `MessageChatMemoryAdvisor` with a `ChatMemory` and a conversation id advisor parameter |
| Tools | `defaultTools(Object...)`, `defaultToolCallbacks(ToolCallback... / ToolCallbackProvider...)`, per request `.tools(...)`, `.toolCallbacks(...)` |
| MCP | `SyncMcpToolCallbackProvider` from `spring-ai-mcp`, registered with `defaultToolCallbacks(...)` |
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
var provider = LangChain4JLLMProvider.builder(streamingChatModel)
        .systemPrompt("You are a support assistant for ACME.")
        .customize(service -> service
                .contentRetriever(retriever)          // RAG
                .toolProvider(mcpToolProvider)        // MCP
                .inputGuardrails(new PromptInjectionGuardrail())
                .tools(new WeatherTools()))           // @Tool objects
        .build();
```

Spring AI:

```java
var provider = SpringAILLMProvider.builder(chatModel)
        .systemPrompt("You are a support assistant for ACME.")
        .customize(client -> client
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .defaultAdvisors(new SafeGuardAdvisor(List.of("secret")))
                .defaultToolCallbacks(mcpToolCallbackProvider)
                .defaultTools(new WeatherTools()))
        .build();
```

The plain path stays a one-liner: `LangChain4JLLMProvider.builder(model).build()`.
In a Spring Boot application the entry point should also accept the
auto-configured `ChatClient.Builder` bean, so observability and
`spring.ai.tools.limits` from the application configuration are kept:
`SpringAILLMProvider.builder(chatClientBuilder)`.

How the bridge is met, LangChain4j side:

- The provider declares a nested interface, for example
  `LangChain4JLLMProvider.ChatService extends ChatMemoryAccess`, with a
  `TokenStream stream(@UserMessage List<Content> message, InvocationParameters parameters)`
  method and a `Result<String> call(...)` counterpart for a blocking model.
  The customizer receives `AiServices<ChatService>`.
- Controller tools: the provider registers a dynamic `ToolProvider` that reads
  the turn's `ToolSpec` list from the invocation parameters and converts it the
  way the provider does today (placeholder schema, argument parsing,
  `ToolException` policy inside the `ToolExecutor`).
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
system prompt and `@Tool` objects (both move to the builder), and keeps
per-request `toolCallbacks(...)` for the controller tools. The memory advisor
and conversation id are added by the provider unless the customizer registered
a `MemoryAdvisor`.

Vaadin policies:

| Policy | LangChain4j with `AiServices` | Spring AI |
| --- | --- | --- |
| Tool traffic out of memory | lost by default; can be re-established with a Vaadin `ChatMemory` decorator that exposes the turn's tool messages while the turn runs and drops them when the final answer is added (research item S2) | kept by Spring AI's advisor ordering |
| `ToolException` policy for `ToolSpec` tools | kept, the executor is still Vaadin's | kept |
| Tool errors of `@Tool` objects | vendor default, as today; the application can set `toolExecutionErrorHandler(...)` in the customizer, which is better than today | vendor default, as today |
| Tool call limits | the provider setters go away; `maxToolCallingRoundTrips` replaces them with a different unit; mapping the vendor's `RuntimeException` to `ToolCallLimitExceededException` needs a spike (S3) | unchanged, configured on the client |
| Prompt caching order | unchanged, tools and system prompt stay stable, session context stays in the user message | unchanged |

Parity: full. Both providers have `builder(model)`, `systemPrompt(...)`,
`customize(...)`, `build()`; the type the customizer receives is the vendor's
own builder.

Breaking changes: constructors become builders; `LangChain4JLLMProvider`
loses `setMaxCallsPerTool` and `setMaxTotalToolCalls`; see section 5 for the
orchestrator side.

Open questions:

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
because the provider has to call it. `controllerTools()` is the dynamic tool
provider that hands the orchestrator's controller tools to the model; the
application has to register it, which is the one line that option A hides.

Spring AI:

```java
var chatClient = chatClientBuilder   // Spring Boot's auto-configured builder
        .defaultSystem("You are a support assistant for ACME.")
        .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
        .defaultToolCallbacks(mcpToolCallbackProvider)
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

Open questions:

- Whether a forgotten `controllerTools()` registration can be detected. The
  provider could pass the tools in the invocation parameters and check, after
  the first turn, whether its tool provider was ever asked; a warning on the
  first turn is the likely answer.
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
request, token-level transforms of the response, and a custom loop.

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
  vendor-neutral input side where it has to be vendor-neutral (5.3).

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
both providers.

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

S1. LangChain4j bridge on `AiServices` (option A core). About 2 days.
Build `ChatService` with the `@UserMessage List<Content>` and
`InvocationParameters` parameters, the dynamic controller tool provider, the
`TokenStream` to `Flux` bridge with metadata, and the `Result<String>` blocking
path. Drive it with the existing `LangChain4JLLMProviderTest` scenarios
(they mock `ChatModel.chat`, which `AiServices` still calls): attachments,
session context placement, placeholder schema, `ToolException` relay,
cancellation, metadata accumulation. Record which scenarios change meaning and
why. Must prove: streaming with tool calls works through `TokenStream`, a
`null` text answer after a tool call does not break the next turn.

S2. Memory policy with vendor-owned memory. About 1 day, after S1.
Prototype a `ChatMemory` decorator that keeps the turn's tool messages visible
during the turn and drops them when the final answer is added, and run the
eviction reproduction from the backlog issue ("Long tool-calling turns evict
the conversation") against it. Decide between shipping the decorator as the
provider's default memory, or accepting LangChain4j's behaviour and
documenting the window consequence. Check the interplay with
`MessageWindowChatMemory` eviction and `storeRetrievedContentInChatMemory`.

S3. Tool call limits and `ToolCallLimitExceededException`. Half a day, after S1.
Options: map the vendor's round-trip `RuntimeException` by message (fragile),
count calls in `beforeToolExecution` and throw the Vaadin exception from
there, or drop the exception promise for LangChain4j and document the vendor's
error. Pick one and note the docs change.

S4. Spring AI builder and `mutate()`. About 1 day, independent of S1.
`SpringAILLMProvider.builder(chatModel | chatClientBuilder)` with
`systemPrompt` and `customize`, plus `mutate()`-based memory injection for a
built `ChatClient` without a `MemoryAdvisor`. Verify `defaultSystem()` is
honoured once the provider stops sending `.system()` per request, and that
`spring.ai.tools.limits` from Boot survive the builder path.

S5. Orchestrator API removal. About 1 day, after S1 and S4.
Remove the system prompt parameter, `withTools`, `Reconnector.withTools`,
`LLMRequest.systemPrompt()` and `LLMRequest.tools()`; move the "no system
prompt" warning to the providers. Measure the test churn (196 builder call
sites) and list the docs pages to rewrite (index, llm-providers, tool-calling,
controllers, conversation-history).

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
- `ToolService.java:579`: dynamic tool providers are refreshed per request.
- `ToolService.java:661` and `:698`: the loop adds the `AiMessage` and the
  tool results to the `ChatMemory`; `:1183` reads the next request from the
  memory messages.
- `AiServiceStreamingResponseHandler.java:365`: token usage is summed over the
  round trips before `onCompleteResponse`.
- `AiServiceContext.java:49`: `storeRetrievedContentInChatMemory` defaults to
  `true`.
- `AiServices` builder methods: listed in 3.1, read with `javap` from
  `langchain4j-1.22.0.jar`.

Spring AI 2.0.2, from `spring-ai-client-chat-2.0.2`:

- `ToolCallingAdvisor.java`, `DEFAULT_ORDER` comment: memory advisors at their
  default order are placed outside the tool loop.
- `ChatClient.mutate()` returns a `Builder`; `ChatClient.Builder` has
  `defaultAdvisors`, `defaultSystem`, `defaultTools`, `defaultToolCallbacks`
  (with `ToolCallbackProvider`), `defaultOptions`.
- `ChatClientRequestSpec` has per-request `system`, `user`, `tools`,
  `toolCallbacks`, `advisors`.
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
