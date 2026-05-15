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
            Assertions.assertThrows(NullPointerException.class, () -> controller
                    .valueOptions(null, (f, l) -> List.of(), Function.identity()));
            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.valueOptions(null, List.of(), Function.identity()));
            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.ignore(null));
        }

        @Test
        void fixedOptionsFilterRestrictsResultsByLabelSubstring() {
            // The Collection overload of valueOptions builds a case-
            // insensitive 'contains' filter on the supplied labels. Pin the
            // behavior by capturing the BiFunction the controller passes to
            // the BiFunction overload and exercising it directly.
            var captured = new AtomicReference<BiFunction<String, Integer, List<String>>>();
            var field = new TestField();
            var controller = new FormAIController(new Div(field)) {
                @Override
                public <T> FormAIController valueOptions(HasValue<?, T> f,
                        BiFunction<String, Integer, List<String>> query,
                        Function<String, T> toValue) {
                    captured.set(query);
                    return super.valueOptions(f, query, toValue);
                }
            };
            controller.valueOptions(field, List.of("apple", "banana", "cherry"),
                    Function.identity());

            Assertions.assertEquals(List.of("banana"),
                    captured.get().apply("an", 10),
                    "Filter must restrict results to options containing the "
                            + "filter substring");
            Assertions.assertEquals(List.of("apple", "banana", "cherry"),
                    captured.get().apply("", 10),
                    "Empty filter must return all options up to the limit");
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
            Assertions.assertThrows(NullPointerException.class, () -> controller
                    .valueOptions(field, (Collection<String>) null, Function.identity()));
            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.valueOptions(field, List.of(), null));
        }
    }

    @Nested
    class GetTools {

        @Test
        void exposesQueryFieldOptionsTool() {
            var controller = new FormAIController(new Div(new TestField()));
            var names = controller.getTools().stream()
                    .map(LLMProvider.ToolSpec::getName).toList();
            Assertions.assertEquals(List.of("query_field_options"), names);
        }

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
            Assertions.assertFalse(result.startsWith("Error"),
                    "Tool must not error for a field that was registered "
                            + "with valueOptions, got: " + result);
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

        private static String executeQueryFieldOptions(
                FormAIController controller, HasValue<?, ?> field,
                String filter, int limit) {
            var fieldId = (String) ComponentUtil.getData((Component) field,
                    FormAIController.FIELD_ID_KEY);
            return findTool(controller.getTools(), "query_field_options")
                    .execute(json("{\"field\":\"" + fieldId + "\",\"filter\":\""
                            + filter + "\",\"limit\":" + limit + "}"));
        }
    }

    // --- Helpers ---

    private static LLMProvider.ToolSpec findTool(
            List<LLMProvider.ToolSpec> tools, String name) {
        return tools.stream().filter(t -> t.getName().equals(name)).findFirst()
                .orElseThrow();
    }

    private static JsonNode json(String text) {
        return JacksonUtils.readTree(text);
    }
}
