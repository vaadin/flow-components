# Vaadin AI Components - Agent Guide

This document provides an overview of the Vaadin AI Components architecture for AI coding agents.

## Module Structure

The `vaadin-ai-flow-parent` is a multi-module Maven project containing AI-powered Flow components:

```
vaadin-ai-flow-parent/
├── vaadin-ai-flow/              # Core AI abstractions and base components
├── vaadin-ai-chat-flow/         # AI Chat orchestrator (standard)
├── vaadin-ai-chart-flow/        # AI Chart orchestrator (commercial)
├── vaadin-ai-chat-flow-integration-tests/   # Integration tests for chat
└── vaadin-ai-chart-flow-integration-tests/  # Integration tests for charts
```

## Core Module: vaadin-ai-flow

**Purpose**: Provides core interfaces and abstractions for AI-powered components, including the base orchestrator class.

### Key Interfaces

#### LLMProvider (`com.vaadin.flow.component.ai.provider.LLMProvider`)

- Framework-agnostic interface for integrating Large Language Model services
- Provides streaming response generation via Reactor `Flux<String>`
- Uses `LLMRequest` interface for request configuration containing:
  - `userMessage` - the user's input text
  - `attachments` - list of file attachments (images, PDFs, text files)
  - `systemPrompt` - optional system prompt override
  - `tools` - array of `Tool` instances (vendor-agnostic)
  - `modelName` - optional model name override
- Uses vendor-agnostic `Tool` interface with:
  - `getName()` - tool identifier
  - `getDescription()` - what the tool does and its parameters
  - `getParametersSchema()` - optional JSON schema for parameters
  - `execute(String arguments)` - executes the tool with JSON arguments
- Implementations handle conversation memory internally per provider instance

**Implementations**:

1. **[LangChain4JLLMProvider](vaadin-ai-flow/src/main/java/com/vaadin/flow/component/ai/provider/langchain4j/LangChain4JLLMProvider.java)**

   - Bridges Vaadin AI components with LangChain4j library
   - Manages conversation memory internally per provider instance using `ChatMemory`
   - Each provider instance maintains its own conversation context
   - Converts vendor-agnostic `Tool` instances to LangChain4j `ToolSpecification`
   - Handles multimodal inputs (text, images, PDFs)
   - Automatic tool execution and follow-up requests
   - **Testing**: [LangChain4JLLMProviderTest](vaadin-ai-flow/src/test/java/com/vaadin/flow/component/ai/provider/langchain4j/LangChain4JLLMProviderTest.java)
   - **Dependency**: `dev.langchain4j:langchain4j:0.36.2` (optional)

2. **[SpringAILLMProvider](vaadin-ai-flow/src/main/java/com/vaadin/flow/component/ai/provider/springai/SpringAILLMProvider.java)**
   - Integration with Spring AI's chat models (fully implemented)
   - Manages conversation memory using Spring AI's `ChatMemory` and `MessageChatMemoryAdvisor`
   - Supports multimodal inputs (text, images, PDFs) via `Media` objects
   - Document processing with Apache Tika for PDF and text attachments
   - Each provider instance maintains its own conversation context
   - **Note**: Tool/function calling requires configuration at ChatModel level or via FunctionCallback
   - **Testing**: [SpringAILLMProviderTest](vaadin-ai-flow/src/test/java/com/vaadin/flow/component/ai/provider/springai/SpringAILLMProviderTest.java)
   - **Dependencies**:
     - `org.springframework.ai:spring-ai-core:1.0.0-M2` (optional)
     - `org.springframework.ai:spring-ai-tika-document-reader:1.0.0-M2` (optional)
   - **Repository**: Requires Spring Milestones repository (`https://repo.spring.io/milestone`)

#### DatabaseProvider (`com.vaadin.flow.component.ai.provider.DatabaseProvider`)

