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

import static com.vaadin.flow.component.ai.form.FormTestSupport.executeQueryFieldOptions;
import static com.vaadin.flow.component.ai.form.FormTestSupport.findTool;
import static com.vaadin.flow.component.ai.form.FormTestSupport.idOf;
import static com.vaadin.flow.component.ai.form.FormTestSupport.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.form.FormTestFields.TestField;
import com.vaadin.flow.component.html.Div;

/**
 * Tests for {@link FormAIController}'s {@code query_field_options} tool —
 * filtering, default and clamped limits, error surfacing for missing or unknown
 * field ids, output escaping, and the static parameters schema.
 */
class QueryFieldOptionsToolTest {

    @Test
    void queryFieldOptionsReturnsRegisteredOptions() {
        // End-to-end: register fieldValueOptions on a field, then drive the
        // tool
        // the way an LLM would — call getTools().execute(...) with the field
        // id. Pins the wiring from fieldValueOptions registration through
        // ToolCallbacks to the query function.
        var field = new TestField();
        var controller = new FormAIController(new Div(field));
        controller.fieldValueOptions(ValueOptions.forField(field)
                .options(List.of("apple", "banana", "cherry")));
        controller.onRequest();

        var result = executeQueryFieldOptions(controller, field, "an", 10);

        Assertions.assertTrue(result.contains("banana"),
                "Expected the registered option matching the filter to be "
                        + "returned, got: " + result);
        Assertions.assertFalse(result.contains("Error"),
                "Tool must not error for a field that was registered with "
                        + "fieldValueOptions, got: " + result);
    }

    @Test
    void queryFieldOptionsReportsUnknownFieldId() {
        // When the LLM sends a field id the controller doesn't recognize
        // (hallucinated, stale, or for a field that was registered with
        // describeField()/ignoreField() but not fieldValueOptions), the tool
        // must surface a
        // specific 'unknown field id' error including the id itself so the
        // LLM can correlate parallel tool calls and recover.
        var registered = new TestField();
        var unregistered = new TestField();
        var controller = new FormAIController(
                new Div(registered, unregistered));
        controller.fieldValueOptions(
                ValueOptions.forField(registered).options(List.of("apple")));
        controller.onRequest();

        var resultUnknownId = executeQueryFieldOptions(controller,
                json("{\"field\":\"not-a-real-id\",\"filter\":\"\"}"));

        Assertions.assertTrue(resultUnknownId.contains("Unknown field id"),
                "Hallucinated field id should produce the unknown-id error "
                        + "message, got: " + resultUnknownId);
        Assertions.assertTrue(resultUnknownId.contains("not-a-real-id"),
                "Error message should echo the offending id so the LLM can "
                        + "correlate, got: " + resultUnknownId);

        var unregisteredId = idOf(unregistered);
        var resultFieldWithoutOptions = executeQueryFieldOptions(controller,
                unregistered, "", 10);

        Assertions.assertTrue(
                resultFieldWithoutOptions.contains("Unknown field id"),
                "Field that was never registered with fieldValueOptions should "
                        + "produce the same unknown-id error, got: "
                        + resultFieldWithoutOptions);
        Assertions.assertTrue(
                resultFieldWithoutOptions.contains(unregisteredId),
                "Error message should echo the offending id, got: "
                        + resultFieldWithoutOptions);
    }

    @Test
    void queryFieldOptionsRecoversToDefaultLimitForNonPositiveLimit() {
        // A misbehaving LLM may send limit=0 (or negative). The tool must
        // recover to the default rather than forward the bogus value to the
        // query callback. Drive end-to-end via the same path the LLM uses.
        var field = new TestField();
        var capturedLimit = new AtomicInteger();
        var controller = new FormAIController(new Div(field));
        controller.fieldValueOptions(
                ValueOptions.forField(field).options((filter, limit) -> {
                    capturedLimit.set(limit);
                    return List.of();
                }));
        controller.onRequest();

        executeQueryFieldOptions(controller, field, "", 0);

        Assertions.assertEquals(50, capturedLimit.get(),
                "Non-positive limit must be coerced to the default before "
                        + "reaching the query callback, got: "
                        + capturedLimit.get());
    }

