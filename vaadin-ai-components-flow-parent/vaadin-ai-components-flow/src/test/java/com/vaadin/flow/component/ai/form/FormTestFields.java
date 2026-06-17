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
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.data.binder.HasItems;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.HasDataView;
import com.vaadin.flow.data.provider.HasLazyDataView;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.shared.Registration;

/**
 * Minimal {@link HasValue} test fixtures shared across the form-controller
 * tests. Each class is the smallest implementation that exercises one
 * classification path (value type, marker interface, raw-class hierarchy)
 * without pulling in a concrete Vaadin input component module — that keeps this
 * test module compile-light and makes the classifier contract testable in
 * isolation from the real component classes.
 */
final class FormTestFields {

    private FormTestFields() {
    }

    /**
     * Base class for the simple {@link AbstractField}-derived test fixtures.
     * Concentrates the {@code setPresentationValue} no-op in one place — test
     * fixtures have no DOM presentation to update.
     */
    private abstract static class StubField<C extends AbstractField<C, T>, T>
            extends AbstractField<C, T> {
        StubField(T defaultValue) {
            super(defaultValue);
        }

        @Override
        protected final void setPresentationValue(T value) {
            // No DOM in tests, so there is nothing to render.
        }
    }

    /**
     * Minimal {@link HasValue} component used by the discovery tests.
     * {@link AbstractField} ships with flow-server, so exercising discovery
     * does not require any concrete Vaadin input component module.
     */
    @Tag("test-field")
    static class TestField extends StubField<TestField, String> {
        TestField() {
            super("");
        }
    }

    /**
     * Composite field that is both {@link HasValue} and {@link HasComponents} —
     * used to verify that discovery stops at the field and does not descend
     * into the field's internal composition.
     */
    @Tag("composite-field")
    static class CompositeField extends StubField<CompositeField, String>
            implements HasComponents {
        CompositeField(Component... children) {
            super("");
            add(children);
        }
    }

    /**
     * Integer-valued field used to exercise {@code fieldValueOptions} with a
     * non-{@link String} value type, where {@code toValue} must convert the
     * chosen label into the field's actual value type.
     */
    @Tag("int-field")
    static class IntField extends StubField<IntField, Integer> {
        IntField() {
            super(0);
        }
    }

    @Tag("double-field")
    static class DoubleField extends StubField<DoubleField, Double> {
        DoubleField() {
            super(null);
        }
    }

    @Tag("bigint-field")
    static class BigIntField extends StubField<BigIntField, BigInteger> {
        BigIntField() {
            super(null);
        }
    }

    @Tag("bigdec-field")
    static class BigDecField extends StubField<BigDecField, BigDecimal> {
        BigDecField() {
            super(null);
        }
    }

    @Tag("bool-field")
    static class BoolField extends StubField<BoolField, Boolean> {
        BoolField() {
            super(false);
        }
    }

    @Tag("date-field")
    static class DateField extends StubField<DateField, LocalDate> {
        DateField() {
            super(null);
        }
    }

    @Tag("datetime-field")
    static class DateTimeField extends StubField<DateTimeField, LocalDateTime> {
        DateTimeField() {
            super(null);
        }
    }

    @Tag("time-field")
    static class TimeField extends StubField<TimeField, LocalTime> {
        TimeField() {
            super(null);
        }
    }

    @Tag("long-field")
    static class LongField extends StubField<LongField, Long> {
        LongField() {
            super(null);
        }
    }

    @Tag("short-field")
    static class ShortField extends StubField<ShortField, Short> {
        ShortField() {
            super(null);
        }
    }

    @Tag("byte-field")
    static class ByteField extends StubField<ByteField, Byte> {
        ByteField() {
            super(null);
        }
    }

    @Tag("float-field")
    static class FloatField extends StubField<FloatField, Float> {
        FloatField() {
            super(null);
        }
    }

    /**
     * Collection-valued field that does not implement {@link MultiSelect}. Used
     * to verify the controller rejects this shape — Collection-valued fields
     * must implement {@code MultiSelect} to be registered via
     * {@code fieldValueOptions(...)}.
     */
    @Tag("collection-without-multiselect-field")
    static class CollectionWithoutMultiSelectField
            extends StubField<CollectionWithoutMultiSelectField, List<String>> {
        CollectionWithoutMultiSelectField() {
            super(List.of());
        }
    }

    /**
     * String-valued field that also implements {@link HasLabel} and
     * {@link HasHelper}, used by the description-merging test.
     */
    @Tag("labeled-string-field")
    static class LabeledStringField
            extends StubField<LabeledStringField, String>
            implements HasLabel, HasHelper {
        LabeledStringField() {
            super("");
        }
    }

