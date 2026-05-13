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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.html.Div;

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
            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.allowedValues(null, List.of()));
            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.queryable(null, (f, l) -> List.of()));
            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.ignore(null));
        }

        @Test
        void hintMethodsRejectNullPayload() {
            var field = new TestField();
            var controller = new FormAIController(new Div(field));

            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.describe(field, null));
            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.allowedValues(field, null));
            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.queryable(field, null));
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
    }
}
