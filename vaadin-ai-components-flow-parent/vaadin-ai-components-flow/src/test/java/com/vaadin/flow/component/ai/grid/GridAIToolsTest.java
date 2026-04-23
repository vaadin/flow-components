/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.ai.grid;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;

class GridAIToolsTest {

    private static JsonNode json(String json) {
        return JacksonUtils.readTree(json);
    }

    // --- Stub callbacks ---

    private static GridAITools.Callbacks singleGridCallbacks(
            String currentQuery, AtomicReference<String> updated) {
        return new GridAITools.Callbacks() {
            @Override
            public String getState(String gridId) {
                if (currentQuery == null) {
                    return "{\"gridId\":\"" + gridId
                            + "\",\"status\":\"empty\"}";
                }
                return "{\"gridId\":\"" + gridId + "\",\"query\":\""
                        + currentQuery.replace("\"", "\\\"") + "\"}";
            }

            @Override
            public void updateData(String gridId, String query) {
                if (updated != null) {
                    updated.set(query);
                }
            }

            @Override
            public Set<String> getGridIds() {
                return Set.of("grid");
            }
        };
    }

    private static GridAITools.Callbacks multiGridCallbacks() {
        return new GridAITools.Callbacks() {
            @Override
            public String getState(String gridId) {
                return "{\"gridId\":\"" + gridId + "\",\"status\":\"empty\"}";
            }

            @Override
            public void updateData(String gridId, String query) {
            }

            @Override
            public Set<String> getGridIds() {
                return Set.of("grid1", "grid2");
            }
        };
    }

    // --- getGridState ---

    @Test
    void getGridState_noQuery_returnsEmpty() {
        var tool = GridAITools.getGridState(singleGridCallbacks(null, null));
        var result = tool.execute(json("{}"));
        Assertions.assertTrue(result.contains("empty"));
    }

    @Test
    void getGridState_withQuery_returnsQuery() {
        var tool = GridAITools
                .getGridState(singleGridCallbacks("SELECT * FROM t", null));
        var result = tool.execute(json("{}"));
        Assertions.assertTrue(result.contains("SELECT * FROM t"));
    }

    @Test
    void getGridState_name() {
        var tool = GridAITools.getGridState(singleGridCallbacks(null, null));
        Assertions.assertEquals("get_grid_state", tool.getName());
    }

    @Test
    void getGridState_hasGridIdParameter() {
        var tool = GridAITools.getGridState(singleGridCallbacks(null, null));
        Assertions.assertNotNull(tool.getParametersSchema());
        Assertions.assertTrue(tool.getParametersSchema().contains("gridId"));
    }

    @Test
    void getGridState_singleGrid_autoResolvesId() {
        var tool = GridAITools.getGridState(singleGridCallbacks(null, null));
        // No gridId in arguments — auto-resolves to the single grid
        var result = tool.execute(json("{}"));
        Assertions.assertTrue(result.contains("grid"));
    }

    @Test
    void getGridState_multipleGrids_requiresId() {
        var tool = GridAITools.getGridState(multiGridCallbacks());
        var result = tool.execute(json("{}"));
        Assertions.assertTrue(result.contains("Error"));
        Assertions.assertTrue(result.contains("gridId is required"));
    }

    @Test
    void getGridState_multipleGrids_withId_works() {
        var tool = GridAITools.getGridState(multiGridCallbacks());
        var result = tool.execute(json("{\"gridId\": \"grid1\"}"));
        Assertions.assertTrue(result.contains("grid1"));
    }

