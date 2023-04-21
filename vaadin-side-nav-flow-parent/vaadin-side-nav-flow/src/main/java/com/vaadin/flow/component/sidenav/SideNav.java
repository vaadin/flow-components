/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.sidenav;

import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.dom.Element;

/**
 * A side navigation menu with support for hierarchical and flat menus.
 * <p>
 * Items can be added using {@link #addItem(SideNavItem...)} and hierarchy can
 * be created by adding {@link SideNavItem} instances to other
 * {@link SideNavItem} instances.
 */
@Tag("vaadin-side-nav")
@JsModule("@vaadin/side-nav/src/vaadin-side-nav.js")
// @NpmPackage(value = "@vaadin/side-nav", version = "24.1.0-alpha8")
public class SideNav extends Component implements HasSize, HasStyle {

    /**
     * Creates a new menu without any label.
     */
    public SideNav() {
    }

    /**
     * Creates a new menu with the given label.
     *
     * @param label
     *            the label to use
     */
    public SideNav(String label) {
        setLabel(label);
    }

    /**
     * Adds menu item(s) to the menu.
     *
     * @param sideNavItems
     *            the menu item(s) to add
     * @return the menu for chaining
     */
    public SideNav addItem(SideNavItem... sideNavItems) {
        for (SideNavItem sideNavItem : sideNavItems) {
            getElement().appendChild(sideNavItem.getElement());
        }

        return this;
    }

    /**
     * Removes the menu item from the menu.
     * <p>
     * If the given menu item is not a child of this menu, does nothing.
     *
     * @param sideNavItem
     *            the menu item to remove
     * @return the menu for chaining
     */
    public SideNav removeItem(SideNavItem sideNavItem) {
        Optional<Component> parent = sideNavItem.getParent();
        if (parent.isPresent() && parent.get() == this) {
            getElement().removeChild(sideNavItem.getElement());
        }

        return this;
    }

    /**
     * Removes all menu items from this item.
     *
     * @return this item for chaining
     */
    public SideNav removeAllItems() {
        final List<Element> allNavItems = getElement().getChildren().filter(
                element -> !"label".equals(element.getAttribute("slot")))
                .toList();
        getElement().removeChild(allNavItems);
        return this;
    }

    /**
     * Gets the textual label for the navigation.
     *
     * @return the label or null if no label has been set
     */
    public String getLabel() {
        return getExistingLabelElement().map(Element::getText).orElse(null);
    }

    /**
     * Set a textual label for the navigation.
     * <p>
     * This can help the end user to distinguish groups of navigation items. The
     * label is also available for screen reader users.
     *
     * @param label
     *            the label to set
     * @return this instance for chaining
     */
    public SideNav setLabel(String label) {
        getLabelElement().setText(label);
        return this;
    }

    private Optional<Element> getExistingLabelElement() {
        return getElement().getChildren()
                .filter(child -> "label".equals(child.getAttribute("slot")))
                .findFirst();
    }

    private Element getLabelElement() {
        return getExistingLabelElement().orElseGet(() -> {
            Element element = new Element("span");
            element.setAttribute("slot", "label");
            getElement().appendChild(element);
            return element;
        });
    }

    /**
     * Check if the end user is allowed to collapse/hide and expand/show the
     * navigation items.
     * <p>
     * NOTE: The navigation has to have a label for it to be collapsible.
     *
     * @return true if the menu is collapsible, false otherwise
     */
    public boolean isCollapsible() {
        return getElement().hasAttribute("collapsible");
    }

    /**
     * Allow the end user to collapse/hide and expand/show the navigation items.
     * <p>
     * NOTE: The navigation has to have a label for it to be collapsible.
     *
     * @param collapsible
     *            true to make the whole navigation component collapsible, false
     *            otherwise
     * @return this instance for chaining
     */
    public SideNav setCollapsible(boolean collapsible) {
        if (collapsible) {
            getElement().setAttribute("collapsible", "");
        } else {
            getElement().removeAttribute("collapsible");
        }
        return this;
    }

}
