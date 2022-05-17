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

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasClearButton;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.combobox.events.CustomValueSetEvent;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.function.SerializableBiPredicate;
import com.vaadin.flow.shared.Registration;

import elemental.json.Json;
import elemental.json.JsonObject;

/**
 * Combo Box allows the user to choose a value from a filterable list of options
 * presented in an overlay.
 * <p>
 * ComboBox supports lazy loading. This means that when using large data sets,
 * items are requested from the server one "page" at a time when the user
 * scrolls down the overlay. The number of items in one page is by default 50,
 * and can be changed with {@link #setPageSize(int)}.
 * <p>
 * ComboBox can do filtering either in the browser or in the server. When
 * ComboBox has only a relatively small set of items, the filtering will happen
 * in the browser, allowing smooth user-experience. When the size of the data
 * set is larger than the {@code pageSize}, the webcomponent doesn't necessarily
 * have all the data available and it will make requests to the server to handle
 * the filtering. Also, if you have defined custom filtering logic, with eg.
 * {@link #setItems(ItemFilter, Collection)}, filtering will happen in the
 * server. To enable client-side filtering with larger data sets, you can
 * override the {@code pageSize} to be bigger than the size of your data set.
 * However, then the full data set will be sent to the client immediately and
 * you will lose the benefits of lazy loading.
 *
 * @param <T>
 *            the type of the items to be inserted in the combo box
 * @author Vaadin Ltd
 */
