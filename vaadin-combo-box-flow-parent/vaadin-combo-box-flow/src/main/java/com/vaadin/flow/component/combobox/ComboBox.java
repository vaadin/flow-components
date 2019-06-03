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
package com.vaadin.flow.component.combobox;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.data.binder.HasFilterableDataProvider;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.ArrayUpdater.Update;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataChangeEvent.DataRefreshEvent;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableBiPredicate;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.shared.Registration;

import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

/**
 * Server-side component for the {@code vaadin-combo-box} webcomponent. It
 * contains the same features of the webcomponent, such as item filtering,
 * object selection and item templating.
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
@HtmlImport("frontend://flow-component-renderer.html")
@JsModule("@vaadin/flow-frontend/flow-component-renderer.js")
@JavaScript("frontend://comboBoxConnector.js")
@JsModule("./comboBoxConnector-es6.js")
public class ComboBox<T> extends GeneratedVaadinComboBox<ComboBox<T>, T>
        implements HasSize, HasValidation,
        HasFilterableDataProvider<T, String> {

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
        public Stream<T> fetchItems(String filter, int offset, int limit);
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

    private final class UpdateQueue implements Update {
        private transient List<Runnable> queue = new ArrayList<>();

        private UpdateQueue(int size) {
            enqueue("$connector.updateSize", size);
        }

        @Override
        public void set(int start, List<JsonValue> items) {
            enqueue("$connector.set", start,
                    items.stream().collect(JsonUtils.asArray()),
                    ComboBox.this.lastFilter);
        }

        @Override
        public void clear(int start, int length) {
            // NO-OP
        }

        @Override
        public void commit(int updateId) {
            enqueue("$connector.confirm", updateId, ComboBox.this.lastFilter);
            queue.forEach(Runnable::run);
            queue.clear();
        }

        private void enqueue(String name, Serializable... arguments) {
            queue.add(() -> getElement().callJsFunction(name, arguments));
        }
    }

    /**
     * Lazy loading updater, used when calling setDataProvider()
     */
    private final ArrayUpdater arrayUpdater = new ArrayUpdater() {
        @Override
        public Update startUpdate(int sizeChange) {
            return new UpdateQueue(sizeChange);
        }

        @Override
        public void initialize() {
            // NO-OP
        }
    };

    /**
     * Predicate to check {@link ComboBox} items against user typed strings.
     */
    @FunctionalInterface
    public interface ItemFilter<T> extends SerializableBiPredicate<T, String> {
        @Override
        public boolean test(T item, String filterText);
    }

    private ItemLabelGenerator<T> itemLabelGenerator = String::valueOf;

    private Renderer<T> renderer;
    private boolean renderScheduled;

    // Filter set by the client when requesting data. It's sent back to client
    // together with the response so client may know for what filter data is
    // provided.
    private String lastFilter;

    private DataCommunicator<T> dataCommunicator;
    private final CompositeDataGenerator<T> dataGenerator = new CompositeDataGenerator<>();
    private Registration dataGeneratorRegistration;

    private Element template;

    private int customValueListenersCount;

    private SerializableConsumer<String> filterSlot = filter -> {
        // Just ignore when setDataProvider has not been called
    };

    private enum UserProvidedFilter {
        UNDECIDED, YES, NO
    }

    private UserProvidedFilter userProvidedFilter = UserProvidedFilter.UNDECIDED;

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
        super(null, null, String.class, ComboBox::presentationToModel,
                ComboBox::modelToPresentation);
        dataGenerator.addDataGenerator((item, jsonObject) -> jsonObject
                .put("label", generateLabel(item)));

        setItemValuePath("key");
        setItemIdPath("key");
        setPageSize(pageSize);

        addAttachListener(e -> initConnector());

        runBeforeClientResponse(ui -> {
            // If user didn't provide any data, initialize with empty data set.
            if (dataCommunicator == null) {
                setItems();
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
     * @see #setItems(Object...)
     */
    @SafeVarargs
    public ComboBox(String label, T... items) {
        this();
        setLabel(label);
        setItems(items);
    }

    private static <T> T presentationToModel(ComboBox<T> comboBox,
            String presentation) {
        if (presentation == null || comboBox.dataCommunicator == null) {
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

    @Override
    public void setValue(T value) {
        if (dataCommunicator == null) {
            if (value == null) {
                return;
            } else {
                throw new IllegalStateException(
                        "Cannot set a value for a ComboBox without items. "
                                + "Use setItems or setDataProvider to populate "
                                + "items into the ComboBox before setting a value.");
            }
        }
        super.setValue(value);
        refreshValue();
    }

    private void refreshValue() {
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
        dataGenerator.generateData(value, json);
        setSelectedItem(json);
        getElement().setProperty(PROP_VALUE, keyMapper.key(value));

        // Workaround for property not updating in certain scenario
        // https://github.com/vaadin/flow/issues/4862
        runBeforeClientResponse(ui -> ui.getPage().executeJs("$0.value=$1",
                getElement(), getElement().getProperty(PROP_VALUE)));
    }

    /**
     * Sets the TemplateRenderer responsible to render the individual items in
     * the list of possible choices of the ComboBox. It doesn't affect how the
     * selected item is rendered - that can be configured by using
     * {@link #setItemLabelGenerator(ItemLabelGenerator)}.
     *
     * @param renderer
     *            a renderer for the items in the selection list of the
     *            ComboBox, not <code>null</code>
     */
    public void setRenderer(Renderer<T> renderer) {
        Objects.requireNonNull(renderer, "The renderer must not be null");
        this.renderer = renderer;

        if (template == null) {
            template = new Element("template");
            getElement().appendChild(template);
        }
        scheduleRender();
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
     */
    @Override
    public void setItems(Collection<T> items) {
        setDataProvider(DataProvider.ofCollection(items));
    }

    /**
     * Sets the data items of this combo box and a filtering function for
     * defining which items are displayed when user types into the combo box.
     * <p>
     * Note that defining a custom filter will force the component to make
     * server roundtrips to handle the filtering. Otherwise it can handle
     * filtering in the client-side, if the size of the data set is less than
     * the {@link #setPageSize(int) pageSize}.
     *
     * @param itemFilter
     *            filter to check if an item is shown when user typed some text
     *            into the ComboBox
     * @param items
     *            the data items to display
     */
    public void setItems(ItemFilter<T> itemFilter, Collection<T> items) {
        ListDataProvider<T> listDataProvider = DataProvider.ofCollection(items);

        setDataProvider(itemFilter, listDataProvider);
    }

    /**
     * Sets the data items of this combo box and a filtering function for
     * defining which items are displayed when user types into the combo box.
     * <p>
     * Note that defining a custom filter will force the component to make
     * server roundtrips to handle the filtering. Otherwise it can handle
     * filtering in the client-side, if the size of the data set is less than
     * the {@link #setPageSize(int) pageSize}.
     *
     * @param itemFilter
     *            filter to check if an item is shown when user typed some text
     *            into the ComboBox
     * @param items
     *            the data items to display
     */
    public void setItems(ItemFilter<T> itemFilter,
            @SuppressWarnings("unchecked") T... items) {
        setItems(itemFilter, Arrays.asList(items));
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
     */
    @Override
    public void setDataProvider(DataProvider<T, String> dataProvider) {
        setDataProvider(dataProvider, SerializableFunction.identity());
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
     */
    @Override
    public <C> void setDataProvider(DataProvider<T, C> dataProvider,
            SerializableFunction<String, C> filterConverter) {
        Objects.requireNonNull(dataProvider,
                "The data provider can not be null");
        Objects.requireNonNull(filterConverter,
                "filterConverter cannot be null");

        if (userProvidedFilter == UserProvidedFilter.UNDECIDED) {
            userProvidedFilter = UserProvidedFilter.YES;
        }

        if (dataCommunicator == null) {
            dataCommunicator = new DataCommunicator<>(dataGenerator,
                    arrayUpdater, data -> getElement()
                            .callJsFunction("$connector.updateData", data),
                    getElement().getNode());
        }

        scheduleRender();
        setValue(null);

        SerializableFunction<String, C> convertOrNull = filterText -> {
            if (filterText == null) {
                return null;
            }

            return filterConverter.apply(filterText);
        };

        SerializableConsumer<C> providerFilterSlot = dataCommunicator
                .setDataProvider(dataProvider,
                        convertOrNull.apply(getFilterString()));

        filterSlot = filter -> {
            if (!Objects.equals(filter, lastFilter)) {
                providerFilterSlot.accept(convertOrNull.apply(filter));
                lastFilter = filter;
            }
        };

        boolean shouldForceServerSideFiltering = userProvidedFilter == UserProvidedFilter.YES;

        dataProvider.addDataProviderListener(e -> {
            if (e instanceof DataRefreshEvent) {
                dataCommunicator.refresh(((DataRefreshEvent<T>) e).getItem());
            } else {
                refreshAllData(shouldForceServerSideFiltering);
            }
        });
        refreshAllData(shouldForceServerSideFiltering);

        userProvidedFilter = UserProvidedFilter.UNDECIDED;
    }

    private void refreshAllData(boolean forceServerSideFiltering) {
        setClientSideFilter(!forceServerSideFiltering && getDataProvider()
                .size(new Query<>()) <= getPageSizeDouble());

        reset();
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
     *
     * @param listDataProvider
     *            the list data provider to use, not <code>null</code>
     */
    public void setDataProvider(ListDataProvider<T> listDataProvider) {
        if (userProvidedFilter == UserProvidedFilter.UNDECIDED) {
            userProvidedFilter = UserProvidedFilter.NO;
        }

        // Cannot use the case insensitive contains shorthand from
        // ListDataProvider since it wouldn't react to locale changes
        ItemFilter<T> defaultItemFilter = (item,
                filterText) -> generateLabel(item).toLowerCase(getLocale())
                        .contains(filterText.toLowerCase(getLocale()));

        setDataProvider(defaultItemFilter, listDataProvider);
    }

    /**
     * Sets a CallbackDataProvider using the given fetch items callback and a
     * size callback.
     * <p>
     * This method is a shorthand for making a {@link CallbackDataProvider} that
     * handles a partial {@link com.vaadin.data.provider.Query Query} object.
     *
     * @param fetchItems
     *            a callback for fetching items
     * @param sizeCallback
     *            a callback for getting the count of items
     * @see CallbackDataProvider
     * @see #setDataProvider(DataProvider)
     */
    public void setDataProvider(FetchItemsCallback<T> fetchItems,
            SerializableFunction<String, Integer> sizeCallback) {
        userProvidedFilter = UserProvidedFilter.YES;
        setDataProvider(new CallbackDataProvider<>(
                q -> fetchItems.fetchItems(q.getFilter().orElse(""),
                        q.getOffset(), q.getLimit()),
                q -> sizeCallback.apply(q.getFilter().orElse(""))));
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
     *
     * @param itemFilter
     *            filter to check if an item is shown when user typed some text
     *            into the ComboBox
     * @param listDataProvider
     *            the list data provider to use, not <code>null</code>
     */
    public void setDataProvider(ItemFilter<T> itemFilter,
            ListDataProvider<T> listDataProvider) {
        Objects.requireNonNull(listDataProvider,
                "List data provider cannot be null");

        setDataProvider(listDataProvider,
                filterText -> item -> itemFilter.test(item, filterText));
    }

    /**
     * Gets the data provider used by this ComboBox.
     *
     * @return the data provider used by this ComboBox
     */
    public DataProvider<T, ?> getDataProvider() { // NOSONAR
        return dataCommunicator.getDataProvider();
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
            ItemLabelGenerator<T> itemLabelGenerator) {
        Objects.requireNonNull(itemLabelGenerator,
                "The item label generator can not be null");
        this.itemLabelGenerator = itemLabelGenerator;
        reset();
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
    public ItemLabelGenerator<T> getItemLabelGenerator() {
        return itemLabelGenerator;
    }

    /**
     * Sets the page size, which is the number of items fetched at a time from
     * the data provider.
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
        super.setPageSize(pageSize);
        reset();
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
     * @see {@link #setPageSize(int)}
     */
    public int getPageSize() {
        return getElement().getProperty("pageSize", 50);
    }

    @Override
    public void setOpened(boolean opened) {
        super.setOpened(opened);
    }

    /**
     * Gets the states of the drop-down.
     *
     * @return {@code true} if the drop-down is opened, {@code false} otherwise
     */
    public boolean isOpened() {
        return isOpenedBoolean();
    }

    @Override
    public void setInvalid(boolean invalid) {
        super.setInvalid(invalid);
    }

    /**
     * Gets the validity of the combobox output.
     * <p>
     * return true, if the value is invalid.
     *
     * @return the {@code validity} property from the component
     */
    @Override
    public boolean isInvalid() {
        return isInvalidBoolean();
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        super.setErrorMessage(errorMessage);
    }

    /**
     * Gets the current error message from the combobox.
     *
     * @return the current error message
     */
    @Override
    public String getErrorMessage() {
        return getErrorMessageString();
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
    @Override
    public void setAllowCustomValue(boolean allowCustomValue) {
        super.setAllowCustomValue(allowCustomValue);
    }

    /**
     * If {@code true}, the user can input string values that do not match to
     * any existing item labels, which will fire a {@link CustomValueSetEvent}.
     *
     * @return {@code true} if the component fires custom value set events,
     *         {@code false} otherwise
     *
     * @see #setAllowCustomValue(boolean)
     * @see #addCustomValueSetListener(ComponentEventListener)
     */
    public boolean isAllowCustomValue() {
        return isAllowCustomValueBoolean();
    }

    /**
     * Set the combobox to be input focused when the page loads.
     *
     * @param autofocus
     *            the boolean value to set
     */
    @Override
    public void setAutofocus(boolean autofocus) {
        super.setAutofocus(autofocus);
    }

    /**
     * Get the state for the auto-focus property of the combobox.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     *
     * @return the {@code autofocus} property from the combobox
     */
    public boolean isAutofocus() {
        return isAutofocusBoolean();
    }

    @Override
    public void setPreventInvalidInput(boolean preventInvalidInput) {
        super.setPreventInvalidInput(preventInvalidInput);
    }

    /**
     * Determines whether preventing the user from inputing invalid value.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     *
     * @return the {@code preventInvalidInput} property of the combobox
     */
    public boolean isPreventInvalidInput() {
        return isPreventInvalidInputBoolean();
    }

    @Override
    public void setRequired(boolean required) {
        super.setRequired(required);
    }

    /**
     * Determines whether the combobox is marked as input required.
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
    public void setLabel(String label) {
        super.setLabel(label);
    }

    /**
     * Gets the label of the combobox.
     *
     * @return the {@code label} property of the combobox
     */
    public String getLabel() {
        return getLabelString();
    }

    @Override
    public void setPlaceholder(String placeholder) {
        super.setPlaceholder(placeholder);
    }

    /**
     * Gets the placeholder of the combobox.
     *
     * @return the {@code placeholder} property of the combobox
     */
    public String getPlaceholder() {
        return getPlaceholderString();
    }

    @Override
    public void setPattern(String pattern) {
        super.setPattern(pattern);
    }

    /**
     * Gets the valid input pattern
     *
     * @return the {@code pattern} property of the combobox
     */
    public String getPattern() {
        return getPatternString();
    }

    @Override
    public T getEmptyValue() {
        return null;
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

    /**
     * Allows displaying a clear button in the combo box when a value is
     * selected.
     * <p>
     * The clear button is an icon, which can be clicked to set the combo box
     * value to {@code null}.
     * 
     * @param clearButtonVisible
     *            {@code true} to display the clear button, {@code false} to
     *            hide it
     */
    @Override
    public void setClearButtonVisible(boolean clearButtonVisible) {
        super.setClearButtonVisible(clearButtonVisible);
    }

    /**
     * Gets whether this combo box displays a clear button when a value is
     * selected.
     * 
     * @return {@code true} if this combo box displays a clear button,
     *         {@code false} otherwise
     * @see #setClearButtonVisible(boolean)
     */
    public boolean isClearButtonVisible() {
        return super.isClearButtonVisibleBoolean();
    }

    CompositeDataGenerator<T> getDataGenerator() {
        return dataGenerator;
    }

    private String generateLabel(T item) {
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

    private void scheduleRender() {
        if (renderScheduled || dataCommunicator == null || renderer == null) {
            return;
        }
        renderScheduled = true;
        runBeforeClientResponse(ui -> {
            if (dataGeneratorRegistration != null) {
                dataGeneratorRegistration.remove();
                dataGeneratorRegistration = null;
            }
            Rendering<T> rendering = renderer.render(getElement(),
                    dataCommunicator.getKeyMapper(), template);
            if (rendering.getDataGenerator().isPresent()) {
                dataGeneratorRegistration = dataGenerator
                        .addDataGenerator(rendering.getDataGenerator().get());
            }
            reset();
        });
    }

    @ClientCallable
    private void confirmUpdate(int id) {
        dataCommunicator.confirmUpdate(id);
    }

    @ClientCallable
    private void setRequestedRange(int start, int length, String filter) {
        dataCommunicator.setRequestedRange(start, length);
        filterSlot.accept(filter);
    }

    @ClientCallable
    private void resetDataCommunicator() {
        dataCommunicator.reset();
    }

    void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    private void initConnector() {
        getElement().executeJs(
                "window.Vaadin.Flow.comboBoxConnector.initLazy(this)");
    }

    private DataKeyMapper<T> getKeyMapper() {
        return dataCommunicator.getKeyMapper();
    }

    private void setClientSideFilter(boolean clientSideFilter) {
        getElement().setProperty("_clientSideFilter", clientSideFilter);
    }

    private void reset() {
        lastFilter = null;
        if (dataCommunicator != null) {
            dataCommunicator.setRequestedRange(0, 0);
            dataCommunicator.reset();
        }
        runBeforeClientResponse(ui -> ui.getPage().executeJs(
                // If-statement is needed because on the first attach this
                // JavaScript is called before initializing the connector.
                "if($0.$connector) $0.$connector.reset();", getElement()));
    }

}
