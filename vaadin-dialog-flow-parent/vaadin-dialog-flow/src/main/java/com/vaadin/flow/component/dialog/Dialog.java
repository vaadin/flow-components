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

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementConstants;
import com.vaadin.flow.shared.Registration;

/**
 * Server-side component for the {@code <vaadin-dialog>} element.
 *
 * @author Vaadin Ltd
 */
@JsModule("./flow-component-renderer.js")
public class Dialog extends GeneratedVaadinDialog<Dialog>
        implements HasComponents, HasSize, HasTheme {

    private Element template;
    private Element container;
    private boolean autoAddedToTheUi;
    private int onCloseConfigured;
    private String width;
    private String minWidth;
    private String maxWidth;
    private String height;
    private String minHeight;
    private String maxHeight;

    /**
     * Creates an empty dialog.
     */
    public Dialog() {
        template = new Element("template");
        getElement().appendChild(template);

        container = new Element("div");
        container.getClassList().add("draggable");
        container.getClassList().add("draggable-leaf-only");
        container.getStyle().set(ElementConstants.STYLE_WIDTH, "100%");
        container.getStyle().set(ElementConstants.STYLE_HEIGHT, "100%");

        getElement().appendVirtualChild(container);

        // Attach <flow-component-renderer>. Needs to be updated on each
        // attach, as element depends on node id which is subject to change if
        // the dialog is transferred to another UI, e.g. due to
        // @PreserveOnRefresh
        getElement().getNode().addAttachListener(this::attachComponentRenderer);

        // Workaround for: https://github.com/vaadin/flow/issues/3496
        setOpened(false);

        getElement().addEventListener("opened-changed", event -> {
            if (autoAddedToTheUi && !isOpened()) {
                getElement().removeFromParent();
                autoAddedToTheUi = false;
            }
        });

        addListener(DialogResizeEvent.class, event -> {
            width = event.getWidth();
            height = event.getHeight();
        });
    }

    /**
     * `vaadin-dialog-close-action` is sent when the user clicks outside the
     * overlay or presses the escape key.
     */
    @DomEvent("vaadin-dialog-close-action")
    public static class DialogCloseActionEvent extends ComponentEvent<Dialog> {
        public DialogCloseActionEvent(Dialog source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    /**
     * `resize` event is sent when the user finishes resizing the overlay.
     */
    @DomEvent("resize")
    public static class DialogResizeEvent
            extends ComponentEvent<Dialog> {

        private final String width;
        private final String height;

        public DialogResizeEvent(Dialog source, boolean fromClient,
                @EventData("event.detail.width") String width,
                @EventData("event.detail.height") String height) {
            super(source, fromClient);
            this.width = width;
            this.height = height;
        }

        /**
         * Gets the width of the overlay after resize is done
         *
         * @return the width in pixels of the overlay
         */
        public String getWidth() {
            return width;
        }

        /**
         * Gets the height of the overlay after resize is done
         *
         * @return the height in pixels of the overlay
         */
        public String getHeight() {
            return height;
        }
    }

    @Override
    public void setWidth(String value) {
        width = value;
        setDimension(ElementConstants.STYLE_WIDTH, value);
    }

    @Override
    public void setMinWidth(String value) {
        minWidth = value;
        setDimension(ElementConstants.STYLE_MIN_WIDTH, value);
    }

    @Override
    public void setMaxWidth(String value) {
        maxWidth = value;
        setDimension(ElementConstants.STYLE_MAX_WIDTH, value);
    }

    @Override
    public void setHeight(String value) {
        height = value;
        setDimension(ElementConstants.STYLE_HEIGHT, value);
    }

    @Override
    public void setMinHeight(String value) {
        minHeight = value;
        setDimension(ElementConstants.STYLE_MIN_HEIGHT, value);
    }

    @Override
    public void setMaxHeight(String value) {
        maxHeight = value;
        setDimension(ElementConstants.STYLE_MAX_HEIGHT, value);
    }

    @Override
    public String getWidth() {
        return width;
    }

    @Override
    public String getMinWidth() {
        return minWidth;
    }

    @Override
    public String getMaxWidth() {
        return maxWidth;
    }

    @Override
    public String getHeight() {
        return height;
    }

    @Override
    public String getMinHeight() {
        return minHeight;
    }

    @Override
    public String getMaxHeight() {
        return maxHeight;
    }

    /**
     * Add a listener that controls whether the dialog should be closed or not.
     * <p>
     * The listener is informed when the user wants to close the dialog by
     * clicking outside the dialog, or by pressing escape. Then you can decide
     * whether to close or to keep opened the dialog. It means that dialog won't
     * be closed automatically unless you call {@link #close()} method
     * explicitly in the listener implementation.
     * <p>
     * NOTE: adding this listener changes behavior of the dialog. Dialog is
     * closed automatically in case there are no any close listeners. And the
     * {@link #close()} method should be called explicitly to close the dialog
     * in case there are close listeners.
     *
     * @see #close()
     *
     * @param listener
     * @return registration for removal of listener
     */
    public Registration addDialogCloseActionListener(
            ComponentEventListener<DialogCloseActionEvent> listener) {
        if (isOpened()) {
            ensureOnCloseConfigured();
        }
        Registration openedRegistration = getElement()
                .addPropertyChangeListener("opened", event -> {
                    if (isOpened()) {
                        ensureOnCloseConfigured();
                    } else {
                        onCloseConfigured = 0;
                    }
                });

        Registration registration = addListener(DialogCloseActionEvent.class,
                listener);
        return () -> {
            if (isOpened()) {
                // the count is decremented if the dialog is closed. So we
                // should decrement is explicitly if listener is deregistered
                onCloseConfigured--;
            }
            openedRegistration.remove();
            registration.remove();
        };
    }

    /**
     * Adds a listener that is called after user finishes resizing the overlay.
     * It is called only if resizing is enabled (see
     * {@link Dialog#setResizable(boolean)}).
     * <p>
     * Note: By default, the component will sync the width/height values after
     * every resizing.
     *
     * @param listener
     * @return registration for removal of listener
     */
    public Registration addResizeListener(
            ComponentEventListener<DialogResizeEvent> listener) {
        return addListener(DialogResizeEvent.class, listener);
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
        Objects.requireNonNull(components, "Components should not be null");
        for (Component component : components) {
            Objects.requireNonNull(component,
                    "Component to add cannot be null");
            container.appendChild(component.getElement());
        }
    }

    @Override
    public void remove(Component... components) {
        Objects.requireNonNull(components, "Components should not be null");
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

    @Override
    public void removeAll() {
        container.removeAllChildren();
    }

    /**
     * Adds the given component into this dialog at the given index.
     * <p>
     * The element in the DOM will not be child of the {@code <vaadin-dialog>}
     * element, but will be inserted into an overlay that is attached into the
     * {@code <body>}.
     *
     * @param index
     *            the index, where the component will be added.
     *
     * @param component
     *            the component to add
     */
    @Override
    public void addComponentAtIndex(int index, Component component) {
        Objects.requireNonNull(component, "Component should not be null");
        if (index < 0) {
            throw new IllegalArgumentException(
                    "Cannot add a component with a negative index");
        }
        // The case when the index is bigger than the children count is handled
        // inside the method below
        container.insertChild(index, component.getElement());
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
     * if necessary.
     */
    public void open() {
        setOpened(true);
    }

    /**
     * Closes the dialog.
     * <p>
     * Note: This method also removes the dialog component from the DOM after
     * closing it, unless you have added the component manually.
     */
    public void close() {
        setOpened(false);
    }

    /**
     * Sets whether component will open modal or modeless dialog.
     * <p>
     * Note: When dialog is set to be modeless, then it's up to you to provide
     * means for it to be closed (eg. a button that calls {@link Dialog#close()}).
     * The reason being that a modeless dialog allows user to interact with the
     * interface under it and won't be closed by clicking outside or the ESC key.
     *
     * @param modal
     *          {@code false} to enable dialog to open as modeless modal,
     *          {@code true} otherwise.
     */
    public void setModal(boolean modal) {
        getElement().setProperty("modeless", !modal);
    }

    /**
     * Gets whether component is set as modal or modeless dialog.
     *
     * @return  {@code true} if modal dialog (default),
     *          {@code false} otherwise.
     */
    public boolean isModal() {
        return !getElement().getProperty("modeless", false);
    }

    /**
     * Sets whether dialog is enabled to be dragged by the user or not.
     * <p>
     * To allow an element inside the dialog to be dragged by the user
     * (for instance, a header inside the dialog), a class {@code "draggable"}
     * can be added to it (see {@link HasStyle#addClassName(String)}).
     * <p>
     * Note: If draggable is enabled and dialog is opened without first
     * being explicitly attached to a parent, then it won't restore its
     * last position in the case the user closes and opens it again.
     * Reason being that a self attached dialog is removed from the DOM
     * when it's closed and position is not synched.
     *
     * @param draggable
     *          {@code true} to enable dragging of the dialog,
     *          {@code false} otherwise
     */
    public void setDraggable(boolean draggable) {
        getElement().setProperty("draggable", draggable);
    }

    /**
     * Gets whether dialog is enabled to be dragged or not.
     *
     * @return
     *      {@code true} if dragging is enabled,
     *      {@code false} otherwise (default).
     */
    public boolean isDraggable() {
        return getElement().getProperty("draggable", false);
    }

    /**
     * Sets whether dialog can be resized by user or not.
     *
     * @param resizable
     *          {@code true} to enabled resizing of the dialog,
     *          {@code false} otherwise.
     */
    public void setResizable(boolean resizable) {
        getElement().setProperty("resizable", resizable);
    }

    /**
     * Gets whether dialog is enabled to be resized or not.
     *
     * @return
     *      {@code true} if resizing is enabled,
     *      {@code falsoe} otherwiser (default).
     */
    public boolean isResizable() {
        return getElement().getProperty("resizable", false);
    }

    private UI getCurrentUI() {
        UI ui = UI.getCurrent();
        if (ui == null) {
            throw new IllegalStateException("UI instance is not available. "
                    + "It means that you are calling this method "
                    + "out of a normal workflow where it's always implicitely set. "
                    + "That may happen if you call the method from the custom thread without "
                    + "'UI::access' or from tests without proper initialization.");
        }
        return ui;
    }

    private void ensureAttached() {
        UI ui = getCurrentUI();
        ui.beforeClientResponse(ui, context -> {
            if (getElement().getNode().getParent() == null) {
                ui.add(this);
                autoAddedToTheUi = true;
            }
        });
    }

    private void ensureOnCloseConfigured() {
        if (onCloseConfigured == 0) {
            getElement().getNode()
                    .runWhenAttached(ui -> ui.beforeClientResponse(this,
                            context -> doEnsureOnCloseConfigured(ui)));
        }
        onCloseConfigured++;
    }

    private void doEnsureOnCloseConfigured(UI ui) {
        if (onCloseConfigured > 0) {
            ui.getPage().executeJs("var f = function(e) {"
                    + "  if (e.type == 'vaadin-overlay-escape-press' && !$0.noCloseOnEsc ||"
                    + "      e.type == 'vaadin-overlay-outside-click' && !$0.noCloseOnOutsideClick) {"
                    + "    e.preventDefault();"
                    + "    $0.dispatchEvent(new CustomEvent('vaadin-dialog-close-action'));"
                    + "  }" + "};"
                    + "$0.$.overlay.addEventListener('vaadin-overlay-outside-click', f);"
                    + "$0.$.overlay.addEventListener('vaadin-overlay-escape-press', f);"
                    + "$0.addEventListener('opened-changed', function(){"
                    + " if (!$0.opened) {"
                    + " $0.$.overlay.removeEventListener('vaadin-overlay-outside-click',f);"
                    + "$0.$.overlay.removeEventListener('vaadin-overlay-escape-press', f);"
                    + "} });", getElement());
        }
    }

    /**
     * Opens or closes the dialog.
     * <p>
     * Note: You don't need to add the dialog component anywhere before opening
     * it. Since {@code <vaadin-dialog>}'s location in the DOM doesn't really
     * matter, opening a dialog will automatically add it to the {@code <body>}
     * if necessary.
     *
     * @param opened
     *            {@code true} to open the dialog, {@code false} to close it
     */
    @Override
    public void setOpened(boolean opened) {
        if (opened) {
            ensureAttached();
        }
        super.setOpened(opened);
    }

    /**
     * Gets the open state from the dialog.
     *
     * @return the {@code opened} property from the dialog
     */
    public boolean isOpened() {
        return super.isOpenedBoolean();
    }

    @Override
    public Stream<Component> getChildren() {
        Builder<Component> childComponents = Stream.builder();
        container.getChildren().forEach(childElement -> ComponentUtil
                .findComponents(childElement, childComponents::add));
        return childComponents.build();
    }

    /**
     * Add a lister for event fired by the {@code opened-changed} events.
     *
     * @param: listener
     *             the listener to add;
     * @return: a Registration for removing the event listener
     */
    @Override
    public Registration addOpenedChangeListener(
            ComponentEventListener<OpenedChangeEvent<Dialog>> listener) {
        return super.addOpenedChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: To listen for opening the dialog, you should use
     * {@link #addOpenedChangeListener(ComponentEventListener)}.
     */
    @Override
    public Registration addAttachListener(
            ComponentEventListener<AttachEvent> listener) {
        return super.addAttachListener(listener);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: To listen for closing the dialog, you should use
     * {@link #addOpenedChangeListener(ComponentEventListener)}, as the
     * component is not necessarily removed from the DOM when closing.
     */
    @Override
    public Registration addDetachListener(
            ComponentEventListener<DetachEvent> listener) {
        return super.addDetachListener(listener);
    }


    /**
     * Adds theme variants to the component.
     *
     * @param variants
     *            theme variants to add
     */
    public void addThemeVariants(DialogVariant... variants) {
        getThemeNames()
                .addAll(Stream.of(variants)
                        .map(DialogVariant::getVariantName)
                        .collect(Collectors.toList()));
    }

    /**
     * Removes theme variants from the component.
     *
     * @param variants
     *            theme variants to remove
     */
    public void removeThemeVariants(DialogVariant... variants) {
        getThemeNames()
                .removeAll(Stream.of(variants)
                        .map(DialogVariant::getVariantName)
                        .collect(Collectors.toList()));
    }

    private void setDimension(String dimension, String value) {
        getElement()
            .executeJs("this.$.overlay.$.overlay.style[$0]=$1", dimension, value);
    }

    private void attachComponentRenderer() {
        String appId = UI.getCurrent().getInternals().getAppId();
        int nodeId = container.getNode().getId();
        String renderer = String.format(
                "<flow-component-renderer appid=\"%s\" nodeid=\"%s\" style=\"display: flex; height: 100%%;\"></flow-component-renderer>",
                appId, nodeId);
        template.setProperty("innerHTML", renderer);

        setDimension(ElementConstants.STYLE_WIDTH, width);
        setDimension(ElementConstants.STYLE_MIN_WIDTH, minWidth);
        setDimension(ElementConstants.STYLE_MAX_WIDTH, maxWidth);
        setDimension(ElementConstants.STYLE_HEIGHT, height);
        setDimension(ElementConstants.STYLE_MIN_HEIGHT, minHeight);
        setDimension(ElementConstants.STYLE_MAX_HEIGHT, maxHeight);
    }
}
