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

import com.vaadin.flow.component.combobox.dataview.ComboBoxDataView;
import com.vaadin.flow.component.combobox.dataview.ComboBoxLazyDataView;
import com.vaadin.flow.component.combobox.dataview.ComboBoxListDataView;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataChangeEvent;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderWrapper;
import com.vaadin.flow.data.provider.DataViewUtils;
import com.vaadin.flow.data.provider.HasDataView;
import com.vaadin.flow.data.provider.HasLazyDataView;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.dom.PropertyChangeEvent;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Internal class that encapsulates the data communication logic with the web
 * component
 *
 * @param <TItem>
 *            Type of individual items that are selectable in the combo box
 */
class ComboBoxDataController<TItem>
        implements HasDataView<TItem, String, ComboBoxDataView<TItem>>,
        HasListDataView<TItem, ComboBoxListDataView<TItem>>,
        HasLazyDataView<TItem, String, ComboBoxLazyDataView<TItem>> {
    private enum UserProvidedFilter {
        UNDECIDED, YES, NO
    }

    private final class UpdateQueue implements ArrayUpdater.Update {
        private final transient List<Runnable> queue = new ArrayList<>();

        private UpdateQueue(int size) {
            enqueue("$connector.updateSize", size);

            // Triggers a size update on the client side.
            // This is exclusively needed for supporting immediate update of the
            // dropdown scroller size when the
            // LazyDataView::setItemCountEstimate() has been called, i.e. as
            // soon as the user opens the dropdown. Otherwise, the scroller
            // size update would be triggered only after a manual scrolling to
            // the next page, which is a bad UX.
            ComboBoxDataController.this.comboBox.getElement()
                    .setProperty("size", size);
        }

        @Override
        public void set(int start, List<JsonValue> items) {
            enqueue("$connector.set", start,
                    items.stream().collect(JsonUtils.asArray()),
                    ComboBoxDataController.this.lastFilter);
        }

        @Override
        public void clear(int start, int length) {
            enqueue("$connector.clear", start, length);
        }

        @Override
        public void commit(int updateId) {
            enqueue("$connector.confirm", updateId,
                    ComboBoxDataController.this.lastFilter);
            queue.forEach(Runnable::run);
            queue.clear();
        }

        private void enqueue(String name, Serializable... arguments) {
            queue.add(() -> ComboBoxDataController.this.comboBox.getElement()
                    .callJsFunction(name, arguments));
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

    private final ComboBoxBase<?, TItem, ?> comboBox;
    private final SerializableSupplier<Locale> localeSupplier;

    private ComboBoxDataCommunicator<TItem> dataCommunicator;

    private final CompositeDataGenerator<TItem> dataGenerator = new CompositeDataGenerator<>();

    private UserProvidedFilter userProvidedFilter = UserProvidedFilter.UNDECIDED;

    private boolean shouldForceServerSideFiltering = false;

    // Filter set by the client when requesting data. It's sent back to client
    // together with the response so client may know for what filter data is
    // provided.
    private String lastFilter;

    private SerializableConsumer<String> filterSlot = filter -> {
        // Just ignore when setDataProvider has not been called
    };

    private Registration lazyOpenRegistration;
    private Registration clearFilterOnCloseRegistration;
    private Registration dataProviderListener = null;

    /**
     * Creates a new data controller for that combo box
     *
     * @param comboBox
     *            the combo box that this controller manages
     * @param localeSupplier
     *            supplier for the current locale of the combo box
     */
    ComboBoxDataController(ComboBoxBase<?, TItem, ?> comboBox,
            SerializableSupplier<Locale> localeSupplier) {
        this.comboBox = comboBox;
        this.localeSupplier = localeSupplier;
    }

    /**
     * Accesses the data communicator managed by this controller
     */
    ComboBoxDataCommunicator<TItem> getDataCommunicator() {
        return dataCommunicator;
    }

    /**
     * Accesses the data provider managed by this controller
     */
    DataProvider<TItem, ?> getDataProvider() {
        if (dataCommunicator != null) {
            return dataCommunicator.getDataProvider();
        }
        return null;
    }

    /**
     * Accesses the data generator managed by this controller
     */
    CompositeDataGenerator<TItem> getDataGenerator() {
        return dataGenerator;
    }

    /**
     * Updates the page size in the data communicator and triggers a full
     * refresh
     */
    void setPageSize(int pageSize) {
        if (dataCommunicator != null) {
            dataCommunicator.setPageSize(pageSize);
        }
        refreshAllData(shouldForceServerSideFiltering);
    }

    /**
     * Called to notify this controller that the component has been attached
     */
    void onAttach() {
        DataProvider<TItem, ?> dataProvider = getDataProvider();
        if (dataProvider != null) {
            setupDataProviderListener(dataProvider);
        }

        clearFilterOnCloseRegistration = comboBox.getElement()
                .addPropertyChangeListener("opened", this::clearFilterOnClose);
    }

    /**
     * Called to notify this controller that the component has been detached
     */
    void onDetach() {
        if (dataProviderListener != null) {
            dataProviderListener.remove();
            dataProviderListener = null;
        }

        if (clearFilterOnCloseRegistration != null) {
            clearFilterOnCloseRegistration.remove();
            clearFilterOnCloseRegistration = null;
        }
    }

    /**
     * Refresh item data of the web component when data has been updated, or
     * after changes that might affect the presentation / rendering of items
     */
    void reset() {
        lastFilter = null;
        if (dataCommunicator != null) {
            dataCommunicator.setRequestedRange(0, 0);
            dataCommunicator.reset();
        }
        comboBox.runBeforeClientResponse(ui -> ui.getPage().executeJs(
                // If-statement is needed because on the first attach this
                // JavaScript is called before initializing the connector.
                "if($0.$connector) $0.$connector.reset();",
                comboBox.getElement()));
    }

    /**
     * Called to confirm that an update has been processed by the client-side
     * connector
     *
     * @param id
     *            the update identifier
     */
    void confirmUpdate(int id) {
        dataCommunicator.confirmUpdate(id);
    }

    /**
     * Called when the client-side connector requests data
     */
    void setRequestedRange(int start, int length, String filter) {
        // If the filter is null, which indicates that the combo box was closed
        // before, then reset the data communicator to force sending an update
        // to the client connector. This covers an edge-case when using an empty
        // lazy data provider and refreshing it before opening the combo box
        // again. In that case the data provider thinks that the client should
        // already be up-to-date from the refresh, as in both cases, refresh and
        // empty data provider, the effective requested size is zero, which
        // results in it not sending an update. However, the client needs to
        // receive an update in order to clear the loading state from opening
        // the combo box.
        if (lastFilter == null) {
            dataCommunicator.reset();
        }
        dataCommunicator.setRequestedRange(start, length);
        filterSlot.accept(filter);
    }

    /**
     * Called by the client-side connector to reset the data communicator
     */
    void resetDataCommunicator() {
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

    // ****************************************************
    // List data view implementation
    // ****************************************************

    @Override
    public ComboBoxListDataView<TItem> getListDataView() {
        return new ComboBoxListDataView<>(getDataCommunicator(), comboBox,
                this::onInMemoryFilterOrSortingChange);
    }

    public ComboBoxListDataView<TItem> setItems(
            ComboBox.ItemFilter<TItem> itemFilter, Collection<TItem> items) {
        ListDataProvider<TItem> listDataProvider = DataProvider
                .ofCollection(items);
        setDataProvider(itemFilter, listDataProvider);
        return getListDataView();
    }

    @SafeVarargs
    public final ComboBoxListDataView<TItem> setItems(
            ComboBox.ItemFilter<TItem> itemFilter, TItem... items) {
        return setItems(itemFilter, new ArrayList<>(Arrays.asList(items)));
    }

    public ComboBoxListDataView<TItem> setItems(
            ComboBox.ItemFilter<TItem> itemFilter,
            ListDataProvider<TItem> listDataProvider) {
        setDataProvider(itemFilter, listDataProvider);
        return getListDataView();
    }

    @Override
    public ComboBoxListDataView<TItem> setItems(
            ListDataProvider<TItem> dataProvider) {
        setDataProvider(dataProvider);
        return getListDataView();
    }

    // ****************************************************
    // Lazy data view implementation
    // ****************************************************

    @Override
    public ComboBoxLazyDataView<TItem> getLazyDataView() {
        return new ComboBoxLazyDataView<>(getDataCommunicator(), comboBox);
    }

    public <TComponent> ComboBoxLazyDataView<TItem> setItemsWithFilterConverter(
            CallbackDataProvider.FetchCallback<TItem, TComponent> fetchCallback,
            SerializableFunction<String, TComponent> filterConverter) {
        Objects.requireNonNull(fetchCallback, "Fetch callback cannot be null");
        ComboBoxLazyDataView<TItem> lazyDataView = setItemsWithFilterConverter(
                fetchCallback, query -> {
                    throw new IllegalStateException(
                            "Trying to use exact size with a lazy loading component"
                                    + " without either providing a count callback for the"
                                    + " component to fetch the count of the items or a data"
                                    + " provider that implements the size query. Provide the "
                                    + "callback for fetching item count with%n"
                                    + "comboBox.getLazyDataView().withDefinedSize(CallbackDataProvider.CountCallback);"
                                    + "%nor switch to undefined size with%n"
                                    + "comboBox.getLazyDataView().withUndefinedSize()");
                }, filterConverter);
        lazyDataView.setItemCountUnknown();
        return lazyDataView;
    }

    public <TComponent> ComboBoxLazyDataView<TItem> setItemsWithFilterConverter(
            CallbackDataProvider.FetchCallback<TItem, TComponent> fetchCallback,
            CallbackDataProvider.CountCallback<TItem, TComponent> countCallback,
            SerializableFunction<String, TComponent> filterConverter) {
        setDataProvider(DataProvider.fromFilteringCallbacks(fetchCallback,
                countCallback), filterConverter);
        return getLazyDataView();
    }

    @Override
    public ComboBoxLazyDataView<TItem> setItems(
            BackEndDataProvider<TItem, String> dataProvider) {
        setDataProvider(dataProvider);
        return getLazyDataView();
    }

    // ****************************************************
    // Generic data view implementation
    // ****************************************************

    @Override
    public ComboBoxDataView<TItem> getGenericDataView() {
        return new ComboBoxDataView<>(dataCommunicator, comboBox);
    }

    @Override
    public ComboBoxDataView<TItem> setItems(
            DataProvider<TItem, String> dataProvider) {
        setDataProvider(dataProvider);
        return getGenericDataView();
    }

    @Override
    public ComboBoxDataView<TItem> setItems(
            InMemoryDataProvider<TItem> dataProvider) {
        throw new UnsupportedOperationException();
    }

    public ComboBoxDataView<TItem> setItems(
            InMemoryDataProvider<TItem> inMemoryDataProvider,
            SerializableFunction<String, SerializablePredicate<TItem>> filterConverter) {
        Objects.requireNonNull(filterConverter,
                "FilterConverter cannot be null");
        // We don't use DataProvider.withConvertedFilter() here because its
        // implementation does not apply the filter converter if Query has a
        // null filter
        DataProvider<TItem, String> convertedDataProvider = new DataProviderWrapper<>(
                inMemoryDataProvider) {
            @Override
            protected SerializablePredicate<TItem> getFilter(
                    Query<TItem, String> query) {
                final Optional<SerializablePredicate<TItem>> componentInMemoryFilter = DataViewUtils
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

    // ****************************************************
    // Data provider implementation
    // ****************************************************

    public void setDataProvider(DataProvider<TItem, String> dataProvider) {
        setDataProvider(dataProvider, SerializableFunction.identity());
    }

    public void setDataProvider(ListDataProvider<TItem> listDataProvider) {
        if (userProvidedFilter == UserProvidedFilter.UNDECIDED) {
            userProvidedFilter = UserProvidedFilter.NO;
        }

        // Cannot use the case insensitive contains shorthand from
        // ListDataProvider since it wouldn't react to locale changes
        ComboBox.ItemFilter<TItem> defaultItemFilter = (item,
                filterText) -> comboBox.getItemLabelGenerator().apply(item)
                        .toLowerCase(localeSupplier.get())
                        .contains(filterText.toLowerCase(localeSupplier.get()));

        setDataProvider(defaultItemFilter, listDataProvider);
    }

    public void setDataProvider(ComboBox.FetchItemsCallback<TItem> fetchItems,
            SerializableFunction<String, Integer> sizeCallback) {
        Objects.requireNonNull(fetchItems, "Fetch callback cannot be null");
        Objects.requireNonNull(sizeCallback, "Size callback cannot be null");
        userProvidedFilter = UserProvidedFilter.YES;
        setDataProvider(new CallbackDataProvider<>(
                query -> fetchItems.fetchItems(query.getFilter().orElse(""),
                        query.getOffset(), query.getLimit()),
                query -> sizeCallback.apply(query.getFilter().orElse(""))));
    }

    public void setDataProvider(ComboBox.ItemFilter<TItem> itemFilter,
            ListDataProvider<TItem> listDataProvider) {
        Objects.requireNonNull(listDataProvider,
                "List data provider cannot be null");

        setDataProvider(listDataProvider, filterText -> {
            Optional<SerializablePredicate<TItem>> componentInMemoryFilter = DataViewUtils
                    .getComponentFilter(comboBox);
            SerializablePredicate<TItem> componentInMemoryFilterOrAlwaysPass = componentInMemoryFilter
                    .orElse(ignore -> true);
            return item -> itemFilter.test(item, filterText)
                    && componentInMemoryFilterOrAlwaysPass.test(item);
        });
    }

    public <TComponent> void setDataProvider(
            DataProvider<TItem, TComponent> dataProvider,
            SerializableFunction<String, TComponent> filterConverter) {
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
            dataCommunicator = new ComboBoxDataCommunicator<>(comboBox,
                    dataGenerator, arrayUpdater,
                    data -> comboBox.getElement()
                            .callJsFunction("$connector.updateData", data),
                    comboBox.getElement().getNode(), enableFetch);
            dataCommunicator.setPageSize(comboBox.getPageSize());
        } else {
            // Enable/disable items fetch from data provider depending on the
            // data provider type
            dataCommunicator.setFetchEnabled(enableFetch);
        }

        comboBox.getRenderManager().scheduleRender();
        comboBox.setValue(null);

        SerializableFunction<String, TComponent> convertOrNull = filterText -> {
            if (filterText == null) {
                return null;
            }

            return filterConverter.apply(filterText);
        };

        SerializableConsumer<DataCommunicator.Filter<TComponent>> providerFilterSlot = dataCommunicator
                .setDataProvider(dataProvider,
                        convertOrNull.apply(comboBox.getFilter()), false);

        filterSlot = filter -> {
            if (!Objects.equals(filter, lastFilter)) {
                DataCommunicator.Filter<TComponent> objectFilter = new DataCommunicator.Filter<>(
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
            lazyOpenRegistration = comboBox.getElement()
                    .addPropertyChangeListener("opened",
                            this::executeRegistration);
        }
    }

    private void refreshAllData(boolean forceServerSideFiltering) {
        if (dataCommunicator != null) {
            setClientSideFilter(!forceServerSideFiltering && dataCommunicator
                    .getItemCount() <= comboBox.getPageSize());
        }

        reset();
    }

    private void setClientSideFilter(boolean clientSideFilter) {
        comboBox.getElement().setProperty("_clientSideFilter",
                clientSideFilter);
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
            if (!comboBox.isAutoOpen()) {
                setRequestedRange(0, comboBox.getPageSize(),
                        comboBox.getFilter());
            }
        }
    }

    private void removeLazyOpenRegistration() {
        if (lazyOpenRegistration != null) {
            lazyOpenRegistration.remove();
            lazyOpenRegistration = null;
        }
    }

    private void onInMemoryFilterOrSortingChange(
            SerializablePredicate<TItem> filter,
            SerializableComparator<TItem> sortComparator) {
        dataCommunicator.setInMemorySorting(sortComparator);
        clearClientSideFilterAndUpdateInMemoryFilter();
    }

    private void clearClientSideFilterAndUpdateInMemoryFilter() {
        lastFilter = null;
        filterSlot.accept("");
        reset();
    }

    private <C> void setupDataProviderListener(
            DataProvider<TItem, C> dataProvider) {
        if (dataProviderListener != null) {
            dataProviderListener.remove();
        }
        dataProviderListener = dataProvider.addDataProviderListener(e -> {
            if (e instanceof DataChangeEvent.DataRefreshEvent) {
                dataCommunicator
                        .refresh(((DataChangeEvent.DataRefreshEvent<TItem>) e)
                                .getItem());
            } else {
                refreshAllData(shouldForceServerSideFiltering);
            }
        });
    }
}