- Interface for database operations (schema introspection, query execution)
- Used by AI Chart orchestrator to query data sources
- Returns: schema as String, query results as `List<Map<String, Object>>`

#### AiMessageList (`com.vaadin.flow.component.ai.messagelist.AiMessageList`)

- Interface for UI components that display conversation messages
- Methods: `addMessage()`, `updateMessage()`, `createMessage()`
- Used by chat orchestrator for real-time message updates

#### AiInput (`com.vaadin.flow.component.ai.input.AiInput`)

- Interface for input components with submit capabilities
- Uses listener pattern via `InputSubmitListener` and `InputSubmitEvent`

#### AiFileReceiver (`com.vaadin.flow.component.ai.upload.AiFileReceiver`)

- Interface for file upload components
- Uses listener pattern via `FileUploadListener` and `FileUploadEvent`

#### AiMessage (`com.vaadin.flow.component.ai.messagelist.AiMessage`)

- Interface representing a single message in a conversation
- Properties: text, time, userName

#### BaseAiOrchestrator (`com.vaadin.flow.component.ai.orchestrator.BaseAiOrchestrator`)

- Abstract base class for all AI orchestrators providing common functionality
- **Shared Functionality**:
  - LLM provider management
  - Message list, input, and file receiver integration
  - UI context validation
  - User message handling (`addUserMessageToList()`)
  - Assistant message placeholder creation (`createAssistantMessagePlaceholder()`)
  - Streaming response handling with real-time UI updates (`streamResponseToMessage()`)
  - Complete input processing logic (`processUserInput()`)
  - Input event handling with Template Method pattern (`handleUserInput()`)
  - File attachment management (`configureFileReceiver()`, `pendingAttachments`)
  - Thread-safe UI updates via `UI.access()`
  - Error handling for streaming responses
- **Template Method Pattern**:
  - `handleUserInput(event)` validates input, adds user message, then calls `processUserInput(message)`
  - `processUserInput(message)` builds LLM request, handles attachments, streams response
  - Subclasses customize behavior by overriding hook methods
- **Hook Methods** (subclasses override for customization):

  - `createTools()` - returns array of tools for the LLM (default: empty array)
  - `getSystemPrompt()` - returns system prompt for the LLM (default: null)

  - `onProcessingComplete()` - called after streaming completes (default: no-op)

- **File Attachment Support**:
  - Manages `pendingAttachments` list for both orchestrators
  - Automatically configures file receiver upload handlers
  - Clears attachments after including them in LLM requests
- **Builder Pattern**:
  - Provides `BaseBuilder` abstract class for fluent API configuration
  - Automatically registers input listeners in `applyCommonConfiguration()`
  - Automatically configures file receivers when provided
- **Benefits**: Eliminates code duplication between ChatOrchestrator and ChartOrchestrator
- Both chat and chart orchestrators extend this base class and inherit common behavior

## Chat Module: vaadin-ai-chat-flow

**Purpose**: Orchestrates AI chat interactions between user input, LLM, and message display.

### Key Class: AiChatOrchestrator

**Location**: [AiChatOrchestrator.java](vaadin-ai-chat-flow/src/main/java/com/vaadin/flow/component/ai/chat/AiChatOrchestrator.java)

**Pattern**: Builder pattern for configuration

**Components**:

- `LLMProvider` (required) - handles AI generation
- `AiMessageList` (optional) - displays messages
- `AiInput` (optional) - captures user input
- `AiFileReceiver` (optional) - handles file uploads

**Flow**:

1. User submits input via `AiInput`
2. Base class `handleUserInput()` validates input and adds user message to `AiMessageList`
3. Base class `processUserInput()` builds LLM request and calls `LLMProvider.stream()`
4. Base class streams tokens to assistant message in real-time
5. Provider manages conversation history internally

**Key Features**:

