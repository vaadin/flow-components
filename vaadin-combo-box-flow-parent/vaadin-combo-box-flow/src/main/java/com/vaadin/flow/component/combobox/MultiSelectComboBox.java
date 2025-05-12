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
package com.vaadin.flow.component.combobox;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.IdentifierProviderChangeEvent;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.data.selection.MultiSelectionEvent;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.shared.Registration;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonType;

/**
 * MultiSelectComboBox allows the user to select one or more values from a
 * filterable list of options presented in an overlay. Compared to
 * {@link ComboBox}, MultiSelectComboBox allows to select multiple values.
 * <p>
 * MultiSelectComboBox supports lazy loading. This means that when using large
 * data sets, items are requested from the server one "page" at a time when the
 * user scrolls down the overlay. The number of items in one page is by default
 * 50, and can be changed with {@link #setPageSize(int)}.
 * <p>
 * MultiSelectComboBox can do filtering either in the browser or in the server.
 * When MultiSelectComboBox has only a relatively small set of items, the
 * filtering will happen in the browser, allowing smooth user-experience. When
 * the size of the data set is larger than the {@code pageSize}, the
 * webcomponent doesn't necessarily have all the data available, and it will
 * make requests to the server to handle the filtering. Also, if you have
 * defined custom filtering logic, with eg.
 * {@link #setItems(ComboBox.ItemFilter, Collection)}, filtering will happen in
 * the server. To enable client-side filtering with larger data sets, you can
 * override the {@code pageSize} to be bigger than the size of your data set.
 * However, then the full data set will be sent to the client immediately, and
 * you will lose the benefits of lazy loading.
 * <h2>Validation</h2>
 * <p>
 * MultiSelectComboBox comes with a built-in validation mechanism that verifies
 * that the field is not empty when {@link #setRequiredIndicatorVisible(boolean)
 * required} is enabled.
 * <p>
 * Validation is triggered whenever the user initiates a value change, for
 * example by selection from the dropdown or manual entry followed by Enter or
 * blur. Programmatic value changes trigger validation as well. If validation
 * fails, the component is marked as invalid and an error message is displayed
 * below the input.
 * <p>
 * The required error message can be configured using either
 * {@link MultiSelectComboBoxI18n#setRequiredErrorMessage(String)} or
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
 * @param <TItem>
 *            the type of the items to be selectable from the combo box
 * @author Vaadin Ltd
 */
