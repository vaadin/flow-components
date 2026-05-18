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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;

class FormAIControllerTest {

    /**
     * Minimal {@link com.vaadin.flow.component.HasValue} component used by the
     * discovery tests. {@link AbstractField} ships with flow-server, so
     * exercising discovery does not require any concrete Vaadin input component
     * module.
     */
    @Tag("test-field")
    private static class TestField extends AbstractField<TestField, String> {
        TestField() {
            super("");
        }

        @Override
        protected void setPresentationValue(String value) {
        }
    }

    /**
     * Composite field that is both {@link com.vaadin.flow.component.HasValue}
     * and {@link HasComponents} — used to verify that discovery stops at the
     * field and does not descend into the field's internal composition.
     */
    @Tag("composite-field")
    private static class CompositeField extends
            AbstractField<CompositeField, String> implements HasComponents {
        CompositeField(Component... children) {
            super("");
            add(children);
        }

        @Override
        protected void setPresentationValue(String value) {
        }
    }

    /**
     * Integer-valued field used to exercise {@code valueOptions} with a
     * non-{@link String} value type, where {@code toValue} must convert the
     * chosen label into the field's actual value type.
     */
    @Tag("int-field")
    private static class IntField extends AbstractField<IntField, Integer> {
        IntField() {
            super(0);
        }

        @Override
        protected void setPresentationValue(Integer value) {
        }
    }

    @Nested
    class Construction {

        @Test
        void constructionWithFieldsSucceeds() {
            var form = new Div(new TestField(), new TestField());
            Assertions.assertDoesNotThrow(() -> new FormAIController(form));
        }

        @Test
        void nullFormThrows() {
            Assertions.assertThrows(NullPointerException.class,
                    () -> new FormAIController(null));
        }
    }

    @Nested
    class Traversal {

        @Test
        void deeplyNestedFieldsAreAllDiscoveredInDocumentOrder() {
            var l0 = new TestField();
            var l1 = new TestField();
            var l2 = new TestField();
            var l3 = new TestField();
            var l4 = new TestField();

            var deepest = new Div(l4);
            var deep = new Div(l3, deepest);
            var middle = new Div(l2, deep);
            var inner = new Div(l1, middle);
            var form = new Div(l0, inner);

            Assertions.assertEquals(List.of(l0, l1, l2, l3, l4),
                    FormFieldDiscovery.collectFields(form),
                    "Every nested field should appear once, in document "
                            + "order, regardless of depth");
        }

        @Test
        void siblingContainersEachContributeTheirFields() {
            var a1 = new TestField();
            var a2 = new TestField();
            var b1 = new TestField();
            var b2 = new TestField();

            var sideA = new Div(a1, a2);
            var sideB = new Div(b1, b2);
            var form = new Div(sideA, sideB);

            Assertions.assertEquals(List.of(a1, a2, b1, b2),
                    FormFieldDiscovery.collectFields(form));
        }

        @Test
        void fieldsAndContainersInterleavedAreAllDiscovered() {
            var direct1 = new TestField();
            var nested = new TestField();
            var direct2 = new TestField();

            var sub = new Div(nested);
            // Layout children: field, container, field — interleaved.
            var form = new Div(direct1, sub, direct2);

            Assertions.assertEquals(List.of(direct1, nested, direct2),
                    FormFieldDiscovery.collectFields(form));
        }

        @Test
        void emptyContainersDoNotBreakTheWalk() {
            var empty1 = new Div();
            var empty2 = new Div();
            var field = new TestField();
            var form = new Div(empty1, field, empty2);

            Assertions.assertEquals(List.of(field),
                    FormFieldDiscovery.collectFields(form));
        }

        @Test
        void compositeFieldIsTreatedAsLeafAndItsChildrenAreNotDiscovered() {
            var innerChild = new TestField();
            var composite = new CompositeField(innerChild);
            var sibling = new TestField();
            var form = new Div(composite, sibling);

            Assertions.assertEquals(List.of(composite, sibling),
                    FormFieldDiscovery.collectFields(form),
                    "A component that is both HasValue and HasComponents "
                            + "should be discovered as a single field; its "
                            + "internal children should not be exposed as "
                            + "separate form fields");
        }

        @Test
        void formWithNoFieldsProducesEmptyList() {
            var emptyChild = new Div(new Div(), new Div());
            var form = new Div(emptyChild);

            Assertions.assertEquals(List.of(),
                    FormFieldDiscovery.collectFields(form));
        }
    }

    @Nested
    class HintApi {

