/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import com.vaadin.flow.function.SerializableRunnable;

/**
 * API that allows adding content into the sub menus of a {@link ContextMenu} to
 * create hierarchical menus. Get it by calling {@link MenuItem#getSubMenu()} on
 * the item component that should open the sub menu. Sub menu will be rendered
 * only if content has been added inside it.
 *
 * @author Vaadin Ltd.
 */
public class SubMenu extends SubMenuBase<ContextMenu, MenuItem, SubMenu>
        implements HasMenuItems {

    private final SerializableRunnable contentReset;

    public SubMenu(MenuItem parentMenuItem, SerializableRunnable contentReset) {
        super(parentMenuItem);
        this.contentReset = contentReset;
    }

    @Override
    public MenuItem addItem(String text,
            ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        return getMenuManager().addItem(text, clickListener);
    }

    @Override
    public MenuItem addItem(Component component,
            ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        return getMenuManager().addItem(component, clickListener);
    }

    /**
     * Creates a new {@link MenuItem} component with the given text content and
     * tooltip text and adds it to this sub menu.
     *
     * @param text
     *            the text content for the new item
     * @param tooltipText
     *            the tooltip text for the new item
     * @return the added {@link MenuItem} component
     */
    public MenuItem addItem(String text, String tooltipText) {
        var item = addItem(text);
        item.setTooltipText(tooltipText);
        return item;
    }

    /**
     * Creates a new {@link MenuItem} component with the given tooltip text and
     * adds it to this sub menu. The provided component is added into the
     * created {@link MenuItem}.
     *
     * @param component
     *            the component to add inside the new item
     * @param tooltipText
     *            the tooltip text for the new item
     * @return the added {@link MenuItem} component
     */
    public MenuItem addItem(Component component, String tooltipText) {
        var item = addItem(component);
        item.setTooltipText(tooltipText);
        return item;
    }

    /**
     * Creates a new {@link MenuItem} component with the given text content,
     * tooltip text and click listener and adds it to this sub menu.
     *
     * @param text
     *            the text content for the new item
     * @param tooltipText
     *            the tooltip text for the new item
     * @param clickListener
     *            the handler for clicking the new item, can be {@code null} to
     *            not add listener
     * @return the added {@link MenuItem} component
     */
    public MenuItem addItem(String text, String tooltipText,
            ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        var item = addItem(text, clickListener);
        item.setTooltipText(tooltipText);
        return item;
    }

    /**
     * Creates a new {@link MenuItem} component with the given tooltip text and
     * click listener and adds it to this sub menu. The provided component is
     * added into the created {@link MenuItem}.
     *
     * @param component
     *            the component to add inside the new item
     * @param tooltipText
     *            the tooltip text for the new item
     * @param clickListener
     *            the handler for clicking the new item, can be {@code null} to
     *            not add listener
     * @return the added {@link MenuItem} component
     */
    public MenuItem addItem(Component component, String tooltipText,
            ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        var item = addItem(component, clickListener);
        item.setTooltipText(tooltipText);
        return item;
    }

    @Override
    protected MenuManager<ContextMenu, MenuItem, SubMenu> createMenuManager() {
        return new MenuManager<>(getParentMenuItem().getContextMenu(),
                contentReset, MenuItem::new, MenuItem.class,
                getParentMenuItem());
    }
}
