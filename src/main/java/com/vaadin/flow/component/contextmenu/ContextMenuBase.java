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
package com.vaadin.flow.component.contextmenu;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;

/**
 * Base functionality for server-side components based on
 * {@code <vaadin-context-menu>}. Classes extending this should provide API for
 * adding items and handling events related to them. For basic example, see
 * {@link ContextMenu}.
 * 
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
@HtmlImport("flow-component-renderer.html")
@HtmlImport("frontend://bower_components/vaadin-list-box/src/vaadin-list-box.html")
@JavaScript("frontend://contextMenuConnector.js")
public class ContextMenuBase<C extends ContextMenuBase<C>>
        extends GeneratedVaadinContextMenu<C>
        implements HasComponents {

    private Component target;

    private Element template;
    private Element container;

    private String openOnEventName = "vaadin-contextmenu";
    private Registration targetBeforeOpenRegistration;

    private boolean autoAddedToTheUi;

    /**
     * Creates an empty context menu.
     */
    public ContextMenuBase() {
        template = new Element("template");
        getElement().appendChild(template);

        container = new Element("vaadin-list-box");
        getElement().appendVirtualChild(container);

        getElement().getNode()
                .runWhenAttached(ui -> ui.beforeClientResponse(this,
                        context -> attachComponentRenderer()));

        // Workaround for: https://github.com/vaadin/flow/issues/3496
        getElement().setProperty("opened", false);

        // Don't open the overlay immediately with any event, let
        // contextMenuConnector.js make a server round-trip first.
        setOpenOn("none");

        getElement().addEventListener("opened-changed", event -> {
            if (autoAddedToTheUi && !isOpened()) {
                getElement().removeFromParent();
                autoAddedToTheUi = false;
            }
        });
    }

    /**
     * Sets the target component for this context menu.
     * <p>
     * By default, the context menu can be opened with a right click or a long
     * touch on the target component.
     * 
     * @param target
     *            the target component for this context menu, can be
     *            {@code null} to remove the target
     */
    public void setTarget(Component target) {
        if (getTarget() != null) {
            targetBeforeOpenRegistration.remove();
            getTarget().getElement()
                    .callFunction("$contextMenuConnector.removeConnector");
        }

        this.target = target;
        getElement().getNode().runWhenAttached(
                ui -> ui.beforeClientResponse(this, context -> ui.getPage()
                        .executeJavaScript("$0.listenOn=$1", this, target)));

        if (target == null) {
            return;
        }

        target.getElement().getNode().runWhenAttached(
                ui -> ui.beforeClientResponse(target, context -> {
                    ui.getInternals()
                            .addComponentDependencies(ContextMenuBase.class);
                    ui.getPage().executeJavaScript(
                            "window.Vaadin.Flow.contextMenuConnector.init($0)",
                            target.getElement());
                }));

        updateOpenOn();

        // Server round-trip before opening the overlay
        targetBeforeOpenRegistration = target.getElement()
                .addEventListener("vaadin-context-menu-before-open", event -> {
                    beforeOpen();
                    target.getElement().callFunction(
                            "$contextMenuConnector.openMenu", getElement());
                });
    }

    private void updateOpenOn() {
        if (target != null) {
            target.getElement().callFunction(
                    "$contextMenuConnector.updateOpenOn", openOnEventName);
        }
    }

    private void beforeOpen() {
        if (getElement().getNode().getParent() == null) {
            UI ui = getCurrentUI();
            ui.beforeClientResponse(ui, context -> {
                ui.add(this);
                autoAddedToTheUi = true;
            });
        }
    }

    private UI getCurrentUI() {
        UI ui = UI.getCurrent();
        if (ui == null) {
            throw new IllegalStateException("UI instance is not available. "
                    + "It means that you are calling this method "
                    + "out of a normal workflow where it's always implicitly set. "
                    + "That may happen if you call the method from the custom thread without "
                    + "'UI::access' or from tests without proper initialization.");
        }
        return ui;
    }

    /**
     * Gets the target component of this context menu, or {@code null} if it
     * doesn't have a target.
     * 
     * @return the target component of this context menu
     * @see #setTarget(Component)
     */
    public Component getTarget() {
        return target;
    }

    /**
     * Determines the way for opening the context menu.
     * <p>
     * By default, the context menu can be opened with a right click or a long
     * touch on the target component.
     * 
     * @param openOnClick
     *            if {@code true}, the context menu can be opened with left
     *            click only. Otherwise the context menu follows the default
     *            behavior.
     */
    public void setOpenOnClick(boolean openOnClick) {
        openOnEventName = openOnClick ? "click" : "vaadin-contextmenu";
        updateOpenOn();
    }

    /**
     * Gets whether the context menu can be opened via left click.
     * <p>
     * By default, this will return {@code false} and context menu can be opened
     * with a right click or a long touch on the target component.
     * 
     * @return {@code true} if the context menu can be opened with left click
     *         only. Otherwise the context menu follows the default behavior.
     */
    public boolean isOpenOnClick() {
        return "click".equals(openOnEventName);
    }

    /**
     * Closes this context menu if it is currently open.
     */
    @Override
    public void close() {
        super.close();
    }

    /**
     * Adds the given components into the context menu overlay.
     * <p>
     * For the common use case of having a list of high-lightable items inside
     * the overlay, you can use the
     * {@link #addItem(Component, ComponentEventListener)} convenience methods
     * instead.
     * <p>
     * The added elements in the DOM will not be children of the
     * {@code <vaadin-context-menu>} element, but will be inserted into an
     * overlay that is attached into the {@code <body>}.
     *
     * @param components
     *            the components to add
     * @see #addItem(String, ComponentEventListener)
     * @see #addItem(Component, ComponentEventListener)
     */
    @Override
    public void add(Component... components) {
        Objects.requireNonNull(components, "Components to add cannot be null");
        for (Component component : components) {
            Objects.requireNonNull(component,
                    "Component to add cannot be null");
            container.appendChild(component.getElement());
        }
    }

    @Override
    public void remove(Component... components) {
        Objects.requireNonNull(components,
                "Components to remove cannot be null");
        for (Component component : components) {
            Objects.requireNonNull(component,
                    "Component to remove cannot be null");
            if (container.equals(component.getElement().getParent())) {
                container.removeChild(component.getElement());
            } else {
                throw new IllegalArgumentException("The given component ("
                        + component + ") is not a child of this component");
            }
        }
    }

    /**
     * {@inheritDoc} This also removes all the items added with
     * {@link #addItem(String)} and its overload methods.
     */
    @Override
    public void removeAll() {
        container.removeAllChildren();
    }

    /**
     * Adds the given component into this context menu at the given index.
     * <p>
     * The added elements in the DOM will not be children of the
     * {@code <vaadin-context-menu>} element, but will be inserted into an
     * overlay that is attached into the {@code <body>}.
     * 
     * @param index
     *            the index, where the component will be added.
     * @param component
     *            the component to add
     */
    @Override
    public void addComponentAtIndex(int index, Component component) {
        Objects.requireNonNull(component, "Component should not be null");
        int indexCheck;
        if (index < 0) {
            indexCheck = 0;
        } else if (index > container.getChildCount()) {
            indexCheck = container.getChildCount();
        } else {
            indexCheck = index;
        }
        container.insertChild(indexCheck, component.getElement());
    }

    /**
     * Gets the child components of this component. This includes components
     * added with {@link #add(Component...)} and the {@link MenuItem} components
     * created with {@link #addItem(String)} and its overload methods.
     *
     * @return the child components of this component
     */
    @Override
    public Stream<Component> getChildren() {
        Builder<Component> childComponents = Stream.builder();
        container.getChildren().forEach(childElement -> ComponentUtil
                .findComponents(childElement, childComponents::add));
        return childComponents.build();
    }

    /**
     * Gets the items added to this component (the children of this component
     * that are instances of {@link MenuItem}).
     * 
     * @return the {@link MenuItem} components in this context menu
     * @see #addItem(String, ComponentEventListener)
     */
    public List<MenuItem> getItems() {
        return getChildren().filter(MenuItem.class::isInstance)
                .map(child -> (MenuItem) child).collect(Collectors.toList());
    }

    /**
     * Gets the open state from the context menu.
     *
     * @return the {@code opened} property from the context menu
     */
    public boolean isOpened() {
        return super.isOpenedBoolean();
    }

    /**
     * Creates and adds a new item component to this context menu with the given
     * text content.
     * 
     * @param text
     *            the text content for the created menu item
     * @return the created menu item
     */
    protected MenuItem addItem(String text) {
        MenuItem menuItem = new MenuItem(this);
        add(menuItem);
        menuItem.setText(text);
        return menuItem;
    }

    /**
     * Creates and adds a new item component to this context menu with the given
     * component inside.
     * 
     * @param component
     *            the component to add to the created menu item
     * @return the created menu item
     */
    protected MenuItem addItem(Component component) {
        MenuItem menuItem = new MenuItem(this);
        add(menuItem);
        menuItem.add(component);
        return menuItem;
    }

    /**
     * Adds a listener for the {@code opened-changed} events fired by the web
     * component.
     *
     * @param listener
     *            the listener to add
     * @return a Registration for removing the event listener
     */
    @Override
    public Registration addOpenedChangeListener(
            ComponentEventListener<OpenedChangeEvent<C>> listener) {
        return super.addOpenedChangeListener(listener);
    }

    private void attachComponentRenderer() {
        String appId = UI.getCurrent().getInternals().getAppId();
        int nodeId = container.getNode().getId();
        String renderer = String.format(
                "<flow-component-renderer appid=\"%s\" nodeid=\"%s\"></flow-component-renderer>",
                appId, nodeId);
        template.setProperty("innerHTML", renderer);
    }

}
