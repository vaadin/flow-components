/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.contextmenu;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.function.SerializableRunnable;

/**
 * Context Menu is a component that you can attach to any component to display a
 * context menu. The menu appears on right (default) or left click. On a touch
 * device, a long press opens the context menu. You can use dividers to separate
 * and group related content. Use dividers sparingly, though, to avoid creating
 * unnecessary visual clutter.
 * <p>
 * Context Menu, like Menu Bar, supports multi-level sub-menus. You can use a
 * hierarchical menu to organize a large set of options and group related items.
 * Moreover, Context Menu supports checkable menu items that can be used to
 * toggle a setting on and off. It also supports disabling menu items to show
 * that they are unavailable. Menu items can also be customized to include more
 * than a single line of text. You can use left-click to open Context Menu in
 * situations where left-click does not have any other function, for example a
 * Grid without selection support.
 * <p>
 * Best Practices:<br>
 * Context Menu is used to provide shortcuts to the user. You should not use it
 * as the only or primary means to complete a task. The primary way should be
 * accessible elsewhere in the UI. Also note that you should use Context Menu
 * when there is no dedicated button for opening an overlay menu, such as
 * right-clicking a grid row. When there is a dedicated element/component, such
 * as an overflow menu, use Menu Bar.
 *
 * @author Vaadin Ltd.
 */
public class ContextMenu extends ContextMenuBase<ContextMenu, MenuItem, SubMenu>
        implements HasMenuItems {

    /**
     * Creates an empty context menu.
     */
    public ContextMenu() {
        getElement().setAttribute("suppress-template-warning", true);
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

    @Override
    protected MenuManager<ContextMenu, MenuItem, SubMenu> createMenuManager(
            SerializableRunnable contentReset) {
        return new MenuManager<>(this, contentReset, MenuItem::new,
                MenuItem.class, null);
    }

}
