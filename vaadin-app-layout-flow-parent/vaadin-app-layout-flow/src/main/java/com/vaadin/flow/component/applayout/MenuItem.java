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
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.shared.Registration;

/**
 * Base class representing menu items.
 */
public abstract class MenuItem extends Tab {

    private Component icon;
    private String title;

    private ComponentEventListener<MenuItemClickEvent> listener;
    private Registration listenerRegistration;

    /**
     * Constructs a new object with the given title.
     *
     * @param title
     *            the title to display
     */
    public MenuItem(String  title) {
        this(null, title);
    }

    /**
     * Constructs a new object with the given icon.
     *
     * @param icon
     *            the icon to display
     */
    public MenuItem(Component icon) {
        this(icon, null);
    }

    /**
     * Constructs a new object with the given icon and title.
     *
     * @param icon
     *            the icon to display
     * @param title
     *            the title to display
     */
    public MenuItem(Component icon, String title) {
        this(icon, title, null);
    }

    /**
     * Constructs a new object with the given icon, title and click listener.
     *
     * @param icon
     *            the icon to display
     * @param title
     *            the title to display
     * @param listener
     *            the menu item click listener
     */
    protected MenuItem(Component icon,
                       String title,
                       ComponentEventListener<MenuItemClickEvent> listener) {
        setIcon(icon);
        setTitle(title);
        setListener(listener);
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
     * @param icon
     *            Icon to display in MenuItem
     */
    public void setIcon(Component icon) {
        removeAll();

        if (icon != null) {
            icon.getElement().setAttribute("role", "img");
            add(icon);
        }

        if (title != null) {
            add(new Span(title));
        }

        this.icon = icon;
    }

    /**
     * Returns displayed MenuItem title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets MenuItem title
     *
     * @param title
     *            the displayed title of MenuItem
     */
    public void setTitle(String title) {
        removeAll();

        if (icon != null) {
            add(icon);
        }

        if (title != null) {
            add(new Span(title));
            getElement().setAttribute("title", title);
        } else {
            getElement().removeAttribute("title");
        }

        this.title = title;
    }

    protected ComponentEventListener<MenuItemClickEvent> getListener() {
        return listener;
    }

    protected void setListener(ComponentEventListener<MenuItemClickEvent> listener) {
        if (listener == null) {
            listener = event -> {};
        }

        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        listenerRegistration = ComponentUtil.addListener(
                this, MenuItemClickEvent.class, listener);
        this.listener = listener;
    }
}
