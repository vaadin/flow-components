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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.binder.HasItems;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.shared.Registration;

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

    @Tag("double-field")
    private static class DoubleField
            extends AbstractField<DoubleField, Double> {
        DoubleField() {
            super(null);
        }

        @Override
        protected void setPresentationValue(Double value) {
        }
    }

    @Tag("bigdec-field")
    private static class BigDecField
            extends AbstractField<BigDecField, BigDecimal> {
        BigDecField() {
            super(null);
        }

        @Override
        protected void setPresentationValue(BigDecimal value) {
        }
    }

    @Tag("bool-field")
    private static class BoolField extends AbstractField<BoolField, Boolean> {
        BoolField() {
            super(false);
        }

        @Override
        protected void setPresentationValue(Boolean value) {
        }
    }

    @Tag("date-field")
    private static class DateField extends AbstractField<DateField, LocalDate> {
        DateField() {
            super(null);
        }

        @Override
        protected void setPresentationValue(LocalDate value) {
        }
    }

    @Tag("datetime-field")
    private static class DateTimeField
            extends AbstractField<DateTimeField, LocalDateTime> {
        DateTimeField() {
            super(null);
        }

        @Override
        protected void setPresentationValue(LocalDateTime value) {
        }
    }

    @Tag("time-field")
    private static class TimeField extends AbstractField<TimeField, LocalTime> {
        TimeField() {
            super(null);
        }

        @Override
        protected void setPresentationValue(LocalTime value) {
        }
    }

    /**
     * String-valued field that also implements {@link HasLabel} and
     * {@link HasHelper}, used by the description-merging test.
     */
    @Tag("labeled-string-field")
    private static class LabeledStringField
            extends AbstractField<LabeledStringField, String>
            implements HasLabel, HasHelper {
        LabeledStringField() {
            super("");
        }

        @Override
        protected void setPresentationValue(String value) {
        }
    }

    /**
     * Single-select field. Implements {@link HasItems} so the controller
     * classifies it as a selection component, and exposes
     * {@code getDataProvider()}/{@code getItemLabelGenerator()} reflectively so
     * {@link FormValueConverter} can read items and labels.
     */
    @Tag("single-select-field")
    private static class SingleSelectField<T> extends
            AbstractField<SingleSelectField<T>, T> implements HasItems<T> {

        private DataProvider<T, ?> provider = DataProvider
                .ofCollection(List.of());
        private ItemLabelGenerator<T> labelGenerator;

        SingleSelectField() {
            super(null);
        }

        @Override
        protected void setPresentationValue(T value) {
        }

        @Override
        public void setItems(Collection<T> items) {
            provider = DataProvider.ofCollection(items);
        }

        @SafeVarargs
        @SuppressWarnings("varargs")
        @Override
        public final void setItems(T... items) {
            setItems(Arrays.asList(items));
        }

        void setDataProvider(DataProvider<T, ?> provider) {
            this.provider = provider;
        }

        public DataProvider<T, ?> getDataProvider() {
            return provider;
        }

        public ItemLabelGenerator<T> getItemLabelGenerator() {
            return labelGenerator;
        }

        public void setItemLabelGenerator(ItemLabelGenerator<T> generator) {
            this.labelGenerator = generator;
        }
    }

    /**
     * Multi-select field. Implements {@link MultiSelect} so the controller
     * classifies it as a multi-select, and exposes the same reflective
     * accessors as {@link SingleSelectField} for items and label generation.
     */
    @Tag("multi-select-field")
    private static class MultiSelectField<T>
            extends AbstractField<MultiSelectField<T>, Set<T>>
            implements MultiSelect<MultiSelectField<T>, T> {

        private ListDataProvider<T> provider = DataProvider
                .ofCollection(List.of());
        private ItemLabelGenerator<T> labelGenerator;

        MultiSelectField() {
            super(Set.of());
        }

        @Override
        protected void setPresentationValue(Set<T> value) {
        }

        @SafeVarargs
        final void setItems(T... items) {
            provider = DataProvider.ofCollection(Arrays.asList(items));
        }

        @Override
        public void updateSelection(Set<T> added, Set<T> removed) {
            var next = new HashSet<>(getValue());
            next.addAll(added);
            next.removeAll(removed);
            setValue(next);
        }

        @Override
        public Set<T> getSelectedItems() {
            return getValue();
        }

        @Override
        public Registration addSelectionListener(
                MultiSelectionListener<MultiSelectField<T>, T> listener) {
            return () -> {
            };
        }

        public ListDataProvider<T> getDataProvider() {
            return provider;
        }

        public ItemLabelGenerator<T> getItemLabelGenerator() {
            return labelGenerator;
        }

        public void setItemLabelGenerator(ItemLabelGenerator<T> generator) {
            this.labelGenerator = generator;
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

    @Nested
    class FormState {

        @Test
        void getFormStateReturnsAllVisibleFieldsInDocumentOrder() {
            var a = new TestField();
            var b = new DoubleField();
            var c = new BoolField();
            var nested = new Div(b, c);
            var form = new Div(a, nested);
            var controller = new FormAIController(form);

            var fields = formStateFields(controller);

            Assertions.assertEquals(3, fields.size());
            Assertions.assertEquals(idOf(a),
                    fields.get(0).get("id").asString());
            Assertions.assertEquals(idOf(b),
                    fields.get(1).get("id").asString());
            Assertions.assertEquals(idOf(c),
                    fields.get(2).get("id").asString());
        }

        @Test
        void getFormStateOmitsIgnoredFields() {
            var visible = new TestField();
            var hidden = new TestField();
            var controller = new FormAIController(new Div(visible, hidden));
            controller.ignore(hidden);

            var fields = formStateFields(controller);

            Assertions.assertEquals(1, fields.size());
            Assertions.assertEquals(idOf(visible),
                    fields.get(0).get("id").asString());
        }

        @Test
        void getFormStateMergesLabelDescriptionAndHelperText() {
            var field = new LabeledStringField();
            field.setLabel("Merchant");
            field.setHelperText("As shown on the receipt");
            var controller = new FormAIController(new Div(field));
            controller.describe(field, "The vendor name");

            var f = formStateFields(controller).get(0);

            Assertions.assertEquals(
                    "Merchant. The vendor name. As shown on the receipt",
                    f.get("description").asString(),
                    "label, registered description, and helper text must be "
                            + "joined in that order");
        }

        @Test
        void getFormStateOmitsBlankDescriptionEntirely() {
            var field = new TestField();
            var controller = new FormAIController(new Div(field));

            var f = formStateFields(controller).get(0);

            Assertions.assertFalse(f.has("description"),
                    "Empty merged description must be omitted, got: " + f);
        }

        @Test
        void getFormStateMapsStringValueTypeToTypeString() {
            assertTypeOnly(typeNodeFor(new TestField()), "string");
        }

        @Test
        void getFormStateMapsDoubleValueTypeToTypeNumber() {
            assertTypeOnly(typeNodeFor(new DoubleField()), "number");
        }

        @Test
        void getFormStateMapsIntegerValueTypeToTypeInteger() {
            assertTypeOnly(typeNodeFor(new IntField()), "integer");
        }

        @Test
        void getFormStateMapsBigDecimalValueTypeToStringWithPattern() {
            assertTypeAndPattern(typeNodeFor(new BigDecField()), "string",
                    "^-?\\d+(\\.\\d+)?$");
        }

        @Test
        void getFormStateMapsBooleanValueTypeToTypeBoolean() {
            assertTypeOnly(typeNodeFor(new BoolField()), "boolean");
        }

        @Test
        void getFormStateMapsLocalDateValueTypeToStringWithDateFormat() {
            assertTypeAndFormat(typeNodeFor(new DateField()), "string", "date");
        }

        @Test
        void getFormStateMapsLocalDateTimeValueTypeToStringWithDateTimeFormat() {
            assertTypeAndFormat(typeNodeFor(new DateTimeField()), "string",
                    "date-time");
        }

        @Test
        void getFormStateMapsLocalTimeValueTypeToStringWithTimeFormat() {
            assertTypeAndFormat(typeNodeFor(new TimeField()), "string", "time");
        }

        @Test
        void getFormStateMapsSingleSelectFieldToStringType() {
            // A selection component without registered options or items
            // surfaces as bare "type": "string" — no enum, no queryable.
            var f = typeNodeFor(new SingleSelectField<String>());

            Assertions.assertEquals("string", f.path("type").asString());
            Assertions.assertTrue(f.path("enum").isMissingNode());
            Assertions.assertTrue(f.path("queryable").isMissingNode());
        }

        @Test
        void getFormStateMapsMultiSelectFieldToArrayWithItemsBlock() {
            var f = typeNodeFor(new MultiSelectField<String>());

            Assertions.assertTrue(f.path("array").asBoolean(),
                    "MultiSelect must be encoded with array=true");
            Assertions.assertEquals("string",
                    f.path("items").path("type").asString());
        }

        @Test
        void getFormStateEncodesEnumForFixedValueOptions() {
            // Fixed valueOptions registered against a selection component
            // surface in the JSON output as enum. The same registration
            // against a non-selection field would not (LLM uses
            // query_field_options instead).
            var combo = new SingleSelectField<String>();
            var controller = new FormAIController(new Div(combo));
            controller.valueOptions(combo, List.of("EUR", "USD", "GBP"));

            var f = formStateFields(controller).get(0);

            Assertions.assertEquals("string", f.path("type").asString());
            Assertions.assertTrue(f.path("queryable").isMissingNode(),
                    "Fixed-options field must not carry queryable=true");
            var values = new ArrayList<String>();
            f.path("enum").forEach(n -> values.add(n.asString()));
            Assertions.assertEquals(List.of("EUR", "USD", "GBP"), values);
        }

        @Test
        void getFormStateEncodesQueryableForBiFunctionValueOptions() {
            var combo = new SingleSelectField<String>();
            var controller = new FormAIController(new Div(combo));
            controller.valueOptions(combo,
                    (filter, limit) -> List.of("Apollo", "Polaris"));

            var f = formStateFields(controller).get(0);

            Assertions.assertTrue(f.path("queryable").asBoolean(),
                    "Queryable-options field must carry queryable=true, "
                            + "got: " + f);
            Assertions.assertTrue(f.path("enum").isMissingNode(),
                    "Queryable-options field must not enumerate options "
                            + "inline, got: " + f);
        }

        @Test
        void getFormStateExposesListDataProviderItemsAsEnum() {
            var combo = new SingleSelectField<String>();
            combo.setItems("alpha", "beta", "gamma");
            var controller = new FormAIController(new Div(combo));

            var f = formStateFields(controller).get(0);

            var values = new ArrayList<String>();
            f.path("enum").forEach(n -> values.add(n.asString()));
            Assertions.assertEquals(List.of("alpha", "beta", "gamma"), values,
                    "ListDataProvider items must populate the enum when no "
                            + "valueOptions hint is registered");
        }

        @Test
        void getFormStateBackendDataProviderProducesNoEnum() {
            var combo = new SingleSelectField<String>();
            combo.setDataProvider(new CallbackDataProvider<String, String>(
                    q -> List.of("a", "b").stream(), q -> 2));
            var controller = new FormAIController(new Div(combo));

            var f = formStateFields(controller).get(0);

            Assertions.assertEquals("string", f.path("type").asString());
            Assertions.assertTrue(f.path("enum").isMissingNode(),
                    "Backend data provider must not contribute an enum");
            Assertions.assertTrue(f.path("queryable").isMissingNode(),
                    "Backend data provider alone must not flag queryable");
        }

        @Test
        void getFormStateRendersCurrentValuesPerType() {
            var text = new TestField();
            text.setValue("Trattoria");
            var integer = new IntField();
            integer.setValue(42);
            var number = new DoubleField();
            number.setValue(58.4);
            var bigDecimal = new BigDecField();
            bigDecimal.setValue(new BigDecimal("58.40"));
            var date = new DateField();
            date.setValue(LocalDate.of(2026, 5, 4));
            var bool = new BoolField();
            bool.setValue(true);
            var combo = new SingleSelectField<String>();
            combo.setItems("Meals", "Travel");
            combo.setValue("Meals");
            var controller = new FormAIController(new Div(text, integer, number,
                    bigDecimal, date, bool, combo));

            var fields = formStateFields(controller);

            Assertions.assertEquals("Trattoria",
                    fields.get(0).get("value").asString());
            Assertions.assertEquals(42, fields.get(1).get("value").asInt());
            Assertions.assertEquals(58.4, fields.get(2).get("value").asDouble(),
                    1e-9);
            Assertions.assertEquals("58.40",
                    fields.get(3).get("value").asString());
            Assertions.assertEquals("2026-05-04",
                    fields.get(4).get("value").asString());
            Assertions.assertTrue(fields.get(5).get("value").asBoolean());
            Assertions.assertEquals("Meals",
                    fields.get(6).get("value").asString());
        }

        @Test
        void getFormStateRendersNullForEmptyValues() {
            var text = new TestField();
            var date = new DateField();
            var multi = new MultiSelectField<String>();
            var controller = new FormAIController(new Div(text, date, multi));

            var fields = formStateFields(controller);

            Assertions.assertTrue(fields.get(0).get("value").isNull(),
                    "Empty text value must serialize as JSON null");
            Assertions.assertTrue(fields.get(1).get("value").isNull(),
                    "Empty date value must serialize as JSON null");
            Assertions.assertTrue(fields.get(2).get("value").isNull(),
                    "Empty multi-select value must serialize as JSON null");
        }

        @Test
        void getFormStateMultiSelectUsesItemsBlock() {
            var multi = new MultiSelectField<String>();
            multi.setItems("a", "b", "c");
            multi.setValue(Set.of("a", "c"));
            var controller = new FormAIController(new Div(multi));

            var f = formStateFields(controller).get(0);

            Assertions.assertTrue(f.path("array").asBoolean());
            Assertions.assertEquals("string",
                    f.path("items").path("type").asString());
            var enumValues = new ArrayList<String>();
            f.path("items").path("enum")
                    .forEach(n -> enumValues.add(n.asString()));
            Assertions.assertTrue(
                    enumValues.containsAll(List.of("a", "b", "c")),
                    "Items block must enumerate the data-provider labels, "
                            + "got: " + enumValues);
            var rendered = new ArrayList<String>();
            f.path("value").forEach(n -> rendered.add(n.asString()));
            Assertions.assertTrue(
                    rendered.containsAll(List.of("a", "c"))
                            && rendered.size() == 2,
                    "Multi-select value must render as a JSON array of label "
                            + "strings, got: " + rendered);
        }

        @Test
        void getFormStateMultiSelectQueryableUsesItemsQueryable() {
            var multi = new MultiSelectField<String>();
            var controller = new FormAIController(new Div(multi));
            controller.valueOptions(multi, (filter, limit) -> List.of("x"),
                    Set::of);

            var f = formStateFields(controller).get(0);

            Assertions.assertTrue(f.path("array").asBoolean());
            Assertions.assertTrue(f.path("items").path("queryable").asBoolean(),
                    "Queryable multi-select must carry queryable=true on the "
                            + "items block, got: " + f);
            Assertions.assertTrue(f.path("items").path("enum").isMissingNode(),
                    "Queryable multi-select must not enumerate options "
                            + "inline, got: " + f);
        }

        @Test
        void getFormStateSchemaIsStaticAndEmpty() {
            var controller = new FormAIController(new Div(new TestField()));

            var schema = findTool(controller.getTools(), "get_form_state")
                    .getParametersSchema();
            var node = json(schema);

            Assertions.assertEquals("object", node.path("type").asString());
            Assertions.assertTrue(node.path("properties").isObject());
            Assertions.assertEquals(0, node.path("properties").size(),
                    "get_form_state must take no parameters");
        }

        @Test
        void getFormStateReflectsLiveLabelChanges() {
            var field = new LabeledStringField();
            field.setLabel("Old");
            var controller = new FormAIController(new Div(field));

            Assertions.assertEquals("Old", formStateFields(controller).get(0)
                    .get("description").asString());

            field.setLabel("New");

            Assertions.assertEquals("New",
                    formStateFields(controller).get(0).get("description")
                            .asString(),
                    "description must be re-read from the live field on each "
                            + "call");
        }

        @Test
        void getFormStateAssignsStableIdAcrossCalls() {
            var field = new TestField();
            var controller = new FormAIController(new Div(field));

            var firstId = formStateFields(controller).get(0).get("id")
                    .asString();
            var secondId = formStateFields(controller).get(0).get("id")
                    .asString();

            Assertions.assertEquals(firstId, secondId);
            Assertions.assertEquals(firstId, idOf(field),
                    "id in get_form_state must match the id stored on the "
                            + "component");
        }

        @Test
        void getToolsAlwaysIncludesFormState() {
            // Unlike query_field_options, the form-state tool is always
            // present — there is no per-field configuration that gates it.
            var controllerNoHints = new FormAIController(new Div());
            Assertions.assertTrue(
                    controllerNoHints.getTools().stream().anyMatch(
                            t -> t.getName().equals("get_form_state")),
                    "get_form_state must be exposed even when the form has "
                            + "no fields");

            var field = new TestField();
            var controllerWithIgnored = new FormAIController(new Div(field));
            controllerWithIgnored.ignore(field);
            Assertions.assertTrue(
                    controllerWithIgnored.getTools().stream().anyMatch(
                            t -> t.getName().equals("get_form_state")),
                    "get_form_state must be exposed even when every field "
                            + "is ignored");
        }

        // ---- helpers scoped to FormState ----

        private JsonNode typeNodeFor(HasValue<?, ?> field) {
            var controller = new FormAIController(new Div((Component) field));
            return formStateFields(controller).get(0);
        }

        private List<JsonNode> formStateFields(FormAIController controller) {
            var result = findTool(controller.getTools(), "get_form_state")
                    .execute(JacksonUtils.createObjectNode());
            var root = json(result);
            var out = new ArrayList<JsonNode>();
            root.path("fields").forEach(out::add);
            return out;
        }

        private String idOf(HasValue<?, ?> field) {
            return (String) ComponentUtil.getData((Component) field,
                    FormAIController.FIELD_ID_KEY);
        }

        private void assertTypeOnly(JsonNode field, String expectedType) {
            Assertions.assertEquals(expectedType, field.path("type").asString(),
                    "Expected type=" + expectedType + " for " + field);
            Assertions.assertTrue(field.path("format").isMissingNode(),
                    "Expected no format for " + field);
            Assertions.assertTrue(field.path("pattern").isMissingNode(),
                    "Expected no pattern for " + field);
        }

        private void assertTypeAndFormat(JsonNode field, String expectedType,
                String expectedFormat) {
            Assertions.assertEquals(expectedType,
                    field.path("type").asString());
            Assertions.assertEquals(expectedFormat,
                    field.path("format").asString());
        }

        private void assertTypeAndPattern(JsonNode field, String expectedType,
                String expectedPattern) {
            Assertions.assertEquals(expectedType,
                    field.path("type").asString());
            Assertions.assertEquals(expectedPattern,
                    field.path("pattern").asString());
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
