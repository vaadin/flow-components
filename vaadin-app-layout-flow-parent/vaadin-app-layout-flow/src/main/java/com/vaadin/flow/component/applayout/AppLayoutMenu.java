package com.vaadin.flow.component.applayout;

/*
 * #%L
 * Vaadin App Layout for Vaadin 10
 * %%
 * Copyright (C) 2017 - 2018 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.component.AttachNotifier;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.dom.Element;

import java.util.Objects;
import java.util.Optional;

/**
 * Tabs for AppLayout.
 */
class AppLayoutMenu implements HasElement, AttachNotifier {

    private final Tabs tabs = new Tabs();
    private AppLayoutMenuItem selectedMenuItem;

    /**
     * Initializes a new app layout with a default menu.
     */
    AppLayoutMenu() {
        getElement().setAttribute("slot", "menu");
        getElement().setAttribute("theme", "minimal");

        tabs.addAttachListener(attachEvent -> {
            tabs.setSelectedTab(selectedMenuItem);
            tabs.addSelectedChangeListener(event -> {
                final AppLayoutMenuItem selectedTab = (AppLayoutMenuItem) tabs
                    .getSelectedTab();

                if (selectedTab != null) {
                    if (selectedTab.getRoute() == null) {
                        // If there is no route associated, set previous tab as selected
                        if (selectedMenuItem != null) {
                            tabs.setSelectedTab(selectedMenuItem);
                        } else {
                            tabs.setSelectedIndex(-1);
                        }
                    } else {
                        // Update selected tab if it is associated with a route.
                        selectedMenuItem = selectedTab;
                    }
                    selectedTab.fireMenuItemClickEvent();
                }
            });
        });
    }

    /**
     * Clears existing menu items and sets the new the arguments.
     *
     * @param menuItems items to set
     */
    void setMenuItems(AppLayoutMenuItem... menuItems) {
        clearMenuItems();
        tabs.add(menuItems);
    }

    /**
     * Adds menu item to the menu
     *
     * @param menuItem Menu Item to add
     */
    void addMenuItem(AppLayoutMenuItem menuItem) {
        tabs.add(menuItem);
    }

    /**
     * Removes menu item from the menu
     */
    void removeMenuItem(AppLayoutMenuItem menuItem) {
        if (Objects.equals(this.selectedMenuItem, menuItem)) {
            this.selectedMenuItem = null;
        }
        tabs.remove(menuItem);
    }

    /**
     * Removes all menu items.
     */
    public void clearMenuItems() {
        selectedMenuItem = null;
        tabs.removeAll();
    }

    /**
     * Selects a menu item.
     */
    void selectMenuItem(AppLayoutMenuItem menuItem) {
        selectedMenuItem = menuItem;
        tabs.setSelectedTab(menuItem);
    }

    Optional<AppLayoutMenuItem> getMenuItemTargetingRoute(String route) {
        Objects.requireNonNull(route, "Route can not be null");
        return tabs.getChildren().map(AppLayoutMenuItem.class::cast)
            .filter(e -> route.equals(e.getRoute())).findFirst();
    }

    AppLayoutMenuItem getSelectedMenuItem() {
        return selectedMenuItem;
    }

    @Override
    public Element getElement() {
        return tabs.getElement();
    }
}
