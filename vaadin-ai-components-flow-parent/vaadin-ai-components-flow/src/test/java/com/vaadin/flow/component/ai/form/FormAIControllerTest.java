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
    class FieldLocking {

        @Test
        void onRequestStartLocksAllDiscoveredFields() {
            var a = new TestField();
            var b = new TestField();
            var nested = new TestField();
            var form = new Div(a, new Div(b, nested));
            var controller = new FormAIController(form);

            controller.onRequestStart();

            Assertions.assertTrue(a.isReadOnly());
            Assertions.assertTrue(b.isReadOnly());
            Assertions.assertTrue(nested.isReadOnly());
        }

        @Test
        void onResponseCompleteReleasesLockedFields() {
            var a = new TestField();
            var b = new TestField();
            var controller = new FormAIController(new Div(a, b));

            controller.onRequestStart();
            controller.onResponseComplete();

            Assertions.assertFalse(a.isReadOnly());
            Assertions.assertFalse(b.isReadOnly());
        }

        @Test
        void onResponseFailedReleasesLockedFields() {
            var a = new TestField();
            var b = new TestField();
            var controller = new FormAIController(new Div(a, b));

            controller.onRequestStart();
            controller.onResponseFailed(new RuntimeException("boom"));

            Assertions.assertFalse(a.isReadOnly());
            Assertions.assertFalse(b.isReadOnly());
        }

        @Test
        void ignoredFieldsAreNotLocked() {
            var visible = new TestField();
            var hidden = new TestField();
            var controller = new FormAIController(new Div(visible, hidden));
            controller.ignore(hidden);

            controller.onRequestStart();

            Assertions.assertTrue(visible.isReadOnly());
            Assertions.assertFalse(hidden.isReadOnly(),
                    "Ignored fields must not be locked during a fill");
        }

        @Test
        void preexistingReadOnlyFieldsStayReadOnlyAfterRelease() {
            // A field the application put into read-only state before the
            // turn started must remain read-only after the turn ends —
            // unlocking should only revert fields the controller itself
            // locked.
            var editable = new TestField();
            var preReadOnly = new TestField();
            preReadOnly.setReadOnly(true);
            var controller = new FormAIController(
                    new Div(editable, preReadOnly));

            controller.onRequestStart();
            Assertions.assertTrue(editable.isReadOnly());
            Assertions.assertTrue(preReadOnly.isReadOnly());

            controller.onResponseComplete();
            Assertions.assertFalse(editable.isReadOnly());
            Assertions.assertTrue(preReadOnly.isReadOnly(),
                    "A field that was already read-only before the fill "
                            + "must remain read-only after the fill ends");
        }

        @Test
        void describedFieldIsLocked() {
            // describe() registers a hint but does not ignore the field;
            // the controller must distinguish "has a hint entry" from "is
            // ignored". If they collapse, every described or
            // valueOptions-bound field would silently escape locking.
            var described = new TestField();
            var controller = new FormAIController(new Div(described));
            controller.describe(described, "the merchant name");

            controller.onRequestStart();

            Assertions.assertTrue(described.isReadOnly(),
                    "A field with a description hint but no ignore() call "
                            + "must still be locked during a fill");
        }

        @Test
        void appReadOnlyBetweenTurnsSurvivesNextRelease() {
            // The application may legitimately switch a field to
            // read-only between turns. The next turn's unlock must only
            // release fields locked by *that* turn — leftover tracking
            // from a previous turn would clobber the app's state.
            var field = new TestField();
            var controller = new FormAIController(new Div(field));

            controller.onRequestStart();
            controller.onResponseComplete();

            field.setReadOnly(true);

            controller.onRequestStart();
            controller.onResponseComplete();

            Assertions.assertTrue(field.isReadOnly(),
                    "A field the application set read-only between turns "
                            + "must stay read-only after a subsequent "
                            + "fill releases its own locks");
        }

        @Test
        void fieldAddedBetweenTurnsIsLockedOnNextRequest() {
            var initial = new TestField();
            var form = new Div(initial);
            var controller = new FormAIController(form);

            controller.onRequestStart();
            controller.onResponseComplete();

            var added = new TestField();
            form.add(added);

            controller.onRequestStart();

            Assertions.assertTrue(initial.isReadOnly());
            Assertions.assertTrue(added.isReadOnly(),
                    "Fields added between turns must be locked on the "
                            + "next request");
        }

        @Test
        void fieldIgnoredBetweenTurnsIsNotLockedOnNextRequest() {
            // The application may flag a field as ignored after the
            // controller has been wired up — e.g., a feature toggle
            // hides PII from the AI. The next turn must respect that.
            // Today this works only because discovery re-evaluates each
            // request; if anyone caches the active set "for performance",
            // this regression slips through.
            var field = new TestField();
            var controller = new FormAIController(new Div(field));

            controller.onRequestStart();
            controller.onResponseComplete();

            controller.ignore(field);
            controller.onRequestStart();

            Assertions.assertFalse(field.isReadOnly(),
                    "A field ignored after a previous turn must not be "
                            + "locked by the next turn");
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
        }
    }
}