- Extends `BaseAiOrchestrator` for all functionality
- Uses base class `processUserInput()` implementation with default hook methods
- No tools (returns empty array from `createTools()`)
- No system prompt override (returns null from `getSystemPrompt()`)
- Conversation history managed internally by the provider instance
- System prompts configured at the provider level (via `provider.setSystemPrompt()`)
- All streaming, error handling, and file attachment support inherited from base class
- Minimal code - just constructor, builder, and getter

**Testing**: [AiChatOrchestratorTest](vaadin-ai-chat-flow/src/test/java/com/vaadin/flow/component/ai/chat/AiChatOrchestratorTest.java) (extensive, ~40+ tests)

- Tests verify LLMRequest properties (userMessage) instead of message history
- Uses ArgumentCaptor<LLMProvider.LLMRequest> to capture and validate requests
- Verifies that multiple messages use the same provider instance for conversation continuity

## Chart Module: vaadin-ai-chart-flow (Commercial)

**Purpose**: Orchestrates AI-powered chart generation from natural language queries.

### Key Classes

#### AiChartOrchestrator

**Location**: [AiChartOrchestrator.java](vaadin-ai-chart-flow/src/main/java/com/vaadin/flow/component/ai/pro/chart/AiChartOrchestrator.java)

**Pattern**: Builder pattern for configuration

**Components**:

- `LLMProvider` (required) - AI model for chart generation
- `DatabaseProvider` (required) - database access
- `Chart` (optional) - Vaadin Charts component to update
- `AiInput` (optional) - user input for queries
- `AiFileReceiver` (optional) - handles file uploads
- `DataConverter` (optional) - converts query results to chart data

**Flow**:

1. User submits natural language query (e.g., "Show sales by region")
2. Base class `handleUserInput()` validates input and adds user message to `AiMessageList`
3. Override `processUserInput()` stores currentUserRequest and currentUI, then calls super
4. Base class `processUserInput()` calls hook methods to get tools and system prompt
5. Base class builds LLM request with tools and streams response
6. AI generates SQL queries and chart configurations using provided tools:
   - `getSchema` - retrieve database schema
   - `updateChartData` - execute SQL and update chart with data
   - `updateChartConfig` - update chart configuration (title, colors, etc.)
7. Tools execute and update the chart directly
8. Base class handles streaming and calls `onProcessingComplete()` when done

**Key Features**:

- Extends `BaseAiOrchestrator` and uses base class `processUserInput()` implementation
- Overrides hook methods for customization:
  - `createTools()` - returns 3 tools (getSchema, updateChartData, updateChartConfig)
  - `getSystemPrompt()` - returns SYSTEM_PROMPT with chart generation instructions
  - `onProcessingComplete()` - logs completion
- Overrides `processUserInput()` to store currentUserRequest and currentUI for tool use
- All streaming, error handling, and file attachment support inherited from base class

**Tools Implementation**:

- Tools use JSON for parameter passing
- Each tool returns success/error messages to AI
- AI can make follow-up tool calls based on results
- Thread-safe updates via UI.access() for chart manipulation

**Testing**: [AiChartOrchestratorTest](vaadin-ai-chart-flow/src/test/java/com/vaadin/flow/component/ai/pro/chart/AiChartOrchestratorTest.java) (37 tests)

#### DataConverter Interface

**Location**: [DataConverter.java](vaadin-ai-chart-flow/src/main/java/com/vaadin/flow/component/ai/pro/chart/DataConverter.java)

**Purpose**: Converts database query results to Vaadin Charts `DataSeries`

**Default Implementation**: [DefaultDataConverter](vaadin-ai-chart-flow/src/main/java/com/vaadin/flow/component/ai/pro/chart/DefaultDataConverter.java)

- Assumes first column = category/label
- Second column = numeric value
- Handles null values (category→"Unknown", value→0)
- Parses string numbers
- Supports Integer, Long, Double types

**Testing**: [DefaultDataConverterTest](vaadin-ai-chart-flow/src/test/java/com/vaadin/flow/component/ai/pro/chart/DefaultDataConverterTest.java) (17 tests)

