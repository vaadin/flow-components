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

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.provider.LLMProvider;

class GridAIToolsTest {

    // --- getGridState ---

    @Test
    void getGridState_noQuery_returnsEmpty() {
        var tool = GridAITools.getGridState(() -> null);
        var result = tool.execute("{}");
        Assertions.assertTrue(result.contains("empty"));
        Assertions.assertTrue(result.contains("No grid data"));
    }

    @Test
    void getGridState_withQuery_returnsQuery() {
        var tool = GridAITools.getGridState(() -> "SELECT * FROM t");
        var result = tool.execute("{}");
        Assertions.assertTrue(result.contains("SELECT * FROM t"));
    }

    @Test
    void getGridState_queryWithQuotes_escaped() {
        var tool = GridAITools.getGridState(() -> "SELECT \"name\" FROM t");
        var result = tool.execute("{}");
        Assertions.assertTrue(result.contains("\\\"name\\\""));
    }

    @Test
    void getGridState_name() {
        var tool = GridAITools.getGridState(() -> null);
        Assertions.assertEquals("getGridCurrentState", tool.getName());
    }

    @Test
    void getGridState_description_notEmpty() {
        var tool = GridAITools.getGridState(() -> null);
        Assertions.assertNotNull(tool.getDescription());
        Assertions.assertFalse(tool.getDescription().isBlank());
    }

    @Test
    void getGridState_noParameterSchema() {
        var tool = GridAITools.getGridState(() -> null);
        Assertions.assertNull(tool.getParametersSchema());
    }

    @Test
    void getGridState_nullSupplier_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> GridAITools.getGridState(null));
    }

    // --- updateGridData ---

    @Test
    void updateGridData_validQuery_accepted() {
        var accepted = new AtomicReference<String>();
        var tool = GridAITools.updateGridData(accepted::set);
        var result = tool.execute("{\"query\": \"SELECT 1\"}");
        Assertions.assertTrue(result.contains("queued successfully"));
        Assertions.assertEquals("SELECT 1", accepted.get());
    }

    @Test
    void updateGridData_handlerThrows_queryNotAccepted() {
        var accepted = new AtomicReference<String>();
        var tool = GridAITools.updateGridData(q -> {
            if (q.contains("BAD")) {
                throw new RuntimeException("invalid");
            }
            accepted.set(q);
        });
        var result = tool.execute("{\"query\": \"BAD SQL\"}");
        Assertions.assertTrue(result.contains("Error"));
        Assertions.assertTrue(result.contains("invalid"));
        Assertions.assertNull(accepted.get());
    }

    @Test
    void updateGridData_missingQuery_returnsError() {
        var tool = GridAITools.updateGridData(q -> {
        });
        var result = tool.execute("{}");
        Assertions.assertTrue(result.contains("Error"));
    }

    @Test
    void updateGridData_name() {
        var tool = GridAITools.updateGridData(q -> {
        });
        Assertions.assertEquals("updateGridData", tool.getName());
    }

    @Test
    void updateGridData_hasParameterSchema() {
        var tool = GridAITools.updateGridData(q -> {
        });
        Assertions.assertNotNull(tool.getParametersSchema());
        Assertions.assertTrue(tool.getParametersSchema().contains("query"));
        Assertions.assertTrue(tool.getParametersSchema().contains("required"));
    }

    @Test
    void updateGridData_nullHandler_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> GridAITools.updateGridData(null));
    }

    // --- createAll ---

    @Test
    void createAll_returnsCorrectTools() {
        var tools = GridAITools.createAll(() -> null, q -> {
        });
        Assertions.assertEquals(2, tools.size());
        var names = tools.stream().map(LLMProvider.ToolSpec::getName).toList();
        Assertions.assertTrue(names.contains("getGridCurrentState"));
        Assertions.assertTrue(names.contains("updateGridData"));
    }
}
