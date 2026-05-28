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
import static com.vaadin.flow.component.ai.form.FormTestSupport.formStateFields;
import static com.vaadin.flow.component.ai.form.FormTestSupport.idOf;
import static com.vaadin.flow.component.ai.form.FormTestSupport.json;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.ai.form.FormTestFields.CompositeField;
import com.vaadin.flow.component.ai.form.FormTestFields.IntField;
import com.vaadin.flow.component.ai.form.FormTestFields.SingleSelectField;
import com.vaadin.flow.component.ai.form.FormTestFields.TestField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.shared.Registration;

/**
 * Tests covering {@link FormAIController}'s construction, container traversal,
 * field-locking lifecycle, and hint-registration API. Tool-output specifics for
 * {@code get_form_state} and {@code query_field_options} live in
 * {@link FormStateToolTest} and {@link QueryFieldOptionsToolTest} so this class
 * stays focused on controller behaviour rather than schema details.
 */
class FormAIControllerTest {

    /** {@link TestField} variant that also exposes a label. */
    @Tag("labeled-field")
    private static class LabeledField
            extends AbstractField<LabeledField, String> implements HasLabel {
        LabeledField(String label) {
            super("");
            setLabel(label);
        }

        @Override
        protected void setPresentationValue(String value) {
        }
    }

    /** Bean used by binder integration tests. */
    private static class TestBean {
        private String name;
        private String email;

        @SuppressWarnings("unused")
        public String getName() {
            return name;
        }

        @SuppressWarnings("unused")
        public void setName(String name) {
            this.name = name;
        }

        @SuppressWarnings("unused")
        public String getEmail() {
            return email;
        }

        @SuppressWarnings("unused")
        public void setEmail(String email) {
            this.email = email;
        }
    }

    @Nested
    class InstructionsTool {

        @Test
        void getToolsExposesGetFormInstructionsAsTheFirstTool() {
            // Most providers feed the tool list to the model in order. The
            // controller surfaces get_form_instructions first so the
            // workflow text is the model's first read regardless of
            // provider-side reordering.
            var controller = new FormAIController(new Div(new TestField()));

            var tools = controller.getTools();

            Assertions.assertEquals("get_form_instructions",
                    tools.get(0).getName(),
                    "First tool must be get_form_instructions; got: "
                            + tools.stream().map(t -> t.getName()).toList());
        }

        @Test
        void instructionsToolDescriptionCarriesTheFullWorkflow() {
            // The workflow lives in the description so the LLM sees it
            // just from listing tools — no extra tool call needed. Pin
            // load-bearing phrases so accidental truncation surfaces as a
            // failing assertion.
            var controller = new FormAIController(new Div(new TestField()));
            var instructions = findTool(controller.getTools(),
                    "get_form_instructions");

            var description = instructions.getDescription();

            for (var anchor : List.of("get_form_state", "fill_form",
                    "query_field_options", "queryable", "enum", "rejected",
                    ".ignore()", "SAME turn", "newly-appeared")) {
                Assertions.assertTrue(description.contains(anchor),
                        "Workflow description must mention '" + anchor
                                + "', got: " + description);
            }
        }

