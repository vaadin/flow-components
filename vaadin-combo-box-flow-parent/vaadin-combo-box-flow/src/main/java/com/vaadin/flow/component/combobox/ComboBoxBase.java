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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.HasPlaceholder;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.dataview.ComboBoxDataView;
import com.vaadin.flow.component.combobox.dataview.ComboBoxLazyDataView;
import com.vaadin.flow.component.combobox.dataview.ComboBoxListDataView;
import com.vaadin.flow.component.shared.HasAllowedCharPattern;
import com.vaadin.flow.component.shared.HasAutoOpen;
import com.vaadin.flow.component.shared.HasClearButton;
import com.vaadin.flow.component.shared.HasOverlayClassName;
import com.vaadin.flow.component.shared.HasValidationProperties;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.component.shared.internal.ValidationController;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataView;
import com.vaadin.flow.data.provider.HasDataView;
import com.vaadin.flow.data.provider.HasLazyDataView;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.ListDataView;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

/**
 * Provides base functionality for combo box related components, such as
 * {@link ComboBox}
 *
 * @param <TComponent>
 *            Type of the component that extends from this class
 * @param <TItem>
 *            Type of individual items that are selectable in the combo box
 * @param <TValue>
 *            Type of the selection / value of the extending component
 */
public abstract class ComboBoxBase<TComponent extends ComboBoxBase<TComponent, TItem, TValue>, TItem, TValue>
        extends AbstractSinglePropertyField<TComponent, TValue>
        implements Focusable<TComponent>, HasAllowedCharPattern, HasAriaLabel,
        HasAutoOpen, HasClearButton, HasOverlayClassName,
        HasDataView<TItem, String, ComboBoxDataView<TItem>>,
        InputField<AbstractField.ComponentValueChangeEvent<TComponent, TValue>, TValue>,
        HasLazyDataView<TItem, String, ComboBoxLazyDataView<TItem>>,
        HasListDataView<TItem, ComboBoxListDataView<TItem>>, HasTheme,
        HasValidationProperties, HasValidator<TValue>, HasPlaceholder {

    /**
     * Registration for custom value listeners that disallows entering custom
     * values as soon as there are no more listeners for the custom value event
     */
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

    private ItemLabelGenerator<TItem> itemLabelGenerator = String::valueOf;
    private SerializableFunction<TItem, String> classNameGenerator = item -> null;
    private final ComboBoxRenderManager<TItem> renderManager;
    private final ComboBoxDataController<TItem> dataController;
    private int customValueListenersCount;

    private ComboBoxBaseI18n i18n;

    private Validator<TValue> defaultValidator = (value, context) -> {
        boolean fromComponent = context == null;

        // Do the required check only if the validator is called from the
        // component, and not from Binder. Binder has its own implementation
        // of required validation.
        boolean isRequired = fromComponent && isRequiredIndicatorVisible();
        return ValidationUtil.validateRequiredConstraint(
                getI18nErrorMessage(ComboBoxBaseI18n::getRequiredErrorMessage),
                isRequired, getValue(), getEmptyValue());
    };

    private ValidationController<ComboBoxBase<TComponent, TItem, TValue>, TValue> validationController = new ValidationController<>(
            this);

    /**
     * Constructs a new ComboBoxBase instance
     *
     * @param valuePropertyName
     *            name of the value property of the web component that should be
     *            used to set values, or listen to value changes
     * @param defaultValue
     *            the default value of the component
     * @param valuePropertyType
     *            the class that represents the type of the raw value of the
     *            Flow element property
     * @param presentationToModel
     *            a function to convert a raw property value into a value using
     *            the user-specified model type
     * @param modelToPresentation
     *            a function to convert a value using the user-specified model
     *            type into a raw property value
     * @param <TValueProperty>
     *            the type of the raw value of the Flow element property
     */
    public <TValueProperty> ComboBoxBase(String valuePropertyName,
            TValue defaultValue, Class<TValueProperty> valuePropertyType,
            SerializableBiFunction<TComponent, TValueProperty, TValue> presentationToModel,
            SerializableBiFunction<TComponent, TValue, TValueProperty> modelToPresentation) {
        super(valuePropertyName, defaultValue, valuePropertyType,
                presentationToModel, modelToPresentation);

        getElement().setProperty("manualValidation", true);

        // Extracted as implementation to fix serialization issue:
        // https://github.com/vaadin/flow-components/issues/4420
        // Do not replace with method reference
        SerializableSupplier<Locale> localeSupplier = new SerializableSupplier<Locale>() {
            @Override
            public Locale get() {
                return ComboBoxBase.this.getLocale();
            }
        };

        renderManager = new ComboBoxRenderManager<>(this);
        dataController = new ComboBoxDataController<>(this, localeSupplier);
        dataController.getDataGenerator().addDataGenerator((item,
                jsonObject) -> jsonObject.put("label", generateLabel(item)));
        dataController.getDataGenerator()
                .addDataGenerator((item, jsonObject) -> jsonObject
                        .put("className", generateClassName(item)));

        // Configure web component to use key property from the generated
        // wrapper items for identification
        getElement().setProperty("itemValuePath", "key");
        getElement().setProperty("itemIdPath", "key");

        // Notify data communicator when selection changes, which allows to
        // free up items / keys in the KeyMapper that are not used anymore in
        // the selection
        addValueChangeListener(
                e -> getDataCommunicator().notifySelectionChanged());

        addValueChangeListener(e -> validate());
    }

    /**
     * Whether the component should automatically receive focus when the page
     * loads.
     *
     * @return {@code true} if the component should automatically receive focus
     */
    public boolean isAutofocus() {
        return getElement().getProperty("autofocus", false);
    }

    /**
     * Sets the whether the component should automatically receive focus when
     * the page loads. Defaults to {@code false}.
     *
     * @param autofocus
     *            {@code true} component should automatically receive focus
     */
    public void setAutofocus(boolean autofocus) {
        getElement().setProperty("autofocus", autofocus);
    }

    /**
     * Gets the page size, which is the number of items fetched at a time from
     * the data provider.
     * <p>
     * The page size is also the largest number of items that can support
     * client-side filtering. If you provide more items than the page size, the
     * component has to fall back to server-side filtering.
     * <p>
     * The default page size is 50.
     *
     * @return the maximum number of items sent per request
     * @see #setPageSize(int)
     */
    public int getPageSize() {
        return getElement().getProperty("pageSize", 50);
    }

    /**
     * Sets the page size, which is the number of items requested at a time from
     * the data provider. This does not guarantee a maximum query size to the
     * backend; when the overlay has room to render more new items than the page
     * size, multiple "pages" will be requested at once.
     * <p>
     * The page size is also the largest number of items that can support
     * client-side filtering. If you provide more items than the page size, the
     * component has to fall back to server-side filtering.
     * <p>
     * Setting the page size after the ComboBox has been rendered effectively
     * resets the component, and the current page(s) and sent over again.
     * <p>
     * The default page size is 50.
     *
     * @param pageSize
     *            the maximum number of items sent per request, should be
     *            greater than zero
     */
    public void setPageSize(int pageSize) {
        if (pageSize < 1) {
            throw new IllegalArgumentException(
                    "Page size should be greater than zero.");
        }
        getElement().setProperty("pageSize", pageSize);
        dataController.setPageSize(pageSize);
    }

    /**
     * Whether the dropdown is opened or not.
     *
     * @return {@code true} if the drop-down is opened, {@code false} otherwise
     */
    @Synchronize(property = "opened", value = "opened-changed")
    public boolean isOpened() {
        return getElement().getProperty("opened", false);
    }

    /**
     * Sets whether the dropdown should be opened or not.
     *
     * @param opened
     *            {@code true} to open the drop-down, {@code false} to close it
     */
    public void setOpened(boolean opened) {
        getElement().setProperty("opened", opened);
    }

    /**
     * If {@code true}, the user can input string values that do not match to
     * any existing item labels, which will fire a {@link CustomValueSetEvent}.
     *
     * @return {@code true} if the component fires custom value set events,
     *         {@code false} otherwise
     * @see #setAllowCustomValue(boolean)
     * @see #addCustomValueSetListener(ComponentEventListener)
     */
    public boolean isAllowCustomValue() {
        return getElement().getProperty("allowCustomValue", false);
    }

    /**
     * Enables or disables the component firing events for custom string input.
     * <p>
     * When enabled, a {@link CustomValueSetEvent} will be fired when the user
     * inputs a string value that does not match any existing items and commits
     * it eg. by blurring or pressing the enter-key.
     * <p>
     * Note that ComboBox doesn't do anything with the custom value string
     * automatically. Use the
     * {@link #addCustomValueSetListener(ComponentEventListener)} method to
     * determine how the custom value should be handled. For example, when the
     * ComboBox has {@code String} as the value type, you can add a listener
     * which sets the custom string as the value of the ComboBox with
     * {@link #setValue(Object)}.
     * <p>
     * Setting to {@code true} also allows an unfocused ComboBox to display a
     * string that doesn't match any of its items nor its current value, unless
     * this is explicitly handled with
     * {@link #addCustomValueSetListener(ComponentEventListener)}. When set to
     * {@code false}, an unfocused ComboBox will always display the label of the
     * currently selected item.
     *
     * @param allowCustomValue
     *            {@code true} to enable custom value set events, {@code false}
     *            to disable them
     * @see #addCustomValueSetListener(ComponentEventListener)
     */
    public void setAllowCustomValue(boolean allowCustomValue) {
        getElement().setProperty("allowCustomValue", allowCustomValue);
    }

    /**
     * Filtering string the user has typed into the input field.
     *
     * @return the filter string
     */
    @Synchronize(property = "filter", value = "filter-changed")
    protected String getFilter() {
        return getElement().getProperty("filter");
    }

    /**
     * Sets the filter string for the filter input.
     * <p>
     * Setter is only required to allow using @Synchronize
     *
     * @param filter
     *            the String value to set
     */
    protected void setFilter(String filter) {
        getElement().setProperty("filter", filter == null ? "" : filter);
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
     * Sets the item label generator that is used to produce the strings shown
     * in the combo box for each item. By default,
     * {@link String#valueOf(Object)} is used.
     * <p>
     * When the {@link #setRenderer(Renderer)} is used, the ItemLabelGenerator
     * is only used to show the selected item label.
     *
     * @param itemLabelGenerator
     *            the item label provider to use, not null
     */
    public void setItemLabelGenerator(
            ItemLabelGenerator<TItem> itemLabelGenerator) {
        Objects.requireNonNull(itemLabelGenerator,
                "The item label generator can not be null");
        this.itemLabelGenerator = itemLabelGenerator;
        dataController.reset();
        if (getValue() != null) {
            refreshValue();
        }
    }

    /**
     * Gets the item label generator that is used to produce the strings shown
     * in the combo box for each item.
     *
     * @return the item label generator used, not null
     */
    public ItemLabelGenerator<TItem> getItemLabelGenerator() {
        return itemLabelGenerator;
    }

    /**
     * Generates a string label for a data item using the current item label
     * generator
     *
     * @param item
     *            the data item
     * @return string label for the data item
     */
    protected String generateLabel(TItem item) {
        if (item == null) {
            return "";
        }
        String label = getItemLabelGenerator().apply(item);
        if (label == null) {
            throw new IllegalStateException(String.format(
                    "Got 'null' as a label value for the item '%s'. "
                            + "'%s' instance may not return 'null' values",
                    item, ItemLabelGenerator.class.getSimpleName()));
        }
        return label;
    }

    /**
     * Sets the function that is used for generating CSS class names for the
     * dropdown items in the ComboBox. Returning {@code null} from the generator
     * results in no custom class name being set. Multiple class names can be
     * returned from the generator as space-separated.
     *
     * @since 24.5
     * @param classNameGenerator
     *            the class name generator to set, not {@code null}
     * @throws NullPointerException
     *             if {@code classNameGenerator} is {@code null}
     */
    public void setClassNameGenerator(
            SerializableFunction<TItem, String> classNameGenerator) {
        Objects.requireNonNull(classNameGenerator,
                "Class name generator can not be null");
        this.classNameGenerator = classNameGenerator;
        dataController.reset();
    }

    /**
     * Gets the item class name generator that is used for generating CSS class
     * names for the dropdown items in the ComboBox.
     *
     * @since 24.5
     * @return the item class name generator, not null
     */
    public SerializableFunction<TItem, String> getItemClassNameGenerator() {
        return classNameGenerator;
    }

    /**
     * Generates a string class name for a data item using the current item
     * class name generator
     *
     * @param item
     *            the data item
     * @return string class name for the data item
     */
    protected String generateClassName(TItem item) {
        if (item == null) {
            return "";
        }
        String label = getItemClassNameGenerator().apply(item);
        if (label == null) {
            return "";
        }
        return label;
    }

    /**
     * Sets the Renderer responsible to render the individual items in the list
     * of possible choices of the ComboBox. It doesn't affect how the selected
     * item is rendered - that can be configured by using
     * {@link #setItemLabelGenerator(ItemLabelGenerator)}.
     *
     * @param renderer
     *            a renderer for the items in the selection list of the
     *            ComboBox, not <code>null</code>
     *            <p>
     *            Note that filtering of the ComboBox is not affected by the
     *            renderer that is set here. Filtering is done on the original
     *            values and can be affected by
     *            {@link #setItemLabelGenerator(ItemLabelGenerator)}.
     */
    public void setRenderer(Renderer<TItem> renderer) {
        Objects.requireNonNull(renderer, "The renderer must not be null");

        renderManager.setRenderer(renderer);
    }

    @Override
    public void setValue(TValue value) {
        if (getDataCommunicator() == null
                || getDataProvider() instanceof DataCommunicator.EmptyDataProvider) {
            if (valueEquals(value, getEmptyValue())) {
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
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initConnector();
        dataController.onAttach();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        dataController.onDetach();
        super.onDetach(detachEvent);
    }

    /**
     * Adds a listener for the event which is fired when user inputs a string
     * value that does not match any existing items and commits it eg. by
     * blurring or pressing the enter-key.
     * <p>
     * Note that ComboBox doesn't do anything with the custom value string
     * automatically. Use this method to determine how the custom value should
     * be handled. For example, when the ComboBox has {@code String} as the
     * value type, you can add a listener which sets the custom string as the
     * value of the ComboBox with {@link #setValue(Object)}.
     * <p>
     * As a side effect, this makes the ComboBox allow custom values. If you
     * want to disable the firing of custom value set events once the listener
     * is added, please disable it explicitly via the
     * {@link #setAllowCustomValue(boolean)} method.
     * <p>
     * The custom value becomes disallowed automatically once the last custom
     * value set listener is removed.
     *
     * @param listener
     *            the listener to be notified when a new value is filled
     * @return a {@link Registration} for removing the event listener
     * @see #setAllowCustomValue(boolean)
     */
    public Registration addCustomValueSetListener(
            ComponentEventListener<CustomValueSetEvent<TComponent>> listener) {
        setAllowCustomValue(true);
        customValueListenersCount++;
        Registration registration = addInternalCustomValueSetListener(listener);
        return new CustomValueRegistration(registration);
    }

    /**
     * Adds a custom value event listener to the component. Can be used
     * internally to register a listener without also enabling allowing custom
     * values, which is a side-effect of
     * {@link #addCustomValueSetListener(ComponentEventListener)}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Registration addInternalCustomValueSetListener(
            ComponentEventListener<CustomValueSetEvent<TComponent>> listener) {
        return addListener(CustomValueSetEvent.class,
                (ComponentEventListener) listener);
    }

    // ****************************************************
    // List data view implementation
    // ****************************************************

    /**
     * Gets the list data view for the ComboBox. This data view should only be
     * used when the items are in-memory set with:
     * <ul>
     * <li>{@link #setItems(Collection)}</li>
     * <li>{@link #setItems(Object[])}</li>
     * <li>{@link #setItems(ListDataProvider)}</li>
     * <li>{@link #setItems(ComboBox.ItemFilter, ListDataProvider)}</li>
     * <li>{@link #setItems(ComboBox.ItemFilter, Object[])}</li>
     * <li>{@link #setItems(ComboBox.ItemFilter, Collection)}</li>
     * </ul>
     * If the items are not in-memory an exception is thrown. When the items are
     * fetched lazily, use {@link #getLazyDataView()} instead.
     *
     * @return the list data view that provides access to the items in the
     *         ComboBox
     */
    @Override
    public ComboBoxListDataView<TItem> getListDataView() {
        return dataController.getListDataView();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Filtering will use a case insensitive match to show all items where the
     * filter text is a substring of the label displayed for that item, which
     * you can configure with
     * {@link #setItemLabelGenerator(ItemLabelGenerator)}.
     * <p>
     * Filtering will be handled in the client-side if the size of the data set
     * is less than the page size. To force client-side filtering with a larger
     * data set (at the cost of increased network traffic), you can increase the
     * page size with {@link #setPageSize(int)}.
     * <p>
     * Setting the items resets the combo box's value to {@code null}.
     */
    @Override
    public ComboBoxListDataView<TItem> setItems(Collection<TItem> items) {
        return dataController.setItems(items);
    }

    /**
     * Sets the data items of this combo box and a filtering function for
     * defining which items are displayed when user types into the combo box.
     * <p>
     * Note that defining a custom filter will force the component to make
     * server roundtrips to handle the filtering. Otherwise it can handle
     * filtering in the client-side, if the size of the data set is less than
     * the {@link #setPageSize(int) pageSize}.
     * <p>
     * Setting the items resets the combo box's value to {@code null}.
     * <p>
     * The returned data view object can be used for further access to combo box
     * items, or later on fetched with {@link #getListDataView()}. For using
     * lazy data loading, use one of the {@code setItems} methods which take a
     * fetch callback parameter instead.
     *
     * @param itemFilter
     *            filter to check if an item is shown when user typed some text
     *            into the ComboBox
     * @param items
     *            the data items to display
     * @return the in-memory data view instance that provides access to the data
     *         bound to the combo box
     */
    public ComboBoxListDataView<TItem> setItems(
            ComboBox.ItemFilter<TItem> itemFilter, Collection<TItem> items) {
        return dataController.setItems(itemFilter, items);
    }

    /**
     * Sets the data items of this combo box and a filtering function for
     * defining which items are displayed when user types into the combo box.
     * <p>
     * Note that defining a custom filter will force the component to make
     * server roundtrips to handle the filtering. Otherwise it can handle
     * filtering in the client-side, if the size of the data set is less than
     * the {@link #setPageSize(int) pageSize}.
     * <p>
     * Setting the items resets the combo box's value to {@code null}.
     * <p>
     * The returned data view object can be used for further access to combo box
     * items, or later on fetched with {@link #getListDataView()}. For using
     * lazy data loading, use one of the {@code setItems} methods which take a
     * fetch callback parameter instead.
     *
     * @param itemFilter
     *            filter to check if an item is shown when user typed some text
     *            into the ComboBox
     * @param items
     *            the data items to display
     * @return the in-memory data view instance that provides access to the data
     *         bound to the combo box
     */
    @SuppressWarnings("unchecked")
    public ComboBoxListDataView<TItem> setItems(
            ComboBox.ItemFilter<TItem> itemFilter, TItem... items) {
        return dataController.setItems(itemFilter, items);
    }

    /**
     * Sets a ListDataProvider for this combo box and a filtering function for
     * defining which items are displayed when user types into the combo box.
     * <p>
     * Note that defining a custom filter will force the component to make
     * server roundtrips to handle the filtering. Otherwise it can handle
     * filtering in the client-side, if the size of the data set is less than
     * the {@link #setPageSize(int) pageSize}.
     * <p>
     * Setting the items resets the combo box's value to {@code null}.
     * <p>
     * The returned data view object can be used for further access to combo box
     * items, or later on fetched with {@link #getListDataView()}. For using
     * lazy data loading, use one of the {@code setItems} methods which take a
     * fetch callback parameter instead.
     *
     * @param itemFilter
     *            filter to check if an item is shown when user typed some text
     *            into the ComboBox.
     * @param listDataProvider
     *            ListDataProvider providing items to the component.
     * @return the in-memory data view instance that provides access to the data
     *         bound to the combo box
     */
    public ComboBoxListDataView<TItem> setItems(
            ComboBox.ItemFilter<TItem> itemFilter,
            ListDataProvider<TItem> listDataProvider) {
        return dataController.setItems(itemFilter, listDataProvider);
    }

    @Override
    public ComboBoxListDataView<TItem> setItems(
            ListDataProvider<TItem> dataProvider) {
        return dataController.setItems(dataProvider);
    }

    // ****************************************************
    // Lazy data view implementation
    // ****************************************************

    /**
     * Gets the lazy data view for the ComboBox. This data view should only be
     * used when the items are provided lazily from the backend with:
     * <ul>
     * <li>{@link #setItems(CallbackDataProvider.FetchCallback)}</li>
     * <li>{@link #setItemsWithFilterConverter(CallbackDataProvider.FetchCallback, SerializableFunction)}</li>
     * <li>{@link #setItems(CallbackDataProvider.FetchCallback, CallbackDataProvider.CountCallback)}</li>
     * <li>{@link #setItemsWithFilterConverter(CallbackDataProvider.FetchCallback, CallbackDataProvider.CountCallback, SerializableFunction)}
     * </li>
     * <li>{@link #setItems(BackEndDataProvider)}</li>
     * </ul>
     * If the items are not fetched lazily an exception is thrown. When the
     * items are in-memory, use {@link #getListDataView()} instead.
     *
     * @return the lazy data view that provides access to the data bound to the
     *         ComboBox
     * @throws IllegalStateException
     *             if no items fetch callback(s) set
     */
    @Override
    public ComboBoxLazyDataView<TItem> getLazyDataView() {
        return dataController.getLazyDataView();
    }

    /**
     * Supply items lazily with a callback from a backend, using custom filter
     * type. The combo box will automatically fetch more items and adjust its
     * size until the backend runs out of items. Usage example:
     * <p>
     * {@code comboBox.setItemsWithFilterConverter(
     * query -> orderService.getOrdersByCount(query.getFilter(),
     * query.getOffset(),
     * query.getLimit()),
     * orderCountStr -> Integer.parseInt(orderCountStr));} Note: Validations for
     * <code>orderCountStr</code> are omitted for briefness.
     * <p>
     * Combo box's client-side filter typed by the user is transformed into a
     * callback's filter through the given filter converter.
     * <p>
     * The returned data view object can be used for further configuration, or
     * later on fetched with {@link #getLazyDataView()}. For using in-memory
     * data, like {@link java.util.Collection}, use
     * {@link #setItems(Collection)} instead.
     *
     * @param fetchCallback
     *            function that returns a stream of items from the backend based
     *            on the offset, limit and a object filter
     * @param filterConverter
     *            a function which converts a combo box's filter-string typed by
     *            the user into a callback's object filter
     * @param <C>
     *            filter type used by a callback
     * @return ComboBoxLazyDataView instance for further configuration
     */
    public <C> ComboBoxLazyDataView<TItem> setItemsWithFilterConverter(
            CallbackDataProvider.FetchCallback<TItem, C> fetchCallback,
            SerializableFunction<String, C> filterConverter) {
        return dataController.setItemsWithFilterConverter(fetchCallback,
                filterConverter);
    }

    /**
     * Supply items lazily with callbacks: the first one fetches the items based
     * on offset, limit and an optional filter, the second provides the exact
     * count of items in the backend. Use this only in case getting the count is
     * cheap and the user benefits from the component showing immediately the
     * exact size. Usage example:
     * <p>
     * {@code comboBox.setItemsWithFilterConverter(
     * query -> orderService.getOrdersByCount(query.getFilter(),
     * query.getOffset(),
     * query.getLimit()),
     * query -> orderService.getSize(query.getFilter()),
     * orderCountStr -> Integer.parseInt(orderCountStr));} Note: Validations for
     * <code>orderCountStr</code> are omitted for briefness.
     * <p>
     * Combo box's client-side filter typed by the user is transformed into a
     * custom filter type through the given filter converter.
     * <p>
     * The returned data view object can be used for further configuration, or
     * later on fetched with {@link #getLazyDataView()}. For using in-memory
     * data, like {@link java.util.Collection}, use
     * {@link #setItems(Collection)} instead.
     *
     * @param fetchCallback
     *            function that returns a stream of items from the backend based
     *            on the offset, limit and a object filter
     * @param countCallback
     *            function that return the number of items in the back end for a
     *            query
     * @param filterConverter
     *            a function which converts a combo box's filter-string typed by
     *            the user into a callback's object filter
     * @param <C>
     *            filter type used by a callbacks
     * @return ComboBoxLazyDataView instance for further configuration
     */
    public <C> ComboBoxLazyDataView<TItem> setItemsWithFilterConverter(
            CallbackDataProvider.FetchCallback<TItem, C> fetchCallback,
            CallbackDataProvider.CountCallback<TItem, C> countCallback,
            SerializableFunction<String, C> filterConverter) {
        return dataController.setItemsWithFilterConverter(fetchCallback,
                countCallback, filterConverter);
    }

    /**
     * Supply items lazily with a callback from a backend. The ComboBox will
     * automatically fetch more items and adjust its size until the backend runs
     * out of items. Usage example without component provided filter:
     * <p>
     * {@code comboBox.setItems(query ->
     * orderService.getOrders(query.getOffset(), query.getLimit());}
     * <p>
     * Since ComboBox supports filtering, it can be fetched via
     * query.getFilter():
     * <p>
     * {@code comboBox.setItems(query ->
     * orderService.getOrders(query.getFilter(), query.getOffset(),
     * query.getLimit());}
     * <p>
     * The returned data view object can be used for further configuration, or
     * later on fetched with {@link #getLazyDataView()}. For using in-memory
     * data, like {@link Collection}, use
     * {@link HasListDataView#setItems(Collection)} instead.
     * <p>
     * If item filtering by some value type other than String is preferred and
     * backend service is able to fetch and filter items by such type, converter
     * for client side's filter string can be set along with fetch callback.
     * See:
     * {@link #setItemsWithFilterConverter(CallbackDataProvider.FetchCallback, SerializableFunction)}
     *
     * @param fetchCallback
     *            function that returns a stream of items from the backend based
     *            on the offset, limit and an optional filter provided by the
     *            query object
     * @return ComboBoxLazyDataView instance for further configuration
     */
    @Override
    public ComboBoxLazyDataView<TItem> setItems(
            CallbackDataProvider.FetchCallback<TItem, String> fetchCallback) {
        return HasLazyDataView.super.setItems(fetchCallback);
    }

    /**
     * Supply items lazily with callbacks: the first one fetches the items based
     * on offset, limit and an optional filter, the second provides the exact
     * count of items in the backend. Use this only in case getting the count is
     * cheap and the user benefits from the ComboBox showing immediately the
     * exact size. Usage example without component provided filter:
     * <p>
     * {@code comboBox.setItems(
     * query -> orderService.getOrders(query.getOffset(), query.getLimit()),
     * query -> orderService.getSize());}
     * <p>
     * Since ComboBox supports filtering, it can be fetched via
     * query.getFilter():
     * <p>
     * {@code comboBox.setItems(
     * query -> orderService.getOrders(query.getFilter(), query.getOffset(),
     * query.getLimit()),
     * query -> orderService.getSize(query.getFilter()));}
     * <p>
     * The returned data view object can be used for further configuration, or
     * later on fetched with {@link #getLazyDataView()}. For using in-memory
     * data, like {@link Collection}, use
     * {@link HasListDataView#setItems(Collection)} instead.
     * <p>
     * If item filtering by some value type other than String is preferred and
     * backend service is able to fetch and filter items by such type, converter
     * for client side's filter string can be set along with fetch callback.
     * See:
     * {@link #setItemsWithFilterConverter(CallbackDataProvider.FetchCallback, CallbackDataProvider.CountCallback, SerializableFunction)}
     *
     * @param fetchCallback
     *            function that returns a stream of items from the back end for
     *            a query
     * @param countCallback
     *            function that return the number of items in the back end for a
     *            query
     * @return ComboBoxLazyDataView instance for further configuration
     */
    @Override
    public ComboBoxLazyDataView<TItem> setItems(
            CallbackDataProvider.FetchCallback<TItem, String> fetchCallback,
            CallbackDataProvider.CountCallback<TItem, String> countCallback) {
        return HasLazyDataView.super.setItems(fetchCallback, countCallback);
    }

    public interface SpringData extends Serializable {
        /**
         * Callback interface for fetching a list of items from a backend based
         * on a Spring Data Pageable and a filter string.
         *
         * @param <T>
         *            the type of the items to fetch
         */
        @FunctionalInterface
        public interface FetchCallback<PAGEABLE, T> extends Serializable {

            /**
             * Fetches a list of items based on a pageable and a filter string.
             * The pageable defines the paging of the items to fetch and the
             * sorting.
             *
             * @param pageable
             *            the pageable that defines which items to fetch and the
             *            sort order
             * @param filterString
             *            the filter string provided by the ComboBox
             * @return a list of items
             */
            List<T> fetch(PAGEABLE pageable, String filterString);
        }

        /**
         * Callback interface for counting the number of items in a backend
         * based on a Spring Data Pageable and a filter string.
         */
        @FunctionalInterface
        public interface CountCallback<PAGEABLE> extends Serializable {
            /**
             * Counts the number of available items based on a pageable and a
             * filter string. The pageable defines the paging of the items to
             * fetch and the sorting and is provided although it is generally
             * not needed for determining the number of items.
             *
             * @param pageable
             *            the pageable that defines which items to fetch and the
             *            sort order
             * @param filterString
             *            the filter string provided by the ComboBox
             * @return the number of available items
             */
            long count(PAGEABLE pageable, String filterString);
        }
    }

    /**
     * Supply items lazily with a callback from a backend based on a Spring Data
     * Pageable. The component will automatically fetch more items and adjust
     * its size until the backend runs out of items. Usage example:
     * <p>
     * {@code comboBox.setItemsPageable((pageable, filterString) -> orderService.getOrders(pageable, filterString));}
     * <p>
     * The returned data view object can be used for further configuration, or
     * later on fetched with {@link #getLazyDataView()}. For using in-memory
     * data, like {@link java.util.Collection}, use
     * {@link HasListDataView#setItems(Collection)} instead.
     *
     * @param fetchCallback
     *            a function that returns a sorted list of items from the
     *            backend based on the given pageable
     * @return a data view for further configuration
     */
    public ComboBoxLazyDataView<TItem> setItemsPageable(
            SpringData.FetchCallback<Pageable, TItem> fetchCallback) {
        return setItems(
                query -> handleSpringFetchCallback(query, fetchCallback));
    }

    /**
     * Supply items lazily with callbacks: the first one fetches a list of items
     * from a backend based on a Spring Data Pageable, the second provides the
     * exact count of items in the backend. Use this in case getting the count
     * is cheap and the user benefits from the component showing immediately the
     * exact size. Usage example:
     * <p>
     * {@code component.setItemsPageable(
     *                    (pageable, filterString) -> orderService.getOrders(pageable, filterString),
     *                    (pageable, filterString) -> orderService.countOrders(filterString));}
     * <p>
     * The returned data view object can be used for further configuration, or
     * later on fetched with {@link #getLazyDataView()}. For using in-memory
     * data, like {@link java.util.Collection}, use
     * {@link HasListDataView#setItems(Collection)} instead.
     *
     * @param fetchCallback
     *            a function that returns a sorted list of items from the
     *            backend based on the given pageable and filter string
     * @param countCallback
     *            a function that returns the number of items in the back end
     *            based on the filter string
     * @return LazyDataView instance for further configuration
     */
    public ComboBoxLazyDataView<TItem> setItemsPageable(
            SpringData.FetchCallback<Pageable, TItem> fetchCallback,
            SpringData.CountCallback<Pageable> countCallback) {
        return setItems(
                query -> handleSpringFetchCallback(query, fetchCallback),
                query -> handleSpringCountCallback(query, countCallback));
    }

    @SuppressWarnings("unchecked")
    private static <PAGEABLE, T> Stream<T> handleSpringFetchCallback(
            Query<T, String> query,
            SpringData.FetchCallback<PAGEABLE, T> fetchCallback) {
        PAGEABLE pageable = (PAGEABLE) VaadinSpringDataHelpers
                .toSpringPageRequest(query);
        List<T> itemList = fetchCallback.fetch(pageable,
                query.getFilter().orElse(""));
        return itemList.stream();
    }

    @SuppressWarnings("unchecked")
    private static <PAGEABLE> int handleSpringCountCallback(
            Query<?, String> query,
            SpringData.CountCallback<PAGEABLE> countCallback) {
        PAGEABLE pageable = (PAGEABLE) VaadinSpringDataHelpers
                .toSpringPageRequest(query);
        long count = countCallback.count(pageable,
                query.getFilter().orElse(""));
        if (count > Integer.MAX_VALUE) {
            LoggerFactory.getLogger(ComboBoxBase.class).warn(
                    "The count of items in the backend ({}) exceeds the maximum supported by the ComboBox.",
                    count);
            return Integer.MAX_VALUE;
        }
        return (int) count;
    }

    @Override
    public ComboBoxLazyDataView<TItem> setItems(
            BackEndDataProvider<TItem, String> dataProvider) {
        return dataController.setItems(dataProvider);
    }

    // ****************************************************
    // Generic data view implementation
    // ****************************************************

    /**
     * Gets the generic data view for the ComboBox. This data view can be used
     * when {@link #getListDataView()} or {@link #getLazyDataView()} are not
     * applicable for the underlying data provider, or you don't want to
     * distinct between which type of data view to use.
     *
     * @return the generic {@link DataView} implementation for ComboBox
     * @see #getListDataView()
     * @see #getLazyDataView()
     */
    @Override
    public ComboBoxDataView<TItem> getGenericDataView() {
        return dataController.getGenericDataView();
    }

    @Override
    public ComboBoxDataView<TItem> setItems(
            DataProvider<TItem, String> dataProvider) {
        return dataController.setItems(dataProvider);
    }

    /**
     * The method is not supported for the {@link ComboBox} component, use
     * another overloaded method with filter converter
     * {@link #setItems(InMemoryDataProvider, SerializableFunction)}
     * <p>
     * Always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException
     *             when using this method with an {@link InMemoryDataProvider}
     * @see #setItems(InMemoryDataProvider, SerializableFunction)
     * @deprecated does not work so don't use
     */
    @Deprecated
    @Override
    public ComboBoxDataView<TItem> setItems(
            InMemoryDataProvider<TItem> dataProvider) {
        throw new UnsupportedOperationException(
                String.format("ComboBox does not support "
                        + "setting a custom in-memory data provider without "
                        + "knowledge of the rules on how to convert internal text filter "
                        + "into a predicate applied to the data provider. Please use%n"
                        + "setItems(InMemoryDataProvider<T>, SerializableFunction<String, "
                        + "SerializablePredicate<T>>)"
                        + "%noverloaded method instead"));
    }

    /**
     * Sets an in-memory data provider for the combo box to use, taking into
     * account both in-memory filtering from data provider and combo box's text
     * filter.
     * <p>
     * Text filter is transformed into a predicate filter through the given
     * filter converter. Example of filter converter which produces the Person's
     * name predicate:
     * {@code (String nameFilter) -> person -> person.getName().equalsIgnoreCase
     * (nameFilter);}
     * <p>
     * Filtering will be handled in the client-side if the size of the data set
     * is less than the page size. To force client-side filtering with a larger
     * data set (at the cost of increased network traffic), you can increase the
     * page size with {@link #setPageSize(int)}.
     * <p>
     * Note! Using a {@link ListDataProvider} instead of a
     * {@link InMemoryDataProvider} is recommended to get access to
     * {@link ListDataView} API by using {@link #setItems(ListDataProvider)}.
     *
     * @param inMemoryDataProvider
     *            InMemoryDataProvider to use, not <code>null</code>
     * @param filterConverter
     *            a function which converts a component's internal filter into a
     *            predicate applied to the data provider
     * @return DataView providing information on the data
     */
    public ComboBoxDataView<TItem> setItems(
            InMemoryDataProvider<TItem> inMemoryDataProvider,
            SerializableFunction<String, SerializablePredicate<TItem>> filterConverter) {
        return dataController.setItems(inMemoryDataProvider, filterConverter);
    }

    // ****************************************************
    // Data provider implementation
    // ****************************************************

    /**
     * Sets a generic data provider for the ComboBox to use.
     * <p>
     * ComboBox triggers filtering queries based on the strings users type into
     * the field. For this reason you need to provide the second parameter, a
     * function which converts the filter-string typed by the user into
     * filter-type used by your data provider. If your data provider already
     * supports String as the filter-type, it can be used without a converter
     * function via {@link #setItems(DataProvider)}.
     * <p>
     * Using this method provides the same result as using a data provider
     * wrapped with
     * {@link DataProvider#withConvertedFilter(SerializableFunction)}.
     * <p>
     * Changing the combo box's data provider resets its current value to
     * {@code null}.
     * <p>
     * Use this method when none of the {@code setItems} methods are applicable,
     * e.g. when having a data provider with filter that cannot be transformed
     * to {@code DataProvider<T, Void>}.
     */
    public <C> void setDataProvider(DataProvider<TItem, C> dataProvider,
            SerializableFunction<String, C> filterConverter) {
        dataController.setDataProvider(dataProvider, filterConverter);
    }

    /**
     * Sets a CallbackDataProvider using the given fetch items callback and a
     * size callback.
     * <p>
     * This method is a shorthand for making a {@link CallbackDataProvider} that
     * handles a partial {@link com.vaadin.flow.data.provider.Query Query}
     * object.
     * <p>
     * Changing the combo box's data provider resets its current value to
     * {@code null}.
     *
     * @param fetchItems
     *            a callback for fetching items, not <code>null</code>
     * @param sizeCallback
     *            a callback for getting the count of items, not
     *            <code>null</code>
     * @see CallbackDataProvider
     */
    public void setDataProvider(ComboBox.FetchItemsCallback<TItem> fetchItems,
            SerializableFunction<String, Integer> sizeCallback) {
        dataController.setDataProvider(fetchItems, sizeCallback);
    }

    /**
     * Gets the data provider used by this ComboBox.
     * <p>
     * To get information and control over the items in the ComboBox, use either
     * {@link #getListDataView()} or {@link #getLazyDataView()} instead.
     *
     * @return the data provider used by this ComboBox
     */
    public DataProvider<TItem, ?> getDataProvider() {
        return dataController.getDataProvider();
    }

    /**
     * Whether the item is currently selected in the combo box.
     *
     * @param item
     *            the item to check
     * @return {@code true} if the item is selected, {@code false} otherwise
     */
    protected abstract boolean isSelected(TItem item);

    /**
     * Refresh value / selection of the web component after changes that might
     * affect the presentation / rendering of items
     */
    protected abstract void refreshValue();

    /**
     * Accesses the render manager that is managing the custom renderer
     *
     * @return the render manager
     */
    protected ComboBoxRenderManager<TItem> getRenderManager() {
        return renderManager;
    }

    /**
     * Accesses the data controller that is managing data communication with the
     * web component
     * <p>
     * Can be null if the constructor has not run yet
     *
     * @return the data controller
     */
    protected ComboBoxDataController<TItem> getDataController() {
        return dataController;
    }

    /**
     * Accesses the data communicator that is managed by the data controller
     * <p>
     * Can be null if the no data source has been set yet, or if the constructor
     * has not run yet
     *
     * @return the data communicator
     */
    protected ComboBoxDataCommunicator<TItem> getDataCommunicator() {
        return dataController != null ? dataController.getDataCommunicator()
                : null;
    }

    /**
     * Accesses the data generator that is managed by the data controller
     * <p>
     * Can be null if the constructor has not run yet
     *
     * @return the data generator
     */
    protected CompositeDataGenerator<TItem> getDataGenerator() {
        return dataController != null ? dataController.getDataGenerator()
                : null;
    }

    /**
     * Accesses the key mapper that is managed by the data controller
     * <p>
     * Can be null if the no data source has been set yet, or if the constructor
     * has not run yet
     *
     * @return the key mapper
     */
    protected DataKeyMapper<TItem> getKeyMapper() {
        return getDataCommunicator() != null
                ? getDataCommunicator().getKeyMapper()
                : null;
    }

    /**
     * Called by the client-side connector, delegates to data controller
     *
     * @param id
     *            the update identifier
     */
    @ClientCallable
    private void confirmUpdate(int id) {
        dataController.confirmUpdate(id);
    }

    /**
     * Called by the client-side connector, delegates to data controller
     */
    @ClientCallable
    private void setViewportRange(int start, int length, String filter) {
        dataController.setViewportRange(start, length, filter);
    }

    /**
     * Called by the client-side connector, delegates to data controller
     */
    @ClientCallable
    private void resetDataCommunicator() {
        dataController.resetDataCommunicator();
    }

    /**
     * Helper for running a command in the before client response hook
     *
     * @param command
     *            the command to execute
     */
    protected void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    private void initConnector() {
        getElement().executeJs(
                "window.Vaadin.Flow.comboBoxConnector.initLazy(this)");
    }

    @Override
    public void setManualValidation(boolean enabled) {
        validationController.setManualValidation(enabled);
    }

    @Override
    public Validator<TValue> getDefaultValidator() {
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
     * Event that is dispatched from a combo box component, if the component
     * allows setting custom values, and the user has entered a non-empty value
     * that does not match any of the existing items
     *
     * @param <TComponent>
     *            The specific combo box component type
     */
    @DomEvent("custom-value-set")
    public static class CustomValueSetEvent<TComponent extends ComboBoxBase<TComponent, ?, ?>>
            extends ComponentEvent<TComponent> {
        private final String detail;

        public CustomValueSetEvent(TComponent source, boolean fromClient,
                @EventData("event.detail") String detail) {
            super(source, fromClient);
            this.detail = detail;
        }

        public String getDetail() {
            return detail;
        }
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link #setI18n(ComboBoxBaseI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    protected ComboBoxBaseI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization object for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    protected void setI18n(ComboBoxBaseI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
    }

    private String getI18nErrorMessage(
            Function<ComboBoxBaseI18n, String> getter) {
        return Optional.ofNullable(i18n).map(getter).orElse("");
    }
}
