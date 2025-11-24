/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.ai.orchestrator;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;

import static org.junit.Assert.*;

/**
 * Tests for tool annotation feature in BaseAiOrchestrator.
 */
public class ToolAnnotationTest {

    private UI ui;
    private LLMProvider mockProvider;

    @Before
    public void setUp() {
        // Create a mock UI and set it as current
        ui = Mockito.mock(UI.class);
        UI.setCurrent(ui);

        // Create a mock LLM provider
        mockProvider = Mockito.mock(LLMProvider.class);
        Mockito.when(mockProvider.stream(Mockito.any()))
                .thenReturn(Flux.just("test response"));
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    /**
     * Test orchestrator that exposes createTools method for testing.
     */
    private static class TestOrchestrator extends BaseAiOrchestrator {
        public TestOrchestrator(LLMProvider provider) {
            super(provider);
        }

        @Override
        public LLMProvider.Tool[] createTools() {
            return super.createTools();
        }

        public static class Builder
                extends BaseBuilder<TestOrchestrator, Builder> {
            public Builder(LLMProvider provider) {
                super(provider);
            }

            @Override
            public TestOrchestrator build() {
                TestOrchestrator orchestrator = new TestOrchestrator(provider);
                applyCommonConfiguration(orchestrator);
                return orchestrator;
            }
        }
    }

    /**
     * Test class with tool methods.
     */
    private static class TestToolClass {
        private int callCount = 0;

        @Tool("Gets the current time")
        public String getCurrentTime() {
            callCount++;
            return "2025-11-24T10:00:00";
        }

        @Tool("Adds two numbers together")
        public String add(
                @ParameterDescription("First number") int a,
                @ParameterDescription("Second number") int b) {
            callCount++;
            return String.valueOf(a + b);
        }

        @Tool("Greets a person by name")
        public String greet(@ParameterDescription("Person's name") String name) {
            callCount++;
            return "Hello, " + name + "!";
        }

        @Tool("Gets user information")
        public String getUserInfo(
                @ParameterDescription("User ID") String userId,
                @ParameterDescription("Include details") boolean includeDetails) {
            callCount++;
            return "User: " + userId + ", details: " + includeDetails;
        }

        // Non-tool method should be ignored
        public String notATool() {
            return "This should not be a tool";
        }

        public int getCallCount() {
            return callCount;
        }
    }

    @Test
    public void testToolDiscovery() {
        TestToolClass toolObj = new TestToolClass();
        TestOrchestrator orchestrator = new TestOrchestrator.Builder(
                mockProvider).setTools(toolObj).build();

        LLMProvider.Tool[] tools = orchestrator.createTools();

        assertNotNull("Tools array should not be null", tools);
        assertEquals("Should discover 4 tool methods", 4, tools.length);

        // Check that tool names match method names
        boolean foundGetCurrentTime = false;
        boolean foundAdd = false;
        boolean foundGreet = false;
        boolean foundGetUserInfo = false;

        for (LLMProvider.Tool tool : tools) {
            String name = tool.getName();
            if (name.equals("getCurrentTime"))
                foundGetCurrentTime = true;
            if (name.equals("add"))
                foundAdd = true;
            if (name.equals("greet"))
                foundGreet = true;
            if (name.equals("getUserInfo"))
                foundGetUserInfo = true;
        }

        assertTrue("Should find getCurrentTime tool", foundGetCurrentTime);
        assertTrue("Should find add tool", foundAdd);
        assertTrue("Should find greet tool", foundGreet);
        assertTrue("Should find getUserInfo tool", foundGetUserInfo);
    }

    @Test
    public void testToolDescriptions() {
        TestToolClass toolObj = new TestToolClass();
        TestOrchestrator orchestrator = new TestOrchestrator.Builder(
                mockProvider).setTools(toolObj).build();

        LLMProvider.Tool[] tools = orchestrator.createTools();

        for (LLMProvider.Tool tool : tools) {
            if (tool.getName().equals("add")) {
                String description = tool.getDescription();
                assertTrue("Description should contain tool description",
                        description.contains("Adds two numbers together"));
                assertTrue("Description should contain parameter info",
                        description.contains("First number"));
                assertTrue("Description should contain parameter info",
                        description.contains("Second number"));
            }
        }
    }

    @Test
    public void testToolWithNoParameters() {
        TestToolClass toolObj = new TestToolClass();
        TestOrchestrator orchestrator = new TestOrchestrator.Builder(
                mockProvider).setTools(toolObj).build();

        LLMProvider.Tool[] tools = orchestrator.createTools();

        LLMProvider.Tool getCurrentTimeTool = null;
        for (LLMProvider.Tool tool : tools) {
            if (tool.getName().equals("getCurrentTime")) {
                getCurrentTimeTool = tool;
                break;
            }
        }

        assertNotNull("Should find getCurrentTime tool", getCurrentTimeTool);
        assertNull("Tool with no parameters should have null schema",
                getCurrentTimeTool.getParametersSchema());
    }

    @Test
    public void testToolWithParameters() {
        TestToolClass toolObj = new TestToolClass();
        TestOrchestrator orchestrator = new TestOrchestrator.Builder(
                mockProvider).setTools(toolObj).build();

        LLMProvider.Tool[] tools = orchestrator.createTools();

        LLMProvider.Tool addTool = null;
        for (LLMProvider.Tool tool : tools) {
            if (tool.getName().equals("add")) {
                addTool = tool;
                break;
            }
        }

        assertNotNull("Should find add tool", addTool);
        String schema = addTool.getParametersSchema();
        assertNotNull("Tool with parameters should have schema", schema);
        // Parameters will be arg0, arg1 if -parameters flag is not set
        assertTrue("Schema should contain parameters",
                (schema.contains("\"a\"") && schema.contains("\"b\""))
                        || (schema.contains("\"arg0\"")
                                && schema.contains("\"arg1\"")));
        assertTrue("Schema should contain type 'integer'",
                schema.contains("\"integer\""));
        assertTrue("Schema should contain parameter descriptions",
                schema.contains("First number"));
    }

    @Test
    public void testToolExecution() {
        TestToolClass toolObj = new TestToolClass();
        TestOrchestrator orchestrator = new TestOrchestrator.Builder(
                mockProvider).setTools(toolObj).build();

        LLMProvider.Tool[] tools = orchestrator.createTools();

        LLMProvider.Tool getCurrentTimeTool = null;
        for (LLMProvider.Tool tool : tools) {
            if (tool.getName().equals("getCurrentTime")) {
                getCurrentTimeTool = tool;
                break;
            }
        }

        assertNotNull("Should find getCurrentTime tool", getCurrentTimeTool);

        String result = getCurrentTimeTool.execute("{}");
        assertEquals("Tool should execute and return result",
                "2025-11-24T10:00:00", result);
        assertEquals("Tool method should have been called once", 1,
                toolObj.getCallCount());
    }

    @Test
    public void testToolExecutionWithIntegerParameters() {
        TestToolClass toolObj = new TestToolClass();
        TestOrchestrator orchestrator = new TestOrchestrator.Builder(
                mockProvider).setTools(toolObj).build();

        LLMProvider.Tool[] tools = orchestrator.createTools();

        LLMProvider.Tool addTool = null;
        for (LLMProvider.Tool tool : tools) {
            if (tool.getName().equals("add")) {
                addTool = tool;
                break;
            }
        }

        assertNotNull("Should find add tool", addTool);

        // Use arg0, arg1 since parameter names may not be preserved
        String result = addTool.execute("{\"arg0\": 5, \"arg1\": 3}");
        assertEquals("Tool should add numbers correctly", "8", result);
    }

    @Test
    public void testToolExecutionWithStringParameter() {
        TestToolClass toolObj = new TestToolClass();
        TestOrchestrator orchestrator = new TestOrchestrator.Builder(
                mockProvider).setTools(toolObj).build();

        LLMProvider.Tool[] tools = orchestrator.createTools();

        LLMProvider.Tool greetTool = null;
        for (LLMProvider.Tool tool : tools) {
            if (tool.getName().equals("greet")) {
                greetTool = tool;
                break;
            }
        }

        assertNotNull("Should find greet tool", greetTool);

        // Use arg0 since parameter names may not be preserved
        String result = greetTool.execute("{\"arg0\": \"Alice\"}");
        assertEquals("Tool should greet correctly", "Hello, Alice!", result);
    }

    @Test
    public void testToolExecutionWithMixedParameters() {
        TestToolClass toolObj = new TestToolClass();
        TestOrchestrator orchestrator = new TestOrchestrator.Builder(
                mockProvider).setTools(toolObj).build();

        LLMProvider.Tool[] tools = orchestrator.createTools();

        LLMProvider.Tool getUserInfoTool = null;
        for (LLMProvider.Tool tool : tools) {
            if (tool.getName().equals("getUserInfo")) {
                getUserInfoTool = tool;
                break;
            }
        }

        assertNotNull("Should find getUserInfo tool", getUserInfoTool);

        // Use arg0, arg1 since parameter names may not be preserved
        String result = getUserInfoTool
                .execute("{\"arg0\": \"123\", \"arg1\": true}");
        assertEquals("Tool should handle mixed parameters",
                "User: 123, details: true", result);
    }

    @Test
    public void testMultipleToolObjects() {
        TestToolClass toolObj1 = new TestToolClass();
        TestToolClass toolObj2 = new TestToolClass();

        TestOrchestrator orchestrator = new TestOrchestrator.Builder(
                mockProvider).setTools(toolObj1, toolObj2).build();

        LLMProvider.Tool[] tools = orchestrator.createTools();

        // Should discover tools from both objects
        assertEquals("Should discover 8 tools (4 from each object)", 8,
                tools.length);
    }

    @Test
    public void testNoToolObjects() {
        TestOrchestrator orchestrator = new TestOrchestrator.Builder(
                mockProvider).build();

        LLMProvider.Tool[] tools = orchestrator.createTools();

        assertNotNull("Tools array should not be null", tools);
        assertEquals("Should have no tools", 0, tools.length);
    }

    @Test
    public void testPrivateToolMethod() {
        class PrivateToolClass {
            @Tool("Private tool method")
            private String privateTool() {
                return "private result";
            }
        }

        PrivateToolClass toolObj = new PrivateToolClass();
        TestOrchestrator orchestrator = new TestOrchestrator.Builder(
                mockProvider).setTools(toolObj).build();

        LLMProvider.Tool[] tools = orchestrator.createTools();

        assertEquals("Should discover private tool method", 1, tools.length);

        // Test execution of private method
        String result = tools[0].execute("{}");
        assertEquals("Should execute private method", "private result", result);
    }

    @Test
    public void testJsonSchemaGeneration() {
        TestToolClass toolObj = new TestToolClass();
        TestOrchestrator orchestrator = new TestOrchestrator.Builder(
                mockProvider).setTools(toolObj).build();

        LLMProvider.Tool[] tools = orchestrator.createTools();

        LLMProvider.Tool addTool = null;
        for (LLMProvider.Tool tool : tools) {
            if (tool.getName().equals("add")) {
                addTool = tool;
                break;
            }
        }

        String schema = addTool.getParametersSchema();

        // Verify JSON schema structure
        assertTrue("Schema should be valid JSON object",
                schema.startsWith("{") && schema.endsWith("}"));
        assertTrue("Schema should have type", schema.contains("\"type\""));
        assertTrue("Schema should have properties",
                schema.contains("\"properties\""));
        assertTrue("Schema should have required",
                schema.contains("\"required\""));

        // Verify it has parameters (either with real names or arg0, arg1)
        assertTrue("Schema should contain parameters",
                schema.contains("\"arg0\"") || schema.contains("\"a\""));
    }
}
