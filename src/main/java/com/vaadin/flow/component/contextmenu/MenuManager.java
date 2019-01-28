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
 * @param S
 *            the sub menu type
 *
 * @author Vaadin Ltd.
 */
class MenuManager<C extends ContextMenuBase<C, I, S>, I extends MenuItemBase<C, I, S>, S extends SubMenuBase<C, I, S>>
        implements Serializable {

    private final C menu;
    private final SerializableBiFunction<C, SerializableRunnable, I> itemGenerator;
    private final Class<I> itemType;
    private final I parentMenuItem;
    private final SerializableRunnable contentReset;

    private final List<Component> children = new ArrayList<>();

    MenuManager(C menu, SerializableRunnable contentReset,
            SerializableBiFunction<C, SerializableRunnable, I> itemGenerator,
            Class<I> itemType, I parentMenuItem) {
        this.menu = menu;
        this.contentReset = contentReset;
        this.itemGenerator = itemGenerator;
        this.itemType = itemType;
        this.parentMenuItem = parentMenuItem;
    }

    I addItem(String text) {
        I menuItem = itemGenerator.apply(menu, contentReset);
        menuItem.setText(text);
        add(menuItem);
        return menuItem;
    }

    I addItem(Component component) {
        I menuItem = itemGenerator.apply(menu, contentReset);
        add(menuItem);
        menuItem.add(component);
        return menuItem;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    I addItem(String text,
            ComponentEventListener<ClickEvent<I>> clickListener) {
        I menuItem = addItem(text);
        if (clickListener != null) {
            ComponentUtil.addListener(menuItem, ClickEvent.class,
                    (ComponentEventListener) clickListener);
        }
        return menuItem;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    I addItem(Component component,
            ComponentEventListener<ClickEvent<I>> clickListener) {
        I menuItem = addItem(component);
        if (clickListener != null) {
            ComponentUtil.addListener(menuItem, ClickEvent.class,
                    (ComponentEventListener) clickListener);
        }
        return menuItem;
    }

    void add(Component... components) {
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

    void remove(Component... components) {
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

    void removeAll() {
        children.clear();
        updateChildren();
    }

    void addComponentAtIndex(int index, Component component) {
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

    Stream<Component> getChildren() {
        return children.stream();
    }

    List<I> getItems() {
        return getChildren().filter(itemType::isInstance).map(itemType::cast)
                .collect(Collectors.toList());
    }

    private void updateChildren() {
        contentReset.run();
    }

}
