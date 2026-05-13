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
package com.vaadin.flow.component.ai.form;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;

class FormAIToolsTest {

    @Test
    void createAll_alwaysIncludesQueryFieldOptions() {
        var callbacks = (FormAITools.Callbacks) (id, filter, limit) -> List
                .of();
        var names = FormAITools.createAll(callbacks).stream()
                .map(LLMProvider.ToolSpec::getName).toList();
        Assertions.assertEquals(List.of("query_field_options"), names);
    }

    @Test
    void createAll_rejectsNullCallbacks() {
        Assertions.assertThrows(NullPointerException.class,
                () -> FormAITools.createAll(null));
    }

    @Test
    void schemaIsStaticAcrossCalls() {
        var callbacks = (FormAITools.Callbacks) (id, filter, limit) -> List
                .of();
        var first = FormAITools.queryFieldOptions(callbacks)
                .getParametersSchema();
        var second = FormAITools.queryFieldOptions(callbacks)
                .getParametersSchema();
        Assertions.assertEquals(first, second);

        var schema = json(first);
        Assertions.assertTrue(
                schema.path("properties").path("field").path("enum")
                        .isMissingNode(),
                "Static schema should not encode field ids as an enum");
    }

    @Test
    void executePassesFieldFilterAndLimitToCallbacks() {
        var capturedField = new AtomicReference<String>();
        var capturedFilter = new AtomicReference<String>();
        var capturedLimit = new AtomicInteger();
        var callbacks = (FormAITools.Callbacks) (id, filter, limit) -> {
            capturedField.set(id);
            capturedFilter.set(filter);
            capturedLimit.set(limit);
            return List.of();
        };

        FormAITools.queryFieldOptions(callbacks).execute(
                json("{\"field\":\"f-1\",\"filter\":\"acme\",\"limit\":7}"));

        Assertions.assertEquals("f-1", capturedField.get());
        Assertions.assertEquals("acme", capturedFilter.get());
        Assertions.assertEquals(7, capturedLimit.get());
    }

    @Test
    void executeRendersItemsViaToString() {
        record Project(String code, String name) {
            @Override
            public String toString() {
                return name + " #" + code;
            }
        }
        var p1 = new Project("P-1", "Apollo");
        var p2 = new Project("P-2", "Polaris");
        var callbacks = (FormAITools.Callbacks) (id, filter, limit) -> List
                .of(p1, p2);

        var result = FormAITools.queryFieldOptions(callbacks)
                .execute(json("{\"field\":\"f-1\",\"filter\":\"\"}"));

        Assertions.assertTrue(result.contains("Apollo #P-1"),
                "Result should include the first item, got: " + result);
        Assertions.assertTrue(result.contains("Polaris #P-2"),
                "Result should include the second item, got: " + result);
    }

    @Test
    void executeDefaultsFilterToEmptyAndLimitToFifty() {
        var capturedFilter = new AtomicReference<String>();
        var capturedLimit = new AtomicInteger();
        var callbacks = (FormAITools.Callbacks) (id, filter, limit) -> {
            capturedFilter.set(filter);
            capturedLimit.set(limit);
            return List.of();
        };

        FormAITools.queryFieldOptions(callbacks)
                .execute(json("{\"field\":\"f-1\"}"));

        Assertions.assertEquals("", capturedFilter.get());
        Assertions.assertEquals(50, capturedLimit.get());
    }

    @Test
    void executeClampsLimitToTwoHundred() {
        var capturedLimit = new AtomicInteger();
        var callbacks = (FormAITools.Callbacks) (id, filter, limit) -> {
            capturedLimit.set(limit);
            return List.of();
        };

        var result = FormAITools.queryFieldOptions(callbacks).execute(
                json("{\"field\":\"f-1\",\"filter\":\"\",\"limit\":9999}"));

        Assertions.assertEquals(200, capturedLimit.get());
        Assertions.assertTrue(result.contains("(truncated to 200 items)"),
                "Result should signal truncation, got: " + result);
    }

    @Test
    void executeReturnsErrorForMissingFieldArgument() {
        var callbacks = (FormAITools.Callbacks) (id, filter, limit) -> List
                .of();

        var result = FormAITools.queryFieldOptions(callbacks)
                .execute(json("{\"filter\":\"\"}"));

        Assertions.assertTrue(result.startsWith("Error"));
        Assertions.assertTrue(result.contains("field"));
    }

    @Test
    void executeReturnsErrorForNullArguments() {
        var callbacks = (FormAITools.Callbacks) (id, filter, limit) -> List
                .of();

        var result = FormAITools.queryFieldOptions(callbacks).execute(null);

        Assertions.assertTrue(result.startsWith("Error"));
    }

    @Test
    void executeReportsCallbackException() {
        var callbacks = (FormAITools.Callbacks) (id, filter, limit) -> {
            throw new IllegalStateException("backend down");
        };

        var result = FormAITools.queryFieldOptions(callbacks)
                .execute(json("{\"field\":\"f-1\",\"filter\":\"\"}"));

        Assertions.assertTrue(result.startsWith("Error"));
        Assertions.assertTrue(result.contains("backend down"));
    }

    private static JsonNode json(String text) {
        return JacksonUtils.readTree(text);
    }
}