    /**
     * String-valued field that implements {@link HasValidator}. The default
     * validator can be swapped at runtime so individual tests can pin the
     * validation outcome without rebuilding the field hierarchy. Setting a raw
     * validator via {@link #setDefaultValidator} also lets tests exercise
     * pathological returns ({@code null} validator, {@code null} result, blank
     * error message) for the controller's defensive paths.
     */
    @Tag("validated-field")
    static class ValidatedField extends StubField<ValidatedField, String>
            implements HasValidator<String> {

        private Validator<String> validator = Validator.alwaysPass();

        ValidatedField() {
            super("");
        }

        void rejectAllWith(String message) {
            this.validator = (value, ctx) -> ValidationResult.error(message);
        }

        void setDefaultValidator(Validator<String> validator) {
            this.validator = validator;
        }

        @Override
        public Validator<String> getDefaultValidator() {
            return validator;
        }
    }

    /**
     * Field whose {@link #getValue()} throws — used to verify the per-field
     * error fallback in {@code get_form_state}.
     */
    @Tag("throwing-field")
    static class ThrowingField extends StubField<ThrowingField, String> {
        ThrowingField() {
            super("");
        }

        @Override
        public String getValue() {
            throw new RuntimeException("boom");
        }
    }

    /**
     * Domain-typed item record used to exercise label-generator-driven
     * rendering of selection options. Non-String item types are what makes the
     * {@link ItemLabelGenerator} path observable in tests.
     */
    record Project(String code, String name) {
    }

    /**
     * Non-generic interface that pre-binds {@link HasValue}'s value type to
     * {@link Integer}. Used to exercise the {@code findHasValueValueArg} walk
     * when an intermediate interface in the hierarchy is not parameterized: the
     * recursion must propagate the resolved type out of the raw-class branch
     * instead of dropping it.
     */
    interface IntegerSelectableField
            extends HasValue<HasValue.ValueChangeEvent<Integer>, Integer> {
    }