## Architecture Patterns

### 1. Interface-Based Design

All core components are interfaces, allowing flexible implementations:

- UI components (MessageList, Input) can be any Vaadin component
- LLM providers can be any AI service (OpenAI, Anthropic, local models)
- Database providers can use any data source

### 2. Builder Pattern

Both orchestrators use builders for clean, fluent configuration:

```java
AiChatOrchestrator orchestrator = AiChatOrchestrator.create(provider)
    .withMessageList(messageList)
    .withInput(input)
    .build();
```

### 3. Reactive Streaming

Uses Project Reactor `Flux<String>` for:

- Token-by-token streaming responses
- Backpressure handling
- Async operations with error propagation

### 4. Tool Calling Pattern

AI Chart orchestrator demonstrates tool/function calling:

- Tools defined as `LLMProvider.Tool` with JSON schemas
- AI decides when and how to call tools
- Results fed back to AI for next steps
- Supports multi-step workflows

### 5. Thread Safety

All orchestrators (via BaseAiOrchestrator) use `UI.access()` for Vaadin's server push:

- All UI updates wrapped in access() calls
- Validates UI context exists before operations
- Throws `IllegalStateException` if no UI context
- Thread-safe message list updates during streaming

## Testing Strategy

### Unit Tests

- Mock external dependencies (LLM, Database, UI components)
- Test orchestrator logic and state management
- Verify conversation history handling
- Test error scenarios

**Key Test Utilities**:

- Custom `TestUI` class that executes `UI.access()` synchronously
- Mockito for mocking LLM providers and components
- ArgumentCaptor for verifying method calls and parameters

### Integration Tests

- Separate modules: `*-integration-tests`
- Full component integration with real UI
- Typically run in development mode only

## Dependencies

### vaadin-ai-flow

- `reactor-core` (3.6.11) - Reactive streams
- `langchain4j` (0.36.2, optional) - LangChain4j integration
- `flow-data`, `flow-html-components` - Vaadin core

### vaadin-ai-chart-flow

- Inherits from vaadin-ai-flow
- `vaadin-charts-flow` - Vaadin Charts component
- Additional commercial license

## Common Development Tasks

### Adding a New Orchestrator

1. Create new module under `vaadin-ai-flow-parent`
2. Depend on `vaadin-ai-flow` for core interfaces
3. Implement orchestrator with builder pattern
4. Use `LLMProvider` for AI interactions
5. Implement tools if needed (as `LLMProvider.Tool`)
6. Add unit tests following existing patterns
7. Create integration test module

### Adding a New LLM Provider

1. Implement `LLMProvider` interface
2. Handle message conversion to provider's format
3. Convert streaming response to `Flux<String>`
4. Implement tool calling if provider supports it
5. Add comprehensive unit tests
6. Document configuration in JavaDoc

### Extending DataConverter

1. Implement `DataConverter` interface
2. Handle various data structures (multi-series, time-series, etc.)
3. Consider edge cases (nulls, empty results, type mismatches)
4. Add unit tests covering all scenarios
5. Document expected data format in JavaDoc

## Important Notes

### Licensing

- `vaadin-ai-flow` - Apache 2.0 (standard)
- `vaadin-ai-chat-flow` - Apache 2.0 (standard)
- `vaadin-ai-chart-flow` - Commercial license (Pro tier)

### Serialization

All orchestrators and providers implement `Serializable` for Vaadin session storage.

### Conversation History

Conversation history is managed internally by LLM provider instances:

- Each provider instance maintains its own conversation memory
- Orchestrators use a dedicated provider instance for each conversation
- Providers maintain message history internally (e.g., using ChatMemory in LangChain4j)
- Memory management is handled transparently by the provider
- For multiple separate conversations, create multiple provider instances

### Tool Execution Safety

Chart orchestrator tools execute database queries:

