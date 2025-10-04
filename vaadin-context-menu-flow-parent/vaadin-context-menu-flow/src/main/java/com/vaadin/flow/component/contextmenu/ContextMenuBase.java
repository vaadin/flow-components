/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.component.shared.internal.OverlayAutoAddController;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.shared.Registration;

import tools.jackson.databind.node.ObjectNode;

/**
 * Base functionality for server-side components based on
 * {@code <vaadin-context-menu>}. Classes extending this should provide API for
 * adding items and handling events related to them. For basic example, see
 * {@link ContextMenu}.
 *
 * @param <C>
 *            the context-menu type
 * @param <I>
 *            the menu-item type
 * @param <S>
 *            the sub menu type
 *
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
@Tag("vaadin-context-menu")
@NpmPackage(value = "@vaadin/context-menu", version = "25.0.0-alpha20")
@JsModule("@vaadin/context-menu/src/vaadin-context-menu.js")
@JsModule("./flow-component-renderer.js")
@JsModule("./contextMenuConnector.js")
@JsModule("./contextMenuTargetConnector.js")
public abstract class ContextMenuBase<C extends ContextMenuBase<C, I, S>, I extends MenuItemBase<C, I, S>, S extends SubMenuBase<C, I, S>>
        extends Component implements HasStyle {

    public static final String EVENT_DETAIL = "event.detail";

    private Component target;
    private MenuManager<C, I, S> menuManager;
    private MenuItemsArrayGenerator<I> menuItemsArrayGenerator;

    private String openOnEventName = "vaadin-contextmenu";
    private Registration targetBeforeOpenRegistration;
    private Registration targetAttachRegistration;
    private Registration targetDetachRegistration;
    private PendingJavaScriptResult targetJsRegistration;

    private OverlayAutoAddController<ContextMenuBase<C, I, S>> overlayAutoAddController;

    /**
     * Creates an empty context menu.
     */
    public ContextMenuBase() {
        // Workaround for: https://github.com/vaadin/flow/issues/3496
        getElement().setProperty("opened", false);

        // Don't open the overlay immediately with any event, let
        // contextMenuConnector.js make a server round-trip first.
        getElement().setProperty("openOn", "none");

        getElement().addPropertyChangeListener("opened", event -> {
            fireEvent(new OpenedChangeEvent<>((C) this,
                    event.isUserOriginated()));
        });

        menuItemsArrayGenerator = new MenuItemsArrayGenerator<>(this);
        addAttachListener(event -> {
            String appId = event.getUI().getInternals().getAppId();
            initConnector(appId);
            resetContent();
            updateListenOn();
        });

        overlayAutoAddController = new OverlayAutoAddController<>(this,
                () -> ModalityMode.MODELESS);
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
            targetAttachRegistration.remove();
            targetDetachRegistration.remove();
            getTarget().getElement().executeJs(
                    "if (this.$contextMenuTargetConnector) { this.$contextMenuTargetConnector.removeConnector() }");
            cancelTargetJavascriptExecution();
        }

        this.target = target;
        updateListenOn();

        if (target == null) {
            return;
        }

        // Target's JavaScript needs to be executed on each attach,
        // because Flow creates a new client-side element
        target.getUI().ifPresent(this::onTargetAttach);
        targetAttachRegistration = target
                .addAttachListener(e -> onTargetAttach(e.getUI()));
        targetDetachRegistration = target
                .addDetachListener(e -> cancelTargetJavascriptExecution());

        // Server round-trip before opening the overlay
        targetBeforeOpenRegistration = target.getElement()
                .addEventListener("vaadin-context-menu-before-open",
                        this::beforeOpenHandler)
                .addEventData(EVENT_DETAIL);
    }

    private void updateListenOn() {
        var target = getTarget();
        getElement().executeJs("this.listenOn=$0;",
                target == null ? this : target);
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
        requestTargetJsExecutions();
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
    public void close() {
        // See https://github.com/vaadin/flow-components/issues/6449
        getElement().setProperty("opened", false);
        getElement().callJsFunction("close");
    }

    /**
     * Creates a new menu item with the given text content and adds it to the
     * context menu.
     *
     * @param text
     *            the text content for the created menu item
     * @return the created menu item
     */
    public I addItem(String text) {
        return getMenuManager().addItem(text);
    }

    /**
     * Creates a new menu item with the given component content and adds it to
     * the context menu.
     *
     * @param component
     *            the component to add to the created menu item
     * @return the created menu item
     */
    public I addItem(Component component) {
        return getMenuManager().addItem(component);
    }

    /**
     * Adds the given components to the context menu.
     * <p>
     * For the common use case of having a list of high-lightable items inside
     * the menu, use {@link #addItem(String)} and its overload methods instead.
     *
     * @param components
     *            the components to add
     * @see HasMenuItems#addItem(String, ComponentEventListener)
     * @see HasMenuItems#addItem(Component, ComponentEventListener)
     */
    public void addComponent(Component... components) {
        getMenuManager().addComponent(components);
    }

    /**
     * Adds the given components to the context menu.
     * <p>
     * For the common use case of having a list of high-lightable items inside
     * the menu, use {@link #addItem(String)} and its overload methods instead.
     *
     * @param components
     *            the components to add
     * @see HasMenuItems#addItem(String, ComponentEventListener)
     * @see HasMenuItems#addItem(Component, ComponentEventListener)
     */
    public void addComponent(Collection<Component> components) {
        if (components == null) {
            return;
        }
        getMenuManager().addComponent(components.toArray(Component[]::new));
    }

    /**
     * Removes the given components from the context menu.
     *
     * @param components
     *            the components to remove
     */
    public void remove(Component... components) {
        getMenuManager().remove(components);
    }

    /**
     * Removes all the child components. This also removes all the items added
     * with {@link #addItem(String)} and its overload methods.
     */
    public void removeAll() {
        getMenuManager().removeAll();
    }

    /**
     * Adds the given component to the context menu at the given index.
     * <p>
     * For the common use case of having a list of high-lightable items inside
     * the menu, use {@link #addItem(String)} and its overload methods instead.
     *
     * @param index
     *            the index, where the component will be added
     * @param component
     *            the component to add
     */
    public void addComponentAtIndex(int index, Component component) {
        getMenuManager().addComponentAtIndex(index, component);
    }

    /**
     * Adds the given component as the first child to the context menu.
     * <p>
     * For the common use case of having a list of high-lightable items inside
     * the menu, use {@link #addItem(String)} and its overload methods instead.
     *
     * @param component
     *            the component to add
     */
    public void addComponentAsFirst(Component component) {
        getMenuManager().addComponentAtIndex(0, component);
    }

    /**
     * Adds a separator between items.
     */
    public void addSeparator() {
        getMenuManager().addSeparator();
    }

    /**
     * Gets the child components of this component. This includes components
     * added with {@link #addComponent(Component...)} and the {@link MenuItem}
     * components created with {@link #addItem(String)} and its overload
     * methods. This doesn't include the components added to the sub menus of
     * this context menu.
     *
     * @return the child components of this component
     */
    @Override
    public Stream<Component> getChildren() {
        return getMenuManager().getChildren();
    }

    /**
     * Gets the items added to this component (the children of this component
     * that are instances of {@link MenuItem}). This doesn't include the
     * components added to the sub menus of this context menu.
     *
     * @return the {@link MenuItem} components in this context menu
     * @see #addItem(String)
     */
    public List<I> getItems() {
        return getMenuManager().getItems();
    }

    /**
     * Gets the open state from the context menu.
     * <p>
     * This property is synchronized automatically from client side when an
     * {@code opened-changed} event happens.
     *
     * @return the {@code opened} property from the context menu
     */
    @Synchronize(property = "opened", value = "opened-changed")
    public boolean isOpened() {
        return getElement().getProperty("opened", false);
    }

    /**
     * {@code opened-changed} event is sent when the opened state changes.
     */
    public static class OpenedChangeEvent<TComponent extends ContextMenuBase<TComponent, ?, ?>>
            extends ComponentEvent<TComponent> {
        private final boolean opened;

        public OpenedChangeEvent(TComponent source, boolean fromClient) {
            super(source, fromClient);
            this.opened = source.isOpened();
        }

        public boolean isOpened() {
            return opened;
        }
    }

    /**
     * Adds a listener for the {@code opened-changed} events fired by the web
     * component.
     *
     * @param listener
     *            the listener to add
     * @return a Registration for removing the event listener
     */
    public Registration addOpenedChangeListener(
            ComponentEventListener<OpenedChangeEvent<C>> listener) {
        return addListener(OpenedChangeEvent.class,
                (ComponentEventListener) listener);
    }

    /**
     * Gets the menu manager.
     *
     * @return the menu manager
     */
    protected MenuManager<C, I, S> getMenuManager() {
        if (menuManager == null) {
            menuManager = createMenuManager(this::resetContent);
        }
        return menuManager;
    }

    /**
     * Creates a menu manager instance which contains logic to control the menu
     * content.
     *
     * @param contentReset
     *            callback to reset the menu content
     * @return a new menu manager instance
     */
    protected abstract MenuManager<C, I, S> createMenuManager(
            SerializableRunnable contentReset);

    /**
     * Decides whether to open the menu when the
     * {@link ContextMenuBase#beforeOpenHandler(DomEvent)} is processed,
     * sub-classes can easily override it and perform additional operations in
     * this phase.
     * <p>
     * The event details are completely specified by the target component that
     * is in charge of defining the data it sends to the server. Based on this
     * information, this method enables for dynamically modifying the contents
     * of the context menu. Furthermore, this method's return value specifies if
     * the context menu will be opened.
     * </p>
     *
     * @param eventDetail
     *            the client side event details provided by the target
     *            component.
     *
     * @return {@code true} if the context menu should be opened, {@code false}
     *         otherwise.
     */
    protected boolean onBeforeOpenMenu(ObjectNode eventDetail) {
        return true;
    }

    private void resetContent() {
        menuItemsArrayGenerator.generate();
    }

    private void onTargetAttach(UI ui) {
        ui.getInternals().addComponentDependencies(ContextMenu.class);
        requestTargetJsExecutions();
        updateListenOn();
    }

    /*
     * Used for both initializing the client-side connector and to update the
     * openOn-event. This ensures that openOn is never updated before the
     * connector is initialized.
     */
    private void requestTargetJsExecutions() {
        if (target != null) {
            cancelTargetJavascriptExecution();
            targetJsRegistration = target.getElement().executeJs(
                    "window.Vaadin.Flow.contextMenuTargetConnector.init(this);"
                            + "this.$contextMenuTargetConnector.updateOpenOn($0);",
                    openOnEventName);
        }
    }

    private void cancelTargetJavascriptExecution() {
        if (targetJsRegistration != null
                && !targetJsRegistration.isSentToBrowser()) {
            targetJsRegistration.cancelExecution();
        }
        targetJsRegistration = null;
    }

    private void beforeOpenHandler(DomEvent event) {
        ObjectNode eventDetail = (ObjectNode) event.getEventData()
                .get(EVENT_DETAIL);

        boolean shouldOpenMenu = onBeforeOpenMenu(eventDetail);

        if (shouldOpenMenu) {
            overlayAutoAddController.add();
            target.getElement().callJsFunction(
                    "$contextMenuTargetConnector.openMenu", getElement());
        }
    }

    private void initConnector(String appId) {
        getElement().executeJs(
                "window.Vaadin.Flow.contextMenuConnector.initLazy(this, $0)",
                appId);
    }
}
