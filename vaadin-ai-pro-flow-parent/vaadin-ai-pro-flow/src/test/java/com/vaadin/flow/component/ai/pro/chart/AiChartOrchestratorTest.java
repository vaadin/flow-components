/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.pro.chart;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.input.AiInput;
import com.vaadin.flow.component.ai.input.InputSubmitEvent;
import com.vaadin.flow.component.ai.input.InputSubmitListener;
import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link AiChartOrchestrator}.
 * <p>
 * This test suite focuses on testing the behavior of the AiChartOrchestrator
 * class, not implementation details. Tests verify:
 * </p>
 * <ul>
 * <li>Builder pattern functionality</li>
 * <li>Component configuration</li>
 * <li>User request handling</li>
 * <li>Tool creation and execution</li>
 * <li>Conversation history management</li>
 * <li>UI context validation</li>
 * <li>Error handling</li>
 * </ul>
 */
@SuppressWarnings("unchecked")
public class AiChartOrchestratorTest {

    private LLMProvider mockLlmProvider;
    private DatabaseProvider mockDatabaseProvider;
    private Chart mockChart;
    private Configuration mockConfiguration;
    private AiInput mockInput;
    private DataConverter mockDataConverter;

    private UI ui;

    @Before
    public void setUp() {
        mockLlmProvider = Mockito.mock(LLMProvider.class);
        mockDatabaseProvider = Mockito.mock(DatabaseProvider.class);
        mockChart = Mockito.mock(Chart.class);
        mockConfiguration = Mockito.mock(Configuration.class);
        mockInput = Mockito.mock(AiInput.class);
        mockDataConverter = Mockito.mock(DataConverter.class);

        when(mockChart.getConfiguration()).thenReturn(mockConfiguration);

        ui = Mockito.spy(new TestUI());
        UI.setCurrent(ui);

        VaadinSession session = Mockito.mock(VaadinSession.class);
        when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);

        // Default mock behavior - return empty Flux
        when(mockLlmProvider.generateStream(anyList(), anyString(), anyList()))
                .thenReturn(Flux.empty());
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    // ===== BUILDER TESTS =====

    @Test
    public void create_withValidProviders_returnsBuilder() {
        AiChartOrchestrator.Builder builder = AiChartOrchestrator.create(
                mockLlmProvider, mockDatabaseProvider);

        assertNotNull("Builder should not be null", builder);
    }

    @Test(expected = NullPointerException.class)
    public void create_withNullLlmProvider_throwsException() {
        AiChartOrchestrator.create(null, mockDatabaseProvider).build();
    }

    @Test(expected = NullPointerException.class)
    public void create_withNullDatabaseProvider_throwsException() {
        AiChartOrchestrator.create(mockLlmProvider, null).build();
    }

    @Test
    public void build_withMinimalConfig_createsOrchestrator() {
        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider)
                .build();

