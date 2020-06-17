/*
 * Copyright 2000-2018 Vaadin Ltd.
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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.dataview.CheckboxGroupDataView;
import com.vaadin.flow.component.checkbox.dataview.CheckboxGroupDataViewImpl;
import com.vaadin.flow.component.checkbox.dataview.CheckboxGroupListDataView;
import com.vaadin.flow.data.binder.HasDataProvider;
import com.vaadin.flow.data.binder.HasItemsAndComponents;
import com.vaadin.flow.data.provider.DataChangeEvent;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.HasDataView;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SizeChangeEvent;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.data.selection.MultiSelectionEvent;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.dom.PropertyChangeEvent;
import com.vaadin.flow.dom.PropertyChangeListener;
import com.vaadin.flow.function.SerializableConsumer;
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
        MultiSelect<CheckboxGroup<T>, T>, HasDataProvider<T>,
        HasListDataView<T, CheckboxGroupListDataView<T>>,
        HasDataView<T, CheckboxGroupDataView<T>> {

    private static final String VALUE = "value";

    private final KeyMapper<T> keyMapper = new KeyMapper<>(this::getItemId);

    private final AtomicReference<DataProvider<T, ?>> dataProvider =
            new AtomicReference<>(DataProvider.ofItems());

    private boolean isReadOnly;

    private SerializablePredicate<T> itemEnabledProvider = item -> isEnabled();

    private ItemLabelGenerator<T> itemLabelGenerator = String::valueOf;

    private final PropertyChangeListener validationListener = this::validateSelectionEnabledState;
    private Registration validationRegistration;
    private Registration dataProviderListenerRegistration;

    private int lastNotifiedDataSize = -1;

    private volatile int lastFetchedDataSize = -1;

    private SerializableConsumer<UI> sizeRequest;

    public CheckboxGroup() {
        super(Collections.emptySet(), Collections.emptySet(), JsonArray.class,
                CheckboxGroup::presentationToModel,
                CheckboxGroup::modelToPresentation);
        registerValidation();
    }

    @Override
    public CheckboxGroupDataView<T> setDataSource(
            DataProvider<T, ?> dataProvider) {
        setDataProvider(dataProvider);
        return getDataView();
    }

    @Override
    public CheckboxGroupListDataView<T> setDataSource(
            ListDataProvider<T> dataProvider) {
        this.setDataProvider(dataProvider);
        return getListDataView();
    }

    @Override
    public CheckboxGroupListDataView<T> getListDataView() {
        if (getDataProvider() instanceof ListDataProvider) {
            return new CheckboxGroupListDataView<>(this::getDataProvider, this);
        }
        throw new IllegalStateException(String.format(
                "Required ListDataProvider, but got '%s'. Use 'getDataView()' "
                        + "to get a generic DataView instance.",
                getDataProvider().getClass().getSuperclass().getSimpleName()));
    }

    /**
     * Getter for getting a generic CheckboxGroupDataView.
     * <p>
     * {@link #getListDataView()} is recommended when the backing data source is
     * a List.
     *
     * @return DataView instance implementing {@link CheckboxGroupDataView}
     */
    @Override
    public CheckboxGroupDataView<T> getDataView() {
        return new CheckboxGroupDataViewImpl(this::getDataProvider,
                    this);
    }

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

    /**
     * {@inheritDoc}
     * @deprecated use {@link HasListDataView#setDataSource(Object[])} )}
     */
    @Override
    @Deprecated
    public void setItems(T... items) {
        setDataSource(items);
    }

    /**
     * {@inheritDoc}
     * @deprecated use {@link HasListDataView#setDataSource(Collection)}
     */
    @Override
    @Deprecated
    public void setItems(Collection<T> items) {
        setDataSource(items);
    }

    /**
     * {@inheritDoc}
     * @deprecated use {@link HasListDataView#setDataSource(Stream)}
     */
    @Override
    @Deprecated
    public void setItems(Stream<T> streamOfItems) {
        setDataSource(streamOfItems);
    }

    /**
     * {@inheritDoc}
     * @deprecated use instead one of the setDataSource methods from
     * {@link HasListDataView}
     */
    @Override
    @Deprecated
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        this.dataProvider.set(dataProvider);
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
     * @deprecated use {@link #getListDataView()} or getLazyDataView() instead
     */
    @Deprecated
    public DataProvider<T, ?> getDataProvider() {
        return dataProvider != null ? dataProvider.get() : null;
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

    @Override
    public void setLabel(String label) {
        super.setLabel(label);
    }

    /**
     * Gets the label of the checkbox group.
     *
     * @return the {@code label} property of the checkbox group
     */
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
        Set<T> selectedItems = presentationToModel(this,
                (JsonArray) getElement().getPropertyRaw(VALUE));
        if (selectedItems == null || selectedItems.isEmpty()) {
            return true;
        }
        return selectedItems.stream().allMatch(itemEnabledProvider);
    }

    private void reset() {
        keyMapper.removeAll();
        removeAll();
        clear();

        synchronized (dataProvider) {
            final AtomicInteger itemCounter = new AtomicInteger(0);
            getDataProvider().fetch(new Query<>()).map(this::createCheckBox)
                    .forEach(component -> {
                        add(component);
                        itemCounter.incrementAndGet();
                    });
            lastFetchedDataSize = itemCounter.get();

            // Ignore new size requests unless the last one has been executed
            // so as to avoid multiple beforeClientResponses.
            if (sizeRequest == null) {
                sizeRequest = ui -> {
                    fireSizeEvent();
                    sizeRequest = null;
                };
                // Size event is fired before client response so as to avoid
                // multiple size change events during server round trips
                runBeforeClientResponse(sizeRequest);
            }
        }
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
        updateEnabled(checkbox);
        checkbox.setValue(getValue().stream().anyMatch(
                selectedItem -> Objects.equals(getItemId(selectedItem),
                        getItemId(checkbox.getItem()))));
    }

    private void updateEnabled(CheckBoxItem<T> checkbox) {
        boolean disabled = isDisabledBoolean()
                || !getItemEnabledProvider().test(checkbox.getItem());
        Serializable rawValue = checkbox.getElement()
                .getPropertyRaw("disabled");
        if (rawValue instanceof Boolean) {
            // convert the boolean value to a String to force update the
            // property value. Otherwise since the provided value is the same as
            // the current one the update don't do anything.
            checkbox.getElement().setProperty("disabled",
                    disabled ? Boolean.TRUE.toString() : null);
        } else {
            checkbox.setDisabled(disabled);
        }
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
            // Now make sure that the button is still in the correct state
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

    private void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    private void fireSizeEvent() {
        final int newSize = lastFetchedDataSize;
        if (lastNotifiedDataSize != newSize) {
            lastNotifiedDataSize = newSize;
            fireEvent(new SizeChangeEvent<>(this, newSize));
        }
    }
}
