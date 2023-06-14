/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.checkbox;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.data.binder.HasDataProvider;
import com.vaadin.flow.data.binder.HasItemsAndComponents;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.provider.DataChangeEvent;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.data.selection.MultiSelectionEvent;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.dom.PropertyChangeEvent;
import com.vaadin.flow.dom.PropertyChangeListener;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

import elemental.json.Json;
import elemental.json.JsonArray;

/**
 * Server-side component for the {@code vaadin-checkbox-group} element.
 * <p>
 * CheckBoxGroup is a multiselection component where items are displayed as
 * check boxes.
 *
 * @author Vaadin Ltd
 */
public class CheckboxGroup<T>
        extends GeneratedVaadinCheckboxGroup<CheckboxGroup<T>, Set<T>>
        implements HasItemsAndComponents<T>, HasSize, HasValidation,
        MultiSelect<CheckboxGroup<T>, T>, HasDataProvider<T>, HasHelper,
        HasLabel, HasValidator<T> {

    private static final String VALUE = "value";

    private final KeyMapper<T> keyMapper = new KeyMapper<>(this::getItemId);

    private DataProvider<T, ?> dataProvider = DataProvider.ofItems();

    private boolean isReadOnly;

    private SerializablePredicate<T> itemEnabledProvider = item -> isEnabled();

    private ItemLabelGenerator<T> itemLabelGenerator = String::valueOf;

    private final PropertyChangeListener validationListener = this::validateSelectionEnabledState;
    private Registration validationRegistration;
    private Registration dataProviderListenerRegistration;

    private static class CheckBoxItem<T> extends Checkbox
            implements ItemComponent<T> {

        private final T item;

        private CheckBoxItem(String id, T item) {
            this.item = item;
            getElement().setProperty(VALUE, id);
        }

        @Override
        public T getItem() {
            return item;
        }

    }

    public CheckboxGroup() {
        super(Collections.emptySet(), Collections.emptySet(), JsonArray.class,
                CheckboxGroup::presentationToModel,
                CheckboxGroup::modelToPresentation);
        registerValidation();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        FieldValidationUtil.disableClientValidation(this);
    }

    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        this.dataProvider = dataProvider;
        reset();

        if (dataProviderListenerRegistration != null) {
            dataProviderListenerRegistration.remove();
        }
        dataProviderListenerRegistration = dataProvider
                .addDataProviderListener(event -> {
                    if (event instanceof DataChangeEvent.DataRefreshEvent) {
                        T otherItem = ((DataChangeEvent.DataRefreshEvent<T>) event)
                                .getItem();
                        this.getCheckboxItems()
                                .filter(item -> Objects.equals(
                                        getItemId(item.item),
                                        getItemId(otherItem)))
                                .findFirst().ifPresent(this::updateCheckbox);
                    } else {
                        reset();
                    }
                });
    }

    @Override
    public void updateSelection(Set<T> addedItems, Set<T> removedItems) {
        Set<T> value = new HashSet<>(getValue());
        value.addAll(addedItems);
        value.removeAll(removedItems);
        setValue(value);
    }

    /**
     * Sets the value of this component. If the new value is not equal to the
     * previous value, fires a value change event.
     * <p>
     * The component doesn't accept {@code null} values. The value of a checkbox
     * group without any selected items is an empty set. You can use the
     * {@link #clear()} method to set the empty value.
     *
     * @param value
     *            the new value to set, not {@code null}
     * @throws NullPointerException
     *             if value is {@code null}
     */
    @Override
    public void setValue(Set<T> value) {
        Objects.requireNonNull(value,
                "Cannot set a null value to checkbox group. "
                        + "Use the clear-method to reset the component's value to an empty set.");
        super.setValue(value);
        refreshCheckboxes();
    }

    @Override
    public Set<T> getSelectedItems() {
        return getValue();
    }

    @Override
    public Registration addSelectionListener(
            MultiSelectionListener<CheckboxGroup<T>, T> listener) {
        return addValueChangeListener(event -> listener
                .selectionChange(new MultiSelectionEvent<>(this, this,
                        event.getOldValue(), event.isFromClient())));
    }

    /**
     * Gets the data provider.
     *
     * @return the data provider, not {@code null}
     */
    public DataProvider<T, ?> getDataProvider() {
        return dataProvider;
    }

    @Override
    public void onEnabledStateChanged(boolean enabled) {
        if (isReadOnly()) {
            setDisabled(true);
        } else {
            setDisabled(!enabled);
        }
        getCheckboxItems().forEach(this::updateEnabled);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
        if (isEnabled()) {
            setDisabled(readOnly);
            refreshCheckboxes();
        }
    }

    @Override
    public boolean isReadOnly() {
        return isReadOnly;
    }

    /**
     * Returns the item enabled predicate.
     *
     * @return the item enabled predicate
     * @see #setItemEnabledProvider
     */
    public SerializablePredicate<T> getItemEnabledProvider() {
        return itemEnabledProvider;
    }

    /**
     * Sets the item enabled predicate for this checkbox group. The predicate is
     * applied to each item to determine whether the item should be enabled
     * ({@code true}) or disabled ({@code false}). Disabled items are displayed
     * as grayed out and the user cannot select them. The default predicate
     * always returns true (all the items are enabled).
     *
     * @param itemEnabledProvider
     *            the item enable predicate, not {@code null}
     */
    public void setItemEnabledProvider(
            SerializablePredicate<T> itemEnabledProvider) {
        this.itemEnabledProvider = Objects.requireNonNull(itemEnabledProvider);
        refreshCheckboxes();
    }

    /**
     * Sets the item label generator that is used to produce the strings shown
     * in the checkbox group for each item. By default,
     * {@link String#valueOf(Object)} is used.
     *
     * @param itemLabelGenerator
     *            the item label provider to use, not null
     */
    public void setItemLabelGenerator(
            ItemLabelGenerator<T> itemLabelGenerator) {
        Objects.requireNonNull(itemLabelGenerator,
                "The item label generator can not be null");
        this.itemLabelGenerator = itemLabelGenerator;
        reset();
    }

    /**
     * Gets the item label generator that is used to produce the strings shown
     * in the checkbox group for each item.
     *
     * @return the item label generator used, not null
     */
    public ItemLabelGenerator<T> getItemLabelGenerator() {
        return itemLabelGenerator;
    }

    /**
     * Sets the label for the checkbox group.
     *
     * @param label
     *            value for the {@code label} property in the checkbox group
     */
    @Override
    public void setLabel(String label) {
        super.setLabel(label);
    }

    /**
     * Gets the label of the checkbox group.
     *
     * @return the {@code label} property of the checkbox group
     */
    @Override
    public String getLabel() {
        return super.getLabelString();
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        super.setErrorMessage(errorMessage);
    }

    /**
     * Gets the current error message from the checkbox group.
     *
     * @return the current error message
     */
    @Override
    public String getErrorMessage() {
        return getErrorMessageString();
    }

    @Override
    public void setRequired(boolean required) {
        super.setRequired(required);
    }

    /**
     * Determines whether the checkbox group is marked as input required.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     *
     * @return {@code true} if the input is required, {@code false} otherwise
     */
    public boolean isRequired() {
        return isRequiredBoolean();
    }

    @Override
    public boolean isInvalid() {
        return isInvalidBoolean();
    }

    @Override
    public void setInvalid(boolean invalid) {
        super.setInvalid(invalid);
    }

    @Override
    protected boolean valueEquals(Set<T> value1, Set<T> value2) {
        assert value1 != null && value2 != null;
        if (value1.size() != value2.size()) {
            return false;
        }

        if (getDataProvider() == null) {
            return super.valueEquals(value1, value2);
        }
        Set<Object> ids1 = value1.stream().map(getDataProvider()::getId)
                .collect(Collectors.toSet());
        Set<Object> ids2 = value2.stream().map(getDataProvider()::getId)
                .collect(Collectors.toSet());
        return ids1.equals(ids2);
    }

    @Override
    protected boolean hasValidValue() {
        // we need to compare old value with new value to see if any disabled
        // items changed their value
        Set<T> value = presentationToModel(this,
                (JsonArray) getElement().getPropertyRaw(VALUE));
        Set<T> oldValue = getValue();

        // disabled items cannot change their value
        return getCheckboxItems().filter(CheckBoxItem::isDisabledBoolean)
                .noneMatch(item -> oldValue.contains(item.getItem()) != value
                        .contains(item.getItem()));
    }

    private void reset() {
        // Cache helper component before removal
        Component helperComponent = getHelperComponent();
        keyMapper.removeAll();
        removeAll();
        clear();

        // reinsert helper component
        // see https://github.com/vaadin/vaadin-checkbox/issues/191
        setHelperComponent(helperComponent);

        getDataProvider().fetch(new Query<>()).map(this::createCheckBox)
                .forEach(this::add);
    }

    private void refreshCheckboxes() {
        getCheckboxItems().forEach(this::updateCheckbox);
    }

    @SuppressWarnings("unchecked")
    private Stream<CheckBoxItem<T>> getCheckboxItems() {
        return getChildren().filter(CheckBoxItem.class::isInstance)
                .map(child -> (CheckBoxItem<T>) child);
    }

    private Checkbox createCheckBox(T item) {
        CheckBoxItem<T> checkbox = new CheckBoxItem<>(keyMapper.key(item),
                item);
        updateCheckbox(checkbox);
        return checkbox;
    }

    private void updateCheckbox(CheckBoxItem<T> checkbox) {
        checkbox.setLabel(getItemLabelGenerator().apply(checkbox.getItem()));
        checkbox.setValue(getValue().stream().anyMatch(
                selectedItem -> Objects.equals(getItemId(selectedItem),
                        getItemId(checkbox.getItem()))));
        updateEnabled(checkbox);
    }

    private void updateEnabled(CheckBoxItem<T> checkbox) {
        boolean disabled = isDisabledBoolean()
                || !getItemEnabledProvider().test(checkbox.getItem());
        checkbox.setDisabled(disabled);
        // When enabling a disabled checkbox group, individual checkbox Web
        // Components that should remain disabled (due to itemEnabledProvider),
        // may end up rendering as enabled.
        // Enforce the Web Component state using JS.
        checkbox.getElement().executeJs("this.disabled = $0", disabled);
    }

    private void validateSelectionEnabledState(PropertyChangeEvent event) {
        if (!hasValidValue()) {
            Set<T> oldValue = presentationToModel(this,
                    (JsonArray) event.getOldValue());
            // return the value back on the client side
            try {
                validationRegistration.remove();
                getElement().setPropertyJson(VALUE,
                        modelToPresentation(this, oldValue));
            } finally {
                registerValidation();
            }
            // Now make sure that the checkbox is still in the correct state
            Set<T> value = presentationToModel(this,
                    (JsonArray) event.getValue());
            getCheckboxItems()
                    .filter(checkbox -> value.contains(checkbox.getItem()))
                    .forEach(this::updateEnabled);
        }
    }

    private void registerValidation() {
        if (validationRegistration != null) {
            validationRegistration.remove();
        }
        validationRegistration = getElement().addPropertyChangeListener(VALUE,
                validationListener);
    }

    private static <T> Set<T> presentationToModel(CheckboxGroup<T> group,
            JsonArray presentation) {
        JsonArray array = presentation;
        Set<T> set = new HashSet<>();
        for (int i = 0; i < array.length(); i++) {
            set.add(group.keyMapper.get(array.getString(i)));
        }
        return set;
    }

    private static <T> JsonArray modelToPresentation(CheckboxGroup<T> group,
            Set<T> model) {
        JsonArray array = Json.createArray();
        if (model.isEmpty()) {
            return array;
        }

        model.stream().map(group.keyMapper::key)
                .forEach(key -> array.set(array.length(), key));
        return array;
    }

    private Object getItemId(T item) {
        if (getDataProvider() == null) {
            return item;
        }
        return getDataProvider().getId(item);
    }
}
