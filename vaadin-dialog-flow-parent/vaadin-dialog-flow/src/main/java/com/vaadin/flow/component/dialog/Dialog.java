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
package com.vaadin.flow.component.dialog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.internal.OverlayAutoAddController;
import com.vaadin.flow.component.shared.internal.OverlayClassListProxy;
import com.vaadin.flow.dom.ClassList;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementConstants;
import com.vaadin.flow.dom.ElementDetachEvent;
import com.vaadin.flow.dom.ElementDetachListener;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.shared.Registration;

/**
 * A Dialog is a small window that can be used to present information and user
 * interface elements in an overlay.
 * <p>
 * Dialogs can be made modal or non-modal. A modal Dialog blocks the user from
 * interacting with the rest of the user interface while the Dialog is open, as
 * opposed to a non-modal Dialog, which does not block interaction.
 * <p>
 * Dialogs can be made draggable and resizable. When draggable, the user is able
 * to move them around using a pointing device. It is recommended to make
 * non-modal Dialogs draggable so that the user can interact with content that
 * might otherwise be obscured by the Dialog. A resizable Dialog allows the user
 * to resize the Dialog by dragging from the edges of the Dialog with a pointing
 * device. Dialogs are not resizable by default.
 * <p>
 * Dialogs automatically become scrollable when their content overflows. Custom
 * scrollable areas can be created using the Scroller component.
 * <p>
 * Best Practices:<br>
 * Dialogs are disruptive by nature and should be used sparingly. Do not use
 * them to communicate nonessential information, such as success messages like
 * “Logged in”, “Copied”, and so on. Instead, use Notifications when
 * appropriate.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-dialog")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/dialog", version = "24.8.0-alpha18")
@JsModule("@vaadin/dialog/src/vaadin-dialog.js")
@JsModule("./flow-component-renderer.js")
public class Dialog extends Component implements HasComponents, HasSize,
        HasStyle, HasThemeVariant<DialogVariant> {

    private static final String OVERLAY_LOCATOR_JS = "this.$.overlay";

    private int configuredCloseActionListeners;
    private String minWidth;
    private String maxWidth;
    private String minHeight;
    private String maxHeight;
    private DialogHeader dialogHeader;
    private DialogFooter dialogFooter;

    /**
     * Creates an empty dialog.
     */
    public Dialog() {
        // Needs to be updated on each
        // attach, as element depends on node id which is subject to change if
        // the dialog is transferred to another UI, e.g. due to
        // @PreserveOnRefresh
        getElement().getNode().addAttachListener(this::attachComponentRenderer);

        // Workaround for: https://github.com/vaadin/flow/issues/3496
        getElement().setProperty("opened", false);

        getElement().addPropertyChangeListener("opened", event -> {
            // Only handle client-side changes, server-side changes are already
            // handled by setOpened
            if (event.isUserOriginated()) {
                doSetOpened(this.isOpened(), event.isUserOriginated());
            }
        });

        addListener(DialogResizeEvent.class, event -> {
            setWidth(event.getWidth());
            setHeight(event.getHeight());
            setTop(event.getTop());
            setLeft(event.getLeft());
        });

        addListener(DialogDraggedEvent.class, event -> {
            setTop(event.getTop());
            setLeft(event.getLeft());
        });

        setOverlayRole("dialog");

        // Initialize auto-add behavior
        new OverlayAutoAddController<>(this, this::isModal);
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
     * Gets the top position of the overlay.
     *
     * @return the top position of the overlay
     */
    public String getTop() {
        return getElement().getProperty("top");
    }

    /**
     * Sets the top position of the overlay. If a unitless number is provided,
     * pixels are assumed.
     * <p>
     * Note that the overlay top edge may not be the same as the viewport top
     * edge (e.g. the "Lumo" theme defines some spacing to prevent the overlay
     * from stretching all the way to the top of the viewport).
     *
     * @param top
     *            the top position of the overlay
     */
    public void setTop(String top) {
        getElement().setProperty("top", top);
    }

    /**
     * Gets the left position of the overlay.
     *
     * @return the left position of the overlay
     */
    public String getLeft() {
        return getElement().getProperty("left");
    }

    /**
     * Sets the distance of the overlay from the left of its container. If a
     * unitless number is provided, pixels are assumed.
     * <p>
     * Note that the overlay left edge may not be the same as the viewport left
     * edge (e.g. the "Lumo" theme defines some spacing to prevent the overlay
     * from stretching all the way to the left of the viewport).
     *
     * @param left
     *            the left position of the overlay
     */
    public void setLeft(String left) {
        getElement().setProperty("left", left);
    }

    /**
     * `resize` event is sent when the user finishes resizing the overlay.
     */
    @DomEvent("resize")
    public static class DialogResizeEvent extends ComponentEvent<Dialog> {

        private final String width;
        private final String height;
        private final String left;
        private final String top;

        public DialogResizeEvent(Dialog source, boolean fromClient,
                @EventData("event.detail.width") String width,
                @EventData("event.detail.height") String height,
                @EventData("event.detail.left") String left,
                @EventData("event.detail.top") String top) {
            super(source, fromClient);
            this.width = width;
            this.height = height;
            this.left = left;
            this.top = top;
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

        /**
         * Gets the left position of the overlay after resize is done
         *
         * @return the left position in pixels of the overlay
         */
        public String getLeft() {
            return left;
        }

        /**
         * Gets the top position of the overlay after resize is done
         *
         * @return the top position in pixels of the overlay
         */
        public String getTop() {
            return top;
        }
    }

    /**
     * `dragged` event is sent when the user finishes dragging the overlay.
     */
    @DomEvent("dragged")
    public static class DialogDraggedEvent extends ComponentEvent<Dialog> {
        private final String left;
        private final String top;

        public DialogDraggedEvent(Dialog source, boolean fromClient,
                @EventData("event.detail.left") String left,
                @EventData("event.detail.top") String top) {
            super(source, fromClient);
            this.left = left;
            this.top = top;
        }

        /**
         * Gets the left position of the overlay after dragging is done
         *
         * @return the left position in pixels of the overlay
         */
        public String getLeft() {
            return left;
        }

        /**
         * Gets the top position of the overlay after dragging is done
         *
         * @return the top position in pixels of the overlay
         */
        public String getTop() {
            return top;
        }
    }

    /**
     * {@code opened-changed} event is sent when the overlay opened state
     * changes.
     */
    public static class OpenedChangeEvent extends ComponentEvent<Dialog> {
        private final boolean opened;

        public OpenedChangeEvent(Dialog source, boolean fromClient) {
            super(source, fromClient);
            this.opened = source.isOpened();
        }

        public boolean isOpened() {
            return opened;
        }
    }

    @Override
    public void setWidth(String value) {
        getElement().setProperty("width", value);
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
        getElement().setProperty("height", value);
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
        return getElement().getProperty("width");
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
        return getElement().getProperty("height");
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
     * @param listener
     *            the listener to add
     * @return registration for removal of listener
     * @see #close()
     */
    public Registration addDialogCloseActionListener(
            ComponentEventListener<DialogCloseActionEvent> listener) {
        if (isOpened()) {
            configuredCloseActionListeners++;
        }

        Registration openedRegistration = addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                configuredCloseActionListeners++;
            } else {
                configuredCloseActionListeners = 0;
            }
        });

        Registration registration = addListener(DialogCloseActionEvent.class,
                listener);
        return () -> {
            if (isOpened()) {
                // the count is decremented if the dialog is closed. So we
                // should decrement is explicitly if listener is deregistered
                configuredCloseActionListeners--;
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
     * Note: By default, the component will sync the width/height and top/left
     * values after every resizing.
     *
     * @param listener
     *            the listener to add
     * @return registration for removal of listener
     */
    public Registration addResizeListener(
            ComponentEventListener<DialogResizeEvent> listener) {
        return addListener(DialogResizeEvent.class, listener);
    }

    /**
     * Adds a listener that is called after user finishes dragging the overlay.
     * It is called only if dragging is enabled (see
     * {@link Dialog#setDraggable(boolean)}).
     * <p>
     * Note: By default, the component will sync the top/left values after every
     * dragging.
     *
     * @param listener
     *            the listener to add
     * @return registration for removal of listener
     */
    public Registration addDraggedListener(
            ComponentEventListener<DialogDraggedEvent> listener) {
        return addListener(DialogDraggedEvent.class, listener);
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
     * Creates a dialog with given title.
     *
     * @param title
     *            the title of the component
     */
    public Dialog(String title) {
        this();
        setHeaderTitle(title);
    }

    /**
     * Creates a dialog with given title and components inside.
     *
     * @param title
     *            the title of the component
     * @param components
     *            the components inside the dialog
     */
    public Dialog(String title, Component... components) {
        this(components);
        setHeaderTitle(title);
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
    public void add(Collection<Component> components) {
        HasComponents.super.add(components);

        updateVirtualChildNodeIds();
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
     * @param component
     *            the component to add
     */
    @Override
    public void addComponentAtIndex(int index, Component component) {
        HasComponents.super.addComponentAtIndex(index, component);

        updateVirtualChildNodeIds();
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
     * If a dialog was not added manually to a parent component, it will be
     * automatically added to the {@link UI} when opened, and automatically
     * removed from the UI when closed. Note that the dialog is then scoped to
     * the UI, and not the current view. As such, when navigating away from a
     * view, the dialog will still be opened or stay open. In order to close the
     * dialog when navigating away from a view, it should either be explicitly
     * added as a child to the view, or it should be explicitly closed when
     * leaving the view.
     */
    public void open() {
        setOpened(true);
    }

    /**
     * Closes the dialog.
     * <p>
     * This automatically removes the dialog from the {@link UI}, unless it was
     * manually added to a parent component.
     */
    public void close() {
        setOpened(false);
    }

    /**
     * Sets whether component will open modal or modeless dialog.
     * <p>
     * Note: When dialog is set to be modeless, then it's up to you to provide
     * means for it to be closed (eg. a button that calls
     * {@link Dialog#close()}). The reason being that a modeless dialog allows
     * user to interact with the interface under it and won't be closed by
     * clicking outside or the ESC key.
     *
     * @param modal
     *            {@code false} to enable dialog to open as modeless modal,
     *            {@code true} otherwise.
     */
    public void setModal(boolean modal) {
        getElement().setProperty("modeless", !modal);
        getUI().ifPresent(ui -> ui.setChildComponentModal(this, modal));
    }

    /**
     * Gets whether component is set as modal or modeless dialog.
     *
     * @return {@code true} if modal dialog (default), {@code false} otherwise.
     */
    public boolean isModal() {
        return !getElement().getProperty("modeless", false);
    }

    /**
     * Sets whether dialog is enabled to be dragged by the user or not.
     * <p>
     * To allow an element inside the dialog to be dragged by the user (for
     * instance, a header inside the dialog), a class {@code "draggable"} can be
     * added to it (see {@link HasStyle#addClassName(String)}).
     * <p>
     * Note: If draggable is enabled and dialog is opened without first being
     * explicitly attached to a parent, then it won't restore its last position
     * in the case the user closes and opens it again. Reason being that a self
     * attached dialog is removed from the DOM when it's closed and position is
     * not synched.
     *
     * @param draggable
     *            {@code true} to enable dragging of the dialog, {@code false}
     *            otherwise
     */
    public void setDraggable(boolean draggable) {
        getElement().setProperty("draggable", draggable);
    }

    /**
     * Gets whether dialog is enabled to be dragged or not.
     *
     * @return {@code true} if dragging is enabled, {@code false} otherwise
     *         (default).
     */
    public boolean isDraggable() {
        return getElement().getProperty("draggable", false);
    }

    /**
     * Sets whether dialog can be resized by user or not.
     *
     * @param resizable
     *            {@code true} to enabled resizing of the dialog, {@code false}
     *            otherwise.
     */
    public void setResizable(boolean resizable) {
        getElement().setProperty("resizable", resizable);
    }

    /**
     * Gets whether dialog is enabled to be resized or not.
     *
     * @return {@code true} if resizing is enabled, {@code falsoe} otherwiser
     *         (default).
     */
    public boolean isResizable() {
        return getElement().getProperty("resizable", false);
    }

    /**
     * Sets the title to be rendered on the dialog header.
     *
     * @param title
     *            title to be rendered
     */
    public void setHeaderTitle(String title) {
        getElement().setProperty("headerTitle", title);
    }

    /**
     * Gets the title set for the dialog header.
     *
     * @return the title or an empty string, if a header title is not defined.
     */
    public String getHeaderTitle() {
        return getElement().getProperty("headerTitle", "");
    }

    /**
     * Gets the object from which components can be added or removed from the
     * dialog header area. The header is displayed only if there's a
     * {@link #getHeaderTitle()} or at least one component added with
     * {@link DialogHeaderFooter#add(Component...)}.
     *
     * @return the header object
     */
    public DialogHeader getHeader() {
        if (this.dialogHeader == null) {
            this.dialogHeader = new DialogHeader(this);
        }
        return this.dialogHeader;
    }

    /**
     * Gets the object from which components can be added or removed from the
     * dialog footer area. The footer is displayed only if there's at least one
     * component added with {@link DialogHeaderFooter#add(Component...)}.
     *
     * @return the header object
     */
    public DialogFooter getFooter() {
        if (this.dialogFooter == null) {
            this.dialogFooter = new DialogFooter(this);
        }
        return this.dialogFooter;
    }

    /**
     * Class for adding and removing components to the header part of a dialog.
     */
    final public static class DialogHeader extends DialogHeaderFooter {
        private DialogHeader(Dialog dialog) {
            super("headerRenderer", dialog);
        }
    }

    /**
     * Class for adding and removing components to the footer part of a dialog.
     */
    final public static class DialogFooter extends DialogHeaderFooter {
        private DialogFooter(Dialog dialog) {
            super("footerRenderer", dialog);
        }
    }

    /**
     * This class defines the common behavior for adding/removing components to
     * the header and footer parts. It also creates the root element where the
     * components will be attached to as well as the renderer function used by
     * the dialog.
     */
    abstract static class DialogHeaderFooter implements HasComponents {
        protected final Element root;
        private final String rendererFunction;
        private final Component dialog;
        boolean rendererCreated = false;

        protected DialogHeaderFooter(String rendererFunction,
                Component dialog) {
            this.rendererFunction = rendererFunction;
            this.dialog = dialog;
            root = new Element("div");
            root.getStyle().set("display", "contents");
        }

        @Override
        public void add(Component... components) {
            HasComponents.super.add(components);
            updateRendererState();
        }

        @Override
        public void add(Collection<Component> components) {
            HasComponents.super.add(components);
            updateRendererState();
        }

        @Override
        public void add(String text) {
            HasComponents.super.add(text);
            updateRendererState();
        }

        @Override
        public void remove(Component... components) {
            HasComponents.super.remove(components);
            updateRendererState();
        }

        @Override
        public void remove(Collection<Component> components) {
            HasComponents.super.remove(components);
            updateRendererState();
        }

        @Override
        public void removeAll() {
            HasComponents.super.removeAll();
            updateRendererState();
        }

        @Override
        public void addComponentAtIndex(int index, Component component) {
            HasComponents.super.addComponentAtIndex(index, component);
            updateRendererState();
        }

        @Override
        public void addComponentAsFirst(Component component) {
            HasComponents.super.addComponentAsFirst(component);
            updateRendererState();
        }

        private void updateRendererState() {
            if (root.getChildCount() == 0) {
                removeRenderer();
            } else if (!isRendererCreated()) {
                initRenderer();
            }
        }

        /**
         * Method called to create the renderer function using
         * {@link #rendererFunction} as the property name.
         */
        void initRenderer() {
            if (root.getChildCount() == 0) {
                return;
            }
            if (!dialog.getElement().equals(root.getParent())) {
                dialog.getElement().appendVirtualChild(root);
            }
            dialog.getElement().executeJs("this." + rendererFunction
                    + " = (root) => {" + "if (root.firstChild) { "
                    + "   return;" + "}" + "root.appendChild($0);" + "}", root);
            setRendererCreated(true);
        }

        private void removeRenderer() {
            dialog.getElement()
                    .executeJs("this." + rendererFunction + " = null;");
            setRendererCreated(false);
        }

        /**
         * Gets whether the renderer function exists or not
         *
         * @return the renderer function state
         */
        boolean isRendererCreated() {
            return rendererCreated;
        }

        /**
         * Sets the renderer function creation state. To avoid making a
         * JavaScript execution to get the information from the client, this is
         * done on the server by setting it to <code>true</code> on
         * {@link #initRenderer()} and to <code>false</code> when the last child
         * is removed in {@link #remove(Component...)} or when an auto attached
         * dialog is closed.
         *
         * @param rendererCreated
         */
        void setRendererCreated(boolean rendererCreated) {
            this.rendererCreated = rendererCreated;
        }

        @Override
        public Element getElement() {
            return root;
        }
    }

    /**
     * Set the visibility of the dialog.
     * <p>
     * For a modal dialog the server-side modality will be removed when dialog
     * is not visible so that interactions can be made in the application.
     *
     * @param visible
     *            dialog visibility
     * @see Component#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        getUI().ifPresent(
                ui -> ui.setChildComponentModal(this, visible && isModal()));
    }

    /**
     * Registers event listeners on the dialog's overlay that prevent it from
     * closing itself on outside click and escape press. Instead, the event
     * listeners delegate to the server-side {@link #handleClientClose()}
     * method. This serves two purposes:
     * <ul>
     * <li>Prevent the client overlay from closing if a custom close action
     * listener is registered</li>
     * <li>Prevent the client overlay from closing if the server-side dialog has
     * become inert in the meantime, in which case the @ClientCallable call to
     * {@link #handleClientClose()} will never be processed</li>
     * </ul>
     */
    private void registerClientCloseHandler() {
        //@formatter:off
        getElement().executeJs("const listener = (e) => {"
                + "  if (e.type == 'vaadin-overlay-escape-press' && !this.noCloseOnEsc ||"
                + "      e.type == 'vaadin-overlay-outside-click' && !this.noCloseOnOutsideClick) {"
                + "    e.preventDefault();"
                + "    this.$server.handleClientClose();"
                + "  }"
                + "};"
                + "this.$.overlay.addEventListener('vaadin-overlay-outside-click', listener);"
                + "this.$.overlay.addEventListener('vaadin-overlay-escape-press', listener);");
        //@formatter:on
    }

    @ClientCallable
    void handleClientClose() {
        if (!isOpened()) {
            return;
        }

        if (configuredCloseActionListeners > 0) {
            fireEvent(new DialogCloseActionEvent(this, true));
        } else {
            doSetOpened(false, true);
        }
    }

    /**
     * Opens or closes the dialog.
     * <p>
     * If a dialog was not added manually to a parent component, it will be
     * automatically added to the {@link UI} when opened, and automatically
     * removed from the UI when closed. Note that the dialog is then scoped to
     * the UI, and not the current view. As such, when navigating away from a
     * view, the dialog will still be opened or stay open. In order to close the
     * dialog when navigating away from a view, it should either be explicitly
     * added as a child to the view, or it should be explicitly closed when
     * leaving the view.
     *
     * @param opened
     *            {@code true} to open the dialog, {@code false} to close it
     */
    public void setOpened(boolean opened) {
        if (opened != isOpened()) {
            doSetOpened(opened, false);
        }
    }

    private void doSetOpened(boolean opened, boolean fromClient) {
        setModality(opened && isModal());
        getElement().setProperty("opened", opened);
        fireEvent(new OpenedChangeEvent(this, fromClient));
    }

    /**
     * Gets the open state from the dialog.
     *
     * @return the {@code opened} property from the dialog
     */
    @Synchronize(property = "opened", value = "opened-changed", allowInert = true)
    public boolean isOpened() {
        return getElement().getProperty("opened", false);
    }

    private void setModality(boolean modal) {
        if (isAttached()) {
            getUI().ifPresent(ui -> ui.setChildComponentModal(this, modal));
        }
    }

    /**
     * Add a lister for event fired by the {@code opened-changed} events.
     *
     * @param listener
     *            the listener to add
     * @return a Registration for removing the event listener
     */
    public Registration addOpenedChangeListener(
            ComponentEventListener<OpenedChangeEvent> listener) {
        return addListener(OpenedChangeEvent.class, listener);
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

    private Map<Element, Registration> childDetachListenerMap = new HashMap<>();
    // Must not use lambda here as that would break serialization. See
    // https://github.com/vaadin/flow-components/issues/5597
    private ElementDetachListener childDetachListener = new ElementDetachListener() {
        @Override
        public void onDetach(ElementDetachEvent e) {
            var child = e.getSource();
            var childDetachedFromContainer = !getElement().getChildren()
                    .anyMatch(containerChild -> Objects.equals(child,
                            containerChild));

            if (childDetachedFromContainer) {
                // The child was removed from the dialog

                // Remove the registration for the child detach listener
                childDetachListenerMap.get(child).remove();
                childDetachListenerMap.remove(child);

                updateVirtualChildNodeIds();
            }
        }
    };

    /**
     * Updates the virtualChildNodeIds property of the dialog element.
     * <p>
     * This method is called whenever the dialog's child components change.
     * <p>
     * Also calls {@code requestContentUpdate} on the dialog element to trigger
     * the content update.
     */
    private void updateVirtualChildNodeIds() {
        // Add detach listeners (child may be removed with removeFromParent())
        getElement().getChildren().forEach(child -> {
            if (!childDetachListenerMap.containsKey(child)) {
                childDetachListenerMap.put(child,
                        child.addDetachListener(childDetachListener));
            }
        });

        this.getElement().setPropertyList("virtualChildNodeIds",
                getElement().getChildren()
                        .map(element -> element.getNode().getId())
                        .collect(Collectors.toList()));

        this.getElement().callJsFunction("requestContentUpdate");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // vaadin/flow#7799,vaadin/vaadin-dialog#229
        // as the locator is stored inside component's attributes, no need to
        // remove the data as it should live as long as the component does
        Shortcuts.setShortcutListenOnElement(OVERLAY_LOCATOR_JS, this);
        initHeaderFooterRenderer();
        updateVirtualChildNodeIds();
        registerClientCloseHandler();
    }

    /**
     * Sets the ARIA role for the overlay element, used by screen readers.
     *
     * @param role
     *            the role to set
     */
    public void setOverlayRole(String role) {
        Objects.requireNonNull(role, "Role cannot be null");

        getElement().setProperty("overlayRole", role);
    }

    /**
     * Gets the ARIA role for the overlay element, used by screen readers.
     * Defaults to {@code dialog}.
     *
     * @return the role
     */
    public String getOverlayRole() {
        return getElement().getProperty("overlayRole");
    }

    /**
     * Set the {@code aria-label} attribute for assistive technologies like
     * screen readers. An {@code undefined} value for this property (the
     * default) means that the {@code aria-label} attribute is not present at
     * all.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     *
     * @return the {@code ariaLabel} property from the webcomponent
     */
    protected String getAriaLabel() {
        return getElement().getProperty("ariaLabel");
    }

    /**
     * Set the {@code aria-label} attribute for assistive technologies like
     * screen readers. An {@code undefined} value for this property (the
     * default) means that the {@code aria-label} attribute is not present at
     * all.
     *
     * @param ariaLabel
     *            the String value to set
     */
    protected void setAriaLabel(String ariaLabel) {
        getElement().setProperty("ariaLabel",
                ariaLabel == null ? "" : ariaLabel);
    }

    private void initHeaderFooterRenderer() {
        if (dialogHeader != null) {
            dialogHeader.setRendererCreated(false);
            dialogHeader.initRenderer();
        }
        if (dialogFooter != null) {
            dialogFooter.setRendererCreated(false);
            dialogFooter.initRenderer();
        }
    }

    private void setDimension(String dimension, String value) {
        getElement().executeJs(OVERLAY_LOCATOR_JS + ".$.overlay.style[$0]=$1",
                dimension, value);
    }

    private void attachComponentRenderer() {
        this.getElement().executeJs(
                "Vaadin.FlowComponentHost.patchVirtualContainer(this)");

        String appId = UI.getCurrent().getInternals().getAppId();

        getElement().executeJs(
                "this.renderer = (root) => Vaadin.FlowComponentHost.setChildNodes($0, this.virtualChildNodeIds, root)",
                appId);

        setDimension(ElementConstants.STYLE_MIN_WIDTH, minWidth);
        setDimension(ElementConstants.STYLE_MAX_WIDTH, maxWidth);
        setDimension(ElementConstants.STYLE_MIN_HEIGHT, minHeight);
        setDimension(ElementConstants.STYLE_MAX_HEIGHT, maxHeight);
    }

    /**
     * Sets the CSS class names of the dialog overlay element. This method
     * overwrites any previous set class names.
     *
     * @param className
     *            a space-separated string of class names to set, or
     *            <code>null</code> to remove all class names
     */
    @Override
    public void setClassName(String className) {
        getClassNames().clear();
        if (className != null) {
            addClassNames(className.split(" "));
        }
    }

    @Override
    public ClassList getClassNames() {
        return new OverlayClassListProxy(this);
    }

    /**
     * @throws UnsupportedOperationException
     *             Dialog does not support adding styles to overlay
     */
    @Override
    public Style getStyle() {
        throw new UnsupportedOperationException(
                "Dialog does not support adding styles to overlay");
    }
}
