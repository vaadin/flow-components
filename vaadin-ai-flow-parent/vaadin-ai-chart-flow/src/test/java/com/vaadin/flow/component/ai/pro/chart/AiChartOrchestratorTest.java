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
        when(mockLlmProvider.stream(any()))
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
    public void getDataConverter_withDefaultConverter_returnsConverter() {
        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider)
                .build();

        assertNotNull("Should return default data converter",
                orchestrator.getDataConverter());
    }

    // ===== USER REQUEST HANDLING TESTS =====

    @Test
    public void handleUserRequest_withValidMessage_callsLlmProvider()
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

        verify(mockLlmProvider, times(1)).stream(any());
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

        verify(mockLlmProvider, never()).stream(any());
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

        verify(mockLlmProvider, never()).stream(any());
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

        verify(mockLlmProvider, never()).stream(any());
    }

    @Test
    public void handleUserRequest_callsLlmProviderWithSystemPrompt() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Create a bar chart";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider, times(1)).stream(requestCaptor.capture());

        LLMProvider.LLMRequest request = requestCaptor.getValue();
        String systemPrompt = request.systemPrompt();
        assertNotNull("System prompt should not be null", systemPrompt);
        assertTrue("System prompt should mention chart configuration",
                systemPrompt.contains("chart configuration"));
    }

    @Test
    public void handleUserRequest_callsLlmProviderWithTools() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Show sales data";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider, times(1)).stream(requestCaptor.capture());

        LLMProvider.LLMRequest request = requestCaptor.getValue();
        LLMProvider.Tool[] tools = request.tools();
        assertNotNull("Tools array should not be null", tools);
        assertEquals("Should have 3 tools", 3, tools.length);
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
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider).stream(requestCaptor.capture());

        LLMProvider.LLMRequest request = requestCaptor.getValue();
        LLMProvider.Tool[] tools = request.tools();
        boolean hasGetSchema = false;
        for (LLMProvider.Tool tool : tools) {
            if ("getSchema".equals(tool.getName())) {
                hasGetSchema = true;
                break;
            }
        }

        assertTrue("Should have getSchema tool", hasGetSchema);
    }

    @Test
    public void tools_hasUpdateChartDataTool() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider).stream(requestCaptor.capture());

        LLMProvider.LLMRequest request = requestCaptor.getValue();
        LLMProvider.Tool[] tools = request.tools();
        boolean hasUpdateChartData = false;
        for (LLMProvider.Tool tool : tools) {
            if ("updateChartData".equals(tool.getName())) {
                hasUpdateChartData = true;
                break;
            }
        }

        assertTrue("Should have updateChartData tool", hasUpdateChartData);
    }

    @Test
    public void tools_hasUpdateChartConfigTool() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider).stream(requestCaptor.capture());

        LLMProvider.LLMRequest request = requestCaptor.getValue();
        LLMProvider.Tool[] tools = request.tools();
        boolean hasUpdateChartConfig = false;
        for (LLMProvider.Tool tool : tools) {
            if ("updateChartConfig".equals(tool.getName())) {
                hasUpdateChartConfig = true;
                break;
            }
        }

        assertTrue("Should have updateChartConfig tool", hasUpdateChartConfig);
    }

    @Test
    public void getSchemaToolExecution_callsDatabaseProvider() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);

        when(mockDatabaseProvider.getSchema()).thenReturn(
                "CREATE TABLE sales (id INT, amount DECIMAL)");

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider).stream(requestCaptor.capture());

        LLMProvider.LLMRequest request = requestCaptor.getValue();
        LLMProvider.Tool[] tools = request.tools();
        LLMProvider.Tool getSchemaTool = null;
        for (LLMProvider.Tool tool : tools) {
            if ("getSchema".equals(tool.getName())) {
                getSchemaTool = tool;
                break;
            }
        }

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
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);

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

        verify(mockLlmProvider).stream(requestCaptor.capture());

        LLMProvider.LLMRequest request = requestCaptor.getValue();
        LLMProvider.Tool[] tools = request.tools();
        LLMProvider.Tool updateChartDataTool = null;
        for (LLMProvider.Tool tool : tools) {
            if ("updateChartData".equals(tool.getName())) {
                updateChartDataTool = tool;
                break;
            }
        }

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
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);

        when(mockDatabaseProvider.executeQuery(anyString()))
                .thenThrow(new RuntimeException("Database error"));

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withChart(mockChart)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider).stream(requestCaptor.capture());

        LLMProvider.LLMRequest request = requestCaptor.getValue();
        LLMProvider.Tool[] tools = request.tools();
        LLMProvider.Tool updateChartDataTool = null;
        for (LLMProvider.Tool tool : tools) {
            if ("updateChartData".equals(tool.getName())) {
                updateChartDataTool = tool;
                break;
            }
        }

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
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withChart(mockChart)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider).stream(requestCaptor.capture());

        LLMProvider.LLMRequest request = requestCaptor.getValue();
        LLMProvider.Tool[] tools = request.tools();
        LLMProvider.Tool updateChartConfigTool = null;
        for (LLMProvider.Tool tool : tools) {
            if ("updateChartConfig".equals(tool.getName())) {
                updateChartConfigTool = tool;
                break;
            }
        }

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
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);

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

        verify(mockLlmProvider).stream(requestCaptor.capture());

        LLMProvider.LLMRequest request = requestCaptor.getValue();
        LLMProvider.Tool[] tools = request.tools();
        LLMProvider.Tool updateChartConfigTool = null;
        for (LLMProvider.Tool tool : tools) {
            if ("updateChartConfig".equals(tool.getName())) {
                updateChartConfigTool = tool;
                break;
            }
        }

        assertNotNull("updateChartConfig tool should exist",
                updateChartConfigTool);

        String result = updateChartConfigTool.execute(
                "{\"config\": \"{\\\"title\\\": {\\\"text\\\": \\\"Sales Chart\\\"}}\"}");

        assertTrue("Result should indicate error", result.contains("Error"));
    }

    // ===== CONVERSATION HISTORY TESTS =====

    @Test
    public void multipleRequests_callsProviderMultipleTimes() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        when(mockLlmProvider.stream(any()))
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

        // Verify provider was called twice (conversation history managed internally by provider)
        verify(mockLlmProvider, atLeast(2)).stream(any());
    }

    // ===== STREAMING TESTS =====

    @Test
    public void streaming_withTokens_processesTokens() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        when(mockLlmProvider.stream(any()))
                .thenReturn(Flux.just("token1", "token2", "token3"));

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Create chart";
        listenerCaptor.getValue().onSubmit(event);

        Thread.sleep(100);

        verify(mockLlmProvider, times(1)).stream(any());
    }

    @Test
    public void streaming_withError_handlesError() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        RuntimeException error = new RuntimeException("LLM error");
        when(mockLlmProvider.stream(any()))
                .thenReturn(Flux.<String>error(error));

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Create chart";
        listenerCaptor.getValue().onSubmit(event);

        Thread.sleep(100);

        verify(mockLlmProvider, times(1)).stream(any());
    }

    @Test
    public void streaming_onComplete_verifyUserMessage()
            throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);

        when(mockLlmProvider.stream(any()))
                .thenReturn(Flux.just("Response"));

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Create chart";
        listenerCaptor.getValue().onSubmit(event);

        Thread.sleep(100);

        verify(mockLlmProvider, times(1)).stream(requestCaptor.capture());

        LLMProvider.LLMRequest request = requestCaptor.getValue();
        String userMessage = request.userMessage();

        assertNotNull("User message should not be null", userMessage);
        assertEquals("User message should match input", "Create chart", userMessage);
    }

    // ===== EDGE CASES =====

    @Test
    public void multipleOrchestrators_independentConversations()
            throws Exception {
        LLMProvider mockProvider1 = Mockito.mock(LLMProvider.class);
        LLMProvider mockProvider2 = Mockito.mock(LLMProvider.class);
        AiInput mockInput1 = Mockito.mock(AiInput.class);
        AiInput mockInput2 = Mockito.mock(AiInput.class);

        when(mockProvider1.stream(any()))
                .thenReturn(Flux.just("Response1"));
        when(mockProvider2.stream(any()))
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

        verify(mockProvider1, times(1)).stream(any());
        verify(mockProvider2, times(1)).stream(any());
    }

    @Test
    public void getSchemaToolDescription_mentionsNoParameters() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider).stream(requestCaptor.capture());

        LLMProvider.LLMRequest request = requestCaptor.getValue();
        LLMProvider.Tool[] tools = request.tools();
        LLMProvider.Tool getSchemaTool = null;
        for (LLMProvider.Tool tool : tools) {
            if ("getSchema".equals(tool.getName())) {
                getSchemaTool = tool;
                break;
            }
        }

        assertNotNull("getSchema tool should exist", getSchemaTool);
        assertNull("getSchema should have no parameters schema",
                getSchemaTool.getParametersSchema());
    }

    @Test
    public void toolDescriptions_includeUsageInstructions() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);

        AiChartOrchestrator.create(mockLlmProvider, mockDatabaseProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        verify(mockLlmProvider).stream(requestCaptor.capture());

        LLMProvider.LLMRequest request = requestCaptor.getValue();
        LLMProvider.Tool[] tools = request.tools();

        for (LLMProvider.Tool tool : tools) {
            String description = tool.getDescription();
            assertNotNull("Tool description should not be null", description);
            assertFalse("Tool description should not be empty",
                    description.isEmpty());
        }
    }

    @Test
    public void captureState_withNoState_returnsNull() {
        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider).build();

        ChartState state = orchestrator.captureState();

        assertNull("State should be null when no data/config has been set",
                state);
    }

    @Test
    public void captureState_afterDataUpdate_capturesSqlQuery() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        Chart chart = new Chart();
        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider).withChart(chart)
                .withInput(mockInput).build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        // Provide 2-column data as required by DefaultDataConverter
        when(mockDatabaseProvider.executeQuery(anyString()))
                .thenReturn(List.of(Map.of("category", "Q1", "value", 100)));

        // Set up UI context by triggering input
        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        // Now trigger the updateChartData tool directly
        LLMProvider.Tool[] tools = orchestrator.createTools();
        LLMProvider.Tool updateDataTool = null;
        for (LLMProvider.Tool tool : tools) {
            if ("updateChartData".equals(tool.getName())) {
                updateDataTool = tool;
                break;
            }
        }

        assertNotNull(updateDataTool);
        updateDataTool.execute("{\"query\": \"SELECT * FROM sales\"}");

        // Capture state
        ChartState state = orchestrator.captureState();

        assertNotNull("State should not be null after data update", state);
        assertEquals("SELECT * FROM sales", state.getSqlQuery());
        assertNull("Config should be null if not set", state.getChartConfig());
    }

    @Test
    public void captureState_afterConfigUpdate_capturesChartConfig() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        Chart chart = new Chart();
        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider).withChart(chart)
                .withInput(mockInput).build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        // Set up UI context by triggering input
        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        // Now trigger the updateChartConfig tool
        LLMProvider.Tool[] tools = orchestrator.createTools();
        LLMProvider.Tool updateConfigTool = null;
        for (LLMProvider.Tool tool : tools) {
            if ("updateChartConfig".equals(tool.getName())) {
                updateConfigTool = tool;
                break;
            }
        }

        assertNotNull(updateConfigTool);
        String config = "{\"title\": {\"text\": \"Sales Chart\"}}";
        updateConfigTool.execute("{\"config\": \"" + config + "\"}");

        // Capture state
        ChartState state = orchestrator.captureState();

        assertNotNull("State should not be null after config update", state);
        assertNull("SQL should be null if not set", state.getSqlQuery());
        assertEquals(config, state.getChartConfig());
    }

    @Test
    public void captureState_afterBothUpdates_capturesBoth() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        Chart chart = new Chart();
        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider).withChart(chart)
                .withInput(mockInput).build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        // Provide 2-column data as required by DefaultDataConverter
        when(mockDatabaseProvider.executeQuery(anyString()))
                .thenReturn(List.of(Map.of("month", "January", "revenue", 5000)));

        // Set up UI context by triggering input
        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        // Trigger both tools
        LLMProvider.Tool[] tools = orchestrator.createTools();

        // Update data
        for (LLMProvider.Tool tool : tools) {
            if ("updateChartData".equals(tool.getName())) {
                tool.execute("{\"query\": \"SELECT * FROM revenue\"}");
                break;
            }
        }

        // Update config
        String config = "{\"chart\": {\"type\": \"bar\"}}";
        for (LLMProvider.Tool tool : tools) {
            if ("updateChartConfig".equals(tool.getName())) {
                tool.execute("{\"config\": \"" + config + "\"}");
                break;
            }
        }

        // Capture state
        ChartState state = orchestrator.captureState();

        assertNotNull("State should not be null", state);
        assertEquals("SELECT * FROM revenue", state.getSqlQuery());
        assertEquals(config, state.getChartConfig());
    }

    @Test
    public void restoreState_withNullState_throwsException() {
        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider).build();

        try {
            orchestrator.restoreState(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            assertTrue(e.getMessage().contains("Chart state cannot be null"));
        }
    }

    @Test
    public void restoreState_withSqlQuery_executesQueryAndUpdatesChart() {
        Chart chart = new Chart();
        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider).withChart(chart)
                .build();

        when(mockDatabaseProvider.executeQuery("SELECT * FROM products"))
                .thenReturn(List.of(Map.of("product", "Widget", "sales", 100)));

        ChartState state = ChartState.of("SELECT * FROM products", null);

        orchestrator.restoreState(state);

        verify(mockDatabaseProvider).executeQuery("SELECT * FROM products");
        // Chart should be updated with new data
        assertNotNull(chart.getConfiguration().getSeries());
    }

    @Test
    public void restoreState_withChartConfig_appliesConfig() {
        Chart chart = new Chart();
        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider).withChart(chart)
                .build();

        String config = "{\"title\": {\"text\": \"Restored Chart\"}}";
        ChartState state = ChartState.of(null, config);

        orchestrator.restoreState(state);

        // Config should be applied (verified by no exceptions thrown)
        ChartState capturedState = orchestrator.captureState();
        assertNotNull(capturedState);
        assertEquals(config, capturedState.getChartConfig());
    }

    @Test
    public void restoreState_withBothSqlAndConfig_restoresBoth() {
        Chart chart = new Chart();
        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider).withChart(chart)
                .build();

        when(mockDatabaseProvider.executeQuery("SELECT * FROM orders"))
                .thenReturn(List.of(Map.of("month", "Jan", "total", 5000)));

        String config = "{\"chart\": {\"type\": \"line\"}}";
        ChartState state = ChartState.of("SELECT * FROM orders", config);

        orchestrator.restoreState(state);

        verify(mockDatabaseProvider).executeQuery("SELECT * FROM orders");

        // Verify state was captured
        ChartState capturedState = orchestrator.captureState();
        assertNotNull(capturedState);
        assertEquals("SELECT * FROM orders", capturedState.getSqlQuery());
        assertEquals(config, capturedState.getChartConfig());
    }

    // ===== STATE CHANGE EVENT TESTS =====

    @Test
    public void updateChartData_firesDataQueryUpdatedEvent() throws Exception {
        // Setup mock responses
        List<Map<String, Object>> mockResults = List.of(
                Map.of("category", "Q1", "value", 100),
                Map.of("category", "Q2", "value", 150));
        when(mockDatabaseProvider.executeQuery(anyString()))
                .thenReturn(mockResults);

        // Setup event listener
        final ChartStateChangeEvent[] capturedEvent = new ChartStateChangeEvent[1];
        ChartStateChangeListener listener = event -> {
            capturedEvent[0] = event;
        };

        // Build orchestrator
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider)
                .withChart(mockChart)
                .withDataConverter(new DefaultDataConverter())
                .withInput(mockInput)
                .build();

        orchestrator.addStateChangeListener(listener);

        // Trigger input to set up UI context
        verify(mockInput).addSubmitListener(listenerCaptor.capture());
        InputSubmitListener capturedListener = listenerCaptor.getValue();
        InputSubmitEvent mockEvent = mock(InputSubmitEvent.class);
        when(mockEvent.getValue()).thenReturn("Show sales by region");
        capturedListener.onSubmit(mockEvent);

        // Get the updateChartData tool and execute it
        verify(mockLlmProvider).stream(any());
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);
        verify(mockLlmProvider).stream(requestCaptor.capture());

        LLMProvider.Tool[] tools = requestCaptor.getValue().tools();
        LLMProvider.Tool updateChartDataTool = null;
        for (LLMProvider.Tool tool : tools) {
            if ("updateChartData".equals(tool.getName())) {
                updateChartDataTool = tool;
                break;
            }
        }

        assertNotNull("updateChartData tool should exist", updateChartDataTool);

        // Execute the tool
        updateChartDataTool.execute("{\"query\": \"SELECT * FROM sales\"}");

        // Verify event was fired
        assertNotNull("Event should have been fired", capturedEvent[0]);
        assertEquals("Event should have DATA_QUERY_UPDATED type",
                ChartStateChangeEvent.StateChangeType.DATA_QUERY_UPDATED,
                capturedEvent[0].getChangeType());
        assertNotNull("Event should have chart state",
                capturedEvent[0].getChartState());
        assertEquals("SELECT * FROM sales",
                capturedEvent[0].getChartState().getSqlQuery());
    }

    @Test
    public void updateChartConfig_firesConfigurationUpdatedEvent()
            throws Exception {
        // Setup event listener
        final ChartStateChangeEvent[] capturedEvent = new ChartStateChangeEvent[1];
        ChartStateChangeListener listener = event -> {
            capturedEvent[0] = event;
        };

        // Build orchestrator
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider)
                .withChart(mockChart)
                .withDataConverter(new DefaultDataConverter())
                .withInput(mockInput)
                .build();

        orchestrator.addStateChangeListener(listener);

        // Trigger input to set up UI context
        verify(mockInput).addSubmitListener(listenerCaptor.capture());
        InputSubmitListener capturedListener = listenerCaptor.getValue();
        InputSubmitEvent mockEvent = mock(InputSubmitEvent.class);
        when(mockEvent.getValue()).thenReturn("Make it a bar chart");
        capturedListener.onSubmit(mockEvent);

        // Get the updateChartConfig tool and execute it
        verify(mockLlmProvider).stream(any());
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);
        verify(mockLlmProvider).stream(requestCaptor.capture());

        LLMProvider.Tool[] tools = requestCaptor.getValue().tools();
        LLMProvider.Tool updateChartConfigTool = null;
        for (LLMProvider.Tool tool : tools) {
            if ("updateChartConfig".equals(tool.getName())) {
                updateChartConfigTool = tool;
                break;
            }
        }

        assertNotNull("updateChartConfig tool should exist",
                updateChartConfigTool);

        // Execute the tool
        updateChartConfigTool.execute(
                "{\"config\": \"{\\\"chart\\\": {\\\"type\\\": \\\"bar\\\"}}\"}");

        // Verify event was fired
        assertNotNull("Event should have been fired", capturedEvent[0]);
        assertEquals("Event should have CONFIGURATION_UPDATED type",
                ChartStateChangeEvent.StateChangeType.CONFIGURATION_UPDATED,
                capturedEvent[0].getChangeType());
        assertNotNull("Event should have chart state",
                capturedEvent[0].getChartState());
        assertEquals("{\\\"chart\\\": {\\\"type\\\": \\\"bar\\\"}}",
                capturedEvent[0].getChartState().getChartConfig());
    }

    @Test
    public void multipleListeners_allReceiveEvent() throws Exception {
        // Setup mock responses
        List<Map<String, Object>> mockResults = List.of(
                Map.of("category", "Q1", "value", 100));
        when(mockDatabaseProvider.executeQuery(anyString()))
                .thenReturn(mockResults);

        // Setup multiple listeners
        final int[] eventCount = { 0 };
        ChartStateChangeListener listener1 = event -> eventCount[0]++;
        ChartStateChangeListener listener2 = event -> eventCount[0]++;
        ChartStateChangeListener listener3 = event -> eventCount[0]++;

        // Build orchestrator
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider)
                .withChart(mockChart)
                .withDataConverter(new DefaultDataConverter())
                .withInput(mockInput)
                .build();

        orchestrator.addStateChangeListener(listener1);
        orchestrator.addStateChangeListener(listener2);
        orchestrator.addStateChangeListener(listener3);

        // Trigger input to set up UI context
        verify(mockInput).addSubmitListener(listenerCaptor.capture());
        InputSubmitListener capturedListener = listenerCaptor.getValue();
        InputSubmitEvent mockEvent = mock(InputSubmitEvent.class);
        when(mockEvent.getValue()).thenReturn("Show data");
        capturedListener.onSubmit(mockEvent);

        // Get and execute the updateChartData tool
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);
        verify(mockLlmProvider).stream(requestCaptor.capture());

        LLMProvider.Tool[] tools = requestCaptor.getValue().tools();
        LLMProvider.Tool updateChartDataTool = null;
        for (LLMProvider.Tool tool : tools) {
            if ("updateChartData".equals(tool.getName())) {
                updateChartDataTool = tool;
                break;
            }
        }

        updateChartDataTool.execute("{\"query\": \"SELECT 1\"}");

        // Verify all listeners were called
        assertEquals("All 3 listeners should have been called", 3,
                eventCount[0]);
    }

    @Test
    public void removeStateChangeListener_listenerNotCalled() throws Exception {
        // Setup mock responses
        List<Map<String, Object>> mockResults = List.of(
                Map.of("category", "Q1", "value", 100));
        when(mockDatabaseProvider.executeQuery(anyString()))
                .thenReturn(mockResults);

        // Setup listener
        final int[] eventCount = { 0 };
        ChartStateChangeListener listener = event -> eventCount[0]++;

        // Build orchestrator
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider)
                .withChart(mockChart)
                .withDataConverter(new DefaultDataConverter())
                .withInput(mockInput)
                .build();

        orchestrator.addStateChangeListener(listener);
        orchestrator.removeStateChangeListener(listener);

        // Trigger input to set up UI context
        verify(mockInput).addSubmitListener(listenerCaptor.capture());
        InputSubmitListener capturedListener = listenerCaptor.getValue();
        InputSubmitEvent mockEvent = mock(InputSubmitEvent.class);
        when(mockEvent.getValue()).thenReturn("Show data");
        capturedListener.onSubmit(mockEvent);

        // Get and execute the updateChartData tool
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);
        verify(mockLlmProvider).stream(requestCaptor.capture());

        LLMProvider.Tool[] tools = requestCaptor.getValue().tools();
        LLMProvider.Tool updateChartDataTool = null;
        for (LLMProvider.Tool tool : tools) {
            if ("updateChartData".equals(tool.getName())) {
                updateChartDataTool = tool;
                break;
            }
        }

        updateChartDataTool.execute("{\"query\": \"SELECT 1\"}");

        // Verify listener was not called
        assertEquals("Listener should not have been called", 0, eventCount[0]);
    }

    @Test
    public void addStateChangeListener_withNullListener_throwsException() {
        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider)
                .withChart(mockChart)
                .withDataConverter(new DefaultDataConverter())
                .build();

        try {
            orchestrator.addStateChangeListener(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            assertEquals("Listener cannot be null", e.getMessage());
        }
    }

    @Test
    public void eventSource_isOrchestrator() throws Exception {
        // Setup mock responses
        List<Map<String, Object>> mockResults = List.of(
                Map.of("category", "Q1", "value", 100));
        when(mockDatabaseProvider.executeQuery(anyString()))
                .thenReturn(mockResults);

        // Build orchestrator
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        AiChartOrchestrator orchestrator = AiChartOrchestrator
                .create(mockLlmProvider, mockDatabaseProvider)
                .withChart(mockChart)
                .withDataConverter(new DefaultDataConverter())
                .withInput(mockInput)
                .build();

        // Setup event listener
        final ChartStateChangeEvent[] capturedEvent = new ChartStateChangeEvent[1];
        orchestrator.addStateChangeListener(event -> {
            capturedEvent[0] = event;
        });

        // Trigger input to set up UI context
        verify(mockInput).addSubmitListener(listenerCaptor.capture());
        InputSubmitListener capturedListener = listenerCaptor.getValue();
        InputSubmitEvent mockEvent = mock(InputSubmitEvent.class);
        when(mockEvent.getValue()).thenReturn("Show data");
        capturedListener.onSubmit(mockEvent);

        // Get and execute the updateChartData tool
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);
        verify(mockLlmProvider).stream(requestCaptor.capture());

        LLMProvider.Tool[] tools = requestCaptor.getValue().tools();
        LLMProvider.Tool updateChartDataTool = null;
        for (LLMProvider.Tool tool : tools) {
            if ("updateChartData".equals(tool.getName())) {
                updateChartDataTool = tool;
                break;
            }
        }

        updateChartDataTool.execute("{\"query\": \"SELECT 1\"}");

        // Verify event source
        assertNotNull("Event should have been fired", capturedEvent[0]);
        assertSame("Event source should be the orchestrator", orchestrator,
                capturedEvent[0].getSource());
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
