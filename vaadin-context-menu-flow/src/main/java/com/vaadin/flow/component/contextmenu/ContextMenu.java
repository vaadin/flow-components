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
package com.vaadin.flow.component.contextmenu;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasComponents;

/**
 * Server-side component for {@code <vaadin-context-menu>}
 * 
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
public class ContextMenu extends ContextMenuBase<ContextMenu>
        implements HasComponents {

    /**
     * Creates an empty context menu.
     */
    public ContextMenu() {
        super();
    }

    /**
     * Creates an empty context menu with the given target component.
     * 
     * @param target
     *            the target component for this context menu
     * @see #setTarget(Component)
     */
    public ContextMenu(Component target) {
        this();
        setTarget(target);
    }

    /**
     * Adds a new item component with the given text content and click listener
     * to the context menu overlay.
     * <p>
     * This is a convenience method for the use case where you have a list of
     * highlightable {@link MenuItem}s inside the overlay. If you want to
     * configure the contents of the overlay without wrapping them inside
     * {@link MenuItem}s, or if you just want to add some non-highlightable
     * components between the items, use the {@link #add(Component...)} method.
     * 
     * @param text
     *            the text content for the new item
     * @param clickListener
     *            the handler for clicking the new item, can be {@code null} to
     *            not add listener
     * @return the added {@link MenuItem} component
     * @see #addItem(Component, ComponentEventListener)
     * @see #add(Component...)
     */
    public MenuItem addItem(String text,
            ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        MenuItem menuItem = addItem(text);
        if (clickListener != null) {
            menuItem.addClickListener(clickListener);
        }
        return menuItem;
    }

    /**
     * Adds a new item component with the given component and click listener to
     * the context menu overlay.
     * <p>
     * This is a convenience method for the use case where you have a list of
     * highlightable {@link MenuItem}s inside the overlay. If you want to
     * configure the contents of the overlay without wrapping them inside
     * {@link MenuItem}s, or if you just want to add some non-highlightable
     * components between the items, use the {@link #add(Component...)} method.
     * 
     * @param component
     *            the component inside the new item
     * @param clickListener
     *            the handler for clicking the new item, can be {@code null} to
     *            not add listener
     * @return the added {@link MenuItem} component
     * @see #addItem(String, ComponentEventListener)
     * @see #add(Component...)
     */
    public MenuItem addItem(Component component,
            ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        MenuItem menuItem = addItem(component);
        if (clickListener != null) {
            menuItem.addClickListener(clickListener);
        }
        return menuItem;
    }
}
