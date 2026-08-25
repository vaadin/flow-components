/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.form;

import static com.vaadin.flow.component.ai.form.FormTestSupport.executeQueryFieldOptions;
import static com.vaadin.flow.component.ai.form.FormTestSupport.findTool;
import static com.vaadin.flow.component.ai.form.FormTestSupport.formStateFields;
import static com.vaadin.flow.component.ai.form.FormTestSupport.idOf;
import static com.vaadin.flow.component.ai.form.FormTestSupport.json;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
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
import com.vaadin.flow.component.ai.form.FormTestFields.DoubleField;
import com.vaadin.flow.component.ai.form.FormTestFields.IntField;
import com.vaadin.flow.component.ai.form.FormTestFields.SingleSelectField;
import com.vaadin.flow.component.ai.form.FormTestFields.TestField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;

import tools.jackson.databind.node.ObjectNode;

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

        // The user-interaction guard during a turn is applied on the client
        // only, by the web component as part of the "AI is working" state —
        // see the FieldMarker tests. The controller must never change the
        // field's server-side read-only state, so these tests assert that
        // invariant via isReadOnly().

        @Test
        void onRequestDoesNotChangeServerReadOnlyState() {
            var a = new TestField();
            var b = new TestField();
            var nested = new TestField();
            var form = new Div(a, new Div(b, nested));
            var controller = new FormAIController(form);

            controller.onRequest();

            Assertions.assertFalse(a.isReadOnly(),
                    "The controller must not flip server-side read-only");
            Assertions.assertFalse(b.isReadOnly());
            Assertions.assertFalse(nested.isReadOnly());
        }

        @Test
        void onResponseLeavesServerReadOnlyUntouched() {
            var a = new TestField();
            var b = new TestField();
            var controller = new FormAIController(new Div(a, b));

            controller.onRequest();
            controller.onResponse(null);

            Assertions.assertFalse(a.isReadOnly());
            Assertions.assertFalse(b.isReadOnly());
        }

        @Test
        void applicationReadOnlyIsPreservedAcrossTurn() {
            // A field the application set read-only must stay read-only: the
            // controller never touches the server-side state, so there is no
            // unlock to clobber it.
            var editable = new TestField();
            var appReadOnly = new TestField();
            appReadOnly.setReadOnly(true);
            var controller = new FormAIController(
                    new Div(editable, appReadOnly));

            controller.onRequest();
            controller.onResponse(null);

            Assertions.assertFalse(editable.isReadOnly());
            Assertions.assertTrue(appReadOnly.isReadOnly(),
                    "An application-set read-only field must be left "
                            + "untouched across a turn");
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
            // An empty fixed-options list leaves the field un-fillable;
            // rejected at the options() call site.
            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> config.options(List.<String> of()));
            // A ValueOptions with no options(...) set is rejected at
            // registration.
            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> controller
                            .fieldValueOptions(ValueOptions.forField(field)));
        }

        @Test
        void fieldValueOptionsAcceptsAnyValueType() {
            // fieldValueOptions is generic over the field's value type — an
            // Integer-valued field carries Integer items, and the
            // String.valueOf fallback renders the LLM-facing labels when the
            // field has no item-label generator of its own.
            var field = new IntField();
            var controller = new FormAIController(new Div(field));
            controller.fieldValueOptions(
                    ValueOptions.forField(field).options(List.of(1, 2, 3)));
            controller.onRequest();

            Assertions.assertEquals("1\n2\n3\n",
                    executeQueryFieldOptions(controller, field, "", 10));
        }

        @Test
        void fixedOptionsExposeLabelsThroughQueryTool() {
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
                            .itemLabelGenerator(FormTestFields.Project::code));
            controller.fieldValueOptions(
                    ValueOptions.forField(combo).options(List.of(alpha))
                            .itemLabelGenerator(FormTestFields.Project::name));

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
            // typed forField overload at compile time. This runtime check is
            // for the upcast case: the developer holds a HasValue reference
            // to a MultiSelect instance, so the compiler picks the
            // single-value overload. The check redirects to the typed
            // MultiSelect overload.
            var multiSelect = new FormTestFields.MultiSelectField<String>();
            HasValue<?, java.util.Set<String>> upcast = multiSelect;
            var controller = new FormAIController(new Div(multiSelect));
            var ex = Assertions.assertThrows(IllegalArgumentException.class,
                    () -> controller
                            .fieldValueOptions(ValueOptions.forField(upcast)
                                    .options(List.of(java.util.Set.of("a")))));
            Assertions.assertTrue(ex.getMessage().contains("MultiSelect"),
                    "Rejection must name MultiSelect so the developer can "
                            + "tighten the reference type; got: "
                            + ex.getMessage());
        }

        @Test
        void fieldValueOptionsRejectsCollectionValuedFieldNotImplementingMultiSelect() {
            // Collection-valued fields must implement MultiSelect; otherwise
            // there is no defined aggregation for the resolved items and the
            // controller refuses to register them.
            var field = new FormTestFields.CollectionWithoutMultiSelectField();
            var controller = new FormAIController(new Div(field));
            var ex = Assertions.assertThrows(IllegalArgumentException.class,
                    () -> controller.fieldValueOptions(ValueOptions
                            .forField(field).options(List.of(List.of("a")))));
            Assertions.assertTrue(
                    ex.getMessage().contains("Collection")
                            && ex.getMessage().contains("MultiSelect"),
                    "Rejection must name both 'Collection' and "
                            + "'MultiSelect' so the developer can resolve "
                            + "the mismatch; got: " + ex.getMessage());
        }

        @Test
        void fieldValueOptionsAcceptsTypedMultiSelectFieldWithNonStringElementType() {
            // Counterpart to the Collection-value rejection: a MultiSelect-
            // typed field with a non-String per-element type must remain
            // accepted, even though its empty value is itself a Collection.
            var field = new FormTestFields.MultiSelectField<Integer>();
            var controller = new FormAIController(new Div(field));
            Assertions.assertDoesNotThrow(() -> controller.fieldValueOptions(
                    ValueOptions.forField(field).options(List.of(1, 2))));
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

        @Test
        void fixedOptionsWithDuplicateLabelsWarnsAtRegistration() {
            // Resolution under duplicate labels falls back to first-in-list
            // ordering; the developer needs a registration-time signal so
            // the ambiguity is fixed before the LLM sees it.
            TestLoggerFactory.getTestLogger(FormAIController.class).clearAll();
            var first = new FormTestFields.Project("P-1", "Apollo");
            var dup = new FormTestFields.Project("P-2", "Apollo");
            var combo = new SingleSelectField<FormTestFields.Project>();
            combo.setItemLabelGenerator(FormTestFields.Project::name);
            var controller = new FormAIController(new Div(combo));

            controller.fieldValueOptions(
                    ValueOptions.forField(combo).options(List.of(first, dup)));

            var warnings = TestLoggerFactory
                    .getTestLogger(FormAIController.class).getLoggingEvents()
                    .stream().filter(e -> e.getLevel() == Level.WARN).toList();
            Assertions.assertEquals(1, warnings.size(),
                    "Duplicate-label registration must log exactly one "
                            + "warning; got: " + warnings);
            var formatted = warnings.get(0).getFormattedMessage();
            Assertions.assertTrue(formatted.contains("Apollo"),
                    "Warning must name the offending label so the developer "
                            + "can locate it; got: " + formatted);
            Assertions.assertTrue(formatted.contains("itemLabelGenerator"),
                    "Warning must point at itemLabelGenerator as the "
                            + "disambiguation knob; got: " + formatted);
        }

        @Test
        void fixedOptionsWithUniqueLabelsDoesNotWarn() {
            // Negative guard so the warning doesn't flood logs in the
            // common unique-labels case.
            TestLoggerFactory.getTestLogger(FormAIController.class).clearAll();
            var apollo = new FormTestFields.Project("P-1", "Apollo");
            var vega = new FormTestFields.Project("P-2", "Vega");
            var combo = new SingleSelectField<FormTestFields.Project>();
            combo.setItemLabelGenerator(FormTestFields.Project::name);
            var controller = new FormAIController(new Div(combo));

            controller.fieldValueOptions(ValueOptions.forField(combo)
                    .options(List.of(apollo, vega)));

            var warnings = TestLoggerFactory
                    .getTestLogger(FormAIController.class).getLoggingEvents()
                    .stream().filter(e -> e.getLevel() == Level.WARN).toList();
            Assertions.assertTrue(warnings.isEmpty(),
                    "Unique-label registration must not warn; got: "
                            + warnings);
        }

        @Test
        void queryModeDoesNotWarnAtRegistration() {
            // Query mode can't be checked upfront — items arrive in
            // batches. A registration-time warning would be wrong (we
            // don't know the full set) and a per-batch warning would
            // flood. Stay silent.
            TestLoggerFactory.getTestLogger(FormAIController.class).clearAll();
            var apollo = new FormTestFields.Project("P-1", "Apollo");
            var dup = new FormTestFields.Project("P-2", "Apollo");
            var combo = new SingleSelectField<FormTestFields.Project>();
            combo.setItemLabelGenerator(FormTestFields.Project::name);
            var controller = new FormAIController(new Div(combo));

            controller.fieldValueOptions(ValueOptions.forField(combo)
                    .options((filter, limit) -> List.of(apollo, dup)));

            var warnings = TestLoggerFactory
                    .getTestLogger(FormAIController.class).getLoggingEvents()
                    .stream().filter(e -> e.getLevel() == Level.WARN).toList();
            Assertions.assertTrue(warnings.isEmpty(),
                    "Query-mode registration must not warn; got: " + warnings);
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
    class FieldValueChange {

        @Test
        void listenerFiresWithChangedFieldAfterSuccessfulTurn() {
            var field = new TestField();
            var controller = new FormAIController(new Div(field));
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(events::add);

            controller.onRequest();
            field.setValue("John");
            controller.onResponse(null);

            Assertions.assertEquals(1, events.size(), "Listener must fire "
                    + "exactly once for the single changed field");
            var event = eventFor(events, field);
            Assertions.assertEquals("", event.getOldValue());
            Assertions.assertEquals("John", event.getNewValue());
            Assertions.assertSame(controller, event.getSource(),
                    "Event source must be the controller that produced it");
        }

        @Test
        void listenerNotInvokedWhenNoFieldChanged() {
            var field = new TestField();
            var controller = new FormAIController(new Div(field));
            var invocations = new AtomicInteger();
            controller.addFieldValueChangeListener(
                    e -> invocations.incrementAndGet());

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
            controller.addFieldValueChangeListener(
                    e -> invocations.incrementAndGet());

            controller.onRequest();
            field.setValue("partial");
            controller.onResponse(new RuntimeException("boom"));

            Assertions.assertEquals(0, invocations.get(),
                    "Listener must not fire when the turn ended in error, "
                            + "even if a tool call already wrote to a field");
        }

        @Test
        void onlyChangedFieldsProduceEvents() {
            var changed = new TestField();
            var untouched = new TestField();
            var controller = new FormAIController(new Div(changed, untouched));
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(events::add);

            controller.onRequest();
            changed.setValue("X");
            controller.onResponse(null);

            Assertions.assertEquals(1, events.size(),
                    "Only the changed field must produce an event; got: "
                            + events);
            Assertions.assertTrue(containsEventFor(events, changed));
            Assertions.assertFalse(containsEventFor(events, untouched));
        }

        @Test
        void ignoredFieldsDoNotProduceEventsEvenIfTheirValueChanged() {
            // Application-driven cascades into a field marked ignoreField()
            // must not leak into the event stream — ignoreField() is the
            // application's opt-out from AI-driven tracking on either side
            // of the lifecycle.
            var visible = new TestField();
            var ignored = new TestField();
            visible.addValueChangeListener(e -> ignored.setValue("cascade"));
            var controller = new FormAIController(new Div(visible, ignored));
            controller.ignoreField(ignored);
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(events::add);

            controller.onRequest();
            visible.setValue("primary");
            controller.onResponse(null);

            Assertions.assertTrue(containsEventFor(events, visible));
            Assertions.assertFalse(containsEventFor(events, ignored),
                    "Ignored fields must not produce events; got: " + events);
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
            controller.addFieldValueChangeListener(e -> {
                throw new RuntimeException("listener boom");
            });

            controller.onRequest();
            field.setValue("anything");
            controller.onResponse(null);

            Assertions.assertFalse(field.isReadOnly(),
                    "Field must be unlocked even if a listener threw");
        }

        @Test
        void cascadingChangesProduceSeparateEventsInTheSameTurn() {
            // Cascades through ValueChangeListener are observable in the
            // event stream regardless of who triggered them — pin the
            // symmetry so this doesn't quietly regress to "only AI-driven
            // writes are reported".
            var primary = new TestField();
            var cascaded = new TestField();
            primary.addValueChangeListener(e -> cascaded.setValue("derived"));
            var controller = new FormAIController(new Div(primary, cascaded));
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(events::add);

            controller.onRequest();
            primary.setValue("driver");
            controller.onResponse(null);

            Assertions.assertEquals(2, events.size(),
                    "Both the driver and cascaded fields must produce events; "
                            + "got: " + events);
            Assertions.assertEquals("derived",
                    eventFor(events, cascaded).getNewValue());
        }

        @Test
        void multiSelectSetWithEqualContentDoesNotProduceEvent() {
            var field = new FormTestFields.MultiSelectField<String>();
            field.setValue(Set.of("a", "b"));
            var controller = new FormAIController(new Div(field));
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(events::add);

            controller.onRequest();
            // Same content, different Set instance — Objects.equals true.
            field.setValue(Set.of("b", "a"));
            controller.onResponse(null);

            Assertions.assertTrue(events.isEmpty(),
                    "A multi-select set equal to its previous value must "
                            + "not produce an event; got: " + events);
        }

        @Test
        void multiSelectSetWithDifferentContentProducesEvent() {
            var field = new FormTestFields.MultiSelectField<String>();
            field.setValue(Set.of("a", "b"));
            var controller = new FormAIController(new Div(field));
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(events::add);

            controller.onRequest();
            field.setValue(Set.of("a", "c"));
            controller.onResponse(null);

            var event = eventFor(events, field);
            Assertions.assertEquals(Set.of("a", "b"), event.getOldValue());
            Assertions.assertEquals(Set.of("a", "c"), event.getNewValue());
        }

        @Test
        void eventsArriveInDocumentOrder() {
            var first = new TestField();
            var second = new TestField();
            var third = new TestField();
            var controller = new FormAIController(
                    new Div(first, second, third));
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(events::add);

            controller.onRequest();
            third.setValue("c");
            first.setValue("a");
            second.setValue("b");
            controller.onResponse(null);

            Assertions.assertEquals(List.of(first, second, third),
                    events.stream().map(FieldValueChangeEvent::getField)
                            .toList(),
                    "Events must fire in document order regardless of the "
                            + "order writes happened in");
        }

        @Test
        void nullPreTurnValueIsReportedFaithfully() {
            var field = new FormTestFields.DateField();
            var controller = new FormAIController(new Div(field));
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(events::add);

            controller.onRequest();
            field.setValue(LocalDate.of(2026, 1, 1));
            controller.onResponse(null);

            var event = eventFor(events, field);
            Assertions.assertNull(event.getOldValue(),
                    "Pre-turn null must round-trip as null");
            Assertions.assertEquals(LocalDate.of(2026, 1, 1),
                    event.getNewValue());
        }

        @Test
        void clearingAValueToNullProducesEvent() {
            // Inverse of nullPreTurnValueIsReportedFaithfully: pre-turn
            // non-null → post-turn null must surface as a change so
            // applications can react (e.g. clear the marker).
            var field = new FormTestFields.DateField();
            field.setValue(LocalDate.of(2026, 1, 1));
            var controller = new FormAIController(new Div(field));
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(events::add);

            controller.onRequest();
            field.setValue(null);
            controller.onResponse(null);

            var event = eventFor(events, field);
            Assertions.assertEquals(LocalDate.of(2026, 1, 1),
                    event.getOldValue());
            Assertions.assertNull(event.getNewValue(),
                    "Clearing to null must surface as the new value");
        }

        @Test
        void multipleListenersAllFire() {
            var field = new TestField();
            var controller = new FormAIController(new Div(field));
            var first = new ArrayList<FieldValueChangeEvent>();
            var second = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(first::add);
            controller.addFieldValueChangeListener(second::add);

            controller.onRequest();
            field.setValue("X");
            controller.onResponse(null);

            Assertions.assertEquals(1, first.size(),
                    "First listener must fire once for the changed field");
            Assertions.assertEquals(1, second.size(),
                    "Second listener must also fire once");
            Assertions.assertSame(first.get(0), second.get(0),
                    "Both listeners must receive the same event instance, so "
                            + "they see identical field and values");
        }

        @Test
        void nullListenerThrows() {
            var controller = new FormAIController(new Div(new TestField()));

            Assertions.assertThrows(NullPointerException.class,
                    () -> controller.addFieldValueChangeListener(null));
        }

        @Test
        void registrationRemoveStopsFutureCalls() {
            var field = new TestField();
            var controller = new FormAIController(new Div(field));
            var calls = new AtomicInteger();
            var registration = controller
                    .addFieldValueChangeListener(e -> calls.incrementAndGet());

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
            controller.addFieldValueChangeListener(e -> {
                throw new RuntimeException("first throws");
            });
            controller.addFieldValueChangeListener(
                    e -> followingCalls.incrementAndGet());

            controller.onRequest();
            field.setValue("X");
            controller.onResponse(null);

            Assertions.assertEquals(1, followingCalls.get(),
                    "An exception from one listener must not prevent the "
                            + "next listener from firing");
        }

        @Test
        void listenerExceptionDoesNotPreventSubsequentEventsInSameTurn() {
            // When a listener throws on the first field's event, both that
            // throwing listener and any other listener must still fire for
            // the second field's event. Pin both halves so a regression
            // that swallows further events for either party would surface.
            var first = new TestField();
            var second = new TestField();
            var controller = new FormAIController(new Div(first, second));
            var throwerCalls = new AtomicInteger();
            var followerCalls = new AtomicInteger();
            controller.addFieldValueChangeListener(e -> {
                throwerCalls.incrementAndGet();
                throw new RuntimeException("always throws");
            });
            controller.addFieldValueChangeListener(
                    e -> followerCalls.incrementAndGet());

            controller.onRequest();
            first.setValue("a");
            second.setValue("b");
            controller.onResponse(null);

            Assertions.assertEquals(2, throwerCalls.get(),
                    "The throwing listener must still be invoked for every "
                            + "changed field, even after its own prior "
                            + "invocation threw");
            Assertions.assertEquals(2, followerCalls.get(),
                    "A non-throwing follower must also receive every event "
                            + "even when a prior listener throws on each");
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
            registration.set(controller.addFieldValueChangeListener(e -> {
                selfRemovingCalls.incrementAndGet();
                registration.get().remove();
            }));
            controller.addFieldValueChangeListener(
                    e -> followingCalls.incrementAndGet());

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
        void fieldRevealedMidTurnProducesEvent() {
            // Visibility-cascade headline: a hidden field that gets
            // revealed-and-written during a single turn must surface as an
            // event. Otherwise the LLM's effect on the form would be
            // silently underreported.
            var primary = new TestField();
            var conditional = new TestField();
            conditional.setVisible(false);
            primary.addValueChangeListener(e -> {
                conditional.setVisible(true);
                conditional.setValue("derived");
            });
            var controller = new FormAIController(
                    new Div(primary, conditional));
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(events::add);

            controller.onRequest();
            primary.setValue("driver");
            controller.onResponse(null);

            var event = eventFor(events, conditional);
            Assertions.assertEquals("", event.getOldValue(),
                    "Old value must reflect the field's pre-turn value");
            Assertions.assertEquals("derived", event.getNewValue());
        }

        @Test
        void hiddenFieldRevealedAndChangedReportsRealOldValue() {
            // When the hidden field already had a non-null value
            // (e.g. bound to a bean), the event must report the real
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
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(events::add);

            controller.onRequest();
            primary.setValue("driver");
            controller.onResponse(null);

            var event = eventFor(events, conditional);
            Assertions.assertEquals("preset", event.getOldValue(),
                    "Pre-turn value of a hidden field must round-trip into "
                            + "the event, not a spurious null");
            Assertions.assertEquals("derived", event.getNewValue());
        }

        @Test
        void fieldRevealedMidTurnWithUnchangedValueProducesNoEvent() {
            // False-positive guard: revealing a hidden field without
            // writing to it is not a change and must not produce an event.
            var primary = new TestField();
            var conditional = new TestField();
            conditional.setValue("preset");
            conditional.setVisible(false);
            primary.addValueChangeListener(e -> conditional.setVisible(true));
            var controller = new FormAIController(
                    new Div(primary, conditional));
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(events::add);

            controller.onRequest();
            primary.setValue("driver");
            controller.onResponse(null);

            Assertions.assertFalse(containsEventFor(events, conditional),
                    "Revealing a hidden field without changing its value "
                            + "must not produce an event");
        }

        @Test
        void fieldRevealedAndFilledInSameTurnProducesEvent() {
            var controlling = new TestField();
            var conditional = new TestField();
            conditional.setVisible(false);
            controlling
                    .addValueChangeListener(e -> conditional.setVisible(true));
            var controller = new FormAIController(
                    new Div(controlling, conditional));
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(events::add);

            controller.onRequest();
            controlling.setValue("business"); // reveals the conditional field
            conditional.setValue("cost-center-42"); // AI fills the revealed one
            controller.onResponse(null);

            Assertions.assertTrue(containsEventFor(events, conditional),
                    "A field revealed and filled within the same turn must "
                            + "produce an event; got: " + events);
        }

        @Test
        void fieldAddedAndFilledInSameTurnProducesEvent() {
            // A field that does not exist when onRequest snapshots but is
            // added to the form mid-turn (e.g. by a controlling field
            // revealing a new panel) and filled in the same turn must
            // still produce an event.
            var controlling = new TestField();
            var added = new TestField();
            var form = new Div(controlling);
            // Adding the conditional field is application-driven, triggered
            // by the controlling field's value change.
            controlling.addValueChangeListener(e -> form.add(added));
            var controller = new FormAIController(form);
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(events::add);

            controller.onRequest();
            controlling.setValue("business"); // adds the new field to the form
            added.setValue("cost-center-42"); // AI fills the newly-added field
            controller.onResponse(null);

            Assertions.assertTrue(containsEventFor(events, added),
                    "A field added to the form and filled within the same "
                            + "turn must produce an event; got: " + events);
            Assertions.assertEquals(added.getEmptyValue(),
                    eventFor(events, added).getOldValue(),
                    "A field with no pre-turn snapshot must report its empty "
                            + "value as the old value");
        }

        @Test
        void fieldAddedMidTurnWithoutWriteProducesNoEvent() {
            // False-positive guard: a field added to the form mid-turn that
            // keeps its empty value was not changed by the turn and must not
            // produce an event, even though it has no pre-turn snapshot.
            var controlling = new TestField();
            var added = new TestField();
            var form = new Div(controlling);
            controlling.addValueChangeListener(e -> form.add(added));
            var controller = new FormAIController(form);
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(events::add);

            controller.onRequest();
            controlling.setValue("business"); // adds the new field, no write
            controller.onResponse(null);

            Assertions.assertFalse(containsEventFor(events, added),
                    "A field added mid-turn but never written must not "
                            + "produce an event; got: " + events);
        }

        @Test
        void eventSourceIsTheControllerForEveryEventInATurn() {
            // getSource() is the only way for a listener to recover the
            // controller without capturing it. Pin that every event in a
            // multi-event turn returns the same controller, not, say, null
            // on the second event or a different instance.
            var first = new TestField();
            var second = new TestField();
            var controller = new FormAIController(new Div(first, second));
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(events::add);

            controller.onRequest();
            first.setValue("a");
            second.setValue("b");
            controller.onResponse(null);

            Assertions.assertEquals(2, events.size(),
                    "Expected two events for two changed fields");
            for (var event : events) {
                Assertions.assertSame(controller, event.getSource(),
                        "Every event must carry the same controller as its "
                                + "source; got: " + event.getSource());
            }
        }

        @Test
        void eventDispatchVisitsAllListenersBeforeMovingToNextEvent() {
            // Every listener fires in registration order for event 1
            // before any listener fires for event 2. Pinning this lets
            // listeners depend on each other's side effects within an
            // event (e.g. listener A annotates the source, listener B
            // reads the annotation) without leaking across events.
            var first = new TestField();
            var second = new TestField();
            var controller = new FormAIController(new Div(first, second));
            var order = new ArrayList<String>();
            controller.addFieldValueChangeListener(
                    e -> order.add("A:" + e.getNewValue()));
            controller.addFieldValueChangeListener(
                    e -> order.add("B:" + e.getNewValue()));

            controller.onRequest();
            first.setValue("a");
            second.setValue("b");
            controller.onResponse(null);

            Assertions.assertEquals(List.of("A:a", "B:a", "A:b", "B:b"), order,
                    "Listeners must visit each event in registration order, "
                            + "then advance to the next event: got " + order);
        }

        @Test
        void writingToFieldInsideListenerDoesNotPoisonOtherEventsValues() {
            // The diff is materialised before any listener runs, so a
            // listener writing to a tracked field cannot retroactively
            // alter another event's values. Otherwise events would carry
            // values that don't match the LLM's effect — they'd carry
            // whatever the listener happened to write.
            var first = new TestField();
            var second = new TestField();
            var controller = new FormAIController(new Div(first, second));
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(e -> {
                events.add(e);
                if (e.getField() == first) {
                    second.setValue("listener-overwrote");
                }
            });

            controller.onRequest();
            first.setValue("from-llm");
            second.setValue("from-llm-too");
            controller.onResponse(null);

            var secondEvent = eventFor(events, second);
            Assertions.assertEquals("", secondEvent.getOldValue(),
                    "The second event's oldValue must reflect the field's "
                            + "value at turn start, not anything the listener "
                            + "wrote during the turn");
            Assertions.assertEquals("from-llm-too", secondEvent.getNewValue(),
                    "The second event's newValue must reflect the LLM's "
                            + "write, not the side effect of the listener "
                            + "that ran for the first event");
        }

        @Test
        void listenerAddedDuringDispatchDoesNotReceiveCurrentTurnEvents() {
            // Snapshot-per-turn semantics: a listener that registers itself
            // from inside another listener's handler must not start
            // receiving events until the NEXT turn. Otherwise a one-shot
            // self-registering listener pattern would observe a partial
            // view of the current turn's changes.
            var first = new TestField();
            var second = new TestField();
            var controller = new FormAIController(new Div(first, second));
            var lateCalls = new AtomicInteger();
            controller.addFieldValueChangeListener(e -> {
                if (e.getField() == first) {
                    controller.addFieldValueChangeListener(
                            ev -> lateCalls.incrementAndGet());
                }
            });

            controller.onRequest();
            first.setValue("a");
            second.setValue("b");
            controller.onResponse(null);

            Assertions.assertEquals(0, lateCalls.get(),
                    "Listener registered mid-dispatch must not receive any "
                            + "of the current turn's remaining events");

            controller.onRequest();
            first.setValue("c");
            controller.onResponse(null);

            Assertions.assertEquals(1, lateCalls.get(),
                    "Listener registered in the previous turn must receive "
                            + "events from subsequent turns");
        }

        @Test
        void removedListenerStillReceivesRestOfCurrentTurnEvents() {
            // Snapshot-per-turn semantics, opposite direction: a listener
            // that removes itself from inside its own handler must still
            // receive the rest of the current turn's events. The cleanup
            // takes effect only on the next turn.
            var first = new TestField();
            var second = new TestField();
            var controller = new FormAIController(new Div(first, second));
            var calls = new ArrayList<HasValue<?, ?>>();
            var registration = new AtomicReference<Registration>();
            registration.set(controller.addFieldValueChangeListener(e -> {
                calls.add(e.getField());
                if (e.getField() == first) {
                    registration.get().remove();
                }
            }));

            controller.onRequest();
            first.setValue("a");
            second.setValue("b");
            controller.onResponse(null);

            Assertions.assertEquals(List.of(first, second), calls,
                    "A listener that removes itself during event 1 must "
                            + "still receive event 2 of the same turn");
        }

        private static FieldValueChangeEvent eventFor(
                List<FieldValueChangeEvent> events, HasValue<?, ?> field) {
            return events.stream().filter(e -> e.getField() == field)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError(
                            "No FieldValueChangeEvent for field " + field
                                    + " in " + events));
        }

        private static boolean containsEventFor(
                List<FieldValueChangeEvent> events, HasValue<?, ?> field) {
            return events.stream().anyMatch(e -> e.getField() == field);
        }
    }

    @Nested
    class FieldMarker {

        // Marker integration is exercised through the marker element the
        // controller appends to the field. We assert on that element — its
        // presence and its properties — rather than on DOM side effects,
        // because the real visual change happens in the web component, on the
        // client. Tests use a minimal UI so the fields live in a real state
        // tree.

        private static final String MARKER_TAG = "vaadin-ai-field-marker";

        private UI ui;

        @BeforeEach
        void attachUi() {
            ui = new UI();
            var mockSession = Mockito.mock(VaadinSession.class);
            ui.getInternals().setSession(mockSession);
        }

        @Test
        void markListenersSerializeWithoutController() {
            // The controller is deliberately not Serializable (it is restored
            // via reconnect()), while the listeners a mark installs live on
            // the field and are serialized with the UI. They must therefore
            // not capture the controller. The form is kept detached so the
            // write stops at the form instead of the test's mocked session.
            var field = new TestField();
            var form = new Div(field);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);
            Assertions.assertEquals(1, markersOn(field).size());

            Assertions.assertDoesNotThrow(() -> {
                try (var out = new ObjectOutputStream(
                        OutputStream.nullOutputStream())) {
                    out.writeObject(form);
                }
            }, "Serializing a marked field must not reach the controller");
        }

        @Test
        void turnAfterUserEditRemarksField() {
            // A mark-clear-remark sequence: the AI fills the field, the user
            // edits it away, and a later turn fills it again. The field must
            // end up marked by exactly one marker.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("ai");
            controller.onResponse(null);
            field.setValue("user edit"); // clears the marker

            controller.onRequest();
            field.setValue("ai again");
            controller.onResponse(null);

            Assertions.assertEquals(1, markersOn(field).size(),
                    "A turn after the marker was cleared must leave the field "
                            + "marked by exactly one marker");
        }

        @Test
        void markerSurvivesReattach() {
            // The marker lives in the field's element hierarchy, so Flow
            // re-creates it on the client when the field re-enters the DOM —
            // the user does not lose the visual cue.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);
            form.remove(field);
            form.add(field);

            Assertions.assertEquals(1, markersOn(field).size(),
                    "A detach/re-attach must leave exactly one marker on the "
                            + "field");
        }

        @Test
        void markerDoesNotReturnAfterUserEdit() {
            // A user edit removes the marker for good; a later detach/re-attach
            // must not bring it back.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);
            field.setValue("user edit");
            form.remove(field);
            form.add(field);

            Assertions.assertEquals(List.of(), markersOn(field),
                    "After the marker is cleared, a detach/re-attach must not "
                            + "bring it back");
        }

        @Test
        void markerIsNotAComponentChildOfTheField() {
            // The marker is a plain element, so a composite field's own child
            // components — and the controller's field discovery — must not see
            // it.
            var inner = new TestField();
            var field = new CompositeField(inner);
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);

            Assertions.assertEquals(List.of(inner),
                    field.getChildren().toList(),
                    "The marker must not appear among the field's child "
                            + "components");
            Assertions.assertEquals(List.of(field),
                    FormFieldDiscovery.collectFields(form),
                    "The marker must not be discovered as a field");
        }

        @Test
        void defaultI18nCarriesNoTexts() {
            // With no texts configured, the marker carries none either, so the
            // web component applies its built-in defaults.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);

            Assertions.assertTrue(i18nOn(field).isEmpty(),
                    "An unconfigured controller must set no texts; got: "
                            + i18nOn(field));
        }

        @Test
        void fieldMarkerI18nOmitsUnsetTexts() {
            // A text left null falls back to the web component's default, so
            // it must not appear in the marker's texts at all.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form).setFieldMarkerI18n(
                    new FieldMarkerI18n().setMessage("Vain viesti"));

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);
            var i18n = i18nOn(field);

            Assertions.assertEquals("Vain viesti",
                    i18n.get("message").asString());
            for (var key : List.of("revert", "badgeLabel", "badgeTooltip",
                    "confidence")) {
                Assertions.assertFalse(i18n.has(key),
                        "Unset text must be omitted from the marker: " + key);
            }
        }

        @Test
        void fieldMarkerI18nCarriesConfidenceTexts() {
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form)
                    .setFieldMarkerI18n(new FieldMarkerI18n()
                            .setConfidence(new FieldMarkerI18n.Confidence()
                                    .setLow("Epävarma").setMedium("Melko varma")
                                    .setHigh("Varma")));

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);
            var confidence = i18nOn(field).get("confidence");

            Assertions.assertEquals("Epävarma",
                    confidence.get("low").asString());
            Assertions.assertEquals("Melko varma",
                    confidence.get("medium").asString());
            Assertions.assertEquals("Varma", confidence.get("high").asString());
        }

        @Test
        void fieldMarkerI18nOmitsUnsetConfidenceTexts() {
            // A level text left null falls back to the web component's
            // default, so it must not appear in the confidence texts at all.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form)
                    .setFieldMarkerI18n(new FieldMarkerI18n()
                            .setConfidence(new FieldMarkerI18n.Confidence()
                                    .setLow("Epävarma")));

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);
            var confidence = i18nOn(field).get("confidence");

            Assertions.assertEquals("Epävarma",
                    confidence.get("low").asString());
            for (var key : List.of("medium", "high")) {
                Assertions.assertFalse(confidence.has(key),
                        "Unset text must be omitted from the marker: " + key);
            }
        }

        @Test
        void emptyFieldMarkerI18nCarriesNoTexts() {
            // An i18n object with no texts set is equivalent to none at all:
            // the marker carries no texts, so the web component falls back to
            // its defaults for every one of them.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form)
                    .setFieldMarkerI18n(new FieldMarkerI18n());

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);

            Assertions.assertTrue(i18nOn(field).isEmpty(),
                    "An empty i18n must set no texts; got: " + i18nOn(field));
        }

        @Test
        void markerCarriesConfiguredI18nTexts() {
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form)
                    .setFieldMarkerI18n(new FieldMarkerI18n()
                            .setMessage("Tekoäly täytti tämän kentän")
                            .setRevert("Kumoa")
                            .setBadgeLabel("Tekoälyn täyttämä arvo")
                            .setBadgeTooltip("Avaa tiedot"));

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);
            var i18n = i18nOn(field);

            Assertions.assertEquals("Tekoäly täytti tämän kentän",
                    i18n.get("message").asString());
            Assertions.assertEquals("Kumoa", i18n.get("revert").asString());
            Assertions.assertEquals("Tekoälyn täyttämä arvo",
                    i18n.get("badgeLabel").asString());
            Assertions.assertEquals("Avaa tiedot",
                    i18n.get("badgeTooltip").asString());
        }

        @Test
        void i18nTextsAreRefreshedOnLaterTurns() {
            // Texts set after a field was first marked reach it when the next
            // turn marks it again, without the marker being replaced.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("first");
            controller.onResponse(null);

            controller.setFieldMarkerI18n(
                    new FieldMarkerI18n().setMessage("Päivitetty viesti"));
            controller.onRequest();
            field.setValue("second");
            controller.onResponse(null);

            Assertions.assertEquals(1, markersOn(field).size());
            Assertions.assertEquals("Päivitetty viesti",
                    i18nOn(field).get("message").asString());
        }

        @Test
        void userEditClearsOnlyThatFieldsMarker() {
            // Pin behavioural independence: with two fields marked by the same
            // turn, editing one must leave the other marked.
            var keep = new TestField();
            var edited = new TestField();
            var form = new Div(keep, edited);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            keep.setValue("filled");
            edited.setValue("filled");
            controller.onResponse(null);

            edited.setValue("user edit");

            Assertions.assertEquals(1, markersOn(keep).size(),
                    "Clearing one field's marker must leave the other marked");
            Assertions.assertEquals(List.of(), markersOn(edited));
        }

        @Test
        void turnStartAppliesWorkingStateToEditableFields() {
            // Every editable field enters the "AI is working" state at turn
            // start, regardless of whether the AI ends up changing it. The
            // shimmer and the client-side read-only guard are the web
            // component's responsibility, so the controller only sets the
            // state.
            var changed = new TestField();
            var untouched = new TestField();
            var form = new Div(changed, untouched);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();

            for (var field : List.of(changed, untouched)) {
                Assertions.assertTrue(isWorking(field),
                        "Every editable field must enter the working state at "
                                + "turn start");
            }
            // The server-side read-only state is never touched.
            Assertions.assertFalse(changed.isReadOnly());
            Assertions.assertFalse(untouched.isReadOnly());
        }

        @Test
        void turnStartSkipsFieldsTheAiCannotWrite() {
            // Disabled, application-read-only and ignored fields are not
            // "worked on" — the AI cannot write them — so they get no working
            // state and, crucially, their read-only guard is never toggled.
            var editable = new TestField();
            var disabled = new TestField();
            disabled.setEnabled(false);
            var appReadOnly = new TestField();
            appReadOnly.setReadOnly(true);
            var ignored = new TestField();
            var form = new Div(editable, disabled, appReadOnly, ignored);
            ui.add(form);
            var controller = new FormAIController(form);
            controller.ignoreField(ignored);

            controller.onRequest();

            Assertions.assertTrue(isWorking(editable));
            for (var field : List.of(disabled, appReadOnly, ignored)) {
                Assertions.assertEquals(List.of(), markersOn(field),
                        "A field the AI cannot write must not enter the "
                                + "working state; offending field: " + field);
            }
        }

        @Test
        void turnEndClearsWorkingStateFromChangedField() {
            // The working state — shimmer and the client read-only guard — is
            // removed when the turn ends, leaving the mark visible.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);

            Assertions.assertEquals(1, markersOn(field).size(),
                    "A changed field must stay marked after the turn");
            Assertions.assertFalse(isWorking(field),
                    "A changed field's working state must be cleared at turn "
                            + "end");
        }

        @Test
        void turnEndRemovesWorkingMarkerFromUnchangedField() {
            // The marker on an unchanged field only carried the working state,
            // so it goes away with it rather than leaving a mark on a value the
            // AI never touched.
            var field = new TestField();
            field.setValue("kept");
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            controller.onResponse(null);

            Assertions.assertEquals(List.of(), markersOn(field),
                    "An unchanged field must be left without a marker");
        }

        @Test
        void turnEndKeepsExistingMarkOnUnchangedField() {
            // A field marked by an earlier turn keeps its mark through a turn
            // that does not change it: the working state only hid the badge,
            // and clearing the state brings it back.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);

            controller.onRequest();
            controller.onResponse(null); // second turn changes nothing

            Assertions.assertEquals(1, markersOn(field).size(),
                    "A turn that changes nothing must not clear an existing "
                            + "mark");
            Assertions.assertFalse(isWorking(field));
        }

        @Test
        void workingStateClearedEvenWhenTurnFails() {
            // A failed turn must not strand the shimmer on the form.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            controller.onResponse(new RuntimeException("boom"));

            Assertions.assertEquals(List.of(), markersOn(field),
                    "The working state must clear even when the turn fails");
        }

        @Test
        void failedTurnLeavesExistingMarkIntact() {
            // The working state hides an existing mark; a failed turn must
            // bring it back rather than drop it.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);

            controller.onRequest();
            controller.onResponse(new RuntimeException("boom"));

            Assertions.assertEquals(1, markersOn(field).size(),
                    "A failed turn must leave an existing mark in place");
            Assertions.assertFalse(isWorking(field),
                    "A failed turn must clear the working state so the mark "
                            + "shows again");
        }

        @Test
        void workingStateSurvivesReattach() {
            // Detaching a field mid-turn must not drop the working state: the
            // marker carrying it comes back with the field.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest(); // turn in progress, working state applied
            form.remove(field);
            form.add(field);

            Assertions.assertTrue(isWorking(field),
                    "A detach/re-attach during a turn must keep the working "
                            + "state");
        }

        @Test
        void workingStateDoesNotReturnAfterTurnEnd() {
            // Once the turn has ended, a detach/re-attach must not bring the
            // working state back.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            controller.onResponse(null);
            form.remove(field);
            form.add(field);

            Assertions.assertEquals(List.of(), markersOn(field),
                    "After the turn ends, a detach/re-attach must not bring "
                            + "the working state back");
        }

        @Test
        void workingStateClearedOnFieldRemovedMidTurn() {
            // A field removed from the form mid-turn is out of reach of any
            // form-tree walk when the turn ends, but its marker still carries
            // the working state. Re-adding the field later must not bring back
            // a stale shimmer over a field nothing is working on.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            form.remove(field);
            controller.onResponse(null);
            form.add(field);

            Assertions.assertEquals(List.of(), markersOn(field),
                    "A field removed mid-turn must have its working state "
                            + "cleared at turn end");
        }

        @Test
        void turnStartClearsStaleWorkingStateFromUnwritableField() {
            // A turn that never reaches onResponse (a dropped connection)
            // leaves the working state applied. If the field is no longer
            // writable when the next turn starts, that turn skips it — so the
            // stale shimmer has to be cleared up front rather than left to
            // linger over a field nothing is working on.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest(); // turn 1 starts and never completes
            field.setVisible(false);
            controller.onRequest(); // turn 2 starts

            Assertions.assertEquals(List.of(), markersOn(field),
                    "A new turn must clear the working state left behind by an "
                            + "unfinished one");
        }

        @Test
        void fieldSetReadOnlyMidTurn_reassertsClientReadOnlyAtTurnEnd() {
            // A field switched to server-side read-only mid-turn (e.g. by a
            // value-change listener reacting to an AI write) needs its client
            // readonly re-asserted at turn end: the working guard held the
            // client property at true, so Flow dropped the server's own write
            // as a no-op. The sibling left editable must get no script.
            var readOnly = new TestField();
            var editable = new TestField();
            var form = new Div(readOnly, editable);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            readOnly.setValue("filled");
            readOnly.setReadOnly(true);
            drainPendingJs(); // isolate the scripts queued at turn end
            controller.onResponse(null);

            var dump = drainPendingJs();
            var scripts = scriptsOn(dump, readOnly);
            Assertions.assertEquals(1, scripts.size(),
                    "Turn end must queue exactly one re-assert script on the "
                            + "read-only field; got: " + scripts);
            Assertions.assertTrue(
                    scripts.getFirst().contains("readonly = true"),
                    "The script must re-assert the client-side readonly; "
                            + "got: " + scripts.getFirst());
            Assertions.assertEquals(List.of(), scriptsOn(dump, editable),
                    "No script must be queued on a field left editable");
        }

        @Test
        void unchangedFieldSetReadOnlyMidTurn_alsoGetsReassert() {
            // The re-assert depends only on the field's read-only state at
            // turn end, not on whether the AI changed its value.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setReadOnly(true); // no AI write to the field
            drainPendingJs();
            controller.onResponse(null);

            var scripts = scriptsOn(drainPendingJs(), field);
            Assertions.assertEquals(1, scripts.size(),
                    "Turn end must queue the re-assert even when the AI did "
                            + "not change the field; got: " + scripts);
            Assertions
                    .assertTrue(scripts.getFirst().contains("readonly = true"));
        }

        @Test
        void fieldReadOnlyBeforeTurn_getsNoReassert() {
            // A field that was read-only when the turn started never entered
            // the working state, so its client readonly was never held by the
            // guard and no re-assert must be queued.
            var field = new TestField();
            field.setReadOnly(true);
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            drainPendingJs();
            controller.onResponse(null);

            Assertions.assertEquals(List.of(),
                    scriptsOn(drainPendingJs(), field),
                    "No re-assert must be queued on a field that was "
                            + "read-only before the turn started");
        }

        @Test
        void revertDuringTurnKeepsMarkerForWorkingState() {
            // The badge is hidden while the AI works, but a revert event can
            // still arrive from the client just as a turn starts. Clearing the
            // mark must not take the shimmer with it — the marker carries both.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);

            controller.onRequest();
            fireRevert(field);

            Assertions.assertTrue(isWorking(field),
                    "Clearing the mark mid-turn must keep the working state");
        }

        @Test
        void changedFieldIsMarkedAutomatically() {
            // A turn that changes a field marks it without any wiring on the
            // application's side.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);

            Assertions.assertEquals(1, markersOn(field).size(),
                    "A field changed during a turn must be marked "
                            + "automatically");
        }

        @Test
        void unchangedFieldIsNotMarked() {
            // A turn that changes one field must not mark its untouched
            // sibling.
            var changed = new TestField();
            var untouched = new TestField();
            untouched.setValue("kept");
            var form = new Div(changed, untouched);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            changed.setValue("filled");
            controller.onResponse(null);

            Assertions.assertEquals(1, markersOn(changed).size());
            Assertions.assertEquals(List.of(), markersOn(untouched),
                    "An unchanged field must not be marked");
        }

        @Test
        void fieldRevealedDuringTurnIsMarked() {
            // A field hidden at turn start gets no working state, so nothing
            // has put a marker on it yet. When the AI reveals and fills it
            // during the turn — the cascade case — the turn-end marking is
            // what has to add one.
            var trigger = new TestField();
            var revealed = new TestField();
            revealed.setVisible(false);
            var form = new Div(trigger, revealed);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();

            Assertions.assertEquals(List.of(), markersOn(revealed),
                    "A hidden field must not enter the working state");

            trigger.setValue("business");
            revealed.setVisible(true);
            revealed.setValue("cascaded");
            controller.onResponse(null);

            Assertions.assertEquals(1, markersOn(revealed).size(),
                    "A field revealed and filled during the turn must be "
                            + "marked at turn end");
        }

        @Test
        void userEditAfterTurnClearsTheMarker() {
            // A turn marks the field; a subsequent user edit must clear
            // the marker so a stale "AI filled this" cue doesn't linger.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("ai");
            controller.onResponse(null);

            field.setValue("edited by user");

            Assertions.assertEquals(List.of(), markersOn(field),
                    "Editing a marked field must clear the marker");
        }

        @Test
        void aiWritesDuringTurnDoNotClearTheMarker() {
            // The AI may change an already-marked field again on a later
            // turn. Those in-turn writes must not trip the auto-hide listener.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("first");
            controller.onResponse(null);

            controller.onRequest();
            field.setValue("second"); // AI write while a turn is in progress
            controller.onResponse(null);

            Assertions.assertEquals(1, markersOn(field).size(),
                    "An AI write during a turn must not clear the marker");
        }

        @Test
        void userEditAfterMarkerClearedKeepsFieldUnmarked() {
            // Once the marker is cleared by a user edit, its value-change
            // listener is gone, so further edits neither re-clear nor re-mark.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("ai");
            controller.onResponse(null);
            field.setValue("first edit"); // clears the marker

            field.setValue("second edit");

            Assertions.assertEquals(List.of(), markersOn(field),
                    "Editing after the marker was cleared must leave the field "
                            + "unmarked");
            Assertions.assertEquals("second edit", field.getValue());
        }

        @Test
        void revertEventRestoresPreTurnValueAndClearsMarker() {
            // A turn that changes a field marks it automatically. The
            // marker's ai-field-revert event must then restore the field's
            // pre-turn value and clear the marker.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            field.setValue("old");
            controller.onRequest();
            field.setValue("new");
            controller.onResponse(null);

            fireRevert(field);

            Assertions.assertEquals("old", field.getValue(),
                    "Revert must restore the field's pre-turn value");
            Assertions.assertEquals(List.of(), markersOn(field),
                    "Revert must clear the marker");
        }

        @Test
        void revertDuringTurnIsNotAttributedToAi() {
            // The popover can be open from before a turn started, so a revert
            // can arrive while a new turn is running. The turn-end diff must
            // not treat the user's revert as an AI change — that would re-mark
            // the field with the very value the user just discarded as its
            // revert value.
            var field = new TestField();
            field.setValue("original");
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);

            controller.onRequest(); // snapshots "filled"
            fireRevert(field); // restores "original" mid-turn
            controller.onResponse(null); // the AI writes nothing

            Assertions.assertEquals("original", field.getValue());
            Assertions.assertEquals(List.of(), markersOn(field),
                    "A mid-turn revert must not re-mark the field at turn "
                            + "end");
        }

        @Test
        void aiWriteAfterMidTurnRevertComparesAgainstRevertedValue() {
            // When the AI does write the field after a mid-turn revert, the
            // new mark's revert value must be the value the revert restored,
            // not the AI value from the previous turn.
            var field = new TestField();
            field.setValue("original");
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("first");
            controller.onResponse(null);

            controller.onRequest();
            fireRevert(field); // restores "original" mid-turn
            field.setValue("second"); // the AI writes the field again
            controller.onResponse(null);

            Assertions.assertEquals(1, markersOn(field).size());
            fireRevert(field);
            Assertions.assertEquals("original", field.getValue(),
                    "Revert must restore the value the mid-turn revert "
                            + "restored, not the previous turn's AI value");
        }

        @Test
        void revertRestoresValueFromBeforeFirstAiChangeAcrossTurns() {
            // The AI may change the same field over several turns. Revert must
            // restore the value from before the FIRST change, not the value the
            // field held at the start of the most recent turn.
            var field = new TestField();
            field.setValue("original");
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("first");
            controller.onResponse(null);

            controller.onRequest();
            field.setValue("second");
            controller.onResponse(null);

            fireRevert(field);

            Assertions.assertEquals("original", field.getValue(),
                    "Revert must restore the value from before the AI's first "
                            + "change, not the most recent turn's pre-turn "
                            + "value");
        }

        @Test
        void revertAfterUserEditRestoresValueFromBeforeTheLaterFill() {
            // Clearing the marker must drop the field's revert value with it.
            // Otherwise the next fill keeps the stale one and revert jumps back
            // past a value the user typed themselves.
            var field = new TestField();
            field.setValue("original");
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("first ai value");
            controller.onResponse(null);

            field.setValue("user typed"); // clears the marker

            controller.onRequest();
            field.setValue("second ai value");
            controller.onResponse(null);

            fireRevert(field);

            Assertions.assertEquals("user typed", field.getValue(),
                    "Revert must restore the value from before the latest "
                            + "fill, not one the user has since replaced");
        }

        @Test
        void revertClearsMarkerWhenValueAlreadyEqualsThePreFillValue() {
            // The AI changed the field and then changed it back on a later
            // turn, so restoring the pre-fill value writes what the field
            // already holds and fires no value-change event. Revert must clear
            // the marker on its own rather than relying on that event.
            var field = new TestField();
            field.setValue("original");
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("detour");
            controller.onResponse(null);

            controller.onRequest();
            field.setValue("original"); // the AI puts the original value back
            controller.onResponse(null);

            fireRevert(field);

            Assertions.assertEquals("original", field.getValue());
            Assertions.assertEquals(List.of(), markersOn(field),
                    "Revert must clear the marker even when restoring the "
                            + "pre-fill value changes nothing");
        }

        @Test
        void revertClearsFieldWhosePreFillValueWasNull() {
            // Some fields reject setValue(null), so a null pre-fill value is
            // restored by clearing the field. DoubleField starts out null.
            var field = new DoubleField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue(42.0);
            controller.onResponse(null);

            fireRevert(field);

            Assertions.assertNull(field.getValue(),
                    "Revert must restore a null pre-fill value by clearing the "
                            + "field");
        }

        @Test
        void revertKeepsNullPreFillValueAcrossTurns() {
            // A null pre-fill value must survive later turns like any other:
            // the mark, not the stored value, is the record of "already
            // captured", so a second turn must not replace null with its own
            // pre-turn value.
            var field = new DoubleField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue(42.0);
            controller.onResponse(null);

            controller.onRequest();
            field.setValue(43.0);
            controller.onResponse(null);

            fireRevert(field);

            Assertions.assertNull(field.getValue(),
                    "Revert must restore the null pre-fill value even after "
                            + "several turns changed the field");
        }

        @Test
        void fieldMarkerDefaultsToEnabled() {
            var form = new Div(new TestField());
            ui.add(form);
            var controller = new FormAIController(form);

            Assertions.assertTrue(controller.isFieldMarkerEnabled(),
                    "Automatic marking must be on by default");

            controller.setFieldMarkerEnabled(false);

            Assertions.assertFalse(controller.isFieldMarkerEnabled(),
                    "The opt-out must be reflected by the getter");
        }

        @Test
        void fieldMarkerOffLeavesChangedFieldUnmarked() {
            // The opt-out for applications that mark the AI's edits themselves,
            // or not at all.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form)
                    .setFieldMarkerEnabled(false);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);

            Assertions.assertEquals(List.of(), markersOn(field),
                    "With automatic marking off, a changed field must be "
                            + "left unmarked");
        }

        @Test
        void fieldMarkerOffKeepsWorkingState() {
            // The opt-out covers the persistent mark only. The working state —
            // shimmer and the client read-only guard — still protects the user
            // from typing into a field the AI is about to overwrite, and still
            // takes its marker with it at turn end.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form)
                    .setFieldMarkerEnabled(false);

            controller.onRequest();

            Assertions.assertTrue(isWorking(field),
                    "The working state must apply regardless of the automatic "
                            + "marking setting");

            field.setValue("filled");
            controller.onResponse(null);

            Assertions.assertEquals(List.of(), markersOn(field),
                    "The marker that only carried the working state must go at "
                            + "turn end");
        }

        @Test
        void fieldMarkerOffKeepsMarkFromEarlierTurn() {
            // Turning the automatic marking off does not retract marks
            // already shown, and a later turn's working state must hand them
            // back rather than drop them.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("first");
            controller.onResponse(null);

            controller.setFieldMarkerEnabled(false);
            controller.onRequest();
            field.setValue("second");
            controller.onResponse(null);

            Assertions.assertEquals(1, markersOn(field).size(),
                    "A mark from an earlier turn must survive the opt-out");
            Assertions.assertFalse(isWorking(field),
                    "The working state must be cleared at turn end");
        }

        @Test
        void reEnablingFieldMarkerMarksLaterTurns() {
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form)
                    .setFieldMarkerEnabled(false);

            controller.onRequest();
            field.setValue("unmarked");
            controller.onResponse(null);

            controller.setFieldMarkerEnabled(true);
            controller.onRequest();
            field.setValue("marked");
            controller.onResponse(null);

            Assertions.assertEquals(1, markersOn(field).size(),
                    "Re-enabling automatic marking must mark the changes "
                            + "of subsequent turns");
        }

        @Test
        void fieldMarkerOffStillFiresFieldValueChangeListener() {
            // The change events are independent of the marker, so an
            // application that opts out still learns what the AI changed.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form)
                    .setFieldMarkerEnabled(false);
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(events::add);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);

            Assertions.assertEquals(1, events.size(),
                    "The change listener must fire regardless of the automatic "
                            + "marking setting");
            Assertions.assertSame(field, events.get(0).getField());
            Assertions.assertEquals("filled", events.get(0).getNewValue());
        }

        @Test
        void contentProviderDefaultsToNull() {
            var controller = new FormAIController(new Div(new TestField()));

            Assertions.assertNull(controller.getFieldMarkerContentProvider(),
                    "No content provider must be set by default");

            FieldMarkerContentProvider provider = change -> null;
            controller.setFieldMarkerContentProvider(provider);

            Assertions.assertSame(provider,
                    controller.getFieldMarkerContentProvider(),
                    "The getter must reflect the set provider");
        }

        @Test
        void providerContentIsCarriedByMarkerAndHandedToClient() {
            // The content component must travel as a virtual child of the
            // marker — never as a DOM child, whose slot the web component's
            // own rendering owns — and reach the web component through a
            // `content` property assignment.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var content = new Div();
            var controller = new FormAIController(form)
                    .setFieldMarkerContentProvider(change -> content);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);

            var marker = requireMarkerOn(field);
            Assertions.assertTrue(content.getElement().isVirtualChild(),
                    "The content element must be a virtual child");
            Assertions.assertEquals(marker, content.getElement().getParent(),
                    "The content element must be carried by the marker");
            Assertions.assertEquals(0, marker.getChildCount(),
                    "The content must not appear among the marker's DOM "
                            + "children");
            Assertions.assertEquals(1,
                    contentScriptsOn(drainPendingJs(), content).size(),
                    "The content must be assigned to the marker's content "
                            + "property");
        }

        @Test
        void noProviderQueuesNoContentScript() {
            // Applications not using the feature must not pay for it with
            // client traffic on every marking.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);

            Assertions.assertEquals(List.of(),
                    contentScriptsOwnedBy(drainPendingJs(),
                            requireMarkerOn(field)),
                    "Without a provider, no content script must be queued");
        }

        @Test
        void providerReturningNullAddsNoContent() {
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form)
                    .setFieldMarkerContentProvider(change -> null);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);

            Assertions.assertEquals(List.of(),
                    contentScriptsOwnedBy(drainPendingJs(),
                            requireMarkerOn(field)),
                    "A provider returning null must not queue a content "
                            + "script");
        }

        @Test
        void refillReplacesMarkerContent() {
            // A re-filled field's content describes the new fill; the previous
            // content component must be released, not stacked.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var first = new Div();
            var second = new Div();
            var next = new AtomicReference<Component>(first);
            var controller = new FormAIController(form)
                    .setFieldMarkerContentProvider(change -> next.get());

            controller.onRequest();
            field.setValue("one");
            controller.onResponse(null);
            drainPendingJs();

            next.set(second);
            controller.onRequest();
            field.setValue("two");
            controller.onResponse(null);

            var marker = requireMarkerOn(field);
            Assertions.assertNull(first.getElement().getParentNode(),
                    "The replaced content must be released from the marker");
            Assertions.assertEquals(marker, second.getElement().getParent(),
                    "The new content must be carried by the marker");
            Assertions.assertEquals(1,
                    contentScriptsOn(drainPendingJs(), second).size(),
                    "The new content must be assigned to the marker");
        }

        @Test
        void sameContentInstanceIsNotReapplied() {
            // A provider that keeps one component per field must not cause a
            // re-assignment on every turn.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var content = new Div();
            var controller = new FormAIController(form)
                    .setFieldMarkerContentProvider(change -> content);

            controller.onRequest();
            field.setValue("one");
            controller.onResponse(null);
            drainPendingJs();

            controller.onRequest();
            field.setValue("two");
            controller.onResponse(null);

            Assertions.assertEquals(List.of(),
                    contentScriptsOwnedBy(drainPendingJs(),
                            requireMarkerOn(field)),
                    "Re-marking with the same content instance must not queue "
                            + "another assignment");
            Assertions.assertEquals(requireMarkerOn(field),
                    content.getElement().getParent(),
                    "The content must still be carried by the marker");
        }

        @Test
        void removedProviderClearsContentOnNextFill() {
            // Content from an earlier fill must not describe a later one the
            // provider no longer covers.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var content = new Div();
            var controller = new FormAIController(form)
                    .setFieldMarkerContentProvider(change -> content);

            controller.onRequest();
            field.setValue("one");
            controller.onResponse(null);
            drainPendingJs();

            controller.setFieldMarkerContentProvider(null);
            controller.onRequest();
            field.setValue("two");
            controller.onResponse(null);

            Assertions.assertNull(content.getElement().getParentNode(),
                    "The stale content must be released from the marker");
            Assertions.assertEquals(1,
                    contentScriptsOwnedBy(drainPendingJs(),
                            requireMarkerOn(field)).size(),
                    "The marker's content property must be cleared");
        }

        @Test
        void throwingProviderStillMarksFieldWithoutContent() {
            // A misbehaving provider must not cost the user the marker — or
            // the revert control it carries.
            TestLoggerFactory.getTestLogger(FormAIController.class).clearAll();
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form)
                    .setFieldMarkerContentProvider(change -> {
                        throw new IllegalStateException("boom");
                    });

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);

            var marker = requireMarkerOn(field);
            Assertions.assertEquals(List.of(),
                    contentScriptsOwnedBy(drainPendingJs(), marker),
                    "A throwing provider must be treated as returning no "
                            + "content");
            var warnings = TestLoggerFactory
                    .getTestLogger(FormAIController.class).getLoggingEvents()
                    .stream().filter(e -> e.getLevel() == Level.WARN).toList();
            Assertions.assertEquals(1, warnings.size(),
                    "The provider failure must be logged; got: " + warnings);
        }

        @Test
        void attachedProviderContentMarksFieldWithoutContentAndTurnGoesOn() {
            // A provider handing out a component that already sits somewhere
            // must be rejected like a throwing provider: logged, the field
            // marked without content — and above all the rest of the turn
            // must go on, marking the remaining fields and firing the change
            // events.
            TestLoggerFactory.getTestLogger(FormAIController.class).clearAll();
            var first = new TestField();
            var second = new TestField();
            var form = new Div(first, second);
            ui.add(form);
            var attached = new Div();
            ui.add(attached);
            var controller = new FormAIController(form)
                    .setFieldMarkerContentProvider(
                            change -> change.getField() == first ? attached
                                    : null);
            var events = new ArrayList<FieldValueChangeEvent>();
            controller.addFieldValueChangeListener(events::add);

            controller.onRequest();
            first.setValue("one");
            second.setValue("two");
            controller.onResponse(null);

            var marker = requireMarkerOn(first);
            requireMarkerOn(second);
            Assertions.assertEquals(2, events.size(),
                    "The change listeners must still fire for the whole turn");
            Assertions.assertEquals(List.of(),
                    contentScriptsOwnedBy(drainPendingJs(), marker),
                    "The attached component must not be applied as content");
            Assertions.assertEquals(ui.getElement(),
                    attached.getElement().getParent(),
                    "The rejected component must be left where it was");
            var warnings = TestLoggerFactory
                    .getTestLogger(FormAIController.class).getLoggingEvents()
                    .stream().filter(e -> e.getLevel() == Level.WARN).toList();
            Assertions.assertEquals(1, warnings.size(),
                    "The rejected content must be logged; got: " + warnings);
        }

        @Test
        void markingWithoutProviderLogsNoWarning() {
            // Having no provider is the default, not a failure — marking
            // must not spam a warning per marked field.
            TestLoggerFactory.getTestLogger(FormAIController.class).clearAll();
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);

            requireMarkerOn(field);
            var warnings = TestLoggerFactory
                    .getTestLogger(FormAIController.class).getLoggingEvents()
                    .stream().filter(e -> e.getLevel() == Level.WARN).toList();
            Assertions.assertEquals(List.of(), warnings,
                    "Marking without a provider must not log warnings");
        }

        @Test
        void userEditReleasesMarkerContent() {
            // The content goes away with the mark: once the user edits the
            // field, the marker and the content it carried are gone.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var content = new Div();
            var controller = new FormAIController(form)
                    .setFieldMarkerContentProvider(change -> content);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);
            field.setValue("user edit");

            Assertions.assertEquals(List.of(), markersOn(field));
            Assertions.assertNull(content.getElement().getParentNode(),
                    "The content must be released from the discarded marker");
        }

        @Test
        void revertReleasesMarkerContent() {
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var content = new Div();
            var controller = new FormAIController(form)
                    .setFieldMarkerContentProvider(change -> content);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);

            fireRevert(field);

            Assertions.assertEquals(List.of(), markersOn(field));
            Assertions.assertNull(content.getElement().getParentNode(),
                    "The content must be released from the discarded marker");
        }

        @Test
        void revertDuringTurnReleasesContentFromRetainedMarker() {
            // A revert while a later turn runs keeps the marker for the
            // working state, but the mark — and with it the content — is
            // cleared. The retained marker must not keep carrying a content
            // component that nothing tracks anymore.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var content = new Div();
            var controller = new FormAIController(form)
                    .setFieldMarkerContentProvider(change -> content);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);
            drainPendingJs();

            controller.onRequest();
            fireRevert(field);

            Assertions.assertNull(content.getElement().getParentNode(),
                    "The content must be released although the marker stays "
                            + "for the working state");
            Assertions.assertEquals(1,
                    contentScriptsOwnedBy(drainPendingJs(),
                            requireMarkerOn(field)).size(),
                    "The retained marker's content property must be cleared");

            controller.onResponse(null);

            Assertions.assertEquals(List.of(), markersOn(field),
                    "The marker that only carried the working state must go "
                            + "at turn end");
        }

        @Test
        void reattachReassignsMarkerContent() {
            // Flow re-creates the marker and the content element from the
            // state tree when the field re-enters the DOM, but the property
            // assignment is a one-off script — it must be queued again.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var content = new Div();
            var controller = new FormAIController(form)
                    .setFieldMarkerContentProvider(change -> content);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);
            drainPendingJs();

            form.remove(field);
            form.add(field);

            Assertions.assertEquals(1,
                    contentScriptsOn(drainPendingJs(), content).size(),
                    "A re-attach must re-assign the content to the marker");
        }

        @Test
        void reattachWithoutContentQueuesNoScript() {
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var controller = new FormAIController(form);

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);
            drainPendingJs();

            form.remove(field);
            form.add(field);

            Assertions.assertEquals(List.of(),
                    contentScriptsOwnedBy(drainPendingJs(),
                            requireMarkerOn(field)),
                    "A mark without content must not queue a content script "
                            + "on re-attach");
        }

        @Test
        void contentProviderRunsBeforeChangeListeners() {
            // The listener Javadoc promises the marking — content included —
            // is done by the time listeners run, so a listener can rely on
            // the popover being complete.
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var calls = new ArrayList<String>();
            var controller = new FormAIController(form)
                    .setFieldMarkerContentProvider(change -> {
                        calls.add("provider");
                        return null;
                    });
            controller.addFieldValueChangeListener(
                    event -> calls.add("listener"));

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);

            Assertions.assertEquals(List.of("provider", "listener"), calls,
                    "The provider must run before the change listeners");
        }

        @Test
        void contentProviderNotCalledWhenMarkerDisabled() {
            var field = new TestField();
            var form = new Div(field);
            ui.add(form);
            var calls = new AtomicInteger();
            var controller = new FormAIController(form)
                    .setFieldMarkerEnabled(false)
                    .setFieldMarkerContentProvider(change -> {
                        calls.incrementAndGet();
                        return null;
                    });

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);

            Assertions.assertEquals(0, calls.get(),
                    "With marking off there is no popover to fill, so the "
                            + "provider must not be called");
        }

        @Test
        void markWithContentSerializesWithoutController() {
            // The content component and the attach listener re-asserting it
            // persist on the field, so they must not capture the controller
            // any more than the mark's other listeners do.
            var field = new TestField();
            var form = new Div(field);
            var controller = new FormAIController(form)
                    .setFieldMarkerContentProvider(change -> new Div());

            controller.onRequest();
            field.setValue("filled");
            controller.onResponse(null);
            Assertions.assertEquals(1, markersOn(field).size());

            Assertions.assertDoesNotThrow(() -> {
                try (var out = new ObjectOutputStream(
                        OutputStream.nullOutputStream())) {
                    out.writeObject(form);
                }
            }, "Serializing a marked field with content must not reach the "
                    + "controller");
        }

        // The controller's server-invoked scripts — the read-only re-assert
        // and the marker content assignment — are asserted by dumping the
        // UI's pending JavaScript invocations to pin exactly when each is
        // queued. The dump is destructive, so tests inspecting more than one
        // field must filter a single drained list.
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

        private static final String CONTENT_ASSIGNMENT = "this.content = $0";

        /**
         * @return the content-assignment scripts queued with the given content
         *         element as a parameter
         */
        private static List<String> contentScriptsOn(
                List<PendingJavaScriptInvocation> dump, HasElement content) {
            return scriptsOn(dump, content).stream().filter(
                    expression -> expression.contains(CONTENT_ASSIGNMENT))
                    .toList();
        }

        /**
         * @return the content-assignment scripts queued with the given marker
         *         element as {@code this}. Unlike {@link #contentScriptsOn},
         *         matches by the invocation's owner, so it also catches an
         *         assignment whose parameter is {@code null} — the
         *         content-clearing form.
         */
        private static List<String> contentScriptsOwnedBy(
                List<PendingJavaScriptInvocation> dump, Element marker) {
            return dump.stream().filter(p -> p.getOwner() == marker.getNode())
                    .map(p -> p.getInvocation().getExpression())
                    .filter(expression -> expression
                            .contains(CONTENT_ASSIGNMENT))
                    .toList();
        }

        // Dispatch the marker's revert event server-side so tests can drive
        // the revert path without a real client.
        private static void fireRevert(Component field) {
            var element = field.getElement();
            element.getNode().getFeature(ElementListenerMap.class)
                    .fireEvent(new DomEvent(element, "ai-field-revert",
                            JacksonUtils.createObjectNode()));
        }

        /**
         * @return the field's marker elements — normally at most one; the list
         *         form lets a test pin that no second marker is ever stacked on
         *         a field
         */
        private static List<Element> markersOn(Component field) {
            return field.getElement().getChildren()
                    .filter(child -> MARKER_TAG.equals(child.getTag()))
                    .toList();
        }

        private static Element requireMarkerOn(Component field) {
            var markers = markersOn(field);
            Assertions.assertEquals(1, markers.size(),
                    "Expected exactly one marker on the field; got: "
                            + markers);
            return markers.getFirst();
        }

        /**
         * @return whether the field's marker is in the "AI is working" state;
         *         {@code false} when the field has no marker
         */
        private static boolean isWorking(Component field) {
            return markersOn(field).stream()
                    .anyMatch(marker -> marker.getProperty("working", false));
        }

        /**
         * @return the texts set on the field's marker
         */
        private static ObjectNode i18nOn(Component field) {
            return (ObjectNode) requireMarkerOn(field).getPropertyRaw("i18n");
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
