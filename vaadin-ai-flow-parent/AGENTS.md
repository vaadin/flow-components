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
  - Programmatic message sending (`sendMessage(String userMessage)`)
  - File attachment management (`configureFileReceiver()`, `pendingAttachments`)
  - Thread-safe UI updates via `UI.access()`
  - Error handling for streaming responses
  - Automatic UI.access() wrapping for @Tool annotated methods
- **Template Method Pattern**:
  - `handleUserInput(event)` validates input, adds user message, then calls `processUserInput(message)`
  - `processUserInput(message)` builds LLM request, handles attachments, streams response
  - Subclasses customize behavior by overriding hook methods
- **Programmatic Message API**:
  - `sendMessage(String userMessage)` - send messages without requiring an input component
  - Useful for triggering AI interaction from button clicks or other UI events
  - Example: `orchestrator.sendMessage("Analyze the uploaded file");`
- **Hook Methods** (subclasses override for customization):

  - `createTools()` - returns array of tools for the LLM (default: empty array)
  - `getSystemPrompt()` - returns system prompt for the LLM (default: null)

  - `onProcessingComplete()` - called after streaming completes (default: no-op)

- **Tool Annotation Support**:
  - Use `@Tool` annotation to define methods that can be called by the LLM
  - Use `@ParameterDescription` to describe method parameters
  - Configure tools via builder: `.setTools(this)` or `.setTools(toolObject)`
  - Automatic UI.access() wrapping: Tool methods are automatically executed within UI.access() if a UI is available
  - UI is obtained from the input component's getUI() method
  - If no UI is available, tools execute directly (no warning)
  - Developers don't need to manually wrap tool code in UI.access()
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

**Purpose**: Provides data visualization capabilities as a plugin for AI orchestrators.

This module provides the `DataVisualizationPlugin` which can be added to any orchestrator (like `AiChatOrchestrator`) to enable AI-powered data visualization from natural language queries.

### Key Classes

#### DataVisualizationPlugin

**Location**: [DataVisualizationPlugin.java](vaadin-ai-chart-flow/src/main/java/com/vaadin/flow/component/ai/pro/chart/DataVisualizationPlugin.java)

**Pattern**: Plugin architecture with builder pattern for configuration

**Purpose**: Plugin that adds data visualization capabilities (charts, grids, KPIs) to AI orchestrators with dynamic type switching.

**Configuration (Builder)**:

- `DatabaseProvider` (required) - database access for querying data
- `Component visualizationContainer` (optional) - container where visualizations are rendered (can be Div, Chart, or any Component)
- `DataConverter chartDataConverter` (optional) - converts query results to chart data (only used for chart visualizations)
- `VisualizationType initialType` (optional) - initial visualization type (defaults to CHART)

**Supported Visualization Types**:

- **CHART**: Line, bar, column, pie, area charts using Vaadin Charts
  - Requires 2-column data (label, value) after conversion
  - Uses DataConverter for data transformation
- **GRID**: Tabular data with sortable columns using Vaadin Grid
  - Works directly with raw query results
  - No conversion needed - shows all returned columns
- **KPI**: Key Performance Indicator cards showing single metrics
  - Extracts first value from query results
  - Supports custom formatting and labels

**Flow**:

1. User submits natural language request (e.g., "Show sales by region as a chart")
2. Orchestrator routes to plugin's tools
3. AI generates SQL queries using database schema
4. Tool executes query and renders visualization based on current type
5. User can dynamically switch types: "Show this as a table instead"
6. AI calls `changeVisualizationType` tool - same data, different rendering

**Key Features**:

- **Plugin Architecture**: Can be added to any orchestrator (AiChatOrchestrator, custom orchestrators)
- **Dynamic Type Switching**: Users can change visualization types without re-querying
- **Type-Specific Rendering**: Smart rendering based on visualization type
  - Chart: Converts data via DataConverter, applies Highcharts config
  - Grid: Direct rendering with auto-generated columns
  - KPI: Extracts and formats single value
- **State Management**: `VisualizationState` captures type, SQL query, and configuration
- **Flexible Container**: Works with any Component container, not just Chart
- **Composable**: Can be combined with other plugins in the same orchestrator

**Tools**:

1. `getSchema()` - Retrieves database schema
2. `updateChart(query, config)` - Creates/updates chart visualization
3. `updateGrid(query)` - Creates/updates grid/table visualization
4. `updateKpi(query, label, format)` - Creates/updates KPI card
5. `changeVisualizationType(type, config)` - Changes type while keeping current data

**System Prompt Contribution**: Instructs AI on available visualization types and when to use each

**Example Usage**:

```java
// Create plugin
Div container = new Div();
DataVisualizationPlugin plugin = DataVisualizationPlugin
    .create(databaseProvider)
    .withVisualizationContainer(container)
    .withInitialType(VisualizationType.CHART)
    .build();

// Add to orchestrator
AiChatOrchestrator orchestrator = AiChatOrchestrator
    .create(llmProvider)
    .withMessageList(messageList)
    .withInput(messageInput)
    .withPlugin(plugin)
    .build();

// User can now say:
// - "Show monthly revenue as a chart"
// - "Convert this to a table"
// - "Show total revenue as a KPI"
// - "Tell me a joke" (regular chat, no visualization)
```

**Dashboard Integration**:

Perfect for dashboard applications where each widget needs different visualization types:

```java
Dashboard dashboard = new Dashboard();

// Create widget with its own plugin
DashboardWidget widget = new DashboardWidget("Sales Data");
Div content = new Div();
widget.setContent(content);

DataVisualizationPlugin plugin = DataVisualizationPlugin
    .create(databaseProvider)
    .withVisualizationContainer(content)
    .build();

// Create orchestrator for this widget with the plugin
AiChatOrchestrator orchestrator = AiChatOrchestrator
    .create(llm)
    .withPlugin(plugin)
    .build();

dashboard.add(widget);
```

**State Persistence**:

```java
// Capture state
Object state = plugin.captureState();
// State contains: type, sqlQuery, configuration

// Later, restore
plugin.restoreState(state);
// Re-executes query and renders with saved type/config
```

#### AiPlugin Interface

**Location**: [AiPlugin.java](vaadin-ai-flow/src/main/java/com/vaadin/flow/component/ai/orchestrator/AiPlugin.java)

**Purpose**: Base interface for all AI plugins that extend orchestrator capabilities

**Key Methods**:

- `getTools()` - Returns list of tools this plugin provides to the LLM
- `getSystemPromptContribution()` - Returns text to add to the system prompt
- `onAttached(BaseAiOrchestrator)` - Called when plugin is attached to an orchestrator
- `onDetached()` - Called when plugin is detached (cleanup)
- `captureState()` - Returns serializable state object for persistence
- `restoreState(Object)` - Restores plugin from a previously captured state
- `getPluginId()` - Returns unique identifier for this plugin type

**Example Custom Plugin**:

```java
public class MyCustomPlugin implements AiPlugin {
    @Override
    public List<LLMProvider.Tool> getTools() {
        return List.of(new MyTool());
    }

    @Override
    public String getSystemPromptContribution() {
        return "You have access to myTool which does X, Y, Z.";
    }

    @Override
    public void onAttached(BaseAiOrchestrator orchestrator) {
        // Initialize plugin
    }
}

// Use in orchestrator
AiChatOrchestrator orchestrator = AiChatOrchestrator
    .create(llmProvider)
    .withPlugin(new MyCustomPlugin())
    .withPlugin(dataVizPlugin)  // Multiple plugins!
    .build();
```
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

### 2025-11-21: Chart State Persistence API (Phase 1, 2 & 3 - Complete)

**Phase 1 - Core State Model**:
- Created `ChartState` interface for chart state snapshots
- Implemented `DefaultChartState` with full serialization support
- Added comprehensive test suite (16 tests) covering:
  - Factory method creation
  - Null value handling
  - Equality and hashing
  - Java serialization/deserialization
  - Immutability guarantees
- Key design decisions:
  - Only SQL query and chart config are persisted (not conversation history or timestamps)
  - Immutable snapshots for safe persistence
  - Fully serializable for flexible storage options

**Phase 2 - Orchestrator Integration**:
- Added state tracking fields (`currentSqlQuery`, `currentChartConfig`) to `AiChartOrchestrator`
- Implemented `captureState()` method - creates immutable snapshot of current chart state
- Implemented `restoreState(ChartState)` method - restores chart by:
  - Re-executing SQL query to get fresh data
  - Reapplying Highcharts configuration
  - Updating internal state for subsequent captures
- Modified AI tools to automatically capture state:
  - `updateChartData` tool captures SQL query after successful execution
  - `updateChartConfig` tool captures configuration after successful application
- Added 8 new integration tests (total 43 orchestrator tests) covering:
  - State capture after data updates
  - State capture after config updates
  - State capture after both updates
  - State restoration with SQL query only
  - State restoration with config only
  - State restoration with both
  - Null state handling
- Complete dashboard persistence use case now supported

**Phase 3 - Event System**:
- Created `ChartStateChangeEvent` class extending `EventObject`:
  - Contains chart state snapshot
  - Includes `StateChangeType` enum (DATA_QUERY_UPDATED, CONFIGURATION_UPDATED, BOTH_UPDATED)
  - Provides typed `getSource()` method returning `AiChartOrchestrator`
- Created `ChartStateChangeListener` functional interface:
  - Single method: `onStateChange(ChartStateChangeEvent)`
  - Serializable for session persistence
  - Enables lambda expressions for clean listener code
- Added event listener management to `AiChartOrchestrator`:
  - `addStateChangeListener(ChartStateChangeListener)` - register listener (validates non-null)
  - `removeStateChangeListener(ChartStateChangeListener)` - unregister listener
  - `fireStateChangeEvent(StateChangeType)` - internal method to notify all listeners
- Modified AI tools to fire events after state changes:
  - `updateChartData` fires `DATA_QUERY_UPDATED` event after capturing SQL query
  - `updateChartConfig` fires `CONFIGURATION_UPDATED` event after capturing config
- Added 6 comprehensive event tests (total 49 orchestrator tests) covering:
  - Event firing on data update with correct state and type
  - Event firing on config update with correct state and type
  - Multiple listeners all receiving events
  - Listener removal preventing notification
  - Null listener validation
  - Event source verification
- Use cases enabled:
  - Auto-save: Listen to state changes and persist to database/file
  - Undo/Redo: Track state change history
  - Analytics: Monitor chart usage and modifications
  - Real-time collaboration: Notify other users of chart changes
- Updated AGENTS.md documentation with event system usage

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