    @Test
    void queryFieldOptionsReturnsErrorForMissingFieldArgument() {
        var controller = new FormAIController(new Div(new TestField()));

        var result = executeQueryFieldOptions(controller,
                json("{\"filter\":\"\"}"));

        Assertions.assertTrue(result.startsWith("Error"));
        Assertions.assertTrue(result.contains("field"));
    }

    @Test
    void queryFieldOptionsReturnsErrorForNullArguments() {
        var controller = new FormAIController(new Div(new TestField()));

        var result = executeQueryFieldOptions(controller, null);

        Assertions.assertTrue(result.startsWith("Error"));
    }

    @Test
    void queryFieldOptionsDefaultsFilterToEmptyAndLimitToFifty() {
        var field = new TestField();
        var capturedFilter = new AtomicReference<String>();
        var capturedLimit = new AtomicInteger();
        var controller = new FormAIController(new Div(field));
        controller.fieldValueOptions(
                ValueOptions.forField(field).options((filter, limit) -> {
                    capturedFilter.set(filter);
                    capturedLimit.set(limit);
                    return List.of();
                }));
        controller.onRequest();

        var fieldId = idOf(field);
        executeQueryFieldOptions(controller,
                json("{\"field\":\"" + fieldId + "\"}"));

        Assertions.assertEquals("", capturedFilter.get());
        Assertions.assertEquals(50, capturedLimit.get());
    }

    @Test
    void queryFieldOptionsForwardsFilterAndLimitToTheRegisteredQuery() {
        var field = new TestField();
        var capturedFilter = new AtomicReference<String>();
        var capturedLimit = new AtomicInteger();
        var controller = new FormAIController(new Div(field));
        controller.fieldValueOptions(
                ValueOptions.forField(field).options((filter, limit) -> {
                    capturedFilter.set(filter);
                    capturedLimit.set(limit);
                    return List.of();
                }));
        controller.onRequest();

        executeQueryFieldOptions(controller, field, "acme", 7);

        Assertions.assertEquals("acme", capturedFilter.get());
        Assertions.assertEquals(7, capturedLimit.get());
    }

    @Test
    void queryFieldOptionsEmitsOneLinePerLabel() {
        var field = new TestField();
        var controller = new FormAIController(new Div(field));
        controller.fieldValueOptions(ValueOptions.forField(field)
                .options(List.of("Apollo #P-1", "Polaris #P-2")));
        controller.onRequest();

        var result = executeQueryFieldOptions(controller, field, "", 50);

        Assertions.assertEquals("Apollo #P-1\nPolaris #P-2\n", result);
    }

    @Test
    void queryFieldOptionsClampsLimitToTwoHundred() {
        var field = new TestField();
        var capturedLimit = new AtomicInteger();
        var controller = new FormAIController(new Div(field));
        controller.fieldValueOptions(
                ValueOptions.forField(field).options((filter, limit) -> {
                    capturedLimit.set(limit);
                    // Return more items than the cap so truncation kicks in.
                    var items = new ArrayList<String>(201);
                    for (var i = 0; i < 201; i++) {
                        items.add("item-" + i);
                    }
                    return items;
                }));
        controller.onRequest();

        var result = executeQueryFieldOptions(controller, field, "", 9999);

        Assertions.assertEquals(200, capturedLimit.get());
        Assertions.assertTrue(result.contains("(truncated to 200 items)"),
                "Result should signal truncation, got: " + result);
        var dataLines = Arrays.stream(result.split("\n"))
                .filter(s -> !s.isEmpty()).filter(s -> !s.startsWith("("))
                .count();
        Assertions.assertEquals(200, dataLines,
                "Output must not contain more data lines than the clamped "
                        + "limit even when the callback returns more items, "
                        + "got: " + dataLines);
    }

