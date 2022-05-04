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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasClearButton;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.dataview.ComboBoxDataView;
import com.vaadin.flow.component.combobox.dataview.ComboBoxLazyDataView;
import com.vaadin.flow.component.combobox.dataview.ComboBoxListDataView;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.ArrayUpdater.Update;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataChangeEvent.DataRefreshEvent;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderWrapper;
import com.vaadin.flow.data.provider.DataView;
import com.vaadin.flow.data.provider.DataViewUtils;
import com.vaadin.flow.data.provider.HasDataView;
import com.vaadin.flow.data.provider.HasLazyDataView;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.ListDataView;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.PropertyChangeEvent;
import com.vaadin.flow.function.SerializableBiPredicate;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.shared.Registration;

import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

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
@JsModule("./flow-component-renderer.js")
@JsModule("./comboBoxConnector.js")
public class ComboBox<T> extends GeneratedVaadinComboBox<ComboBox<T>, T>
        implements HasSize, HasValidation,
        HasDataView<T, String, ComboBoxDataView<T>>,
        HasListDataView<T, ComboBoxListDataView<T>>,
        HasLazyDataView<T, String, ComboBoxLazyDataView<T>>, HasHelper,
        HasTheme, HasLabel, HasClearButton {

    private static final String PROP_INPUT_ELEMENT_VALUE = "_inputElementValue";
    private static final String PROP_SELECTED_ITEM = "selectedItem";
    private static final String PROP_VALUE = "value";
    private static final String PROP_CLIENT_SIDE_FILTER = "_clientSideFilter";
    private static final String PROP_OPENED = "opened";

    private static final String COUNT_QUERY_WITH_UNDEFINED_SIZE_ERROR_MESSAGE = "Trying to use exact size with a lazy loading component"
            + " without either providing a count callback for the"
            + " component to fetch the count of the items or a data"
            + " provider that implements the size query. Provide the "
            + "callback for fetching item count with%n"
            + "comboBox.getLazyDataView().withDefinedSize(CallbackDataProvider.CountCallback);"
            + "%nor switch to undefined size with%n"
            + "comboBox.getLazyDataView().withUndefinedSize()";

    private Registration dataProviderListener = null;
    private boolean shouldForceServerSideFiltering = false;
    private static final String PROP_AUTO_OPEN_DISABLED = "autoOpenDisabled";

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

    private final class UpdateQueue implements Update {
        private transient List<Runnable> queue = new ArrayList<>();

        private UpdateQueue(int size) {
            enqueue("$connector.updateSize", size);

            // Triggers a size update on the client side.
            // This is exclusively needed for supporting immediate update of the
            // dropdown scroller size when the
            // LazyDataView::setItemCountEstimate() has been called, i.e. as
            // soon as the user opens the dropdown. Otherwise, the scroller
            // size update would be triggered only after a manual scrolling to
            // the next page, which is a bad UX.
            getElement().setProperty("size", size);
        }

        @Override
        public void set(int start, List<JsonValue> items) {
            enqueue("$connector.set", start,
                    items.stream().collect(JsonUtils.asArray()),
                    ComboBox.this.lastFilter);
        }

        @Override
        public void clear(int start, int length) {
            enqueue("$connector.clear", start, length);
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
        boolean test(T item, String filterText);
    }

    private ItemLabelGenerator<T> itemLabelGenerator = String::valueOf;

    private Renderer<T> renderer;
    private boolean renderScheduled;

    // Filter set by the client when requesting data. It's sent back to client
    // together with the response so client may know for what filter data is
    // provided.
    private String lastFilter;

    private DataCommunicator<T> dataCommunicator;
    private Registration lazyOpenRegistration;
    private Registration clearFilterOnCloseRegistration;
    private final CompositeDataGenerator<T> dataGenerator = new CompositeDataGenerator<>();
    private List<Registration> renderingRegistrations = new ArrayList<>();

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
                ComboBox::modelToPresentation, true);
        dataGenerator.addDataGenerator((item, jsonObject) -> jsonObject
                .put("label", generateLabel(item)));

        setItemValuePath("key");
        setItemIdPath("key");
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

        super.addValueChangeListener(e -> updateSelectedKey());
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
        if (dataCommunicator == null || dataCommunicator
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
        getElement().setProperty(PROP_INPUT_ELEMENT_VALUE,
                generateLabel(value));
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
     *
     *            Note that filtering of the ComboBox is not affected by the
     *            renderer that is set here. Filtering is done on the original
     *            values and can be affected by
     *            {@link #setItemLabelGenerator(ItemLabelGenerator)}.
     */
    public void setRenderer(Renderer<T> renderer) {
        Objects.requireNonNull(renderer, "The renderer must not be null");
        this.renderer = renderer;

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
     * <p>
     * Setting the items resets the combo box's value to {@code null}.
     */
    @Override
    public ComboBoxListDataView<T> setItems(Collection<T> items) {
        return HasListDataView.super.setItems(items);
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
     *
     * @return the in-memory data view instance that provides access to the data
     *         bound to the combo box
     */
    public ComboBoxListDataView<T> setItems(ItemFilter<T> itemFilter,
            Collection<T> items) {
        ListDataProvider<T> listDataProvider = DataProvider.ofCollection(items);
        setDataProvider(itemFilter, listDataProvider);
        return getListDataView();
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
     *
     * @return the in-memory data view instance that provides access to the data
     *         bound to the combo box
     */
    public ComboBoxListDataView<T> setItems(ItemFilter<T> itemFilter,
            @SuppressWarnings("unchecked") T... items) {
        return setItems(itemFilter, new ArrayList<>(Arrays.asList(items)));
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
    public void setItems(Stream<T> streamOfItems) {
        setItems(DataProvider.fromStream(streamOfItems));
    }

    @Override
    public ComboBoxDataView<T> setItems(DataProvider<T, String> dataProvider) {
        setDataProvider(dataProvider);
        return getGenericDataView();
    }

    /**
     * The method is not supported for the {@link ComboBox} component, use
     * another overloaded method with filter converter
     * {@link #setItems(InMemoryDataProvider, SerializableFunction)}
     * <p>
     * Always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException
     *
     * @see #setItems(InMemoryDataProvider, SerializableFunction)
     *
     * @deprecated does not work so don't use
     */
    @Deprecated
    @Override
    public ComboBoxDataView<T> setItems(InMemoryDataProvider<T> dataProvider) {
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
     *
     */
    public ComboBoxDataView<T> setItems(
            InMemoryDataProvider<T> inMemoryDataProvider,
            SerializableFunction<String, SerializablePredicate<T>> filterConverter) {
        Objects.requireNonNull(filterConverter,
                "FilterConverter cannot be null");
        final ComboBox<T> comboBox = this;
        // We don't use DataProvider.withConvertedFilter() here because its
        // implementation does not apply the filter converter if Query has a
        // null filter
        DataProvider<T, String> convertedDataProvider = new DataProviderWrapper<T, String, SerializablePredicate<T>>(
                inMemoryDataProvider) {
            @Override
            protected SerializablePredicate<T> getFilter(
                    Query<T, String> query) {
                final Optional<SerializablePredicate<T>> componentInMemoryFilter = DataViewUtils
                        .getComponentFilter(comboBox);
                return Optional.ofNullable(inMemoryDataProvider.getFilter())
                        .orElse(item -> true)
                        .and(item -> filterConverter
                                .apply(query.getFilter().orElse("")).test(item))
                        .and(componentInMemoryFilter.orElse(item -> true));
            }
        };

        // As well as for ListDataProvider, filtering will be handled in the
        // client-side if the size of the data set is less than the page size.
        if (userProvidedFilter == UserProvidedFilter.UNDECIDED) {
            userProvidedFilter = UserProvidedFilter.NO;
        }

        return setItems(convertedDataProvider);
    }

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
    public ComboBoxDataView<T> getGenericDataView() {
        return new ComboBoxDataView<T>(dataCommunicator, this);
    }

    @Override
    public ComboBoxLazyDataView<T> setItems(
            BackEndDataProvider<T, String> dataProvider) {
        setDataProvider(dataProvider);
        return getLazyDataView();
    }

    /**
     * Gets the lazy data view for the ComboBox. This data view should only be
     * used when the items are provided lazily from the backend with:
     * <ul>
     * <li>{@link #setItems(CallbackDataProvider.FetchCallback)}</li>
     * <li>{@link #setItemsWithFilterConverter(CallbackDataProvider.FetchCallback, SerializableFunction)}</>
     * <li>{@link #setItems(CallbackDataProvider.FetchCallback, CallbackDataProvider.CountCallback)}</li>
     * <li>{@link #setItemsWithFilterConverter(CallbackDataProvider.FetchCallback, CallbackDataProvider.CountCallback, SerializableFunction)}
     * </li>
     * <li>{@link #setItems(BackEndDataProvider)}</li>
     * </ul>
     * If the items are not fetched lazily an exception is thrown. When the
     * items are in-memory, use {@link #getListDataView()} instead.
     *
     * @throws IllegalStateException
     *             if no items fetch callback(s) set
     *
     * @return the lazy data view that provides access to the data bound to the
     *         ComboBox
     */
    @Override
    public ComboBoxLazyDataView<T> getLazyDataView() {
        return new ComboBoxLazyDataView<>(dataCommunicator, this);
    }

    @Override
    public ComboBoxListDataView<T> setItems(ListDataProvider<T> dataProvider) {
        setDataProvider(dataProvider);
        return getListDataView();
    }

    /**
     * Gets the list data view for the ComboBox. This data view should only be
     * used when the items are in-memory set with:
     * <ul>
     * <li>{@link #setItems(Collection)}</li>
     * <li>{@link #setItems(Object[])}</li>
     * <li>{@link #setItems(ListDataProvider)}</li>
     * <li>{@link #setItems(ItemFilter, ListDataProvider)}</li>
     * <li>{@link #setItems(ItemFilter, Object[])}</li>
     * <li>{@link #setItems(ItemFilter, Collection)}</li>
     * </ul>
     * If the items are not in-memory an exception is thrown. When the items are
     * fetched lazily, use {@link #getLazyDataView()} instead.
     *
     * @return the list data view that provides access to the items in the
     *         ComboBox
     */
    @Override
    public ComboBoxListDataView<T> getListDataView() {
        return new ComboBoxListDataView<T>(dataCommunicator, this,
                this::onInMemoryFilterOrSortingChange);
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
    public void setDataProvider(DataProvider<T, String> dataProvider) {
        setDataProvider(dataProvider, SerializableFunction.identity());
    }

    /**
     * Supply items lazily with a callback from a backend, using custom filter
     * type. The combo box will automatically fetch more items and adjust its
     * size until the backend runs out of items. Usage example:
     * <p>
     * {@code comboBox.setItemsWithFilterConverter(
     *                 query -> orderService.getOrdersByCount(query.getFilter(),
     *                                                        query.getOffset,
     *                                                        query.getLimit()),
     *                 orderCountStr -> Integer.parseInt(orderCountStr));} Note:
     * Validations for <code>orderCountStr</code> are omitted for briefness.
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
     *
     * @return ComboBoxLazyDataView instance for further configuration
     */
    public <C> ComboBoxLazyDataView<T> setItemsWithFilterConverter(
            CallbackDataProvider.FetchCallback<T, C> fetchCallback,
            SerializableFunction<String, C> filterConverter) {
        Objects.requireNonNull(fetchCallback, "Fetch callback cannot be null");
        ComboBoxLazyDataView<T> lazyDataView = setItemsWithFilterConverter(
                fetchCallback, query -> {
                    throw new IllegalStateException(
                            COUNT_QUERY_WITH_UNDEFINED_SIZE_ERROR_MESSAGE);
                }, filterConverter);
        lazyDataView.setItemCountUnknown();
        return lazyDataView;
    }

    /**
     * Supply items lazily with callbacks: the first one fetches the items based
     * on offset, limit and an optional filter, the second provides the exact
     * count of items in the backend. Use this only in case getting the count is
     * cheap and the user benefits from the component showing immediately the
     * exact size. Usage example:
     * <p>
     * {@code comboBox.setItemsWithFilterConverter(
     *                 query -> orderService.getOrdersByCount(query.getFilter(),
     *                                                        query.getOffset,
     *                                                        query.getLimit()),
     *                 query -> orderService.getSize(query.getFilter()),
     *                 orderCountStr -> Integer.parseInt(orderCountStr));} Note:
     * Validations for <code>orderCountStr</code> are omitted for briefness.
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
     * @param filterConverter
     *            a function which converts a combo box's filter-string typed by
     *            the user into a callback's object filter
     * @param <C>
     *            filter type used by a callbacks
     *
     * @return ComboBoxLazyDataView instance for further configuration
     */
    public <C> ComboBoxLazyDataView<T> setItemsWithFilterConverter(
            CallbackDataProvider.FetchCallback<T, C> fetchCallback,
            CallbackDataProvider.CountCallback<T, C> countCallback,
            SerializableFunction<String, C> filterConverter) {
        setDataProvider(DataProvider.fromFilteringCallbacks(fetchCallback,
                countCallback), filterConverter);
        return getLazyDataView();
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
    public <C> void setDataProvider(DataProvider<T, C> dataProvider,
            SerializableFunction<String, C> filterConverter) {
        Objects.requireNonNull(dataProvider,
                "The data provider can not be null");
        Objects.requireNonNull(filterConverter,
                "filterConverter cannot be null");

        if (userProvidedFilter == UserProvidedFilter.UNDECIDED) {
            userProvidedFilter = UserProvidedFilter.YES;
        }

        // Fetch from data provider is enabled eagerly if the data provider
        // is of in-memory type and it's not empty (no need to fetch from
        // empty data provider). Otherwise, the fetch will be postponed until
        // dropdown open event
        final boolean enableFetch = dataProvider.isInMemory()
                && !DataCommunicator.EmptyDataProvider.class
                        .isAssignableFrom(dataProvider.getClass());

        if (dataCommunicator == null) {
            // Create data communicator with postponed initialisation
            dataCommunicator = new DataCommunicator<>(dataGenerator,
                    arrayUpdater, data -> getElement()
                            .callJsFunction("$connector.updateData", data),
                    getElement().getNode(), enableFetch);
            dataCommunicator.setPageSize(getPageSize());
        } else {
            // Enable/disable items fetch from data provider depending on the
            // data provider type
            dataCommunicator.setFetchEnabled(enableFetch);
        }

        scheduleRender();
        setValue(null);

        SerializableFunction<String, C> convertOrNull = filterText -> {
            if (filterText == null) {
                return null;
            }

            return filterConverter.apply(filterText);
        };

        SerializableConsumer<DataCommunicator.Filter<C>> providerFilterSlot = dataCommunicator
                .setDataProvider(dataProvider,
                        convertOrNull.apply(getFilterString()), false);

        filterSlot = filter -> {
            if (!Objects.equals(filter, lastFilter)) {
                DataCommunicator.Filter<C> objectFilter = new DataCommunicator.Filter<C>(
                        convertOrNull.apply(filter), filter.isEmpty());
                providerFilterSlot.accept(objectFilter);
                lastFilter = filter;
            }
        };

        shouldForceServerSideFiltering = userProvidedFilter == UserProvidedFilter.YES;
        setupDataProviderListener(dataProvider);

        refreshAllData(shouldForceServerSideFiltering);

        userProvidedFilter = UserProvidedFilter.UNDECIDED;

        if (lazyOpenRegistration == null && !enableFetch) {
            // Register an opened listener to enable fetch and size queries to
            // data provider when the dropdown opens.
            lazyOpenRegistration = getElement().addPropertyChangeListener(
                    PROP_OPENED, this::executeRegistration);
        }
    }

    private void clearFilterOnClose(PropertyChangeEvent event) {
        if (Boolean.FALSE.equals(event.getValue())) {
            if (lastFilter != null && !lastFilter.isEmpty()) {
                clearClientSideFilterAndUpdateInMemoryFilter();
            }
        }
    }

    /**
     * Enables {@link DataCommunicator} to fetch items from {@link DataProvider}
     * when the open property changes for a lazy combobox. Clean registration on
     * initialization.
     *
     * @param event
     *            property change event for "open"
     */
    private void executeRegistration(PropertyChangeEvent event) {
        if (Boolean.TRUE.equals(event.getValue())) {
            removeLazyOpenRegistration();
            dataCommunicator.setFetchEnabled(true);
            if (!isAutoOpen()) {
                setRequestedRange(0, getPageSize(), this.getFilterString());
            }
        }
    }

    private <C> void setupDataProviderListener(
            DataProvider<T, C> dataProvider) {
        if (dataProviderListener != null) {
            dataProviderListener.remove();
        }
        dataProviderListener = dataProvider.addDataProviderListener(e -> {
            if (e instanceof DataRefreshEvent) {
                dataCommunicator.refresh(((DataRefreshEvent<T>) e).getItem());
            } else {
                refreshAllData(shouldForceServerSideFiltering);
            }
        });
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        DataProvider<T, ?> dataProvider = getDataProvider();
        if (dataProvider != null) {
            setupDataProviderListener(dataProvider);
        }

        clearFilterOnCloseRegistration = getElement().addPropertyChangeListener(
                PROP_OPENED, this::clearFilterOnClose);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (dataProviderListener != null) {
            dataProviderListener.remove();
            dataProviderListener = null;
        }

        if (clearFilterOnCloseRegistration != null) {
            clearFilterOnCloseRegistration.remove();
            clearFilterOnCloseRegistration = null;
        }
        super.onDetach(detachEvent);
    }

    private void refreshAllData(boolean forceServerSideFiltering) {
        if (dataCommunicator != null) {
            setClientSideFilter(!forceServerSideFiltering
                    && dataCommunicator.getItemCount() <= getPageSizeDouble());
        }

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
     * <p>
     * Changing the combo box's data provider resets its current value to
     * {@code null}.
     *
     * @param listDataProvider
     *            the list data provider to use, not <code>null</code>
     *
     * @deprecated use instead one of the {@code setItems} methods which provide
     *             access to {@link ComboBoxListDataView}
     */
    @Deprecated
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
     *
     * @deprecated use instead
     *             {@link #setItems(CallbackDataProvider.FetchCallback, CallbackDataProvider.CountCallback)}
     *             which provide access to {@link ComboBoxLazyDataView}
     */
    @Deprecated
    public void setDataProvider(FetchItemsCallback<T> fetchItems,
            SerializableFunction<String, Integer> sizeCallback) {
        Objects.requireNonNull(fetchItems, "Fetch callback cannot be null");
        Objects.requireNonNull(sizeCallback, "Size callback cannot be null");
        userProvidedFilter = UserProvidedFilter.YES;
        setDataProvider(new CallbackDataProvider<>(
                query -> fetchItems.fetchItems(query.getFilter().orElse(""),
                        query.getOffset(), query.getLimit()),
                query -> sizeCallback.apply(query.getFilter().orElse(""))));
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
     *
     * @deprecated use instead {@link #setItems(ItemFilter, ListDataProvider)}
     *             which provide access to {@link ComboBoxListDataView}
     */
    @Deprecated
    public void setDataProvider(ItemFilter<T> itemFilter,
            ListDataProvider<T> listDataProvider) {
        Objects.requireNonNull(listDataProvider,
                "List data provider cannot be null");

        setDataProvider(listDataProvider, filterText -> {
            Optional<SerializablePredicate<T>> componentInMemoryFilter = DataViewUtils
                    .getComponentFilter(this);
            return item -> itemFilter.test(item, filterText)
                    && componentInMemoryFilter.orElse(ignore -> true)
                            .test(item);
        });
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
     *
     * @return the in-memory data view instance that provides access to the data
     *         bound to the combo box
     */
    public ComboBoxListDataView<T> setItems(ItemFilter<T> itemFilter,
            ListDataProvider<T> listDataProvider) {
        setDataProvider(itemFilter, listDataProvider);
        return getListDataView();
    }

    /**
     * Gets the data provider used by this ComboBox.
     *
     * @return the data provider used by this ComboBox
     */
    public DataProvider<T, ?> getDataProvider() { // NOSONAR
        if (dataCommunicator != null) {
            return dataCommunicator.getDataProvider();
        }
        return null;
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
        super.setPageSize(pageSize);
        if (dataCommunicator != null) {
            dataCommunicator.setPageSize(pageSize);
        }
        refreshAllData(shouldForceServerSideFiltering);
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
     * Enables or disables the dropdown opening automatically. If {@code false}
     * the dropdown is only opened when clicking the toggle button or pressing
     * Up or Down arrow keys.
     *
     * @param autoOpen
     *            {@code false} to prevent the dropdown from opening
     *            automatically
     */
    public void setAutoOpen(boolean autoOpen) {
        getElement().setProperty(PROP_AUTO_OPEN_DISABLED, !autoOpen);
    }

    /**
     * Gets whether dropdown will open automatically or not.
     *
     * @return @{code true} if enabled, {@code false} otherwise
     */
    public boolean isAutoOpen() {
        return !getElement().getProperty(PROP_AUTO_OPEN_DISABLED, false);
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
        super.setRequiredIndicatorVisible(required);
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

    /**
     * Sets the label for the combobox.
     *
     * @param label
     *            value for the {@code label} property in the combobox
     */
    @Override
    public void setLabel(String label) {
        super.setLabel(label);
    }

    /**
     * Gets the label of the combobox.
     *
     * @return the {@code label} property of the combobox
     */
    @Override
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
     * Supply items lazily with a callback from a backend. The ComboBox will
     * automatically fetch more items and adjust its size until the backend runs
     * out of items. Usage example without component provided filter:
     * <p>
     * {@code comboBox.setItems(query ->
     *             orderService.getOrders(query.getOffset(), query.getLimit());}
     * <p>
     * Since ComboBox supports filtering, it can be fetched via
     * query.getFilter():
     * <p>
     * {@code comboBox.setItems(query ->
     *             orderService.getOrders(query.getFilter(), query.getOffset(),
     *                                      query.getLimit());}
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
    public ComboBoxLazyDataView<T> setItems(
            CallbackDataProvider.FetchCallback<T, String> fetchCallback) {
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
     *      query -> orderService.getOrders(query.getOffset, query.getLimit()),
     *      query -> orderService.getSize());}
     * <p>
     * Since ComboBox supports filtering, it can be fetched via
     * query.getFilter():
     * <p>
     * {@code comboBox.setItems(
     *      query -> orderService.getOrders(query.getFilter(), query.getOffset,
     *                                          query.getLimit()),
     *      query -> orderService.getSize(query.getFilter()));}
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
    public ComboBoxLazyDataView<T> setItems(
            CallbackDataProvider.FetchCallback<T, String> fetchCallback,
            CallbackDataProvider.CountCallback<T, String> countCallback) {
        return HasLazyDataView.super.setItems(fetchCallback, countCallback);
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
            render();
            renderScheduled = false;
        });
    }

    private void render() {
        renderingRegistrations.forEach(Registration::remove);
        renderingRegistrations.clear();

        Rendering<T> rendering;
        if (renderer instanceof LitRenderer) {
            // LitRenderer
            if (template != null && template.getParent() != null) {
                getElement().removeChild(template);
            }
            rendering = renderer.render(getElement(),
                    dataCommunicator.getKeyMapper());
        } else {
            // TemplateRenderer or ComponentRenderer
            if (template == null) {
                template = new Element("template");
            }
            if (template.getParent() == null) {
                getElement().appendChild(template);
            }
            rendering = renderer.render(getElement(),
                    dataCommunicator.getKeyMapper(), template);
        }

        rendering.getDataGenerator().ifPresent(renderingDataGenerator -> {
            Registration renderingDataGeneratorRegistration = dataGenerator
                    .addDataGenerator(renderingDataGenerator);
            renderingRegistrations.add(renderingDataGeneratorRegistration);
        });

        renderingRegistrations.add(rendering.getRegistration());

        reset();
    }

    private void updateSelectedKey() {
        // Send (possibly updated) key for the selected value
        getElement().executeJs("this._selectedKey=$0",
                getValue() != null ? getKeyMapper().key(getValue()) : "");
    }

    @ClientCallable
    private void confirmUpdate(int id) {
        dataCommunicator.confirmUpdate(id);
    }

    @ClientCallable
    private void setRequestedRange(int start, int length, String filter) {
        dataCommunicator.setRequestedRange(start, length);
        filterSlot.accept(filter);
        updateSelectedKey();
    }

    @ClientCallable
    private void resetDataCommunicator() {
        /*
         * The client filter from combo box will be used in the data
         * communicator only within 'setRequestedRange' calls to data provider,
         * and then will be erased to not affect the data view item count
         * handling methods. Thus, if the current client filter is not empty,
         * then we need to re-set it in the data communicator.
         */
        if (lastFilter == null || lastFilter.isEmpty()) {
            dataCommunicator.reset();
        } else {
            String filter = lastFilter;
            lastFilter = null;
            /*
             * This filter slot will eventually call the filter consumer in data
             * communicator and 'DataCommunicator::reset' is done inside this
             * consumer, so we don't need to explicitly call it.
             */
            filterSlot.accept(filter);
        }
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
        getElement().setProperty(PROP_CLIENT_SIDE_FILTER, clientSideFilter);
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

    private void removeLazyOpenRegistration() {
        if (lazyOpenRegistration != null) {
            lazyOpenRegistration.remove();
            lazyOpenRegistration = null;
        }
    }

    private void onInMemoryFilterOrSortingChange(
            SerializablePredicate<T> filter,
            SerializableComparator<T> sortComparator) {
        dataCommunicator.setInMemorySorting(sortComparator);
        clearClientSideFilterAndUpdateInMemoryFilter();
    }

    private void clearClientSideFilterAndUpdateInMemoryFilter() {
        lastFilter = null;
        filterSlot.accept("");
        reset();
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
