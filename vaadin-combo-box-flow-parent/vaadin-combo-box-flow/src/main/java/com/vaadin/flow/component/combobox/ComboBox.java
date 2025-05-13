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

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.function.SerializableBiPredicate;

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
 * have all the data available, and it will make requests to the server to
 * handle the filtering. Also, if you have defined custom filtering logic, with
 * eg. {@link #setItems(ItemFilter, Collection)}, filtering will happen in the
 * server. To enable client-side filtering with larger data sets, you can
 * override the {@code pageSize} to be bigger than the size of your data set.
 * However, then the full data set will be sent to the client immediately, and
 * you will lose the benefits of lazy loading.
 * <h2>Validation</h2>
 * <p>
 * Combo Box comes with a built-in validation mechanism that verifies that the
 * field is not empty when {@link #setRequiredIndicatorVisible(boolean)
 * required} is enabled.
 * <p>
 * Validation is triggered whenever the user initiates a value change, for
 * example by selection from the dropdown or manual entry followed by Enter or
 * blur. Programmatic value changes trigger validation as well. If validation
 * fails, the component is marked as invalid and an error message is displayed
 * below the input.
 * <p>
 * The required error message can be configured using either
 * {@link ComboBoxI18n#setRequiredErrorMessage(String)} or
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
 * @param <T>
 *            the type of the items to be selectable from the combo box
 * @author Vaadin Ltd
 */
@Tag("vaadin-combo-box")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/combo-box", version = "24.8.0-alpha18")
@JsModule("@vaadin/combo-box/src/vaadin-combo-box.js")
@JsModule("./flow-component-renderer.js")
@JsModule("./comboBoxConnector.js")
public class ComboBox<T> extends ComboBoxBase<ComboBox<T>, T, T>
        implements HasPrefix, HasThemeVariant<ComboBoxVariant> {

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

    /**
     * Predicate to check {@link ComboBox} items against user typed strings.
     */
    @FunctionalInterface
    public interface ItemFilter<T> extends SerializableBiPredicate<T, String> {
        @Override
        boolean test(T item, String filterText);
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
    public ComboBox(int pageSize) {
        super("value", null, String.class, ComboBox::presentationToModel,
                ComboBox::modelToPresentation);
        setPageSize(pageSize);
        setItems(new DataCommunicator.EmptyDataProvider<>());

        // Sync server-side `selectedItem` property from client, so that the
        // client's property value can be restored when re-attaching
        addValueChangeListener(event -> {
            if (event.isFromClient()) {
                refreshValue();
            }
        });
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
        DataKeyMapper<T> keyMapper = comboBox.getKeyMapper();

        if (presentation == null || keyMapper == null) {
            return comboBox.getEmptyValue();
        }
        return keyMapper.get(presentation);
    }

    private static <T> String modelToPresentation(ComboBox<T> comboBox,
            T model) {
        DataKeyMapper<T> keyMapper = comboBox.getKeyMapper();

        if (model == null || keyMapper == null) {
            return null;
        }
        return keyMapper.key(model);
    }

    /**
     * @see ComboBoxI18n#setRequiredErrorMessage(String)
     */
    @Override
    public void setRequiredIndicatorVisible(boolean required) {
        super.setRequiredIndicatorVisible(required);
    }

    /**
     * The pattern to validate the input with
     *
     * @return the pattern to validate the input with
     */
    public String getPattern() {
        return getElement().getProperty("pattern");
    }

    /**
     * Sets the pattern with which to validate the input
     *
     * @param pattern
     *            the pattern to validate the input with
     */
    public void setPattern(String pattern) {
        getElement().setProperty("pattern", pattern == null ? "" : pattern);
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
            // Force _inputElementValue update on the client-side by using
            // `executeJs` to ensure the input's value will be cleared even
            // if the component's value hasn't changed. The latter can be
            // the case when calling `clear()` in a `customValueSet` listener
            // which is triggered before any value is committed.
            getElement().executeJs("this._inputElementValue = $0", "");
            return;
        }

        // This ensures that the selection works even with lazy loading when the
        // item is not yet loaded
        JsonObject json = Json.createObject();
        json.put("key", keyMapper.key(value));
        getDataGenerator().generateData(value, json);
        getElement().setPropertyJson(PROP_SELECTED_ITEM, json);
        getElement().setProperty(PROP_VALUE, keyMapper.key(value));
        getElement().executeJs("this._inputElementValue = $0",
                generateLabel(value));
    }

    @Override
    protected boolean isSelected(T item) {
        T value = getValue();
        DataProvider<T, ?> dataProvider = getDataProvider();
        if (dataProvider == null || item == null || value == null) {
            return false;
        }

        return Objects.equals(dataProvider.getId(item),
                dataProvider.getId(value));
    }

    @Override
    public T getEmptyValue() {
        return null;
    }

    /**
     * Sets the dropdown overlay width.
     *
     * @param width
     *            the new dropdown width. Pass in null to set the dropdown width
     *            back to the default value.
     */
    public void setOverlayWidth(String width) {
        getStyle().set("--vaadin-combo-box-overlay-width", width);
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

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link #setI18n(ComboBoxI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public ComboBoxI18n getI18n() {
        return (ComboBoxI18n) super.getI18n();
    }

    /**
     * Sets the internationalization object for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(ComboBoxI18n i18n) {
        super.setI18n(i18n);
    }

    /**
     * The internationalization properties for {@link ComboBox}.
     */
    public static class ComboBoxI18n implements ComboBoxBaseI18n {

        private String requiredErrorMessage;

        /**
         * Gets the error message displayed when the field is required but
         * empty.
         *
         * @return the error message or {@code null} if not set
         * @see ComboBox#isRequiredIndicatorVisible()
         * @see ComboBox#setRequiredIndicatorVisible(boolean)
         */
        public String getRequiredErrorMessage() {
            return requiredErrorMessage;
        }

        /**
         * Sets the error message to display when the field is required but
         * empty.
         * <p>
         * Note, custom error messages set with
         * {@link ComboBox#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see ComboBox#isRequiredIndicatorVisible()
         * @see ComboBox#setRequiredIndicatorVisible(boolean)
         */
        public ComboBoxI18n setRequiredErrorMessage(String errorMessage) {
            requiredErrorMessage = errorMessage;
            return this;
        }
    }
}
