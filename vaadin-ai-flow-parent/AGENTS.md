# Vaadin AI Components - Agent Guide

This document provides an overview of the Vaadin AI Components architecture for AI coding agents.

## Module Structure

The `vaadin-ai-flow-parent` is a multi-module Maven project containing AI-powered Flow components:

```
vaadin-ai-flow-parent/
├── vaadin-ai-flow/              # Core AI abstractions, orchestrator, and components
├── vaadin-ai-chart-flow/        # AI Chart data visualization plugin (commercial)
├── vaadin-ai-chat-flow-integration-tests/   # Integration tests for chat
└── vaadin-ai-chart-flow-integration-tests/  # Integration tests for charts
```

## Core Module: vaadin-ai-flow

**Purpose**: Provides core interfaces, abstractions, and the main `AiOrchestrator` class for AI-powered components.

### Key Interfaces

#### LLMProvider (`com.vaadin.flow.component.ai.provider.LLMProvider`)

- Framework-agnostic interface for integrating Large Language Model services
- Provides streaming response generation via Reactor `Flux<String>`
- Uses `LLMRequest` interface for request configuration containing:
  - `userMessage` - the user's input text
  - `attachments` - list of file attachments (images, PDFs, text files)
  - `systemPrompt` - optional system prompt override
  - `tools` - array of `Tool` instances (vendor-agnostic)
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

### AiOrchestrator (`com.vaadin.flow.component.ai.orchestrator.AiOrchestrator`)

**Location**: [AiOrchestrator.java](vaadin-ai-flow/src/main/java/com/vaadin/flow/component/ai/orchestrator/AiOrchestrator.java)

**Purpose**: Main orchestrator class for AI-powered chat interfaces that connects user input, LLM providers, and message display.

**Pattern**: Builder pattern for configuration

**Components**:

- `LLMProvider` (required) - handles AI generation
- `AiMessageList` (optional) - displays messages
- `AiInput` (optional) - captures user input
- `AiFileReceiver` (optional) - handles file uploads

**Core Functionality**:

- LLM provider management
- Message list, input, and file receiver integration
- UI context validation
- User message handling (`addUserMessageToList()`)
- Assistant message placeholder creation (`createAssistantMessagePlaceholder()`)
- Streaming response handling with real-time UI updates (`streamResponseToMessage()`)
- Complete input processing logic (`processUserInput()`)
- Input event handling with Template Method pattern (`handleUserInput()`)
- Programmatic message sending (`prompt(String userMessage)`)
- File attachment management (`configureFileReceiver()`, `pendingAttachments`)
- Thread-safe UI updates via `UI.access()`
- Error handling for streaming responses
- Automatic UI.access() wrapping for @Tool annotated methods

**Template Method Pattern**:

- `handleUserInput(event)` validates input, adds user message, then calls `processUserInput(message)`
- `processUserInput(message)` builds LLM request, handles attachments, streams response
- Hook methods available for customization

**Programmatic Message API**:

- `prompt(String userMessage)` - send messages without requiring an input component
- Useful for triggering AI interaction from button clicks or other UI events
- Example: `orchestrator.prompt("Analyze the uploaded file");`

**Hook Methods** (can be overridden for customization):

- `createTools()` - returns array of tools for the LLM (default: from controllers only)
- `getSystemPrompt()` - returns system prompt for the LLM (default: null)
- `onProcessingComplete()` - called after streaming completes (default: no-op)

**Tool Annotation Support**:

- Use `@Tool` annotation to define methods that can be called by the LLM
- Use `@ParameterDescription` to describe method parameters
- Configure tools via builder: `.withVendorToolObjects(this)` or `.withVendorToolObjects(toolObject)`
- Automatic UI.access() wrapping: Tool methods are automatically executed within UI.access() if a UI is available
- UI is obtained from the input component's getUI() method
- If no UI is available, tools execute directly (no warning)
- Developers don't need to manually wrap tool code in UI.access()

**Controller Support**:

- Extensible via `AiController` interface
- Controllers can contribute tools, system prompts, and manage state
- Add controllers via builder: `.withController(new MyController())`
- Multiple controllers can be active simultaneously

**File Attachment Support**:

- Manages `pendingAttachments` list
- Automatically configures file receiver upload handlers
- Clears attachments after including them in LLM requests

**Builder Pattern**:

- Use `AiOrchestrator.builder(provider)` to start building
- Fluent API for configuration: `.withMessageList()`, `.withInput()`, `.withFileReceiver()`, `.withController()`, `.withVendorToolObjects()`
- Call `.build()` to construct the orchestrator
- Automatically registers input listeners and configures file receivers

**Flow**:

