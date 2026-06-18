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

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.event.Level;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.form.FormTestFields.CompositeField;
import com.vaadin.flow.component.ai.form.FormTestFields.IntField;
import com.vaadin.flow.component.ai.form.FormTestFields.SingleSelectField;
import com.vaadin.flow.component.ai.form.FormTestFields.TestField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.server.VaadinSession;
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
                    ".ignoreField()", "SAME turn", "newly-appeared",
                    // bean-level cross-field rejections key on the "__form__"
                    // sentinel id, not a real field id.
                    "__form__", "cross-field")) {
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
            controller.ignoreField(hidden);

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
            // describeField() registers a hint but does not ignore the field;
            // the controller must distinguish "has a hint entry" from "is
            // ignored". If they collapse, every described or
            // fieldValueOptions-bound field would silently escape locking.
            var described = new TestField();
            var controller = new FormAIController(new Div(described));
            controller.describeField(described, "the merchant name");

            controller.onRequest();

            Assertions.assertTrue(described.isReadOnly(),
                    "A field with a description hint but no ignoreField() call "
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

            controller.ignoreField(field);
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
                    () -> controller.describeField(null, "x"));
            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.ignoreField(null));
            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.fieldValueOptions(null));
            Assertions.assertThrows(NullPointerException.class,
                    () -> ValueOptions.forField((HasValue<?, String>) null));
            Assertions.assertThrows(NullPointerException.class,
                    () -> ValueOptions.forField(
                            (com.vaadin.flow.data.selection.MultiSelect<?, String>) null));
        }

        @Test
        void fixedOptionsFilterRestrictsResultsByLabelSubstring() {
            // The fixed-options variant builds a case-insensitive 'contains'
            // filter on the supplied labels.
            var field = new TestField();
            var controller = new FormAIController(new Div(field));
            controller.fieldValueOptions(ValueOptions.forField(field)
                    .options(List.of("apple", "banana", "cherry")));
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
            var config = ValueOptions.forField(field);

            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.describeField(field, null));
            Assertions.assertThrows(NullPointerException.class,
                    () -> config.options(
                            (java.util.function.BiFunction<String, Integer, List<String>>) null));
            Assertions.assertThrows(NullPointerException.class,
                    () -> config.options((java.util.Collection<String>) null));
            Assertions.assertThrows(NullPointerException.class,
                    () -> config.itemLabelGenerator(null));
            // A null converter passed to the controller's two-argument
            // overload throws NPE.
            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.fieldValueOptions(ValueOptions
                            .forField(new IntField()).options(List.of(1)),
                            null));
            // An empty fixed-options list leaves the field un-fillable;
            // rejected at the options() call site.
            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> config.options(List.<String> of()));
            // A ValueOptions with no options(...) set is rejected by the
            // controller at registration. The missing-converter case is
            // compile-time enforced (no overload accepts a non-String
            // ValueOptions without a Function argument), so it has no
            // corresponding runtime test.
            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> controller
                            .fieldValueOptions(ValueOptions.forField(field)));
        }

        @Test
        void fieldValueOptionsAcceptsNonStringFieldWithToValueConverter() {
            // fieldValueOptions is generic over the field's value type — an
            // Integer-valued field carries Integer items, and the
            // String.valueOf fallback renders the LLM-facing labels when
            // the field has no item-label generator of its own.
            var field = new IntField();
            var controller = new FormAIController(new Div(field));
            controller.fieldValueOptions(
                    ValueOptions.forField(field).options(List.of(1, 2, 3)),
                    Integer::parseInt);
            controller.onRequest();

            Assertions.assertEquals("1\n2\n3\n",
                    executeQueryFieldOptions(controller, field, "", 10));
        }

        @Test
        void identityToValueLetsStringFieldsUseLabelsDirectly() {
            // Passing Function.identity() for toValue is the canonical
            // String-field shortcut: the chosen label is the value as-is.
            // Smoke-test both shapes (query callback and fixed collection).
            var queriedField = new TestField();
            var fixedField = new TestField();
            var controller = new FormAIController(
                    new Div(queriedField, fixedField));
            controller.fieldValueOptions(ValueOptions.forField(queriedField)
                    .options((filter, limit) -> List.of("alpha", "beta")));
            controller.fieldValueOptions(ValueOptions.forField(fixedField)
                    .options(List.of("apple", "banana", "cherry")));
            controller.onRequest();

            Assertions.assertEquals("alpha\nbeta\n",
                    executeQueryFieldOptions(controller, queriedField, "", 10));
            Assertions.assertEquals("banana\n",
                    executeQueryFieldOptions(controller, fixedField, "an", 10));
        }

        @Test
        void reregisteringWithBiFunctionClearsPriorFixedOptionsFlag() {
            // Each fieldValueOptions call replaces the previous registration
            // for the same field. The fixed-options variant sets a flag that
            // makes the schema render options as 'enum'; re-registering with
            // a query callback must reset that flag so the schema rendering
            // matches the new registration (queryable, not enum).
            var combo = new SingleSelectField<String>();
            var controller = new FormAIController(new Div(combo));
            controller.fieldValueOptions(ValueOptions.forField(combo)
                    .options(List.of("EUR", "USD")));
            controller.fieldValueOptions(ValueOptions.forField(combo)
                    .options((filter, limit) -> List.of("EUR", "USD")));

            var schema = json(findTool(controller.getTools(), "get_form_state")
                    .execute(JacksonUtils.createObjectNode()));
            var field = schema.path("fields").get(0);

            Assertions.assertTrue(field.path("queryable").asBoolean(),
                    "Re-registering fieldValueOptions with a BiFunction must "
                            + "make the field queryable, got: " + field);
            Assertions.assertTrue(field.path("enum").isMissingNode(),
                    "Stale enum block must not survive re-registration with "
                            + "a BiFunction, got: " + field);
        }

        @Test
        void reregisteringValueOptionsOverwritesItemLabelGenerator() {
            // Each fieldValueOptions call replaces the previous registration
            // in full — including the item-label generator — so a stale
            // labeler cannot survive a re-registration. Asserted on both the
            // enum block (the wrapped query path) and the schema's value
            // string (the value-rendering path) so a half-overwrite of one
            // but not the other is caught.
            var alpha = new FormTestFields.Project("P-1", "Alpha");
            var combo = new FormTestFields.SingleSelectField<FormTestFields.Project>();
            combo.setValue(alpha);
            var controller = new FormAIController(new Div(combo));
            controller.fieldValueOptions(
                    ValueOptions.forField(combo).options(List.of(alpha))
                            .itemLabelGenerator(FormTestFields.Project::code),
                    label -> alpha);
            controller.fieldValueOptions(
                    ValueOptions.forField(combo).options(List.of(alpha))
                            .itemLabelGenerator(FormTestFields.Project::name),
                    label -> alpha);

            var f = json(findTool(controller.getTools(), "get_form_state")
                    .execute(JacksonUtils.createObjectNode())).path("fields")
                    .get(0);
            var labels = new java.util.ArrayList<String>();
            f.path("enum").forEach(n -> labels.add(n.asString()));

            Assertions.assertEquals(List.of("Alpha"), labels,
                    "Second registration's labeler must drive the enum "
                            + "block; got: " + labels);
            Assertions.assertEquals("Alpha", f.path("value").asString(),
                    "Second registration's labeler must drive the value "
                            + "rendering; got: " + f.path("value"));
        }

        @Test
        void fieldValueOptionsForFieldOnUpcastMultiSelectReferenceThrowsIllegalArgument() {
            // A MultiSelect statically typed as such picks the MultiSelect-
            // typed forField overload at compile time. This runtime check
            // is for the upcast case: the developer holds a HasValue
            // reference to a MultiSelect instance, so the compiler picks
            // the single-value overload and the controller would otherwise
            // accept a Set-returning converter. The check redirects to the
            // typed MultiSelect overload.
            var multiSelect = new FormTestFields.MultiSelectField<String>();
            HasValue<?, java.util.Set<String>> upcast = multiSelect;
            var controller = new FormAIController(new Div(multiSelect));
            var ex = Assertions.assertThrows(IllegalArgumentException.class,
                    () -> controller.fieldValueOptions(
                            ValueOptions.forField(upcast)
                                    .options(List.of(java.util.Set.of("a"))),
                            label -> java.util.Set.of(label)));
            Assertions.assertTrue(ex.getMessage().contains("MultiSelect"),
                    "Rejection must name MultiSelect so the developer can "
                            + "tighten the reference type; got: "
                            + ex.getMessage());
        }

        @Test
        void fieldValueOptionsRejectsCollectionValuedFieldNotImplementingMultiSelect() {
            // Collection-valued fields must implement MultiSelect; otherwise
            // there is no defined aggregation for per-label converter results
            // and the controller refuses to register them.
            var field = new FormTestFields.CollectionWithoutMultiSelectField();
            var controller = new FormAIController(new Div(field));
            var ex = Assertions
                    .assertThrows(IllegalArgumentException.class,
                            () -> controller
                                    .fieldValueOptions(
                                            ValueOptions.forField(field)
                                                    .options(List
                                                            .of(List.of("a"))),
                                            label -> List.of(label)));
            Assertions.assertTrue(
                    ex.getMessage().contains("Collection")
                            && ex.getMessage().contains("MultiSelect"),
                    "Rejection must name both 'Collection' and "
                            + "'MultiSelect' so the developer can resolve "
                            + "the mismatch; got: " + ex.getMessage());
        }

        @Test
        void fieldValueOptionsAcceptsTypedMultiSelectFieldWithNonStringConverter() {
            // Counterpart to the Collection-value rejection: a MultiSelect-
            // typed field with a non-String per-element type and an explicit
            // converter must remain accepted, even though its empty value is
            // itself a Collection.
            var field = new FormTestFields.MultiSelectField<Integer>();
            var controller = new FormAIController(new Div(field));
            Assertions.assertDoesNotThrow(() -> controller.fieldValueOptions(
                    ValueOptions.forField(field).options(List.of(1, 2)),
                    Integer::parseInt));
        }

        @Test
        void fieldValueOptionsConfigFixedAndQueryClearEachOther() {
            // The two options(...) overloads (fixed Collection, queryable
            // BiFunction) clear each other so a half-finished config can't
            // resurrect stale state. fixed-then-queryable lands as
            // queryable; queryable-then-fixed lands as enum.
            var queryWins = new TestField();
            var fixedWins = new TestField();
            var controller = new FormAIController(
                    new Div(queryWins, fixedWins));
            controller.fieldValueOptions(
                    ValueOptions.forField(queryWins).options(List.of("a", "b"))
                            .options((filter, limit) -> List.of("x", "y")));
            controller.fieldValueOptions(ValueOptions.forField(fixedWins)
                    .options((filter, limit) -> List.of("x", "y"))
                    .options(List.of("a", "b")));

            var schema = json(findTool(controller.getTools(), "get_form_state")
                    .execute(JacksonUtils.createObjectNode()));
            var queryEntry = schema.path("fields").get(0);
            var fixedEntry = schema.path("fields").get(1);

            Assertions.assertTrue(queryEntry.path("queryable").asBoolean(),
                    "fixed options(...) then queryable options(...) must "
                            + "land as queryable, got: " + queryEntry);
            Assertions.assertTrue(queryEntry.path("enum").isMissingNode(),
                    "Stale enum must not survive queryable options(...) "
                            + "overwriting fixed options(...), got: "
                            + queryEntry);
            var fixedLabels = new java.util.ArrayList<String>();
            fixedEntry.path("enum")
                    .forEach(node -> fixedLabels.add(node.asString()));
            Assertions.assertEquals(List.of("a", "b"), fixedLabels,
                    "queryable options(...) then fixed options(...) must "
                            + "land as fixed enum, got: " + fixedEntry);
        }

        @Test
        void multiSelectFixedOptionsRenderAsItemsEnum() {
            // fieldValueOptions on a MultiSelectField surfaces labels inside
            // items.enum (multi-select schema) rather than at the node
            // level — pin that the same nesting path applies whether or
            // not the field is multi-select.
            var field = new FormTestFields.MultiSelectField<String>();
            var controller = new FormAIController(new Div(field));
            controller.fieldValueOptions(ValueOptions.forField(field)
                    .options(List.of("alpha", "beta")));

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
            // Explicit describeField() always wins: seeding only fills nulls,
            // so
            // a developer's description suppresses the bean property name.
            var field = new LabeledField("Customer Name");
            var binder = new Binder<>(TestBean.class);
            binder.forField(field).bind("name");
            var controller = new FormAIController(new Div(field), binder);
            controller.describeField(field, "Full legal name");

            controller.onRequest();
            var entry = formStateFields(controller).get(0);

            var description = entry.path("description").asString();
            Assertions.assertEquals("Customer Name | Full legal name",
                    description);
            Assertions.assertFalse(description.contains("name | "),
                    "Property name must not appear when describeField() set "
                            + "the description explicitly, got: "
                            + description);
        }

        @Test
        void lambdaBoundFieldHasNoSeededDescription() {
            // Lambda-bound bindings carry no property name; the description
            // falls back to whatever label/helper/describeField() provides —
            // here
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
                    "Unbound field with no label / helper / describeField() must "
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

    @Nested
    class FieldValuesChangedListener {

        @Test
        void listenerFiresWithChangedFieldsAfterSuccessfulTurn() {
            var field = new TestField();
            var controller = new FormAIController(new Div(field));
            var captured = new AtomicReference<List<FieldValueChange>>();
            controller.addFieldValueChangedListener(captured::set);

            controller.onRequest();
            field.setValue("John");
            controller.onResponse(null);

            var changes = captured.get();
            Assertions.assertNotNull(changes, "Listener must fire when a "
                    + "field's value changed during the turn");
            Assertions.assertEquals(1, changes.size());
            var change = changeFor(changes, field);
            Assertions.assertEquals("", change.oldValue());
            Assertions.assertEquals("John", change.newValue());
        }

        @Test
        void listenerNotInvokedWhenNoFieldChanged() {
            var field = new TestField();
            var controller = new FormAIController(new Div(field));
            var invocations = new AtomicInteger();
            controller.addFieldValueChangedListener(
                    c -> invocations.incrementAndGet());

            controller.onRequest();
            // No setValue between request and response.
            controller.onResponse(null);

            Assertions.assertEquals(0, invocations.get(),
                    "Listener must not be called when no field changed");
        }

        @Test
        void listenerNotInvokedOnError() {
            var field = new TestField();
            var controller = new FormAIController(new Div(field));
            var invocations = new AtomicInteger();
            controller.addFieldValueChangedListener(
                    c -> invocations.incrementAndGet());

            controller.onRequest();
            field.setValue("partial");
            controller.onResponse(new RuntimeException("boom"));

            Assertions.assertEquals(0, invocations.get(),
                    "Listener must not fire when the turn ended in error, "
                            + "even if a tool call already wrote to a field");
        }

        @Test
        void listContainsOnlyChangedFields() {
            var changed = new TestField();
            var untouched = new TestField();
            var controller = new FormAIController(new Div(changed, untouched));
            var captured = new AtomicReference<List<FieldValueChange>>();
            controller.addFieldValueChangedListener(captured::set);

            controller.onRequest();
            changed.setValue("X");
            controller.onResponse(null);

            var changes = captured.get();
            Assertions.assertEquals(1, changes.size(),
                    "Only the changed field must appear; got: " + changes);
            Assertions.assertTrue(containsChangeFor(changes, changed));
            Assertions.assertFalse(containsChangeFor(changes, untouched));
        }

        @Test
        void ignoredFieldsDoNotAppearEvenIfTheirValueChanged() {
            // Application-driven cascades into a field marked ignoreField()
            // must
            // not leak into the change list — ignoreField() is the
            // application's
            // opt-out from AI-driven tracking on either side of the lifecycle.
            var visible = new TestField();
            var ignored = new TestField();
            visible.addValueChangeListener(e -> ignored.setValue("cascade"));
            var controller = new FormAIController(new Div(visible, ignored));
            controller.ignoreField(ignored);
            var captured = new AtomicReference<List<FieldValueChange>>();
            controller.addFieldValueChangedListener(captured::set);

            controller.onRequest();
            visible.setValue("primary");
            controller.onResponse(null);

            var changes = captured.get();
            Assertions.assertTrue(containsChangeFor(changes, visible));
            Assertions.assertFalse(containsChangeFor(changes, ignored),
                    "Ignored fields must not appear in the change list; got: "
                            + changes);
        }

        @Test
        void noListenerRegisteredIsHarmlessAcrossTheLifecycle() {
            var field = new TestField();
            var controller = new FormAIController(new Div(field));

            Assertions.assertDoesNotThrow(() -> {
                controller.onRequest();
                field.setValue("any");
                controller.onResponse(null);
            }, "Lifecycle must run without a listener registered");
        }

        @Test
        void listenerExceptionStillReleasesFieldLocks() {
            // Locks set in onRequest must release regardless of listener
            // outcome: a stuck-locked field strands the user.
            var field = new TestField();
            var controller = new FormAIController(new Div(field));
            controller.addFieldValueChangedListener(c -> {
                throw new RuntimeException("listener boom");
            });

            controller.onRequest();
            field.setValue("anything");
            controller.onResponse(null);

            Assertions.assertFalse(field.isReadOnly(),
                    "Field must be unlocked even if a listener threw");
        }

        @Test
        void cascadingChangesAppearInTheSameTurn() {
            // Cascades through ValueChangeListener are observable in the diff
            // regardless of who triggered them — pin the symmetry so this
            // doesn't quietly regress to "only AI-driven writes are reported".
            var primary = new TestField();
            var cascaded = new TestField();
            primary.addValueChangeListener(e -> cascaded.setValue("derived"));
            var controller = new FormAIController(new Div(primary, cascaded));
            var captured = new AtomicReference<List<FieldValueChange>>();
            controller.addFieldValueChangedListener(captured::set);

            controller.onRequest();
            primary.setValue("driver");
            controller.onResponse(null);

            var changes = captured.get();
            Assertions.assertEquals(2, changes.size(),
                    "Both the driver and cascaded fields must be reported; "
                            + "got: " + changes);
            Assertions.assertEquals("derived",
                    changeFor(changes, cascaded).newValue());
        }

        @Test
        void multiSelectSetWithEqualContentIsNotReportedAsChange() {
            var field = new FormTestFields.MultiSelectField<String>();
            field.setValue(Set.of("a", "b"));
            var controller = new FormAIController(new Div(field));
            var captured = new AtomicReference<List<FieldValueChange>>();
            controller.addFieldValueChangedListener(captured::set);

            controller.onRequest();
            // Same content, different Set instance — Objects.equals true.
            field.setValue(Set.of("b", "a"));
            controller.onResponse(null);

            Assertions.assertNull(captured.get(),
                    "A multi-select set equal to its previous value must not "
                            + "be reported as a change");
        }

        @Test
        void multiSelectSetWithDifferentContentIsReportedAsChange() {
            var field = new FormTestFields.MultiSelectField<String>();
            field.setValue(Set.of("a", "b"));
            var controller = new FormAIController(new Div(field));
            var captured = new AtomicReference<List<FieldValueChange>>();
            controller.addFieldValueChangedListener(captured::set);

            controller.onRequest();
            field.setValue(Set.of("a", "c"));
            controller.onResponse(null);

            var change = changeFor(captured.get(), field);
            Assertions.assertEquals(Set.of("a", "b"), change.oldValue());
            Assertions.assertEquals(Set.of("a", "c"), change.newValue());
        }

        @Test
        void changeListIteratesInDocumentOrder() {
            var first = new TestField();
            var second = new TestField();
            var third = new TestField();
            var controller = new FormAIController(
                    new Div(first, second, third));
            var captured = new AtomicReference<List<FieldValueChange>>();
            controller.addFieldValueChangedListener(captured::set);

            controller.onRequest();
            third.setValue("c");
            first.setValue("a");
            second.setValue("b");
            controller.onResponse(null);

            Assertions.assertEquals(List.of(first, second, third),
                    captured.get().stream().map(FieldValueChange::field)
                            .toList(),
                    "List iteration must follow document order regardless of "
                            + "the order writes happened in");
        }

        @Test
        void nullPreTurnValueIsReportedFaithfully() {
            var field = new FormTestFields.DateField();
            var controller = new FormAIController(new Div(field));
            var captured = new AtomicReference<List<FieldValueChange>>();
            controller.addFieldValueChangedListener(captured::set);

            controller.onRequest();
            field.setValue(LocalDate.of(2026, 1, 1));
            controller.onResponse(null);

            var change = changeFor(captured.get(), field);
            Assertions.assertNull(change.oldValue(),
                    "Pre-turn null must round-trip as null");
            Assertions.assertEquals(LocalDate.of(2026, 1, 1),
                    change.newValue());
        }

        @Test
        void clearingAValueToNullIsReportedAsChange() {
            // Inverse of nullPreTurnValueIsReportedFaithfully: pre-turn
            // non-null → post-turn null must surface as a change so
            // applications can react (e.g. clear the highlight).
            var field = new FormTestFields.DateField();
            field.setValue(LocalDate.of(2026, 1, 1));
            var controller = new FormAIController(new Div(field));
            var captured = new AtomicReference<List<FieldValueChange>>();
            controller.addFieldValueChangedListener(captured::set);

            controller.onRequest();
            field.setValue(null);
            controller.onResponse(null);

            var change = changeFor(captured.get(), field);
            Assertions.assertEquals(LocalDate.of(2026, 1, 1),
                    change.oldValue());
            Assertions.assertNull(change.newValue(),
                    "Clearing to null must surface as the new value");
        }

        @Test
        void multipleListenersAllFire() {
            var field = new TestField();
            var controller = new FormAIController(new Div(field));
            var first = new AtomicReference<List<FieldValueChange>>();
            var second = new AtomicReference<List<FieldValueChange>>();
            controller.addFieldValueChangedListener(first::set);
            controller.addFieldValueChangedListener(second::set);

            controller.onRequest();
            field.setValue("X");
            controller.onResponse(null);

            Assertions.assertNotNull(first.get(), "First listener must fire");
            Assertions.assertNotNull(second.get(),
                    "Second listener must also fire");
            Assertions.assertEquals(first.get(), second.get(),
                    "Both listeners must receive equal change lists");
        }

        @Test
        void nullListenerThrows() {
            var controller = new FormAIController(new Div(new TestField()));

            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.addFieldValueChangedListener(null));
        }

        @Test
        void registrationRemoveStopsFutureCalls() {
            var field = new TestField();
            var controller = new FormAIController(new Div(field));
            var calls = new AtomicInteger();
            var registration = controller
                    .addFieldValueChangedListener(c -> calls.incrementAndGet());

            controller.onRequest();
            field.setValue("first");
            controller.onResponse(null);
            Assertions.assertEquals(1, calls.get(),
                    "Listener must fire while registered");

            registration.remove();

            controller.onRequest();
            field.setValue("second");
            controller.onResponse(null);
            Assertions.assertEquals(1, calls.get(),
                    "Listener must not fire after Registration.remove()");
        }

        @Test
        void listenerExceptionDoesNotPreventOtherListeners() {
            // One bad listener must not silence the rest — otherwise a
            // library listener could break the application's listener (or
            // vice versa) depending on registration order.
            var field = new TestField();
            var controller = new FormAIController(new Div(field));
            var followingCalls = new AtomicInteger();
            controller.addFieldValueChangedListener(c -> {
                throw new RuntimeException("first throws");
            });
            controller.addFieldValueChangedListener(
                    c -> followingCalls.incrementAndGet());

            controller.onRequest();
            field.setValue("X");
            controller.onResponse(null);

            Assertions.assertEquals(1, followingCalls.get(),
                    "An exception from one listener must not prevent the "
                            + "next listener from firing");
        }

        @Test
        void listenerCanRemoveItselfDuringDispatchWithoutBreakingTheTurn() {
            // Self-removal during dispatch must not throw or skip remaining
            // listeners: a common idiom for one-shot listeners.
            var field = new TestField();
            var controller = new FormAIController(new Div(field));
            var registration = new AtomicReference<Registration>();
            var selfRemovingCalls = new AtomicInteger();
            var followingCalls = new AtomicInteger();
            registration.set(controller.addFieldValueChangedListener(c -> {
                selfRemovingCalls.incrementAndGet();
                registration.get().remove();
            }));
            controller.addFieldValueChangedListener(
                    c -> followingCalls.incrementAndGet());

            controller.onRequest();
            field.setValue("X");
            controller.onResponse(null);

            Assertions.assertEquals(1, selfRemovingCalls.get(),
                    "Self-removing listener fires for the dispatch in which "
                            + "it removed itself");
            Assertions.assertEquals(1, followingCalls.get(),
                    "Listeners following the self-removing one must still fire");

            controller.onRequest();
            field.setValue("Y");
            controller.onResponse(null);

            Assertions.assertEquals(1, selfRemovingCalls.get(),
                    "Self-removing listener must not fire after the turn "
                            + "where it removed itself");
            Assertions.assertEquals(2, followingCalls.get(),
                    "Following listener must keep firing on subsequent turns");
        }

        @Test
        void fieldRevealedMidTurnIsReportedAsChange() {
            // Visibility-cascade headline: a hidden field that gets
            // revealed-and-written during a single turn must surface in
            // the change list. Otherwise the LLM's effect on the form
            // would be silently underreported.
            var primary = new TestField();
            var conditional = new TestField();
            conditional.setVisible(false);
            primary.addValueChangeListener(e -> {
                conditional.setVisible(true);
                conditional.setValue("derived");
            });
            var controller = new FormAIController(
                    new Div(primary, conditional));
            var captured = new AtomicReference<List<FieldValueChange>>();
            controller.addFieldValueChangedListener(captured::set);

            controller.onRequest();
            primary.setValue("driver");
            controller.onResponse(null);

            var change = changeFor(captured.get(), conditional);
            Assertions.assertEquals("", change.oldValue(),
                    "Old value must reflect the field's pre-turn value");
            Assertions.assertEquals("derived", change.newValue());
        }

        @Test
        void hiddenFieldRevealedAndChangedReportsRealOldValue() {
            // When the hidden field already had a non-null value
            // (e.g. bound to a bean), the diff must report the real
            // (preset → derived) transition rather than (null → derived).
            var primary = new TestField();
            var conditional = new TestField();
            conditional.setValue("preset");
            conditional.setVisible(false);
            primary.addValueChangeListener(e -> {
                conditional.setVisible(true);
                conditional.setValue("derived");
            });
            var controller = new FormAIController(
                    new Div(primary, conditional));
            var captured = new AtomicReference<List<FieldValueChange>>();
            controller.addFieldValueChangedListener(captured::set);

            controller.onRequest();
            primary.setValue("driver");
            controller.onResponse(null);

            var change = changeFor(captured.get(), conditional);
            Assertions.assertEquals("preset", change.oldValue(),
                    "Pre-turn value of a hidden field must round-trip into "
                            + "the change record, not a spurious null");
            Assertions.assertEquals("derived", change.newValue());
        }

        @Test
        void fieldRevealedMidTurnWithUnchangedValueIsNotReported() {
            // False-positive guard: revealing a hidden field without
            // writing to it is not a change and must not appear in the
            // change list.
            var primary = new TestField();
            var conditional = new TestField();
            conditional.setValue("preset");
            conditional.setVisible(false);
            primary.addValueChangeListener(e -> conditional.setVisible(true));
            var controller = new FormAIController(
                    new Div(primary, conditional));
            var captured = new AtomicReference<List<FieldValueChange>>();
            controller.addFieldValueChangedListener(captured::set);

            controller.onRequest();
            primary.setValue("driver");
            controller.onResponse(null);

            Assertions.assertFalse(
                    containsChangeFor(captured.get(), conditional),
                    "Revealing a hidden field without changing its value "
                            + "must not be reported as a change");
        }

        @Test
        void fieldRevealedAndFilledInSameTurnIsReported() {
            var controlling = new TestField();
            var conditional = new TestField();
            conditional.setVisible(false);
            controlling
                    .addValueChangeListener(e -> conditional.setVisible(true));
            var controller = new FormAIController(
                    new Div(controlling, conditional));
            var captured = new AtomicReference<List<FieldValueChange>>();
            controller.addFieldValueChangedListener(captured::set);

            controller.onRequest();
            controlling.setValue("business"); // reveals the conditional field
            conditional.setValue("cost-center-42"); // AI fills the revealed one
            controller.onResponse(null);

            var changes = captured.get();
            Assertions.assertTrue(containsChangeFor(changes, conditional),
                    "A field revealed and filled within the same turn must be "
                            + "reported as changed; got: " + changes);
        }

        @Test
        void fieldAddedAndFilledInSameTurnIsReported() {
            // Stronger variant: the conditional field does not exist in
            // the form when onRequest snapshots. A controlling field's
            // listener ADDS it to the form mid-turn (e.g. a checkbox
            // revealing a new panel), and the same turn fills it. It must
            // still be reported as changed.
            var controlling = new TestField();
            var added = new TestField();
            var form = new Div(controlling);
            // Adding the conditional field is application-driven, triggered
            // by the controlling field's value change.
            controlling.addValueChangeListener(e -> form.add(added));
            var controller = new FormAIController(form);
            var captured = new AtomicReference<List<FieldValueChange>>();
            controller.addFieldValueChangedListener(captured::set);

            controller.onRequest();
            controlling.setValue("business"); // adds the new field to the form
            added.setValue("cost-center-42"); // AI fills the newly-added field
            controller.onResponse(null);

            var changes = captured.get();
            Assertions.assertTrue(containsChangeFor(changes, added),
                    "A field added to the form and filled within the same "
                            + "turn must be reported as changed; got: "
                            + changes);
        }

        private static FieldValueChange changeFor(
                List<FieldValueChange> changes, HasValue<?, ?> field) {
            return changes.stream().filter(c -> c.field() == field).findFirst()
                    .orElseThrow(() -> new AssertionError(
                            "No FieldValueChange entry for field " + field
                                    + " in " + changes));
        }

        private static boolean containsChangeFor(List<FieldValueChange> changes,
                HasValue<?, ?> field) {
            return changes.stream().anyMatch(c -> c.field() == field);
        }
    }

    @Nested
    class Highlight {

        // Field-highlighter integration is exercised through the JS
        // invocations the controller queues on the field's element. We
        // assert on the queued script text rather than DOM side effects
        // because the real visual change happens in the web component, on
        // the client. Tests use a minimal UI so executeJs lands in the
        // pending-invocation list.

        private UI ui;

        @BeforeEach
        void attachUi() {
            ui = new UI();
            var mockSession = Mockito.mock(VaadinSession.class);
            ui.getInternals().setSession(mockSession);
        }

        @Test
        void showFieldHighlightQueuesAddUserWithSingleAIUser() {
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.showFieldHighlight(field);
            var invocations = drainPendingJs();
            var scripts = scriptsOn(invocations, field);

            Assertions.assertEquals(1, scripts.size(),
                    "showFieldHighlight must queue exactly one script; got: "
                            + scripts);
            var script = scripts.getFirst();
            Assertions.assertTrue(script.contains(
                    "customElements.get('vaadin-field-highlighter').addUser")
                    && script.contains("'AI'"),
                    "Script must invoke the field-highlighter addUser with "
                            + "the AI user; got: " + script);
            Assertions.assertTrue(
                    paramsOn(invocations, field).stream()
                            .anyMatch(p -> p instanceof String s
                                    && s.startsWith("vaadin-ai-")),
                    "addUser must be parameterised with a vaadin-ai- prefixed "
                            + "UUID so it cannot collide with other users on "
                            + "the field");
        }

        @Test
        void hideFieldHighlightQueuesRemoveUserWithControllerId() {
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.hideFieldHighlight(field);
            var invocations = drainPendingJs();
            var scripts = scriptsOn(invocations, field);

            Assertions.assertEquals(1, scripts.size(),
                    "hideFieldHighlight must queue exactly one script; got: "
                            + scripts);
            var script = scripts.getFirst();
            Assertions.assertTrue(script.contains(
                    "customElements.get('vaadin-field-highlighter').removeUser"),
                    "Script must invoke removeUser keyed by the controller's "
                            + "AI user id; got: " + script);
            Assertions.assertTrue(
                    paramsOn(invocations, field).stream()
                            .anyMatch(p -> p instanceof String s
                                    && s.startsWith("vaadin-ai-")),
                    "removeUser must be parameterised with the controller's "
                            + "vaadin-ai- prefixed UUID so it removes only the "
                            + "AI user and leaves other users untouched");
        }

        @Test
        void showFieldHighlightTwiceQueuesIdenticalScripts() {
            // The web component dedups by user id, so repeated addUser with
            // the same id collapses to one entry on the client. The Java
            // side simply queues the same script twice.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.showFieldHighlight(field);
            controller.showFieldHighlight(field);
            var scripts = pendingJsOn(field);

            Assertions.assertEquals(2, scripts.size(),
                    "Each showFieldHighlight call queues its own script; got: "
                            + scripts);
            Assertions.assertEquals(scripts.get(0), scripts.get(1),
                    "Both invocations must produce identical scripts so the "
                            + "client converges on a single highlighted user");
        }

        @Test
        void showThenHideThenShowQueuesThreeScriptsInOrder() {
            // A flash-clear-reshow sequence (e.g. an application clearing
            // the highlight on user focus and re-applying it on the next
            // turn) must enqueue the three scripts in the call order. Pins
            // that hide doesn't swallow or collapse a subsequent show, and
            // that the show-script appears at both endpoints.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.showFieldHighlight(field);
            controller.hideFieldHighlight(field);
            controller.showFieldHighlight(field);
            var scripts = pendingJsOn(field);

            Assertions.assertEquals(3, scripts.size(),
                    "Each call enqueues its own script; got: " + scripts);
            Assertions.assertTrue(isShowScript(scripts.get(0)),
                    "First script must be the show; got: " + scripts.get(0));
            Assertions.assertTrue(isHideScript(scripts.get(1)),
                    "Middle script must be the hide; got: " + scripts.get(1));
            Assertions.assertTrue(isShowScript(scripts.get(2)),
                    "Third script must be the show again; got: "
                            + scripts.get(2));
        }

        @Test
        void nullFieldThrows() {
            // Message is asserted, not just the exception type: without an
            // explicit null guard, the IllegalArgumentException branch would
            // incidentally NPE on field.getClass(), accidentally satisfying a
            // type-only assertion.
            var controller = new FormAIController(new Div());

            var showNpe = Assertions.assertThrows(NullPointerException.class,
                    () -> controller.showFieldHighlight(null));
            Assertions.assertEquals("Field must not be null",
                    showNpe.getMessage());
            var hideNpe = Assertions.assertThrows(NullPointerException.class,
                    () -> controller.hideFieldHighlight(null));
            Assertions.assertEquals("Field must not be null",
                    hideNpe.getMessage());
        }

        @Test
        void nonComponentFieldThrows() {
            var controller = new FormAIController(new Div());
            var nonComponent = new NonComponentField();

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> controller.showFieldHighlight(nonComponent));
            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> controller.hideFieldHighlight(nonComponent));
        }

        @Test
        void highlightWorksForFieldOutsideTheControllerForm() {
            // Controller's form intentionally does not contain `outsideField`.
            // Pins the contract that showFieldHighlight / hideFieldHighlight
            // operate on
            // any HasValue Component, regardless of form membership.
            var formField = new TestField();
            var outsideField = new TestField();
            var formDiv = new Div(formField);
            var siblingDiv = new Div(outsideField);
            ui.add(formDiv);
            ui.add(siblingDiv);
            var controller = new FormAIController(formDiv);

            controller.showFieldHighlight(outsideField);
            var scripts = pendingJsOn(outsideField);

            Assertions.assertEquals(1, scripts.size(),
                    "showFieldHighlight must queue a script even when the field "
                            + "is outside the controller's form; got: "
                            + scripts);
            Assertions.assertTrue(isShowScript(scripts.getFirst()));
        }

        @Test
        void highlightOnOneFieldDoesNotEmitJsForAnother() {
            // Two fields, only one is highlighted. The other field must not
            // receive any field-highlighter script.
            var highlighted = new TestField();
            var untouched = new TestField();
            var form = new Div(highlighted, untouched);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.showFieldHighlight(highlighted);

            var dump = drainPendingJs();
            Assertions.assertEquals(1, scriptsOn(dump, highlighted).size());
            Assertions.assertEquals(0, scriptsOn(dump, untouched).size(),
                    "Highlighting one field must not enqueue scripts on "
                            + "unrelated fields");
        }

        @Test
        void hideFieldHighlightOnOneFieldDoesNotEmitJsForAnother() {
            // Sibling-independence check on the clearing path: hiding one
            // field's highlight must leave another field's script queue
            // untouched.
            var cleared = new TestField();
            var untouched = new TestField();
            var form = new Div(cleared, untouched);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.hideFieldHighlight(cleared);

            var dump = drainPendingJs();
            Assertions.assertEquals(1, scriptsOn(dump, cleared).size());
            Assertions.assertEquals(0, scriptsOn(dump, untouched).size());
        }

        @Test
        void hideFieldHighlightLeavesOtherHighlightedFieldsAlone() {
            // Pin behavioural independence: with two fields already
            // highlighted, hiding one must only emit the clear-script on the
            // hidden field. The other field's queue keeps its show-script
            // and gains nothing — its client-side state stays highlighted.
            var keep = new TestField();
            var clear = new TestField();
            var form = new Div(keep, clear);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.showFieldHighlight(keep);
            controller.showFieldHighlight(clear);
            controller.hideFieldHighlight(clear);

            var dump = drainPendingJs();
            var keepScripts = scriptsOn(dump, keep);
            var clearScripts = scriptsOn(dump, clear);

            Assertions.assertEquals(1, keepScripts.size(),
                    "keep field should keep only its show script when "
                            + "another field is hidden; got: " + keepScripts);
            Assertions.assertTrue(isShowScript(keepScripts.getFirst()),
                    "keep field's queued script should be the show; got: "
                            + keepScripts.getFirst());
            Assertions.assertEquals(2, clearScripts.size(),
                    "cleared field receives show then hide; got: "
                            + clearScripts);
            Assertions.assertTrue(isShowScript(clearScripts.get(0)),
                    "First script on cleared field is the show; got: "
                            + clearScripts.get(0));
            Assertions.assertTrue(isHideScript(clearScripts.get(1)),
                    "Last script on cleared field is the hide; got: "
                            + clearScripts.get(1));
        }

        @Test
        void showFieldHighlightReappliesOnReattach() {
            // Detach drops the client-side highlight; the controller's
            // attach listener must re-issue addUser on the next attach so
            // the user does not lose the visual cue.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.showFieldHighlight(field);
            drainPendingJs();

            form.remove(field);
            form.add(field);

            var addUserScripts = pendingJsOn(field).stream()
                    .filter(Highlight::isShowScript).toList();
            Assertions.assertEquals(1, addUserScripts.size(),
                    "Re-attach must re-issue exactly one addUser script; "
                            + "got: " + addUserScripts);
        }

        @Test
        void hideFieldHighlightCancelsReapplyOnReattach() {
            // hide removes the attach listener as well as queueing
            // removeUser; a subsequent detach/re-attach must not bring the
            // highlight back.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.showFieldHighlight(field);
            controller.hideFieldHighlight(field);
            drainPendingJs();

            form.remove(field);
            form.add(field);

            var addUserScripts = pendingJsOn(field).stream()
                    .filter(Highlight::isShowScript).toList();
            Assertions.assertEquals(0, addUserScripts.size(),
                    "After hide, re-attach must not re-issue addUser; got: "
                            + addUserScripts);
        }

        @Test
        void repeatedShowDoesNotStackReapplyListeners() {
            // Two showFieldHighlight calls must register exactly one attach
            // listener. Otherwise re-attach would queue duplicate addUser
            // scripts and leak listeners across the field's lifetime.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.showFieldHighlight(field);
            controller.showFieldHighlight(field);
            drainPendingJs();

            form.remove(field);
            form.add(field);

            var addUserScripts = pendingJsOn(field).stream()
                    .filter(Highlight::isShowScript).toList();
            Assertions.assertEquals(1, addUserScripts.size(),
                    "Repeated show calls must collapse to one attach "
                            + "listener; re-attach must re-issue addUser "
                            + "exactly once; got: " + addUserScripts);
        }

        @Test
        void showFieldHighlightAfterHideReinstallsReapply() {
            // show → hide → show on the same field must end with a working
            // attach listener again, because hide removed the previous one
            // and the second show installs a fresh registration.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.showFieldHighlight(field);
            controller.hideFieldHighlight(field);
            controller.showFieldHighlight(field);
            drainPendingJs();

            form.remove(field);
            form.add(field);

            var addUserScripts = pendingJsOn(field).stream()
                    .filter(Highlight::isShowScript).toList();
            Assertions.assertEquals(1, addUserScripts.size(),
                    "Show after hide must reinstall the re-apply listener; "
                            + "re-attach must re-issue addUser exactly once; "
                            + "got: " + addUserScripts);
        }

        // Tie the JS-shape check to the conceptual operation so call
        // sites read as "is this a show / hide?" instead of grepping the
        // raw JS string. The canonical add/remove-user test (above) pins
        // the exact JS contract; these helpers are for follow-on tests
        // that only need to distinguish show from hide.
        private static boolean isShowScript(String script) {
            return script.contains("addUser");
        }

        private static boolean isHideScript(String script) {
            return script.contains("removeUser");
        }

        private List<String> pendingJsOn(HasElement target) {
            return scriptsOn(drainPendingJs(), target);
        }

        // Use when more than one target is inspected from the same dump —
        // dumpPendingJavaScriptInvocations is destructive, so per-target
        // filtering must happen against a single drained list.
        private List<PendingJavaScriptInvocation> drainPendingJs() {
            ui.getInternals().getStateTree()
                    .runExecutionsBeforeClientResponse();
            ui.getInternals().getStateTree().collectChanges(ignore -> {
            });
            return ui.getInternals().dumpPendingJavaScriptInvocations();
        }

        private static List<String> scriptsOn(
                List<PendingJavaScriptInvocation> dump, HasElement target) {
            return dump.stream()
                    .filter(p -> p.getInvocation().getParameters()
                            .contains(target.getElement()))
                    .map(p -> p.getInvocation().getExpression()).toList();
        }

        // Flattened parameter list for every invocation targeted at `target`.
        // Used to assert on the values bound to $0, $1, ... in the queued
        // script expressions.
        private static List<Object> paramsOn(
                List<PendingJavaScriptInvocation> dump, HasElement target) {
            return dump.stream()
                    .filter(p -> p.getInvocation().getParameters()
                            .contains(target.getElement()))
                    .flatMap(p -> p.getInvocation().getParameters().stream())
                    .map(p -> (Object) p).toList();
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