- Validate/sanitize SQL if needed
- Consider query timeouts
- Handle database errors gracefully
- Return descriptive error messages to AI

## Future Development Areas

### Potential Enhancements

1. **Memory Management**: Implement conversation summarization for long chats
2. **Tool Schema**: Use proper JSON schemas for tool parameters
3. **Streaming Charts**: Support incremental chart updates during data queries
4. **Multi-Modal**: Support image inputs/outputs via LLM providers
5. **RAG Support**: Add document retrieval interfaces for knowledge bases
6. **Caching**: Implement response caching for repeated queries

### Breaking Changes to Avoid

- Keep interfaces stable - extend with new methods if needed
- Maintain builder pattern compatibility
- Preserve conversation history format
- Keep tool calling API consistent

## Quick Reference

### Building and Testing

```bash
# Build all modules
mvn clean install

# Run unit tests only
mvn test -pl vaadin-ai-flow,vaadin-ai-chat-flow,vaadin-ai-chart-flow

# Run specific test
mvn test -Dtest=AiChatOrchestratorTest

# Skip integration tests
mvn install -DskipTests
```

### Code Locations

- Core interfaces: `vaadin-ai-flow/src/main/java/com/vaadin/flow/component/ai/`
- Base orchestrator: `vaadin-ai-flow/src/main/java/com/vaadin/flow/component/ai/orchestrator/`
- Providers: `vaadin-ai-flow/src/main/java/com/vaadin/flow/component/ai/provider/`
- Chat orchestrator: `vaadin-ai-chat-flow/src/main/java/com/vaadin/flow/component/ai/chat/`
- Chart orchestrator: `vaadin-ai-chart-flow/src/main/java/com/vaadin/flow/component/ai/pro/chart/`
- Tests: `*/src/test/java/` (mirrors main structure)

## Recent Changes

### 2025-11-21: Complete BaseAiOrchestrator Refactoring

- Created `BaseAiOrchestrator` abstract base class to eliminate ALL code duplication
- Moved complete processing logic to base class:
  - User message handling (`addUserMessageToList()`)
  - Assistant message placeholder creation (`createAssistantMessagePlaceholder()`)
  - Streaming response handling with real-time UI updates (`streamResponseToMessage()`)
  - Complete input processing implementation (`processUserInput()`)
  - Input event handling using Template Method pattern (`handleUserInput()`)
  - Input listener registration (automatic in builder)
  - File attachment management (`configureFileReceiver()`, `pendingAttachments`)
  - Thread-safe UI updates and error handling
- Hook Method pattern implementation:
  - Base class provides complete `processUserInput()` implementation
  - Subclasses customize via hook methods: `createTools()`, `getSystemPrompt()`, `onProcessingComplete()`
  - AiChatOrchestrator uses all default hook implementations (no tools, no system prompt)
  - AiChartOrchestrator overrides all hooks for tool support and chart-specific behavior
  - Eliminates need for subclasses to duplicate processing logic
- Builder improvements:
  - Input listeners registered automatically in `applyCommonConfiguration()`
  - File receivers configured automatically when provided
  - Subclass builders simplified to just orchestrator-specific configuration
- File receiver support:
  - Moved file attachment logic to base class
  - Both orchestrators now support file uploads automatically
  - Chart orchestrator gained file attachment capability
- Message list handling:
  - Chat orchestrator requires messageList (default false)
  - Chart orchestrator can work without messageList (returns true, tools update chart directly)
- Code reduction:
  - AiChatOrchestrator reduced to minimal code (~20 lines - just constructor, builder, getter)
  - AiChartOrchestrator simplified significantly with hook method overrides
  - Eliminated ~150+ lines of duplicate code
- All existing tests pass (129 tests total: 33 + 42 + 54)
- No breaking changes to public API

---

**Last Updated**: 2025-11-21
**Vaadin Version**: 25.0-SNAPSHOT
