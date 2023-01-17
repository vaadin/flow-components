/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.shared.ClientValidationUtil;
import com.vaadin.flow.component.shared.HasAllowedCharPattern;
import com.vaadin.flow.component.shared.HasOverlayClassName;
import com.vaadin.flow.component.shared.HasClearButton;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.dataview.ComboBoxDataView;
import com.vaadin.flow.component.combobox.dataview.ComboBoxLazyDataView;
import com.vaadin.flow.component.combobox.dataview.ComboBoxListDataView;
import com.vaadin.flow.component.shared.HasAutoOpen;
import com.vaadin.flow.component.shared.HasClientValidation;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.HasValidationProperties;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.ValidationStatusChangeEvent;
import com.vaadin.flow.data.binder.ValidationStatusChangeListener;
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
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

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
        implements Focusable<TComponent>, HasAllowedCharPattern, HasAutoOpen,
        HasClearButton, HasClientValidation, HasOverlayClassName,
        HasDataView<TItem, String, ComboBoxDataView<TItem>>, HasHelper,
        HasLabel, HasLazyDataView<TItem, String, ComboBoxLazyDataView<TItem>>,
        HasListDataView<TItem, ComboBoxListDataView<TItem>>, HasSize, HasStyle,
        HasTheme, HasTooltip, HasValidationProperties, HasValidator<TValue> {

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
    private final ComboBoxRenderManager<TItem> renderManager;
    private final ComboBoxDataController<TItem> dataController;
    private int customValueListenersCount;

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

        renderManager = new ComboBoxRenderManager<>(this);
        dataController = new ComboBoxDataController<>(this, this::getLocale);
        dataController.getDataGenerator().addDataGenerator((item,
                jsonObject) -> jsonObject.put("label", generateLabel(item)));

        // Configure web component to use key property from the generated
        // wrapper items for identification
        getElement().setProperty("itemValuePath", "key");
        getElement().setProperty("itemIdPath", "key");

        // Disable template warnings
        getElement().setAttribute("suppress-template-warning", true);

        // Notify data communicator when selection changes, which allows to
        // free up items / keys in the KeyMapper that are not used anymore in
        // the selection
        addValueChangeListener(
                e -> getDataCommunicator().notifySelectionChanged());

        addValueChangeListener(e -> validate());

        addClientValidatedEventListener(e -> validate());
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
     * Sets whether the component requires a value to be considered in a valid
     * state.
     *
     * @return {@code true} if the component requires a value to be valid
     */
    public boolean isRequired() {
        return super.isRequiredIndicatorVisible();
    }

    /**
     * Whether the component requires a value to be considered in a valid state.
     *
     * @param required
     *            {@code true} if the component requires a value to be valid
     */
    public void setRequired(boolean required) {
        super.setRequiredIndicatorVisible(required);
    }

    /**
     * The placeholder text that should be displayed in the input element, when
     * the user has not entered a value
     *
     * @return the placeholder text
     */
    public String getPlaceholder() {
        return getElement().getProperty("placeholder");
    }

    /**
     * Sets the placeholder text that should be displayed in the input element,
     * when the user has not entered a value
     *
     * @param placeholder
     *            the placeholder text
     */
    public void setPlaceholder(String placeholder) {
        getElement().setProperty("placeholder",
                placeholder == null ? "" : placeholder);
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
            if (value == getEmptyValue()) {
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

        ClientValidationUtil.preventWebComponentFromModifyingInvalidState(this);
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

    /**
     * {@inheritDoc}
     *
     * @deprecated Because the stream is collected to a list anyway, use
     *             {@link #setItems(Collection)} or
     *             {@link #setItems(CallbackDataProvider.FetchCallback)}
     *             instead.
     */
    @Deprecated
    public void setItems(Stream<TItem> streamOfItems) {
        setItems(DataProvider.fromStream(streamOfItems));
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
     * query.getOffset,
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
     * query.getOffset,
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
     * query -> orderService.getOrders(query.getOffset, query.getLimit()),
     * query -> orderService.getSize());}
     * <p>
     * Since ComboBox supports filtering, it can be fetched via
     * query.getFilter():
     * <p>
     * {@code comboBox.setItems(
     * query -> orderService.getOrders(query.getFilter(), query.getOffset,
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
     * Gets the data provider used by this ComboBox.
     *
     * @return the data provider used by this ComboBox
     */
    public DataProvider<TItem, ?> getDataProvider() {
        return dataController.getDataProvider();
    }

    /**
     * {@inheritDoc}
     * <p>
     * The filter-type of the given data provider must be String so that it can
     * handle the filters typed into the ComboBox by users. If your data
     * provider uses some other type of filter, you can provide a function which
     * converts the ComboBox's filter-string into that type via
     * {@link #setDataProvider(DataProvider, SerializableFunction)}. Another way
     * to do the same thing is to use this method with your data provider
     * converted with
     * {@link DataProvider#withConvertedFilter(SerializableFunction)}.
     * <p>
     * Changing the combo box's data provider resets its current value to
     * {@code null}.
     *
     * @deprecated use instead one of the {@code setItems} methods which provide
     *             access to either {@link ComboBoxListDataView} or
     *             {@link ComboBoxLazyDataView}
     */
    @Deprecated
    public void setDataProvider(DataProvider<TItem, String> dataProvider) {
        dataController.setDataProvider(dataProvider);
    }

    /**
     * {@inheritDoc}
     * <p>
     * ComboBox triggers filtering queries based on the strings users type into
     * the field. For this reason you need to provide the second parameter, a
     * function which converts the filter-string typed by the user into
     * filter-type used by your data provider. If your data provider already
     * supports String as the filter-type, it can be used without a converter
     * function via {@link #setDataProvider(DataProvider)}.
     * <p>
     * Using this method provides the same result as using a data provider
     * wrapped with
     * {@link DataProvider#withConvertedFilter(SerializableFunction)}.
     * <p>
     * Changing the combo box's data provider resets its current value to
     * {@code null}.
     *
     * @deprecated use instead one of the {@code setItems} methods which provide
     *             access to either {@link ComboBoxListDataView} or
     *             {@link ComboBoxLazyDataView}
     */
    @Deprecated
    public <C> void setDataProvider(DataProvider<TItem, C> dataProvider,
            SerializableFunction<String, C> filterConverter) {
        dataController.setDataProvider(dataProvider, filterConverter);
    }

    /**
     * Sets a list data provider as the data provider of this combo box.
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
     * Changing the combo box's data provider resets its current value to
     * {@code null}.
     *
     * @param listDataProvider
     *            the list data provider to use, not <code>null</code>
     * @deprecated use instead one of the {@code setItems} methods which provide
     *             access to {@link ComboBoxListDataView}
     */
    @Deprecated
    public void setDataProvider(ListDataProvider<TItem> listDataProvider) {
        dataController.setDataProvider(listDataProvider);
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
     * @see #setDataProvider(DataProvider)
     * @deprecated use instead
     *             {@link #setItems(CallbackDataProvider.FetchCallback, CallbackDataProvider.CountCallback)}
     *             which provide access to {@link ComboBoxLazyDataView}
     */
    @Deprecated
    public void setDataProvider(ComboBox.FetchItemsCallback<TItem> fetchItems,
            SerializableFunction<String, Integer> sizeCallback) {
        dataController.setDataProvider(fetchItems, sizeCallback);
    }

    /**
     * Sets a list data provider with an item filter as the data provider of
     * this combo box. The item filter is used to compare each item to the
     * filter text entered by the user.
     * <p>
     * Note that defining a custom filter will force the component to make
     * server roundtrips to handle the filtering. Otherwise it can handle
     * filtering in the client-side, if the size of the data set is less than
     * the {@link #setPageSize(int) pageSize}.
     * <p>
     * Changing the combo box's data provider resets its current value to
     * {@code null}.
     *
     * @param itemFilter
     *            filter to check if an item is shown when user typed some text
     *            into the ComboBox
     * @param listDataProvider
     *            the list data provider to use, not <code>null</code>
     * @deprecated use instead
     *             {@link #setItems(ComboBox.ItemFilter, ListDataProvider)}
     *             which provide access to {@link ComboBoxListDataView}
     */
    @Deprecated
    public void setDataProvider(ComboBox.ItemFilter<TItem> itemFilter,
            ListDataProvider<TItem> listDataProvider) {
        dataController.setDataProvider(itemFilter, listDataProvider);
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
    private void setRequestedRange(int start, int length, String filter) {
        dataController.setRequestedRange(start, length, filter);
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

    protected void validate() {
        boolean isRequired = isRequiredIndicatorVisible();
        boolean isInvalid = ValidationUtil
                .checkRequired(isRequired, getValue(), getEmptyValue())
                .isError();

        setInvalid(isInvalid);
    }

    @Override
    public Registration addValidationStatusChangeListener(
            ValidationStatusChangeListener<TValue> listener) {
        return addClientValidatedEventListener(
                event -> listener.validationStatusChanged(
                        new ValidationStatusChangeEvent<>(this, !isInvalid())));
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
}
