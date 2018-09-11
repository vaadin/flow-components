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

import com.helger.commons.annotation.VisibleForTesting;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.dom.Element;

import java.util.Objects;
import java.util.Optional;

@Tag("vaadin-app-layout")
@HtmlImport("frontend://bower_components/vaadin-app-layout/src/vaadin-app-layout.html")
public class AppLayout extends Component {

    private Element branding;
    private Element content;

    private MenuItem selectedMenuItem;
    private final Tabs menuTabs;

    /**
     * Initializes a new app layout with a default menu.
     */
    public AppLayout() {
        menuTabs = new Tabs();
        menuTabs.getElement().setAttribute("slot", "menu");
        menuTabs.getElement().setAttribute("theme", "minimal");
        getElement().appendChild(menuTabs.getElement());

        menuTabs.addSelectedChangeListener(event -> {
            final MenuItem selectedTab = (MenuItem) menuTabs.getSelectedTab();

            if (selectedTab instanceof ActionMenuItem) {
                // Do not set actions (such as logout) as selected.
                menuTabs.getChildren()
                        .map(MenuItem.class::cast)
                        .filter(e -> e == selectedMenuItem)
                        .findFirst()
                        .ifPresent(this::selectMenuItem);
            } else {
                selectedMenuItem = selectedTab;
            }

            selectedTab.getListener().onComponentEvent(
                    new MenuItemClickEvent(selectedTab, event.isFromClient()));
        });
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        try {
            selectedMenuItem = (MenuItem) menuTabs.getSelectedTab();
        } catch (IllegalArgumentException noMenuItemPresent) { }
    }

    public void setBranding(Element branding) {
        Objects.requireNonNull(branding, "Branding cannot be null");

        removeBranding();

        this.branding = branding;
        branding.setAttribute("slot", "branding");

        getElement().appendChild(branding);
    }

    public void removeBranding() {
        if (this.branding == null) {
            return;
        }

        getElement().removeChild(this.branding);

        this.branding = null;
    }

    /**
     * Clears existing menu items and sets the new the arguments.
     * @param menuItems
     */
    public void setMenuItems(MenuItem... menuItems) {
        menuTabs.removeAll();
        menuTabs.add(menuItems);
    }

    public void addMenuItem(MenuItem menuItem) {
        menuTabs.add(menuItem);
    }

    public void removeMenuItem(MenuItem menuItem) {
        menuTabs.remove(menuItem);
    }

    /**
     * Gets the first {@link RoutingMenuItem} targeting a route.
     */
    Optional<MenuItem> getMenuItemTargetingRoute(String route) {
        return menuTabs.getChildren()
                .map(e -> (MenuItem) e)
                .filter(e -> e instanceof RoutingMenuItem)
                .filter(e -> ((RoutingMenuItem) e).getRoute().equals(route))
                .findFirst();
    }

    /**
     * Gets the currently selected menu item.
     */
    public MenuItem getSelectedMenuItem() {
        return selectedMenuItem;
    }

    /**
     * Selects a menu item.
     */
    public void selectMenuItem(MenuItem menuItem) {
        menuTabs.setSelectedTab(menuItem);
        selectedMenuItem = menuItem;
    }

    public Element getContent() {
        return content;
    }

    /**
     * Sets the displayed content.
     * @param content
     */
    public void setContent(Element content) {
        Objects.requireNonNull(content, "Content cannot be null");

        removeContent();

        this.content = content;
        getElement().appendChild(content);
    }

    /**
     * Removes the displayed content.
     */
    public void removeContent() {
        if (this.content != null) {
            this.content.removeFromParent();
        }

        this.content = null;
    }

    @VisibleForTesting
    Component getMenu() {
        return menuTabs;
    }
}