1. User submits input via `AiInput`
2. `handleUserInput()` validates input and adds user message to `AiMessageList`
3. `processUserInput()` builds LLM request and calls `LLMProvider.stream()`
4. Streams tokens to assistant message in real-time
5. Provider manages conversation history internally

**Conversation History**:

- Managed internally by the provider instance
- Each orchestrator maintains its own conversation context through its provider
- System prompts configured at the provider level (via `provider.setSystemPrompt()`)

**Testing**: [AiOrchestratorTest](vaadin-ai-flow/src/test/java/com/vaadin/flow/component/ai/orchestrator/AiOrchestratorTest.java) (extensive, 50+ tests)

- Tests verify LLMRequest properties (userMessage) instead of message history
- Uses ArgumentCaptor<LLMProvider.LLMRequest> to capture and validate requests
- Verifies that multiple messages use the same provider instance for conversation continuity

**Example Usage**:

```java
MessageList messageList = new MessageList();
MessageInput messageInput = new MessageInput();

LLMProvider provider = new LangChain4jProvider(model);
provider.setSystemPrompt("You are a helpful assistant.");

AiOrchestrator orchestrator = AiOrchestrator.builder(provider)
        .withMessageList(messageList)
        .withInput(messageInput)
        .build();
```

## Chart Module: vaadin-ai-chart-flow (Commercial)

**Purpose**: Provides chart visualization capabilities as a controller for AI orchestrators.

This module provides the `ChartAiController` which can be added to any orchestrator (like `AiOrchestrator`) to enable AI-powered chart visualizations from natural language queries.

### Key Classes

#### ChartAiController

**Location**: [ChartAiController.java](vaadin-ai-chart-flow/src/main/java/com/vaadin/flow/component/ai/pro/chart/ChartAiController.java)

**Pattern**: Direct constructor-based configuration (simple API)

**Purpose**: Controller that adds AI-powered chart visualization capabilities to orchestrators using Vaadin Charts.

**Configuration (Constructor)**:

- `Chart chart` (required) - Vaadin Chart component instance where visualizations are rendered
- `DatabaseProvider databaseProvider` (required) - provides database access for schema introspection and query execution

**Supported Chart Types**:

- **Line charts**: For trends over time
- **Bar/Column charts**: For comparisons between categories
- **Pie charts**: For showing proportions
- **Area charts**: For cumulative values over time

**Flow**:

1. User submits natural language request (e.g., "Show sales by region as a bar chart")
2. Orchestrator routes to controller's tools
3. AI uses `getSchema()` to understand available data
4. AI generates appropriate SQL query
5. AI calls `updateChart()` with query and chart configuration
6. Tool executes query, converts data, and updates the Chart component
7. User can request changes: "Change that to a line chart"
8. AI calls `updateChart()` again with new configuration

**Key Features**:

- **Simple API**: Direct constructor - `new ChartAiController(chart, databaseProvider)`
- **Controller Architecture**: Can be added to any orchestrator (AiOrchestrator, custom orchestrators)
- **Chart-Focused**: Dedicated to chart visualizations (line, bar, column, pie, area)
- **State Management**: `ChartState` captures SQL query and chart configuration for persistence
- **Data Conversion**: Built-in `DefaultDataConverter` handles common query result formats
- **Thread-Safe**: Automatic UI.access() wrapping for safe chart updates
- **Composable**: Can be combined with other controllers in the same orchestrator

**Tools**:

1. `getSchema()` - Retrieves database schema (tables, columns, data types)
2. `updateChart(query, config)` - Executes SQL query and creates/updates chart with specified type and configuration

**System Prompt**: Use static method `ChartAiController.getSystemPrompt()` to get the recommended system prompt text that describes chart capabilities to the AI

**Example Usage**:

```java
// Create chart component and controller
Chart chart = new Chart();
ChartAiController controller = new ChartAiController(chart, databaseProvider);

// Compose system prompt
String systemPrompt = "You are a data visualization assistant. "
        + ChartAiController.getSystemPrompt();

// Create orchestrator with controller
AiOrchestrator orchestrator = AiOrchestrator
    .create(llmProvider, systemPrompt)
    .withMessageList(messageList)
    .withInput(messageInput)
    .withController(controller)
    .build();

// User can now say:
// - "Show monthly revenue as a line chart"
// - "Change that to a bar chart"
// - "Display sales by region as a pie chart"
// - "Tell me a joke" (regular chat, no visualization)
```

**Dashboard Integration**:

Perfect for dashboard applications where each widget contains a chart:

```java
Dashboard dashboard = new Dashboard();

// Create widget with chart
DashboardWidget widget = new DashboardWidget("Sales Data");
Chart chart = new Chart();
widget.setContent(chart);

// Create controller for this chart
ChartAiController controller = new ChartAiController(chart, databaseProvider);

// Create orchestrator for this widget with the controller
AiOrchestrator orchestrator = AiOrchestrator
    .create(llm, ChartAiController.getSystemPrompt())
    .withController(controller)
    .build();

dashboard.add(widget);
```