    @Test
    void queryFieldOptionsDoesNotClaimTruncationWhenResultsFitUnderLimit() {
        // When the LLM requests a limit above the server cap, the cap kicks
        // in — but if the callback returns far fewer items than the cap, the
        // result was not actually truncated. The "(truncated to ... items)"
        // message should only appear when items were dropped.
        var field = new TestField();
        var controller = new FormAIController(new Div(field));
        controller.fieldValueOptions(ValueOptions.forField(field)
                .options((filter, limit) -> List.of("only-one")));
        controller.onRequest();

        var result = executeQueryFieldOptions(controller, field, "", 9999);

        Assertions.assertFalse(result.contains("truncated"),
                "Result must not claim truncation when fewer items than the "
                        + "cap were returned, got: " + result);
    }

    @Test
    void queryFieldOptionsSignalsEmptyResultExplicitly() {
        // An empty body is indistinguishable from a broken tool to the LLM.
        // When the query returns zero items, the result should carry an
        // explicit signal rather than just "".
        var field = new TestField();
        var controller = new FormAIController(new Div(field));
        controller.fieldValueOptions(ValueOptions.forField(field)
                .options((filter, limit) -> List.of()));
        controller.onRequest();

        var result = executeQueryFieldOptions(controller, field, "zzz", 10);

        Assertions.assertFalse(result.isEmpty(),
                "Empty match set must produce an explicit signal to the "
                        + "LLM, not an empty string");
    }

    @Test
    void queryFieldOptionsEscapesNewlinesInLabels() {
        // Labels are emitted one-per-line. A label containing '\n' would
        // silently corrupt the format, leaving the LLM unable to recover
        // the original options. The tool escapes '\n' (and the escape char
        // itself) in labels so a naive split by '\n' yields one entry per
        // original label.
        var field = new TestField();
        var controller = new FormAIController(new Div(field));
        controller.fieldValueOptions(ValueOptions.forField(field)
                .options((filter, limit) -> List.of("first\nsecond", "third")));
        controller.onRequest();

        var result = executeQueryFieldOptions(controller, field, "", 10);

        var lines = Arrays.stream(result.split("\n")).filter(s -> !s.isEmpty())
                .toList();
        Assertions.assertEquals(List.of("first\\nsecond", "third"), lines,
                "Labels containing newlines must be escaped so the output "
                        + "format stays parseable, got: " + result);
    }

    @Test
    void queryFieldOptionsDoesNotLeakRawExceptionContent() {
        // Exception messages from a user-supplied query callback can
        // contain sensitive data (JDBC URLs, file paths, upstream API
        // bodies, tokens). The tool must not echo ex.getMessage() (or any
        // uncontrolled exception content) into the response handed to the
        // LLM.
        var sentinel = "jdbc:postgresql://prod-db.internal:5432/secrets "
                + "TOKEN=abc123";
        var field = new TestField();
        var controller = new FormAIController(new Div(field));
        controller.fieldValueOptions(
                ValueOptions.forField(field).options((filter, limit) -> {
                    throw new IllegalStateException(sentinel);
                }));
        controller.onRequest();

        var result = executeQueryFieldOptions(controller, field, "", 10);

        Assertions.assertTrue(result.startsWith("Error"),
                "Failures should surface as an error to the LLM, got: "
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

    @Test
    void queryFieldOptionsSchemaIsStatic() {
        // The parameters schema is built once and does not enumerate field
        // ids — clients can cache it across requests.
        var controller = new FormAIController(new Div(new TestField()));
        var first = findTool(controller.getTools(), "query_field_options")
                .getParametersSchema();
        var second = findTool(controller.getTools(), "query_field_options")
                .getParametersSchema();
        Assertions.assertEquals(first, second);

        var schema = json(first);
        Assertions.assertTrue(
                schema.path("properties").path("field").path("enum")
                        .isMissingNode(),
                "Static schema should not encode field ids as an enum");
    }
}