    /**
     * Field that implements {@link IntegerSelectableField} (a raw class in the
     * {@code getGenericInterfaces()} view of this class) instead of
     * {@code HasValue<?, Integer>} directly. Without the recursive
     * {@code return found;} in {@code findHasValueValueArg}, the walk would
     * find {@code Integer} but discard it, leaving the field classified as
     * {@link FormFieldType#STRING}.
     */
    @Tag("integer-via-non-generic")
    static class IntegerViaNonGenericInterfaceField extends Component
            implements IntegerSelectableField {

        private Integer value;

        @Override
        public void setValue(Integer value) {
            this.value = value;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public Registration addValueChangeListener(
                HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<Integer>> listener) {
            return () -> {
            };
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }

        @Override
        public void setReadOnly(boolean readOnly) {
            // Read-only state is not exercised by these tests.
        }

        @Override
        public boolean isRequiredIndicatorVisible() {
            return false;
        }

        @Override
        public void setRequiredIndicatorVisible(boolean visible) {
            // Required-indicator state is not exercised by these tests.
        }
    }

    /**
     * A {@link Component} that implements {@link HasValue} directly — not via
     * {@link AbstractField}. Used to verify that
     * {@link FormFieldType#classify(HasValue)} resolves the value type when the
     * {@code HasValue<?, V>} parameterization sits at the class declaration
     * itself rather than several layers up the hierarchy.
     */
    @Tag("direct-integer-field")
    static class DirectIntegerField extends Component
            implements HasValue<DirectIntegerField.ChangeEvent, Integer> {

        private Integer value;

        @Override
        public void setValue(Integer value) {
            this.value = value;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public Registration addValueChangeListener(
                HasValue.ValueChangeListener<? super ChangeEvent> listener) {
            return () -> {
            };
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }

        @Override
        public void setReadOnly(boolean readOnly) {
            // Read-only state is not exercised by these tests.
        }

        @Override
        public boolean isRequiredIndicatorVisible() {
            return false;
        }

        @Override
        public void setRequiredIndicatorVisible(boolean visible) {
            // Required-indicator state is not exercised by these tests.
        }

        static class ChangeEvent implements HasValue.ValueChangeEvent<Integer> {
            @Override
            public HasValue<?, Integer> getHasValue() {
                return null;
            }

            @Override
            public boolean isFromClient() {
                return false;
            }

            @Override
            public Integer getOldValue() {
                return null;
            }

            @Override
            public Integer getValue() {
                return null;
            }
        }
    }

    /**
     * Single-select field. Implements {@link HasItems} so the controller
     * classifies it as a selection component, and exposes
     * {@code getDataProvider()}/{@code getItemLabelGenerator()} reflectively so
     * {@link FormValueConverter} can read items and labels.
     */
    @Tag("single-select-field")
    static class SingleSelectField<T> extends StubField<SingleSelectField<T>, T>
            implements HasItems<T> {

        private DataProvider<T, ?> provider = DataProvider
                .ofCollection(List.of());
        private ItemLabelGenerator<T> labelGenerator;

        SingleSelectField() {
            super(null);
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

        @SuppressWarnings("unused")
        public DataProvider<T, ?> getDataProvider() {
            return provider;
        }

        @SuppressWarnings("unused")
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
    static class MultiSelectField<T>
            extends StubField<MultiSelectField<T>, Set<T>>
            implements MultiSelect<MultiSelectField<T>, T> {

        private ListDataProvider<T> provider = DataProvider
                .ofCollection(List.of());
        private ItemLabelGenerator<T> labelGenerator;

        MultiSelectField() {
            super(Set.of());
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

        @SuppressWarnings("unused")
        public ListDataProvider<T> getDataProvider() {
            return provider;
        }

        @SuppressWarnings("unused")
        public ItemLabelGenerator<T> getItemLabelGenerator() {
            return labelGenerator;
        }

        @SuppressWarnings("unused")
        public void setItemLabelGenerator(ItemLabelGenerator<T> generator) {
            this.labelGenerator = generator;
        }
    }

    /**
     * Minimal field that implements {@link HasLazyDataView} but
     * <strong>not</strong> {@link HasItems}. Real Vaadin selection components
     * (e.g. {@code ComboBox}) follow this shape — they expose the data-view
     * marker interfaces but not {@code HasItems}. The classifier must recognise
     * such fields as {@link FormFieldType#SINGLE_SELECT} via the data-view
     * markers; falling back to {@link HasItems} alone would misclassify them as
     * {@link FormFieldType#STRING}, suppressing the {@code enum} block built
     * from the backing {@link ListDataProvider}.
     */
    @Tag("lazy-data-view-field")
    static class LazyDataViewField extends StubField<LazyDataViewField, String>
            implements
            HasLazyDataView<String, String, com.vaadin.flow.data.provider.LazyDataView<String>> {

        private ListDataProvider<String> provider = DataProvider
                .ofCollection(List.of());

        LazyDataViewField() {
            super("");
        }

        @Override
        public com.vaadin.flow.data.provider.LazyDataView<String> setItems(
                com.vaadin.flow.data.provider.BackEndDataProvider<String, String> dataProvider) {
            return null;
        }

        @Override
        public com.vaadin.flow.data.provider.LazyDataView<String> getLazyDataView() {
            return null;
        }

        void supplyItems(String... items) {
            provider = DataProvider.ofCollection(Arrays.asList(items));
        }

        @SuppressWarnings("unused")
        public DataProvider<String, ?> getDataProvider() {
            return provider;
        }
    }

    /**
     * Minimal field that implements only {@link HasListDataView}. Pinning each
     * data-view marker separately catches regressions where one interface is
     * dropped from the classifier's marker list while the others stay.
     */
    @Tag("list-data-view-field")
    static class ListDataViewField extends StubField<ListDataViewField, String>
            implements
            HasListDataView<String, com.vaadin.flow.data.provider.ListDataView<String, ?>> {

        private ListDataProvider<String> provider = DataProvider
                .ofCollection(List.of());

        ListDataViewField() {
            super("");
        }

        @Override
        public com.vaadin.flow.data.provider.ListDataView<String, ?> setItems(
                ListDataProvider<String> dataProvider) {
            return null;
        }

        @Override
        public com.vaadin.flow.data.provider.ListDataView<String, ?> getListDataView() {
            return null;
        }

        void supplyItems(String... items) {
            provider = DataProvider.ofCollection(Arrays.asList(items));
        }

        @SuppressWarnings("unused")
        public DataProvider<String, ?> getDataProvider() {
            return provider;
        }
    }

    /**
     * Minimal field that implements only {@link HasDataView}. See
     * {@link LazyDataViewField} for rationale.
     */
    @Tag("data-view-field")
    static class DataViewField extends StubField<DataViewField, String>
            implements
            HasDataView<String, String, com.vaadin.flow.data.provider.DataView<String>> {

        private ListDataProvider<String> provider = DataProvider
                .ofCollection(List.of());

        DataViewField() {
            super("");
        }

        @Override
        public com.vaadin.flow.data.provider.DataView<String> setItems(
                DataProvider<String, String> dataProvider) {
            return null;
        }

        @Override
        public com.vaadin.flow.data.provider.DataView<String> setItems(
                com.vaadin.flow.data.provider.InMemoryDataProvider<String> dataProvider) {
            return null;
        }

        @Override
        public com.vaadin.flow.data.provider.DataView<String> getGenericDataView() {
            return null;
        }

        void supplyItems(String... items) {
            provider = DataProvider.ofCollection(Arrays.asList(items));
        }

        @SuppressWarnings("unused")
        public DataProvider<String, ?> getDataProvider() {
            return provider;
        }
    }

}