#### AiController Interface

**Location**: [AiController.java](vaadin-ai-flow/src/main/java/com/vaadin/flow/component/ai/orchestrator/AiController.java)

**Purpose**: Base interface for all AI controllers that extend orchestrator capabilities by providing tools to the LLM

**Key Methods**:

- `getTools()` - Returns list of tools this controller provides to the LLM (default: empty list)

**System Prompts**: System prompts are provided when creating the orchestrator. Built-in controllers like `ChartAiController` provide a static helper method (e.g., `ChartAiController.getSystemPrompt()`) to get the recommended prompt text.

**Example Custom Controller**:

```java
public class MyCustomController implements AiController {
    // Static helper for system prompt
    public static String getSystemPrompt() {
        return "You have access to myTool which does X, Y, Z.";
    }

    @Override
    public List<LLMProvider.Tool> getTools() {
        return List.of(new MyTool());
    }
}

// Use in orchestrator - provide system prompt at creation
String systemPrompt = "You are a helpful assistant. "
    + MyCustomController.getSystemPrompt()
    + ChartAiController.getSystemPrompt();

AiOrchestrator orchestrator = AiOrchestrator
    .create(llmProvider, systemPrompt)
    .withController(new MyCustomController())
    .withController(chartController)  // Multiple controllers!
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
AiOrchestrator orchestrator = AiOrchestrator.builder(provider)
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

All orchestrators (via AiOrchestrator) use `UI.access()` for Vaadin's server push:

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

- `vaadin-ai-flow` - Apache 2.0 (includes core orchestrator)
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
mvn test -pl vaadin-ai-flow,vaadin-ai-chart-flow

# Run specific test
mvn test -Dtest=AiOrchestratorTest -pl vaadin-ai-flow

# Skip integration tests
mvn install -DskipTests
```

### Code Locations

- Core interfaces: `vaadin-ai-flow/src/main/java/com/vaadin/flow/component/ai/`
- AiOrchestrator: `vaadin-ai-flow/src/main/java/com/vaadin/flow/component/ai/orchestrator/`
- Providers: `vaadin-ai-flow/src/main/java/com/vaadin/flow/component/ai/provider/`
- Chart controller: `vaadin-ai-chart-flow/src/main/java/com/vaadin/flow/component/ai/pro/chart/`
- Tests: `*/src/test/java/` (mirrors main structure)

## Recent Changes

### 2025-12-02: ChartAiController Simplification

**Goal**: Simplify the chart controller by focusing exclusively on chart visualizations and removing unnecessary complexity.

**Changes**:

- **Renamed**: `DataVisualizationPlugin` → `ChartAiController`
  - Clearer name reflecting chart-focused purpose
  - Updated all imports and references in demo views
- **Removed Builder Pattern**: Replaced with simple direct constructor
  - Old: `DataVisualizationPlugin.create(provider).withVisualizationContainer(div).build()`
  - New: `new ChartAiController(chart, databaseProvider)`
  - Chart component must be instantiated by user and passed to constructor
- **Removed Grid and KPI Support**: Controller now exclusively handles charts
  - Removed `VisualizationType` enum
  - Removed `updateGrid()` and `updateKpi()` tools
  - Removed `changeVisualizationType()` tool
  - Simplified to 2 tools only: `getSchema()` and `updateChart()`
- **Simplified Constructor**:
  - Takes `Chart` component directly (not a generic container)
  - Takes `DatabaseProvider` for data access
  - No optional parameters or builder configuration
- **Updated State Management**:
  - `ChartState` record now stores only: `sqlQuery` and `configuration`
  - Removed visualization type from state
  - Controller ID changed from "DataVisualization" to "AiChart"
- **Simplified Data Conversion**:
  - Kept `DefaultDataConverter` for common cases
  - Removed complex multi-type rendering logic
- **Updated Demo Views**:
  - `AiChatWithDataVizPluginDemo`: Now uses Chart component directly
  - `AiDashboardDemoView`: Each widget creates its own Chart instance

**Benefits**:

- **Simpler API**: Just `new ChartAiController(chart, provider)` - no builder needed
- **Clearer Purpose**: Chart-focused, not generic visualization
- **Less Code**: ~370 lines vs ~800 lines (54% reduction)
- **Better User Control**: Users instantiate and configure Chart component themselves
- **Easier to Understand**: Single responsibility - chart visualization only

**Migration**:

