/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.contextmenu;

import java.io.Serializable;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;

/**
 * A common interface for components that can have {@link MenuItem}s with click
 * listeners inside them.
 *
 * @see ContextMenu
 * @see SubMenu
 *
 * @author Vaadin Ltd.
 */
public interface HasMenuItems extends Serializable {

    /**
     * Adds a new item component with the given text content and click listener
     * to the context menu overlay.
     * <p>
     * This is a convenience method for the use case where you have a list of
     * highlightable {@link MenuItem}s inside the overlay. If you want to
     * configure the contents of the overlay without wrapping them inside
     * {@link MenuItem}s, or if you just want to add some non-highlightable
     * components between the items, use the
     * {@link ContextMenu#add(Component...)} method.
     *
     * @param text
     *            the text content for the new item
     * @param clickListener
     *            the handler for clicking the new item, can be {@code null} to
     *            not add listener
     * @return the added {@link MenuItem} component
     * @see #addItem(Component, ComponentEventListener)
     * @see ContextMenu#add(Component...)
     * @see SubMenu#add(Component...)
     */
    MenuItem addItem(String text,
            ComponentEventListener<ClickEvent<MenuItem>> clickListener);

    /**
     * Adds a new item component with the given component and click listener to
     * the context menu overlay.
     * <p>
     * This is a convenience method for the use case where you have a list of
     * highlightable {@link MenuItem}s inside the overlay. If you want to
     * configure the contents of the overlay without wrapping them inside
     * {@link MenuItem}s, or if you just want to add some non-highlightable
     * components between the items, use the
     * {@link ContextMenu#add(Component...)} method.
     *
     * @param component
     *            the component inside the new item
     * @param clickListener
     *            the handler for clicking the new item, can be {@code null} to
     *            not add listener
     * @return the added {@link MenuItem} component
     * @see #addItem(String, ComponentEventListener)
     * @see ContextMenu#add(Component...)
     * @see SubMenu#add(Component...)
     */
    MenuItem addItem(Component component,
            ComponentEventListener<ClickEvent<MenuItem>> clickListener);

}