@Tag("vaadin-multi-select-combo-box")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/multi-select-combo-box", version = "24.8.0-alpha18")
@JsModule("@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box.js")
@JsModule("./flow-component-renderer.js")
@JsModule("./comboBoxConnector.js")
public class MultiSelectComboBox<TItem>
        extends ComboBoxBase<MultiSelectComboBox<TItem>, TItem, Set<TItem>>
        implements MultiSelect<MultiSelectComboBox<TItem>, TItem>,
        HasThemeVariant<MultiSelectComboBoxVariant> {

    private final MultiSelectComboBoxSelectionModel<TItem> selectionModel;
    private AutoExpandMode autoExpand;

    /**
     * Default constructor. Creates an empty combo box.
     */
    public MultiSelectComboBox() {
        this(50);
    }

    /**
     * Creates an empty combo box with the defined page size for lazy loading.
     * <p>
     * The default page size is 50.
     * <p>
     * The page size is also the largest number of items that can support
     * client-side filtering. If you provide more items than the page size, the
     * component has to fall back to server-side filtering.
     *
     * @param pageSize
     *            the amount of items to request at a time for lazy loading
     * @see #setPageSize(int)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public MultiSelectComboBox(int pageSize) {
        super("selectedItems", new LinkedHashSet<>(), JsonArray.class,
                MultiSelectComboBox::presentationToModel,
                MultiSelectComboBox::modelToPresentation);

        // Create the selection model that manages the currently selected items.
        // The model ensures that items are compared based on their data
        // provider identify, and that the selection only changes if items
        // actually have a different identity in the data provider.
        selectionModel = new MultiSelectComboBoxSelectionModel<>(
                item -> getDataProvider().getId(item));
        addValueChangeListener(e -> {
            // Synchronize selection if value is updated from client
            if (e.isFromClient()) {
                selectionModel.setSelectedItems(e.getValue());
            }
        });
        // Pass identifier provider to selection model when it is changed
        // through a data view
        ComponentEventListener<IdentifierProviderChangeEvent<TItem, ?>> listener = e -> selectionModel
                .setIdentityProvider(e.getIdentifierProvider());
        ComponentUtil.addListener(this, IdentifierProviderChangeEvent.class,
                (ComponentEventListener) listener);
        // Initialize page size and data provider
        setPageSize(pageSize);
        setItems(new DataCommunicator.EmptyDataProvider<>());

        setAutoExpand(AutoExpandMode.NONE);
    }

    /**
     * Creates an empty combo box with the defined label.
     *
     * @param label
     *            the label describing the combo box
     * @see #setLabel(String)
     */
    public MultiSelectComboBox(String label) {
        this();
        setLabel(label);
    }

    /**
     * Creates a combo box with the defined label and populated with the items
     * in the collection.
     *
     * @param label
     *            the label describing the combo box
     * @param items
     *            the items to be shown in the list of the combo box
     * @see #setLabel(String)
     * @see #setItems(Collection)
     */
    public MultiSelectComboBox(String label, Collection<TItem> items) {
        this();
        setLabel(label);
        setItems(items);
    }

    /**
     * Creates a combo box with the defined label and populated with the items
     * in the array.
     *
     * @param label
     *            the label describing the combo box
     * @param items
     *            the items to be shown in the list of the combo box
     * @see #setLabel(String)
     * @see #setItems(Object...)
     */
    @SafeVarargs
    public MultiSelectComboBox(String label, TItem... items) {
        this();
        setLabel(label);
        setItems(items);
    }

    /**
     * Constructs a combo box with a value change listener.
     *
     * @param listener
     *            the value change listener to add
     * @see #addValueChangeListener(ValueChangeListener)
     */
    public MultiSelectComboBox(
            ValueChangeListener<ComponentValueChangeEvent<MultiSelectComboBox<TItem>, Set<TItem>>> listener) {
        this();
        addValueChangeListener(listener);
    }

    /**
     * Constructs a combo box with the defined label and a value change
     * listener.
     *
     * @param label
     *            the label describing the combo box
     * @param listener
     *            the value change listener to add
     * @see #setLabel(String)
     * @see #addValueChangeListener(ValueChangeListener)
     */
    public MultiSelectComboBox(String label,
            ValueChangeListener<ComponentValueChangeEvent<MultiSelectComboBox<TItem>, Set<TItem>>> listener) {
        this(label);
        addValueChangeListener(listener);
    }

    /**
     * Constructs a combo box with the defined label, a value change listener
     * and populated with the items in the array.
     *
     * @param label
     *            the label describing the combo box
     * @param listener
     *            the value change listener to add
     * @param items
     *            the items to be shown in the list of the combo box
     * @see #setLabel(String)
     * @see #addValueChangeListener(ValueChangeListener)
     * @see #setItems(Object...)
     */
    @SafeVarargs
    public MultiSelectComboBox(String label,
            ValueChangeListener<ComponentValueChangeEvent<MultiSelectComboBox<TItem>, Set<TItem>>> listener,
            TItem... items) {
        this(label, listener);
        setItems(items);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        if (getI18n() != null) {
            updateI18n();
        }
    }

    private static <T> Set<T> presentationToModel(
            MultiSelectComboBox<T> multiSelectComboBox,
            JsonArray presentation) {

        DataKeyMapper<T> keyMapper = multiSelectComboBox.getKeyMapper();

        if (presentation == null || keyMapper == null) {
            return multiSelectComboBox.getEmptyValue();
        }

        Set<T> set = new LinkedHashSet<>();
        for (int i = 0; i < presentation.length(); i++) {
            String key = presentation.getObject(i).getString("key");
            set.add(keyMapper.get(key));
        }
        return set;
    }

    private static <T> JsonArray modelToPresentation(
            MultiSelectComboBox<T> multiSelectComboBox, Set<T> model) {
        JsonArray array = Json.createArray();
        if (model == null || model.isEmpty()) {
            return array;
        }

        model.stream().map(multiSelectComboBox::generateJson)
                .forEach(jsonObject -> array.set(array.length(), jsonObject));

        return array;
    }

    private JsonObject generateJson(TItem item) {
        JsonObject jsonObject = Json.createObject();
        jsonObject.put("key", getKeyMapper().key(item));
        getDataGenerator().generateData(item, jsonObject);
        return jsonObject;
    }

    /**
     * Sets whether the user is required to provide a value. When required, an
     * indicator appears next to the label and the field invalidates if the
     * value is cleared.
     * <p>
     * NOTE: The required indicator is only visible when the field has a label,
     * see {@link #setLabel(String)}.
     *
     * @param required
     *            {@code true} to make the field required, {@code false}
     *            otherwise
     * @see MultiSelectComboBoxI18n#setRequiredErrorMessage(String)
     */
    @Override
    public void setRequiredIndicatorVisible(boolean required) {
        super.setRequiredIndicatorVisible(required);
    }

    /**
     * Gets whether the user is required to provide a value.
     *
     * @return {@code true} if the field is required, {@code false} otherwise
     * @see #setRequiredIndicatorVisible(boolean)
     */
    @Override
    public boolean isRequiredIndicatorVisible() {
        return super.isRequiredIndicatorVisible();
    }

    /**
     * Gets the value of the component, which is a set of selected items.
     * <p>
     * The returned set is immutable and can not be modified. Use
     * {@link #select(Object[])} or {@link #deselect(Object[])} to add or remove
     * individual items.
     *
     * @return an unmodifiable set of selected items
     */
    @Override
    public Set<TItem> getValue() {
        return Collections.unmodifiableSet(super.getValue());
    }

    /**
     * Sets the value of the component, which is a set of selected items.
     * <p>
     * Note that it is allowed to pass {@code null} as value to clear the
     * selection, but that an empty set will be stored as value instead.
     *
     * @param value
     *            the new value
     */
    @Override
    public void setValue(Set<TItem> value) {
        if (value == null) {
            value = Collections.emptySet();
        }
        // Update selection first, which returns a boolean indicating whether
        // the selection (=value) has actually changed
        boolean hasValueChanged = selectionModel.setSelectedItems(value);
        if (hasValueChanged) {
            // Only update field value and generate change event if value has
            // actually changed
            super.setValue(value);
        }
    }

    /**
     * Sets the value of the component, which is a set of selected items. As
     * each item can only be selected once, duplicates in the provided items
     * will be removed. Passing no items will result in an empty selection.
     *
     * @param items
     *            the new value
     */
    @SafeVarargs
    public final void setValue(TItem... items) {
        Set<TItem> value = new LinkedHashSet<>(List.of(items));
        setValue(value);
    }

    /**
     * Sets the value of the component, which is a set of selected items. As
     * each item can only be selected once, duplicates in the provided items
     * will be removed. Passing no items will result in an empty selection.
     *
     * @param items
     *            the new value
     */
    public void setValue(Collection<TItem> items) {
        Set<TItem> value = new LinkedHashSet<>(items);
        setValue(value);
    }

    @Override
    protected void refreshValue() {
        Set<TItem> value = getValue();
        if (value == null || value.isEmpty()) {
            return;
        }
        JsonArray selectedItems = modelToPresentation(this, value);
        getElement().setPropertyJson("selectedItems", selectedItems);
    }

    @Override
    public boolean isSelected(TItem item) {
        Objects.requireNonNull(item, "Null can not be selected");
        // Override the default MultiSelect.isSelected implementation to use the
        // selection model, which compares items using the identity provider of
        // the data provider
        return selectionModel.isSelected(item);
    }

    @Override
    public Set<TItem> getSelectedItems() {
        return Collections.unmodifiableSet(selectionModel.getSelectedItems());
    }

    @Override
    public Registration addSelectionListener(
            MultiSelectionListener<MultiSelectComboBox<TItem>, TItem> listener) {
        // Selection is equivalent to the value, so we can reuse the value
        // change listener here
        return addValueChangeListener(event -> listener
                .selectionChange(new MultiSelectionEvent<>(this, this,
                        event.getOldValue(), event.isFromClient())));
    }

    @Override
    public void updateSelection(Set<TItem> addedItems,
            Set<TItem> removedItems) {
        // Update the selection, which returns a boolean indicating whether
        // the selection (=value) has actually changed
        boolean hasValueChanged = selectionModel.updateSelection(addedItems,
                removedItems);
        if (hasValueChanged) {
            // Only update field value and generate change event if value has
            // actually changed
            super.setValue(selectionModel.getSelectedItems());
        }
    }

    /**
     * Defines possible behavior of the component when not all selected items
     * can be displayed as chips within the current field width.
     */
    public enum AutoExpandMode {
        /**
         * Field expands vertically and chips wrap.
         */
        VERTICAL(false, true),

        /**
         * Field expands horizontally until max-width is reached, then collapses
         * to overflow chip.
         */
        HORIZONTAL(true, false),

        /**
         * Field expands horizontally until max-width is reached, then expands
         * vertically and chips wrap.
         */
        BOTH(true, true),

        /**
         * Field does not expand and collapses to overflow chip.
         */
        NONE(false, false);

        private final boolean expandHorizontally;
        private final boolean expandVertically;

        AutoExpandMode(boolean expandHorizontally, boolean expandVertically) {
            this.expandHorizontally = expandHorizontally;
            this.expandVertically = expandVertically;
        }

        /**
         * Gets whether to expand horizontally.
         *
         * @return Whether to expand horizontally
         */
        public boolean getExpandHorizontally() {
            return expandHorizontally;
        }

        /**
         * Gets whether to expand vertically.
         *
         * @return Whether to expand vertically
         */
        public boolean getExpandVertically() {
            return expandVertically;
        }
    }

    /**
     * Gets the behavior of the component when not all selected items can be
     * displayed as chips within the current field width.
     *
     * @since 24.3
     * @return The current {@link AutoExpandMode}
     */
    public AutoExpandMode getAutoExpand() {
        return autoExpand;
    }

    /**
     * Sets the behavior of the component when not all selected items can be
     * displayed as chips within the current field width.
     *
     * Expansion only works with undefined size in the desired direction (i.e.
     * setting `max-width` limits the component's width).
     *
     * @param {AutoExpandMode}
     *            autoExpandMode
     * @since 24.3
     */
    public void setAutoExpand(AutoExpandMode autoExpandMode) {
        Objects.requireNonNull(autoExpandMode,
                "The mode to be set cannot be null");
        autoExpand = autoExpandMode;

        getElement().setProperty("autoExpandHorizontally",
                autoExpandMode.getExpandHorizontally());
        getElement().setProperty("autoExpandVertically",
                autoExpandMode.getExpandVertically());
    }

    /**
     * Gets whether selected items are grouped at the top of the overlay.
     *
     * @since 24.3
     * @return {@code true} if enabled, {@code false} otherwise
     */
    public boolean isSelectedItemsOnTop() {
        return getElement().getProperty("selectedItemsOnTop", false);
    }

    /**
     * Enables or disables grouping of the selected items at the top of the
     * overlay.
     *
     * @since 24.3
     * @param selectedItemsOnTop
     *            {@code true} to group selected items at the top
     */
    public void setSelectedItemsOnTop(boolean selectedItemsOnTop) {
        getElement().setProperty("selectedItemsOnTop", selectedItemsOnTop);
    }

    /**
     * Gets whether the filter is kept after selecting items. {@code false} by
     * default.
     *
     * @since 24.4
     * @return {@code true} if enabled, {@code false} otherwise
     */
    public boolean isKeepFilter() {
        return getElement().getProperty("keepFilter", false);
    }

    /**
     * Enables or disables keeping the filter after selecting items. By default,
     * the filter is cleared after selecting an item and the overlay shows the
     * unfiltered list of items again. Enabling this option will keep the
     * filter, which allows to select multiple filtered items in succession.
     *
     * @param keepFilter
     *            whether to keep the filter after selecting an item
     */
    public void setKeepFilter(boolean keepFilter) {
        getElement().setProperty("keepFilter", keepFilter);
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link #setI18n(MultiSelectComboBoxI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public MultiSelectComboBoxI18n getI18n() {
        return (MultiSelectComboBoxI18n) super.getI18n();
    }

    /**
     * Sets the internationalization object for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(MultiSelectComboBoxI18n i18n) {
        super.setI18n(i18n);
        updateI18n();
    }

    /**
     * Update I18N settings in the web component. Merges the
     * {@link MultiSelectComboBoxI18n} settings with the current / default
     * settings of the web component.
     */
    private void updateI18n() {
        JsonObject i18nJson = (JsonObject) JsonSerializer.toJson(getI18n());

        // Remove null values so that we don't overwrite existing WC
        // translations with empty ones
        removeNullValuesFromJsonObject(i18nJson);

        // Remove the error message properties because they aren't used on
        // the client-side.
        i18nJson.remove("requiredErrorMessage");

        // Assign new I18N object to WC, by merging the existing
        // WC I18N, and the values from the new I18n instance,
        // into an empty object
        getElement().executeJs("this.i18n = Object.assign({}, this.i18n, $0);",
                i18nJson);
    }

    private void removeNullValuesFromJsonObject(JsonObject jsonObject) {
        for (String key : jsonObject.keys()) {
            if (jsonObject.get(key).getType() == JsonType.NULL) {
                jsonObject.remove(key);
            }
        }
    }

    /**
     * Sets the dropdown overlay width.
     *
     * @param width
     *            the new dropdown width. Pass in null to set the dropdown width
     *            back to the default value.
     */
    public void setOverlayWidth(String width) {
        getStyle().set("--vaadin-multi-select-combo-box-overlay-width", width);
    }

    /**
     * Sets the dropdown overlay width. Negative number implies unspecified size
     * (the dropdown width is reverted back to the default value).
     *
     * @param width
     *            the width of the dropdown.
     * @param unit
     *            the unit used for the dropdown.
     */
    public void setOverlayWidth(float width, Unit unit) {
        Objects.requireNonNull(unit, "Unit can not be null");
        setOverlayWidth(HasSize.getCssSize(width, unit));
    }
}
