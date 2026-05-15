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
    void executePassesLabelsThroughOneLinePerItem() {
        var callbacks = (FormAITools.Callbacks) (id, filter, limit) -> List
                .of("Apollo #P-1", "Polaris #P-2");

        var result = FormAITools.queryFieldOptions(callbacks)
                .execute(json("{\"field\":\"f-1\",\"filter\":\"\"}"));

        Assertions.assertEquals("Apollo #P-1\nPolaris #P-2\n", result);
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
            // Return more items than the cap so truncation actually kicks in.
            var items = new java.util.ArrayList<String>(201);
            for (var i = 0; i < 201; i++) {
                items.add("item-" + i);
            }
            return items;
        };

        var result = FormAITools.queryFieldOptions(callbacks).execute(
                json("{\"field\":\"f-1\",\"filter\":\"\",\"limit\":9999}"));

        Assertions.assertEquals(200, capturedLimit.get());
        Assertions.assertTrue(result.contains("(truncated to 200 items)"),
                "Result should signal truncation, got: " + result);
        var dataLines = java.util.Arrays.stream(result.split("\n"))
                .filter(s -> !s.isEmpty()).filter(s -> !s.startsWith("("))
                .count();
        Assertions.assertEquals(200, dataLines,
                "Output must not contain more data lines than the clamped "
                        + "limit even when the callback returns more items, "
                        + "got: " + dataLines);
    }

    @Test
    void executeDoesNotClaimTruncationWhenResultsFitUnderLimit() {
        // When the LLM requests a limit above the server cap, the cap kicks in
        // — but if the callback returns far fewer items than the cap, the
        // result was not actually truncated. The "(truncated to 200 items)"
        // message should only appear when items were dropped.
        var callbacks = (FormAITools.Callbacks) (id, filter, limit) -> List
                .of("only-one");

        var result = FormAITools.queryFieldOptions(callbacks).execute(
                json("{\"field\":\"f-1\",\"filter\":\"\",\"limit\":9999}"));

        Assertions.assertFalse(result.contains("truncated"),
                "Result must not claim truncation when fewer items than the "
                        + "cap were returned, got: " + result);
    }

    @Test
    void executeSignalsEmptyResultExplicitly() {
        // An empty body is indistinguishable from a broken tool to the LLM.
        // When the callback returns zero items, the result should carry an
        // explicit "no matches" (or similar) signal rather than just "".
        var callbacks = (FormAITools.Callbacks) (id, filter, limit) -> List
                .of();

        var result = FormAITools.queryFieldOptions(callbacks)
                .execute(json("{\"field\":\"f-1\",\"filter\":\"zzz\"}"));

        Assertions.assertFalse(result.isEmpty(),
                "Empty match set must produce an explicit signal to the LLM, "
                        + "not an empty string");
    }

    @Test
    void executeOutputDelimitsLabelsContainingNewlines() {
        // Labels are emitted one-per-line. A label containing '\n' would
        // silently corrupt the format, leaving the LLM unable to recover the
        // original options. The tool escapes '\n' (and the escape char itself)
        // in labels so a naive split by '\n' yields one entry per original
        // label.
        var callbacks = (FormAITools.Callbacks) (id, filter, limit) -> List
                .of("first\nsecond", "third");

        var result = FormAITools.queryFieldOptions(callbacks)
                .execute(json("{\"field\":\"f-1\",\"filter\":\"\"}"));

        var lines = java.util.Arrays.stream(result.split("\n"))
                .filter(s -> !s.isEmpty()).toList();
        Assertions.assertEquals(List.of("first\\nsecond", "third"), lines,
                "Labels containing newlines must be escaped so the output "
                        + "format stays parseable, got: " + result);
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
    void executeDoesNotLeakRawExceptionContentToLLM() {
        // Exception messages from nested libraries can contain sensitive data
        // (JDBC URLs, file paths, upstream API bodies, tokens). The tool wraps
        // a user-supplied callback and has no control over what exceptions it
        // throws, so it must not echo ex.getMessage() (or any uncontrolled
        // exception content) into the response handed to the LLM. The catch
        // block should log server-side and return a generic error.
        var sentinel = "jdbc:postgresql://prod-db.internal:5432/secrets "
                + "TOKEN=abc123";
        var callbacks = (FormAITools.Callbacks) (id, filter, limit) -> {
            throw new IllegalStateException(sentinel);
        };

        var result = FormAITools.queryFieldOptions(callbacks)
                .execute(json("{\"field\":\"f-1\",\"filter\":\"\"}"));

        Assertions.assertTrue(result.startsWith("Error"),
                "Failures should still surface as an error to the LLM, got: "
                        + result);
        Assertions.assertFalse(result.contains(sentinel),
                "Raw exception message must not be forwarded to the LLM "
                        + "verbatim — it can leak internal details. Got: "
                        + result);
        Assertions.assertFalse(
                result.contains("jdbc:") || result.contains("TOKEN="),
                "Fragments of the exception message must not leak either, "
                        + "got: " + result);
    }

    private static JsonNode json(String text) {
        return JacksonUtils.readTree(text);
    }
}
