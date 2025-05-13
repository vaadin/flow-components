/*
 * Copyright 2000-2025 Vaadin Ltd.
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.dataview.CheckboxGroupDataView;
import com.vaadin.flow.component.checkbox.dataview.CheckboxGroupListDataView;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasClientValidation;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasValidationProperties;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.shared.SelectionPreservationHandler;
import com.vaadin.flow.component.shared.SelectionPreservationMode;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.component.shared.internal.ValidationController;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.HasItemComponents;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.Validator;
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
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
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
 * <h2>Validation</h2>
 * <p>
 * CheckboxGroup comes with a built-in validation mechanism that verifies that
 * at least one checkbox is selected when
 * {@link #setRequiredIndicatorVisible(boolean) required} is enabled.
 * <p>
 * Validation is triggered whenever the user initiates a value change by
 * toggling a checkbox. Programmatic value changes trigger validation as well.
 * If validation fails, the component is marked as invalid and an error message
 * is displayed below the group.
 * <p>
 * The required error message can be configured using either
 * {@link CheckboxGroupI18n#setRequiredErrorMessage(String)} or
 * {@link #setErrorMessage(String)}.
 * <p>
 * For more advanced validation that requires custom rules, you can use
 * {@link Binder}. Please note that Binder provides its own API for the required
 * validation, see {@link Binder.BindingBuilder#asRequired(String)
 * asRequired()}.
 * <p>
 * However, if Binder doesn't fit your needs and you want to implement fully
 * custom validation logic, you can disable the built-in validation by setting
 * {@link #setManualValidation(boolean)} to true. This will allow you to control
 * the invalid state and the error message manually using
 * {@link #setInvalid(boolean)} and {@link #setErrorMessage(String)} API.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-checkbox-group")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/checkbox-group", version = "24.8.0-alpha18")
@JsModule("@vaadin/checkbox-group/src/vaadin-checkbox-group.js")
public class CheckboxGroup<T>
        extends AbstractSinglePropertyField<CheckboxGroup<T>, Set<T>>
        implements HasAriaLabel, HasClientValidation,
        HasDataView<T, Void, CheckboxGroupDataView<T>>, HasItemComponents<T>,
        InputField<AbstractField.ComponentValueChangeEvent<CheckboxGroup<T>, Set<T>>, Set<T>>,
        HasListDataView<T, CheckboxGroupListDataView<T>>,
        HasThemeVariant<CheckboxGroupVariant>, HasValidationProperties,
        HasValidator<Set<T>>, MultiSelect<CheckboxGroup<T>, T> {

    private static final String VALUE = "value";

    private final KeyMapper<T> keyMapper = new KeyMapper<>(this::getItemId);

    private final AtomicReference<DataProvider<T, ?>> dataProvider = new AtomicReference<>(
            DataProvider.ofItems());

    private SerializablePredicate<T> itemEnabledProvider = item -> isEnabled();

    private ItemLabelGenerator<T> itemLabelGenerator = String::valueOf;

    private ItemHelperGenerator<T> itemHelperGenerator = item -> null;

    private ComponentRenderer<? extends Component, T> itemRenderer;

    private Registration dataProviderListenerRegistration;

    private int lastNotifiedDataSize = -1;

    private volatile int lastFetchedDataSize = -1;

    private SerializableConsumer<UI> sizeRequest;

    private CheckboxGroupI18n i18n;

    private Validator<Set<T>> defaultValidator = (value, context) -> {
        boolean fromComponent = context == null;

        // Do the required check only if the validator is called from the
        // component, and not from Binder. Binder has its own implementation
        // of required validation.
        boolean isRequired = fromComponent && isRequiredIndicatorVisible();
        return ValidationUtil.validateRequiredConstraint(
                getI18nErrorMessage(CheckboxGroupI18n::getRequiredErrorMessage),
                isRequired, getValue(), getEmptyValue());
    };

    private ValidationController<CheckboxGroup<T>, Set<T>> validationController = new ValidationController<>(
            this);

    private SelectionPreservationHandler<T> selectionPreservationHandler;

    /**
     * Creates an empty checkbox group
     */
    public CheckboxGroup() {
        super("value", Collections.emptySet(), JsonArray.class,
                CheckboxGroup::presentationToModel,
                CheckboxGroup::modelToPresentation);

        getElement().setProperty("manualValidation", true);

        addValueChangeListener(e -> validate());

        initSelectionPreservationHandler();
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
                this::identifierProviderChanged,
                (filter, sorting) -> rebuild());
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

    private void initSelectionPreservationHandler() {
        selectionPreservationHandler = new SelectionPreservationHandler<>(
                SelectionPreservationMode.DISCARD) {

            @Override
            public void onPreserveAll(DataChangeEvent<T> dataChangeEvent) {
                // NO-OP
            }

            @Override
            public void onPreserveExisting(DataChangeEvent<T> dataChangeEvent) {
                Map<Object, T> deselectionCandidateIdsToItems = getSelectedItems()
                        .stream().collect(Collectors
                                .toMap(item -> getItemId(item), item -> item));
                @SuppressWarnings("unchecked")
                Stream<T> itemsStream = getDataProvider()
                        .fetch(DataViewUtils.getQuery(CheckboxGroup.this));
                Set<Object> existingItemIds = itemsStream
                        .map(item -> getItemId(item))
                        .filter(deselectionCandidateIdsToItems::containsKey)
                        .limit(deselectionCandidateIdsToItems.size())
                        .collect(Collectors.toSet());
                existingItemIds.forEach(deselectionCandidateIdsToItems::remove);
                deselect(deselectionCandidateIdsToItems.values());
            }

            @Override
            public void onDiscard(DataChangeEvent<T> dataChangeEvent) {
                clear();
            }
        };
    }

    private void handleDataChange(DataChangeEvent<T> dataChangeEvent) {
        if (dataChangeEvent instanceof DataChangeEvent.DataRefreshEvent<T> dataRefreshEvent) {
            T otherItem = dataRefreshEvent.getItem();
            Object otherItemId = getItemId(otherItem);
            keyMapper.refresh(otherItem);
            getCheckboxItems().filter(
                    item -> Objects.equals(getItemId(item.item), otherItemId))
                    .findFirst().ifPresent(this::updateCheckbox);
        } else {
            keyMapper.removeAll();
            selectionPreservationHandler.handleDataChange(dataChangeEvent);
            rebuild();
        }
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
     * Sets a generic data provider for the CheckboxGroup to use.
     * <p>
     * Use this method when none of the {@code setItems} methods are applicable,
     * e.g. when having a data provider with filter that cannot be transformed
     * to {@code DataProvider<T, Void>}.
     *
     * @param dataProvider
     *            DataProvider instance to use, not <code>null</code>
     */
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        this.dataProvider.set(dataProvider);
        DataViewUtils.removeComponentFilterAndSortComparator(this);
        keyMapper.removeAll();
        clear();
        rebuild();

        if (dataProviderListenerRegistration != null) {
            dataProviderListenerRegistration.remove();
        }
        dataProviderListenerRegistration = dataProvider
                .addDataProviderListener(this::handleDataChange);
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
     * Gets the data provider used by this CheckboxGroup.
     *
     * <p>
     * To get information and control over the items in the CheckboxGroup, use
     * either {@link #getListDataView()} or {@link #getGenericDataView()}
     * instead.
     *
     * @return the data provider used by this CheckboxGroup
     */
    public DataProvider<T, ?> getDataProvider() {
        // dataProvider reference won't have been initialized before
        // calling from CheckboxGroup constructor
        return Optional.ofNullable(dataProvider).map(AtomicReference::get)
                .orElse(null);
    }

    @Override
    public void onEnabledStateChanged(boolean enabled) {
        setDisabled(!enabled);
        getCheckboxItems().forEach(this::updateEnabled);
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
     * <p>
     * Setting an item label generator removes any previously set item renderer.
     *
     * @param itemLabelGenerator
     *            the item label provider to use, not null
     */
    public void setItemLabelGenerator(
            ItemLabelGenerator<T> itemLabelGenerator) {
        Objects.requireNonNull(itemLabelGenerator,
                "The item label generator can not be null");
        this.itemLabelGenerator = itemLabelGenerator;
        this.itemRenderer = null;
        refreshCheckboxes();
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
     * Sets the {@link ItemHelperGenerator} that is used for generating helper
     * text strings used by the checkbox group for each item.
     *
     * @since 24.4
     * @see Checkbox#setHelperText(String)
     * @see #setItemLabelGenerator
     * @param itemHelperGenerator
     *            the item helper generator to use, not null
     */
    public void setItemHelperGenerator(
            ItemHelperGenerator<T> itemHelperGenerator) {
        Objects.requireNonNull(itemHelperGenerator,
                "The item helper generator can not be null");
        this.itemHelperGenerator = itemHelperGenerator;
        refreshCheckboxes();
    }

    /**
     * Gets the {@link ItemHelperGenerator} function that is used for generating
     * helper text strings used by the checkbox group for each item.
     *
     * @since 24.4
     * @see #getItemLabelGenerator()
     * @return the item helper generator, not null
     */
    public ItemHelperGenerator<T> getItemHelperGenerator() {
        return itemHelperGenerator;
    }

    /**
     * {@link ItemHelperGenerator} can be used to generate helper text strings
     * used by the checkbox group for each checkbox.
     *
     * @since 24.4
     * @see Checkbox#setHelperText(String)
     * @param <T>
     *            item type
     */
    @FunctionalInterface
    public interface ItemHelperGenerator<T>
            extends SerializableFunction<T, String> {

        /**
         * Gets a helper text string for the {@code item}.
         *
         * @param item
         *            the item to get helper text for
         * @return the helper text string for the item, not {@code null}
         */
        @Override
        String apply(T item);
    }

    /**
     * Sets the label for the checkbox group.
     *
     * @param label
     *            value for the {@code label} property in the checkbox group
     */
    public void setLabel(String label) {
        getElement().setProperty("label", label == null ? "" : label);
    }

    /**
     * Gets the label of the checkbox group.
     *
     * @return the {@code label} property of the checkbox group
     */
    public String getLabel() {
        return getElement().getProperty("label");
    }

    @Override
    public void setAriaLabel(String ariaLabel) {
        getElement().setProperty("accessibleName", ariaLabel);
    }

    @Override
    public Optional<String> getAriaLabel() {
        return Optional.ofNullable(getElement().getProperty("accessibleName"));
    }

    @Override
    public void setAriaLabelledBy(String labelledBy) {
        getElement().setProperty("accessibleNameRef", labelledBy);
    }

    @Override
    public Optional<String> getAriaLabelledBy() {
        return Optional
                .ofNullable(getElement().getProperty("accessibleNameRef"));
    }

    /**
     * Sets whether the user is required to select at least one checkbox. When
     * required, an indicator appears next to the label and the field
     * invalidates if all previously selected checkboxes are deselected.
     * <p>
     * NOTE: The required indicator is only visible when the field has a label,
     * see {@link #setLabel(String)}.
     *
     * @param required
     *            {@code true} to make the field required, {@code false}
     *            otherwise
     * @see CheckboxGroupI18n#setRequiredErrorMessage(String)
     */
    @Override
    public void setRequiredIndicatorVisible(boolean required) {
        super.setRequiredIndicatorVisible(required);
    }

    /**
     * Gets whether the user is required to select at least one checkbox.
     *
     * @return {@code true} if the field is required, {@code false} otherwise
     * @see #setRequiredIndicatorVisible(boolean)
     */
    @Override
    public boolean isRequiredIndicatorVisible() {
        return super.isRequiredIndicatorVisible();
    }

    /**
     * Alias for {@link #isRequiredIndicatorVisible()}
     *
     * @return {@code true} if the field is required, {@code false} otherwise
     */
    public boolean isRequired() {
        return isRequiredIndicatorVisible();
    }

    /**
     * Alias for {@link #setRequiredIndicatorVisible(boolean)}.
     *
     * @param required
     *            {@code true} to make the field required, {@code false}
     *            otherwise
     */
    public void setRequired(boolean required) {
        setRequiredIndicatorVisible(required);
    }

    /**
     * If true, the user cannot interact with this element.
     *
     * @param disabled
     *            the boolean value to set
     */
    protected void setDisabled(boolean disabled) {
        getElement().setProperty("disabled", disabled);
    }

    /**
     * If true, the user cannot interact with this element.
     *
     * @return the {@code disabled} property from the webcomponent
     */
    protected boolean isDisabledBoolean() {
        return getElement().getProperty("disabled", false);
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

    /**
     * Sets the selection preservation mode. Determines what happens with the
     * selection when {@link DataProvider#refreshAll} is called. The selection
     * is discarded in any case when a new data provider is set. The default is
     * {@link SelectionPreservationMode#DISCARD}.
     *
     * @param selectionPreservationMode
     *            the selection preservation mode to switch to, not {@code null}
     *
     * @see SelectionPreservationMode
     */
    public void setSelectionPreservationMode(
            SelectionPreservationMode selectionPreservationMode) {
        selectionPreservationHandler
                .setSelectionPreservationMode(selectionPreservationMode);
    }

    /**
     * Gets the selection preservation mode.
     *
     * @return the selection preservation mode
     *
     * @see #setSelectionPreservationMode(SelectionPreservationMode)
     */
    public SelectionPreservationMode getSelectionPreservationMode() {
        return selectionPreservationHandler.getSelectionPreservationMode();
    }

    @SuppressWarnings("unchecked")
    private void rebuild() {
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

        String helper = itemHelperGenerator.apply(checkbox.item);
        if (helper != null) {
            checkbox.setHelperText(helper);
        } else if (checkbox.getHelperText() != null) {
            checkbox.setHelperText(null);
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

    @Override
    public void setManualValidation(boolean enabled) {
        validationController.setManualValidation(enabled);
    }

    @Override
    public Validator<Set<T>> getDefaultValidator() {
        return defaultValidator;
    }

    /**
     * Validates the current value against the constraints and sets the
     * {@code invalid} property and the {@code errorMessage} property based on
     * the result. If a custom error message is provided with
     * {@link #setErrorMessage(String)}, it is used. Otherwise, the error
     * message defined in the i18n object is used.
     * <p>
     * The method does nothing if the manual validation mode is enabled.
     */
    protected void validate() {
        validationController.validate(getValue());
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link #setI18n(CheckboxGroupI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public CheckboxGroupI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization object for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(CheckboxGroupI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
    }

    private String getI18nErrorMessage(
            Function<CheckboxGroupI18n, String> getter) {
        return Optional.ofNullable(i18n).map(getter).orElse("");
    }

    /**
     * The internationalization properties for {@link CheckboxGroup}.
     */
    public static class CheckboxGroupI18n implements Serializable {

        private String requiredErrorMessage;

        /**
         * Gets the error message displayed when the field is required but
         * empty.
         *
         * @return the error message or {@code null} if not set
         * @see CheckboxGroup#isRequiredIndicatorVisible()
         * @see CheckboxGroup#setRequiredIndicatorVisible(boolean)
         */
        public String getRequiredErrorMessage() {
            return requiredErrorMessage;
        }

        /**
         * Sets the error message to display when the field is required but
         * empty.
         * <p>
         * Note, custom error messages set with
         * {@link CheckboxGroup#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see CheckboxGroup#isRequiredIndicatorVisible()
         * @see CheckboxGroup#setRequiredIndicatorVisible(boolean)
         */
        public CheckboxGroupI18n setRequiredErrorMessage(String errorMessage) {
            requiredErrorMessage = errorMessage;
            return this;
        }
    }
}