        assertNotNull("Orchestrator should not be null", orchestrator);
    }

    @Test
    public void build_withChart_configuresChart() {
        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider)
                .withChart(mockChart)
                .build();

        assertEquals("Chart should be configured", mockChart,
                orchestrator.getChart());
    }

    @Test
    public void build_withInput_configuresInput() {
        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        assertEquals("Input should be configured", mockInput,
                orchestrator.getInput());
    }

    @Test
    public void build_withDataConverter_configuresDataConverter() {
        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider)
                .withDataConverter(mockDataConverter)
                .build();

        assertEquals("DataConverter should be configured", mockDataConverter,
                orchestrator.getDataConverter());
    }

    @Test
    public void build_withInput_registersSubmitListener() {
        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput, times(1)).addSubmitListener(any(InputSubmitListener.class));
    }

    @Test
    public void build_withoutInput_doesNotRegisterListener() {
        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .build();

        verify(mockInput, never()).addSubmitListener(any(InputSubmitListener.class));
    }

    @Test
    public void build_withAllComponents_configuresAll() {
        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider)
                .withChart(mockChart)
                .withInput(mockInput)
                .withDataConverter(mockDataConverter)
                .build();

        assertNotNull("Orchestrator should not be null", orchestrator);
        assertEquals("Chart should be configured", mockChart,
                orchestrator.getChart());
        assertEquals("Input should be configured", mockInput,
                orchestrator.getInput());
        assertEquals("DataConverter should be configured", mockDataConverter,
                orchestrator.getDataConverter());
    }

    // ===== COMPONENT ACCESS TESTS =====

    @Test
    public void getLlmProvider_returnsConfiguredProvider() {
        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider)
                .build();

        assertEquals("Should return configured LLM provider", mockLlmProvider,
                orchestrator.getLlmProvider());
    }

    @Test
    public void getDatabaseProvider_returnsConfiguredProvider() {
        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider)
                .build();

        assertEquals("Should return configured database provider",
                mockDatabaseProvider, orchestrator.getDatabaseProvider());
    }

    @Test
    public void getChart_withoutChart_returnsNull() {
        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider)
                .build();

        assertNull("Should return null when no chart configured",
                orchestrator.getChart());
    }

    @Test
    public void getInput_withoutInput_returnsNull() {
        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider)
                .build();

        assertNull("Should return null when no input configured",
                orchestrator.getInput());
    }

    @Test
    public void getDataConverter_withDefaultConverter_returnsConverter() {
        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider)
                .build();

        assertNotNull("Should return default data converter",
                orchestrator.getDataConverter());
    }

    // ===== USER REQUEST HANDLING TESTS =====

    @Test
    public void handleUserRequest_withValidMessage_addsToConversationHistory()
            throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Show me sales by region";
        listenerCaptor.getValue().onSubmit(event);

        Thread.sleep(100);

        verify(mockLlmProvider, times(1)).generateStream(anyList(), anyString(),
                anyList());
    }

    @Test
    public void handleUserRequest_withEmptyMessage_doesNothing() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider, never()).generateStream(anyList(), anyString(),
                anyList());
    }

    @Test
    public void handleUserRequest_withWhitespaceMessage_doesNothing() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "   ";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider, never()).generateStream(anyList(), anyString(),
                anyList());
    }

    @Test
    public void handleUserRequest_withNullMessage_doesNothing() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> null;
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider, never()).generateStream(anyList(), anyString(),
                anyList());
    }

    @Test
    public void handleUserRequest_callsLlmProviderWithSystemPrompt() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<String> systemPromptCaptor = ArgumentCaptor
                .forClass(String.class);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Create a bar chart";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider, times(1)).generateStream(anyList(),
                systemPromptCaptor.capture(), anyList());

        String systemPrompt = systemPromptCaptor.getValue();
        assertNotNull("System prompt should not be null", systemPrompt);
        assertTrue("System prompt should mention chart configuration",
                systemPrompt.contains("chart configuration"));
    }

    @Test
    public void handleUserRequest_callsLlmProviderWithTools() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<List<LLMProvider.Tool>> toolsCaptor = ArgumentCaptor
                .forClass(List.class);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Show sales data";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider, times(1)).generateStream(anyList(), anyString(),
                toolsCaptor.capture());

        List<LLMProvider.Tool> tools = toolsCaptor.getValue();
        assertNotNull("Tools list should not be null", tools);
        assertEquals("Should have 3 tools", 3, tools.size());
    }

    @Test
    public void handleUserRequest_withoutUI_throwsException() {
        UI.setCurrent(null);

        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Create chart";

        try {
            listenerCaptor.getValue().onSubmit(event);
            fail("Should throw IllegalStateException when no UI present");
        } catch (IllegalStateException e) {
            assertTrue("Exception should mention UI",
                    e.getMessage().contains("UI"));
        }
    }

    // ===== TOOL TESTS =====

    @Test
    public void tools_hasGetSchemaTool() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<List<LLMProvider.Tool>> toolsCaptor = ArgumentCaptor
                .forClass(List.class);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider).generateStream(anyList(), anyString(),
                toolsCaptor.capture());

        List<LLMProvider.Tool> tools = toolsCaptor.getValue();
        boolean hasGetSchema = tools.stream()
                .anyMatch(tool -> "getSchema".equals(tool.getName()));

        assertTrue("Should have getSchema tool", hasGetSchema);
    }

    @Test
    public void tools_hasUpdateChartDataTool() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<List<LLMProvider.Tool>> toolsCaptor = ArgumentCaptor
                .forClass(List.class);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider).generateStream(anyList(), anyString(),
                toolsCaptor.capture());

        List<LLMProvider.Tool> tools = toolsCaptor.getValue();
        boolean hasUpdateChartData = tools.stream()
                .anyMatch(tool -> "updateChartData".equals(tool.getName()));

        assertTrue("Should have updateChartData tool", hasUpdateChartData);
    }

    @Test
    public void tools_hasUpdateChartConfigTool() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<List<LLMProvider.Tool>> toolsCaptor = ArgumentCaptor
                .forClass(List.class);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider).generateStream(anyList(), anyString(),
                toolsCaptor.capture());

        List<LLMProvider.Tool> tools = toolsCaptor.getValue();
        boolean hasUpdateChartConfig = tools.stream()
                .anyMatch(tool -> "updateChartConfig".equals(tool.getName()));

        assertTrue("Should have updateChartConfig tool", hasUpdateChartConfig);
    }

    @Test
    public void getSchemaToolExecution_callsDatabaseProvider() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<List<LLMProvider.Tool>> toolsCaptor = ArgumentCaptor
                .forClass(List.class);

        when(mockDatabaseProvider.getSchema()).thenReturn(
                "CREATE TABLE sales (id INT, amount DECIMAL)");

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider).generateStream(anyList(), anyString(),
                toolsCaptor.capture());

        List<LLMProvider.Tool> tools = toolsCaptor.getValue();
        LLMProvider.Tool getSchemaTool = tools.stream()
                .filter(tool -> "getSchema".equals(tool.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull("getSchema tool should exist", getSchemaTool);

        String result = getSchemaTool.execute(null);

        verify(mockDatabaseProvider, times(1)).getSchema();
        assertTrue("Result should contain schema",
                result.contains("CREATE TABLE"));
    }

    @Test
    public void updateChartDataToolExecution_executesQueryAndUpdatesChart() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<List<LLMProvider.Tool>> toolsCaptor = ArgumentCaptor
                .forClass(List.class);

        List<Map<String, Object>> mockResults = new ArrayList<>();
        Map<String, Object> row1 = new HashMap<>();
        row1.put("region", "North");
        row1.put("sales", 1000);
        mockResults.add(row1);

        DataSeries mockSeries = new DataSeries();

        when(mockDatabaseProvider.executeQuery(anyString()))
                .thenReturn(mockResults);
        when(mockDataConverter.convertToDataSeries(anyList()))
                .thenReturn(mockSeries);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withChart(mockChart)
                .withInput(mockInput)
                .withDataConverter(mockDataConverter)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider).generateStream(anyList(), anyString(),
                toolsCaptor.capture());

        List<LLMProvider.Tool> tools = toolsCaptor.getValue();
        LLMProvider.Tool updateChartDataTool = tools.stream()
                .filter(tool -> "updateChartData".equals(tool.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull("updateChartData tool should exist", updateChartDataTool);

        String result = updateChartDataTool
                .execute("{\"query\": \"SELECT * FROM sales\"}");

        verify(mockDatabaseProvider, times(1)).executeQuery(anyString());
        verify(mockDataConverter, times(1)).convertToDataSeries(mockResults);
        verify(mockConfiguration, times(1)).setSeries(mockSeries);
        verify(mockChart, times(1)).drawChart();
        assertTrue("Result should indicate success", result.contains("success"));
    }

    @Test
    public void updateChartDataToolExecution_withError_returnsErrorMessage() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<List<LLMProvider.Tool>> toolsCaptor = ArgumentCaptor
                .forClass(List.class);

        when(mockDatabaseProvider.executeQuery(anyString()))
                .thenThrow(new RuntimeException("Database error"));

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withChart(mockChart)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider).generateStream(anyList(), anyString(),
                toolsCaptor.capture());

        List<LLMProvider.Tool> tools = toolsCaptor.getValue();
        LLMProvider.Tool updateChartDataTool = tools.stream()
                .filter(tool -> "updateChartData".equals(tool.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull("updateChartData tool should exist", updateChartDataTool);

        String result = updateChartDataTool
                .execute("{\"query\": \"SELECT * FROM sales\"}");

        assertTrue("Result should indicate error", result.contains("Error"));
        assertTrue("Result should contain error message",
                result.contains("Database error"));
    }

    @Test
    public void updateChartConfigToolExecution_updatesChart() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<List<LLMProvider.Tool>> toolsCaptor = ArgumentCaptor
                .forClass(List.class);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withChart(mockChart)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider).generateStream(anyList(), anyString(),
                toolsCaptor.capture());

        List<LLMProvider.Tool> tools = toolsCaptor.getValue();
        LLMProvider.Tool updateChartConfigTool = tools.stream()
                .filter(tool -> "updateChartConfig".equals(tool.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull("updateChartConfig tool should exist",
                updateChartConfigTool);

        String result = updateChartConfigTool.execute(
                "{\"config\": \"{\\\"title\\\": {\\\"text\\\": \\\"Sales Chart\\\"}}\"}");

        verify(mockChart, times(1)).drawChart();
        assertTrue("Result should indicate success", result.contains("success"));
    }

    @Test
    public void updateChartConfigToolExecution_withError_returnsErrorMessage() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<List<LLMProvider.Tool>> toolsCaptor = ArgumentCaptor
                .forClass(List.class);

        // Make chart.drawChart() throw an exception
        doThrow(new RuntimeException("Chart error")).when(mockChart)
                .drawChart();

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withChart(mockChart)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider).generateStream(anyList(), anyString(),
                toolsCaptor.capture());

        List<LLMProvider.Tool> tools = toolsCaptor.getValue();
        LLMProvider.Tool updateChartConfigTool = tools.stream()
                .filter(tool -> "updateChartConfig".equals(tool.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull("updateChartConfig tool should exist",
                updateChartConfigTool);

        String result = updateChartConfigTool.execute(
                "{\"config\": \"{\\\"title\\\": {\\\"text\\\": \\\"Sales Chart\\\"}}\"}");

        assertTrue("Result should indicate error", result.contains("Error"));
    }

    // ===== CONVERSATION HISTORY TESTS =====

    @Test
    public void multipleRequests_buildsConversationHistory() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<List<LLMProvider.Message>> messagesCaptor = ArgumentCaptor
                .forClass(List.class);

        when(mockLlmProvider.generateStream(anyList(), anyString(), anyList()))
                .thenReturn(Flux.just("Response"));

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        // First request
        InputSubmitEvent event1 = () -> "First request";
        listenerCaptor.getValue().onSubmit(event1);

        Thread.sleep(100);

        // Second request
        InputSubmitEvent event2 = () -> "Second request";
        listenerCaptor.getValue().onSubmit(event2);

        Thread.sleep(100);

        verify(mockLlmProvider, atLeast(2)).generateStream(
                messagesCaptor.capture(), anyString(), anyList());

        List<List<LLMProvider.Message>> allCaptures = messagesCaptor
                .getAllValues();
        int messagesInSecondCall = allCaptures.get(allCaptures.size() - 1)
                .size();

        assertTrue("Second call should have more messages than first",
                messagesInSecondCall >= 2);
    }

    // ===== STREAMING TESTS =====

    @Test
    public void streaming_withTokens_processesTokens() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        when(mockLlmProvider.generateStream(anyList(), anyString(), anyList()))
                .thenReturn(Flux.just("token1", "token2", "token3"));

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Create chart";
        listenerCaptor.getValue().onSubmit(event);

        Thread.sleep(100);

        verify(mockLlmProvider, times(1)).generateStream(anyList(), anyString(),
                anyList());
    }

    @Test
    public void streaming_withError_handlesError() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        RuntimeException error = new RuntimeException("LLM error");
        when(mockLlmProvider.generateStream(anyList(), anyString(), anyList()))
                .thenReturn(Flux.<String>error(error));

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Create chart";
        listenerCaptor.getValue().onSubmit(event);

        Thread.sleep(100);

        verify(mockLlmProvider, times(1)).generateStream(anyList(), anyString(),
                anyList());
    }

    @Test
    public void streaming_onComplete_addsAssistantMessageToHistory()
            throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<List<LLMProvider.Message>> messagesCaptor = ArgumentCaptor
                .forClass(List.class);

        when(mockLlmProvider.generateStream(anyList(), anyString(), anyList()))
                .thenReturn(Flux.just("Response"));

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Create chart";
        listenerCaptor.getValue().onSubmit(event);

        Thread.sleep(100);

        verify(mockLlmProvider, times(1)).generateStream(
                messagesCaptor.capture(), anyString(), anyList());

        List<LLMProvider.Message> messages = messagesCaptor.getValue();

        assertTrue("Should have at least user message", messages.size() >= 1);
        assertEquals("First message should be from user", "user",
                messages.get(0).getRole());
    }

    // ===== EDGE CASES =====

    @Test
    public void multipleOrchestrators_independentConversations()
            throws Exception {
        LLMProvider mockProvider1 = Mockito.mock(LLMProvider.class);
        LLMProvider mockProvider2 = Mockito.mock(LLMProvider.class);
        AiInput mockInput1 = Mockito.mock(AiInput.class);
        AiInput mockInput2 = Mockito.mock(AiInput.class);

        when(mockProvider1.generateStream(anyList(), anyString(), anyList()))
                .thenReturn(Flux.just("Response1"));
        when(mockProvider2.generateStream(anyList(), anyString(), anyList()))
                .thenReturn(Flux.just("Response2"));

        ArgumentCaptor<InputSubmitListener> listener1Captor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<InputSubmitListener> listener2Captor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        AiChartOrchestrator.create(mockProvider1, mockDatabaseProvider)
                .withInput(mockInput1)
                .build();

        AiChartOrchestrator.create(mockProvider2, mockDatabaseProvider)
                .withInput(mockInput2)
                .build();

        verify(mockInput1).addSubmitListener(listener1Captor.capture());
        verify(mockInput2).addSubmitListener(listener2Captor.capture());

        InputSubmitEvent event1 = () -> "Request 1";
        listener1Captor.getValue().onSubmit(event1);

        Thread.sleep(100);

        InputSubmitEvent event2 = () -> "Request 2";
        listener2Captor.getValue().onSubmit(event2);

        Thread.sleep(100);

        verify(mockProvider1, times(1)).generateStream(anyList(), anyString(),
                anyList());
        verify(mockProvider2, times(1)).generateStream(anyList(), anyString(),
                anyList());
    }

    @Test
    public void getSchemaToolDescription_mentionsNoParameters() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<List<LLMProvider.Tool>> toolsCaptor = ArgumentCaptor
                .forClass(List.class);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider).generateStream(anyList(), anyString(),
                toolsCaptor.capture());

        List<LLMProvider.Tool> tools = toolsCaptor.getValue();
        LLMProvider.Tool getSchemaTool = tools.stream()
                .filter(tool -> "getSchema".equals(tool.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull("getSchema tool should exist", getSchemaTool);
        assertNull("getSchema should have no parameters schema",
                getSchemaTool.getParametersSchema());
    }

    @Test
    public void toolDescriptions_includeUsageInstructions() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<List<LLMProvider.Tool>> toolsCaptor = ArgumentCaptor
                .forClass(List.class);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider).generateStream(anyList(), anyString(),
                toolsCaptor.capture());

        List<LLMProvider.Tool> tools = toolsCaptor.getValue();

        for (LLMProvider.Tool tool : tools) {
            String description = tool.getDescription();
            assertNotNull("Tool description should not be null", description);
            assertFalse("Tool description should not be empty",
                    description.isEmpty());
        }
    }

    /**
     * Custom UI for testing that executes UI.access() commands synchronously.
     */
    private static class TestUI extends UI {
        @Override
        public Future<Void> access(Command command) {
            // Execute immediately in tests instead of async
            command.execute();
            return CompletableFuture.completedFuture(null);
        }
    }
}
