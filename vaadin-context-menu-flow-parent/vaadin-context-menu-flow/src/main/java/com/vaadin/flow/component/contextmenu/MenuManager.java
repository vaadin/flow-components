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
package com.vaadin.flow.component.contextmenu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableRunnable;

/**
 * Common management logic for context menus and sub menus. Maintains the list
 * of components to stamp into one overlay.
 *
 * @param <C>
 *            the context menu type
 * @param <I>
 *            the menu item type
 * @param <S>
 *            the sub menu type
 *
 * @author Vaadin Ltd.
 */
public class MenuManager<C extends Component, I extends MenuItemBase<?, I, S>, S extends SubMenuBase<?, I, S>>
        implements Serializable {

    private final C menu;
    private final SerializableBiFunction<C, SerializableRunnable, I> itemGenerator;
    private final Class<I> itemType;
    private final I parentMenuItem;
    private final SerializableRunnable contentReset;

    private final List<Component> children = new ArrayList<>();

    /**
     * Creates a new manager instance.
     *
     * @param menu
     *            the context menu
     * @param contentReset
     *            callback to reset the context menu
     * @param itemGenerator
     *            the item generator/factory
     * @param itemType
     *            the item type
     * @param parentMenuItem
     *            the parent menu item of the submenu
     */
    public MenuManager(C menu, SerializableRunnable contentReset,
            SerializableBiFunction<C, SerializableRunnable, I> itemGenerator,
            Class<I> itemType, I parentMenuItem) {
        this.menu = menu;
        this.contentReset = contentReset;
        this.itemGenerator = itemGenerator;
        this.itemType = itemType;
        this.parentMenuItem = parentMenuItem;
    }

    /**
     * Adds a text as a menu item.
     *
     * @param text
     *            the text for the menu item
     * @return a new menu item
     */
    public I addItem(String text) {
        I menuItem = itemGenerator.apply(menu, contentReset);
        menuItem.setText(text);
        add(menuItem);
        return menuItem;
    }

    /**
     * Adds a component as a menu item.
     *
     * @param component
     *            the component for the menu item
     * @return a new menu item
     */
    public I addItem(Component component) {
        I menuItem = itemGenerator.apply(menu, contentReset);
        add(menuItem);
        menuItem.add(component);
        return menuItem;
    }

    /**
     * Adds a text as a menu item with a click listener.
     *
     * @param text
     *            the text for the menu item
     * @param clickListener
     *            a click listener
     * @return a new menu item
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public I addItem(String text,
            ComponentEventListener<ClickEvent<I>> clickListener) {
        I menuItem = addItem(text);
        if (clickListener != null) {
            ComponentUtil.addListener(menuItem, ClickEvent.class,
                    (ComponentEventListener) clickListener);
        }
        return menuItem;
    }

    /**
     * Adds a component as a menu item with a click listener.
     *
     * @param component
     *            the component for the menu item
     * @param clickListener
     *            a click listener
     * @return a new menu item
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public I addItem(Component component,
            ComponentEventListener<ClickEvent<I>> clickListener) {
        I menuItem = addItem(component);
        if (clickListener != null) {
            ComponentUtil.addListener(menuItem, ClickEvent.class,
                    (ComponentEventListener) clickListener);
        }
        return menuItem;
    }

    /**
     * Adds components to the (sub)menu.
     * <p>
     * The components are added into the content as is, they are not wrapped as
     * menu items.
     *
     * @param components
     *            components to add
     * @see #remove(Component...)
     * @see #addComponentAtIndex(int, Component)
     */
    public void add(Component... components) {
        if (parentMenuItem != null && parentMenuItem.isCheckable()) {
            throw new IllegalStateException(
                    "A checkable item cannot have a sub menu");
        }
        Objects.requireNonNull(components, "Components to add cannot be null");
        for (Component component : components) {
            Objects.requireNonNull(component,
                    "Component to add cannot be null");
            children.add(component);
        }
        if (components.length > 0) {
            updateChildren();
        }
    }

    /**
     * Removes components to the (sub)menu.
     *
     * @param components
     *            components to remove
     * @see #add(Component...)
     */
    public void remove(Component... components) {
        Objects.requireNonNull(components,
                "Components to remove cannot be null");
        boolean needUpdate = false;
        for (Component component : components) {
            Objects.requireNonNull(component,
                    "Component to remove cannot be null");
            if (children.remove(component)) {
                needUpdate = true;
            } else {
                throw new IllegalArgumentException("The given component ("
                        + component + ") is not a child of this component");
            }
        }
        if (needUpdate) {
            updateChildren();
        }
    }

    /**
     * Remove all components and items from (sub)menu.
     *
     * @see #remove(Component...)
     */
    public void removeAll() {
        children.clear();
        updateChildren();
    }

    /**
     * Inserts component to the (sub)menu using the {@code index}.
     * <p>
     * The component is inserted into the content as is, it is not wrapped as a
     * menu item.
     *
     * @param index
     *            index to insert, not negative
     * @param component
     *            component to insert
     *
     * @see #add(Component...)
     * @see #remove(Component...)
     */
    public void addComponentAtIndex(int index, Component component) {
        if (parentMenuItem != null && parentMenuItem.isCheckable()) {
            throw new IllegalStateException(
                    "A checkable item cannot have a sub menu");
        }
        Objects.requireNonNull(component, "Component should not be null");
        if (index < 0) {
            throw new IllegalArgumentException(
                    "Cannot add a component with a negative index");
        }
        children.add(index, component);
        updateChildren();
    }

    /**
     * Gets all (sub)menu children.
     * <p>
     * Children consist of components and items.
     *
     * @see #add(Component...)
     * @see #addItem(Component)
     *
     * @see #getItems()
     *
     * @return the children components
     */
    public Stream<Component> getChildren() {
        return children.stream();
    }

    /**
     * Gets all children items.
     * <p>
     * The items are filtered using the provided item type in the constructor.
     *
     * @see #getChildren()
     *
     * @return all children items
     */
    public List<I> getItems() {
        return getChildren().filter(itemType::isInstance).map(itemType::cast)
                .collect(Collectors.toList());
    }

    private void updateChildren() {
        contentReset.run();
    }

}
