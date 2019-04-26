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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * Base class for item component used inside {@link ContextMenu}s.
 *
 * @see MenuItem
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
@SuppressWarnings("serial")
@Tag("vaadin-context-menu-item")
@NpmPackage(value = "@vaadin/vaadin-item", version = "2.1.0")
@JsModule("@vaadin/vaadin-item/vaadin-item.js")
public abstract class MenuItemBase<C extends ContextMenuBase<C, I, S>, I extends MenuItemBase<C, I, S>, S extends SubMenuBase<C, I, S>>
        extends Component implements HasText, HasComponents, HasEnabled {

    private final C contextMenu;
    private S subMenu;

    private boolean checkable = false;

    /**
     * Default constructor
     *
     * @param contextMenu
     *            the context menu to which this item belongs to
     */
    public MenuItemBase(C contextMenu) {
        this.contextMenu = contextMenu;
        getElement().addEventListener("click", e -> {
            if (checkable) {
                setChecked(!isChecked());
            }
        });

    }

    /**
     * Gets the context menu component that this item belongs to.
     *
     * @return the context-menu component
     */
    public C getContextMenu() {
        return contextMenu;
    }

    /**
     * Gets the sub menu API for this menu item. Adding content to the returned
     * sub menu makes this component a parent item which opens the sub menu
     * overlay. When the sub menu has no content, it won't be rendered.
     *
     * @return the sub menu that can be opened via this item
     */
    public S getSubMenu() {
        if (subMenu == null) {
            subMenu = createSubMenu();
        }
        return subMenu;
    }

    /**
     * Gets whether this item has a sub menu attached to it or not.
     *
     * @return {@code true} if this component has a sub menu with content inside
     *         it, {@code false} otherwise
     * @see #getSubMenu()
     */
    public boolean isParentItem() {
        return getSubMenu().getChildren().findAny().isPresent();
    }

    /**
     * Sets the checkable state of this menu item. A checkable item toggles a
     * checkmark icon when clicked. Changes in the checked state can be handled
     * in the item's click handler with {@link #isChecked()}.
     * <p>
     * Setting a checked item un-checkable also makes it un-checked.
     *
     * @param checkable
     *            {@code true} to enable toggling the checked-state of this menu
     *            item by clicking, {@code false} to disable it.
     * @throws IllegalStateException
     *             if setting a parent item checkable
     */
    public void setCheckable(boolean checkable) {
        if (checkable && isParentItem()) {
            throw new IllegalStateException(
                    "A checkable item cannot have a sub menu");
        }
        this.checkable = checkable;
        if (!checkable) {
            setChecked(false);
        }
    }

    /**
     * Gets whether this item toggles a checkmark icon when clicked.
     *
     * @return the checkable state of the item
     * @see #setCheckable(boolean)
     */
    public boolean isCheckable() {
        return checkable;
    }

    /**
     * Sets the checked state of this item. A checked item displays a checkmark
     * icon next to it. The checked state is also toggled by clicking the item.
     * <p>
     * Note that the item needs to be explicitly set as checkable via
     * {@link #setCheckable(boolean)} in order to check it.
     *
     * @param checked
     *            {@code true} to check this item, {@code false} to un-check it
     * @throws IllegalStateException
     *             if trying to check the item when it's checkable
     */
    public void setChecked(boolean checked) {
        if (isChecked() == checked) {
            return;
        }

        if (!checkable && checked) {
            throw new IllegalStateException(
                    "Trying to set a non-checkable menu item checked. "
                            + "Use setCheckable() to make the item checkable first.");
        }

        getElement().setProperty("_checked", checked);

        getElement().getNode().runWhenAttached(
                ui -> ui.beforeClientResponse(this, context -> {
                    ui.getPage().executeJavaScript(
                            "window.Vaadin.Flow.contextMenuConnector.setChecked($0, $1)",
                            getElement(), checked);
                }));
    }

    /**
     * Gets the checked state of this item. The item can be checked and
     * un-checked with {@link #setChecked(boolean)} or by clicking it when it is
     * checkable. A checked item displays a checkmark icon inside it.
     *
     * @return {@code true} if the item is checked, {@code false} otherwise
     * @see #setCheckable(boolean)
     * @see #setChecked(boolean)
     */
    public boolean isChecked() {
        return getElement().getProperty("_checked", false);
    }

    protected abstract S createSubMenu();
}
