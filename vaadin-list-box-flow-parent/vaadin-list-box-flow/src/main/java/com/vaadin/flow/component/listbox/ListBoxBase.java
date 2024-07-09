/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.listbox;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.data.binder.HasDataProvider;
import com.vaadin.flow.data.binder.HasItemsAndComponents;
import com.vaadin.flow.data.provider.DataChangeEvent.DataRefreshEvent;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

/**
 * Base class for the {@link ListBox} and {@link MultiSelectListBox}.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-list-box")
@NpmPackage(value = "@vaadin/vaadin-list-box", version = "1.4.0")
@JsModule("@vaadin/vaadin-list-box/src/vaadin-list-box.js")
@HtmlImport("frontend://bower_components/vaadin-list-box/src/vaadin-list-box.html")
public abstract class ListBoxBase<C extends ListBoxBase<C, ITEM, VALUE>, ITEM, VALUE>
        extends AbstractSinglePropertyField<C, VALUE>
        implements HasItemsAndComponents<ITEM>, HasDataProvider<ITEM>, HasSize {

    private DataProvider<ITEM, ?> dataProvider = DataProvider.ofItems();
    private List<ITEM> items;
    private ComponentRenderer<? extends Component, ITEM> itemRenderer = new TextRenderer<>();
    private SerializablePredicate<ITEM> itemEnabledProvider = item -> isEnabled();
    private Registration dataProviderListenerRegistration;

    <P> ListBoxBase(String propertyName, Class<P> elementPropertyType,
            VALUE defaultValue,
            SerializableBiFunction<C, P, VALUE> presentationToModel,
            SerializableBiFunction<C, VALUE, P> modelToPresentation) {
        super(propertyName, defaultValue, elementPropertyType,
                presentationToModel, modelToPresentation);
    }

    @Override
    public void setDataProvider(DataProvider<ITEM, ?> dataProvider) {
        this.dataProvider = Objects.requireNonNull(dataProvider);
        clear();
        setupDataProviderListener(dataProvider);
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
        rebuild();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (getDataProvider() != null
                && dataProviderListenerRegistration == null) {
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
     */
    public DataProvider<ITEM, ?> getDataProvider() {
        return dataProvider;
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

    private void rebuild() {
        removeAll();
        items = getDataProvider().fetch(new Query<>())
                .collect(Collectors.toList());
        items.stream().map(this::createItemComponent).forEach(this::add);
    }

    private VaadinItem<ITEM> createItemComponent(ITEM item) {
        VaadinItem<ITEM> itemComponent = new VaadinItem<>(item);
        refresh(itemComponent);
        return itemComponent;
    }

    private void refresh(ITEM item) {
        VaadinItem<ITEM> itemComponent = getItemComponents().stream()
                .filter(component -> component.getItem().equals(item))
                .findFirst().get();
        refresh(itemComponent);
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
}
