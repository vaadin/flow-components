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

    public MenuItem(String  title) {
        this(null, title);
    }

    public MenuItem(Component icon) {
        this(icon, null);
    }

    public MenuItem(Component icon, String title) {
        this(icon, title, null);
    }

    protected MenuItem(Component icon,
                       String title,
                       ComponentEventListener<MenuItemClickEvent> listener) {
        setIcon(icon);
        setTitle(title);
        setListener(listener);
    }

    public Component getIcon() {
        return icon;
    }

    public void setIcon(Component icon) {
        removeAll();

        if (icon != null) {
            icon.getElement().setAttribute("role", "icon");
            add(icon);
        }

        if (title != null) {
            add(new Span(title));
        }

        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

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