        @Test
        void hintMethodsRejectNullField() {
            var controller = new FormAIController(new Div());

            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.describe(null, "x"));
            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.valueOptions(null, (f, l) -> List.of(),
                            Function.identity()));
            Assertions.assertThrows(NullPointerException.class, () -> controller
                    .valueOptions(null, List.of(), Function.identity()));
            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.ignore(null));
        }

        @Test
        void fixedOptionsFilterRestrictsResultsByLabelSubstring() {
            // The Collection overload of valueOptions builds a case-
            // insensitive 'contains' filter on the supplied labels.
            var field = new TestField();
            var controller = new FormAIController(new Div(field));
            controller.valueOptions(field, List.of("apple", "banana", "cherry"),
                    Function.identity());
            controller.onRequestStart();

            Assertions.assertEquals("banana\n",
                    executeQueryFieldOptions(controller, field, "an", 10),
                    "Filter must restrict results to options containing "
                            + "the filter substring");
            Assertions.assertEquals("banana\n",
                    executeQueryFieldOptions(controller, field, "AN", 10),
                    "Filter must match labels regardless of filter case");
            Assertions.assertEquals("apple\nbanana\ncherry\n",
                    executeQueryFieldOptions(controller, field, "", 10),
                    "Empty filter must return all options up to the "
                            + "limit");
        }

        @Test
        void hintMethodsRejectNullPayload() {
            var field = new TestField();
            var controller = new FormAIController(new Div(field));

            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.describe(field, null));
            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.valueOptions(field,
                            (BiFunction<String, Integer, List<String>>) null,
                            Function.identity()));
            Assertions.assertThrows(NullPointerException.class, () -> controller
                    .valueOptions(field, (f, l) -> List.of(), null));
            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.valueOptions(field,
                            (Collection<String>) null, Function.identity()));
            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.valueOptions(field, List.of(), null));
        }

        @Test
        void valueOptionsAcceptsNonStringFieldWithToValueConverter() {
            // valueOptions is generic over the field's value type — verify
            // that an Integer-valued field can be registered with a label
            // -> Integer converter, and that the labels still flow through
            // the query tool unchanged.
            var field = new IntField();
            var controller = new FormAIController(new Div(field));
            controller.valueOptions(field, List.of("1", "2", "3"),
                    Integer::parseInt);
            controller.onRequestStart();

            Assertions.assertEquals("1\n2\n3\n",
                    executeQueryFieldOptions(controller, field, "", 10));
        }

        @Test
        void stringOverloadsUseLabelsAsValuesDirectly() {
            // The two-arg valueOptions overloads omit toValue: for String
            // fields the chosen label is the value as-is. Smoke-test both
            // shapes (query callback and fixed collection) to pin the
            // delegation to the three-arg methods.
            var queriedField = new TestField();
            var fixedField = new TestField();
            var controller = new FormAIController(
                    new Div(queriedField, fixedField));
            controller.valueOptions(queriedField,
                    (filter, limit) -> List.of("alpha", "beta"));
            controller.valueOptions(fixedField,
                    List.of("apple", "banana", "cherry"));
            controller.onRequestStart();

            Assertions.assertEquals("alpha\nbeta\n",
                    executeQueryFieldOptions(controller, queriedField, "", 10));
            Assertions.assertEquals("banana\n",
                    executeQueryFieldOptions(controller, fixedField, "an", 10));
        }
    }

    @Nested
    class GetTools {

        @Test
        void queryFieldOptionsReturnsRegisteredOptions() {
            // End-to-end: register valueOptions on a field, then drive the
            // tool the way an LLM would — call getTools().execute(...) with
            // the field id. Pins the wiring from valueOptions registration
            // through ToolCallbacks to the query function.
            var field = new TestField();
            var controller = new FormAIController(new Div(field));
            controller.valueOptions(field, List.of("apple", "banana", "cherry"),
                    Function.identity());
            controller.onRequestStart();

            var result = executeQueryFieldOptions(controller, field, "an", 10);

            Assertions.assertTrue(result.contains("banana"),
                    "Expected the registered option matching the filter to "
                            + "be returned, got: " + result);
            Assertions.assertFalse(result.contains("Error"),
                    "Tool must not error for a field that was registered "
                            + "with valueOptions, got: " + result);
        }

        @Test
        void queryFieldOptionsReportsUnknownFieldId() {
            // When the LLM sends a field id the controller doesn't recognize
            // (hallucinated, stale, or for a field that was registered with
            // describe()/ignore() but not valueOptions), the tool must
            // surface a specific 'unknown field id' error including the id
            // itself so the LLM can correlate parallel tool calls and
            // recover.
            var registered = new TestField();
            var unregistered = new TestField();
            var controller = new FormAIController(
                    new Div(registered, unregistered));
            controller.valueOptions(registered, List.of("apple"),
                    Function.identity());
            controller.onRequestStart();

            var resultUnknownId = executeQueryFieldOptions(controller,
                    json("{\"field\":\"not-a-real-id\",\"filter\":\"\"}"));

            Assertions.assertTrue(resultUnknownId.contains("Unknown field id"),
                    "Hallucinated field id should produce the unknown-id "
                            + "error message, got: " + resultUnknownId);
            Assertions.assertTrue(resultUnknownId.contains("not-a-real-id"),
                    "Error message should echo the offending id so the LLM "
                            + "can correlate, got: " + resultUnknownId);

            var unregisteredId = (String) ComponentUtil.getData(unregistered,
                    FormAIController.FIELD_ID_KEY);
            var resultFieldWithoutOptions = executeQueryFieldOptions(controller,
                    unregistered, "", 10);

            Assertions.assertTrue(
                    resultFieldWithoutOptions.contains("Unknown field id"),
                    "Field that was never registered with valueOptions "
                            + "should produce the same unknown-id error, "
                            + "got: " + resultFieldWithoutOptions);
            Assertions.assertTrue(
                    resultFieldWithoutOptions.contains(unregisteredId),
                    "Error message should echo the offending id, got: "
                            + resultFieldWithoutOptions);
        }

        @Test
        void queryFieldOptionsRecoversToDefaultLimitForNonPositiveLimit() {
            // A misbehaving LLM may send limit=0 (or negative). The tool
            // must recover to the default rather than forward the bogus
            // value to the query callback. Drive end-to-end via the same
            // path the LLM uses.
            var field = new TestField();
            var capturedLimit = new AtomicInteger();
            var controller = new FormAIController(new Div(field));
            controller.valueOptions(field, (filter, limit) -> {
                capturedLimit.set(limit);
                return List.of();
            }, Function.identity());
            controller.onRequestStart();

            executeQueryFieldOptions(controller, field, "", 0);

            Assertions.assertEquals(50, capturedLimit.get(),
                    "Non-positive limit must be coerced to the default "
                            + "before reaching the query callback, got: "
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
            controller.valueOptions(field, (filter, limit) -> {
                capturedFilter.set(filter);
                capturedLimit.set(limit);
                return List.of();
            }, Function.identity());
            controller.onRequestStart();

            var fieldId = (String) ComponentUtil.getData(field,
                    FormAIController.FIELD_ID_KEY);
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
            controller.valueOptions(field, (filter, limit) -> {
                capturedFilter.set(filter);
                capturedLimit.set(limit);
                return List.of();
            }, Function.identity());
            controller.onRequestStart();

            executeQueryFieldOptions(controller, field, "acme", 7);

            Assertions.assertEquals("acme", capturedFilter.get());
            Assertions.assertEquals(7, capturedLimit.get());
        }

        @Test
        void queryFieldOptionsEmitsOneLinePerLabel() {
            var field = new TestField();
            var controller = new FormAIController(new Div(field));
            controller.valueOptions(field,
                    List.of("Apollo #P-1", "Polaris #P-2"),
                    Function.identity());
            controller.onRequestStart();

            var result = executeQueryFieldOptions(controller, field, "", 50);

            Assertions.assertEquals("Apollo #P-1\nPolaris #P-2\n", result);
        }

        @Test
        void queryFieldOptionsClampsLimitToTwoHundred() {
            var field = new TestField();
            var capturedLimit = new AtomicInteger();
            var controller = new FormAIController(new Div(field));
            controller.valueOptions(field, (filter, limit) -> {
                capturedLimit.set(limit);
                // Return more items than the cap so truncation kicks in.
                var items = new ArrayList<String>(201);
                for (var i = 0; i < 201; i++) {
                    items.add("item-" + i);
                }
                return items;
            }, Function.identity());
            controller.onRequestStart();

            var result = executeQueryFieldOptions(controller, field, "", 9999);

            Assertions.assertEquals(200, capturedLimit.get());
            Assertions.assertTrue(result.contains("(truncated to 200 items)"),
                    "Result should signal truncation, got: " + result);
            var dataLines = Arrays.stream(result.split("\n"))
                    .filter(s -> !s.isEmpty()).filter(s -> !s.startsWith("("))
                    .count();
            Assertions.assertEquals(200, dataLines,
                    "Output must not contain more data lines than the "
                            + "clamped limit even when the callback returns "
                            + "more items, got: " + dataLines);
        }

        @Test
        void queryFieldOptionsDoesNotClaimTruncationWhenResultsFitUnderLimit() {
            // When the LLM requests a limit above the server cap, the cap
            // kicks in — but if the callback returns far fewer items than
            // the cap, the result was not actually truncated. The
            // "(truncated to ... items)" message should only appear when
            // items were dropped.
            var field = new TestField();
            var controller = new FormAIController(new Div(field));
            controller.valueOptions(field,
                    (filter, limit) -> List.of("only-one"),
                    Function.identity());
            controller.onRequestStart();

            var result = executeQueryFieldOptions(controller, field, "", 9999);

            Assertions.assertFalse(result.contains("truncated"),
                    "Result must not claim truncation when fewer items "
                            + "than the cap were returned, got: " + result);
        }

        @Test
        void queryFieldOptionsSignalsEmptyResultExplicitly() {
            // An empty body is indistinguishable from a broken tool to the
            // LLM. When the query returns zero items, the result should
            // carry an explicit signal rather than just "".
            var field = new TestField();
            var controller = new FormAIController(new Div(field));
            controller.valueOptions(field, (filter, limit) -> List.of(),
                    Function.identity());
            controller.onRequestStart();

            var result = executeQueryFieldOptions(controller, field, "zzz", 10);

            Assertions.assertFalse(result.isEmpty(),
                    "Empty match set must produce an explicit signal to "
                            + "the LLM, not an empty string");
        }

        @Test
        void queryFieldOptionsEscapesNewlinesInLabels() {
            // Labels are emitted one-per-line. A label containing '\n'
            // would silently corrupt the format, leaving the LLM unable to
            // recover the original options. The tool escapes '\n' (and the
            // escape char itself) in labels so a naive split by '\n'
            // yields one entry per original label.
            var field = new TestField();
            var controller = new FormAIController(new Div(field));
            controller.valueOptions(field,
                    (filter, limit) -> List.of("first\nsecond", "third"),
                    Function.identity());
            controller.onRequestStart();

            var result = executeQueryFieldOptions(controller, field, "", 10);

            var lines = java.util.Arrays.stream(result.split("\n"))
                    .filter(s -> !s.isEmpty()).toList();
            Assertions.assertEquals(List.of("first\\nsecond", "third"), lines,
                    "Labels containing newlines must be escaped so the "
                            + "output format stays parseable, got: " + result);
        }

        @Test
        void queryFieldOptionsDoesNotLeakRawExceptionContent() {
            // Exception messages from a user-supplied query callback can
            // contain sensitive data (JDBC URLs, file paths, upstream API
            // bodies, tokens). The tool must not echo ex.getMessage() (or
            // any uncontrolled exception content) into the response handed
            // to the LLM.
            var sentinel = "jdbc:postgresql://prod-db.internal:5432/secrets "
                    + "TOKEN=abc123";
            var field = new TestField();
            var controller = new FormAIController(new Div(field));
            controller.valueOptions(field, (filter, limit) -> {
                throw new IllegalStateException(sentinel);
            }, Function.identity());
            controller.onRequestStart();

            var result = executeQueryFieldOptions(controller, field, "", 10);

            Assertions.assertTrue(result.startsWith("Error"),
                    "Failures should surface as an error to the LLM, got: "
                            + result);
            Assertions.assertFalse(result.contains(sentinel),
                    "Raw exception message must not be forwarded to the "
                            + "LLM verbatim — it can leak internal "
                            + "details. Got: " + result);
            Assertions.assertFalse(
                    result.contains("jdbc:") || result.contains("TOKEN="),
                    "Fragments of the exception message must not leak "
                            + "either, got: " + result);
        }

        @Test
        void queryFieldOptionsSchemaIsStatic() {
            // The parameters schema is built once and does not enumerate
            // field ids — clients can cache it across requests.
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

    // --- Helpers ---

    private static String executeQueryFieldOptions(FormAIController controller,
            HasValue<?, ?> field, String filter, int limit) {
        var fieldId = (String) ComponentUtil.getData((Component) field,
                FormAIController.FIELD_ID_KEY);
        return executeQueryFieldOptions(controller,
                json("{\"field\":\"" + fieldId + "\",\"filter\":\"" + filter
                        + "\",\"limit\":" + limit + "}"));
    }

    private static String executeQueryFieldOptions(FormAIController controller,
            JsonNode arguments) {
        return findTool(controller.getTools(), "query_field_options")
                .execute(arguments);
    }

    private static LLMProvider.ToolSpec findTool(
            List<LLMProvider.ToolSpec> tools, String name) {
        return tools.stream().filter(t -> t.getName().equals(name)).findFirst()
                .orElseThrow();
    }

    private static JsonNode json(String text) {
        return JacksonUtils.readTree(text);
    }
}
