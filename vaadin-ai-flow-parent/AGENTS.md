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

**Purpose**: Provides core interfaces and abstractions for AI-powered components.

### Key Interfaces

#### LLMProvider (`com.vaadin.flow.component.ai.provider.LLMProvider`)
- Interface for integrating Large Language Model services
- Provides streaming response generation via Reactor `Flux<String>`
- Supports system prompts, conversation history, and tool calling
- Contains nested interfaces:
  - `Message` - represents conversation messages with role and content
  - `Tool` - represents callable tools with name, description, parameters, and execution logic

**Implementation**: [LangChain4jProvider](vaadin-ai-flow/src/main/java/com/vaadin/flow/component/ai/provider/langchain4j/LangChain4jProvider.java)
- Bridges Vaadin AI components with LangChain4j library
- Converts between generic `LLMProvider` types and LangChain4j-specific types
- Handles tool execution and follow-up requests automatically
- **Testing**: [LangChain4jProviderTest](vaadin-ai-flow/src/test/java/com/vaadin/flow/component/ai/provider/langchain4j/LangChain4jProviderTest.java) (23 tests)

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
2. Orchestrator adds user message to `AiMessageList`
3. Calls `LLMProvider.generateStream()` with conversation history
4. Streams tokens to assistant message in real-time
5. Updates conversation history on completion

**Key Features**:
- Maintains conversation history (user + assistant messages)
- Streaming responses with incremental UI updates
- Error handling with error messages displayed in chat
- UI.access() for thread-safe Vaadin updates
- Validates UI context exists before operations

**Testing**: [AiChatOrchestratorTest](vaadin-ai-chat-flow/src/test/java/com/vaadin/flow/component/ai/chat/AiChatOrchestratorTest.java) (extensive, ~40+ tests)

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
- `DataConverter` (optional) - converts query results to chart data

**Flow**:
1. User submits natural language query (e.g., "Show sales by region")
2. Orchestrator provides AI with three tools:
   - `getSchema` - retrieve database schema
   - `updateChartData` - execute SQL and update chart with data
   - `updateChartConfig` - update chart configuration (title, colors, etc.)
3. AI generates SQL queries and chart configurations
4. Tools execute and update the chart
5. System prompt guides AI on proper chart configuration

**Tools Implementation**:
- Tools use JSON for parameter passing
- Each tool returns success/error messages to AI
- AI can make follow-up tool calls based on results
- Thread-safe updates via UI.access()

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
Both orchestrators use `UI.access()` for Vaadin's server push:
- All UI updates wrapped in access() calls
- Validates UI context exists before operations
- Throws `IllegalStateException` if no UI context

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
Chat orchestrator maintains full conversation history in memory:
- User messages
- Assistant responses
- Used for context in subsequent requests
- Consider memory implications for long conversations

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
- Providers: `vaadin-ai-flow/src/main/java/com/vaadin/flow/component/ai/provider/`
- Chat orchestrator: `vaadin-ai-chat-flow/src/main/java/com/vaadin/flow/component/ai/chat/`
- Chart orchestrator: `vaadin-ai-chart-flow/src/main/java/com/vaadin/flow/component/ai/pro/chart/`
- Tests: `*/src/test/java/` (mirrors main structure)

---

**Last Updated**: 2025-11-20
**Vaadin Version**: 25.0-SNAPSHOT
