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

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.DataProvider;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/multi-select-combo-box", version = "23.1.0")
@JsModule("@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box.js")
@JsModule("@vaadin/polymer-legacy-adapter/template-renderer.js")
@JsModule("./flow-component-renderer.js")
@JsModule("./comboBoxConnector.js")
public class MultiSelectComboBox<TItem>
        extends ComboBoxBase<MultiSelectComboBox<TItem>, TItem, Set<TItem>> {

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
    public MultiSelectComboBox(int pageSize) {
        super("selectedItems", Collections.emptySet(), JsonArray.class,
                MultiSelectComboBox::presentationToModel,
                MultiSelectComboBox::modelToPresentation);

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

    private static <T> Set<T> presentationToModel(
            MultiSelectComboBox<T> multiSelectComboBox,
            JsonArray presentation) {

        DataKeyMapper<T> keyMapper = multiSelectComboBox.getKeyMapper();

        if (presentation == null || keyMapper == null) {
            return multiSelectComboBox.getEmptyValue();
        }

        Set<T> set = new HashSet<>();
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
        super.setValue(value);
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
    protected boolean isSelected(TItem item) {
        if (item == null)
            return false;

        DataProvider<TItem, ?> dataProvider = getDataProvider();
        Object itemId = dataProvider.getId(item);

        return getValue().stream().anyMatch(selectedItem -> Objects
                .equals(itemId, dataProvider.getId(selectedItem)));
    }
}