```java
// Old way (no longer supported)
Div container = new Div();
DataVisualizationPlugin plugin = DataVisualizationPlugin
    .create(databaseProvider)
    .withVisualizationContainer(container)
    .withInitialType(VisualizationType.CHART)
    .build();

// New way
Chart chart = new Chart();
ChartAiController controller = new ChartAiController(chart, databaseProvider);

String systemPrompt = "You are helpful. " + ChartAiController.getSystemPrompt();
AiOrchestrator.builder(provider, systemPrompt)
    .withController(controller)
    .build();
```

### 2025-12-02: Controller System Prompt Simplification

**Goal**: Simplify system prompt management by removing it from the controller API and making it explicit at orchestrator creation time.

**Changes**:

- **Removed**: `getSystemPromptContribution()` method from `AiController` interface
  - System prompts are no longer aggregated from controllers
  - This was confusing - users couldn't see what the final prompt would be
- **Updated**: `AiOrchestrator` now accepts system prompt as a creation parameter
  - New signature: `AiOrchestrator.builder(provider, systemPrompt)`
  - Original signature still works: `AiOrchestrator.builder(provider)` (no system prompt)
  - System prompt is stored as a final field in the orchestrator
- **Added**: Static helper methods for built-in controllers
  - `ChartAiController.getSystemPrompt()` - returns recommended prompt text
  - Users explicitly compose system prompts: `"You are helpful. " + ChartAiController.getSystemPrompt()`
- **Updated**: All demo views to use new pattern

**Benefits**:

- **Explicit over implicit**: Users see exactly what system prompt is being used
- **Better composability**: Easy to combine prompts from multiple sources
- **Simpler API**: Controllers only provide tools, not hidden prompt modifications
- **Clearer debugging**: System prompt is visible at orchestrator creation, not hidden in controllers

**Migration**:

```java
// Old way (no longer supported)
public class MyController implements AiController {
    @Override
    public String getSystemPromptContribution() {
        return "You have access to...";
    }
}

// New way
public class MyController implements AiController {
    public static String getSystemPrompt() {
        return "You have access to...";
    }
}

// Usage
String systemPrompt = "You are helpful. " + MyController.getSystemPrompt();
AiOrchestrator.builder(provider, systemPrompt)
    .withController(new MyController())
    .build();
```

### 2025-12-02: Orchestrator Consolidation

**Goal**: Simplify the architecture by removing redundancy and making `AiOrchestrator` the single entry point for chat orchestration.

**Changes**:

- **Removed**: `AiChatOrchestrator` class from `vaadin-ai-chat-flow` module
  - Was a thin wrapper that only provided a builder around `BaseAiOrchestrator`
  - All functionality moved to the base class
- **Renamed**: `BaseAiOrchestrator` → `AiOrchestrator`
  - Changed from abstract class to concrete class
  - Made constructor private, added public `create()` factory method
  - Simplified builder from generic `BaseBuilder<T, B>` to concrete `Builder`
  - Updated all references in demo views and plugins
- **Moved**: Test class from `vaadin-ai-chat-flow` to `vaadin-ai-flow`
  - `AiChatOrchestratorTest` → `AiOrchestratorTest`
  - Updated package from `com.vaadin.flow.component.ai.chat` to `com.vaadin.flow.component.ai.orchestrator`
- **Module simplification**: `vaadin-ai-chat-flow` module no longer needed
  - Core orchestrator now lives in `vaadin-ai-flow` alongside interfaces
  - Chart functionality remains as a controller in `vaadin-ai-chart-flow`

**Benefits**:

- Single, clear entry point: `AiOrchestrator.builder(provider).build()`
- No confusion between base class and concrete implementation
- Cleaner module structure - core orchestrator in core module
- Controller pattern more prominent (chart functionality as controller)

**API Impact**: Minimal - users now use `AiOrchestrator` instead of `AiChatOrchestrator`, but the builder API remains identical.

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

**Phase 2 - Orchestrator Integration** (Removed 2025-12-02):

- State management functionality removed from AiController interface
- Controllers simplified to only provide tools via `getTools()` method
- Lifecycle methods (`onAttached`, `onDetached`) removed
- Persistence methods (`captureState`, `restoreState`, `getPluginId`) removed

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

### 2025-11-21: Complete AiOrchestrator Refactoring

- Created `AiOrchestrator` abstract base class to eliminate ALL code duplication
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
  - AiOrchestrator uses all default hook implementations (no tools, no system prompt)
  - Controllers override hooks for tool support and specific behavior
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
  - AiOrchestrator reduced to minimal code (~20 lines - just constructor, builder, getter)
  - ChartAiController simplified significantly with hook method overrides
  - Eliminated ~150+ lines of duplicate code
- All existing tests pass (129 tests total: 33 + 42 + 54)
- No breaking changes to public API

---

**Last Updated**: 2026-01-07
**Vaadin Version**: 25.1-SNAPSHOT
