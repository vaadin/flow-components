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
import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;

/**
 * Base class for sub-menus in {@link ContextMenuBase} extensions. Classes
 * extending this should provide API for adding items and handling events
 * related to them. For basic example, see {@link SubMenu}.
 *
 * @param <C>
 *            the context-menu type
 * @param <I>
 *            the menu-item type
 * @param <S>
 *            the sub menu type
 */
public abstract class SubMenuBase<C extends ContextMenuBase<C, I, S>, I extends MenuItemBase<C, I, S>, S extends SubMenuBase<C, I, S>>
        implements Serializable {

    private MenuManager<C, I, S> menuManager;
    private final I parentMenuItem;

    public SubMenuBase(I parentMenuItem) {
        this.parentMenuItem = parentMenuItem;
    }

    /**
     * Adds a new item component with the given text content to the sub menu
     * overlay.
     * <p>
     * This is a convenience method for the use case where you have a list of
     * highlightable {@link MenuItem}s inside the overlay. If you want to
     * configure the contents of the overlay without wrapping them inside
     * {@link MenuItem}s, or if you just want to add some non-highlightable
     * components between the items, use the {@link #add(Component...)} method.
     *
     * @param text
     *            the text content for the created menu item
     * @return the created menu item
     * @see #add(Component...)
     */
    public I addItem(String text) {
        return getMenuManager().addItem(text);
    }

    /**
     * Adds a new item component with the given component to the sub menu
     * overlay.
     * <p>
     * This is a convenience method for the use case where you have a list of
     * highlightable {@link MenuItem}s inside the overlay. If you want to
     * configure the contents of the overlay without wrapping them inside
     * {@link MenuItem}s, or if you just want to add some non-highlightable
     * components between the items, use the {@link #add(Component...)} method.
     *
     * @param component
     *            the component to add to the created menu item
     * @return the created menu item
     * @see #add(Component...)
     */
    public I addItem(Component component) {
        return getMenuManager().addItem(component);
    }

    /**
     * Adds the given components into the sub menu overlay.
     * <p>
     * For the common use case of having a list of high-lightable items inside
     * the overlay, you can use the {@link #addItem(String)} convenience methods
     * instead.
     * <p>
     * The added elements will be inserted into an overlay that is attached into
     * the {@code <body>}.
     *
     * @param components
     *            the components to add
     * @see HasMenuItems#addItem(String, ComponentEventListener)
     * @see HasMenuItems#addItem(Component, ComponentEventListener)
     */
    public void add(Component... components) {
        getMenuManager().add(components);
    }

    /**
     * Removes the given components from the sub menu overlay.
     *
     * @param components
     *            the components to remove
     * @see #add(Component...)
     */
    public void remove(Component... components) {
        getMenuManager().remove(components);
    }

    /**
     * Removes all components inside the sub menu overlay.
     *
     * @see #add(Component...)
     */
    public void removeAll() {
        getMenuManager().removeAll();
    }

    /**
     * Adds the given component into the sub menu overlay at the given index.
     * <p>
     * The added elements will be inserted into an overlay that is attached into
     * the {@code <body>}.
     *
     * @param index
     *            the index, where the component will be added
     * @param component
     *            the component to add
     * @see #add(Component...)
     */
    public void addComponentAtIndex(int index, Component component) {
        getMenuManager().addComponentAtIndex(index, component);
    }

    /**
     * Gets the child components of this sub menu. This includes components
     * added with {@link #add(Component...)} and the {@link MenuItem} components
     * created with {@link #addItem(String)} and its overload methods. This
     * doesn't include the components added to the main context menu or any
     * other sub menus it may have.
     *
     * @return the child components of this sub menu
     */
    public Stream<Component> getChildren() {
        return getMenuManager().getChildren();
    }

    /**
     * Gets the items added to this sub menu (the children of this component
     * that are instances of {@link MenuItem}).
     *
     * @return the {@link MenuItem} components in this sub menu
     * @see #addItem(String)
     * @see #getChildren()
     */
    public List<I> getItems() {
        return getMenuManager().getItems();
    }

    /**
     * Gets the menu item component that opens this sub menu overlay.
     *
     * @return the parent menu item of this sub menu
     */
    public I getParentMenuItem() {
        return parentMenuItem;
    }

    /**
     * Gets a (sub) menu manager.
     *
     * @return
     */
    protected MenuManager<C, I, S> getMenuManager() {
        if (menuManager == null) {
            menuManager = createMenuManager();
        }
        return menuManager;
    }

    /**
     * Creates a (sub) menu manager instance which contains logic to control the
     * (sub) menu content.
     *
     * @return a new menu manager instance
     */
    protected abstract MenuManager<C, I, S> createMenuManager();
}
