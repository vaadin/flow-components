/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.dialog;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.dom.Element;

/**
 * Server-side component for the {@code <vaadin-dialog>} element.
 * 
 * @author Vaadin Ltd
 */
@HtmlImport("frontend://flow-component-renderer.html")
public class Dialog extends GeneratedVaadinDialog<Dialog>
        implements HasComponents {

    private Element container;

    /**
     * Creates an empty dialog.
     */
    public Dialog() {
        container = new Element("div", false);
        getElement().appendVirtualChild(container);

        // Attach <flow-component-renderer>
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, () -> attachComponentRenderer()));
    }

    /**
     * Creates a dialog with given components inside.
     * 
     * @param components
     *            the components inside the dialog
     * @see #add(Component...)
     */
    public Dialog(Component... components) {
        this();
        add(components);
    }

    /**
     * Adds the given components into this dialog.
     * <p>
     * The elements in the DOM will not be children of the
     * {@code <vaadin-dialog>} element, but will be inserted into an overlay
     * that is attached into the {@code <body>}.
     *
     * @param components
     *            the components to add
     */
    @Override
    public void add(Component... components) {
        assert components != null;
        for (Component component : components) {
            assert component != null;
            container.appendChild(component.getElement());
        }
    }

    @Override
    public void remove(Component... components) {
        for (Component component : components) {
            assert component != null;
            if (container.equals(component.getElement().getParent())) {
                container.removeChild(component.getElement());
            } else {
                throw new IllegalArgumentException("The given component ("
                        + component + ") is not a child of this component");
            }
        }
    }

    @Override
    public void removeAll() {
        container.removeAllChildren();
    }

    /**
     * Gets whether this dialog can be closed by hitting the esc-key or not.
     * <p>
     * By default, the dialog is closable with esc.
     * 
     * @return {@code true} if this dialog can be closed with the esc-key,
     *         {@code false} otherwise
     */
    public boolean isCloseOnEsc() {
        return !getElement().getProperty("noCloseOnEsc", false);
    }

    /**
     * Sets whether this dialog can be closed by hitting the esc-key or not.
     * <p>
     * By default, the dialog is closable with esc.
     * 
     * @param closeOnEsc
     *            {@code true} to enable closing this dialog with the esc-key,
     *            {@code false} to disable it
     */
    public void setCloseOnEsc(boolean closeOnEsc) {
        getElement().setProperty("noCloseOnEsc", !closeOnEsc);
    }

    /**
     * Gets whether this dialog can be closed by clicking outside of it or not.
     * <p>
     * By default, the dialog is closable with an outside click.
     * 
     * @return {@code true} if this dialog can be closed by an outside click,
     *         {@code false} otherwise
     */
    public boolean isCloseOnOutsideClick() {
        return !getElement().getProperty("noCloseOnOutsideClick", false);
    }

    /**
     * Sets whether this dialog can be closed by clicking outside of it or not.
     * <p>
     * By default, the dialog is closable with an outside click.
     * 
     * @param closeOnOutsideClick
     *            {@code true} to enable closing this dialog with an outside
     *            click, {@code false} to disable it
     */
    public void setCloseOnOutsideClick(boolean closeOnOutsideClick) {
        getElement().setProperty("noCloseOnOutsideClick", !closeOnOutsideClick);
    }

    /**
     * Opens the dialog.
     * <p>
     * Note: You don't need to add the dialog component anywhere before opening
     * it. Since {@code <vaadin-dialog>}'s location in the DOM doesn't really
     * matter, opening a dialog will automatically add it to the {@code <body>}
     * if it's not yet attached anywhere.
     */
    public void open() {
        setOpened(true);
    }

    /**
     * Closes the dialog.
     */
    public void close() {
        setOpened(false);
    }

    /**
     * Opens or closes the dialog.
     * <p>
     * Note: You don't need to add the dialog component anywhere before opening
     * it. Since {@code <vaadin-dialog>}'s location in the DOM doesn't really
     * matter, opening a dialog will automatically add it to the {@code <body>}
     * if it's not yet attached anywhere.
     * 
     * @param opened
     *            {@code true} to open the dialog, {@code false} to close it
     */
    @Override
    public void setOpened(boolean opened) {
        if (opened && getElement().getNode().getParent() == null
                && UI.getCurrent() != null) {
            UI.getCurrent().add(this);
        }
        super.setOpened(opened);
    }

    private void attachComponentRenderer() {
        String appId = UI.getCurrent().getInternals().getAppId();
        int nodeId = container.getNode().getId();
        String template = "<template><flow-component-renderer appid=" + appId
                + " nodeid=" + nodeId
                + "></flow-component-renderer></template>";
        getElement().setProperty("innerHTML", template);
    }

}

