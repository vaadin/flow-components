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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.dataview.CheckboxGroupDataView;
import com.vaadin.flow.component.checkbox.dataview.CheckboxGroupListDataView;
import com.vaadin.flow.data.binder.HasItemComponents;
import com.vaadin.flow.data.provider.DataChangeEvent;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderWrapper;
import com.vaadin.flow.data.provider.DataViewUtils;
import com.vaadin.flow.data.provider.HasDataView;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.flow.data.provider.IdentifierProvider;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.ItemCountChangeEvent;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
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
 * CheckBoxGroup is a multi-selection component where items are displayed as
 * check boxes.
 * <p>
 * Use CheckBoxGroup to group related items. Individual checkboxes should be
 * used for options that are not related to each other in any way.
 *
 * @author Vaadin Ltd
 */
public class CheckboxGroup<T>
        extends GeneratedVaadinCheckboxGroup<CheckboxGroup<T>, Set<T>>
        implements HasItemComponents<T>, HasSize, HasValidation,
        MultiSelect<CheckboxGroup<T>, T>,
        HasListDataView<T, CheckboxGroupListDataView<T>>,
        HasDataView<T, Void, CheckboxGroupDataView<T>>, HasHelper, HasLabel {

    private static final String VALUE = "value";

    private final KeyMapper<T> keyMapper = new KeyMapper<>(this::getItemId);

    private final AtomicReference<DataProvider<T, ?>> dataProvider = new AtomicReference<>(
            DataProvider.ofItems());

    private boolean isReadOnly;

    private SerializablePredicate<T> itemEnabledProvider = item -> isEnabled();

    private ItemLabelGenerator<T> itemLabelGenerator = String::valueOf;

    private ComponentRenderer<? extends Component, T> itemRenderer;

    private final PropertyChangeListener validationListener = this::validateSelectionEnabledState;
    private Registration validationRegistration;
    private Registration dataProviderListenerRegistration;

    private int lastNotifiedDataSize = -1;

    private volatile int lastFetchedDataSize = -1;

    private SerializableConsumer<UI> sizeRequest;

    /**
     * Creates an empty checkbox group
     */
    public CheckboxGroup() {
        super(Collections.emptySet(), Collections.emptySet(), JsonArray.class,
                CheckboxGroup::presentationToModel,
                CheckboxGroup::modelToPresentation, true);
        registerValidation();
    }

    /**
     * Creates an empty checkbox group with the defined label.
     *
     * @param label
     *            the label describing the checkbox group
     * @see #setLabel(String)
     */
    public CheckboxGroup(String label) {
        this();
        setLabel(label);
    }

    /**
     * Creates a checkbox group with the defined label and populated with the
     * items in the collection.
     *
     * @param label
     *            the label describing the checkbox group
     * @param items
     *            the items to be shown in the list of the checkbox group
     * @see #setLabel(String)
     * @see #setItems(Collection)
     */
    public CheckboxGroup(String label, Collection<T> items) {
        this();
        setLabel(label);
        setItems(items);
    }

    /**
     * Creates a checkbox group with the defined label and populated with the
     * items in the array.
     *
     * @param label
     *            the label describing the checkbox group
     * @param items
     *            the items to be shown in the list of the checkbox group
     * @see #setLabel(String)
     * @see #setItems(Object...)
     */
    @SafeVarargs
    public CheckboxGroup(String label, T... items) {
        this();
        setLabel(label);
        setItems(items);
    }

    /**
     * Constructs a checkbox group with a value change listener.
     *
     * @param listener
     *            the value change listener to add
     * @see #addValueChangeListener(ValueChangeListener)
     */
    public CheckboxGroup(
            ValueChangeListener<ComponentValueChangeEvent<CheckboxGroup<T>, Set<T>>> listener) {
        this();
        addValueChangeListener(listener);
    }

    /**
     * Constructs a checkbox group with the defined label and a value change
     * listener.
     *
     * @param label
     *            the label describing the checkbox group
     * @param listener
     *            the value change listener to add
     * @see #setLabel(String)
     * @see #addValueChangeListener(ValueChangeListener)
     */
    public CheckboxGroup(String label,
            ValueChangeListener<ComponentValueChangeEvent<CheckboxGroup<T>, Set<T>>> listener) {
        this(label);
        addValueChangeListener(listener);
    }

    /**
     * Constructs a checkbox group with the defined label, a value change
     * listener and populated with the items in the array.
     *
     * @param label
     *            the label describing the checkbox group
     * @param listener
     *            the value change listener to add
     * @param items
     *            the items to be shown in the list of the checkbox group
     * @see #setLabel(String)
     * @see #addValueChangeListener(ValueChangeListener)
     * @see #setItems(Object...)
     */
    @SafeVarargs
    public CheckboxGroup(String label,
            ValueChangeListener<ComponentValueChangeEvent<CheckboxGroup<T>, Set<T>>> listener,
            T... items) {
        this(label, listener);
        setItems(items);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        FieldValidationUtil.disableClientValidation(this);
    }

    @Override
    public CheckboxGroupDataView<T> setItems(
            DataProvider<T, Void> dataProvider) {
        setDataProvider(dataProvider);
        return getGenericDataView();
    }

    @Override
    public CheckboxGroupDataView<T> setItems(
            InMemoryDataProvider<T> inMemoryDataProvider) {
        // We don't use DataProvider.withConvertedFilter() here because it's
        // implementation does not apply the filter converter if Query has a
        // null filter
        DataProvider<T, Void> convertedDataProvider = new DataProviderWrapper<T, Void, SerializablePredicate<T>>(
                inMemoryDataProvider) {
            @Override
            protected SerializablePredicate<T> getFilter(Query<T, Void> query) {
                // Just ignore the query filter (Void) and apply the
                // predicate only
                return Optional.ofNullable(inMemoryDataProvider.getFilter())
                        .orElse(item -> true);
            }
        };
        return setItems(convertedDataProvider);
    }

    @Override
    public CheckboxGroupListDataView<T> setItems(
            ListDataProvider<T> dataProvider) {
        setDataProvider(dataProvider);
        return getListDataView();
    }

    /**
     * Gets the list data view for the checkbox group. This data view should
     * only be used when the items are in-memory and set with:
     * <ul>
     * <li>{@link #setItems(Collection)}</li>
     * <li>{@link #setItems(Object[])}</li>
     * <li>{@link #setItems(ListDataProvider)}</li>
     * </ul>
     * If the items are not in-memory an exception is thrown.
     *
     * @return the list data view that provides access to the data bound to the
     *         checkbox group
     */
    @Override
    public CheckboxGroupListDataView<T> getListDataView() {
        return new CheckboxGroupListDataView<>(this::getDataProvider, this,
                this::identifierProviderChanged, (filter, sorting) -> reset());
    }

    /**
     * Gets the generic data view for the checkbox group. This data view should
     * only be used when {@link #getListDataView()} is not applicable for the
     * underlying data provider.
     *
     * @return the generic DataView instance implementing
     *         {@link CheckboxGroupDataView}
     */
    @Override
    public CheckboxGroupDataView<T> getGenericDataView() {
        return new CheckboxGroupDataView<>(this::getDataProvider, this,
                this::identifierProviderChanged);
    }

    private static class CheckBoxItem<T> extends Checkbox
            implements HasItemComponents.ItemComponent<T> {

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
     *
     * @deprecated Because the stream is collected to a list anyway, use
     *             {@link HasListDataView#setItems(Collection)} instead.
     */
    @Deprecated
    public void setItems(Stream<T> streamOfItems) {
        setItems(DataProvider.fromStream(streamOfItems));
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated use instead one of the {@code setItems} methods which provide
     *             access to either {@link CheckboxGroupListDataView} or
     *             {@link CheckboxGroupDataView}
     */
    @Deprecated
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        this.dataProvider.set(dataProvider);
        DataViewUtils.removeComponentFilterAndSortComparator(this);
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
     * @deprecated use {@link #getListDataView()} or
     *             {@link #getGenericDataView()} instead
     */
    @Deprecated
    public DataProvider<T, ?> getDataProvider() {
        // dataProvider reference won't have been initialized before
        // calling from CheckboxGroup constructor
        return Optional.ofNullable(dataProvider).map(AtomicReference::get)
                .orElse(null);
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
        IdentifierProvider<T> identifierProvider = getIdentifierProvider();
        Set<Object> ids1 = value1.stream().map(identifierProvider)
                .collect(Collectors.toSet());
        Set<Object> ids2 = value2.stream().map(identifierProvider)
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

    /**
     * Returns the item component renderer.
     *
     * @return the item renderer
     * @see #setRenderer(ComponentRenderer)
     *
     * @since 23.1
     */
    public ComponentRenderer<? extends Component, T> getItemRenderer() {
        return itemRenderer;
    }

    /**
     * Sets the item renderer for this checkbox group. The renderer is applied
     * to each item to create a component which represents the item.
     * <p>
     * Note: Component acts as a label to the checkbox and clicks on it trigger
     * the checkbox. Hence interactive components like DatePicker or ComboBox
     * cannot be used.
     *
     * @param renderer
     *            the item renderer, not {@code null}
     *
     * @since 23.1
     */
    public void setRenderer(
            ComponentRenderer<? extends Component, T> renderer) {
        this.itemRenderer = Objects.requireNonNull(renderer);
        refreshCheckboxItems();
    }

    @SuppressWarnings("unchecked")
    private void reset() {
        keyMapper.removeAll();
        clear();

        synchronized (dataProvider) {
            // Cache helper component before removal
            Component helperComponent = getHelperComponent();

            // Remove all known children (doesn't remove client-side-only
            // children such as the label)
            getChildren().forEach(this::remove);

            // reinsert helper component
            // see https://github.com/vaadin/vaadin-checkbox/issues/191
            setHelperComponent(helperComponent);

            final AtomicInteger itemCounter = new AtomicInteger(0);

            getDataProvider().fetch(DataViewUtils.getQuery(this))
                    .map(item -> createCheckBox((T) item))
                    .forEach(component -> {
                        add((Component) component);
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

    private void refreshCheckboxItems() {
        getCheckboxItems().forEach(this::updateCheckbox);
    }

    private void updateCheckbox(CheckBoxItem<T> checkbox) {
        if (itemRenderer == null) {
            checkbox.setLabel(
                    getItemLabelGenerator().apply(checkbox.getItem()));
        } else {
            checkbox.setLabelComponent(
                    getItemRenderer().createComponent(checkbox.item));
        }

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
        if (group.keyMapper == null) {
            return Collections.emptySet();
        }
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
        return getIdentifierProvider().apply(item);
    }

    private void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    private void fireSizeEvent() {
        final int newSize = lastFetchedDataSize;
        if (lastNotifiedDataSize != newSize) {
            lastNotifiedDataSize = newSize;
            fireEvent(new ItemCountChangeEvent<>(this, newSize, false));
        }
    }

    @SuppressWarnings("unchecked")
    private IdentifierProvider<T> getIdentifierProvider() {
        IdentifierProvider<T> identifierProviderObject = ComponentUtil
                .getData(this, IdentifierProvider.class);
        if (identifierProviderObject == null) {
            DataProvider<T, ?> dataProvider = getDataProvider();
            if (dataProvider != null) {
                return dataProvider::getId;
            } else {
                return IdentifierProvider.identity();
            }
        } else {
            return identifierProviderObject;
        }
    }

    private void identifierProviderChanged(
            IdentifierProvider<T> identifierProvider) {
        keyMapper.setIdentifierGetter(identifierProvider);
    }

}