@Tag("vaadin-combo-box")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta4")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/combo-box", version = "23.1.0-beta4")
@NpmPackage(value = "@vaadin/vaadin-combo-box", version = "23.1.0-beta4")
@JsModule("@vaadin/combo-box/src/vaadin-combo-box.js")
@JsModule("@vaadin/polymer-legacy-adapter/template-renderer.js")
@JsModule("./flow-component-renderer.js")
@JsModule("./comboBoxConnector.js")
public class ComboBox<T> extends ComboBoxBase<ComboBox<T>, T, T> implements
        HasSize, HasValidation, HasHelper, HasTheme, HasLabel, HasClearButton {

    private static final String PROP_INPUT_ELEMENT_VALUE = "_inputElementValue";
    private static final String PROP_SELECTED_ITEM = "selectedItem";
    private static final String PROP_VALUE = "value";

    /**
     * A callback method for fetching items. The callback is provided with a
     * non-null string filter, offset index and limit.
     *
     * @param <T>
     *            item (bean) type in ComboBox
     */
    @FunctionalInterface
    public interface FetchItemsCallback<T> extends Serializable {

        /**
         * Returns a stream of items that match the given filter, limiting the
         * results with given offset and limit.
         *
         * @param filter
         *            a non-null filter string
         * @param offset
         *            the first index to fetch
         * @param limit
         *            the fetched item count
         * @return stream of items
         */
        Stream<T> fetchItems(String filter, int offset, int limit);
    }

    private class CustomValueRegistration implements Registration {

        private Registration delegate;

        private CustomValueRegistration(Registration delegate) {
            this.delegate = delegate;
        }

        @Override
        public void remove() {
            if (delegate != null) {
                delegate.remove();
                customValueListenersCount--;

                if (customValueListenersCount == 0) {
                    setAllowCustomValue(false);
                }
                delegate = null;
            }
        }
    }

    /**
     * Predicate to check {@link ComboBox} items against user typed strings.
     */
    @FunctionalInterface
    public interface ItemFilter<T> extends SerializableBiPredicate<T, String> {
        @Override
        boolean test(T item, String filterText);
    }

    private int customValueListenersCount;

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
     * @see {@link #setPageSize(int)}
     */
    public ComboBox(int pageSize) {
        super("value", null, String.class, ComboBox::presentationToModel,
                ComboBox::modelToPresentation);

        // Configure web component to use key property from the generated
        // wrapper items for identification
        getElement().setProperty("itemValuePath", "key");
        getElement().setProperty("itemIdPath", "key");

        setPageSize(pageSize);

        addAttachListener(e -> initConnector());

        setItems(new DataCommunicator.EmptyDataProvider<>());

        getElement().setAttribute("suppress-template-warning", true);

        // Synchronize input element value property state when setting a custom
        // value. This is necessary to allow clearing the input value in
        // `ComboBox.refreshValue`. If the input element value is not
        // synchronized here, then setting the property to an empty value would
        // not trigger a client update. Need to use `super` here, in order to
        // avoid enabling custom values, which is a side effect of
        // `ComboBox.addCustomValueSetListener`.
        super.addCustomValueSetListener(e -> this.getElement()
                .setProperty(PROP_INPUT_ELEMENT_VALUE, e.getDetail()));

        addValueChangeListener(
                e -> getDataCommunicator().notifySelectionChanged());
    }

    /**
     * Default constructor. Creates an empty combo box.
     */
    public ComboBox() {
        this(50);
    }

    /**
     * Creates an empty combo box with the defined label.
     *
     * @param label
     *            the label describing the combo box
     * @see #setLabel(String)
     */
    public ComboBox(String label) {
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
    public ComboBox(String label, Collection<T> items) {
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
    public ComboBox(String label, T... items) {
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
    public ComboBox(
            ValueChangeListener<ComponentValueChangeEvent<ComboBox<T>, T>> listener) {
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
    public ComboBox(String label,
            ValueChangeListener<ComponentValueChangeEvent<ComboBox<T>, T>> listener) {
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
    public ComboBox(String label,
            ValueChangeListener<ComponentValueChangeEvent<ComboBox<T>, T>> listener,
            T... items) {
        this(label, listener);
        setItems(items);
    }

    private static <T> T presentationToModel(ComboBox<T> comboBox,
            String presentation) {
        if (presentation == null || comboBox.getDataCommunicator() == null) {
            return comboBox.getEmptyValue();
        }
        return comboBox.getKeyMapper().get(presentation);
    }

    private static <T> String modelToPresentation(ComboBox<T> comboBox,
            T model) {
        if (model == null) {
            return null;
        }
        return comboBox.getKeyMapper().key(model);
    }

    /**
     * Whether the component should block user input that does not match the
     * configured pattern
     */
    public boolean isPreventInvalidInput() {
        return getElement().getProperty("preventInvalidInput", false);
    }

    /**
     * Sets whether the component should block user input that does not match
     * the configured pattern
     */
    public void setPreventInvalidInput(boolean preventInvalidInput) {
        getElement().setProperty("preventInvalidInput", preventInvalidInput);
    }

    /**
     * The pattern to validate the input with
     */
    public String getPattern() {
        return getElement().getProperty("pattern");
    }

    /**
     * Sets the pattern with which to validate the input
     */
    public void setPattern(String pattern) {
        getElement().setProperty("pattern", pattern == null ? "" : pattern);
    }

    @Override
    public void setValue(T value) {
        if (getDataCommunicator() == null || getDataCommunicator()
                .getDataProvider() instanceof DataCommunicator.EmptyDataProvider) {
            if (value == null) {
                return;
            } else {
                throw new IllegalStateException(
                        "Cannot set a value for a ComboBox without items. "
                                + "Use setItems to populate items into the "
                                + "ComboBox before setting a value.");
            }
        }
        super.setValue(value);
        refreshValue();
    }

    @Override
    protected void refreshValue() {
        T value = getValue();

        DataKeyMapper<T> keyMapper = getKeyMapper();
        if (value != null && keyMapper.has(value)) {
            value = keyMapper.get(keyMapper.key(value));
        }

        if (value == null) {
            getElement().setProperty(PROP_SELECTED_ITEM, null);
            getElement().setProperty(PROP_VALUE, "");
            getElement().setProperty(PROP_INPUT_ELEMENT_VALUE, "");
            return;
        }

        // This ensures that the selection works even with lazy loading when the
        // item is not yet loaded
        JsonObject json = Json.createObject();
        json.put("key", keyMapper.key(value));
        getDataGenerator().generateData(value, json);
        getElement().setPropertyJson("selectedItem", json);
        getElement().setProperty(PROP_VALUE, keyMapper.key(value));
        getElement().setProperty(PROP_INPUT_ELEMENT_VALUE,
                generateLabel(value));
    }

    @Override
    protected boolean isSelected(T item) {
        T value = getValue();
        DataProvider<T, ?> dataProvider = getDataProvider();
        if (dataProvider == null || item == null || value == null)
            return false;

        return Objects.equals(dataProvider.getId(item),
                dataProvider.getId(value));
    }

    @Override
    public T getEmptyValue() {
        return null;
    }

    @Override
    public Registration addCustomValueSetListener(
            ComponentEventListener<CustomValueSetEvent<ComboBox<T>>> listener) {
        setAllowCustomValue(true);
        customValueListenersCount++;
        Registration registration = super.addCustomValueSetListener(listener);
        return new CustomValueRegistration(registration);
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        super.setRequiredIndicatorVisible(requiredIndicatorVisible);
        runBeforeClientResponse(ui -> getElement().callJsFunction(
                "$connector.enableClientValidation",
                !requiredIndicatorVisible));
    }

    private void initConnector() {
        getElement().executeJs(
                "window.Vaadin.Flow.comboBoxConnector.initLazy(this)");
    }

    /**
     * Adds theme variants to the component.
     *
     * @param variants
     *            theme variants to add
     */
    public void addThemeVariants(ComboBoxVariant... variants) {
        getThemeNames()
                .addAll(Stream.of(variants).map(ComboBoxVariant::getVariantName)
                        .collect(Collectors.toList()));
    }

    /**
     * Removes theme variants from the component.
     *
     * @param variants
     *            theme variants to remove
     */
    public void removeThemeVariants(ComboBoxVariant... variants) {
        getThemeNames().removeAll(
                Stream.of(variants).map(ComboBoxVariant::getVariantName)
                        .collect(Collectors.toList()));
    }

}
