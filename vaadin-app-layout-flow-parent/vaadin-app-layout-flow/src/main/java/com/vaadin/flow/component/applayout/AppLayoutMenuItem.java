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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.shared.Registration;

/**
 * Base class representing menu items.
 */
public class AppLayoutMenuItem extends Tab {

    private Component icon;
    private String title;
    private String route;

    {
        addMenuItemClickListener(event -> {
            if (getRoute() != null) {
                getUI().ifPresent(ui -> ui.navigate(route));
            }
        });
    }

    /**
     * Constructs a new object with the given title.
     *
     * @param title the title to display
     */
    public AppLayoutMenuItem(String title) {
        this((Component) null, title);
    }

    /**
     * Constructs a new object with the given icon.
     *
     * @param icon the icon to display
     */
    public AppLayoutMenuItem(Component icon) {
        this(icon, (String) null);
    }

    /**
     * Constructs a new object with the given icon and title.
     *
     * @param icon  the icon to display
     * @param title the title to display
     */
    public AppLayoutMenuItem(Component icon, String title) {
        updateTitleAndIcon(icon, title);
    }

    /**
     * Constructs a new object with the given title and route.
     *
     * @param title the title to display
     * @param route The route to navigate on click
     */
    public AppLayoutMenuItem(String title, String route) {
        this(null, title, route);
    }

    /**
     * Constructs a new object with the given icon, title and route.
     *
     * @param icon  the icon to display
     * @param title the title to display
     * @param route the route to navigate on click
     */
    public AppLayoutMenuItem(Component icon, String title, String route) {
        this(icon, title);
        setRoute(route);
    }

    /**
     * Constructs a new object with the given icon and click listener.
     *
     * @param icon     the icon to display
     * @param listener the menu item click listener
     */
    public AppLayoutMenuItem(Component icon,
        ComponentEventListener<MenuItemClickEvent> listener) {
        this(icon, null, listener);
    }

    /**
     * Constructs a new object with the given title and click listener.
     *
     * @param title    the title to display
     * @param listener the menu item click listener
     */
    public AppLayoutMenuItem(String title,
        ComponentEventListener<MenuItemClickEvent> listener) {
        this(null, title, listener);
    }

    /**
     * Constructs a new object with the given icon, title and click listener.
     *
     * @param icon     the icon to display
     * @param title    the title to display
     * @param listener the menu item click listener
     */
    public AppLayoutMenuItem(Component icon, String title,
        ComponentEventListener<MenuItemClickEvent> listener) {
        this(icon, title);
        addMenuItemClickListener(listener);
    }

    /**
     * Returns icon
     */
    public Component getIcon() {
        return icon;
    }

    /**
     * Sets icon
     *
     * @param icon Icon to display in AppLayoutMenuItem
     */
    public void setIcon(Component icon) {
        updateTitleAndIcon(icon, this.title);
    }

    /**
     * Returns displayed AppLayoutMenuItem title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets AppLayoutMenuItem title
     *
     * @param title the displayed title of AppLayoutMenuItem
     */
    public void setTitle(String title) {
        updateTitleAndIcon(this.icon, title);
    }

    private void updateTitleAndIcon(Component icon, String title) {
        removeAll();
        if (icon != null) {
            icon.getElement().setAttribute("role", "img");
            add(icon);
        }
        if (title != null) {
            getElement().setAttribute("title", title);
            add(new Span(title));
        } else {
            getElement().removeAttribute("title");
        }
        this.icon = icon;
        this.title = title;
    }

    /**
     * Sets the route to be navigated to when this menu item is selected.
     *
     * @param route Route to be navigated to
     */
    public void setRoute(String route) {
        this.route = route;
    }

    /**
     * @return Route associated with this menu item.
     */
    public String getRoute() {
        return route;
    }

    /**
     * @param listener listener to called when the menu item is clicked.
     * @return {@link Registration}
     */
    public Registration addMenuItemClickListener(
        ComponentEventListener<MenuItemClickEvent> listener) {
        return addListener(MenuItemClickEvent.class, listener);
    }

    void fireMenuItemClickEvent() {
        fireEvent(new MenuItemClickEvent(this, false));
    }
}
