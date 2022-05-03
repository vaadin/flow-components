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
package com.vaadin.flow.component.listbox;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.listbox.dataview.ListBoxDataView;
import com.vaadin.flow.component.listbox.dataview.ListBoxListDataView;
import com.vaadin.flow.data.binder.HasItemComponents;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.DataChangeEvent.DataRefreshEvent;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderWrapper;
import com.vaadin.flow.data.provider.DataViewUtils;
import com.vaadin.flow.data.provider.HasDataView;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.flow.data.provider.IdentifierProvider;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.ItemCountChangeEvent;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.ListDataView;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

/**
 * Base class for the {@link ListBox} and {@link MultiSelectListBox}.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-list-box")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/list-box", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-list-box", version = "23.1.0-beta1")
@JsModule("@vaadin/list-box/src/vaadin-list-box.js")
public abstract class ListBoxBase<C extends ListBoxBase<C, ITEM, VALUE>, ITEM, VALUE>
        extends AbstractSinglePropertyField<C, VALUE>
        implements HasItemComponents<ITEM>, HasSize,
        HasListDataView<ITEM, ListBoxListDataView<ITEM>>,
        HasDataView<ITEM, Void, ListBoxDataView<ITEM>>, HasStyle {

    private final AtomicReference<DataProvider<ITEM, ?>> dataProvider = new AtomicReference<>(
            DataProvider.ofItems());
    private List<ITEM> items;
    private ItemLabelGenerator<ITEM> itemLabelGenerator = String::valueOf;
    private ComponentRenderer<? extends Component, ITEM> itemRenderer = new TextRenderer<>(
            itemLabelGenerator);
    private SerializablePredicate<ITEM> itemEnabledProvider = item -> isEnabled();
    private Registration dataProviderListenerRegistration;

    private int lastNotifiedDataSize = -1;
    private volatile int lastFetchedDataSize = -1;
    private SerializableConsumer<UI> sizeRequest;

    <P> ListBoxBase(String propertyName, Class<P> elementPropertyType,
            VALUE defaultValue,
            SerializableBiFunction<C, P, VALUE> presentationToModel,
            SerializableBiFunction<C, VALUE, P> modelToPresentation) {
        super(propertyName, defaultValue, elementPropertyType,
                presentationToModel, modelToPresentation);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated use instead one of the {@code setItems} methods which provide
     *             access to either {@link ListBoxListDataView} or
     *             {@link ListBoxDataView}
     */
    @Deprecated
    public void setDataProvider(DataProvider<ITEM, ?> dataProvider) {
        this.dataProvider.set(Objects.requireNonNull(dataProvider));
        DataViewUtils.removeComponentFilterAndSortComparator(this);
        clear();
        rebuild();
        setupDataProviderListener(this.dataProvider.get());
    }

    private void setupDataProviderListener(DataProvider<ITEM, ?> dataProvider) {
        if (dataProviderListenerRegistration != null) {
            dataProviderListenerRegistration.remove();
        }
        dataProviderListenerRegistration = dataProvider
                .addDataProviderListener(event -> {
                    if (event instanceof DataRefreshEvent) {
                        refresh(((DataRefreshEvent<ITEM>) event).getItem());
                    } else {
                        clear();
                        rebuild();
                    }
                });
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (getDataProvider() != null) {
            setupDataProviderListener(getDataProvider());
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (dataProviderListenerRegistration != null) {
            dataProviderListenerRegistration.remove();
            dataProviderListenerRegistration = null;
        }
        super.onDetach(detachEvent);
    }

    /**
     * Gets the data provider.
     *
     * @return the data provider, not {@code null}
     * @deprecated use {@link #getListDataView()} or
     *             {@link #getGenericDataView()} instead
     */
    @Deprecated
    public DataProvider<ITEM, ?> getDataProvider() {
        return dataProvider.get();
    }

    /**
     * Returns the item component renderer.
     *
     * @return the item renderer
     * @see #setRenderer
     */
    public ComponentRenderer<? extends Component, ITEM> getItemRenderer() {
        return itemRenderer;
    }

    /**
     * Sets the item renderer for this ListBox. The renderer is applied to each
     * item to create a component which represents the item.
     *
     * @param itemRenderer
     *            the item renderer, not {@code null}
     */
    public void setRenderer(
            ComponentRenderer<? extends Component, ITEM> itemRenderer) {
        this.itemRenderer = Objects.requireNonNull(itemRenderer);
        getItemComponents().forEach(this::refreshContent);
    }

    /**
     * Sets the item label generator that is used to produce the strings shown
     * in the ListBox for each item. By default, {@link String#valueOf(Object)}
     * is used.
     *
     * @param itemLabelGenerator
     *            the item label provider to use, not null
     */
    public void setItemLabelGenerator(
            ItemLabelGenerator<ITEM> itemLabelGenerator) {
        Objects.requireNonNull(itemLabelGenerator,
                "The item label generator can not be null");
        this.itemLabelGenerator = itemLabelGenerator;
        setRenderer(new TextRenderer<>(this.itemLabelGenerator));
    }

    /**
     * Gets the item label generator that is used to produce the strings shown
     * in the ListBox for each item.
     *
     * @return the item label generator used, not null
     */
    public ItemLabelGenerator<ITEM> getItemLabelGenerator() {
        return itemLabelGenerator;
    }

    /**
     * Sets the item enabled predicate for this ListBox. The predicate is
     * applied to each item to determine whether the item should be enabled
     * ({@code true}) or disabled ({@code false}). Disabled items are displayed
     * as grayed out and the user cannot select them. The default predicate
     * always returns true (all the items are enabled).
     *
     * @param itemEnabledProvider
     *            the item enable predicate, not {@code null}
     */
    public void setItemEnabledProvider(
            SerializablePredicate<ITEM> itemEnabledProvider) {
        this.itemEnabledProvider = Objects.requireNonNull(itemEnabledProvider);
        getItemComponents().forEach(this::refreshEnabled);
    }

    /**
     * Returns the item enabled predicate.
     *
     * @return the item enabled predicate
     * @see #setItemEnabledProvider
     */
    public SerializablePredicate<ITEM> getItemEnabledProvider() {
        return itemEnabledProvider;
    }

    /**
     * <b>Not supported!</b>
     * <p>
     * Not supported by the client-side web-component, see
     * <a href= "https://github.com/vaadin/vaadin-list-box/issues/19">issue in
     * GitHub</a>.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        throw new UnsupportedOperationException(
                "Not supported by the client-side web-component: "
                        + "https://github.com/vaadin/vaadin-list-box/issues/19");
    }

    @SuppressWarnings("unchecked")
    private void rebuild() {
        removeAll();

        synchronized (dataProvider) {
            final AtomicInteger itemCounter = new AtomicInteger(0);
            items = (List<ITEM>) getDataProvider()
                    .fetch(DataViewUtils.getQuery(this))
                    .collect(Collectors.toList());
            items.stream().map(this::createItemComponent).forEach(component -> {
                add(component);
                itemCounter.incrementAndGet();
            });
            lastFetchedDataSize = itemCounter.get();

            // Ignore new size requests unless the last one has been executed
            // so as to avoid multiple beforeClientResponses.
            if (sizeRequest == null) {
                sizeRequest = ui -> {
                    fireSizeEvent();
                    sizeRequest = null;
                };
                // Size event is fired before client response so as to avoid
                // multiple size change events during server round trips
                runBeforeClientResponse(sizeRequest);
            }
        }
    }

    private VaadinItem<ITEM> createItemComponent(ITEM item) {
        VaadinItem<ITEM> itemComponent = new VaadinItem<>(item);
        refresh(itemComponent);
        return itemComponent;
    }

    private void refresh(ITEM item) {
        getItemComponents().stream()
                .filter(vaadinItem -> getItemId(vaadinItem.getItem())
                        .equals(getItemId(item)))
                .findFirst().ifPresent(this::refresh);
    }

    private void refresh(VaadinItem<ITEM> itemComponent) {
        refreshContent(itemComponent);
        refreshEnabled(itemComponent);
    }

    private void refreshContent(VaadinItem<ITEM> itemComponent) {
        itemComponent.removeAll();
        itemComponent
                .add(itemRenderer.createComponent(itemComponent.getItem()));
    }

    @Override
    public void onEnabledStateChanged(boolean enabled) {
        super.onEnabledStateChanged(enabled);
        getItemComponents().forEach(this::refreshEnabled);
    }

    private void refreshEnabled(VaadinItem<ITEM> itemComponent) {
        itemComponent
                .setEnabled(itemEnabledProvider.test(itemComponent.getItem()));
    }

    List<ITEM> getItems() {
        return items;
    }

    @SuppressWarnings("unchecked")
    List<VaadinItem<ITEM>> getItemComponents() {
        return getChildren().filter(VaadinItem.class::isInstance)
                .map(component -> (VaadinItem<ITEM>) component)
                .collect(Collectors.toList());
    }

    /**
     * Set a generic data provider for the ListBox to use and returns the base
     * {@link ListBoxDataView} that provides API to get information on the
     * items.
     * <p>
     * This method should be used only when the data provider type is not either
     * {@link ListDataProvider} or {@link BackEndDataProvider}.
     *
     * @param dataProvider
     *            DataProvider instance to use, not <code>null</code>
     * @return ListBoxDataView providing information on the data
     */
    @Override
    public ListBoxDataView<ITEM> setItems(
            DataProvider<ITEM, Void> dataProvider) {
        setDataProvider(dataProvider);
        return getGenericDataView();
    }

    /**
     * Sets an in-memory data provider for the ListBox to use
     * <p>
     * Note! Using a {@link ListDataProvider} instead of a
     * {@link InMemoryDataProvider} is recommended to get access to
     * {@link ListBoxListDataView} API by using
     * {@link HasListDataView#setItems(ListDataProvider)}.
     *
     * @param inMemoryDataProvider
     *            InMemoryDataProvider to use, not <code>null</code>
     * @return ListBoxDataView providing information on the data
     */
    @Override
    public ListBoxDataView<ITEM> setItems(
            InMemoryDataProvider<ITEM> inMemoryDataProvider) {
        // We don't use DataProvider.withConvertedFilter() here because it's
        // implementation does not apply the filter converter if Query has a
        // null filter
        DataProvider<ITEM, Void> convertedDataProvider = new DataProviderWrapper<ITEM, Void, SerializablePredicate<ITEM>>(
                inMemoryDataProvider) {
            @Override
            protected SerializablePredicate<ITEM> getFilter(
                    Query<ITEM, Void> query) {
                // Just ignore the query filter (Void) and apply the
                // predicate only
                return Optional.ofNullable(inMemoryDataProvider.getFilter())
                        .orElse(item -> true);
            }
        };
        return setItems(convertedDataProvider);
    }

    /**
     * Sets a ListDataProvider for the ListBox to use and returns a
     * {@link ListDataView} that provides information and allows operations on
     * the items.
     *
     * @param listDataProvider
     *            ListDataProvider providing items to the ListBox.
     * @return ListBoxListDataView providing access to the items
     */
    @Override
    public ListBoxListDataView<ITEM> setItems(
            ListDataProvider<ITEM> listDataProvider) {
        setDataProvider(listDataProvider);
        return getListDataView();
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Because the stream is collected to a list anyway, use
     *             {@link HasListDataView#setItems(Collection)} instead.
     */
    @Deprecated
    public void setItems(Stream<ITEM> streamOfItems) {
        setItems(DataProvider.fromStream(streamOfItems));
    }

    /**
     * Gets the list data view for the ListBox. This data view should only be
     * used when the items are in-memory and set with:
     * <ul>
     * <li>{@link #setItems(Collection)}</li>
     * <li>{@link #setItems(Object[])}</li>
     * <li>{@link #setItems(ListDataProvider)}</li>
     * </ul>
     * If the items are not in-memory an exception is thrown.
     *
     * @return the list data view that provides access to the data bound to the
     *         ListBox
     */
    @Override
    public ListBoxListDataView<ITEM> getListDataView() {
        return new ListBoxListDataView<>(this::getDataProvider, this,
                (filter, sorting) -> rebuild());
    }

    /**
     * Gets the generic data view for the ListBox. This data view should only be
     * used when {@link #getListDataView()} is not applicable for the underlying
     * data provider.
     *
     * @return the generic DataView instance implementing
     *         {@link ListBoxDataView}
     */
    @Override
    public ListBoxDataView<ITEM> getGenericDataView() {
        return new ListBoxDataView<>(this::getDataProvider, this);
    }

    private void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    private void fireSizeEvent() {
        final int newSize = lastFetchedDataSize;
        if (lastNotifiedDataSize != newSize) {
            lastNotifiedDataSize = newSize;
            fireEvent(new ItemCountChangeEvent<>(this, newSize, false));
        }
    }

    protected Object getItemId(ITEM item) {
        return getIdentifierProvider().apply(item);
    }

    @SuppressWarnings("unchecked")
    private IdentifierProvider<ITEM> getIdentifierProvider() {
        IdentifierProvider<ITEM> identifierProviderObject = (IdentifierProvider<ITEM>) ComponentUtil
                .getData(this, IdentifierProvider.class);
        if (identifierProviderObject != null)
            return identifierProviderObject;

        DataProvider<ITEM, ?> dataProvider = getDataProvider();
        if (dataProvider != null)
            return dataProvider::getId;

        return IdentifierProvider.identity();
    }

}
