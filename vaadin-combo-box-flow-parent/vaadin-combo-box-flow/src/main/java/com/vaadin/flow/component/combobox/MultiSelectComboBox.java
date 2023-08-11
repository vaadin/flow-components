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
package com.vaadin.flow.component.combobox;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasThemeVariant;
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
 *
 * @param <TItem>
 *            the type of the items to be selectable from the combo box
 * @author Vaadin Ltd
 */
@Tag("vaadin-multi-select-combo-box")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.3.20")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/multi-select-combo-box", version = "23.3.20")
@JsModule("@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box.js")
@JsModule("@vaadin/polymer-legacy-adapter/template-renderer.js")
@JsModule("./flow-component-renderer.js")
@JsModule("./comboBoxConnector.js")
public class MultiSelectComboBox<TItem>
        extends ComboBoxBase<MultiSelectComboBox<TItem>, TItem, Set<TItem>>
        implements MultiSelect<MultiSelectComboBox<TItem>, TItem>,
        HasThemeVariant<MultiSelectComboBoxVariant> {

    private final MultiSelectComboBoxSelectionModel<TItem> selectionModel;
    private MultiSelectComboBoxI18n i18n;

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

        if (i18n != null) {
            this.updateI18n();
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
     * Gets the internationalization object previously set for this component.
     * <p>
     * Note: updating the i18n object that is returned from this method will not
     * update the component, unless it is set again using
     * {@link #setI18n(MultiSelectComboBoxI18n)}
     *
     * @return the i18n object. It will be <code>null</code>, if it has not been
     *         set previously
     */
    public MultiSelectComboBoxI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization properties for this component.
     *
     * @param i18n
     *            the internationalized properties, not <code>null</code>
     */
    public void setI18n(MultiSelectComboBoxI18n i18n) {
        Objects.requireNonNull(i18n,
                "The I18N properties object should not be null");
        this.i18n = i18n;
        updateI18n();
    }

    /**
     * Update I18N settings in the web component. Merges the
     * {@link MultiSelectComboBoxI18n} settings with the current / default
     * settings of the web component.
     */
    private void updateI18n() {
        JsonObject i18nJson = (JsonObject) JsonSerializer.toJson(this.i18n);

        // Remove null values so that we don't overwrite existing WC
        // translations with empty ones
        removeNullValuesFromJsonObject(i18nJson);

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
}
