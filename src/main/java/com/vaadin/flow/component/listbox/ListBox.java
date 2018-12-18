/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.data.binder.HasDataProvider;
import com.vaadin.flow.data.binder.HasItemsAndComponents;
import com.vaadin.flow.data.provider.DataChangeEvent.DataRefreshEvent;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * Server-side component for the {@code vaadin-list-box} element.
 *
 * @author Vaadin Ltd
 *
 * @param <T>
 *            the type of the items contained by this component
 */
// Not extending the generated class since it adds no value
@Tag("vaadin-list-box")
@HtmlImport("frontend://bower_components/vaadin-list-box/src/vaadin-list-box.html")
public class ListBox<T> extends AbstractSinglePropertyField<ListBox<T>, T>
        implements HasItemsAndComponents<T>, SingleSelect<ListBox<T>, T>,
        HasDataProvider<T>, HasComponents, HasSize {

    private DataProvider<T, ?> dataProvider = DataProvider.ofItems();
    private ComponentRenderer<? extends Component, T> itemRenderer = new TextRenderer<>();
    private SerializablePredicate<T> itemEnabledProvider = item -> isEnabled();

    public ListBox() {
        super("selected", null, Integer.class, ListBox::presentationToModel,
                ListBox::modelToPresentation);
    }

    private static <T> T presentationToModel(ListBox<T> listBox,
            Integer selectedIndex) {
        if (selectedIndex == null || selectedIndex.intValue() == -1) {
            return null;
        }

        return listBox.getItemComponents().get(selectedIndex.intValue())
                .getItem();
    }

    private static <T> Integer modelToPresentation(ListBox<T> listBox,
            T selectedItem) {
        if (selectedItem == null) {
            return Integer.valueOf(-1);
        }

        List<VaadinItem<T>> itemComponents = listBox.getItemComponents();
        int itemIndex = IntStream.range(0, itemComponents.size()).filter(
                i -> selectedItem.equals(itemComponents.get(i).getItem()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "Could not find given value from the item set"));
        return Integer.valueOf(itemIndex);
    }

    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        this.dataProvider = Objects.requireNonNull(dataProvider);
        dataProvider.addDataProviderListener(event -> {
            if (event instanceof DataRefreshEvent) {
                refresh(((DataRefreshEvent<T>) event).getItem());
            } else {
                rebuild();
            }
        });
        rebuild();
    }

    /**
     * Gets the data provider.
     *
     * @return the data provider, not {@code null}
     */
    public DataProvider<T, ?> getDataProvider() {
        return dataProvider;
    }

    /**
     * Returns the item component renderer.
     *
     * @return the item renderer
     * @see #setRenderer
     */
    public ComponentRenderer<? extends Component, T> getItemRenderer() {
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
            ComponentRenderer<? extends Component, T> itemRenderer) {
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
            SerializablePredicate<T> itemEnabledProvider) {
        this.itemEnabledProvider = Objects.requireNonNull(itemEnabledProvider);
        getItemComponents().forEach(this::refreshEnabled);
    }

    /**
     * Returns the item enabled predicate.
     *
     * @return the item enabled predicate
     * @see #setItemEnabledProvider
     */
    public SerializablePredicate<T> getItemEnabledProvider() {
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
        setValue(null);
        getDataProvider().fetch(new Query<>()).map(this::createItemComponent)
                .forEach(this::add);
    }

    private VaadinItem<T> createItemComponent(T item) {
        VaadinItem<T> itemComponent = new VaadinItem<>(item);
        refresh(itemComponent);
        return itemComponent;
    }

    private void refresh(T item) {
        VaadinItem<T> itemComponent = getItemComponents().stream()
                .filter(component -> component.getItem().equals(item))
                .findFirst().get();
        refresh(itemComponent);
    }

    private void refresh(VaadinItem<T> itemComponent) {
        refreshContent(itemComponent);
        refreshEnabled(itemComponent);
    }

    private void refreshContent(VaadinItem<T> itemComponent) {
        itemComponent.removeAll();
        itemComponent
                .add(itemRenderer.createComponent(itemComponent.getItem()));
    }

    @Override
    public void onEnabledStateChanged(boolean enabled) {
        super.onEnabledStateChanged(enabled);
        getItemComponents().forEach(this::refreshEnabled);
    }

    private void refreshEnabled(VaadinItem<T> itemComponent) {
        itemComponent
                .setEnabled(itemEnabledProvider.test(itemComponent.getItem()));
    }

    @SuppressWarnings("unchecked")
    private List<VaadinItem<T>> getItemComponents() {
        return getChildren().filter(VaadinItem.class::isInstance)
                .map(component -> (VaadinItem<T>) component)
                .collect(Collectors.toList());
    }
}
