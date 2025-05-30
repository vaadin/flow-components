/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;

/**
 * A common interface for components that can have {@link MenuItem}s with click
 * listeners inside them.
 *
 * @see ContextMenu
 * @see SubMenu
 *
 * @author Vaadin Ltd.
 */
public interface HasMenuItems extends Serializable {

    /**
     * Creates a new menu item with the given text content and click listener
     * and adds it to the context menu overlay.
     *
     * @param text
     *            the text content for the new item
     * @param clickListener
     *            the handler for clicking the new item, can be {@code null} to
     *            not add listener
     * @return the added {@link MenuItem} component
     * @see #addItem(Component, ComponentEventListener)
     */
    MenuItem addItem(String text,
            ComponentEventListener<ClickEvent<MenuItem>> clickListener);

    /**
     * Creates a new menu item with the given component content and click
     * listener and adds it to the context menu overlay.
     *
     * @param component
     *            the component inside the new item
     * @param clickListener
     *            the handler for clicking the new item, can be {@code null} to
     *            not add listener
     * @return the added {@link MenuItem} component
     * @see #addItem(String, ComponentEventListener)
     */
    MenuItem addItem(Component component,
            ComponentEventListener<ClickEvent<MenuItem>> clickListener);

}