        @Test
        void instructionsToolExecuteReturnsTheSameText() {
            // The execute() return value is the LLM's fallback if it
            // forgot the workflow mid-turn. It must match what the
            // description advertised so the model gets a consistent
            // story.
            var controller = new FormAIController(new Div(new TestField()));
            var instructions = findTool(controller.getTools(),
                    "get_form_instructions");

            var description = instructions.getDescription();
            var execResult = instructions
                    .execute(JacksonUtils.createObjectNode());

            Assertions.assertTrue(description.endsWith(execResult),
                    "execute() output must be the trailing workflow "
                            + "block of the description so calling the "
                            + "tool returns the same text the model "
                            + "already read; description: " + description
                            + " execResult: " + execResult);
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

        @Test
        void constructionWithBinderSucceeds() {
            var form = new Div(new TestField());
            var binder = new Binder<>(TestBean.class);
            Assertions.assertDoesNotThrow(
                    () -> new FormAIController(form, binder));
        }

        @Test
        void nullFormForBinderConstructorThrows() {
            Assertions.assertThrows(NullPointerException.class,
                    () -> new FormAIController(null,
                            new Binder<>(TestBean.class)));
        }

        @Test
        void nullBinderForBinderConstructorThrows() {
            Assertions.assertThrows(NullPointerException.class,
                    () -> new FormAIController(new Div(), null));
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
        void onRequestLocksAllDiscoveredFields() {
            var a = new TestField();
            var b = new TestField();
            var nested = new TestField();
            var form = new Div(a, new Div(b, nested));
            var controller = new FormAIController(form);

            controller.onRequest();

            Assertions.assertTrue(a.isReadOnly());
            Assertions.assertTrue(b.isReadOnly());
            Assertions.assertTrue(nested.isReadOnly());
        }

        @Test
        void onResponseReleasesLockedFieldsOnSuccess() {
            var a = new TestField();
            var b = new TestField();
            var controller = new FormAIController(new Div(a, b));

            controller.onRequest();
            controller.onResponse(null);

            Assertions.assertFalse(a.isReadOnly());
            Assertions.assertFalse(b.isReadOnly());
        }

        @Test
        void onResponseReleasesLockedFieldsOnFailure() {
            var a = new TestField();
            var b = new TestField();
            var controller = new FormAIController(new Div(a, b));

            controller.onRequest();
            controller.onResponse(new RuntimeException("boom"));

            Assertions.assertFalse(a.isReadOnly());
            Assertions.assertFalse(b.isReadOnly());
        }

        @Test
        void ignoredFieldsAreNotLocked() {
            var visible = new TestField();
            var hidden = new TestField();
            var controller = new FormAIController(new Div(visible, hidden));
            controller.ignore(hidden);

            controller.onRequest();

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

            controller.onRequest();
            Assertions.assertTrue(editable.isReadOnly());
            Assertions.assertTrue(preReadOnly.isReadOnly());

            controller.onResponse(null);
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

            controller.onRequest();

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

            controller.onRequest();
            controller.onResponse(null);

            field.setReadOnly(true);

            controller.onRequest();
            controller.onResponse(null);

            Assertions.assertTrue(field.isReadOnly(),
                    "A field the application set read-only between turns "
                            + "must stay read-only after a subsequent fill "
                            + "releases its own locks");
        }

        @Test
        void fieldAddedBetweenTurnsIsLockedOnNextRequest() {
            var initial = new TestField();
            var form = new Div(initial);
            var controller = new FormAIController(form);

            controller.onRequest();
            controller.onResponse(null);

            var added = new TestField();
            form.add(added);

            controller.onRequest();

            Assertions.assertTrue(initial.isReadOnly());
            Assertions.assertTrue(added.isReadOnly(),
                    "Fields added between turns must be locked on the next "
                            + "request");
        }

        @Test
        void fieldIgnoredBetweenTurnsIsNotLockedOnNextRequest() {
            // The application may flag a field as ignored after the
            // controller has been wired up — e.g., a feature toggle hides
            // PII from the AI. The next turn must respect that. Today this
            // works only because discovery re-evaluates each request; if
            // anyone caches the active set "for performance", this
            // regression slips through.
            var field = new TestField();
            var controller = new FormAIController(new Div(field));

            controller.onRequest();
            controller.onResponse(null);

            controller.ignore(field);
            controller.onRequest();

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
            controller.onRequest();

            Assertions.assertEquals("banana\n",
                    executeQueryFieldOptions(controller, field, "an", 10),
                    "Filter must restrict results to options containing the "
                            + "filter substring");
            Assertions.assertEquals("banana\n",
                    executeQueryFieldOptions(controller, field, "AN", 10),
                    "Filter must match labels regardless of filter case");
            Assertions.assertEquals("apple\nbanana\ncherry\n",
                    executeQueryFieldOptions(controller, field, "", 10),
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
            controller.onRequest();

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
            controller.onRequest();

            Assertions.assertEquals("alpha\nbeta\n",
                    executeQueryFieldOptions(controller, queriedField, "", 10));
            Assertions.assertEquals("banana\n",
                    executeQueryFieldOptions(controller, fixedField, "an", 10));
        }

        @Test
        void reregisteringWithBiFunctionClearsPriorFixedOptionsFlag() {
            // Each valueOptions call replaces the previous registration for
            // the same field. The fixed-collection overload sets a flag
            // that makes the schema render options as 'enum'; re-
            // registering with a BiFunction must reset that flag so the
            // schema rendering matches the new registration (queryable, not
            // enum).
            var combo = new SingleSelectField<String>();
            var controller = new FormAIController(new Div(combo));
            controller.valueOptions(combo, List.of("EUR", "USD"));
            controller.valueOptions(combo,
                    (filter, limit) -> List.of("EUR", "USD"));

            var schema = json(findTool(controller.getTools(), "get_form_state")
                    .execute(JacksonUtils.createObjectNode()));
            var field = schema.path("fields").get(0);

            Assertions.assertTrue(field.path("queryable").asBoolean(),
                    "Re-registering valueOptions with a BiFunction must "
                            + "make the field queryable, got: " + field);
            Assertions.assertTrue(field.path("enum").isMissingNode(),
                    "Stale enum block must not survive re-registration with "
                            + "a BiFunction, got: " + field);
        }

        @Test
        void valueOptionsOnMultiSelectFixedCollectionRendersAsItemsEnum() {
            // valueOptions on a MultiSelectField registers a Set-returning
            // toValue (the typed signature requires it because T =
            // Set<String> for a multi-select). In get_form_state the
            // labels surface inside items.enum (multi-select schema)
            // rather than at the node level — pin that the same nesting
            // path applies whether or not the field is multi-select.
            var field = new FormTestFields.MultiSelectField<String>();
            var controller = new FormAIController(new Div(field));
            controller.valueOptions(field, List.of("alpha", "beta"), Set::of);

            var schema = json(findTool(controller.getTools(), "get_form_state")
                    .execute(JacksonUtils.createObjectNode()));
            var entry = schema.path("fields").get(0);
            var items = entry.path("items");

            Assertions.assertTrue(entry.path("array").asBoolean());
            var labels = new java.util.ArrayList<String>();
            items.path("enum").forEach(node -> labels.add(node.asString()));
            Assertions.assertEquals(List.of("alpha", "beta"), labels);
            Assertions.assertTrue(items.path("queryable").isMissingNode(),
                    "Fixed-collection registration must surface as enum, "
                            + "not queryable; got items: " + items);
        }
    }

    @Nested
    class BinderDescriptionSeeding {

        private final TestLogger binderReflectionLogger = TestLoggerFactory
                .getTestLogger(BinderReflection.class);

        @BeforeEach
        void clearLogger() {
            binderReflectionLogger.clear();
        }

        @Test
        void noBinderController_seedingIsNoOpAndDoesNotThrow() {
            // The 1-arg constructor leaves binder == null; the per-turn
            // seeding must short-circuit silently. Without the short-circuit
            // every plain-form controller would log a WARN on every
            // onRequestStart (BinderReflection catches the resulting
            // Field.get(null) NPE but logs the failure).
            var field = new TestField();
            var controller = new FormAIController(new Div(field));

            Assertions.assertDoesNotThrow(controller::onRequest);
            // And no description got seeded — the no-binder path simply
            // didn't run.
            Assertions.assertTrue(
                    formStateFields(controller).get(0).path("description")
                            .isMissingNode(),
                    "No-binder controller must not seed any description");
            var warnings = binderReflectionLogger.getLoggingEvents().stream()
                    .filter(event -> event.getLevel() == Level.WARN).toList();
            Assertions.assertTrue(warnings.isEmpty(),
                    "No-binder seeding must run silently — no WARN should "
                            + "be logged. Got: " + warnings);
        }

        @Test
        void boundFieldPropertyNameSurfacesInDescription() {
            // A named binding contributes the bean property name as the
            // default description; with no label or helper text, that's the
            // entire description string the LLM sees.
            var field = new TestField();
            var binder = new Binder<>(TestBean.class);
            binder.forField(field).bind("name");
            var controller = new FormAIController(new Div(field), binder);

            controller.onRequest();
            var entry = formStateFields(controller).get(0);

            Assertions.assertEquals("name",
                    entry.path("description").asString(),
                    "Bean property name should default the description");
        }

        @Test
        void labelAndPropertyNameMergeInDescription() {
            // When both label and property name are available, the merged
            // description is `label | propertyName`, in that order.
            var field = new LabeledField("Customer Name");
            var binder = new Binder<>(TestBean.class);
            binder.forField(field).bind("name");
            var controller = new FormAIController(new Div(field), binder);

            controller.onRequest();
            var entry = formStateFields(controller).get(0);

            Assertions.assertEquals("Customer Name | name",
                    entry.path("description").asString());
        }

        @Test
        void describeOverridesBinderSeeding() {
            // Explicit describe() always wins: seeding only fills nulls, so
            // a developer's description suppresses the bean property name.
            var field = new LabeledField("Customer Name");
            var binder = new Binder<>(TestBean.class);
            binder.forField(field).bind("name");
            var controller = new FormAIController(new Div(field), binder);
            controller.describe(field, "Full legal name");

            controller.onRequest();
            var entry = formStateFields(controller).get(0);

            var description = entry.path("description").asString();
            Assertions.assertEquals("Customer Name | Full legal name",
                    description);
            Assertions.assertFalse(description.contains("name | "),
                    "Property name must not appear when describe() set "
                            + "the description explicitly, got: "
                            + description);
        }

        @Test
        void lambdaBoundFieldHasNoSeededDescription() {
            // Lambda-bound bindings carry no property name; the description
            // falls back to whatever label/helper/describe() provides — here
            // just the label.
            var field = new LabeledField("Email");
            var binder = new Binder<>(TestBean.class);
            binder.forField(field).bind(TestBean::getEmail, TestBean::setEmail);
            var controller = new FormAIController(new Div(field), binder);

            controller.onRequest();
            var entry = formStateFields(controller).get(0);

            Assertions.assertEquals("Email",
                    entry.path("description").asString(),
                    "Lambda-bound field has no property name to seed; "
                            + "description should be the label alone");
        }

        @Test
        void unboundFieldHasNoSeededDescription() {
            // A field present in the form but not registered with the binder
            // contributes no seeded property name. With no label either, the
            // description is omitted entirely from the JSON.
            var bound = new LabeledField("Customer Name");
            var unbound = new TestField();
            var binder = new Binder<>(TestBean.class);
            binder.forField(bound).bind("name");
            var controller = new FormAIController(new Div(bound, unbound),
                    binder);

            controller.onRequest();
            var entries = formStateFields(controller);
            var unboundEntry = entries.stream()
                    .filter(e -> e.path("id").asString().equals(idOf(unbound)))
                    .findFirst().orElseThrow();

            Assertions.assertTrue(
                    unboundEntry.path("description").isMissingNode(),
                    "Unbound field with no label / helper / describe() must "
                            + "have no description, got: " + unboundEntry);
        }

        @Test
        void bindInstanceFieldsWithPropertyIdSurfacesPropertyName() {
            // bindInstanceFields walks the holder's declared fields and binds
            // each to the matching bean property. @PropertyId re-targets a
            // Java field whose name doesn't match the bean property —
            // emailField → "email". The seeded description should reflect
            // the bean property name, not the Java field name.
            var holder = new InstanceFieldsHolder();
            var binder = new Binder<>(TestBean.class);
            binder.bindInstanceFields(holder);
            var controller = new FormAIController(holder, binder);

            controller.onRequest();
            var entries = formStateFields(controller);

            var nameEntry = entries.stream().filter(
                    e -> e.path("id").asString().equals(idOf(holder.name)))
                    .findFirst().orElseThrow();
            var emailEntry = entries.stream()
                    .filter(e -> e.path("id").asString()
                            .equals(idOf(holder.emailField)))
                    .findFirst().orElseThrow();

            Assertions.assertEquals("Customer Name | name",
                    nameEntry.path("description").asString());
            var emailDescription = emailEntry.path("description").asString();
            Assertions.assertEquals("Address Of Email | email",
                    emailDescription,
                    "@PropertyId should surface the bean property name "
                            + "(\"email\"), not the Java field name "
                            + "(\"emailField\")");
            Assertions.assertFalse(emailDescription.contains("emailField"),
                    "Java field name must not leak into the description");
        }

        @Test
        void bindingsAddedBetweenTurnsAppearOnNextRequest() {
            // Seeding runs every turn, so a binding added after the
            // controller is constructed surfaces in the next get_form_state.
            var field = new TestField();
            var binder = new Binder<>(TestBean.class);
            var controller = new FormAIController(new Div(field), binder);

            controller.onRequest();
            Assertions.assertTrue(
                    formStateFields(controller).get(0).path("description")
                            .isMissingNode(),
                    "First turn: not yet bound, no seeded description");

            binder.forField(field).bind("name");
            controller.onRequest();

            Assertions.assertEquals("name",
                    formStateFields(controller).get(0).path("description")
                            .asString(),
                    "Second turn: binding added between turns surfaces as "
                            + "the seeded description");
        }

        @Test
        void seedingSkipsNonComponentHasValue() {
            // Binder.forField accepts any HasValue, including custom
            // adapters that aren't Components. Such fields cannot carry the
            // controller's UUID id and cannot appear in the LLM-facing
            // tools, so the seeding skips them silently rather than throwing
            // out of the per-turn lifecycle. Pins that contract: a
            // non-Component bound HasValue must not break onRequest.
            var nonComponentField = new NonComponentField();
            var formField = new TestField();
            var binder = new Binder<>(TestBean.class);
            binder.forField(nonComponentField).bind("name");
            var controller = new FormAIController(new Div(formField), binder);

            Assertions.assertDoesNotThrow(controller::onRequest,
                    "Seeding must not crash on a non-Component bound field");
            // The non-Component field never participates in discovery
            // either, so the LLM-facing form state lists only the real
            // component.
            Assertions.assertEquals(1, formStateFields(controller).size());
        }
    }

    /**
     * Minimal {@link HasValue} implementation that is <strong>not</strong> a
     * {@link Component}. Used to pin the seeding contract for bound HasValues
     * that can't carry the controller's UUID id.
     */
    private static class NonComponentField
            implements HasValue<HasValue.ValueChangeEvent<String>, String> {

        private String value = "";

        @Override
        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public Registration addValueChangeListener(
                ValueChangeListener<? super ValueChangeEvent<String>> listener) {
            return () -> {
            };
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }

        @Override
        public void setReadOnly(boolean readOnly) {
            // No-op; the controller does not lock a non-Component field.
        }

        @Override
        public boolean isRequiredIndicatorVisible() {
            return false;
        }

        @Override
        public void setRequiredIndicatorVisible(boolean visible) {
            // No-op; not exercised by these tests.
        }
    }

    /**
     * Form-holder used by {@code bindInstanceFields} tests. {@link PropertyId}
     * on {@code emailField} re-targets the binding to the bean's {@code email}
     * property; without it the Java field name {@code emailField} would not
     * match any bean property.
     */
    private static class InstanceFieldsHolder extends Div {
        final LabeledField name = new LabeledField("Customer Name");
        @PropertyId("email")
        final LabeledField emailField = new LabeledField("Address Of Email");

        InstanceFieldsHolder() {
            add(name, emailField);
        }
    }
}