    @Test
    void getGridState_nullCallbacks_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> GridAITools.getGridState(null));
    }

    @Test
    void getGridState_handlerThrows_doesNotLeakExceptionMessage() {
        // Exception messages can carry sensitive internal detail (SQL
        // fragments, schema names, file paths, credentials). The tool
        // result is fed to the LLM, which a user can prompt to repeat
        // it verbatim — so raw exception messages must not be included.
        var tool = GridAITools.getGridState(new GridAITools.Callbacks() {
            @Override
            public String getState(String gridId) {
                throw new RuntimeException("SENSITIVE_INTERNAL_DETAIL_XYZ");
            }

            @Override
            public void updateData(String gridId, String query) {
                throw new UnsupportedOperationException(
                        "updateData not expected in getState leak test");
            }

            @Override
            public Set<String> getGridIds() {
                return Set.of("grid");
            }
        });
        var result = tool.execute(json("{}"));
        Assertions.assertFalse(result.contains("SENSITIVE_INTERNAL_DETAIL_XYZ"),
                "Tool result leaks exception message to LLM: " + result);
    }

    // --- updateGridData ---

    @Test
    void updateGridData_validQuery_handled() {
        var updated = new AtomicReference<String>();
        var tool = GridAITools
                .updateGridData(singleGridCallbacks(null, updated));
        var result = tool.execute(json("{\"query\": \"SELECT 1\"}"));
        Assertions.assertTrue(result.contains("queued successfully"));
        Assertions.assertEquals("SELECT 1", updated.get());
    }

    @Test
    void updateGridData_handlerThrows_returnsError() {
        var tool = GridAITools.updateGridData(new GridAITools.Callbacks() {
            @Override
            public String getState(String gridId) {
                return "{}";
            }

            @Override
            public void updateData(String gridId, String query) {
                throw new RuntimeException("bad query");
            }

            @Override
            public Set<String> getGridIds() {
                return Set.of("grid");
            }
        });
        var result = tool.execute(json("{\"query\": \"BAD\"}"));
        Assertions.assertTrue(result.contains("Error"));
    }

    @Test
    void updateGridData_handlerThrows_doesNotLeakExceptionMessage() {
        var tool = GridAITools.updateGridData(new GridAITools.Callbacks() {
            @Override
            public String getState(String gridId) {
                return "{}";
            }

            @Override
            public void updateData(String gridId, String query) {
                throw new RuntimeException("SENSITIVE_INTERNAL_DETAIL_XYZ");
            }

            @Override
            public Set<String> getGridIds() {
                return Set.of("grid");
            }
        });
        var result = tool.execute(json("{\"query\": \"BAD\"}"));
        Assertions.assertFalse(result.contains("SENSITIVE_INTERNAL_DETAIL_XYZ"),
                "Tool result leaks exception message to LLM: " + result);
    }

    @Test
    void updateGridData_missingQuery_returnsError() {
        var tool = GridAITools.updateGridData(singleGridCallbacks(null, null));
        var result = tool.execute(json("{}"));
        Assertions.assertTrue(result.contains("Error"));
    }

    @Test
    void updateGridData_name() {
        var tool = GridAITools.updateGridData(singleGridCallbacks(null, null));
        Assertions.assertEquals("update_grid_data", tool.getName());
    }

    @Test
    void updateGridData_hasParameterSchema() {
        var tool = GridAITools.updateGridData(singleGridCallbacks(null, null));
        Assertions.assertNotNull(tool.getParametersSchema());
        Assertions.assertTrue(tool.getParametersSchema().contains("query"));
        Assertions.assertTrue(tool.getParametersSchema().contains("gridId"));
    }

    @Test
    void updateGridData_multipleGrids_withId_works() {
        var updated = new AtomicReference<String>();
        var tool = GridAITools.updateGridData(new GridAITools.Callbacks() {
            @Override
            public String getState(String gridId) {
                return "{}";
            }

            @Override
            public void updateData(String gridId, String query) {
                updated.set(gridId + ":" + query);
            }

            @Override
            public Set<String> getGridIds() {
                return Set.of("g1", "g2");
            }
        });
        var result = tool
                .execute(json("{\"gridId\": \"g1\", \"query\": \"SELECT 1\"}"));
        Assertions.assertTrue(result.contains("queued successfully"));
        Assertions.assertEquals("g1:SELECT 1", updated.get());
    }

    @Test
    void updateGridData_nullCallbacks_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> GridAITools.updateGridData(null));
    }

    // --- createAll ---

    @Test
    void createAll_returnsCorrectTools() {
        var tools = GridAITools.createAll(singleGridCallbacks(null, null));
        Assertions.assertEquals(2, tools.size());
        var names = tools.stream().map(LLMProvider.ToolSpec::getName).toList();
        Assertions.assertTrue(names.contains("get_grid_state"));
        Assertions.assertTrue(names.contains("update_grid_data"));
    }

    @Test
    void createAll_nullCallbacks_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> GridAITools.createAll(null));
    }
}
