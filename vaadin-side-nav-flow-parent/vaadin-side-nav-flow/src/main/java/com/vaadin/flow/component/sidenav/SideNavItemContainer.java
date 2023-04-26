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
import com.vaadin.flow.dom.Element;

abstract class SideNavItemContainer extends Component {

    /**
     * Implement this method to set up/modify the SideNavItem right before it's
     * added to the list of the navigation items.
     * 
     * @param item
     *            Item to be set up
     */
    protected void setupSideNavItem(SideNavItem item) {
        // no setup by default
    }

    /**
     * Adds navigation menu item(s) to the menu.
     *
     * @param items
     *            the navigation menu item(s) to add
     */
    public void addItem(SideNavItem... items) {
        for (SideNavItem item : items) {
            setupSideNavItem(item);
            getElement().appendChild(item.getElement());
        }
    }

    public void addItemAsFirst(SideNavItem item) {
        addItemAtIndex(0, item);
    }

    public void addItemAtIndex(int index, SideNavItem item) {
        if (index < 0) {
            throw new IllegalArgumentException(
                    "Cannot add a SideNavItem with a negative index");
        }

        final List<SideNavItem> items = getItems();

        if (index > items.size()) {
            throw new IllegalArgumentException(String.format(
                    "Cannot insert item with index %d when there are %d items",
                    index, items.size()));
        }

        if (index == items.size()) {
            addItem(item);
        } else {
            setupSideNavItem(item);
            int insertPosition = getElement()
                    .indexOfChild(items.get(index).getElement());
            getElement().insertChild(insertPosition, item.getElement());
        }
    }

    public List<SideNavItem> getItems() {
        return getElement().getChildren().map(Element::getComponent)
                .flatMap(Optional::stream)
                .filter(component -> component instanceof SideNavItem)
                .map(component -> (SideNavItem) component).toList();
    }

    /**
     * Removes the menu item(s) from the menu.
     * <p>
     * If the given menu item is not a child of this menu, does nothing.
     *
     * @param items
     *            the menu item(s) to remove
     */
    public void remove(SideNavItem... items) {
        for (SideNavItem item : items) {
            Optional<Component> parent = item.getParent();
            if (parent.isPresent() && parent.get() == this) {
                getElement().removeChild(item.getElement());
            }
        }
    }

    /**
     * Removes all navigation menu items from this item.
     */
    public void removeAll() {
        final List<Element> items = getItems().stream()
                .map(Component::getElement).toList();
        getElement().removeChild(items);
    }

}
